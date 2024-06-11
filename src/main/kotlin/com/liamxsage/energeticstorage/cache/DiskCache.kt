package com.liamxsage.energeticstorage.cache

import com.liamxsage.energeticstorage.model.Disk
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object DiskCache {

    private val cacheLock: ReadWriteLock = ReentrantReadWriteLock()
    private var cache = mapOf<UUID, Disk>()

    fun getDiskByUUID(diskUUID: UUID): Disk? {
        cacheLock.readLock().lock()
        val system = cache[diskUUID]
        cacheLock.readLock().unlock()
        return system
    }

    fun addDisk(disk: Disk) {
        cacheLock.writeLock().lock()
        cache = cache + (disk.uuid to disk)
        cacheLock.writeLock().unlock()
    }

    fun removeDiskByUUID(diskUUID: UUID) {
        cacheLock.writeLock().lock()
        cache = cache - diskUUID
        cacheLock.writeLock().unlock()
    }

    fun getCachedDisks(): List<Disk> {
        cacheLock.readLock().lock()
        val disks = cache.values.toList()
        cacheLock.readLock().unlock()
        return disks
    }
}