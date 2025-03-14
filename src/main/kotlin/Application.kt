package com.grimco

import com.grimco.data.local.DatabaseFactory
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.config.yaml.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {


    val env = System.getenv("APP_ENV") ?: ""
    val configFile = if ("dev" in env) "application-dev.yaml" else "application.yaml"

    val config = ApplicationConfig(configFile)

    val url = config.property("ktor.database.url").getString()
    val nameDB = config.property("ktor.database.name").getString()
    val driver = config.property("ktor.database.driver").getString()
    val user = config.property("ktor.database.user").getString()
    val password = config.property("ktor.database.password").getString()

    println("u: $user p: $password url: $url name: $nameDB")

    try {
        DatabaseFactory.init(url = url, driver = driver, databaseName = nameDB, user = user, passwd = password)
    }catch (e: Exception) {
        println("Error in database ${e.message}")
    }
    configureSecurity()
    configureSockets()
    configureRouting()
}
