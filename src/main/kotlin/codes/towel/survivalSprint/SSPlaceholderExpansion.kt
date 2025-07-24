package codes.towel.survivalSprint

import codes.towel.survivalSprint.util.Ref
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.ceil

class SSPlaceholderExpansion(private val plugin: JavaPlugin,
                             val conf: Ref<ServerConfiguration>,
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
            "border_raw" -> return state.borderSize.toString()
            "border" -> return ceil(state.borderSize).toInt().toString()
            "border_moving" -> return if (state.borderMoving) "true" else "false"
            "border_target" -> return state.borderTarget.toString()
            "border_initial" -> return conf.value.initialBorder.toString()
            "border_shrink" -> return conf.value.borderShrink.toString()
            "total_days" -> return conf.value.totalDays.toString()
            "start_time" -> return conf.value.startTime.toString()
        }
        return null
    }
}