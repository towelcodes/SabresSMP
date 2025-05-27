package codes.towel.survivalSprint.effect

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityEvent

class EffectListener(val effectManager: GlobalEffectManager) : Listener {
    @EventHandler
    fun onEntityEvent(e: EntityEvent) {
        if (e.entity !is Player) return
        val player = e.entity as Player
        effectManager.getPlayerEffects(player.uniqueId).forEach {
            it.relevantEvents[e::class.java]?.invoke(e)
        }
    }
}