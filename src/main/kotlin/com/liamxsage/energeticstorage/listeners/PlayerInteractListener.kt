package com.liamxsage.energeticstorage.listeners

import com.liamxsage.energeticstorage.DISK_ID_NAMESPACE
import com.liamxsage.energeticstorage.DISK_DRIVE_ID_NAMESPACE
import com.liamxsage.energeticstorage.cache.DiskCache
import com.liamxsage.energeticstorage.cache.DiskDriveCache
import com.liamxsage.energeticstorage.database.saveToDB
import com.liamxsage.energeticstorage.extensions.*
import com.liamxsage.energeticstorage.model.DiskDrive
import com.liamxsage.energeticstorage.network.NetworkInterfaceType
import com.liamxsage.energeticstorage.network.getConnectedNetworkInterfaces
import com.liamxsage.energeticstorage.network.getNetworkInterfaceType
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.ChiseledBookshelf
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class PlayerInteractListener : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent): Unit = with(event) {
        if (event.action != Action.RIGHT_CLICK_BLOCK || event.hand != EquipmentSlot.HAND || event.player.isSneaking) return@with
        if (clickedBlock == null) return@with
        val block = clickedBlock!!
        if (!block.isNetworkInterface) return@with
        val networkInterfaceType = getNetworkInterfaceType(block) ?: return@with
        isCancelled = true

        if (item?.isNetworkInterface == true) {
            return@with
        }

        if (networkInterfaceType == NetworkInterfaceType.DISK_DRIVE) {
            handleDiskDriveInteraction(block, player.inventory.itemInMainHand, player)
        }

        val connectedInterfaced = getConnectedNetworkInterfaces(clickedBlock!!)
        val interfacesSummedByType = connectedInterfaced.values.groupBy { it::class.java }.mapValues { it.value.size }

        player.sendMessagePrefixed("Clicked Network Interface: <green>${
            networkInterfaceType.name.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}")
        player.sendMessagePrefixed("Network Interfaces found:")
        interfacesSummedByType.forEach { (type, amount) ->
            player.sendMessagePrefixed("${type.simpleName}: $amount")
        }
    }

    private fun handleDiskDriveInteraction(block: Block, item: ItemStack?, player: Player) {
        if (!block.persistentDataContainer.has(DISK_DRIVE_ID_NAMESPACE)) return
        val diskDriveUUID = block.persistentDataContainer[DISK_DRIVE_ID_NAMESPACE, PersistentDataType.STRING] ?: return
        val diskDrive = DiskDriveCache.getDiskDriveByUUID(UUID.fromString(diskDriveUUID)) ?: DiskDrive(UUID.fromString(diskDriveUUID))

        if (item != null && item.type == Material.MUSIC_DISC_5 && item.hasKey(DISK_ID_NAMESPACE)) {
            val driveUUID = item.getKey(DISK_ID_NAMESPACE) ?: return
            val drive = DiskCache.getDiskByUUID(UUID.fromString(driveUUID)) ?: return

            if (!diskDrive.canFitDrive) {
                player.sendMessagePrefixed("All drive slots of this system are full.")
                return
            }

            if (diskDrive.disks.any { it.uuid == drive.uuid }) {
                player.sendMessagePrefixed("This drive is already inserted into the system. Did you dupe it?")
                return
            }

            drive.diskDriveUUID = diskDrive.uuid
            diskDrive.disks.add(drive)
            drive.saveToDB()
            if (item.amount == 1) player.inventory.removeItem(item)
            else item.amount = item.amount.minus(1)
            DiskDriveCache.addDiskDrive(diskDrive)

            val chiseledBookshelf = block!!.blockData as ChiseledBookshelf
            val drives = diskDrive.disks.size
            for (i in 0 until drives) {
                chiseledBookshelf.setSlotOccupied(i, true)
            }
            block.blockData = chiseledBookshelf

            player.sendMessagePrefixed("Successfully inserted drive.")
            player.sendSuccessSound()
            return
        }


        player.sendMessagePrefixed("Drives inserted: ${diskDrive.disks.size}/6")
        player.sendMessagePrefixed("Filled Items: ${diskDrive.totalItems}/${diskDrive.totalSize}")
        player.sendMessagePrefixed("Filled Types: ${diskDrive.totalTypes}/${diskDrive.totalTypesSize}")
        player.sendInfoSound()
    }
}