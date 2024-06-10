package com.liamxsage.boilerplates.listeners

import dev.fruxz.stacked.text
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class TemplateListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(text("<#55efc4>${event.player.name} joined the party."))
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage(text("<#ff7675>${event.player.name} left the party."))
    }
}