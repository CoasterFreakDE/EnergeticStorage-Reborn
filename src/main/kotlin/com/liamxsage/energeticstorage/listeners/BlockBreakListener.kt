package com.liamxsage.energeticstorage.listeners

import com.liamxsage.energeticstorage.SYSTEM_ID_NAMESPACE
import com.liamxsage.energeticstorage.cache.SystemCache
import com.liamxsage.energeticstorage.extensions.sendMessagePrefixed
import com.liamxsage.energeticstorage.model.ESSystem
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import java.util.*

class BlockBreakListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent): Unit = with(event) {
        if (block.type != Material.CHISELED_BOOKSHELF) return@with
        if (!block.hasMetadata(SYSTEM_ID_NAMESPACE.key)) return@with
        val systemUUID = block.getMetadata(SYSTEM_ID_NAMESPACE.key).first().asString()
        val system = SystemCache.getSystem(UUID.fromString(systemUUID)) ?: ESSystem(UUID.fromString(systemUUID))

        isDropItems = false

        val drops = player.inventory.addItem(system.createSystemItem())
        if (drops.isNotEmpty()) {
            player.sendMessagePrefixed("Your inventory is full, the system has been dropped on the ground.")
            drops.values.forEach { player.world.dropItemNaturally(player.location, it) }
        }

        player.sendMessagePrefixed("Successfully removed system with ID $systemUUID.")
    }


}