package com.liamxsage.energeticstorage.network

import com.liamxsage.energeticstorage.MAX_NETWORK_LENGTH
import com.liamxsage.energeticstorage.NETWORK_INTERFACE_ID_NAMESPACE
import com.liamxsage.energeticstorage.cache.NetworkInterfaceCache
import com.liamxsage.energeticstorage.extensions.getKey
import com.liamxsage.energeticstorage.extensions.isNetworkInterface
import com.liamxsage.energeticstorage.extensions.persistentDataContainer
import com.liamxsage.energeticstorage.model.*
import dev.fruxz.ascend.extension.forceCastOrNull
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*


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

        val iteratedInterfaces =
            getConnectedNetworkInterfaces(relativeBlock, face.oppositeFace, iteration + 1, visitedBlocks)

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
fun updateNetworkCoreWithConnectedInterfaces(block: Block): Core? {
    val connectedInterfaces = getConnectedNetworkInterfaces(block)

    if (connectedInterfaces.isEmpty()) return null
    val cores = connectedInterfaces.filter { it.value is Core }
    if (cores.isEmpty()) return null

    assert(cores.size == 1) { "Multiple cores found in network." }

    val core = cores.entries.first().value as Core
    core.connectedDiskDrives.clear()
    core.connectedTerminals.clear()

    connectedInterfaces.forEach { (_, networkInterface) ->
        when (networkInterface) {
            is DiskDrive -> {
                core.connectedDiskDrives.add(networkInterface)
                networkInterface.connectedCoreUUID = core.uuid
            }

            is Terminal -> {
                core.connectedTerminals.add(networkInterface)
                networkInterface.connectedCoreUUID = core.uuid
            }

            else -> { /* Do nothing */
            }
        }
    }
    NetworkInterfaceCache.addNetworkInterface(core)
    return core
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
        NetworkInterfaceType.DISK_DRIVE -> getNetworkInterfaceFromBlock<DiskDrive>(block)
        NetworkInterfaceType.TERMINAL -> getNetworkInterfaceFromBlock<Terminal>(block)
        NetworkInterfaceType.CORE -> getNetworkInterfaceFromBlock<Core>(block)
        NetworkInterfaceType.CABLE -> getNetworkInterfaceFromBlock<Cable>(block)
        else -> null
    }
}

/**
 * Retrieves the network interface of the given block.
 *
 * @param block The block to retrieve the network interface from.
 * @return The network interface associated with the block.
 */
inline fun <reified T : NetworkInterface> getNetworkInterfaceFromBlock(block: Block): T {
    val networkInterfaceUUID =
        block.persistentDataContainer[NETWORK_INTERFACE_ID_NAMESPACE, PersistentDataType.STRING]?.let {
            UUID.fromString(it)
        } ?: UUID.randomUUID()
    return NetworkInterfaceCache.getNetworkInterfaceByUUID(networkInterfaceUUID)?.forceCastOrNull<T>()
        ?: T::class.java.getDeclaredConstructor(UUID::class.java).newInstance(networkInterfaceUUID)
            .setBlockUUID(block) as T
}

/**
 * Retrieves the NetworkInterface associated with the given ItemStack.
 *
 * @param itemStack The ItemStack to get the NetworkInterface from.
 * @return The NetworkInterface associated with the ItemStack, or null if none is found.
 */
fun getNetworkInterface(itemStack: ItemStack): NetworkInterface? {
    return when (getNetworkInterfaceType(itemStack)) {
        NetworkInterfaceType.DISK_DRIVE -> getNetworkInterfaceFromItemStack<DiskDrive>(itemStack)
        NetworkInterfaceType.TERMINAL -> getNetworkInterfaceFromItemStack<Terminal>(itemStack)
        NetworkInterfaceType.CORE -> getNetworkInterfaceFromItemStack<Core>(itemStack)
        NetworkInterfaceType.CABLE -> getNetworkInterfaceFromItemStack<Cable>(itemStack)
        else -> null
    }
}

/**
 * Retrieves the network interface of the given block.
 *
 * @param block The block to retrieve the network interface from.
 * @return The network interface associated with the block.
 */
inline fun <reified T : NetworkInterface> getNetworkInterfaceFromItemStack(itemStack: ItemStack): T {
    val networkInterfaceUUID =
        itemStack.getKey(NETWORK_INTERFACE_ID_NAMESPACE)?.let { UUID.fromString(it) } ?: UUID.randomUUID()
    return NetworkInterfaceCache.getNetworkInterfaceByUUID(networkInterfaceUUID)?.forceCastOrNull<T>()
        ?: T::class.java.getDeclaredConstructor(UUID::class.java).newInstance(networkInterfaceUUID) as T
}