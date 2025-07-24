package codes.towel.survivalSprint

import codes.towel.survivalSprint.effect.FireChargeInteractEffect
import codes.towel.survivalSprint.effect.GlobalEffectManager
import codes.towel.survivalSprint.effect.TotemCooldownEffect
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ManagementCommand(val em: GlobalEffectManager?, val borderManager: BorderManager) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /ss <give|effect|effectget|save|border> [args]")
            return false
        }

        if (args[0] == "give" && args.size > 1 && sender is Player) {
            try {
                val item = CustomItemType.valueOf(args[1].uppercase())
                sender.inventory.addItem(item.getItem())
                sender.sendMessage("Given you ${item.getItem().displayName()}")
                return true
            } catch (e: IllegalArgumentException) {
                sender.sendMessage("Unknown item: ${args[1]}")
                return false
            }
        }

        else if (args[0] == "effect" && args.size > 3) {
            val effect = when (args[2].lowercase()) {
                "totemcooldown" -> TotemCooldownEffect(args[3].toInt())
                "firechargeinteract" -> FireChargeInteractEffect(args[3].toInt())
                else -> {
                    sender.sendMessage("Unknown effect: ${args[2]}")
                    return false
                }
            }
            Bukkit.getPlayer(args[1])?.uniqueId?.let { em?.addPlayerEffect(it, effect) }
            sender.sendMessage("Applied effect to ${args[1]}")
            return true
        }

        else if (args[0] == "effectget" && args.size > 1) {
            val player = Bukkit.getPlayer(args[1]) ?: return false
            em?.getPlayerEffects(player.uniqueId)?.forEach { sender.sendMessage(it.toString()) }
            sender.sendMessage("Got effects for ${args[1]}")
            return true
        }

        else if (args[0] == "save") {
            em?.save()
            sender.sendMessage("Saved effects")
            return true
        }

        else if (args[0] == "enableborder") {
            borderManager.enableBorderMovement()
            return true
        }

        else if (args[0] == "disableborder") {
            borderManager.stopBorderMovement()
            return true
        }

        else if(args[0] == "bordertarget" && args.size > 1) {
            borderManager.setBorderTarget(args[1].toInt())
        }

        return false
    }
}