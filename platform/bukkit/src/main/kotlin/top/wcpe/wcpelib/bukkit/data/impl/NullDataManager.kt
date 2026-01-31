package top.wcpe.wcpelib.bukkit.data.impl

import top.wcpe.wcpelib.bukkit.data.IDataManager
import top.wcpe.wcpelib.bukkit.entity.PlayerData
import top.wcpe.wcpelib.bukkit.entity.PlayerDailyLoginData
import top.wcpe.wcpelib.bukkit.entity.PlayerOnlineData
import java.time.LocalDate
import java.util.*

/**
 * 由 WCPE 在 2023/7/29 9:47 创建
 * <p>
 * Created by WCPE on 2023/7/29 9:47
 * <p>
 * <p>
 * GitHub  : <a href="https://github.com/wcpe">wcpe 's GitHub</a>
 * <p>
 * QQ      : 1837019522
 * @author : WCPE
 * @since  : v1.2.1-alpha-dev-2
 */
class NullDataManager : IDataManager {
    override fun getPlayerDataByName(playerName: String): PlayerData? {
        return null
    }

    override fun savePlayerData(playerData: PlayerData): Boolean {
        return false
    }

    override fun addColumn(columnName: String, columnType: String, columnComment: String) {
        TODO("Not yet implemented")
    }

    override fun savePlayerOnlineData(playerOnlineData: PlayerOnlineData): Boolean {
        return false
    }

    override fun getPlayerOnlineData(playerUuid: UUID, statDate: LocalDate): PlayerOnlineData {
        return PlayerOnlineData.create(playerUuid)
    }

    override fun listPlayerOnlineData(playerUuid: UUID, startDate: LocalDate, endDate: LocalDate): List<PlayerOnlineData> {
        return emptyList()
    }

    override fun getPlayerOnlineMinutes(playerUuid: UUID, startDate: LocalDate, endDate: LocalDate): Int {
        return 0
    }

    override fun savePlayerDailyLoginData(data: PlayerDailyLoginData): Boolean {
        return false
    }

    override fun getPlayerDailyLoginData(statDate: LocalDate): PlayerDailyLoginData {
        return PlayerDailyLoginData.create(statDate)
    }
}
