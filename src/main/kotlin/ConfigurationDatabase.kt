package com.grimco

import com.grimco.data.local.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.flywaydb.core.Flyway

fun Application.configurationDatabase(config: ApplicationConfig) {
    val url = config.property("ktor.database.url").getString()
    val nameDB = config.property("ktor.database.name").getString()
    val driver = config.property("ktor.database.driver").getString()
    val user = config.property("ktor.database.user").getString()
    val password = config.property("ktor.database.password").getString()

    try {
        DatabaseFactory.init(url = url, driver = driver, databaseName = nameDB, user = user, passwd = password)
    }catch (e: Exception) {
        println("Error in database ${e.message}")
    }


    Flyway.configure()
        .dataSource("$url$nameDB", user, password)
        .baselineOnMigrate(true)
        .load()
        .migrate()

}