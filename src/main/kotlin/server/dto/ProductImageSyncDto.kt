package com.example.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductImageSyncDto(
    val uid: String,
    val productUid: String,
    val fileName: String,
    val sortOrder: Int,
    val updatedAt: Long,
    val deletedAt: Long?
)