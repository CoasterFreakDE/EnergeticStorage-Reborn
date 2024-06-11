package com.liamxsage.energeticstorage.listeners

import com.liamxsage.energeticstorage.NETWORK_INTERFACE_ID_NAMESPACE
import com.liamxsage.energeticstorage.cache.NetworkInterfaceCache
import com.liamxsage.energeticstorage.database.saveToDB
import com.liamxsage.energeticstorage.extensions.isNetworkInterface
import com.liamxsage.energeticstorage.extensions.persistentDataContainer
import com.liamxsage.energeticstorage.extensions.sendMessagePrefixed
import com.liamxsage.energeticstorage.extensions.sendSuccessSound
import com.liamxsage.energeticstorage.model.Cable
import com.liamxsage.energeticstorage.model.Core
import com.liamxsage.energeticstorage.model.DiskDrive
import com.liamxsage.energeticstorage.model.Terminal
import com.liamxsage.energeticstorage.network.NetworkInterfaceType
import com.liamxsage.energeticstorage.network.getNetworkInterface
import com.liamxsage.energeticstorage.network.getNetworkInterfaceFromBlock
import com.liamxsage.energeticstorage.network.getNetworkInterfaceType
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import java.util.*

class BlockBreakListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent): Unit = with(event) {
        if (!block.isNetworkInterface) return@with
        val networkInterfaceType = getNetworkInterfaceType(block) ?: return@with
        val networkInterface = getNetworkInterface(block) ?: return@with
        isDropItems = false

        when (networkInterfaceType) {
            NetworkInterfaceType.DISK_DRIVE -> removeDiskDrive(block, player)
            NetworkInterfaceType.TERMINAL -> removeTerminal(block)
            else -> { /* Do nothing */ }
        }

        val itemStack = when (networkInterface) {
            is DiskDrive -> networkInterface.createDiskDriveItem()
            is Core -> networkInterface.createCoreItem()
            is Cable -> networkInterface.createCableItem()
            is Terminal -> networkInterface.createTerminalItem()
            else -> return@with
        }

        player.world.dropItemNaturally(block.location, itemStack)
        player.world.playSound(player.location, Sound.ENTITY_ITEM_FRAME_BREAK, 0.3f, 1f)

        player.sendMessagePrefixed("Successfully removed ${
            networkInterfaceType.name.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }."
        )
        player.sendSuccessSound()
    }

    /**
     * Removes the disk drive and drops any disks it contains.
     *
     * @param block The block representing the disk drive.
     * @param player The player who triggered the removal.
     */
    private fun removeDiskDrive(block: Block, player: Player) {
        val diskDrive = getNetworkInterfaceFromBlock<DiskDrive>(block)

        for (drive in diskDrive.disks) {
            player.world.dropItemNaturally(block.location, drive.createDiskItem())
            drive.diskDriveUUID = null
            drive.saveToDB()
        }
        diskDrive.disks.clear()
        NetworkInterfaceCache.addNetworkInterface(diskDrive)

        if (diskDrive.connectedCoreUUID == null) return
        val core =
            NetworkInterfaceCache.getNetworkInterfaceByUUID(diskDrive.connectedCoreUUID!!) as? Core ?: return

        core.connectedDiskDrives.remove(diskDrive)
        NetworkInterfaceCache.addNetworkInterface(core)
    }

    /**
     * Removes the terminal from the network.
     *
     * @param block The block containing the terminal.
     */
    private fun removeTerminal(block: Block) {
        val terminal = getNetworkInterfaceFromBlock<Terminal>(block)

        if (terminal.connectedCoreUUID == null) return
        val core =
            NetworkInterfaceCache.getNetworkInterfaceByUUID(terminal.connectedCoreUUID!!) as? Core ?: return

        core.connectedTerminals.remove(terminal)
        NetworkInterfaceCache.addNetworkInterface(core)
    }
}