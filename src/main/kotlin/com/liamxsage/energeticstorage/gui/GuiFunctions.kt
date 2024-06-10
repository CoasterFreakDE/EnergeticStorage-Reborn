package com.liamxsage.energeticstorage.gui

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.InventoryHolder

fun InventoryHolder.findClickType(event: InventoryClickEvent, title: Component): ClickType {
    val inventory = event.clickedInventory

    if ((inventory == null) || inventory.holder == null || inventory.holder !== this) {
        // Check for a shift click or bottom inventory click.
        if (event.view.title() == title) {
            return if ((event.isShiftClick)) ClickType.SHIFT_IN else ClickType.INVENTORY_CLICK
        }

        return ClickType.NONE
    }

    val clickedItem = event.currentItem
    val cursor = event.cursor

    return when {
        (clickedItem == null || clickedItem.type == Material.AIR) && (cursor.type == Material.AIR) -> {
            ClickType.NONE
        }

        clickedItem == null || clickedItem.type == Material.AIR -> {
            if ((event.isLeftClick)) ClickType.INTO else ClickType.INTO_HALF
        }

        cursor.type == Material.AIR -> {
            if ((event.isShiftClick)) ClickType.SHIFT_OUT else if ((event.isLeftClick)) ClickType.OUT else ClickType.OUT_HALF
        }

        else -> if ((event.isLeftClick)) ClickType.SWAP else ClickType.SWAP_RIGHT_CLICK
    }
}