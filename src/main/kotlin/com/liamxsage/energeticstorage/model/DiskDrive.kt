package com.liamxsage.energeticstorage.model

import com.liamxsage.energeticstorage.NETWORK_INTERFACE_ID_NAMESPACE
import com.liamxsage.energeticstorage.NETWORK_INTERFACE_NAMESPACE
import com.liamxsage.energeticstorage.TEXT_GRAY
import com.liamxsage.energeticstorage.cache.NetworkInterfaceCache
import com.liamxsage.energeticstorage.database.loadDisks
import com.liamxsage.energeticstorage.extensions.persistentDataContainer
import com.liamxsage.energeticstorage.extensions.toItemBuilder
import com.liamxsage.energeticstorage.network.NetworkInterface
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.ChiseledBookshelf
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

@Serializable
data class DiskDrive(
    @Contextual override val uuid: UUID = UUID.randomUUID(),
    val disks: MutableList<Disk> = mutableListOf()
) : NetworkInterface {

    constructor(uuid: UUID) : this(uuid, mutableListOf())

    init {
        loadDisks()

        NetworkInterfaceCache.addNetworkInterface(this)
    }

    override fun setBlockUUID(block: Block): NetworkInterface {
        block.persistentDataContainer[NETWORK_INTERFACE_ID_NAMESPACE, PersistentDataType.STRING] = uuid.toString()
        return this
    }

    /**
     * Creates an ItemStack representing the system item for the ESSystem.
     *
     * @return The created system item.
     */
    fun createDiskDriveItem(): ItemStack = Material.CHISELED_BOOKSHELF.toItemBuilder {
        display("${TEXT_GRAY}Energetic Storage System")
        lore(
            "${TEXT_GRAY}Drives inserted: <color:#b8e994>${totalDrives}</color>/<color:#26de81>6</color>",
            "${TEXT_GRAY}Filled Items: ${colorItems}/<color:#26de81>${totalSize}</color>",
            "${TEXT_GRAY}Filled Types: ${colorTypes}/<color:#26de81>${totalTypesSize}</color>",
        )
        setGlinting(true)
        customModelData(1)
        addPersistentData(NETWORK_INTERFACE_ID_NAMESPACE, uuid.toString())
        addPersistentData(NETWORK_INTERFACE_NAMESPACE, PersistentDataType.BOOLEAN, true)
        flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS)
    }.build()

    /**
     * Checks if a drive can fit in the available disk slots.
     *
     * @return true if there are less than 6 disks, false otherwise.
     */
    val canFitDrive: Boolean
        get() = disks.size < 6

    /**
     * Represents the total number of items present in the disks of a DiskDrive.
     *
     * @return The total number of items.
     */
    val totalItems: Long
        get() = disks.sumOf { it.totalItems }

    /**
     * Represents the total number of types of items stored in the disk drive.
     *
     * This variable is calculated by summing up the totalTypes property of each disk in the disks list.
     *
     * @see DiskDrive
     * @see Disk
     */
    val totalTypes: Int
        get() = disks.sumOf { it.totalTypes }

    /**
     * Represents the total number of disk drives in the system.
     */
    private val totalDrives: Int
        get() = disks.size

    /**
     * Represents the total size of all disks in a disk drive.
     *
     * @property totalSize The total size of all disks.
     */
    val totalSize: Long
        get() = disks.sumOf { it.size.size }

    /**
     *
     */
    val totalTypesSize: Int
        get() = disks.sumOf { it.size.types }

    /**
     * Represents a string that contains color-coded items information.
     * The color code is specified in the form of hexadecimal color values.
     * The value of this variable is determined based on the `totalItems` and `totalSize` properties.
     */
    private val colorItems: String
        get() = when {
            totalItems > ((totalSize / 10) * 8) -> "<color:#fc5c65>$totalItems</color>"
            totalItems > (totalSize / 2) -> "<color:#fed330>$totalItems</color>"
            else -> "<color:#b8e994>$totalItems</color>"
        }

    /**
     * The color representation of the total types.
     */
    private val colorTypes: String
        get() = when {
            totalTypes > ((totalTypesSize / 10) * 8) -> "<color:#fc5c65>$totalTypes</color>"
            totalTypes > (totalTypesSize / 2) -> "<color:#fed330>$totalTypes</color>"
            else -> "<color:#b8e994>$totalTypes</color>"
        }

    /**
     * Updates the given block by setting the occupied slots of the ChiseledBookshelf block data.
     *
     * @param block The block to update.
     */
    fun updateBlock(block: Block) {
        val chiseledBookshelf = block.blockData as ChiseledBookshelf
        val drives = disks.size
        for (i in 0 until 6) {
            chiseledBookshelf.setSlotOccupied(i, drives > i)
        }
        block.blockData = chiseledBookshelf
        block.state.update(true)
    }
}
