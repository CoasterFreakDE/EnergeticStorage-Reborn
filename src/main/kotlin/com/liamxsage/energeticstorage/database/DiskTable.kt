package com.liamxsage.energeticstorage.database

import com.liamxsage.energeticstorage.EnergeticStorage
import com.liamxsage.energeticstorage.model.DiskSize
import com.liamxsage.energeticstorage.model.Disk
import com.liamxsage.energeticstorage.model.ESItem
import com.liamxsage.energeticstorage.model.DiskDrive
import dev.fruxz.ascend.tool.time.calendar.Calendar
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object DiskTable : UUIDTable("energeticstorage_disks") {

    val diskDrive = uuid("drive_uuid").nullable()
    val size = enumeration("size", DiskSize::class)
    val items = blob("items")

    val updatedAt = timestamp("updated_at")
}

/**
 * Saves the ESDrive to the database by replacing the existing record with the current values.
 * This method updates the DriveTable with the current values of the ESDrive instance.
 */
fun Disk.saveToDB() = transaction {
    DiskTable.replace {
        it[DiskTable.id] = this@saveToDB.uuid
        it[DiskTable.diskDrive] = this@saveToDB.diskDriveUUID
        it[DiskTable.size] = this@saveToDB.size
        it[DiskTable.items] = ExposedBlob(EnergeticStorage.instance.gson.toJson(this@saveToDB.items.toTypedArray()).toByteArray())
        it[DiskTable.updatedAt] = Calendar.now().javaInstant
    }
}

/**
 * Loads an ESDrive object from the database based on the given drive UUID.
 *
 * @param driveUUID The UUID of the drive to load.
 * @return The loaded ESDrive object.
 */
fun loadFromDB(driveUUID: UUID): Disk = transaction {
    val result = DiskTable.select(DiskTable.id eq driveUUID).single()

    val systemUUID = result[DiskTable.diskDrive]
    val size = result[DiskTable.size]
    val items = EnergeticStorage.instance.gson.fromJson(result[DiskTable.items].bytes.decodeToString(), Array<ESItem>::class.java).toMutableList()

    return@transaction Disk(driveUUID, size, items, systemUUID)
}

/**
 * Loads the drives associated with the ESSystem instance.
 * Retrieves drive data from the DriveTable and creates ESDrive objects.
 * Adds the created ESDrive objects to the ESSystem's drives list.
 */
fun DiskDrive.loadDisks() = transaction {
    val drives = DiskTable.selectAll().where(DiskTable.diskDrive eq this@loadDisks.uuid).map { row ->
        val driveUUID = row[DiskTable.id].value
        val size = row[DiskTable.size]
        val items = EnergeticStorage.instance.gson.fromJson(row[DiskTable.items].bytes.decodeToString(), Array<ESItem>::class.java).toMutableList()

        Disk(driveUUID, size, items, this@loadDisks.uuid)
    }

    this@loadDisks.disks.addAll(drives)
}