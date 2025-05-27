package codes.towel.survivalSprint

import codes.towel.survivalSprint.effect.Effect
import codes.towel.survivalSprint.effect.GlobalEffectManager
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
    val fileConf: FileConfiguration,
    val file: File,
    val plugin: JavaPlugin
) {
    var borderPos: Int
        get() = fileConf.getInt("border_pos")
        set(value) { fileConf.set("border_pos", value) }
    var day: Int
        get() = fileConf.getInt("day")
        set(value) { fileConf.set("day", value) }

    companion object {
        fun load(plugin: JavaPlugin, file: File, serverConf: ServerConfiguration): ServerState {
            file.createNewFile()
            val fileConf = YamlConfiguration.loadConfiguration(file)
            fileConf.addDefaults(mapOf(
                "border_pos" to serverConf.initialBorder,
                "day" to 0))
            return ServerState(
                fileConf,
                file,
                plugin
            )
        }
    }

    init {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, { save() }, 20L, 20L);
    }

    fun save() {
        fileConf.save(file)
    }
}

class SurvivalSprint : JavaPlugin() {
    lateinit var serverConf: ServerConfiguration
    lateinit var serverState: ServerState
    lateinit var effectManager: GlobalEffectManager

    override fun onEnable() {
        Effect.registerEffects()
        saveDefaultConfig()
        serverConf = ServerConfiguration.load(config)

        val serverStateFile = File(dataFolder, "state.yml")
        serverState = ServerState.load(this, serverStateFile, serverConf)

        // TODO: create a class to hold logic for this
        val playerDataFile = File(dataFolder, "players.yml")
        playerDataFile.createNewFile()
        effectManager = GlobalEffectManager.loadFrom(this, playerDataFile) ?: GlobalEffectManager(this)
//        server.pluginManager.registerEvents(EffectListener(effectManager), this)

        getCommand("ss")?.setExecutor(ManagementCommand())

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            SSPlaceholderExpansion(this, serverConf, serverState, effectManager).register()
            logger.info("Hooked into PlaceholderAPI")
        }
    }

    override fun onDisable() {
        logger.info("Saving data...")
        serverState.save()
        effectManager.save()
        logger.info("Goodbye!")
    }
}
