package com.grimco

import com.grimco.application.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

val env = System.getenv("APP_ENV") ?: ""
val configFile = if ("dev" in env) "application-dev.yaml" else "application.yaml"

fun Application.module() {
    configurationKoin()
    configurationCors()
    configurationDatabase()
    configureSecurity()
    configureSockets()
    configureRouting()
}

