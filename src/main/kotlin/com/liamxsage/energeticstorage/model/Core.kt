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

    val connectedDiskDrives = mutableListOf<DiskDrive>()
    val connectedTerminals = mutableListOf<Terminal>()

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


    /**
     * Represents the total number of disks in the system.
     *
     * This variable is calculated by summing up the number of disks in each connected disk drive.
     *
     * @return The total number of disks.
     */
    val totalDisks: Int
        get() = connectedDiskDrives.sumOf { it.disks.size }

    /**
     * Represents the total number of items stored in the connected disk drives.
     *
     * This property is calculated by summing up the totalItems property of each DiskDrive
     * in the connectedDiskDrives list.
     * @see DiskDrive
     *
     * @property totalItems The total number of items stored in the connected disk drives.
     */
    val totalItems: Long
        get() = connectedDiskDrives.sumOf { it.totalItems }

    /**
     * Represents the total number of types of items stored in the disk drives of a network system.
     *
     * This variable is calculated by summing up the totalTypes property of each connected disk drive in the network.
     *
     * @see Core
     * @see DiskDrive
     */
    val totalTypes: Int
        get() = connectedDiskDrives.sumOf { it.totalTypes }

    /**
     * Represents the possible total size of types stored in connected disk drives.
     *
     * This variable is calculated by summing up the totalTypesSize property of each connected disk drive
     * in the connectedDiskDrives list.
     *
     * @see DiskDrive
     */
    val totalTypesSize: Int
        get() = connectedDiskDrives.sumOf { it.totalTypesSize }

    /**
     * Represents the possible total size of all connected disk drives in the system.
     *
     * This variable is calculated by summing up the totalSize property of each connected disk drive.
     *
     * @see connectedDiskDrives
     * @see DiskDrive
     */
    val totalSize: Long
        get() = connectedDiskDrives.sumOf { it.totalSize }
}