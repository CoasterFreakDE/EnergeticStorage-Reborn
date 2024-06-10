package com.liamxsage.boilerplates.commands

import com.liamxsage.boilerplates.annotations.RegisterCommand
import com.liamxsage.boilerplates.extensions.sendMessagePrefixed
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.permissions.PermissionDefault

@RegisterCommand(
    name = "template",
    description = "Template command",
    permission = "boilerplates.commands.template",
    permissionDefault = PermissionDefault.OP,
    aliases = ["temp"]
)
class TemplateCommand : CommandExecutor {

    /**
     * Executes the given command, returning its success.
     * <br></br>
     * If false is returned, then the "usage" paper-plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender Source of the command
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return true if a valid command, otherwise false
     */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        sender.sendMessagePrefixed(
            "<hover:show_text:'<#dfe6e9>This is a tooltip'><gradient:#ffeaa7:#74b9ff>Just a cool looking template text.</gradient></hover>"
        )

        return true
    }

}