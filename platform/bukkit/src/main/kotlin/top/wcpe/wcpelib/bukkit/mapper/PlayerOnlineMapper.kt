package top.wcpe.wcpelib.bukkit.mapper

import org.apache.ibatis.annotations.Param
import top.wcpe.wcpelib.bukkit.entity.PlayerOnlineData
import java.time.LocalDate
import java.util.UUID

/**
 * 玩家在线时长统计数据表
 */
interface PlayerOnlineMapper {
    fun createTable()

    fun upsertOnlineMinutes(data: PlayerOnlineData): Int

    fun listByPlayerUuid(@Param("playerUuid") playerUuid: UUID): List<PlayerOnlineData>

    fun listBetween(
        @Param("playerUuid") playerUuid: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): List<PlayerOnlineData>

    fun getByPlayerUuidAndStatDate(
        @Param("playerUuid") playerUuid: UUID,
        @Param("statDate") statDate: LocalDate
    ): PlayerOnlineData?

    fun sumOnlineMinutesBetween(
        @Param("playerUuid") playerUuid: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): Int?
}
