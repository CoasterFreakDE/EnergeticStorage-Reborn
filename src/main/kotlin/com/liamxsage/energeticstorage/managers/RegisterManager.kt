package com.liamxsage.energeticstorage.managers

import com.google.common.reflect.ClassPath
import com.liamxsage.energeticstorage.PACKAGE_NAME
import com.liamxsage.energeticstorage.EnergeticStorage
import com.liamxsage.energeticstorage.annotations.RegisterCommand
import com.liamxsage.energeticstorage.extensions.getLogger
import com.liamxsage.energeticstorage.extensions.sendMessagePrefixed
import com.liamxsage.energeticstorage.listeners.ItemClickListener
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.PluginCommand
import org.bukkit.permissions.Permission
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object RegisterManager {

    private val logger = getLogger()

    private fun <T : Any> loadClassesInPackage(packageName: String, clazzType: KClass<T>): List<KClass<out T>> {
        try {
            val classLoader = EnergeticStorage.instance.javaClass.classLoader
            val allClasses = ClassPath.from(classLoader).allClasses
            val classes = mutableListOf<KClass<out T>>()
            for (classInfo in allClasses) {
                if (!classInfo.name.startsWith(PACKAGE_NAME)) continue
                if (classInfo.packageName.startsWith(packageName) && !classInfo.name.contains('$')) {
                    try {
                        val loadedClass = classInfo.load().kotlin
                        if (clazzType.isInstance(loadedClass.javaObjectType.getDeclaredConstructor().newInstance())) {
                            classes.add(loadedClass as KClass<out T>)
                        }
                    } catch (_: Exception) {
                        // Ignore
                    }
                }
            }
            return classes
        } catch (exception: Exception) {
            logger.error("Failed to load classes", exception)
            return emptyList()
        }
    }

    /**
     * Loads all classes in a given package that are annotated with the specified annotation.
     *
     * @param packageName The name of the package.
     * @param annotation The annotation class.
     * @return A list of loaded classes annotated with the specified annotation.
     */
    private fun loadClassesInPackageWithAnnotation(
        packageName: String,
        annotation: KClass<out Annotation>
    ): List<KClass<out Any>> {
        try {
            val classLoader = EnergeticStorage.instance.javaClass.classLoader
            val allClasses = ClassPath.from(classLoader).allClasses
            val classes = mutableListOf<KClass<out Any>>()
            for (classInfo in allClasses) {
                if (!classInfo.name.startsWith(PACKAGE_NAME)) continue
                if (classInfo.packageName.startsWith(packageName) && !classInfo.name.contains('$')) {
                    try {
                        val loadedClass = classInfo.load().kotlin
                        if (loadedClass.annotations.any { it.annotationClass == annotation }) {
                            classes.add(loadedClass)
                        }
                    } catch (_: Exception) {
                        // Ignore
                    }
                }
            }
            return classes
        } catch (exception: Exception) {
            logger.error("Failed to load classes", exception)
            return emptyList()
        }
    }

    fun registerCommands() {
        val commandClasses = loadClassesInPackageWithAnnotation(PACKAGE_NAME, RegisterCommand::class)

        commandClasses.forEach {
            val annotation: RegisterCommand = it.annotations.first { it is RegisterCommand } as RegisterCommand

            val pluginClass: Class<PluginCommand> = PluginCommand::class.java

            val constructor = pluginClass.getDeclaredConstructor(String::class.java, Plugin::class.java)

            constructor.isAccessible = true

            val command: PluginCommand = constructor.newInstance(annotation.name, EnergeticStorage.instance)


            command.aliases = annotation.aliases.toList()
            command.description = annotation.description
            if(annotation.permission.isEmpty()) {
                command.permission = null
            } else {
                command.permission = Permission(annotation.permission, annotation.permissionDefault).name
            }
            command.usage = annotation.usage
            val commandInstance = it.primaryConstructor?.call() as CommandExecutor
            command.setExecutor { sender, command, label, args ->
                try {
                    commandInstance.onCommand(sender, command, label, args)
                } catch (e: Exception) {
                    sender.sendMessagePrefixed("An error occurred while executing the command.")
                    throw e
                }
            }
            command.tabCompleter = commandInstance as? org.bukkit.command.TabCompleter


            Bukkit.getCommandMap().register(EnergeticStorage.instance.name.lowercase(), command)
            Bukkit.getConsoleSender().sendMessage("Command ${command.name} registered")
        }

        logger.info("Registered ${commandClasses.size} minecraft commands")
    }

    /**
     * Registers listeners by iterating through a list of listener classes and registering them
     * with the Bukkit plugin manager.
     */
    fun registerListeners() {
        val listenerClasses = listOf(
            ItemClickListener(),
            // Add more listeners here
        )
        var amountListeners = 0
        for (listener in listenerClasses) {
            try {
                Bukkit.getPluginManager().registerEvents(listener, EnergeticStorage.instance)
                amountListeners++
            } catch (e: Exception) {
                logger.error("Exception while registering listener: ${listener.javaClass.simpleName}")
                e.printStackTrace()
            }
        }
        logger.info("Registered $amountListeners listeners")
    }
}