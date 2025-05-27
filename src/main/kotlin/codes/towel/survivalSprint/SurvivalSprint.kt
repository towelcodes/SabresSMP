package codes.towel.survivalSprint

import codes.towel.survivalSprint.effect.Effect
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

data class ServerConfiguration(
    val defaultPlayerHealth: Int,
    val playerInvulnerabilityOnRespawn: Int,
    val playerInvulnerabilityOnFirstJoin: Int,
    val dayChangeoverHour: Int,
    val initialBorder: Int,
    val borderShrink: Int,
    val borderShrinkSpeed: Int,
) {
    companion object {
        fun load(file: FileConfiguration): ServerConfiguration {
            return ServerConfiguration(
                file.getInt("default_player_health"),
                file.getInt("player_invulnerability_on_respawn"),
                file.getInt("player_invulnerability_on_first_join"),
                file.getInt("day_changeover_hour"),
                file.getInt("initial_border"),
                file.getInt("border_shrink"),
                file.getInt("border_shrink_speed"),
            )
        }
    }
}

data class ServerState(
    var borderPos: Long,
    var day: Int,
    val file: FileConfiguration,
    val plugin: JavaPlugin
) {
    companion object {
        fun load(plugin: JavaPlugin): ServerState {
            val file = YamlConfiguration.loadConfiguration(File(plugin.dataFolder, "config.yml"))
            return ServerState(
                file.getLong("border_pos"),
                file.getInt("day"),
                file,
                plugin
            )
        }
    }

    init {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, { save() }, 20L, 20L);
    }

    fun save() {
        file.set("border_pos", borderPos)
        file.set("day", day)
    }
}

class SurvivalSprint : JavaPlugin() {
    lateinit var serverConf: ServerConfiguration
    lateinit var serverState: ServerState
    lateinit var effectManager: GlobalEffectManager

    override fun onEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            logger.info("Hooked into PlaceholderAPI")
        }

        Effect.registerEffects()

        saveDefaultConfig()
        serverConf = ServerConfiguration.load(config)
        serverState = ServerState.load(this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
