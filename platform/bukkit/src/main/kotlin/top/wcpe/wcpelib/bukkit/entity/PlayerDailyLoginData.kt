package top.wcpe.wcpelib.bukkit.entity

import java.time.LocalDate

data class PlayerDailyLoginData(
    var id: Long? = null,
    var statDate: LocalDate,
    var playerUuids: String = "",
) {
    companion object {
        fun create(statDate: LocalDate): PlayerDailyLoginData {
            return PlayerDailyLoginData(null, statDate, "")
        }
    }
}
