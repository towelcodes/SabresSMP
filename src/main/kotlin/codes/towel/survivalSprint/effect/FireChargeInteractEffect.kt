package codes.towel.survivalSprint.effect

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerInteractEvent

class FireChargeInteractEffect(duration: Int?) : Effect(duration) {
    override val name = "Interact: Fire Charge"
    override val icon = ""
    override val color = "&c"
    override val visible = false
    override val persist = false // we need to check on join
    override val relevantEvents: Map<Class<out Event>, (Event) -> Unit>
        get() = mapOf(PlayerInteractEvent::class.java to ::onItemInteract)

    private fun onItemInteract(e: Event) {
        val event = e as PlayerInteractEvent
        Bukkit.getLogger().info("it works")
    }
}