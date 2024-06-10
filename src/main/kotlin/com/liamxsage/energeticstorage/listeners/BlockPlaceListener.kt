package com.liamxsage.energeticstorage.listeners

import com.liamxsage.energeticstorage.DISK_DRIVE_ID_NAMESPACE
import com.liamxsage.energeticstorage.cache.DiskDriveCache
import com.liamxsage.energeticstorage.extensions.*
import com.liamxsage.energeticstorage.model.DiskDrive
import org.bukkit.Material
import org.bukkit.block.data.type.ChiseledBookshelf
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class BlockPlaceListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent): Unit = with(event) {
        if (block.type != Material.CHISELED_BOOKSHELF) return@with
        if (!itemInHand.hasKey(DISK_DRIVE_ID_NAMESPACE)) return@with
        val systemUUID = itemInHand.getKey(DISK_DRIVE_ID_NAMESPACE) ?: return@with
        val system = DiskDriveCache.getDiskDriveByUUID(UUID.fromString(systemUUID)) ?: DiskDrive(UUID.fromString(systemUUID))

        val chiseledBookshelf = block.blockData as ChiseledBookshelf
        val drives = system.disks.size
        for (i in 0 until drives) {
            chiseledBookshelf.setSlotOccupied(i, true)
        }
        block.blockData = chiseledBookshelf
        block.persistentDataContainer[DISK_DRIVE_ID_NAMESPACE, PersistentDataType.STRING] = systemUUID

        player.sendMessagePrefixed("Successfully placed ESSystem.")
        player.sendSuccessSound()
    }

}