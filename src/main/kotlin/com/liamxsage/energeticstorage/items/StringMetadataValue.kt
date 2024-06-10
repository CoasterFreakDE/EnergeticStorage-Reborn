package com.liamxsage.energeticstorage.items

import com.liamxsage.energeticstorage.EnergeticStorage
import org.bukkit.metadata.MetadataValueAdapter

class StringMetadataValue(private val value: String) : MetadataValueAdapter(EnergeticStorage.instance) {

    override fun value(): Any {
        return value
    }

    override fun invalidate() {
        // Do nothing
    }
}