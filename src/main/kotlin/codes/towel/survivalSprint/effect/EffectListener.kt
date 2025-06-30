package codes.towel.survivalSprint.effect

import org.bukkit.event.Listener

/**
 * class to handle all events for effects
 * since spigot doesn't support listening for all EntityEvents we have to listen for specific ones
 * this should be owned by the GlobalEffectManager
 */
class EffectListener(val effectManager: GlobalEffectManager) : Listener {
}