package codes.towel.survivalSprint

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
    val primaryWorld: String,
    val netherWorld: String,
    val warningDistance: Int,
    val startTime: Int,
    val totalDays: Int
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
                file.getString("primary_world") ?: "world",
                file.getString("nether_world") ?: "world_nether",
                file.getInt("warning_distance"),
                file.getInt("start_time"),
                file.getInt("totalDays")
            )
        }
    }
}

data class ServerState(
    val fileConf: FileConfiguration,
    val file: File,
    val plugin: JavaPlugin
) {
    var borderSize: Double
        get() = fileConf.getDouble("border_size")
        set(value) { fileConf.set("border_size", value) }

    /**
     * Permits border movement, false by default
     */
    var borderMoving: Boolean
        get() = fileConf.getBoolean("border_moving")
        set(value) { fileConf.set("border_moving", value) }

    /**
     * Current target border size; when reached it will stop moving
     */
    var borderTarget: Int
        get() = fileConf.getInt("border_target")
        set(value) { fileConf.set("border_target", value) }

    /**
     * How much the border shrinks per second
     */
    var borderShrinkPerSecond: Double
        get() = fileConf.getDouble("border_shrink_per_second")
        set(value) { fileConf.set("border_shrink_per_second", value) }

    /**
     * This is for convenience and should be updated by the BorderManager
     */
    var day: Int
        get() = fileConf.getInt("day")
        set(value) { fileConf.set("day", value) }

    companion object {
        fun load(plugin: JavaPlugin, file: File, serverConf: ServerConfiguration): ServerState {
            file.createNewFile()
            val fileConf = YamlConfiguration.loadConfiguration(file)
            fileConf.addDefaults(mapOf(
                "border_pos" to serverConf.initialBorder,
                "border_moving" to false,
                "border_target" to serverConf.initialBorder,
                "border_shrink_per_second" to 0.0,
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
    lateinit var lang: FileConfiguration
    lateinit var borderManager: BorderManager
    var customResources: CustomResourcesManager? = null

    fun saveDefaultLang(): FileConfiguration {
        val resource = getResource("lang.yml")
        val outFile = File(dataFolder, "lang.yml")
        if (!outFile.exists()) {
            outFile.createNewFile()
            try {
                resource.use { input ->
                    outFile.outputStream().use { output ->
                        input!!.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                logger.severe("Failed to copy lang.yml. Unloading plugin...")
                e.printStackTrace()
                server.pluginManager.disablePlugin(this)
            }
        }
        return YamlConfiguration.loadConfiguration(outFile)
    }

    override fun onEnable() {
        saveDefaultConfig()
        lang = saveDefaultLang()
        serverConf = ServerConfiguration.load(config)

        val serverStateFile = File(dataFolder, "state.yml")
        serverState = ServerState.load(this, serverStateFile, serverConf)

        // initialize the border manager
        borderManager = BorderManager(this, serverConf, serverState, lang)

        if (config.getBoolean("enable_custom_resources")) {
            customResources = CustomResourcesManager(this)
        }

        getCommand("ss")?.setExecutor(ManagementCommand(customResources?.effectManager, borderManager))

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            SSPlaceholderExpansion(this, serverConf, serverState).register()
            logger.info("Hooked into PlaceholderAPI")
        }
    }

    override fun onDisable() {
        logger.info("Saving data...")
        serverState.save()
        customResources?.disable()
        logger.info("Goodbye!")
    }
}
