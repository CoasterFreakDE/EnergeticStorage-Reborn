package com.liamxsage.energeticstorage.customblockdata.events

import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin

/**
 * Called when a block with CustomBlockData is moved by a piston to a new location.
 * <p>
 * Blocks with protected CustomBlockData (see {@link CustomBlockData#isProtected()} will not trigger this event, however
 * it is possible that unprotected CustomBlockData will be moved to a destination block with protected CustomBlockData. You have
 * to cancel this event yourself to prevent this.
 */
class CustomBlockDataMoveEvent(
    plugin: Plugin,
    blockFrom: Block,
    private val blockTo: Block,
    bukkitEvent: Event
) : CustomBlockDataEvent(plugin, blockFrom, bukkitEvent) {

    fun getBlockTo(): Block {
        return blockTo
    }

}