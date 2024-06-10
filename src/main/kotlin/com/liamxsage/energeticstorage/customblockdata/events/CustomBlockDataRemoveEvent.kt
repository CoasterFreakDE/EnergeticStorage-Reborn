package com.liamxsage.energeticstorage.customblockdata.events

import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin

/**
 * Called when a block's CustomBlockData is about to be removed because the block was broken, replaced, or has changed in other ways.
 */
class CustomBlockDataRemoveEvent(
    plugin: Plugin,
    block: Block,
    bukkitEvent: Event
) : CustomBlockDataEvent(plugin, block, bukkitEvent)