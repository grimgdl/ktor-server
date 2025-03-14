package com.grimco.data.local.dto

import kotlinx.serialization.Serializable


@Serializable
data class JsonResponse<T>(val body: T, val status: Int)
