package codes.towel.survivalSprint.effect

import org.bukkit.event.Event
import java.util.*

/// Duration is relative to the start time; should be compared when loading

// TODO change data type to long
abstract class Effect(val duration: Int?) {
    abstract val name: String
    abstract val icon: String
    abstract val color: String
    abstract val visible: Boolean

    abstract val persist: Boolean // true means relative to world time, false is player time

    override fun toString(): String {
        return this.name
    }

    val start: Date = Date()
    val end: Date? get() = this.start.let { start ->
        this.duration?.let { duration ->
            Date(start.time + duration)
        }
    }

    val remaining: Int? get() = this.end?.let { end ->
        (end.time - System.currentTimeMillis()).toInt()
    }

    abstract val relevantEvents: Map<Class<out Event>, (Event) -> Unit>

    fun serialize(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        // make the duration the time remaining
        map["duration"] = this.start.compareTo(this.end)
        map["class"] = this.javaClass.simpleName
        return map
    }

    companion object {
        @JvmStatic
        fun deserialize(data: Map<String, Any>): Effect {
            var duration = data["duration"] as Int?
            if (duration != null) {
                if (duration < 0) {
                    duration = null
                }
            }
            // TODO add other effects
            return when (val className = data["class"] as String) {
                "TotemCooldownEffect" -> TotemCooldownEffect(duration)
                else -> throw IllegalArgumentException("Unknown effect class: $className")
            }
        }

//        fun registerEffects() {
//            ConfigurationSerialization.registerClass(TotemCooldownEffect::class.java)
//        }
    }
}