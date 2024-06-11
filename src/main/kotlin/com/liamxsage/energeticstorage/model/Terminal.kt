package com.liamxsage.energeticstorage.model

import com.liamxsage.energeticstorage.NETWORK_INTERFACE_ID_NAMESPACE
import com.liamxsage.energeticstorage.NETWORK_INTERFACE_NAMESPACE
import com.liamxsage.energeticstorage.TEXT_GRAY
import com.liamxsage.energeticstorage.cache.NetworkInterfaceCache
import com.liamxsage.energeticstorage.extensions.persistentDataContainer
import com.liamxsage.energeticstorage.extensions.toItemBuilder
import com.liamxsage.energeticstorage.network.NetworkInterface
import com.liamxsage.energeticstorage.network.NetworkInterfaceType
import org.bukkit.block.Block
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class Terminal(override val uuid: UUID = UUID.randomUUID()) : NetworkInterface {

    var connectedCoreUUID: UUID? = null

    init {
        NetworkInterfaceCache.addNetworkInterface(this)
    }

    override fun setBlockUUID(block: Block): Terminal {
        block.persistentDataContainer[NETWORK_INTERFACE_ID_NAMESPACE, PersistentDataType.STRING] = uuid.toString()
        return this
    }

    /**
     * Creates an ItemStack representing a terminal item for the Network.
     *
     * @return The created terminal item.
     */
    fun createTerminalItem(): ItemStack = NetworkInterfaceType.TERMINAL.material.toItemBuilder {
        display("${TEXT_GRAY}Terminal")
        lore(
            "${TEXT_GRAY}Interact with your network",
        )
        setGlinting(true)
        customModelData(1)
        addPersistentData(NETWORK_INTERFACE_NAMESPACE, PersistentDataType.BOOLEAN, true)
        flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS)
    }.build()

}