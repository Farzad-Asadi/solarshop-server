package com.example

import com.auth0.jwt.JWT
import com.example.data.database.DatabaseFactory
import com.example.data.database.EntitlementRepository
import com.example.data.database.UserRepository
import com.example.data.repository.*
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
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.io.File

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
    val issuer = "solarshop.local"
    val audience = "solarshop.app"
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


    println("DB_URL = ${System.getenv("DB_URL")}")
    println("DB_USER = ${System.getenv("DB_USER")}")

    DatabaseFactory.init(
        jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/solarshop",
        driver = "org.postgresql.Driver",
        user = System.getenv("DB_USER") ?: "postgres",
        pass = System.getenv("DB_PASS") ?: "postgres"
    )

    val users = UserRepository()
    val ents = EntitlementRepository()
    val syncDeviceRepository = SyncDeviceRepository()
    val categoryRepository = ProductCategoryRepository()
    val brandRepository = ProductBrandRepository()
    val productRepository = ProductRepository()
    val productImageRepository = ProductImageRepository()
    val inventoryTransactionRepository = InventoryTransactionRepository()
    val productPurchasePriceRepository = ProductPurchasePriceRepository()
    val productSalePriceRepository = ProductSalePriceRepository()

    val uploadsDir = File(
        System.getenv("UPLOADS_DIR") ?: "uploads"
    ).apply {
        mkdirs()
    }

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



        route("/files") {

            post("/upload") {
                val multipart = call.receiveMultipart()

                var savedFileName: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val originalName = part.originalFileName ?: "image.jpg"

                            val safeName = originalName
                                .substringAfterLast("/")
                                .substringAfterLast("\\")
                                .replace(Regex("[^A-Za-z0-9._-]"), "_")

                            val finalFileName = if (safeName.startsWith("img_")) {
                                safeName
                            } else {
                                "img_${System.currentTimeMillis()}_$safeName"
                            }

                            val targetFile = File(uploadsDir, finalFileName)

                            part.streamProvider().use { input ->
                                targetFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }

                            savedFileName = finalFileName
                        }

                        else -> Unit
                    }

                    part.dispose()
                }

                val fileName = savedFileName
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("message" to "file_not_found")
                    )

                call.respond(
                    mapOf(
                        "fileName" to fileName
                    )
                )
            }

            get("/{fileName}") {
                val fileName = call.parameters["fileName"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val safeName = fileName
                    .substringAfterLast("/")
                    .substringAfterLast("\\")

                val file = File(uploadsDir, safeName)

                if (!file.exists()) {
                    return@get call.respond(HttpStatusCode.NotFound)
                }

                call.respondFile(file)
            }

            get("/exists/{fileName}") {
                val fileName = call.parameters["fileName"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val safeName = fileName
                    .substringAfterLast("/")
                    .substringAfterLast("\\")

                val exists = File(uploadsDir, safeName).exists()

                call.respond(
                    mapOf(
                        "exists" to exists
                    )
                )
            }
        }

        route("/sync") {

            get("/ping") {
                call.respond(
                    SyncPingResponse(
                        ok = true,
                        message = "pong"
                    )
                )
            }

            get("/status") {
                call.respond(
                    SyncStatusResponse(
                        serverTime = System.currentTimeMillis(),
                        serverVersion = 1,
                        message = "server ready"
                    )
                )
            }

            post("/register-device") {
                val request = call.receive<RegisterDeviceRequest>()

                syncDeviceRepository.registerOrUpdate(
                    deviceId = request.deviceId,
                    platform = request.platform,
                    appVersion = request.appVersion
                )

                call.respond(
                    RegisterDeviceResponse(
                        accepted = true,
                        serverVersion = 1
                    )
                )
            }

            get("/categories") {
                val since = call.request.queryParameters["since"]
                    ?.toLongOrNull()
                    ?: 0L

                val categories = if (since > 0L) {
                    categoryRepository.getChangedSince(since)
                } else {
                    categoryRepository.getAll()
                }

                call.respond(categories)
            }
            post("/categories") {
                val categories = call.receive<List<CategorySyncDto>>()

                categoryRepository.upsertAll(categories)

                call.respond(
                    BasicOkResponse(
                        ok = true,
                        message = "categories synced"
                    )
                )
            }

            get("/brands") {
                val since = call.request.queryParameters["since"]
                    ?.toLongOrNull()
                    ?: 0L

                val brands = if (since > 0L) {
                    brandRepository.getChangedSince(since)
                } else {
                    brandRepository.getAll()
                }

                call.respond(brands)
            }
            post("/brands") {
                val brands = call.receive<List<BrandSyncDto>>()

                brandRepository.upsertAll(brands)

                call.respond(
                    BasicOkResponse(
                        ok = true,
                        message = "brands synced"
                    )
                )
            }

            get("/products") {
                val since = call.request.queryParameters["since"]
                    ?.toLongOrNull()
                    ?: 0L

                val products = if (since > 0L) {
                    productRepository.getChangedSince(since)
                } else {
                    productRepository.getAll()
                }

                call.respond(products)
            }
            post("/products") {
                val products = call.receive<List<ProductSyncDto>>()

                productRepository.upsertAll(products)

                call.respond(
                    BasicOkResponse(
                        ok = true,
                        message = "products synced"
                    )
                )
            }
            get("/product-images") {
                val since = call.request.queryParameters["since"]
                    ?.toLongOrNull()
                    ?: 0L

                val images = if (since > 0L) {
                    productImageRepository.getChangedSince(since)
                } else {
                    productImageRepository.getAll()
                }

                call.respond(images)
            }
            post("/product-images") {
                val images = call.receive<List<ProductImageSyncDto>>()

                productImageRepository.upsertAll(images)

                call.respond(
                    BasicOkResponse(
                        ok = true,
                        message = "product images synced"
                    )
                )
            }

            get("/inventory-transactions") {

                val since = call.request.queryParameters["since"]
                    ?.toLongOrNull()
                    ?: 0L

                val items =
                    if (since > 0L) {
                        inventoryTransactionRepository
                            .getChangedSince(since)
                    } else {
                        inventoryTransactionRepository
                            .getAll()
                    }

                call.respond(items)
            }
            post("/inventory-transactions") {

                val items =
                    call.receive<List<InventoryTransactionSyncDto>>()

                inventoryTransactionRepository.upsertAll(items)

                call.respond(
                    BasicOkResponse(
                        ok = true,
                        message = "inventory transactions synced"
                    )
                )
            }

            get("/purchase-prices") {

                val since = call.request.queryParameters["since"]
                    ?.toLongOrNull()
                    ?: 0L

                val items =
                    if (since > 0L) {
                        productPurchasePriceRepository
                            .getChangedSince(since)
                    } else {
                        productPurchasePriceRepository
                            .getAll()
                    }

                call.respond(items)
            }
            post("/purchase-prices") {

                val items =
                    call.receive<List<ProductPurchasePriceSyncDto>>()

                productPurchasePriceRepository.upsertAll(items)

                call.respond(
                    BasicOkResponse(
                        ok = true,
                        message = "purchase prices synced"
                    )
                )
            }

            get("/sale-prices") {

                val since = call.request.queryParameters["since"]
                    ?.toLongOrNull()
                    ?: 0L

                val items =
                    if (since > 0L) {
                        productSalePriceRepository
                            .getChangedSince(since)
                    } else {
                        productSalePriceRepository
                            .getAll()
                    }

                call.respond(items)
            }
            post("/sale-prices") {

                val items =
                    call.receive<List<ProductSalePriceSyncDto>>()

                productSalePriceRepository.upsertAll(items)

                call.respond(
                    BasicOkResponse(
                        ok = true,
                        message = "sale prices synced"
                    )
                )
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

