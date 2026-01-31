package top.wcpe.wcpelib.bukkit.mapper

import org.apache.ibatis.annotations.*
import top.wcpe.wcpelib.bukkit.entity.PlayerData
import top.wcpe.wcpelib.bukkit.mapper.provider.PlayerDataSqlProvider


/**
 * 由 WCPE 在 2023/7/10 14:46 创建
 * <p>
 * Created by WCPE on 2023/7/10 14:46
 * <p>
 * <p>
 * GitHub  : <a href="https://github.com/wcpe">wcpe 's GitHub</a>
 * <p>
 * QQ      : 1837019522
 * @author : WCPE
 * @since  : v1.1.5-alpha-dev-3
 */
interface PlayerDataMapper {
    @Update(
        """
        CREATE TABLE IF NOT EXISTS `wpcelib_player_data` (
            `id` INT AUTO_INCREMENT PRIMARY KEY,
            `player_name` VARCHAR(255) NOT NULL COMMENT '玩家名',
            `uuid` VARCHAR(36) NOT NULL COMMENT '玩家UUID',
            `last_server_name` VARCHAR(255) COMMENT '上一次进入的服务器',
            `first_login_time` bigint NOT NULL COMMENT '首次登录时间',
            `last_login_time` bigint NOT NULL COMMENT '上一次登录时间',
            `login_out_xyz` VARCHAR(255) COMMENT '下线世界和坐标'
        );
        """
    )
    fun createTable()

    @Insert("INSERT INTO `wpcelib_player_data` (player_name, uuid, last_server_name, first_login_time, last_login_time, login_out_xyz) VALUES (#{playerName}, #{uuid}, #{lastServerName}, #{firstLoginTime}, #{lastLoginTime}, #{loginOutXYZ})")
    fun insertPlayerData(player: PlayerData): Int

    @Update("UPDATE `wpcelib_player_data` SET uuid = #{uuid}," +
            " last_server_name = #{lastServerName}," +
            " last_login_time = #{lastLoginTime}," +
            " login_out_xyz = #{loginOutXYZ}" +
            " WHERE player_name = #{playerName}")
    fun updatePlayerByName(playerData: PlayerData): Int

    @Select("SELECT * FROM `wpcelib_player_data` WHERE player_name = #{playerName}")
    @Results(
        Result(property = "playerName", column = "player_name"),
        Result(property = "uuid", column = "uuid"),
        Result(property = "lastServerName", column = "last_server_name"),
        Result(property = "firstLoginTime", column = "first_login_time"),
        Result(property = "lastLoginTime", column = "last_login_time"),
        Result(property = "loginOutXYZ", column = "login_out_xyz")
    )
    fun getPlayerDataByName(playerName: String): PlayerData?

    /**
     * 检查表中是否存在指定字段
     * @param columnName 字段名
     * @return 如果字段存在返回true，否则返回false
     */
    @Select(
        """
        SELECT COUNT(*) > 0
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'wpcelib_player_data'
        AND COLUMN_NAME = #{columnName}
        """
    )
    fun columnExists(@Param("columnName") columnName: String): Boolean

    /**
     * 向表中添加新字段
     * @param columnName 字段名
     * @param columnType 字段类型（如 VARCHAR(255)、INT、BIGINT、TEXT等）
     */
    @UpdateProvider(type = PlayerDataSqlProvider::class, method = "addColumn")
    fun addColumn(
        @Param("columnName") columnName: String,
        @Param("columnType") columnType: String,
        @Param("columnComment") columnComment: String
    )
}