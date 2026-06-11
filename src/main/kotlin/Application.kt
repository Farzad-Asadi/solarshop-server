package com.example

import com.auth0.jwt.JWT
import com.example.data.database.DatabaseFactory
import com.example.data.database.EntitlementRepository
import com.example.data.database.UserRepository
import com.example.server.auth.TokenService
import com.example.server.dto.*
import com.example.server.otp.OtpService
import com.example.server.sms.ConsoleSmsSender
import com.example.server.sms.SmsSender
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*

import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun main() {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

fun Application.module() {
    // JSON
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                prettyPrint = false
                explicitNulls = false
                encodeDefaults = true
            }
        )
    }

    // لاگ و CORS (فقط توسعه)
    install(CallLogging)
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
    }




    // --- کانفیگ ساده (در عمل از env/config بخوان)
    val issuer = "bambo.local"
    val audience = "bambo.app"
    val jwtSecret = System.getenv("JWT_SECRET") ?: "dev-secret-change-me"

    val sms: SmsSender = ConsoleSmsSender()
    val otp = OtpService(sms)
    val tokenService = TokenService(
        issuer, audience, secret = jwtSecret,
        accessTtlSec = 30,      // ⬅️ برای تست: 30 ثانیه
        refreshTtlSec = 600     // ⬅️ 10 دقیقه
    )

    val jwtVerifier = tokenService.verifier()

    // --- نصب JWT Auth
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwtVerifier)  // همانی که از TokenService گرفتی
            validate { cred ->
                // cred: JWTCredential
                val typ = cred.payload.getClaim("typ").asString() // 👈 به‌جای decode(token)
                if (typ == "access") {
                    JWTPrincipal(cred.payload)  // موفق
                } else {
                    null                        // ردِ احراز
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Unauthorized"))
            }
        }
    }

    DatabaseFactory.init(
        jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/bambo",
        driver = "org.postgresql.Driver",
        user = System.getenv("DB_USER") ?: "postgres",
        pass = System.getenv("DB_PASS") ?: "postgres"
    )

    val users = UserRepository()
    val ents = EntitlementRepository()

    routing {
        // سلامت
        get("/health") { call.respondText("ok") }

        // --- Auth ---
        route("/auth") {
            post("/request-otp") {
                val req = call.receive<RequestOtpRequest>()
                val res = otp.requestOtp(req.phone)
                if (res.isSuccess) {
                    call.respond(BasicOkResponse(ok = true))
                } else {
                    call.respond(HttpStatusCode.BadRequest, BasicOkResponse(ok = false, message = "invalid_phone"))
                }
            }

            post("/verify-otp") {
                val req = call.receive<VerifyOtpRequest>()
                val ok = otp.verifyOtp(req.phone, req.code)
                if (!ok) return@post call.respond(HttpStatusCode.Unauthorized, BasicOkResponse(false, "invalid_code"))

                // ۱) یوزر را از DB بگیر/بساز
                val u = users.getOrCreateByPhone(req.phone)
                users.touchLastLogin(u.id)

                // ۲) توکن‌ها
                val t = tokenService.issueTokens(u.id)

                // ۳) Entitlement از DB (یا مقدار پیش‌فرض)
                val ent = ents.getForUser(u.id) ?: EntitlementDto(isActive = false)

                call.respond(TokenResponse(t.access, t.refresh, user = u, entitlement = ent))
            }

            post("/refresh") {
                val req = call.receive<RefreshRequest>()
                // امضا/issuer/audience
                val decoded = try {
                    jwtVerifier.verify(req.refresh)  // اگر نامعتبر باشد، Exception می‌دهد
                } catch (_: Throwable) {
                    return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "invalid_refresh"))
                }
                // حالا decoded از نوع DecodedJWT است
                val typ = decoded.getClaim("typ").asString()
                if (typ != "refresh") {
                    return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "invalid_refresh"))
                }
                val userId = decoded.subject?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "invalid_refresh"))

                val t = tokenService.issueTokens(userId)
                val user = UserDto(userId, "+989121234567", System.currentTimeMillis() - 86_400_000)
                call.respond(TokenResponse(t.access, t.refresh, user = user))
            }

        }

        // --- مسیرهای محافظت‌شده با Bearer ---
        authenticate("auth-jwt") {
            get("/me") {
                val uid = call.principal<JWTPrincipal>()!!.subject!!.toInt()
                val u = users.getById(uid) ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(u)
            }

            get("/entitlements") {
                val uid = call.principal<JWTPrincipal>()!!.subject!!.toInt()
                val ent = ents.getForUser(uid) ?: EntitlementDto(isActive = false)
                call.respond(ent)
            }
        }
    }
}

