package com.grimco.data.local.dto

import kotlinx.serialization.Serializable


@Serializable
data class LoginSuccessDTO(
    val uuid: String,
    val accessToken: String,
    val refreshToken: String
)
