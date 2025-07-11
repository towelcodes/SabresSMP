package codes.towel.survivalSprint.effect

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent

/**
 * class to handle all events for effects
 * since spigot doesn't support listening for all EntityEvents we have to listen for specific ones
 * this should be owned by the GlobalEffectManager
 */
class EffectDispatcher(private val effectManager: GlobalEffectManager) : Listener {
    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val clazz = e::class.java
        val effects = effectManager.getPlayerEffects(e.player.uniqueId)
        effects.forEach { effect ->
            if (effect.relevantEvents.containsKey(clazz)) {
                effect.relevantEvents[clazz]!!.invoke(e)
            }
        }
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val clazz = e::class.java
        val effects = effectManager.getPlayerEffects(e.whoClicked.uniqueId)
        effects.forEach { effect ->
            if (effect.relevantEvents.containsKey(clazz)) {
                effect.relevantEvents[clazz]!!.invoke(e)
            }
        }
    }
}