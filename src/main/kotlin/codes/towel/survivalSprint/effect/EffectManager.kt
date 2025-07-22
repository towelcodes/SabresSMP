package codes.towel.survivalSprint.effect

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

/**
 * handles effects, as well as removing effects on expiry
 */
open class EffectManager(protected val plugin: JavaPlugin) {
    private val _effects = mutableMapOf<Effect, BukkitTask?>() // we could use an Optional here instead to make function clearer
    val effects: List<Effect> get() = _effects.keys.toList()

    fun addEffect(effect: Effect) {
        if (effect.duration != null) {
            val task = object : BukkitRunnable() {
                override fun run() {
                    plugin.logger.info("Removing effect ${effect.javaClass.simpleName} (expired)")
                    _effects.remove(effect)
                }
            }.runTaskLater(plugin, (effect.duration * 20).toLong())
            _effects[effect] = task
        } else {
            _effects[effect] = null
        }
    }

    fun removeEffect(effect: Effect) {
        if (_effects.containsKey(effect)) {
            _effects[effect]?.cancel()
            _effects.remove(effect)
        }
    }

    fun serialize(): List<Map<String, Any>> {
        return effects.map { it.serialize() }
    }

    companion object {
        @JvmStatic
        fun deserialize(plugin: JavaPlugin, data: List<Map<String, Any>>): EffectManager {
            plugin.logger.info("Deserializing effects...")
            plugin.logger.info(data.toString())
            val manager = EffectManager(plugin)
            data.forEach {
                plugin.logger.info(it.toString())
                val effect = Effect.deserialize(it)
                plugin.logger.info(effect.toString())
                manager.addEffect(effect)
            }
            plugin.logger.info("Deserialized effects: ${manager.effects}")
            return manager
        }
    }
}

class GlobalEffectManager(plugin: JavaPlugin, val file: File) : EffectManager(plugin), Listener {
    private val logger = LoggerFactory.getLogger("SS.GlobalEffectManager")

    private val _playerEffects = mutableMapOf<UUID, EffectManager>()

    private val fileConfiguration: YamlConfiguration = YamlConfiguration.loadConfiguration(file)

    val playerEffects: Map<UUID, EffectManager> get() = _playerEffects.toMap()

    private val effectDispatcher: EffectDispatcher = EffectDispatcher(this)

    init {
        plugin.server.pluginManager.registerEvents(effectDispatcher, plugin)

        // deserialize global em from file
        val global = fileConfiguration.getMapList("global")
        if (global.size > 0) {
            logger.info("Loading global effects...")
            global.forEach { addEffect(Effect.deserialize(it as Map<String, Any>)) }
        } else {
            logger.info("No global effects are present")
        }

        val players = fileConfiguration.getConfigurationSection("player")
        if (players != null) {
            logger.info("Loading player effects...")
            logger.info(players.toString())
            for ((uuid, data) in players.getValues(true)) {
                val em = deserialize(plugin, data as List<Map<String, Any>>)
                _playerEffects[UUID.fromString(uuid)] = em
            }
        } else {
            logger.info("No player effects are present")
        }
        // create timer to save to file automatically
        object : BukkitRunnable() {
            override fun run() {
                save()
            }
        }.runTaskTimer(plugin, 40, 600) // TODO: make configurable
    }

    fun save() : Boolean {
        fileConfiguration.set("global", this.serialize())
        val playerSection = fileConfiguration.createSection("player")
        for ((uuid, em) in _playerEffects) {
            playerSection.set(uuid.toString(), em.serialize())
        }
        fileConfiguration.save(file)
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
        if (this._playerEffects.containsKey(player)) {
            return this._playerEffects[player]!!.effects
        }
        return emptyList()
    }

    fun removePlayerEffect(player: UUID, effect: Effect) {
        if (this._playerEffects.containsKey(player)) {
            this._playerEffects[player]!!.removeEffect(effect)
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        if (_playerEffects.containsKey(e.player.uniqueId)) {
            logger.info("Effects for ${e.player.name} are loaded")
            logger.info(_playerEffects[e.player.uniqueId]!!.effects.toString())
            // TODO we need to pause counting for effects that do not persist !!
            return
        }
    }

// we will need this later for adding and removing listeners maybe, but not now
//    @EventHandler
//    fun onPlayerLeave(e: PlayerQuitEvent) {
//        val em = _playerEffects.remove(e.player.uniqueId)
//        if (em != null) {
//            linkedFile?.second?.set("effects.player_effects.${e.player.uniqueId}", em.serialize())
//            logger.info("Saved effects for ${e.player.name}")
//        } else {
//            logger.info("No effects to save for ${e.player.name}")
//        }
//    }

}