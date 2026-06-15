package com.example.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyFetchResponse(
    val ok: Boolean,
    val rateToman: Long? = null,
    val message: String? = null
)