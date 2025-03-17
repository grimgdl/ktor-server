package com.grimco

import com.grimco.application.configurationDatabase
import com.grimco.application.configureRouting
import com.grimco.application.configureSecurity
import com.grimco.application.configureSockets
import com.grimco.data.repository.UserRepositoryImp
import com.grimco.domain.repository.UserRepository
import com.grimco.domain.service.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.cors.routing.*
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

val env = System.getenv("APP_ENV") ?: ""
val configFile = if ("dev" in env) "application-dev.yaml" else "application.yaml"

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        allowHost("localhost:5173", schemes = listOf("http"))
        allowHost("localhost", schemes = listOf("http"))

        allowCredentials = true
        allowSameOrigin = true

        anyHost()
    }

    val config: ApplicationConfig by inject()

    configurationDatabase(config)
    configureSecurity(config)
    configureSockets()
    configureRouting()
}


val appModule = module {
    single<ApplicationConfig> { ApplicationConfig(configFile) }
    single<UserRepository> { UserRepositoryImp() }
    single<AuthService> { AuthService(get(), get()) }
}
