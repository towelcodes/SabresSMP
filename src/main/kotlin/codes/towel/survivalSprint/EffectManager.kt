package codes.towel.survivalSprint

import codes.towel.survivalSprint.effect.Effect
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
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

class GlobalEffectManager(plugin: JavaPlugin) : EffectManager(plugin) {
    private val _playerEffects = mutableMapOf<UUID, EffectManager>()
    val playerEffects: Map<UUID, EffectManager> get() = _playerEffects.toMap()

    var linkedFile: FileConfiguration? = null
         private set

    fun linkFile(file: FileConfiguration) : Boolean {
        if (linkedFile != null) {
            return false
        }

        linkedFile = file
        object : BukkitRunnable() {
            override fun run() {
                save()
            }
        }.runTaskTimer(plugin, 100, 600) // TODO: make configurable
        return true
    }

    fun save() : Boolean {
        if (linkedFile == null) {
            return false
        }
        linkedFile?.set("effects", serialize())
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
        return this._playerEffects[player]!!.getEffects()
    }

    fun removePlayerEffect(player: UUID, effect: Effect) {
        if (this._playerEffects.containsKey(player)) {
            this._playerEffects[player]!!.removeEffect(effect)
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["player_effects"] = this._playerEffects.map { it.key.toString() to it.value.serialize() }
        map["global_effects"] = this.getEffects().map { it.serialize() }
        return map
    }

    companion object {
        fun loadFrom(plugin: JavaPlugin, file: File) : GlobalEffectManager? {
            val data = YamlConfiguration.loadConfiguration(file)
            val em = data.getSerializable("effects", GlobalEffectManager::class.java)
            em?.linkFile(data)
            return em
        }

        @JvmStatic
        fun deserialize(plugin: JavaPlugin, data: Map<String, Any>): GlobalEffectManager {
            val manager = GlobalEffectManager(plugin)
            val playerEffects = data["player_effects"] as List<Pair<String, Map<String, Any>>>?
            playerEffects?.forEach { manager._playerEffects[UUID.fromString(it.first)] = EffectManager.deserialize(plugin, it.second) }
            val globalEffects = data["global_effects"] as List<Map<String, Any>>?
            globalEffects?.forEach { manager.addEffect(Effect.deserialize(it)) }
            return manager
        }
    }
}