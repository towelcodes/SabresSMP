package codes.towel.survivalSprint

import codes.towel.survivalSprint.event.EventEnum
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.*

abstract class Event(val em: GlobalEventManager, val duration: Long?) {
    abstract val enum: EventEnum

    override fun toString(): String {
        return this.name
    }

    // TODO add set() for duration and update schedules if applicable

    val active get() = this.scheduled || this.running
    val scheduled get() = (this.start?.compareTo(Date()) ?: 1) <= 0

    var running = false
        private set
    var start: Date? = null
        private set
    val end: Date? get() = this.start?.let { start ->
        this.duration?.let { duration ->
            Date(start.time + duration)
        }
    }

    abstract val name: String
    abstract val description: String

    abstract val listeners: Set<Listener>

    /**
     * Immeediately start without pushing to queue
     */
    abstract fun quickstart(): Boolean

    /**
     * Queues the event to start at the specified time.
     * Will do nothing if the event already has been queued.
     * Returns true if the event was queued.
     */
    abstract fun queue(start: Date): Boolean;

    abstract fun start(): Boolean

    abstract fun stop(): Boolean

    abstract fun cancel()

    abstract fun halt()

    abstract fun halt(delay: Long)
}

abstract class TargetedEvent(em: GlobalEventManager, duration: Long?, val target: Player?) : Event(em, duration)

data class SerializableEvent(val enum: EventEnum, val start: Date?, val duration: Long?)