package com.liamxsage.energeticstorage.network

import com.liamxsage.energeticstorage.DISK_DRIVE_ID_NAMESPACE
import com.liamxsage.energeticstorage.MAX_NETWORK_LENGTH
import com.liamxsage.energeticstorage.cache.DiskDriveCache
import com.liamxsage.energeticstorage.extensions.*
import com.liamxsage.energeticstorage.model.Core
import com.liamxsage.energeticstorage.model.DiskDrive
import com.liamxsage.energeticstorage.model.Cable
import com.liamxsage.energeticstorage.model.Terminal
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

interface NetworkInterface

/**
 * Returns a map of connected network interfaces to the given block.
 *
 * @param block The block to check for connected network interfaces.
 * @param sourceFace The face to exclude when checking for connected network interfaces. Defaults to null.
 * @param iteration The current iteration count. Defaults to 0.
 * @return A map of connected network interfaces to the given block.
 */
fun getConnectedNetworkInterfaces(
    block: Block,
    sourceFace: BlockFace? = null,
    iteration: Int = 0,
    visitedBlocks: MutableSet<Block> = mutableSetOf()
): Map<Block, NetworkInterface> {
    if (iteration >= MAX_NETWORK_LENGTH || !visitedBlocks.add(block)) return emptyMap()

    val faces = sourceFace?.let { getFacesExcluding(it) } ?: BlockFace.entries
    val connectedInterfaces = mutableMapOf<Block, NetworkInterface>()

    for (face in faces) {
        val relativeBlock = block.getRelative(face)
        if (!relativeBlock.isNetworkInterface) continue

        val networkInterface = getNetworkInterface(relativeBlock) ?: continue
        connectedInterfaces[relativeBlock] = networkInterface

        val iteratedInterfaces = getConnectedNetworkInterfaces(relativeBlock, face.oppositeFace, iteration + 1, visitedBlocks)

        // Merge iteratedInterfaces into connectedInterfaces
        iteratedInterfaces.forEach { (key, value) ->
            connectedInterfaces.putIfAbsent(key, value)
        }
    }

    return connectedInterfaces
}

/**
 * Updates the network core with the connected network interfaces.
 *
 * @param block The block to check for connected network interfaces.
 */
fun updateNetworkCoreWithConnectedInterfaces(block: Block, player: Player) {
    val connectedInterfaces = getConnectedNetworkInterfaces(block)

    if (connectedInterfaces.isEmpty()) return
    val cores = connectedInterfaces.filter { it.value is Core }
    if (cores.isEmpty()) return

    assert(cores.size == 1) { "Multiple cores found in network." }

    val core = cores.entries.first().value as Core
    core.connectedDiskDrives.clear()
    core.connectedTerminals.clear()

    connectedInterfaces.forEach { (_, networkInterface) ->
        when (networkInterface) {
            is DiskDrive -> {
                core.connectedDiskDrives.add(networkInterface)
                player.sendMessagePrefixed("Connected disk drive to core.")
                player.sendInfoSound()
            }
            is Terminal -> {
                core.connectedTerminals.add(networkInterface)
                networkInterface.connectedCore = core
                player.sendMessagePrefixed("Connected terminal to core.")
                player.sendInfoSound()
            }
            else -> { /* Do nothing */ }
        }
    }
}


/**
 * Retrieves a list of BlockFaces excluding the given face.
 *
 * @param face The BlockFace to exclude.
 * @return A List of BlockFaces excluding the given face.
 */
private fun getFacesExcluding(face: BlockFace): List<BlockFace> {
    return BlockFace.entries.filter { it != face }
}


/**
 * Retrieves the NetworkInterfaceType of a given block.
 *
 * @param block the block to retrieve the network interface type of.
 * @return the NetworkInterfaceType of the block.
 * @throws IllegalArgumentException if the block is not a network interface.
 */
fun getNetworkInterfaceType(block: Block): NetworkInterfaceType? {
    return NetworkInterfaceType.entries.find { it.material == block.type }
}

/**
 * Retrieves the type of network interface associated with the given ItemStack.
 *
 * @param itemStack The ItemStack to get the network interface type from.
 * @return The NetworkInterfaceType associated with the ItemStack, or null if none is found.
 */
fun getNetworkInterfaceType(itemStack: ItemStack): NetworkInterfaceType? {
    return NetworkInterfaceType.entries.find { it.material == itemStack.type }
}

/**
 * Retrieves the network interface associated with the given block.
 *
 * @param block The block to retrieve the network interface from.
 * @return The network interface associated with the block, or null if none is found.
 */
fun getNetworkInterface(block: Block): NetworkInterface? {
    return when (getNetworkInterfaceType(block)) {
        NetworkInterfaceType.DISK_DRIVE -> {
            if (!block.persistentDataContainer.has(DISK_DRIVE_ID_NAMESPACE)) return null
            val diskDriveUUID = block.persistentDataContainer[DISK_DRIVE_ID_NAMESPACE, PersistentDataType.STRING] ?: return null
            return DiskDriveCache.getDiskDriveByUUID(UUID.fromString(diskDriveUUID)) ?: DiskDrive(UUID.fromString(diskDriveUUID))
        }
        NetworkInterfaceType.TERMINAL -> Terminal()
        NetworkInterfaceType.CORE -> Core()
        NetworkInterfaceType.CABLE -> Cable()
        else -> null
    }
}

/**
 * Retrieves the NetworkInterface associated with the given ItemStack.
 *
 * @param itemStack The ItemStack to get the NetworkInterface from.
 * @return The NetworkInterface associated with the ItemStack, or null if none is found.
 */
fun getNetworkInterface(itemStack: ItemStack): NetworkInterface? {
    return when (getNetworkInterfaceType(itemStack)) {
        NetworkInterfaceType.DISK_DRIVE -> {
            if (!itemStack.hasKey(DISK_DRIVE_ID_NAMESPACE)) return null
            val diskDriveUUID = itemStack.getKey(DISK_DRIVE_ID_NAMESPACE) ?: return null
            return DiskDriveCache.getDiskDriveByUUID(UUID.fromString(diskDriveUUID)) ?: DiskDrive(UUID.fromString(diskDriveUUID))
        }
        NetworkInterfaceType.TERMINAL -> Terminal()
        NetworkInterfaceType.CORE -> Core()
        NetworkInterfaceType.CABLE -> Cable()
        else -> null
    }
}
