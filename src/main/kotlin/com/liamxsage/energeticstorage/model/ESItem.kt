package com.liamxsage.energeticstorage.model

import org.bukkit.inventory.ItemStack

data class ESItem(
    val itemStackAsSingle: ItemStack,
    var amount: Long
)
