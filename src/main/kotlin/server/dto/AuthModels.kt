package com.example.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class RequestOtpRequest(val phone: String)

@Serializable
data class VerifyOtpRequest(val phone: String, val code: String)

@Serializable
data class RefreshRequest(val refresh: String)

// پاسخ نهایی بعد از verify/refresh
@Serializable
data class TokenResponse(
    val access: String,
    val refresh: String,
    val user: UserDto,
    val entitlement: EntitlementDto? = null
)

// برای مراحل بعد (پریمیوم)، همین الان مدل ساده داشته باشیم
@Serializable
data class EntitlementDto(
    val isActive: Boolean,
    val plan: String? = null,
    val expiresAt: Long? = null
)

@Serializable
data class BasicOkResponse(val ok: Boolean, val message: String? = null)
