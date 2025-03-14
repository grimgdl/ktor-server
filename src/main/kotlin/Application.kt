package com.grimco

import com.grimco.data.local.DatabaseFactory
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    try {
        DatabaseFactory.init()
    }catch (e: Exception) {
        println("Error in database ${e.message}")
    }
    configureSecurity()
    configureSockets()
    configureRouting()
}
