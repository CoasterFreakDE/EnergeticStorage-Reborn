package com.liamxsage.energeticstorage.model

import com.liamxsage.energeticstorage.EnergeticStorage

enum class DriveSize {
    SMALL,
    MEDIUM,
    LARGE,
    XLARGE;

    val diskName: String
        get() {
            return EnergeticStorage.instance.config.getString("drives.$name.diskName") ?: "$name Drive"
        }

    val size: Long
        get() {
            return EnergeticStorage.instance.config.getLong("drives.$name.items", 0)
        }

    val types: Int
        get() {
            return EnergeticStorage.instance.config.getInt("drives.$name.types", 0)
        }
}