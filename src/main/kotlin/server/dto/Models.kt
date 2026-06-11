package com.example.server.dto

import kotlinx.serialization.Serializable


@Serializable
data class UserDto(
    val id: Int,
    val phone: String,
    val createdAt: Long
)
