package top.wcpe.wcpelib.bukkit.manager

import org.bukkit.scheduler.BukkitRunnable

class PlayerOnlineTickTask(
    private val playerOnlineManager: PlayerOnlineManager,
) : BukkitRunnable() {
    override fun run() {
        playerOnlineManager.tick()
    }
}
