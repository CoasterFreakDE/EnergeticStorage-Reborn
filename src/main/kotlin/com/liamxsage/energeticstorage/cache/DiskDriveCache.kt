package com.liamxsage.energeticstorage.cache

import com.liamxsage.energeticstorage.model.DiskDrive
import java.util.UUID
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object DiskDriveCache {

    private val cacheLock: ReadWriteLock = ReentrantReadWriteLock()
    private var cache = mapOf<UUID, DiskDrive>()

    fun getDiskDriveByUUID(diskDriveUUID: UUID): DiskDrive? {
        cacheLock.readLock().lock()
        val system = cache[diskDriveUUID]
        cacheLock.readLock().unlock()
        return system
    }

    fun addDiskDrive(diskDrive: DiskDrive) {
        cacheLock.writeLock().lock()
        cache = cache + (diskDrive.uuid to diskDrive)
        cacheLock.writeLock().unlock()
    }

    fun removeDiskDrive(diskDriveUUID: UUID) {
        cacheLock.writeLock().lock()
        cache = cache - diskDriveUUID
        cacheLock.writeLock().unlock()
    }

    fun getDiskDrives(): List<DiskDrive> {
        cacheLock.readLock().lock()
        val diskDrives = cache.values.toList()
        cacheLock.readLock().unlock()
        return diskDrives
    }
}