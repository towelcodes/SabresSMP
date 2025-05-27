package codes.towel.survivalSprint.effect

import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType


class TotemCooldownEffect(duration: Long?) : Effect(duration) {
    override val name = "Totem Cooldown"
    override val icon = "C"
    override val color = "&8"
    override val visible = true
    override val relevantEvents: Map<Class<out Event>, (Event) -> Unit>
        get() = mapOf(InventoryClickEvent::class.java to ::onInventoryClick)

    private fun onInventoryClick(e: Event) {
        val event = e as InventoryClickEvent
        if (event.slotType == InventoryType.SlotType.ARMOR) {
            event.isCancelled = true
        }
    }
}