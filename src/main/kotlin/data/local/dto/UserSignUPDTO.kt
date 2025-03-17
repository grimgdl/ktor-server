package com.grimco.data.local.dto

import com.grimco.application.Role
import kotlinx.serialization.Serializable


@Serializable
data class UserSignUPDTO(
    val name: String,
    val user: String,
    val password: String,
    val role: Int? = Role.USER.ordinal
)
