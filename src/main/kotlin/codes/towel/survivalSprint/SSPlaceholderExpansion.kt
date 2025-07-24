package codes.towel.survivalSprint

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.java.JavaPlugin

class SSPlaceholderExpansion(private val plugin: JavaPlugin,
                             val conf: ServerConfiguration,
                             val state: ServerState,
) : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "ss"
    }

    override fun getAuthor(): String {
        return "towel.codes"
    }

    override fun getVersion(): String {
        return plugin.description.version
    }

    override fun persist(): Boolean {
        return true
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        when (params.lowercase()) {
            "day" -> return "Day " + state.day
            "border" -> return state.borderSize.toString()
            "border_moving" -> return if (state.borderMoving) "true" else "false"
            "border_target" -> return state.borderTarget.toString()
            "border_initial" -> return conf.initialBorder.toString()
            "border_shrink" -> return conf.borderShrink.toString()
            "total_days" -> return conf.totalDays.toString()
            "start_time" -> return conf.startTime.toString()
        }
        return null
    }
}