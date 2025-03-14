package com.grimco.data.local

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {

        createDatabaseIFNotExists()
        val dataSource = hikari()
        val database = Database.connect(dataSource)
        createSchema(database)
    }

    private fun createDatabaseIFNotExists() {
        val db = Database.connect(
            url = "jdbc:mariadb://localhost:3307/",
            driver = "org.mariadb.jdbc.Driver",
            user = "root",
            password = ""
        )

        transaction(db) {
            exec("CREATE DATABASE IF NOT EXISTS `order-kotlin` ")
            exec("SET FOREIGN_KEY_CHECKS = 1;")
        }
    }

    private fun createSchema(db: Database) {
        transaction(db) {
            SchemaUtils.create(Users)
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:mariadb://localhost:3307/order-kotlin?useSSL=false&serverTimezone=UTC"
            driverClassName = "org.mariadb.jdbc.Driver"
            username = "root"
            password = ""
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        return HikariDataSource(config)
    }


}