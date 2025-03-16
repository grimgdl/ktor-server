package com.grimco

import com.grimco.data.local.Users
import com.grimco.data.local.UsersDTO
import com.grimco.data.local.dto.JsonResponse
import com.grimco.data.local.dto.LoginDTO
import com.grimco.data.local.dto.LoginSuccessDTO
import com.grimco.data.local.dto.UserSignUPDTO
import com.grimco.routes.apiRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.util.*


fun Application.configureRouting(config: ApplicationConfig) {

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }

//    install(RoleBasedAuthorizationPlugin)

    routing {

        apiRoutes(config)

        route("/") {
            val isDev = System.getenv("APP_ENV")?.contains("dev") ?: false
            val filesPath = if(isDev) "D:\\Projects\\Server\\react" else "react"
            singlePageApplication {
                react(filesPath)
            }
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

fun Route.authorized(
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

