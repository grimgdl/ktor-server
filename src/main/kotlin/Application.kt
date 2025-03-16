package com.grimco

import io.ktor.server.application.*
import io.ktor.server.config.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val env = System.getenv("APP_ENV") ?: ""
    val configFile = if ("dev" in env) "application-dev.yaml" else "application.yaml"
    val config = ApplicationConfig(configFile)
    configurationDatabase(config)
    configureSecurity(config)
    configureSockets()
    configureRouting(config)
}
