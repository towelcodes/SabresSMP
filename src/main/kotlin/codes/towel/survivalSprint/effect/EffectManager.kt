package codes.towel.survivalSprint.effect

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

/**
 * handles effects, as well as removing effects on expiry
 */
open class EffectManager(val plugin: JavaPlugin) : ConfigurationSerializable {
    private val effects = mutableMapOf<Effect, BukkitTask?>()

    fun addEffect(effect: Effect) {
        if (effect.duration != null) {
            val task = object : BukkitRunnable() {
                override fun run() {
                    effects.remove(effect)
                }
            }.runTaskLater(plugin, effect.duration)
            effects[effect] = task
        }
    }

    fun removeEffect(effect: Effect) {
        if (effects.containsKey(effect)) {
            effects[effect]?.cancel()
            effects.remove(effect)
        }
    }

    fun getEffects(): List<Effect> {
        return effects.keys.toList()
    }

    override fun serialize(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["effects"] = this.effects.keys.map { it.serialize() }
        return map
    }

    companion object {
        @JvmStatic
        fun deserialize(plugin: JavaPlugin, data: Map<String, Any>): EffectManager {
            val manager = EffectManager(plugin)
            val effects = data["effects"] as List<Map<String, Any>>?
            effects?.forEach { manager.addEffect(Effect.deserialize(it)) }
            return manager
        }
    }
}

class GlobalEffectManager(plugin: JavaPlugin) : EffectManager(plugin), Listener {
    private val logger = LoggerFactory.getLogger("GlobalEffectManager:${hashCode()}")

    private val _playerEffects = mutableMapOf<UUID, EffectManager>()
    val playerEffects: Map<UUID, EffectManager> get() = _playerEffects.toMap()

    var linkedFile: Pair<File, FileConfiguration>? = null
         private set

    private val effectListener: EffectListener = EffectListener(this)

    init {
        plugin.server.pluginManager.registerEvents(effectListener, plugin)
    }

    fun linkFile(file: File, fileConf: FileConfiguration) : Boolean {
        logger.info("linking file")
        if (linkedFile != null) {
            logger.info("file already linked")
            return false
        }

        linkedFile = Pair(file, fileConf)
        object : BukkitRunnable() {
            override fun run() {
                save()
            }
        }.runTaskTimer(plugin, 40, 600) // TODO: make configurable
        logger.info("linked file: $linkedFile")
        return true
    }

    fun save() : Boolean {
        if (linkedFile == null) {
            logger.info("No file linked")
            return false
        }
        logger.info("saving effects to file")
        linkedFile?.second?.set("effects", serialize())
        linkedFile?.second?.save(linkedFile?.first!!)
        logger.info("Saved effects")
        val section = linkedFile?.second?.getValues(true)
        logger.info(section.toString())

        return true
    }

    fun addPlayerEffect(player: UUID, effect: Effect) {
        if (this._playerEffects.containsKey(player)) {
            this._playerEffects[player]!!.addEffect(effect)
        } else {
            this._playerEffects[player] = EffectManager(plugin)
            this._playerEffects[player]!!.addEffect(effect)
        }
    }

    fun getPlayerEffects(player: UUID): List<Effect> {
        // TODO add global effects
        return this._playerEffects[player]!!.getEffects()
    }

    fun removePlayerEffect(player: UUID, effect: Effect) {
        if (this._playerEffects.containsKey(player)) {
            this._playerEffects[player]!!.removeEffect(effect)
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val em = linkedFile?.second?.getSerializable("effects.player_effects.${e.player.uniqueId}", EffectManager::class.java)
        if (em != null) {
            _playerEffects[e.player.uniqueId] = em
            logger.info("Loaded effects for ${e.player.name}")
        } else {
            _playerEffects[e.player.uniqueId] = EffectManager(plugin)
            logger.info("No effects to load for ${e.player.name}")
        }
        logger.info(_playerEffects.keys.toString())
        logger.info(_playerEffects.values.toString())
    }

    @EventHandler
    fun onPlayerLeave(e: PlayerQuitEvent) {
        val em = _playerEffects.remove(e.player.uniqueId)
        if (em != null) {
            linkedFile?.second?.set("effects.player_effects.${e.player.uniqueId}", em.serialize())
            logger.info("Saved effects for ${e.player.name}")
        } else {
            logger.info("No effects to save for ${e.player.name}")
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        val effects = this._playerEffects.map { it.key.toString() to it.value.serialize() }.toMap()
        logger.info("Stored effects: $effects")
        val existingEffects = linkedFile?.second?.getConfigurationSection("effects.player_effects")?.getValues(true)
        logger.info("Existing effects: $existingEffects")
        val merged = (existingEffects?.toMutableMap() ?: mutableMapOf<String, Any>()) + effects
        logger.info("Merged effects: $merged")

        map["player_effects"] = merged
        map["global_effects"] = this.getEffects().map { it.serialize() }
        return map
    }

    companion object {
        fun loadFrom(plugin: JavaPlugin, file: File) : GlobalEffectManager {
            file.createNewFile()
            val data = YamlConfiguration.loadConfiguration(file)
            val em = data.getSerializable("effects", GlobalEffectManager::class.java) ?: GlobalEffectManager(plugin)
            em.linkFile(file, data)
            return em
        }

        @JvmStatic
        fun deserialize(plugin: JavaPlugin, data: Map<String, Any>): GlobalEffectManager {
            val manager = GlobalEffectManager(plugin)
            val playerEffects = data["player_effects"] as List<Pair<String, Map<String, Any>>>?
            playerEffects?.forEach { manager._playerEffects[UUID.fromString(it.first)] =
                EffectManager.deserialize(plugin, it.second)
            }
            val globalEffects = data["global_effects"] as List<Map<String, Any>>?
            globalEffects?.forEach { manager.addEffect(Effect.deserialize(it)) }
            return manager
        }
    }
}