package com.liamxsage.energeticstorage.gui

import com.liamxsage.energeticstorage.*
import com.liamxsage.energeticstorage.database.saveToDB
import com.liamxsage.energeticstorage.extensions.hasKey
import com.liamxsage.energeticstorage.extensions.sendOpenSound
import com.liamxsage.energeticstorage.extensions.toItemBuilder
import com.liamxsage.energeticstorage.model.Core
import com.liamxsage.energeticstorage.model.SortOrder
import dev.fruxz.stacked.text
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.max
import kotlin.math.min

class TerminalGui : InventoryHolder, Listener {

    companion object {
        lateinit var instance: TerminalGui
    }

    private val globalInv: Inventory
    private val title = text("$PREFIX<color:#b2c2d4>Disks</color>")

    private var openTerminals: Map<UUID, Core> = emptyMap()
    private var openPages: Map<UUID, Int> = emptyMap()
    private var selectedSortOrder: Map<UUID, SortOrder> = emptyMap()

    private val placeholderStack = Material.BLACK_STAINED_GLASS_PANE.toItemBuilder {
        display(" ")
        addPersistentData(GUI_FIXED_ITEM_NAMESPACE, "x")
    }.build()

    init {
        instance = this
        globalInv = Bukkit.createInventory(this, 9 * 6, title)
    }

    override fun getInventory(): Inventory {
        return globalInv
    }

    /**
     * Initializes the items in the player's inventory for the TerminalGui.
     *
     * @param player The Player whose inventory needs to be initialized.
     * @param core The Core object representing the network system.
     */
    private fun initializeItems(player: Player, core: Core) {
        val inventory = player.openInventory.topInventory
        inventory.clear()

        (0..8).forEach { slot ->
            inventory.setItem(slot, placeholderStack)
            inventory.setItem(slot + (9 * 5), placeholderStack)
        }

        (1..4).forEach { row ->
            inventory.setItem(row * 9, placeholderStack)
            inventory.setItem(row * 9 + 8, placeholderStack)
        }

        inventory.setItem(1, Material.LIME_STAINED_GLASS_PANE.toItemBuilder {
            display("${TEXT_GRAY}To insert items, put them to the left.")
            addPersistentData(GUI_FIXED_ITEM_NAMESPACE, "x")
        }.build())
        inventory.setItem(9, Material.LIME_STAINED_GLASS_PANE.toItemBuilder {
            display("${TEXT_GRAY}To insert items, put them above.")
            addPersistentData(GUI_FIXED_ITEM_NAMESPACE, "x")
        }.build())


        inventory.setItem(49, Material.COMPASS.toItemBuilder {
            display("${TEXT_GRAY}Search Items")
            addPersistentData(GUI_FIXED_ITEM_NAMESPACE, "x")
        }.build())
        inventory.setItem(48, Material.PAPER.toItemBuilder {
            display("${TEXT_GRAY}Previous Page")
            addPersistentData(GUI_FIXED_ITEM_NAMESPACE, "x")
            onClick {
                val pageIndex = openPages[player.uniqueId] ?: 0
                openPages = openPages + (player.uniqueId to max(0, pageIndex - 1))
                initializeItems(player, core)
            }
        }.build())
        inventory.setItem(50, Material.PAPER.toItemBuilder {
            display("${TEXT_GRAY}Next Page")
            addPersistentData(GUI_FIXED_ITEM_NAMESPACE, "x")
            onClick {
                val pageIndex = openPages[player.uniqueId] ?: 0
                openPages = openPages + (player.uniqueId to min((core.totalItems / 28).toInt(), pageIndex + 1))
                initializeItems(player, core)
            }
        }.build())

        val pageIndex = openPages[player.uniqueId] ?: 0
        val sortOrder = selectedSortOrder[player.uniqueId] ?: SortOrder.ALPHABETICAL
        val items = core.getItems(sortOrder, pageIndex)

        // We need to insert the items in a 7 x 4 grid to fit in the middle of the 9 x 6 inventory.
        items.forEachIndexed { index, item ->
            val row = index / 7
            val column = index % 7
            val slot = (row + 1) * 9 + column + 1
            inventory.setItem(slot, item.itemStackAsSingle.clone().toItemBuilder {
                asAmount(min(item.amount, item.itemStackAsSingle.maxStackSize.toLong()).toInt())
                addPersistentData(ITEM_AMOUNT_NAMESPACE, PersistentDataType.LONG, item.amount)
                lore(
                    "${TEXT_GRAY}Amount: <green>${item.amount}",
                    "",
                    "${TEXT_GRAY}Left-Click to get one,",
                    "${TEXT_GRAY}Right-Click to get half (up to 32).",
                    "${TEXT_GRAY}Shift-Click to get a full stack."
                )
            }.build())
        }

        inventory.setItem(46, Material.CHISELED_BOOKSHELF.toItemBuilder {
            display("${TEXT_GRAY}<bold>Drives")
            addPersistentData(GUI_FIXED_ITEM_NAMESPACE, "x")
            lore(
                "${TEXT_GRAY}Total Items: ${core.totalItems}/${core.totalSize}",
                "${TEXT_GRAY}Total Types: ${core.totalTypes}/${core.totalTypesSize}",
                "${TEXT_GRAY}Total Disks: ${core.totalDisks}/${core.connectedDiskDrives.size * 6}"
            )
        }.build())

        inventory.setItem(47, Material.HOPPER.toItemBuilder {
            display("${TEXT_GRAY}<bold>Sort Order")
            addPersistentData(GUI_FIXED_ITEM_NAMESPACE, "x")
            lore(
                "${TEXT_GRAY}Current: <green>${sortOrder.displayString}",
                "${TEXT_GRAY}Click to cycle.",
                " ",
                "${TEXT_GRAY}Available Sort Orders:",
                "${TEXT_GRAY}<i>${
                    SortOrder.entries.joinToString(", ") { sortOrderEntry ->
                        if (sortOrderEntry == sortOrder) "<green>${sortOrderEntry.displayString}</green>" else sortOrderEntry.displayString
                    }
                }"
            )
            onClick {
                val currentSortOrder = selectedSortOrder[player.uniqueId] ?: SortOrder.ALPHABETICAL
                val nextSortOrder = SortOrder.entries.let { sortOrders ->
                    val currentIndex = sortOrders.indexOf(currentSortOrder)
                    val nextIndex = (currentIndex + 1) % sortOrders.size
                    sortOrders[nextIndex]
                }
                selectedSortOrder = selectedSortOrder + (player.uniqueId to nextSortOrder)
                initializeItems(player, core)
            }
        }.build())

