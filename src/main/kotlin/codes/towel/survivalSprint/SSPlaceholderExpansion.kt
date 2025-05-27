package codes.towel.survivalSprint

import codes.towel.survivalSprint.effect.GlobalEffectManager
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.java.JavaPlugin

class SSPlaceholderExpansion(private val plugin: JavaPlugin,
                             val conf: ServerConfiguration,
                             val state: ServerState,
                             val em: GlobalEffectManager
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
        }
        return null
    }
}