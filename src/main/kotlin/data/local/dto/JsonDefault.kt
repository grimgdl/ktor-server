package com.grimco.data.local.dto

import kotlinx.serialization.Serializable

@Serializable
data class JsonDefault(
    val message: String,
    val code: Int
)
