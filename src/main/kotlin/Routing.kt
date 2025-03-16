package com.grimco

import com.grimco.data.local.Users
import com.grimco.data.local.UsersDTO
import com.grimco.data.local.dto.JsonResponse
import com.grimco.data.local.dto.LoginDTO
import com.grimco.data.local.dto.LoginSuccessDTO
import com.grimco.data.local.dto.UserSignUPDTO
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*


fun Application.configureRouting(config: ApplicationConfig) {

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }

//    install(RoleBasedAuthorizationPlugin)

    routing {
        authorized(Role.ADMIN.name) {

            get("/run") {
                call.respond("run")
            }

            get("/run2") {
                call.respond("run 2")
            }
            post("/signup") {
                try {
                    val userData = call.receive<UserSignUPDTO>()
                    val userId = transaction {

                        Users.insert {
                            it[name] = userData.name
                            it[password] = BCrypt.hashpw(userData.password, BCrypt.gensalt()).toString()
                            it[username] = userData.user
                            it[uuid] = UUID.randomUUID().toString()
                            it[role] = userData.role
                        }

                    }
                    call.respondText("Created")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, "NOt created")
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
        get("/test") {
            val principal = call.principal<JWTPrincipal>()
            println("test $principal")
        }

        post("/login") {
            val dataLogin = call.receive<LoginDTO>()
            val user = transaction {
                Users.selectAll()
                    .where { Users.username eq dataLogin.username}
                    .singleOrNull()
            }
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Access Deny")
                return@post
            }

            if(!BCrypt.checkpw(dataLogin.password, user[Users.password])){
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
        get("/") {
            call.respondText("Landing page")
        }
        get("{...}") {
            call.respond(HttpStatusCode.NotFound, "not found \uD83D\uDE21")
        }
    }
}


class PluginConfiguration {
    var roles: Set<String> = emptySet()
}

val RoleBasedAuthorizationPlugin = createRouteScopedPlugin(
    name = "RoleBasedAuthPlugin",
    createConfiguration = ::PluginConfiguration
) {
    val roles = pluginConfig.roles
    pluginConfig.apply {
        on(AuthenticationChecked) { call ->
            call.requireRol(roles)
        }
    }
}

suspend fun ApplicationCall.requireRol(roles: Set<String>) {
    val principal = principal<JWTPrincipal>()
    println("principal $principal")
    val roleToken = principal?.payload?.getClaim("role")?.asString()
    println("roletoken $roleToken")
    println("roles $roles")
    if (roleToken !in roles) {
        respond(HttpStatusCode.Forbidden, "Access Deny")
    }
}

private fun Route.authorized(
    vararg hasAnyRole: String,
    build: Route.() -> Unit
) {
    authenticate("auth-jwt") {
        install(RoleBasedAuthorizationPlugin){
            roles = hasAnyRole.toSet()
        }
        build()
    }
}

