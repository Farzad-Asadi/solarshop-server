package com.example.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductSalePriceSyncDto(
    val uid: String,
    val productUid: String,

    val priceType: String,

    val salePriceToman: Long,

    val profitPercent: Double?,

    val baseDollarPrice: Double?,

    val dollarRateToman: Long?,

    val basePurchasePriceToman: Long?,

    val note: String,

    val isActive: Boolean,

    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?
)