package com.grimco

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

object JwtUtils {

    fun generateToken(config: ApplicationConfig, userName: String):String {

        val jwtDomain = config.property("jwt.domain").getString()
        val jwtAudience = config.property("jwt.audience").getString()
        val jwtSecret = config.property("jwt.secret").getString()
        val expirationTime = System.currentTimeMillis() + 3_600_000 * 4
        val claim = "user.create,user.show,user.delete"

        return JWT.create()
            .withAudience(jwtAudience)
            .withSubject(userName)
            .withClaim("role", claim)
            .withIssuer(jwtDomain)
            .withIssuedAt(Date())
            .withExpiresAt(Date(expirationTime))
            .sign(Algorithm.HMAC256(jwtSecret))
    }

}