package com.liamxsage.energeticstorage.database

import com.liamxsage.energeticstorage.EnergeticStorage
import com.liamxsage.energeticstorage.extensions.getLogger
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * A singleton object representing a connection to the database.
 */
internal object DatabaseConnection {

    /**
     * Configuration class for the database connection.
     */
    private val dbConfig = HikariConfig().apply {
        jdbcUrl = buildJDBCUrl()
        driverClassName = buildDriverClass()
        username = EnergeticStorage.instance.config.getString("storage.username")
        password = EnergeticStorage.instance.config.getString("storage.password")
        maximumPoolSize = 100
    }

    private fun buildJDBCUrl(): String {
        val type = EnergeticStorage.instance.config.getString("storage.type") // MariaDB, MySQL, SQLite
        val host = EnergeticStorage.instance.config.getString("storage.host")
        val port = EnergeticStorage.instance.config.getString("storage.port")
        val database = EnergeticStorage.instance.config.getString("storage.database")
        return when (type) {
            "mariadb" -> "jdbc:mariadb://$host:$port/$database"
            "mysql" -> "jdbc:mysql://$host:$port/$database"
            "sqlite" -> "jdbc:sqlite:${EnergeticStorage.instance.dataFolder}/$database"
            "postgresql" -> "jdbc:postgresql://$host:$port/$database"
            "h2" -> "jdbc:h2:file:${EnergeticStorage.instance.dataFolder}/$database"
            else -> throw IllegalArgumentException("Invalid database type: $type")
        }
    }

    private fun buildDriverClass(): String {
        return when (val type = EnergeticStorage.instance.config.getString("storage.type")) {
            "mariadb" -> "org.mariadb.jdbc.Driver"
            "mysql" -> "com.mysql.cj.jdbc.Driver"
            "sqlite" -> "org.sqlite.JDBC"
            "postgresql" -> "org.postgresql.Driver"
            "h2" -> "org.h2.Driver"
            else -> throw IllegalArgumentException("Invalid database type: $type")
        }
    }

    /**
     * Represents a database connection.
     *
     * This variable is used to establish a connection to the database using the provided database configuration.
     * It is a private property, so it can only be accessed within the scope of its containing class.
     *
     * @property database The database connection instance.
     */
    private val database = Database.connect(HikariDataSource(dbConfig))

    /**
     * Connects to the database and performs necessary operations.
     */
    fun connect() {
        getLogger().info("Connecting to database...")
        database

        getLogger().info("Check for table updates...")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(

            )
        }

        getLogger().info("Connected to database.")
    }

    /**
     * Disconnects from the database.
     * This method closes the database connection and logs a message indicating disconnection.
     */
    fun disconnect() {
        database.connector().close()
        getLogger().info("Disconnected from database.")
    }
}