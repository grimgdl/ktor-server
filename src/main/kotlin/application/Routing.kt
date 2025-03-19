package com.grimco.application

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.grimco.domain.service.AuthService
import com.grimco.presentation.routes.apiRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject



fun Application.configureRouting() {

    val config by inject<ApplicationConfig>()

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }

    routing {

        apiRoutes(application.get(), config)

        route("/") {
            val isDev = System.getenv("APP_ENV")?.contains("dev") ?: false
            val filesPath = if (isDev) "D:\\Projects\\Server\\frontend\\dist" else "react"
            singlePageApplication {
                react(filesPath)
            }
        }
    }
}

class PluginConfiguration {
    var roles: Set<String> = emptySet()
    lateinit var configValues: ApplicationConfig
}


val RoleBasedAuthorizationPlugin = createRouteScopedPlugin(
    name = "RoleBasedAuthPlugin",
    createConfiguration = ::PluginConfiguration
) {
    val roles = pluginConfig.roles
    val config = pluginConfig.configValues

    onCall { call ->
        call.cookieValidation(config)
    }

    on(AuthenticationChecked){ call ->
        call.requireRol(roles)
    }

}


fun ApplicationCall.cookieValidation(config: ApplicationConfig) {
    val token = request.cookies["refreshToken"] ?: return
    val jwtSecret = config.property("jwt.secret").getString()
    val jwtAudience = config.property("jwt.audience").getString()
    val jwtDomain = config.property("jwt.domain").getString()
    try {
        val verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
            .withAudience(jwtAudience)
            .withIssuer(jwtDomain)
            .build()
        val decodedJWT = verifier.verify(token)
        val principal = JWTPrincipal(decodedJWT)
        authentication.principal(principal)
    } catch (e: Exception) {
        println("? token cookie not valid: ${e.message}")
    }

}


suspend fun ApplicationCall.requireRol(roles: Set<String>) {
    val principal = principal<JWTPrincipal>()
    val roleToken = principal?.payload?.getClaim("role")?.asString()
    if (roleToken !in roles) {
        respond(HttpStatusCode.Forbidden, "Forbidden Access")
    }
}

fun Route.authorized(
    vararg hasAnyRole: String,
    config: ApplicationConfig,
    build: Route.() -> Unit
) {
    authenticate("auth-jwt") {
        install(RoleBasedAuthorizationPlugin) {
            roles = hasAnyRole.toSet()
            configValues = config
        }
        build()
    }
}

