package com.liamxsage.energeticstorage.listeners

import com.liamxsage.energeticstorage.DISK_DRIVE_ID_NAMESPACE
import com.liamxsage.energeticstorage.cache.DiskDriveCache
import com.liamxsage.energeticstorage.database.saveToDB
import com.liamxsage.energeticstorage.extensions.isNetworkInterface
import com.liamxsage.energeticstorage.extensions.persistentDataContainer
import com.liamxsage.energeticstorage.extensions.sendMessagePrefixed
import com.liamxsage.energeticstorage.extensions.sendSuccessSound
import com.liamxsage.energeticstorage.model.Core
import com.liamxsage.energeticstorage.model.DiskDrive
import com.liamxsage.energeticstorage.model.Cable
import com.liamxsage.energeticstorage.model.Terminal
import com.liamxsage.energeticstorage.network.NetworkInterfaceType
import com.liamxsage.energeticstorage.network.getNetworkInterface
import com.liamxsage.energeticstorage.network.getNetworkInterfaceType
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class BlockBreakListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent): Unit = with(event) {
        if (!block.isNetworkInterface) return@with
        val networkInterfaceType = getNetworkInterfaceType(block) ?: return@with
        val networkInterface = getNetworkInterface(block) ?: return@with
        isDropItems = false

        if (networkInterfaceType == NetworkInterfaceType.DISK_DRIVE) {
            removeDiskDrive(block, player)
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
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}.")
        player.sendSuccessSound()
    }

    private fun removeDiskDrive(block: Block, player: Player) {
        if (block.type != Material.CHISELED_BOOKSHELF) return
        if (!block.persistentDataContainer.has(DISK_DRIVE_ID_NAMESPACE)) return
        val systemUUID = block.persistentDataContainer[DISK_DRIVE_ID_NAMESPACE, PersistentDataType.STRING] ?: return
        val system = DiskDriveCache.getDiskDriveByUUID(UUID.fromString(systemUUID)) ?: DiskDrive(UUID.fromString(systemUUID))

        for (drive in system.disks) {
            player.world.dropItemNaturally(block.location, drive.createDiskItem())
            drive.diskDriveUUID = null
            drive.saveToDB()
        }
        system.disks.clear()
        DiskDriveCache.addDiskDrive(system)
    }
}