package top.wcpe.wcpelib.bukkit.data

import top.wcpe.wcpelib.bukkit.entity.PlayerDailyLoginData
import java.time.LocalDate

interface IPlayerDailyLoginDataManager {
    fun savePlayerDailyLoginData(data: PlayerDailyLoginData): Boolean
    fun getPlayerDailyLoginData(statDate: LocalDate): PlayerDailyLoginData
}
