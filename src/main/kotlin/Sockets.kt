package com.grimco

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds


val connections = ConcurrentHashMap<String, WebSocketSession>()

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        authenticate("auth-jwt") {
            webSocket("/ws") { // websocketSession
                val principal = call.principal<JWTPrincipal>()
                val uuid = principal?.subject ?: "unknown"

                connections[uuid] = this

                for (frame in incoming) {
                    if (frame is Frame.Text) {

                        val text = frame.readText()
                        println(text)
                        outgoing.send(Frame.Text("YOU SAID: $text"))
                        if (text.equals("bye", ignoreCase = true)) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        }
                    }
                }

            }
        }

    }
}
