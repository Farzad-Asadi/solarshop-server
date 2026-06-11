package com.example.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.*
import java.util.Date

data class Tokens(val access: String, val refresh: String)

class TokenService(
    private val issuer: String,
    private val audience: String,
    private val secret: String,
    private val accessTtlSec: Long = 900,
    private val refreshTtlSec: Long = 2_592_000
) {
    fun issueTokens(userId: Int): TokenPair {
        val now = System.currentTimeMillis()
        val accessExp  = java.util.Date(now + accessTtlSec * 1000)
        val refreshExp = java.util.Date(now + refreshTtlSec * 1000)

        val access = com.auth0.jwt.JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withClaim("typ", "access")
            .withExpiresAt(accessExp)
            .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(secret))

        val refresh = com.auth0.jwt.JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withClaim("typ", "refresh")
            .withExpiresAt(refreshExp)
            .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(secret))

        return TokenPair(access, refresh)
    }

    fun verifier(): com.auth0.jwt.JWTVerifier =
        com.auth0.jwt.JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC256(secret))
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
}

// مدل استاندارد توکن‌ها
data class TokenPair(
    val access: String,
    val refresh: String
)
