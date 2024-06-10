package com.liamxsage.energeticstorage.commands

import com.liamxsage.energeticstorage.annotations.RegisterCommand
import com.liamxsage.energeticstorage.extensions.sendMessagePrefixed
import com.liamxsage.energeticstorage.extensions.sendSuccessSound
import com.liamxsage.energeticstorage.model.DiskSize
import com.liamxsage.energeticstorage.model.Disk
import com.liamxsage.energeticstorage.model.DiskDrive
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import java.util.*

@RegisterCommand(
    name = "esgive",
    description = "Give a player a drive or system",
    usage = "/esgive <item> [player]",
    permission = "energeticstorage.give",
    permissionDefault = PermissionDefault.OP
)
class ESGiveCommand : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessagePrefixed("<red>Usage: /esgive <item> [player]")
            return true
        }

        val item = args[0].lowercase(Locale.getDefault())
        val player = if (args.size > 1) Bukkit.getPlayer(args[1]) else sender

        if (player !is Player) {
            sender.sendMessagePrefixed("<red>Only players can receive items")
            return true
        }

        val itemStack = when (item) {
            "system" -> {
                DiskDrive().createSystemItem()
            }
            else -> {
                val diskSize = DiskSize.entries.find { it.diskName.lowercase(Locale.getDefault()) == item.replace("_", " ") }
                if (diskSize == null) {
                    sender.sendMessagePrefixed("<red>Invalid drive size")
                    return true
                }

                Disk(size = diskSize).createDiskItem()
            }
        }

        player.inventory.addItem(itemStack)
        player.sendMessagePrefixed("<green>Received item")
        player.sendSuccessSound()

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> {
                listOf("system", *DiskSize.entries.map { it.diskName.replace(" ", "_") }.toTypedArray())
                    .filter { it.startsWith(args[0], ignoreCase = true)}
            }
            2 -> {
                Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[1], ignoreCase = true)}
            }
            else -> {
                emptyList()
            }
        }
    }
}