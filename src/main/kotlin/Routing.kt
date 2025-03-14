package com.grimco

import com.grimco.data.local.Users
import com.grimco.data.local.UsersDTO
import com.grimco.data.local.dto.JsonResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureRouting() {

    install(ContentNegotiation) {
       json(Json {
           ignoreUnknownKeys = true
       })
    }

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/users") {
            val users: List<UsersDTO> = transaction {
                Users.selectAll().map { row ->
                    UsersDTO(
                        id = row[Users.id],
                        name = row[Users.name]
                    )
                }
            }
            call.respond(
                JsonResponse(
                    body = users,
                    status = HttpStatusCode.OK.value
                )
            )
        }
    }
}