        inventory.setItem(0, null)
    }

    fun openInventory(player: Player, core: Core) {
        player.openInventory(globalInv)
        initializeItems(player, core)
        player.sendOpenSound()

        openTerminals = openTerminals + (player.uniqueId to core)
        openPages = openPages + (player.uniqueId to 0)
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent): Unit = with(event) {
        if (inventory.holder == null || inventory.holder != instance) return
        event.isCancelled = true
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent): Unit = with(event) {
        if (view.title() != title) return
        val core = openTerminals[player.uniqueId] ?: return
        core.connectedDiskDrives.forEach { diskDrive -> diskDrive.disks.forEach { it.saveToDB() } }
        openTerminals = openTerminals - player.uniqueId
        openPages = openPages - player.uniqueId
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent): Unit = with(event) {
        if (view.title() != title) return
        val player = whoClicked as Player
        val core = openTerminals[player.uniqueId] ?: return
        val clickType = findClickType(event, title)
        if (clickType == ClickType.NONE) return
        if (currentItem == null) return
        player.playSound(player, Sound.UI_BUTTON_CLICK, 0.3f, 1f)

        val clickedItem = currentItem!!
        if (clickedItem.hasKey(GUI_FIXED_ITEM_NAMESPACE)) {
            isCancelled = true
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.3f, 1f)
            return
        }

        when (clickType) {
            ClickType.SHIFT_IN -> {
                if (!core.addItemToSystem(clickedItem)) return@with
                isCancelled = false

                Bukkit.getScheduler().runTaskLater(EnergeticStorage.instance, Runnable {
                    initializeItems(player, core)
                }, 1L)
            }

            ClickType.SWAP_RIGHT_CLICK -> {
                // This will take an item out one by one when the player is holding the same material.
                if (cursor.amount >= cursor.maxStackSize) return
                val takingItem = cursor.clone()
                takingItem.amount = 1
                core.removeItemFromSystem(takingItem)
                cursor.amount += 1

                Bukkit.getScheduler().runTaskLater(EnergeticStorage.instance, Runnable {
                    initializeItems(player, core)
                }, 1L)
            }

            ClickType.SWAP -> {
                isCancelled = true

                if (!core.addItemToSystem(cursor)) return@with
                view.setCursor(null)

                Bukkit.getScheduler().runTaskLater(EnergeticStorage.instance, Runnable {
                    initializeItems(player, core)
                }, 1L)
            }

            ClickType.INTO_HALF -> {
                val itemStack = cursor.clone()
                itemStack.amount = 1
                if (!core.addItemToSystem(itemStack)) return@with
                isCancelled = false

                Bukkit.getScheduler().runTaskLater(EnergeticStorage.instance, Runnable {
                    initializeItems(player, core)
                }, 1L)
            }

            ClickType.INTO -> {
                if (!core.addItemToSystem(cursor)) return@with
                isCancelled = false

                Bukkit.getScheduler().runTaskLater(EnergeticStorage.instance, Runnable {
                    initializeItems(player, core)
                }, 1L)
            }

            ClickType.SHIFT_OUT -> {
                val toRemoveStack = clickedItem.clone()
                val addingESItem =
                    core.getFirstMatchingItem(clickedItem.clone()) ?: return@with run { isCancelled = true }
                val addingItem = addingESItem.itemStackAsSingle.asQuantity(toRemoveStack.amount)

                val leftOverItems = player.inventory.addItem(addingItem)
                if (leftOverItems.isNotEmpty()) {
                    val leftOver = leftOverItems[0]
                    toRemoveStack.amount -= leftOver?.amount ?: 0
                }

                core.removeItemFromSystem(toRemoveStack)

                Bukkit.getScheduler().runTaskLater(EnergeticStorage.instance, Runnable {
                    initializeItems(player, core)
                }, 1L)
            }

            in setOf(ClickType.OUT, ClickType.OUT_HALF) -> {
                val takingItem = clickedItem.clone()
                takingItem.amount =
                    if ((clickType == ClickType.OUT_HALF && clickedItem.amount / 2 > 0)) clickedItem.amount / 2 else clickedItem.maxStackSize
                val addingESItem =
                    core.getFirstMatchingItem(clickedItem.clone()) ?: return@with run { isCancelled = true }
                val addingItem = addingESItem.itemStackAsSingle.asQuantity(takingItem.amount)

                core.removeItemFromSystem(takingItem)
                view.setCursor(addingItem)

                Bukkit.getScheduler().runTaskLater(EnergeticStorage.instance, Runnable {
                    initializeItems(player, core)
                }, 1L)
            }

            else -> return@with run { isCancelled = true }
        }
    }

}