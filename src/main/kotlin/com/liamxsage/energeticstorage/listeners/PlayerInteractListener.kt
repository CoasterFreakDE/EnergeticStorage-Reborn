package com.liamxsage.energeticstorage.listeners

import com.liamxsage.energeticstorage.DISK_DRIVE_ID_NAMESPACE
import com.liamxsage.energeticstorage.DISK_ID_NAMESPACE
import com.liamxsage.energeticstorage.cache.DiskCache
import com.liamxsage.energeticstorage.cache.DiskDriveCache
import com.liamxsage.energeticstorage.database.saveToDB
import com.liamxsage.energeticstorage.extensions.*
import com.liamxsage.energeticstorage.gui.DiskDriveGui
import com.liamxsage.energeticstorage.model.DiskDrive
import com.liamxsage.energeticstorage.network.NetworkInterfaceType
import com.liamxsage.energeticstorage.network.getConnectedNetworkInterfaces
import com.liamxsage.energeticstorage.network.getNetworkInterfaceType
import org.bukkit.Material
import org.bukkit.block.Block
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
        if (item?.isNetworkInterface == true) return@with

        handleClickOnNetworkInterface(networkInterfaceType, block)
    }

    /**
     * Handles the interaction with the network interface based on the provided network interface type.
     *
     * @param networkInterfaceType The type of the network interface.
     * @param block The block that was clicked.
     */
    private fun PlayerInteractEvent.handleClickOnNetworkInterface(
        networkInterfaceType: NetworkInterfaceType,
        block: Block
    ) {
        when (networkInterfaceType) {
            NetworkInterfaceType.DISK_DRIVE -> handleDiskDriveInteraction(
                block,
                player.inventory.itemInMainHand,
                player
            )

            NetworkInterfaceType.TERMINAL -> player.sendMessagePrefixed("Terminal")
            else -> { /* Do nothing */
            }
        }

        if (!player.isESDebugModeEnabled) return
        sendDebugInfo(networkInterfaceType)
    }

    /**
     * Sends debug information about the player's interaction with a network interface block.
     *
     * @param networkInterfaceType The type of the network interface.
     */
    private fun PlayerInteractEvent.sendDebugInfo(networkInterfaceType: NetworkInterfaceType) {
        // The following code is only for debugging purposes
        val connectedInterfaced = getConnectedNetworkInterfaces(clickedBlock!!)
        val interfacesSummedByType = connectedInterfaced.values.groupBy { it::class.java }.mapValues { it.value.size }
        player.sendMessagePrefixed("Clicked Network Interface: <green>${
            networkInterfaceType.name.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }"
        )
        player.sendMessagePrefixed("Network Interfaces found:")
        interfacesSummedByType.forEach { (type, amount) ->
            player.sendMessagePrefixed("${type.simpleName}: $amount")
        }
        player.sendInfoSound()
    }

    /**
     * Handles the interaction with a disk drive.
     *
     * @param block The block representing the disk drive.
     * @param item The item held by the player.
     * @param player The player interacting with the disk drive.
     */
    private fun handleDiskDriveInteraction(block: Block, item: ItemStack?, player: Player) {
        if (!block.persistentDataContainer.has(DISK_DRIVE_ID_NAMESPACE)) return
        val diskDriveUUID = block.persistentDataContainer[DISK_DRIVE_ID_NAMESPACE, PersistentDataType.STRING] ?: return
        val diskDrive = DiskDriveCache.getDiskDriveByUUID(UUID.fromString(diskDriveUUID)) ?: DiskDrive(UUID.fromString(diskDriveUUID))

        if (tryInsertingDiskIntoDrive(item, diskDrive, player, block)) return

        DiskDriveGui.instance.openInventory(player, diskDrive, block)

        if (!player.isESDebugModeEnabled) return

        player.sendMessagePrefixed("Drives inserted: ${diskDrive.disks.size}/6")
        player.sendMessagePrefixed("Filled Items: ${diskDrive.totalItems}/${diskDrive.totalSize}")
        player.sendMessagePrefixed("Filled Types: ${diskDrive.totalTypes}/${diskDrive.totalTypesSize}")
    }

    /**
     * Tries to insert a disk into a disk drive.
     *
     * @param item The item to insert into the disk drive.
     * @param diskDrive The disk drive to insert the item into.
     * @param player The player performing the action.
     * @param block The block representing the disk drive.
     * @return True if the disk was successfully inserted, false otherwise.
     */
    private fun tryInsertingDiskIntoDrive(
        item: ItemStack?,
        diskDrive: DiskDrive,
        player: Player,
        block: Block
    ): Boolean {
        if (item == null || item.type != Material.MUSIC_DISC_5 || !item.hasKey(DISK_ID_NAMESPACE)) return false
        val driveUUID = item.getKey(DISK_ID_NAMESPACE) ?: return false
        val drive = DiskCache.getDiskByUUID(UUID.fromString(driveUUID)) ?: return false

        if (!diskDrive.canFitDrive) {
            player.sendMessagePrefixed("All drive slots of this system are full.")
            return true
        }

        if (diskDrive.disks.any { it.uuid == drive.uuid }) {
            player.sendMessagePrefixed("This drive is already inserted into the system. Did you dupe it?")
            return true
        }

        drive.diskDriveUUID = diskDrive.uuid
        diskDrive.disks.add(drive)
        drive.saveToDB()
        if (item.amount == 1) player.inventory.removeItem(item)
        else item.amount = item.amount.minus(1)
        DiskDriveCache.addDiskDrive(diskDrive)
        diskDrive.updateBlock(block)

        player.sendMessagePrefixed("Successfully inserted drive.")
        player.sendSuccessSound()
        return true
    }
}