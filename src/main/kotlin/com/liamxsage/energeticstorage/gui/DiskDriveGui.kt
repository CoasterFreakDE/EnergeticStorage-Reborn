package com.liamxsage.energeticstorage.gui

import com.liamxsage.energeticstorage.DISK_ID_NAMESPACE
import com.liamxsage.energeticstorage.PREFIX
import com.liamxsage.energeticstorage.TEXT_GRAY
import com.liamxsage.energeticstorage.cache.DiskCache
import com.liamxsage.energeticstorage.database.saveToDB
import com.liamxsage.energeticstorage.extensions.*
import com.liamxsage.energeticstorage.model.Disk
import com.liamxsage.energeticstorage.model.DiskDrive
import dev.fruxz.stacked.text
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.*

class DiskDriveGui : InventoryHolder, Listener {

    companion object {
        lateinit var instance: DiskDriveGui
    }

    private val globalInv: Inventory
    private val title = text("$PREFIX<color:#b2c2d4>Disks</color>")

    private var openDiskDrives: Map<UUID, Pair<DiskDrive, Block>> = emptyMap()

    private val placeholderStack = Material.BLACK_STAINED_GLASS_PANE.toItemBuilder { display(" ") }.build()

    init {
        instance = this
        globalInv = Bukkit.createInventory(this, 9, title)
    }

    override fun getInventory(): Inventory {
        return globalInv
    }

    private fun initializeItems(player: Player, diskDrive: DiskDrive) {
        val inventory = player.openInventory.topInventory
        inventory.clear()

        inventory.setItem(0, Material.PAPER.toItemBuilder { display("${TEXT_GRAY}Back") }.build())
        listOf(1, 8).forEach { slot ->
            if (slot < inventory.size) {
                inventory.setItem(slot, placeholderStack)
            }
        }

        val disks = diskDrive.disks
        disks.forEachIndexed { index, disk ->
            val slotIndex = index + 2
            if (slotIndex < inventory.size) {
                inventory.setItem(slotIndex, disk.createDiskItem())
            }
        }
    }


    fun openInventory(player: Player, diskDrive: DiskDrive, block: Block) {
        player.openInventory(globalInv)
        initializeItems(player, diskDrive)
        player.sendOpenSound()

        openDiskDrives = openDiskDrives + (player.uniqueId to Pair(diskDrive, block))
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent): Unit = with(event) {
        if (view.title() != title) return
        val (diskDrive, block) = openDiskDrives[player.uniqueId] ?: return
        openDiskDrives = openDiskDrives - player.uniqueId
        diskDrive.disks.forEach { it.saveToDB() }
        diskDrive.updateBlock(block)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent): Unit = with(event) {
        if (view.title() != title) return
        val clickType = findClickType(event, title)
        if (clickType == ClickType.NONE) return
        isCancelled = true

        val player = whoClicked as Player
        val (diskDrive, block) = openDiskDrives[player.uniqueId] ?: return
        val thisInventory = player.openInventory.topInventory
        val playerInventory = player.inventory

        when(clickType) {
           in setOf(ClickType.SWAP, ClickType.SWAP_RIGHT_CLICK) -> return@with
            in setOf(ClickType.INVENTORY_CLICK, ClickType.SHIFT_IN, ClickType.INTO_HALF, ClickType.INTO) -> {
                val item = currentItem ?: return
                if (item.type != Material.MUSIC_DISC_5 || !item.hasKey(DISK_ID_NAMESPACE)) return@with
                val driveUUID = item.getKey(DISK_ID_NAMESPACE) ?: return
                val drive = DiskCache.getDiskByUUID(UUID.fromString(driveUUID)) ?: return

                if (!diskDrive.canFitDrive) {
                    player.sendDeniedSound()
                    return@with
                }

                if (diskDrive.disks.any { it.uuid == drive.uuid }) {
                    player.sendDeniedSound()
                    return@with
                }

                val firstEmpty = thisInventory.firstEmpty()
                if (firstEmpty == -1) {
                    player.sendDeniedSound()
                    return@with
                }

                drive.diskDriveUUID = diskDrive.uuid
                diskDrive.disks.add(drive)
                drive.saveToDB()
                playerInventory.removeItem(item)
                thisInventory.setItem(firstEmpty, item)

                player.sendInsertDiskSound()
                diskDrive.updateBlock(block)
            }
            in setOf(ClickType.OUT, ClickType.OUT_HALF, ClickType.SHIFT_OUT) -> {
                if (slot == 0) {
                    player.closeInventory()
                    return@with
                }
                if (slot in setOf(1, 8)) return@with

                val diskItem = thisInventory.getItem(slot) ?: return@with
                if (diskItem.type != Material.MUSIC_DISC_5 || !diskItem.hasKey(DISK_ID_NAMESPACE)) return@with

                val diskUUID = diskItem.getKey(DISK_ID_NAMESPACE) ?: return
                val disk = DiskCache.getDiskByUUID(UUID.fromString(diskUUID)) ?: Disk(UUID.fromString(diskUUID))

                val firstEmpty = playerInventory.firstEmpty()
                if (firstEmpty == -1) {
                    player.sendDeniedSound()
                    return@with
                }

                diskDrive.disks.remove(disk)
                disk.diskDriveUUID = null
                disk.saveToDB()

                thisInventory.setItem(slot, null)
                playerInventory.setItem(firstEmpty, diskItem)

                player.sendRemoveDiskSound()
                diskDrive.updateBlock(block)
            }
            else -> return@with
        }
    }
}