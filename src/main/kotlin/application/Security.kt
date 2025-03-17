package com.grimco.application

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.response.*

fun Application.configureSecurity(config: ApplicationConfig) {
    val jwtAudience = config.property("jwt.audience").getString()
    val jwtDomain = config.property("jwt.domain").getString()
    val jwtRealm = config.property("jwt.realm").getString()
    val jwtSecret = config.property("jwt.secret").getString()

    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->

                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                println("🔴 Token inválido o ausente") // <-- Si el token es inválido, verás esto
                call.respond(HttpStatusCode.Unauthorized, "Access Deny")
            }
        }
    }
}


