package com.liamxsage.energeticstorage.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack

@Serializable
data class ESItem(
    @Contextual val itemStackAsSingle: ItemStack,
    val amount: Long
)
