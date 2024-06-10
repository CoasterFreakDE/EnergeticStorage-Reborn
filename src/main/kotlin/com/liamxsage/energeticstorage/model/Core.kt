package com.liamxsage.energeticstorage.model

import com.liamxsage.energeticstorage.NETWORK_INTERFACE_NAMESPACE
import com.liamxsage.energeticstorage.TEXT_GRAY
import com.liamxsage.energeticstorage.extensions.toItemBuilder
import com.liamxsage.energeticstorage.network.NetworkInterface
import com.liamxsage.energeticstorage.network.NetworkInterfaceType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class Core : NetworkInterface {

    /**
     * Creates an ItemStack representing the core item for the Network.
     *
     * @return The created core item.
     */
    fun createCoreItem(): ItemStack = NetworkInterfaceType.CORE.material.toItemBuilder {
        display("${TEXT_GRAY}Core")
        lore(
            "${TEXT_GRAY}Heart of the system",
            "${TEXT_GRAY}Needs to be inserted into a system to function.",
            "${TEXT_GRAY}Maximum of 1 per system."
        )
        setGlinting(true)
        customModelData(1)
        addPersistentData(NETWORK_INTERFACE_NAMESPACE, PersistentDataType.BOOLEAN, true)
        flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS)
    }.build()

}