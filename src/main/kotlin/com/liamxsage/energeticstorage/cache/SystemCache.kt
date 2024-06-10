package com.liamxsage.energeticstorage.cache

import com.liamxsage.energeticstorage.model.ESSystem
import java.util.UUID
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object SystemCache {

    private val cacheLock: ReadWriteLock = ReentrantReadWriteLock()
    private var cache = mapOf<UUID, ESSystem>()

    fun getSystem(systemId: UUID): ESSystem? {
        cacheLock.readLock().lock()
        val system = cache[systemId]
        cacheLock.readLock().unlock()
        return system
    }

    fun addSystem(system: ESSystem) {
        cacheLock.writeLock().lock()
        cache = cache + (system.uuid to system)
        cacheLock.writeLock().unlock()
    }

    fun removeSystem(systemId: UUID) {
        cacheLock.writeLock().lock()
        cache = cache - systemId
        cacheLock.writeLock().unlock()
    }

    fun getSystems(): List<ESSystem> {
        cacheLock.readLock().lock()
        val systems = cache.values.toList()
        cacheLock.readLock().unlock()
        return systems
    }
}