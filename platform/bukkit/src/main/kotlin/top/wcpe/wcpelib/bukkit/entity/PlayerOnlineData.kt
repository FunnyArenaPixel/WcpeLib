package top.wcpe.wcpelib.bukkit.entity

import java.time.LocalDate
import java.util.UUID

/**
 * @author NuStar
 * @since 2025/12/7 17:13
 */
data class PlayerOnlineData(
    var id: Long? = null,
    var playerUuid: UUID,
    var statDate: LocalDate,
    var onlineMinutes: Int = 0,
    var loginCount: Int = 0,
    var extraInfo: String = "",
) {
    companion object {
        @JvmStatic
        fun create(playerUuid: UUID): PlayerOnlineData {
            return PlayerOnlineData(null, playerUuid, LocalDate.now(), 0, 0, "")
        }

        @JvmStatic
        fun or(playerUuid: UUID, playerOnlineData: PlayerOnlineData?): PlayerOnlineData {
            return playerOnlineData ?: create(playerUuid)
        }
    }

    fun hasFlag(flag: String): Boolean {
        return parseFlags().contains(flag)
    }

    fun setFlags(flags: Collection<String>) {
        extraInfo = flags.toMutableSet().joinToString(",") { it.trim() }.trim(',')
    }

    fun addFlag(flag: String): Boolean {
        val flags = parseFlags()
        val added = flags.add(flag)
        if (added) {
            setFlags(flags)
        }
        return added
    }

    private fun parseFlags(): MutableSet<String> {
        if (extraInfo.isBlank()) {
            return mutableSetOf()
        }
        return extraInfo.split(",").mapNotNull {
            val v = it.trim()
            if (v.isEmpty()) null else v
        }.toMutableSet()
    }
}
