package com.grimco.presentation.routes

import com.grimco.application.Role
import com.grimco.application.authorized
import com.grimco.data.local.Users
import com.grimco.data.local.UsersDTO
import com.grimco.data.local.dto.JsonResponse
import com.grimco.data.local.dto.LoginDTO
import com.grimco.data.local.dto.UserSignUPDTO
import com.grimco.domain.service.AuthService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.util.*

fun Route.apiRoutes(authService: AuthService, config: ApplicationConfig) {
    route("/api") {

        authorized(Role.ADMIN.name, config = config) {

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


        post("/upload"){
            var fileName = ""
            var fileDescription = ""
            val multipart = call.receiveMultipart()

            multipart.forEachPart { part ->
                when(part){
                    is PartData.FileItem -> {
                        println("file item ${part.originalFileName}")
                        val name = part.originalFileName.toString().split(".")
                        fileName = "${name.first()}${UUID.randomUUID()}.${name.last()}"
                        println("filename $fileName")
                        val fileByte = part.provider().readRemaining().readByteArray()
                        val file = File("D:\\images", fileName)

                        file.writeBytes(fileByte)

                        if(file.exists()) {
                            call.respond(HttpStatusCode.OK, "Uploaded")
                        }else{
                            call.respond(HttpStatusCode.Conflict, "Failed")
                        }

                    }

                    is PartData.FormItem -> {
                        println("file form ${part.value}")
                        fileDescription = part.value
                    }
                    else -> {
                        println("something else")
                    }
                }
                part.dispose()
            }

        }


        post("/login") {
            val dataLogin = call.receive<LoginDTO>()
            authService.login(call, dataLogin)
        }

    }


}