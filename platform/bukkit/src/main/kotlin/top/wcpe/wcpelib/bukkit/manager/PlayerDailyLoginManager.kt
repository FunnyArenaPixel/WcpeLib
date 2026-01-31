package top.wcpe.wcpelib.bukkit.manager

import top.wcpe.wcpelib.bukkit.data.IPlayerDailyLoginDataManager
import top.wcpe.wcpelib.bukkit.entity.PlayerDailyLoginData
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PlayerDailyLoginManager(
    private val dataManager: IPlayerDailyLoginDataManager,
) {
    private val cache = ConcurrentHashMap<LocalDate, DailyLoginCache>()

    fun record(uuid: UUID, statDate: LocalDate = LocalDate.now()) {
        val cacheEntry = cache.computeIfAbsent(statDate) { loadCache(statDate) }
        cacheEntry.lock.withLock {
            if (cacheEntry.playerUuids.add(uuid)) {
                persist(statDate, cacheEntry.playerUuids)
            }
        }
    }

    fun hasLogged(uuid: UUID, statDate: LocalDate = LocalDate.now()): Boolean {
        val cacheEntry = cache.computeIfAbsent(statDate) { loadCache(statDate) }
        return cacheEntry.lock.withLock {
            cacheEntry.playerUuids.contains(uuid)
        }
    }

    fun list(statDate: LocalDate = LocalDate.now()): Set<UUID> {
        val cacheEntry = cache.computeIfAbsent(statDate) { loadCache(statDate) }
        return cacheEntry.lock.withLock {
            cacheEntry.playerUuids.toSet()
        }
    }

    private fun loadCache(statDate: LocalDate): DailyLoginCache {
        val data = dataManager.getPlayerDailyLoginData(statDate)
        val set = parse(data.playerUuids)
        return DailyLoginCache(ReentrantLock(), set)
    }

    private fun persist(statDate: LocalDate, uuids: Set<UUID>) {
        val data = PlayerDailyLoginData(statDate = statDate, playerUuids = serialize(uuids))
        dataManager.savePlayerDailyLoginData(data)
    }

    private fun parse(raw: String): MutableSet<UUID> {
        if (raw.isBlank()) return mutableSetOf()
        return raw.split(",").mapNotNull { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            .toMutableSet()
    }

    private fun serialize(set: Set<UUID>): String {
        return set.joinToString(",") { it.toString() }
    }
}

private data class DailyLoginCache(
    val lock: ReentrantLock,
    val playerUuids: MutableSet<UUID>,
)
