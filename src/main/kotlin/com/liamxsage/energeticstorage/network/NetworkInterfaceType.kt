package com.liamxsage.energeticstorage.network

import org.bukkit.Material

enum class NetworkInterfaceType(val material: Material) {

    DISK_DRIVE(Material.CHISELED_BOOKSHELF),
    TERMINAL(Material.SMOKER),
    CORE(Material.LODESTONE),
    CABLE(Material.YELLOW_CONCRETE)

}