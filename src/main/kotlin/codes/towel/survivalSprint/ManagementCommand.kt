package codes.towel.survivalSprint

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ManagementCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /ss give <item>")
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

        return false
    }
}