package com.grimco.application

import com.grimco.configFile
import com.grimco.data.repository.UserRepositoryImp
import com.grimco.domain.repository.UserRepository
import com.grimco.domain.service.AuthService
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


fun Application.configurationKoin() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}


val appModule = module {
    single<ApplicationConfig> { ApplicationConfig(configFile) }
    single<UserRepository> { UserRepositoryImp() }
    single<AuthService> { AuthService(get(), get()) }
}
