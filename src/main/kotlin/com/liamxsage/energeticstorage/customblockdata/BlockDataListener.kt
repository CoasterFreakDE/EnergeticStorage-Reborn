package com.liamxsage.energeticstorage.customblockdata

import com.liamxsage.energeticstorage.EnergeticStorage
import com.liamxsage.energeticstorage.customblockdata.events.CustomBlockDataMoveEvent
import com.liamxsage.energeticstorage.customblockdata.events.CustomBlockDataRemoveEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.StructureGrowEvent
import java.util.*
import java.util.function.Predicate

internal class BlockDataListener : Listener {
    private val customDataPredicate: Predicate<Block> =
        Predicate<Block> { block -> CustomBlockData.hasCustomBlockData(block, EnergeticStorage.instance) }

    private fun getCbd(event: BlockEvent): CustomBlockData {
        return getCbd(event.block)
    }

    private fun getCbd(block: Block): CustomBlockData {
        return CustomBlockData(block, EnergeticStorage.instance)
    }

    private fun callAndRemove(blockEvent: BlockEvent) {
        if (callEvent(blockEvent)) {
            getCbd(blockEvent).clear()
        }
    }

    private fun callEvent(blockEvent: BlockEvent): Boolean {
        return callEvent(blockEvent.block, blockEvent)
    }

    private fun callEvent(block: Block, bukkitEvent: Event): Boolean {
        if (!CustomBlockData.hasCustomBlockData(block, EnergeticStorage.instance)) {
            return false
        }

        val cbdEvent = CustomBlockDataRemoveEvent(EnergeticStorage.instance, block, bukkitEvent)
        Bukkit.getPluginManager().callEvent(cbdEvent)

        return !cbdEvent.isCancelled()
    }

    private fun callAndRemoveBlockStateList(blockStates: List<BlockState>, bukkitEvent: Event) {
        blockStates.stream()
            .map { blockState -> blockState.block }
            .filter(customDataPredicate)
            .forEach { block -> callAndRemove(block, bukkitEvent) }
    }

    private fun callAndRemoveBlockList(blocks: List<Block>, bukkitEvent: Event) {
        blocks.stream()
            .filter(customDataPredicate)
            .forEach { block -> callAndRemove(block, bukkitEvent) }
    }

    private fun callAndRemove(block: Block, bukkitEvent: Event) {
        if (callEvent(block, bukkitEvent)) {
            getCbd(block).clear()
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) {
        callAndRemove(event)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) {
        if (!CustomBlockData.isDirty(event.block)) {
            callAndRemove(event)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntity(event: EntityChangeBlockEvent) {
        if (event.to != event.block.type) {
            callAndRemove(event.block, event)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onExplode(event: BlockExplodeEvent) {
        callAndRemoveBlockList(event.blockList(), event)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onExplode(event: EntityExplodeEvent) {
        callAndRemoveBlockList(event.blockList(), event)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBurn(event: BlockBurnEvent) {
        callAndRemove(event)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPiston(event: BlockPistonExtendEvent) {
        onPiston(event.blocks, event)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPiston(event: BlockPistonRetractEvent) {
        onPiston(event.blocks, event)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onFade(event: BlockFadeEvent) {
        if (event.block.type == Material.FIRE) return
        if (event.newState.type != event.block.type) {
            callAndRemove(event)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onStructure(event: StructureGrowEvent) {
        callAndRemoveBlockStateList(event.blocks, event)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onFertilize(event: BlockFertilizeEvent) {
        callAndRemoveBlockStateList(event.blocks, event)
    }

    private fun onPiston(blocks: List<Block>, bukkitEvent: BlockPistonEvent) {
        val map = LinkedHashMap<Block, CustomBlockData>()
        val direction = bukkitEvent.direction
        blocks.stream().filter(customDataPredicate).forEach { block ->
            val cbd = CustomBlockData(block, EnergeticStorage.instance)
            if (cbd.isEmpty) return@forEach
            val destinationBlock = block.getRelative(direction)
            val moveEvent = CustomBlockDataMoveEvent(EnergeticStorage.instance, block, destinationBlock, bukkitEvent)
            Bukkit.getPluginManager().callEvent(moveEvent)
            if (moveEvent.isCancelled) return@forEach
            map[destinationBlock] = cbd
        }
        Utils.reverse(map).forEach { (block, cbd) ->
            cbd.copyTo(block, EnergeticStorage.instance)
            cbd.clear()
        }
    }

    private object Utils {
        fun <K, V> reverse(map: Map<K, V>): Map<K, V> {
            val reversed = LinkedHashMap<K, V>()
            val keys = ArrayList(map.keys)
            Collections.reverse(keys)
            keys.forEach { key -> reversed[key] = map[key]!! }
            return reversed
        }
    }
}