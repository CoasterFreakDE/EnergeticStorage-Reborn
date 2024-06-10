package com.liamxsage.energeticstorage.commands

import com.liamxsage.energeticstorage.annotations.RegisterCommand
import com.liamxsage.energeticstorage.extensions.isESDebugModeEnabled
import com.liamxsage.energeticstorage.extensions.sendMessagePrefixed
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@RegisterCommand(
    name = "esdebug",
    description = "Debug your nodes and connections in the Energetic Storage network.",
    usage = "/esdebug",
    permission = "energeticstorage.debug",
    permissionDefault = PermissionDefault.OP
)
class ESDebugCommand : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessagePrefixed("<red>Only players can use this command")
            return true
        }

        val enabled = if (args.isNotEmpty()) args[0] == "on" else !sender.isESDebugModeEnabled
        sender.isESDebugModeEnabled = enabled

        sender.sendMessagePrefixed("<gray>Debug mode is now ${if (enabled) "enabled" else "disabled"}")
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> listOf("on", "off")
            else -> emptyList()
        }
    }

}