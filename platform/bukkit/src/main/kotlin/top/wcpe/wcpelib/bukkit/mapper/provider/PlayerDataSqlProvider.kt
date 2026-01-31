package top.wcpe.wcpelib.bukkit.mapper.provider

import org.apache.ibatis.annotations.Param

/**
 * @author NuStar
 * @since 2025/12/7 14:52
 */
@Suppress("unused")
class PlayerDataSqlProvider {
    /**
     * 生成添加字段的 SQL
     */
    fun addColumn(
        @Param("columnName") columnName: String,
        @Param("columnType") columnType: String,
        @Param("columnComment") columnComment: String
    ): String =
        """                                                                                                                                                                                                                                             
          ALTER TABLE `wpcelib_player_data`                                                                                                                                                                                                               
          ADD COLUMN `${columnName}` $columnType COMMENT #{columnComment}                                                                                                                                                                                 
          """.trimIndent()
}