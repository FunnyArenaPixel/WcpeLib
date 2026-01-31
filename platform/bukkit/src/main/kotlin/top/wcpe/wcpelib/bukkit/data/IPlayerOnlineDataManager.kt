package top.wcpe.wcpelib.bukkit.data

import top.wcpe.wcpelib.bukkit.entity.PlayerOnlineData
import java.time.LocalDate
import java.util.*

/**
 * @author NuStar
 * @since 2025/12/7 19:11
 */
interface IPlayerOnlineDataManager {
    fun savePlayerOnlineData(playerOnlineData: PlayerOnlineData): Boolean
    fun getPlayerOnlineData(playerUuid: UUID, statDate: LocalDate): PlayerOnlineData
    fun getPlayerOnlineMinutes(playerUuid: UUID, startDate: LocalDate, endDate: LocalDate): Int
    fun listPlayerOnlineData(playerUuid: UUID, startDate: LocalDate, endDate: LocalDate): List<PlayerOnlineData>
}
