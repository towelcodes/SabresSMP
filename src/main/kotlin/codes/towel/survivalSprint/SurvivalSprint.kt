package codes.towel.survivalSprint

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

data class ServerConfiguration(
    var initialBorder: Int,
    var borderShrink: Int,
    var borderShrinkSpeed: Int,
    var dayChangeoverHour: Int,
    var currentDay: Int
)

class SurvivalSprint : JavaPlugin() {
    lateinit var serverConf: ServerConfiguration

    override fun onEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            logger.info("Hooked into PlaceholderAPI")
        }

        saveDefaultConfig()
        serverConf = ServerConfiguration(
            config.getInt("initial_border"),
            config.getInt("border_shrink"),
            config.getInt("border_shrink_speed"),
            config.getInt("day_changeover_hour"),
            config.getInt("current_day")
        )

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
