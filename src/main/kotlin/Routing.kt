package com.grimco

import com.grimco.data.local.Users
import com.grimco.data.local.UsersDTO
import com.grimco.data.local.dto.JsonResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
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

        authenticate("auth-jwt") {

            get("/users") {
                call.requireRol("user.create")

                val principal = call.principal<JWTPrincipal>()

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

        get("/") {
            call.respondText("Hello World!")
        }


        get("{...}"){
            call.respond(HttpStatusCode.NotFound, "not found \uD83D\uDE21")

        }
    }
}

suspend fun ApplicationCall.requireRol(role: String) {
    val principal = principal<JWTPrincipal>()
    val roles = principal?.payload?.getClaim("role").toString()
    if (role !in roles){
        respond(HttpStatusCode.Forbidden, "Access Deny")
        throw AuthorizationException("Role required")
    }
}

class AuthorizationException(message: String): Exception(message)
