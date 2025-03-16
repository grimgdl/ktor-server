package com.grimco.data.local

import com.grimco.Role
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

object DatabaseFactory{

    fun init(url: String, driver: String, databaseName: String ,user: String, passwd: String) {

        createDatabaseIFNotExists(url, driver, databaseName ,user, passwd)

        val dataSource = hikari(url, driver, databaseName, user, passwd)
        val database = Database.connect(dataSource)
        createSchema(database)
    }

    private fun createDatabaseIFNotExists(url: String, driver: String, nameDB: String ,user: String, passwd: String) {
        val db = Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = passwd
        )

        transaction(db) {
            exec("CREATE DATABASE IF NOT EXISTS `$nameDB`")
            exec("SET FOREIGN_KEY_CHECKS = 1;")
        }
    }


    private fun createSchema(db: Database) {
        transaction(db) {
            SchemaUtils.create(Users)

            Users.insertIgnore {
                it[name] = "Admin"
                it[uuid] = UUID.randomUUID().toString()
                it[username] = "admin"
                it[role] = Role.ADMIN.ordinal
                it[password] = BCrypt.hashpw("gus19890719", BCrypt.gensalt())
            }

        }
    }

    private fun hikari(url: String, driver: String, databaseName: String,user: String, passwd: String): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = "$url$databaseName?useSSL=false&serverTimezone=UTC"
            driverClassName = driver
            username = user
            password = passwd
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        return HikariDataSource(config)
    }


}