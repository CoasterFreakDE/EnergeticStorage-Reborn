package com.liamxsage.energeticstorage.model

import com.liamxsage.energeticstorage.DISK_ID_NAMESPACE
import com.liamxsage.energeticstorage.TEXT_GRAY
import com.liamxsage.energeticstorage.extensions.toItemBuilder
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.UUID

@Serializable
data class ESDrive(
    @Contextual private val uuid: UUID = UUID.randomUUID(),
    private val size: DriveSize = DriveSize.SMALL,
    private val items: MutableList<ESItem> = mutableListOf()
) : Cloneable {

    /**
     * Creates an ItemStack representing the disk item for the ESDrive.
     *
     * @return The created disk item.
     */
    fun createDiskItem(): ItemStack = Material.MUSIC_DISC_5.toItemBuilder {
        display("$TEXT_GRAY${size.diskName}")
        lore(
            "${TEXT_GRAY}Filled Items: ${colorItems}/<color:#26de81>${size.size}</color>",
            "${TEXT_GRAY}Filled Types: ${colorTypes}/<color:#26de81>${size.types}</color>",
        )
        setGlinting(true)
        customModelData(size.ordinal + 1)
        addPersistentData(DISK_ID_NAMESPACE, uuid.toString())
    }.build()

    private val totalItems: Long
        get() = items.sumOf { it.amount }

    private val totalTypes: Int
        get() = items.size

    private val colorItems: String
        get() = when {
            totalItems > ((size.types / 10) * 8) -> "<color:#fc5c65>$totalItems</color>"
            totalItems > (size.size / 2) -> "<color:#fed330>$totalItems</color>"
            else -> "<color:#b8e994>$totalItems</color>"
        }

    private val colorTypes: String
        get() = when {
            totalTypes > ((size.types / 10) * 8) -> "<color:#fc5c65>$totalTypes</color>"
            totalTypes > (size.types / 2) -> "<color:#fed330>$totalTypes</color>"
            else -> "<color:#b8e994>$totalTypes</color>"
        }
}