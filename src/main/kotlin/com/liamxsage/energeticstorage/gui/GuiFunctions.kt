package com.liamxsage.energeticstorage.gui

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.InventoryHolder

/**
 * Determines the [ClickType] for a given [InventoryClickEvent] based on the [InventoryHolder] and [title] of the clicked inventory.
 *
 * @param event The [InventoryClickEvent] that triggered the click.
 * @param title The [Component] title of the inventory.
 * @return The determined [ClickType] for the click.
 */
fun InventoryHolder.findClickType(event: InventoryClickEvent, title: Component): ClickType {
    val inventory = event.clickedInventory
    val isHolderClick = inventory?.holder == this

    return when {
        !isHolderClick -> determineClickTypeForNonHolderClick(event.view.title(), title, event.isShiftClick)
        else -> determineTypeForHolderClick(event)
    }
}

/**
 * Determines the [ClickType] for a non-holder click in an inventory.
 *
 * @param viewTitle the title of the inventory view
 * @param title the title of the inventory being clicked
 * @param isShiftClick indicates if the click is a shift-click
 * @return the determined [ClickType] for the click
 */
private fun determineClickTypeForNonHolderClick(
    viewTitle: Component,
    title: Component,
    isShiftClick: Boolean
): ClickType {
    return if (viewTitle == title) {
        if (isShiftClick) ClickType.SHIFT_IN else ClickType.INVENTORY_CLICK
    } else {
        ClickType.NONE
    }
}

/**
 * Determines the [ClickType] based on the [InventoryClickEvent].
 *
 * @param event the InventoryClickEvent
 * @return the determined ClickType
 */
private fun determineTypeForHolderClick(event: InventoryClickEvent): ClickType {
    val clickedItem = event.currentItem
    val cursor = event.cursor
    val isClickedItemEmpty = clickedItem == null || clickedItem.type == Material.AIR
    val isCursorEmpty = cursor.type == Material.AIR

    return when {
        isClickedItemEmpty && isCursorEmpty -> ClickType.NONE
        isClickedItemEmpty -> determineClickTypeForEmptyClickedItem(event.isLeftClick)
        isCursorEmpty -> determineClickTypeForEmptyCursor(event)
        else -> if (event.isLeftClick) ClickType.SWAP else ClickType.SWAP_RIGHT_CLICK
    }
}

/**
 * Determines the click type for an empty clicked item.
 *
 * @param isLeftClick Boolean indicating whether the click is a left click or not
 * @return The determined ClickType - either ClickType.INTO or ClickType.INTO_HALF
 */
private fun determineClickTypeForEmptyClickedItem(isLeftClick: Boolean): ClickType {
    return if (isLeftClick) ClickType.INTO else ClickType.INTO_HALF
}

/**
 * Determines the click type for an empty cursor in the InventoryClickEvent.
 *
 * @param event The InventoryClickEvent.
 * @return The ClickType corresponding to the click type for an empty cursor.
 */
private fun determineClickTypeForEmptyCursor(event: InventoryClickEvent): ClickType {
    return if (event.isShiftClick) {
        ClickType.SHIFT_OUT
    } else {
        if (event.isLeftClick) ClickType.OUT else ClickType.OUT_HALF
    }
}