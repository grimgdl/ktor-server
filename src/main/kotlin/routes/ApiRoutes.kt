package com.grimco.routes

import com.grimco.JwtUtils
import com.grimco.Role
import com.grimco.authorized
import com.grimco.data.local.Users
import com.grimco.data.local.UsersDTO
import com.grimco.data.local.dto.JsonResponse
import com.grimco.data.local.dto.LoginDTO
import com.grimco.data.local.dto.LoginSuccessDTO
import com.grimco.data.local.dto.UserSignUPDTO
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

fun Route.apiRoutes(config: ApplicationConfig) {

    route("/api") {

        authorized(Role.ADMIN.name) {

            post("/signup") {
                val userData = call.receive<UserSignUPDTO>()
                val userId = kotlin.runCatching {
                    transaction {
                        Users.insert {
                            it[name] = userData.name
                            it[password] = BCrypt.hashpw(userData.password, BCrypt.gensalt()).toString()
                            it[username] = userData.user
                            it[uuid] = UUID.randomUUID().toString()
                            it[role] = userData.role
                        }
                    }
                }

                if (userId.isSuccess) {
                    call.respondText("Created")
                } else {
                    call.respond(HttpStatusCode.Conflict, "Failed ${userId.exceptionOrNull()?.cause?.message}")
                }
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

        post("/login") {
            val dataLogin = call.receive<LoginDTO>()
            val user = transaction {
                Users.selectAll()
                    .where { Users.username eq dataLogin.username }
                    .singleOrNull()
            }
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Access Deny")
                return@post
            }

            if (!BCrypt.checkpw(dataLogin.password, user[Users.password])) {
                call.respond(HttpStatusCode.Unauthorized, "Access Deny")
                return@post
            }
            val role = Role.entries[user[Users.role] ?: Role.USER.ordinal].name
            val accessToken = JwtUtils.generateToken(config, user[Users.uuid], role, JwtUtils.TokenType.ACCESS)
            val refreshToken = JwtUtils.generateToken(config, user[Users.uuid], role, JwtUtils.TokenType.REFRESH)
            call.respond(
                HttpStatusCode.OK,
                LoginSuccessDTO(
                    uuid = user[Users.uuid],
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            )
        }

    }


}