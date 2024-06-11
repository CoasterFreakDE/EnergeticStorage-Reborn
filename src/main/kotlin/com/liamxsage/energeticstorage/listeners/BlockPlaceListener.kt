package com.liamxsage.energeticstorage.listeners

import com.liamxsage.energeticstorage.NETWORK_INTERFACE_ID_NAMESPACE
import com.liamxsage.energeticstorage.NETWORK_INTERFACE_NAMESPACE
import com.liamxsage.energeticstorage.cache.NetworkInterfaceCache
import com.liamxsage.energeticstorage.extensions.*
import com.liamxsage.energeticstorage.model.DiskDrive
import com.liamxsage.energeticstorage.network.NetworkInterfaceType
import com.liamxsage.energeticstorage.network.getNetworkInterfaceType
import com.liamxsage.energeticstorage.network.updateNetworkCoreWithConnectedInterfaces
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.ChiseledBookshelf
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class BlockPlaceListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent): Unit = with(event) {
        if (!itemInHand.isNetworkInterface) return@with
        val networkInterfaceType = getNetworkInterfaceType(itemInHand) ?: return@with

        if (networkInterfaceType == NetworkInterfaceType.DISK_DRIVE) {
            placeDiskDrive(block, itemInHand)
        }
        block.persistentDataContainer[NETWORK_INTERFACE_NAMESPACE, PersistentDataType.BOOLEAN] = true

        try {
            updateNetworkCoreWithConnectedInterfaces(block)
        } catch (e: AssertionError) {
            player.sendMessagePrefixed("Multiple Cores detected. Please remove one.")
            player.sendDeniedSound()
            isCancelled = true
            return
        }

        player.sendMessagePrefixed("Successfully placed ${
            networkInterfaceType.name.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }."
        )
        player.sendSuccessSound()
    }

    private fun placeDiskDrive(block: Block, itemInHand: ItemStack) {
        if (block.type != Material.CHISELED_BOOKSHELF) return
        if (!itemInHand.hasKey(NETWORK_INTERFACE_ID_NAMESPACE)) return
        val diskDriveUUID = itemInHand.getKey(NETWORK_INTERFACE_ID_NAMESPACE) ?: return
        val diskDrive =
            NetworkInterfaceCache.getNetworkInterfaceByUUID(UUID.fromString(diskDriveUUID)) as? DiskDrive ?: DiskDrive(
                UUID.fromString(diskDriveUUID)
            )

        val chiseledBookshelf = block.blockData as ChiseledBookshelf
        val drives = diskDrive.disks.size
        for (i in 0 until drives) {
            chiseledBookshelf.setSlotOccupied(i, true)
        }
        block.blockData = chiseledBookshelf
        block.persistentDataContainer[NETWORK_INTERFACE_ID_NAMESPACE, PersistentDataType.STRING] = diskDriveUUID
    }
}