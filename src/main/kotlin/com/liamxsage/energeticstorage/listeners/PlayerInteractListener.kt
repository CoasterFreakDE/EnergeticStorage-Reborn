package com.liamxsage.energeticstorage.listeners

import com.liamxsage.energeticstorage.DISK_ID_NAMESPACE
import com.liamxsage.energeticstorage.DISK_DRIVE_ID_NAMESPACE
import com.liamxsage.energeticstorage.cache.DiskCache
import com.liamxsage.energeticstorage.cache.DiskDriveCache
import com.liamxsage.energeticstorage.database.saveToDB
import com.liamxsage.energeticstorage.extensions.*
import com.liamxsage.energeticstorage.model.DiskDrive
import org.bukkit.Material
import org.bukkit.block.data.type.ChiseledBookshelf
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import java.util.*

class PlayerInteractListener : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent): Unit = with(event) {
        if (event.action != Action.RIGHT_CLICK_BLOCK || event.hand != EquipmentSlot.HAND || event.player.isSneaking) return@with
        if (clickedBlock == null) return@with
        if (clickedBlock!!.type != Material.CHISELED_BOOKSHELF) return@with
        if (!clickedBlock!!.persistentDataContainer.has(DISK_DRIVE_ID_NAMESPACE)) return@with
        val systemUUID = clickedBlock!!.persistentDataContainer[DISK_DRIVE_ID_NAMESPACE, PersistentDataType.STRING] ?: return@with
        val system = DiskDriveCache.getDiskDriveByUUID(UUID.fromString(systemUUID)) ?: DiskDrive(UUID.fromString(systemUUID))

        if (item != null && item?.type == Material.MUSIC_DISC_5 && item?.hasKey(DISK_ID_NAMESPACE) == true) {
            val driveUUID = item?.getKey(DISK_ID_NAMESPACE) ?: return
            val drive = DiskCache.getDiskByUUID(UUID.fromString(driveUUID)) ?: return

            if (!system.canFitDrive) {
                player.sendMessagePrefixed("All drive slots of this system are full.")
                return
            }

            if (system.disks.any { it.uuid == drive.uuid }) {
                player.sendMessagePrefixed("This drive is already inserted into the system. Did you dupe it?")
                return
            }

            drive.diskDriveUUID = system.uuid
            system.disks.add(drive)
            drive.saveToDB()
            if (item?.amount == 1) player.inventory.removeItem(item!!)
            else item?.amount = item?.amount?.minus(1) ?: 0
            DiskDriveCache.addDiskDrive(system)

            val chiseledBookshelf = clickedBlock!!.blockData as ChiseledBookshelf
            val drives = system.disks.size
            for (i in 0 until drives) {
                chiseledBookshelf.setSlotOccupied(i, true)
            }
            clickedBlock!!.blockData = chiseledBookshelf

            player.sendMessagePrefixed("Successfully inserted drive.")
            player.sendSuccessSound()
            return
        }


        player.sendMessagePrefixed("Drives inserted: ${system.disks.size}/6")
        player.sendMessagePrefixed("Filled Items: ${system.totalItems}/${system.totalSize}")
        player.sendMessagePrefixed("Filled Types: ${system.totalTypes}/${system.totalTypesSize}")
        player.sendInfoSound()
    }

}