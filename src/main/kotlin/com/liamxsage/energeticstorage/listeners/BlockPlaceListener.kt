package com.liamxsage.energeticstorage.listeners

import com.liamxsage.energeticstorage.SYSTEM_ID_NAMESPACE
import com.liamxsage.energeticstorage.cache.SystemCache
import com.liamxsage.energeticstorage.extensions.getKey
import com.liamxsage.energeticstorage.extensions.hasKey
import com.liamxsage.energeticstorage.extensions.sendMessagePrefixed
import com.liamxsage.energeticstorage.items.StringMetadataValue
import com.liamxsage.energeticstorage.model.ESSystem
import org.bukkit.Material
import org.bukkit.block.data.type.ChiseledBookshelf
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import java.util.*

class BlockPlaceListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent): Unit = with(event) {
        if (block.type != Material.CHISELED_BOOKSHELF) return@with
        if (!itemInHand.hasKey(SYSTEM_ID_NAMESPACE)) return@with
        val systemUUID = itemInHand.getKey(SYSTEM_ID_NAMESPACE) ?: return@with
        val system = SystemCache.getSystem(UUID.fromString(systemUUID)) ?: ESSystem(UUID.fromString(systemUUID))

        val chiseledBookshelf = block.blockData as ChiseledBookshelf
        val drives = system.drives.size
        for (i in 0 until drives) {
            chiseledBookshelf.setSlotOccupied(i, true)
        }

        block.setMetadata(SYSTEM_ID_NAMESPACE.key, StringMetadataValue(systemUUID))

        player.sendMessagePrefixed("Successfully placed system with ID $systemUUID.")
    }

}