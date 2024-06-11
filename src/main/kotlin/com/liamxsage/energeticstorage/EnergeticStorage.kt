package com.liamxsage.energeticstorage

import com.google.gson.GsonBuilder
import com.liamxsage.energeticstorage.database.DatabaseConnection
import com.liamxsage.energeticstorage.managers.RegisterManager
import com.liamxsage.energeticstorage.model.ESItem
import com.liamxsage.energeticstorage.serialization.*
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.system.measureTimeMillis

class EnergeticStorage : JavaPlugin() {

    companion object {
        lateinit var instance: EnergeticStorage
            private set
    }

    /**
     * Gson instance for serializing and deserializing objects.
     *
     * This instance is created
     * using GsonBuilder with a custom TypeAdapter for serializing and deserializing ItemStack objects.
     * The registered TypeAdapter is the `ItemStackAdapter` class,
     * which handles the serialization and deserialization of ItemStack objects.
     *
     * @see ItemStackAdapter
     */
    val gson = GsonBuilder()
        .registerTypeAdapter(ESItem::class.java, ESItemAdapter())
        .registerTypeAdapter(ItemStack::class.java, ItemStackAdapter())
        .registerTypeAdapter(UUID::class.java, UUIDAdapter())
        .registerTypeAdapter(Location::class.java, LocationAdapter())
        .registerTypeAdapter(Optional::class.java, OptionalTypeAdapter())
        .create()

    init {
        instance = this
    }

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()

        MAX_NETWORK_LENGTH = config.getInt("networks.maxLength", 128)
        logger.info("Max network length set to $MAX_NETWORK_LENGTH")

        DatabaseConnection.connect()

        val time = measureTimeMillis {
            RegisterManager.registerCommands()
            RegisterManager.registerListeners()
        }
        logger.info("Plugin enabled in $time ms")
        logger.info("EnergeticStorage is now tweaking your item storage behavior!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        DatabaseConnection.disconnect()

        logger.info("EnergeticStorage is now shutting down.")
    }
}