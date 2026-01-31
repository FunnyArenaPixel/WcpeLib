package top.wcpe.wcpelib.bukkit.mapper

import org.apache.ibatis.annotations.Param
import top.wcpe.wcpelib.bukkit.entity.PlayerDailyLoginData
import java.time.LocalDate

interface PlayerDailyLoginMapper {
    fun createTable()

    fun upsert(data: PlayerDailyLoginData): Int

    fun getByStatDate(@Param("statDate") statDate: LocalDate): PlayerDailyLoginData?
}
