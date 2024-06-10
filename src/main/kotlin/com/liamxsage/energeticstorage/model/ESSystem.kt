package com.liamxsage.energeticstorage.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.Location
import java.util.UUID

@Serializable
data class ESSystem(
    @Contextual val uuid: UUID,
    @Contextual val location: Location,
    val drives: MutableList<ESDrive> = mutableListOf()
)
