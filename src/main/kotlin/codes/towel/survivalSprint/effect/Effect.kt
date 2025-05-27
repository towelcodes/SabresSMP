package codes.towel.survivalSprint.effect

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.event.Event
import java.util.*

/// Duration is relative to the start time; should be compared when loading
abstract class Effect(val duration: Long?) : ConfigurationSerializable {
    abstract val name: String
    abstract val icon: String
    abstract val color: String
    abstract val visible: Boolean

    override fun toString(): String {
        return this.name
    }

    val start: Date = Date()
    val end: Date? get() = this.start.let { start ->
        this.duration?.let { duration ->
            Date(start.time + duration)
        }
    }

    val remaining: Long? get() = this.end?.let { end ->
        end.time - System.currentTimeMillis()
    }

    abstract val relevantEvents: Map<Class<out Event>, (Event) -> Unit>

    override fun serialize(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        // make the duration the time remaining
        map["duration"] = this.start.compareTo(this.end)
        map["class"] = this.javaClass.simpleName
        return map
    }

    companion object {
        @JvmStatic
        fun deserialize(data: Map<String, Any>): Effect {
            val duration = data["duration"] as Long?
            return when (val className = data["class"] as String) {
                "TotemCooldownEffect" -> TotemCooldownEffect(duration)
                else -> throw IllegalArgumentException("Unknown effect class: $className")
            }
        }

        fun registerEffects() {
            ConfigurationSerialization.registerClass(TotemCooldownEffect::class.java)
        }
    }
}