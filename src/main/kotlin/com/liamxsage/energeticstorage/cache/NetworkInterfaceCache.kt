package com.liamxsage.energeticstorage.cache

import com.liamxsage.energeticstorage.network.NetworkInterface
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object NetworkInterfaceCache {

    private val cacheLock: ReadWriteLock = ReentrantReadWriteLock()
    private var cache = mapOf<UUID, NetworkInterface>()

    fun getNetworkInterfaceByUUID(interfaceUUID: UUID): NetworkInterface? {
        cacheLock.readLock().lock()
        val networkInterface = cache[interfaceUUID]
        cacheLock.readLock().unlock()
        return networkInterface
    }

    fun addNetworkInterface(networkInterface: NetworkInterface) {
        cacheLock.writeLock().lock()
        cache = cache + (networkInterface.uuid to networkInterface)
        cacheLock.writeLock().unlock()
    }

    fun removeNetworkInterfaceByUUID(interfaceUUID: UUID) {
        cacheLock.writeLock().lock()
        cache = cache - interfaceUUID
        cacheLock.writeLock().unlock()
    }

    fun getCachedNetworkInterfaces(): List<NetworkInterface> {
        cacheLock.readLock().lock()
        val networkInterfaces = cache.values.toList()
        cacheLock.readLock().unlock()
        return networkInterfaces
    }

}