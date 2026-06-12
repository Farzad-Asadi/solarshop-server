package com.example.server.dto

import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class SyncPingResponse(
    val ok: Boolean,
    val message: String
)

@Serializable
data class SyncStatusResponse(
    val serverTime: Long,
    val serverVersion: Int,
    val message: String
)

@Serializable
data class RegisterDeviceRequest(
    val deviceId: String,
    val appVersion: Int,
    val platform: String
)

@Serializable
data class RegisterDeviceResponse(
    val accepted: Boolean,
    val serverVersion: Int
)