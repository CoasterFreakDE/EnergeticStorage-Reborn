package com.liamxsage.energeticstorage.extensions

import com.liamxsage.energeticstorage.NETWORK_INTERFACE_NAMESPACE
import com.liamxsage.energeticstorage.items.ItemBuilder
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataType

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


fun Material.asQuantity(amount: Int): ItemStack {
    return ItemStack(this, amount)
}

fun ItemStack.hasKey(namespacedKey: NamespacedKey): Boolean {
    return this.hasKey(namespacedKey, PersistentDataType.STRING)
}

fun <T, I> ItemStack.hasKey(namespacedKey: NamespacedKey, persistentDataType: PersistentDataType<T, I>): Boolean {
    return this.itemMeta?.persistentDataContainer?.has(
        namespacedKey,
        persistentDataType
    ) ?: false
}

fun ItemStack.getKey(namespacedKey: NamespacedKey): String? {
    return this.getKey(namespacedKey, PersistentDataType.STRING)
}

fun <T, I> ItemStack.getKey(namespacedKey: NamespacedKey, persistentDataType: PersistentDataType<T, I>): I? {
    return this.itemMeta?.persistentDataContainer?.get(
        namespacedKey,
        persistentDataType
    )
}

val ItemStack.isNetworkInterface: Boolean
    get() = hasKey(NETWORK_INTERFACE_NAMESPACE, PersistentDataType.BOOLEAN)