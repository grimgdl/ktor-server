package com.grimco.domain.service

import com.grimco.application.JwtUtils
import com.grimco.application.Role
import com.grimco.data.local.Users
import com.grimco.data.local.dto.LoginDTO
import com.grimco.data.local.dto.LoginSuccessDTO
import com.grimco.domain.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt


class AuthService(private val userRepository: UserRepository, private val config: ApplicationConfig) {

    suspend fun login(call: ApplicationCall, dataLogin: LoginDTO){


        val user = transaction {
            Users.selectAll()
                .where { Users.username eq dataLogin.username }
                .singleOrNull()
        }
        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized, "Access Deny")
            return
        }

        if (!BCrypt.checkpw(dataLogin.password, user[Users.password])) {
            call.respond(HttpStatusCode.Unauthorized, "Access Deny")
            return
        }
        val role = Role.entries[user[Users.role] ?: Role.USER.ordinal].name
        val accessToken = JwtUtils.generateToken(config, user[Users.uuid], role, JwtUtils.TokenType.ACCESS)
        val refreshToken = JwtUtils.generateToken(config, user[Users.uuid], role, JwtUtils.TokenType.REFRESH)

        call.response.cookies.append(
            Cookie(
                name = "refreshToken",
                value = refreshToken,
                domain = "grimgdl.ddns.net",
                httpOnly = true,
                secure = false,
                path = "/",
                maxAge = 7 * 24 * 60 * 60 // 7 days

            )
        )


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