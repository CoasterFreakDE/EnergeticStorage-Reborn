package com.liamxsage.energeticstorage

import org.bukkit.NamespacedKey

const val PREFIX = "<gradient:#0984e3:#74b9ff>Energetic Storage</gradient> <color:#4a628f>>></color> <color:#b2c2d4>"
const val BLOCK_PREFIX =
    "               <color:#4a628f>◆</color> <gradient:#0984e3:#74b9ff>Energetic Storage</gradient> <color:#4a628f>◆</color>"

const val TEXT_GRAY = "<color:#b2c2d4>"
const val TEXT_GRADIENT_DEFAULT = "<gradient:#f6e58d:#ffbe76>"

const val PACKAGE_NAME = "com.liamxsage.energeticstorage"

val DISK_ID_NAMESPACE = NamespacedKey(EnergeticStorage.instance, "disk_id")
val NETWORK_INTERFACE_NAMESPACE = NamespacedKey(EnergeticStorage.instance, "network_interface")
val NETWORK_INTERFACE_ID_NAMESPACE = NamespacedKey(EnergeticStorage.instance, "network_interface_id")

val PLAYER_DEBUG_MODE_NAMESPACE = NamespacedKey(EnergeticStorage.instance, "debug_mode")

var MAX_NETWORK_LENGTH = 512