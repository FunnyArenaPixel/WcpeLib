package top.wcpe.wcpelib.bukkit.data.impl

import top.wcpe.wcpelib.bukkit.data.IDataManager
import top.wcpe.wcpelib.bukkit.entity.PlayerData
import top.wcpe.wcpelib.bukkit.entity.PlayerDailyLoginData
import top.wcpe.wcpelib.bukkit.entity.PlayerOnlineData
import top.wcpe.wcpelib.bukkit.mapper.PlayerDataMapper
import top.wcpe.wcpelib.bukkit.mapper.PlayerDailyLoginMapper
import top.wcpe.wcpelib.bukkit.mapper.PlayerOnlineMapper
import top.wcpe.wcpelib.common.mybatis.Mybatis
import java.time.LocalDate
import java.util.*

/**
 * 由 WCPE 在 2023/7/10 15:24 创建
 * <p>
 * Created by WCPE on 2023/7/10 15:24
 * <p>
 * <p>
 * GitHub  : <a href="https://github.com/wcpe">wcpe 's GitHub</a>
 * <p>
 * QQ      : 1837019522
 * @author : WCPE
 * @since  : v1.1.5-alpha-dev-3
 */
class MySQLDataManager(private val mybatis: Mybatis) : IDataManager {

    init {
        mybatis.addMapper(PlayerDataMapper::class.java, PlayerOnlineMapper::class.java, PlayerDailyLoginMapper::class.java)
        mybatis.sqlSessionFactory.openSession(true).use { sqlSession ->
            sqlSession.getMapper(PlayerDataMapper::class.java).createTable()
            sqlSession.getMapper(PlayerOnlineMapper::class.java).createTable()
            sqlSession.getMapper(PlayerDailyLoginMapper::class.java).createTable()
        }
    }

    override fun getPlayerDataByName(playerName: String): PlayerData? {
        mybatis.sqlSessionFactory.openSession(true).use { sqlSession ->
            val mapper = sqlSession.getMapper(PlayerDataMapper::class.java)
            return mapper.getPlayerDataByName(playerName)
        }
    }

    override fun savePlayerData(playerData: PlayerData): Boolean {
        mybatis.sqlSessionFactory.openSession(true).use { sqlSession ->
            val mapper = sqlSession.getMapper(PlayerDataMapper::class.java)
            val playerDataByName = mapper.getPlayerDataByName(playerData.playerName)
            return if (playerDataByName == null) {
                mapper.insertPlayerData(playerData)
            } else {
                mapper.updatePlayerByName(playerData)
            } > 0
        }
    }

    override fun addColumn(columnName: String, columnType: String, columnComment: String) {
        mybatis.sqlSessionFactory.openSession(true).use { sqlSession ->
            val mapper = sqlSession.getMapper(PlayerDataMapper::class.java)
            val columnExists = mapper.columnExists(columnName)
            if (!columnExists) {
                mapper.addColumn(columnName, columnType, columnComment)
            }
        }
    }

    override fun savePlayerOnlineData(playerOnlineData: PlayerOnlineData): Boolean {
        mybatis.sqlSessionFactory.openSession(true).use { sqlSession ->
            val mapper = sqlSession.getMapper(PlayerOnlineMapper::class.java)
            return mapper.upsertOnlineMinutes(playerOnlineData) > 0
        }
    }

    override fun getPlayerOnlineData(playerUuid: UUID, statDate: LocalDate): PlayerOnlineData {
        mybatis.sqlSessionFactory.openSession(true).use { sqlSession ->
            val mapper = sqlSession.getMapper(PlayerOnlineMapper::class.java)
            return PlayerOnlineData.or(playerUuid, mapper.getByPlayerUuidAndStatDate(playerUuid, statDate))
        }
    }

    override fun listPlayerOnlineData(playerUuid: UUID, startDate: LocalDate, endDate: LocalDate): List<PlayerOnlineData> {
        mybatis.sqlSessionFactory.openSession(true).use { sqlSession ->
            val mapper = sqlSession.getMapper(PlayerOnlineMapper::class.java)
            return mapper.listBetween(playerUuid, startDate, endDate)
        }
    }

    override fun getPlayerOnlineMinutes(playerUuid: UUID, startDate: LocalDate, endDate: LocalDate): Int {
        mybatis.sqlSessionFactory.openSession(true).use { sqlSession ->
            val mapper = sqlSession.getMapper(PlayerOnlineMapper::class.java)
            return mapper.sumOnlineMinutesBetween(playerUuid, startDate, endDate) ?: 0
        }
    }

    override fun savePlayerDailyLoginData(data: PlayerDailyLoginData): Boolean {
        mybatis.sqlSessionFactory.openSession(true).use { sqlSession ->
            val mapper = sqlSession.getMapper(PlayerDailyLoginMapper::class.java)
            return mapper.upsert(data) > 0
        }
    }

    override fun getPlayerDailyLoginData(statDate: LocalDate): PlayerDailyLoginData {
        mybatis.sqlSessionFactory.openSession(true).use { sqlSession ->
            val mapper = sqlSession.getMapper(PlayerDailyLoginMapper::class.java)
            return mapper.getByStatDate(statDate) ?: PlayerDailyLoginData.create(statDate)
        }
    }
}
