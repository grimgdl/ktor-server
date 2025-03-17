package com.grimco.domain.repository

import com.grimco.data.local.Users
import com.grimco.data.local.UsersDTO

interface UserRepository {
    fun findUserByName(username: String): UsersDTO?
}