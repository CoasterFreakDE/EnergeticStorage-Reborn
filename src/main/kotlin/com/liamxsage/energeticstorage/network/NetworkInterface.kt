package com.liamxsage.energeticstorage.network

import org.bukkit.block.Block
import java.util.*

interface NetworkInterface {
    val uuid: UUID

    fun setBlockUUID(block: Block): NetworkInterface
}