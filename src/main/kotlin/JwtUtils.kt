package com.grimco

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

object JwtUtils {


    enum class TokenType {
        ACCESS,
        REFRESH
    }

    fun generateToken(config: ApplicationConfig, uuid: String, role: String, tokenType: TokenType):String {

        val jwtDomain = config.property("jwt.domain").getString()
        val jwtAudience = config.property("jwt.audience").getString()
        val jwtSecret = config.property("jwt.secret").getString()
        val expirationTime = when (tokenType) {
            TokenType.ACCESS -> System.currentTimeMillis() + 3_600_000 * 4
            TokenType.REFRESH -> System.currentTimeMillis() + ((3_600_000 * 24) * 60 )
        }

        return JWT.create()
            .withAudience(jwtAudience)
            .withSubject(uuid)
            .withClaim("role", role)
            .withIssuer(jwtDomain)
            .withIssuedAt(Date())
            .withExpiresAt(Date(expirationTime))
            .sign(Algorithm.HMAC256(jwtSecret))
    }


}