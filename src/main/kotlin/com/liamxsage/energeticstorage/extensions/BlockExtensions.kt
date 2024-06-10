package com.liamxsage.energeticstorage.extensions

import com.liamxsage.energeticstorage.EnergeticStorage
import com.liamxsage.energeticstorage.customblockdata.CustomBlockData
import org.bukkit.block.Block

val Block.persistentDataContainer
    get() = CustomBlockData(this, EnergeticStorage.instance)