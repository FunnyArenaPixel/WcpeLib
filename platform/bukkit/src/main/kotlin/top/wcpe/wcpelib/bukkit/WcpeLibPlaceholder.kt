package top.wcpe.wcpelib.bukkit

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

class WcpeLibPlaceholder : PlaceholderExpansion() {
    override fun getAuthor(): String {
        return "WCPE"
    }

    override fun getIdentifier(): String {
        return "WcpeLib"
    }

    override fun getVersion(): String {
        return "1.0.1"
    }

    //%WcpeLib_server_name%
    //%WcpeLib_display_name%
    //%WcpeLib_firstLoginTime_{playerName}%
    //%WcpeLib_lastLoginTime_{playerName}%
    //%WcpeLib_loginOutXYZ_{playerName}%
    //%WcpeLib_todayOnlineMinutes_{playerName}%
    //%WcpeLib_onlineMinutes_thisWeek_{playerName}%
    //%WcpeLib_onlineMinutes_lastWeek_{playerName}%
    //%WcpeLib_onlineMinutes_thisMonth_{playerName}%
    //%WcpeLib_todayOnlineMinutes%
    //%WcpeLib_onlineMinutes_thisWeek%
    //%WcpeLib_onlineMinutes_lastWeek%
    //%WcpeLib_onlineMinutes_thisMonth%
    override fun onRequest(p: OfflinePlayer, identifier: String): String {
        when (identifier) {
            "server_name" -> return WcpeLib.getServerName()
            "display_name" -> return WcpeLib.getDisplayName()
        }
        val parts = identifier.split("_")
        if (parts.isEmpty()) return ""
        return when (parts[0]) {
            "firstLoginTime" -> {
                val playerData = WcpeLib.dataManager.getPlayerDataByName(parts.getOrNull(1) ?: return "")
                playerData?.firstLoginTime?.toString() ?: ""
            }

            "lastLoginTime" -> {
                val playerData = WcpeLib.dataManager.getPlayerDataByName(parts.getOrNull(1) ?: return "")
                playerData?.lastLoginTime?.toString() ?: ""
            }

            "loginOutXYZ" -> {
                val playerData = WcpeLib.dataManager.getPlayerDataByName(parts.getOrNull(1) ?: return "")
                playerData?.loginOutXYZ ?: ""
            }

            "todayOnlineMinutes", "todayOnlineTime" -> {
                val uuid = resolveUuid(parts.getOrNull(1)) ?: p.uniqueId
                val today = LocalDate.now()
                WcpeLib.dataManager.getPlayerOnlineMinutes(uuid, today, today).toString()
            }

            "todayLoginCount" -> {
                val uuid = resolveUuid(parts.getOrNull(1)) ?: return ""
                val playerOnlineData = WcpeLib.playerOnlineManager.getPlayerOnlineData(uuid)
                return playerOnlineData?.loginCount.toString()
            }

            "onlineMinutes" -> {
                val scope = parts.getOrNull(1) ?: return ""
                val uuid = resolveUuid(parts.getOrNull(2)) ?: p.uniqueId
                val today = LocalDate.now()
                val result = when (scope) {
                    "thisWeek" -> {
                        val start = today.with(DayOfWeek.MONDAY)
                        WcpeLib.dataManager.getPlayerOnlineMinutes(uuid, start, today)
                    }
                    "lastWeek" -> {
                        val mondayThisWeek = today.with(DayOfWeek.MONDAY)
                        val end = mondayThisWeek.minusDays(1)
                        val start = end.minusDays(6)
                        WcpeLib.dataManager.getPlayerOnlineMinutes(uuid, start, end)
                    }

                    "thisMonth" -> {
                        val time = System.currentTimeMillis()
                        val start = today.withDayOfMonth(1)
                        val playerOnlineMinutes = WcpeLib.dataManager.getPlayerOnlineMinutes(uuid, start, today)
                        println("spend ${System.currentTimeMillis() - time}ms")
                        playerOnlineMinutes
                    }

                    else -> 0
                }
                result.toString()
            }

            else -> ""
        }
    }

    private fun resolveUuid(name: String?): UUID? {
        if (name.isNullOrBlank()) {
            return null
        }
        return Bukkit.getOfflinePlayer(name).uniqueId
    }
}
