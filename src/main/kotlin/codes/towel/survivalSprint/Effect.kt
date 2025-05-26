package codes.towel.survivalSprint

import codes.towel.survivalSprint.event.EventEnum
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.*

abstract class Effect(val em: GlobalEffectManager, val duration: Long?) {
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
     * Queues the event to start at the specified time.
     * Will do nothing if the event already has been queued.
     * Returns true if the event was queued.
     */
    abstract fun queue(start: Date): Boolean;

    /**
     * Starts the event after the specified delay. Places in the queue and updates the start time.
     * If the delay is 0, it will start immediately without pushing to queue.
     * Does nothing if the event is already started.
     * Returns true if the event was started.
     */
    abstract fun start(delay: Long): Boolean

    /**
     * Stops the event. Removes from active events. Does not update duration.
     * All active Listeners are unregistered.
     * Does nothing if the event is already stopped.
     * Returns true if the event was stopped.
     */
    abstract fun stop(): Boolean

    /**
     * Cancels the event, stopping it if it is active or removing it from the queue.
     * Unregisters all active Listeners.
     */
    abstract fun cancel()

    /**
     * Pause execution of the event, until the event is manually resumed.
     * The start time will be updated when the event is resumed to account for the change,
     * relative to its original start time.
     * Unregisters all active listeners.
     * If a halt is already active, it will be overriden.
     */
    // abstract fun halt()

    /**
     * Pauses execution of the event for a specified amount of time.
     * Updates the start time accordingly.
     * Unregisters all active listeners until the event automatically resumes.
     * If a halt is already active, it will be overriden with this new delay.
     */
    // abstract fun halt(delay: Long)

    /**
     * Unhalts a halt(). Resumes the event.
     */
    // abstract fun unhalt()
}

abstract class TargetedEffect(em: GlobalEffectManager, duration: Long?, val target: Player?) : Effect(em, duration)

data class SerializableEffect(val enum: EventEnum, val start: Long?, val duration: Long?) {
    companion object {
        fun from(input: String, delimiter: Char): SerializableEffect {
            val parts = input.split(delimiter)
            return SerializableEffect(
                EventEnum.valueOf(parts[0]),
                parts[1].toLongOrNull(),
                parts[2].toLongOrNull()
            )
        }
    }

    fun toString(delimiter: Char): String {
        return "${enum}${delimiter}${start ?: "null"}${delimiter}${duration ?: "null"}"
    }
}