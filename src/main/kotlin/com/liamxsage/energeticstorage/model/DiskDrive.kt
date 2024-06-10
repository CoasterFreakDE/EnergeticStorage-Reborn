package com.liamxsage.energeticstorage.model

import com.liamxsage.energeticstorage.DISK_DRIVE_ID_NAMESPACE
import com.liamxsage.energeticstorage.TEXT_GRAY
import com.liamxsage.energeticstorage.cache.DiskDriveCache
import com.liamxsage.energeticstorage.database.loadDisks
import com.liamxsage.energeticstorage.extensions.toItemBuilder
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.UUID

@Serializable
data class DiskDrive(
    @Contextual val uuid: UUID = UUID.randomUUID(),
    val disks: MutableList<Disk> = mutableListOf()
) {

    init {
        loadDisks()

        DiskDriveCache.addDiskDrive(this)
    }

    /**
     * Creates an ItemStack representing the system item for the ESSystem.
     *
     * @return The created system item.
     */
    fun createSystemItem(): ItemStack = Material.CHISELED_BOOKSHELF.toItemBuilder {
        display("${TEXT_GRAY}Energetic Storage System")
        lore(
            "${TEXT_GRAY}Drives inserted: <color:#b8e994>${totalDrives}</color>/<color:#26de81>6</color>",
            "${TEXT_GRAY}Filled Items: ${colorItems}/<color:#26de81>${totalSize}</color>",
            "${TEXT_GRAY}Filled Types: ${colorTypes}/<color:#26de81>${totalTypesSize}</color>",
        )
        setGlinting(true)
        customModelData(1)
        addPersistentData(DISK_DRIVE_ID_NAMESPACE, uuid.toString())
        flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS)
    }.build()

    val canFitDrive: Boolean
        get() = disks.size < 6

    val totalItems: Long
        get() = disks.sumOf { it.totalItems }

    val totalTypes: Int
        get() = disks.sumOf { it.totalTypes }

    private val totalDrives: Int
        get() = disks.size

    val totalSize: Long
        get() = disks.sumOf { it.size.size }

    val totalTypesSize: Int
        get() = disks.sumOf { it.size.types }

    private val colorItems: String
        get() = when {
            totalItems > ((totalSize / 10) * 8) -> "<color:#fc5c65>$totalItems</color>"
            totalItems > (totalSize / 2) -> "<color:#fed330>$totalItems</color>"
            else -> "<color:#b8e994>$totalItems</color>"
        }

    private val colorTypes: String
        get() = when {
            totalTypes > ((totalTypesSize / 10) * 8) -> "<color:#fc5c65>$totalTypes</color>"
            totalTypes > (totalTypesSize / 2) -> "<color:#fed330>$totalTypes</color>"
            else -> "<color:#b8e994>$totalTypes</color>"
        }
}
