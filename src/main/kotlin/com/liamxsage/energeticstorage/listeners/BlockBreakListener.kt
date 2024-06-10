package com.liamxsage.energeticstorage.listeners

import com.liamxsage.energeticstorage.DISK_DRIVE_ID_NAMESPACE
import com.liamxsage.energeticstorage.cache.DiskDriveCache
import com.liamxsage.energeticstorage.database.saveToDB
import com.liamxsage.energeticstorage.extensions.persistentDataContainer
import com.liamxsage.energeticstorage.extensions.sendMessagePrefixed
import com.liamxsage.energeticstorage.extensions.sendSuccessSound
import com.liamxsage.energeticstorage.model.DiskDrive
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class BlockBreakListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent): Unit = with(event) {
        if (block.type != Material.CHISELED_BOOKSHELF) return@with
        if (!block.persistentDataContainer.has(DISK_DRIVE_ID_NAMESPACE)) return@with
        val systemUUID = block.persistentDataContainer[DISK_DRIVE_ID_NAMESPACE, PersistentDataType.STRING] ?: return@with
        val system = DiskDriveCache.getDiskDriveByUUID(UUID.fromString(systemUUID)) ?: DiskDrive(UUID.fromString(systemUUID))

        isDropItems = false

        player.world.dropItemNaturally(block.location, system.createSystemItem())

        for (drive in system.disks) {
            player.world.dropItemNaturally(block.location, drive.createDiskItem())
            drive.diskDriveUUID = null
            drive.saveToDB()
        }
        system.disks.clear()
        DiskDriveCache.addDiskDrive(system)

        player.world.playSound(player.location, Sound.ENTITY_ITEM_FRAME_BREAK, 0.3f, 1f)

        player.sendMessagePrefixed("Successfully removed system.")
        player.sendSuccessSound()
    }


}