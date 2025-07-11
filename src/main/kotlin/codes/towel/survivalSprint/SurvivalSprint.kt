package codes.towel.survivalSprint

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
     * Manually set by host
     */
    var borderMoving: Boolean
        get() = fileConf.getBoolean("border_moving")
        set(value) { fileConf.set("border_moving", value) }

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
//        ConfigurationSerialization.registerClass(Effect::class.java)
//        ConfigurationSerialization.registerClass(EffectManager::class.java)
//        ConfigurationSerialization.registerClass(GlobalEffectManager::class.java)
//        Effect.registerEffects()

        saveDefaultConfig()
        val lang = saveDefaultLang()
        serverConf = ServerConfiguration.load(config)

        val serverStateFile = File(dataFolder, "state.yml")
        serverState = ServerState.load(this, serverStateFile, serverConf)

        // initialize the effect manager
        val effectDataFile = File(dataFolder, "effect.yml")
        effectDataFile.createNewFile()
        effectDataFile.reader().use { logger.info(it.readText()) }
        effectManager = GlobalEffectManager(this, effectDataFile)
//        effectManager = GlobalEffectManager.loadFrom(this, effectDataFile)
        server.pluginManager.registerEvents(effectManager, this)
//        server.pluginManager.registerEvents(EffectListener(effectManager), this)

        getCommand("ss")?.setExecutor(ManagementCommand(effectManager))

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
