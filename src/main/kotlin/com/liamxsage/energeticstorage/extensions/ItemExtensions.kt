package com.liamxsage.energeticstorage.extensions

import com.liamxsage.energeticstorage.items.ItemBuilder
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Converts a Material to an ItemBuilder and applies the provided DSL.
 *
 * @param dsl The DSL to be applied to the ItemBuilder.
 * @return The resulting ItemBuilder.
 */
fun Material.toItemBuilder(dsl: ItemBuilder.() -> Unit = {}): ItemBuilder {
    val mat = if (ItemBuilder.invalidMaterials.contains(this)) Material.GRASS_BLOCK else this
    return ItemBuilder(mat).apply(dsl)
}

/**
 * Converts an ItemStack to an ItemBuilder.
 *
 * @param dsl a lambda function that allows customization of the ItemBuilder.
 * @return the converted ItemBuilder.
 */
fun ItemStack.toItemBuilder(dsl: ItemBuilder.() -> Unit = {}): ItemBuilder {
    val mat = if (ItemBuilder.invalidMaterials.contains(this.type)) Material.GRASS_BLOCK else this.type
    val builder = ItemBuilder(mat)
    builder.itemStack = this
    builder.dsl()
    return builder
}