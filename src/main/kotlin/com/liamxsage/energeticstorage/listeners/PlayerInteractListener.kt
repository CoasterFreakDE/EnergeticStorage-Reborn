package com.liamxsage.energeticstorage.listeners

import com.liamxsage.energeticstorage.SYSTEM_ID_NAMESPACE
import com.liamxsage.energeticstorage.cache.SystemCache
import com.liamxsage.energeticstorage.extensions.sendMessagePrefixed
import com.liamxsage.energeticstorage.model.ESSystem
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.*

class PlayerInteractListener : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent): Unit = with(event) {
        if (event.action != Action.RIGHT_CLICK_BLOCK || event.hand != EquipmentSlot.HAND || event.player.isSneaking) return@with
        if (clickedBlock == null) return@with
        if (clickedBlock!!.type != Material.CHISELED_BOOKSHELF) return@with
        if (!clickedBlock!!.hasMetadata(SYSTEM_ID_NAMESPACE.key)) return@with
        val systemUUID = clickedBlock!!.getMetadata(SYSTEM_ID_NAMESPACE.key).first().asString()
        val system = SystemCache.getSystem(UUID.fromString(systemUUID)) ?: ESSystem(UUID.fromString(systemUUID))

        player.sendMessagePrefixed("Successfully interacted with system with ID $systemUUID.")
    }

}