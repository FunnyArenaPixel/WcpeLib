package top.wcpe.wcpelib.bukkit

import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import top.wcpe.wcpelib.bukkit.entity.PlayerData

class WcpeLibListener : Listener {
    private val logger = WcpeLib.instance.logger

    @EventHandler
    fun listenerPlayerJoinEvent(e: PlayerJoinEvent) {
        WcpeLib.pluginScope.launch {
            val player = e.player
            WcpeLib.playerOnlineManager.recordJoin(player)
            WcpeLib.playerOnlineManager.addLoginCount(player.uniqueId)
            WcpeLib.playerDailyLoginManager.record(player.uniqueId)
            val firstPlayed = player.firstPlayed

            val playerData = WcpeLib.dataManager.getPlayerDataByName(player.name)
            if (playerData == null) {
                logger.info("由于玩家: [${player.name}] 未上线过 自动读取本地 firstPlayed: [$firstPlayed] 填入")
                WcpeLib.dataManager.savePlayerData(
                    PlayerData(
                        playerName = player.name,
                        uuid = player.uniqueId.toString(),
                        firstLoginTime = if (firstPlayed <= 0) {
                            logger.info("firstPlayed 为空以当前时间为注册时间!")
                            System.currentTimeMillis()
                        } else {
                            logger.info("firstPlayed 为[${firstPlayed}] 以这个时间为注册时间!")
                            firstPlayed
                        },
                        loginOutXYZ = ""
                    )
                )
                return@launch
            }
            synchronized(playerData) {
                logger.info("玩家: [${player.name}] 上一次进入的服务器 [${playerData.lastServerName}]")
                playerData.lastServerName = WcpeLib.getServerName()
                playerData.lastLoginTime = System.currentTimeMillis()
                WcpeLib.dataManager.savePlayerData(playerData)
            }
        }
    }

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        WcpeLib.pluginScope.launch {
            val player = e.player
            WcpeLib.playerOnlineManager.recordQuit(player)
            val playerData = WcpeLib.dataManager.getPlayerDataByName(player.name) ?: return@launch
            synchronized(playerData) {
                playerData.loginOutXYZ =
                    "world:${player.world.name},x:${player.location.x.toInt()},y:${player.location.y.toInt()},z:${player.location.z.toInt()}"
                WcpeLib.dataManager.savePlayerData(playerData)
            }
        }
    }
}
