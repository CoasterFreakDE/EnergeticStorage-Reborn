package com.liamxsage.energeticstorage.model

import com.liamxsage.energeticstorage.ITEM_AMOUNT_NAMESPACE
import com.liamxsage.energeticstorage.NETWORK_INTERFACE_ID_NAMESPACE
import com.liamxsage.energeticstorage.NETWORK_INTERFACE_NAMESPACE
import com.liamxsage.energeticstorage.TEXT_GRAY
import com.liamxsage.energeticstorage.cache.NetworkInterfaceCache
import com.liamxsage.energeticstorage.extensions.persistentDataContainer
import com.liamxsage.energeticstorage.extensions.toItemBuilder
import com.liamxsage.energeticstorage.network.NetworkInterface
import com.liamxsage.energeticstorage.network.NetworkInterfaceType
import dev.fruxz.stacked.extension.asPlainString
import org.bukkit.block.Block
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*


data class Core(
    override val uuid: UUID = UUID.randomUUID()
) : NetworkInterface {

    val connectedDiskDrives = mutableListOf<DiskDrive>()
    val connectedTerminals = mutableListOf<Terminal>()

    init {
        NetworkInterfaceCache.addNetworkInterface(this)
    }

    override fun setBlockUUID(block: Block): Core {
        block.persistentDataContainer[NETWORK_INTERFACE_ID_NAMESPACE, PersistentDataType.STRING] = uuid.toString()
        return this
    }

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
     * Returns a list of all disks in the system.
     *
     * @return A list of [Disk] objects representing the disks in the system.
     */
    fun getAllDisksInSystem(): List<Disk> {
        return connectedDiskDrives.map { it.disks }.flatten()
    }

    /**
     * Retrieves a list of items from the connected disk drives, sorted according to the given sort order, and returns a specific page of the results.
     *
     * @param sortOrder The sort order to apply to the items.
     * @param pageIndex The index of the page to retrieve.
     * @return A list of [ESItem] objects representing the items on the requested page.
     */
    fun getItems(sortOrder: SortOrder, pageIndex: Int): List<ESItem> {
        return connectedDiskDrives.flatMap { diskDrive -> diskDrive.disks.flatMap { it.items } }
            .sortedWith(sortOrder.comparator).drop(pageIndex * 28).take(28)
    }


    /**
     * Adds an item to the system.
     *
     * @param item The item to be added to the system. It must be an instance of ItemStack.
     * @return true if the item was successfully added to the system, false otherwise.
     */
    fun addItemToSystem(item: ItemStack): Boolean {
        val allDisks = getAllDisksInSystem()
        if (addExistingItemToSystem(allDisks, item)) return true
        if (createNewItemInSystem(allDisks, item)) return true
        return false
    }

    /**
     * Adds an existing item to the system.
     *
     * @param allDisks The list of all disks in the system.
     * @param item The item to be added to the system.
     * @return true if the item was successfully added to the system, false otherwise.
     */
    private fun addExistingItemToSystem(allDisks: List<Disk>, item: ItemStack): Boolean {
        for (disk in allDisks) {
            val existingItem = disk.items.find { it.itemStackAsSingle.isSimilar(item) } ?: continue
            val remainingCapacity = checkRemainingCapacity(disk, item)
            if (remainingCapacity <= 0) continue

            accumulateItem(existingItem, item, remainingCapacity)
            if (item.amount <= 0) return true
        }
        return false
    }

    /**
     * Returns the first matching item from the system that is similar to the provided item.
     *
     * @param item The item to search for. It must be an instance of ItemStack.
     * @return The first matching [ESItem] object representing the item, or null if no matching item is found.
     */
    fun getFirstMatchingItem(item: ItemStack): ESItem? {
        return getAllDisksInSystem().flatMap { it.items }.find { it.itemStackAsSingle.isSimilar(getItemStackWithOutGuiModifications(item)) }
    }

    /**
     * Returns an ItemStack without the amount identifier.
     *
     * @param item The input ItemStack.
     * @return The modified ItemStack without the amount identifier.
     */
    private fun getItemStackWithOutGuiModifications(item: ItemStack): ItemStack {
        return item.clone().toItemBuilder {
            removePersistentData(ITEM_AMOUNT_NAMESPACE)
            lore(
                *item.lore()?.dropLast(1)?.map { it.asPlainString }?.toTypedArray() ?: emptyArray()
            )
        }.build()
    }

    /**
     * Creates a new item in the system by adding it to a disk.
     *
     * @param allDisks The list of all disks in the system.
     * @param item The item to be added to the system. It must be an instance of ItemStack.
     * @return true if the item was successfully added to the system, false otherwise.
     */
    private fun createNewItemInSystem(allDisks: List<Disk>, item: ItemStack): Boolean {
        for (disk in allDisks) {
            if (disk.isFull || disk.isTypeFull) continue
            val remainingCapacity = checkRemainingCapacity(disk, item)
            if (remainingCapacity <= 0) continue

            val newItemStack = createNewItemStack(item)
            disk.items.add(ESItem(newItemStack, item.amount.toLong()))
            subtractItemAmount(item, item.amount)
            if (item.amount <= 0) return true
        }
        return false
    }

    /**
     * Calculates the remaining capacity on a disk for a given item.
     *
     * @param disk The disk on which to check the remaining capacity.
     * @param item The item for which to calculate the remaining capacity.
     * @return The remaining capacity on the disk for the given item.
     */
    private fun checkRemainingCapacity(disk: Disk, item: ItemStack): Long {
        return (disk.size.size - disk.totalItems).coerceAtLeast(item.amount.toLong())
    }

    /**
     * Subtracts the specified amount from the given item stack.
     *
     * @param item The item stack from which to subtract the amount.
     * @param amount The amount to subtract.
     */
    private fun subtractItemAmount(item: ItemStack, amount: Int) {
        item.amount -= amount
    }

    /**
     * Accumulates the amount of an item in an existing ESItem object and subtracts the specified amount from the given item.
     *
     * @param existingItem The existing ESItem object to accumulate the amount to.
     * @param item The item to subtract the amount from.
     * @param amount The amount to accumulate.
     */
    private fun accumulateItem(existingItem: ESItem, item: ItemStack, amount: Long) {
        existingItem.amount += amount
        subtractItemAmount(item, amount.toInt())
    }

    /**
     * Creates a new ItemStack with an amount of 1 based on the provided item.
     *
     * @param item The base ItemStack.
     * @return A new ItemStack with an amount of 1.
     */
    private fun createNewItemStack(item: ItemStack): ItemStack {
        return item.clone().apply { amount = 1 }
    }

    /**
     * Removes an item from the system.
     *
     * @param item The item to be removed from the system. It must be an instance of ItemStack.
     * @return true if the item was successfully removed from the system, false otherwise.
     */
    fun removeItemFromSystem(item: ItemStack): Boolean {
        val allDisks = getAllDisksInSystem()
        return removeExistingItemFromSystem(allDisks, item)
    }

    /**
     * Removes an existing item from the system. This method iterates over the list of all disks in the system,
     * and for each disk, it searches for the item to remove. If the item is found, it adjusts the amount or removes
     * the item altogether from the disk. If the removal is successful, it returns true. Otherwise, it returns false.
     *
     * @param allDisks The list of all disks in the system.
     * @param item The item to be removed from the system.
     * @return true if the item was successfully removed from the system, false otherwise.
     */
    private fun removeExistingItemFromSystem(allDisks: List<Disk>, item: ItemStack): Boolean {
        for (disk in allDisks) {
            val existingItem = disk.items.find { it.itemStackAsSingle.isSimilar(getItemStackWithOutGuiModifications(item)) } ?: continue
            if (existingItem.amount >= item.amount) {
                existingItem.amount -= item.amount
                if (existingItem.amount == 0L) {
                    disk.items.remove(existingItem)
                }
                return true
            } else {
                item.amount -= existingItem.amount.toInt()
                disk.items.remove(existingItem)
            }
        }
        return false
    }

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