package top.wcpe.wcpelib.bukkit.manager

import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.slf4j.LoggerFactory
import top.wcpe.wcpelib.bukkit.data.IPlayerOnlineDataManager
import top.wcpe.wcpelib.bukkit.entity.PlayerOnlineData
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

// 异步刷新的周期：60 秒
private const val ONLINE_UPDATE_PERIOD_TICKS = 20L * 60
private val MINUTE_MILLIS = TimeUnit.MINUTES.toMillis(1)

class PlayerOnlineManager(
    private val dataManager: IPlayerOnlineDataManager,
    private val plugin: JavaPlugin,
) {
    // 在线会话缓存：一人一锁，避免全局锁
    private val logger = LoggerFactory.getLogger(javaClass)
    private val zoneId = ZoneId.systemDefault()
    private val sessions = ConcurrentHashMap<UUID, PlayerOnlineSession>()
    private var task: BukkitTask? = null

    fun start() {
        stopTask()
        task = PlayerOnlineTickTask(this).runTaskTimerAsynchronously(
            plugin,
            ONLINE_UPDATE_PERIOD_TICKS,
            ONLINE_UPDATE_PERIOD_TICKS
        )
    }

    fun shutdown() {
        stopTask()
        settleAll(System.currentTimeMillis())
        sessions.clear()
    }

    fun recordJoin(player: Player) {
        val uuid = player.uniqueId
        val now = System.currentTimeMillis()
        sessions.compute(uuid) { _, exist ->
            val lock = exist?.lock ?: ReentrantLock()
            val todayData = dataManager.getPlayerOnlineData(uuid, LocalDate.now())
            PlayerOnlineSession(
                playerUuid = uuid,
                data = todayData,
                lastTimestamp = now,
                pendingMillis = 0,
                lock = lock,
            )
        }
    }

    fun recordQuit(player: Player) {
        settle(player.uniqueId, System.currentTimeMillis(), true)
    }

    fun addFlag(playerUuid: UUID, flag: String): Boolean {
        val session = sessions[playerUuid]
        return if (session != null) {
            session.lock.withLock {
                val added = session.data.addFlag(flag)
                if (added) {
                    dataManager.savePlayerOnlineData(session.data)
                }
                added
            }
        } else {
            val data = dataManager.getPlayerOnlineData(playerUuid, LocalDate.now())
            val added = data.addFlag(flag)
            if (added) {
                dataManager.savePlayerOnlineData(data)
            }
            added
        }
    }

    fun hasFlag(playerUuid: UUID, flag: String): Boolean {
        val session = sessions[playerUuid]
        return if (session != null) {
            session.lock.withLock {
                session.data.hasFlag(flag)
            }
        } else {
            val data = dataManager.getPlayerOnlineData(playerUuid, LocalDate.now())
            data.hasFlag(flag)
        }
    }

    fun hasFlag(playerUuid: UUID, flag: String, startDate: LocalDate, endDate: LocalDate): Boolean {
        require(!endDate.isBefore(startDate)) { "startDate must not be after endDate" }
        val trimmedFlag = flag.trim()
        if (trimmedFlag.isEmpty()) {
            return false
        }

        val session = sessions[playerUuid]
        if (session != null) {
            val statDate = session.data.statDate
            if (!statDate.isBefore(startDate) && !statDate.isAfter(endDate)) {
                session.lock.withLock {
                    if (session.data.hasFlag(trimmedFlag)) {
                        return true
                    }
                }
            }
        }

        return dataManager.listPlayerOnlineData(playerUuid, startDate, endDate)
            .any { it.hasFlag(trimmedFlag) }
    }

    internal fun tick(nowMillis: Long = System.currentTimeMillis()) {
        settleAll(nowMillis)
    }

    private fun settleAll(nowMillis: Long) {
        sessions.keys.forEach { uuid ->
            settle(uuid, nowMillis, false)
        }
    }

    private fun settle(uuid: UUID, nowMillis: Long, removeSession: Boolean) {
        val session = sessions[uuid] ?: return
        session.lock.withLock {
            if (nowMillis <= session.lastTimestamp) {
                if (removeSession) {
                    sessions.remove(uuid)
                }
                return
            }
            distributeOnlineMinutes(session, nowMillis)
            if (removeSession) {
                sessions.remove(uuid)
            }
        }
    }

    private fun distributeOnlineMinutes(session: PlayerOnlineSession, nowMillis: Long) {
        var cursor = session.lastTimestamp
        var data = session.data
        var pendingMillis = session.pendingMillis
        while (cursor < nowMillis) {
            // 计算当前分片（跨天则截断到当天 24 点）
            val dayEnd = data.statDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val sliceEnd = minOf(nowMillis, dayEnd)
            pendingMillis += sliceEnd - cursor
            val minutesLong = pendingMillis / MINUTE_MILLIS
            val minutes = minutesLong.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            if (minutes > 0) {
                // 只在满 1 分钟时落库，剩余毫秒累积到下一次 tick
                pendingMillis -= MINUTE_MILLIS * minutes
                val newMinutes = safeAddMinutes(data.onlineMinutes, minutes)
                data.onlineMinutes = newMinutes
                val saved = dataManager.savePlayerOnlineData(data)
                if (!saved) {
                    logger.warn("保存玩家 [{}] {} 在线时长失败", data.playerUuid, data.statDate)
                }
            }
            cursor = sliceEnd

            if (sliceEnd >= dayEnd && nowMillis > dayEnd) {
                // 跨天，切换到下一天的记录继续结算
                data = dataManager.getPlayerOnlineData(data.playerUuid, data.statDate.plusDays(1))
            } else {
                break
            }
        }
        session.lastTimestamp = cursor
        session.data = data
        session.pendingMillis = pendingMillis
    }

    private fun stopTask() {
        task?.cancel()
        task = null
    }

    private fun safeAddMinutes(current: Int, increment: Int): Int {
        val result = current.toLong() + increment
        if (result > Int.MAX_VALUE) {
            logger.warn("在线分钟数溢出，当前累积:{}，强行截断为 Int.MAX_VALUE", result)
            return Int.MAX_VALUE
        }
        return result.toInt()
    }

    fun getPlayerOnlineData(playerUuid: UUID): PlayerOnlineData? {
        return sessions[playerUuid]?.data
    }

    fun addLoginCount(playerUuid: UUID) {
        val session = sessions[playerUuid] ?: return
        val data = session.data
        session.lock.withLock {
            session.data.loginCount++
        }

        val saved = dataManager.savePlayerOnlineData(data)
        if (!saved) {
            logger.warn("保存玩家 [{}] {} 在线时长失败", data.playerUuid, data.statDate)
        }
    }
}

private data class PlayerOnlineSession(
    val playerUuid: UUID,
    var data: PlayerOnlineData,
    var lastTimestamp: Long,
    var pendingMillis: Long,
    val lock: ReentrantLock,
)
