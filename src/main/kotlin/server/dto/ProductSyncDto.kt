package com.example.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductSyncDto(
    val uid: String,
    val categoryUid: String?,
    val brandUid: String?,
    val name: String,
    val model: String,
    val description: String,
    val isArchived: Boolean,
    val updatedAt: Long,
    val deletedAt: Long?
)