package com.example.server.otp

import com.example.server.sms.SmsSender
import kotlin.random.Random
import java.util.concurrent.ConcurrentHashMap

private data class OtpEntry(val code: String, val expiresAt: Long, var attempts: Int = 0)

/**
 * سرویس سادهٔ OTP با ذخیرهٔ موقت در حافظه:
 * - کد ۶ رقمی
 * - اعتبار ۲ دقیقه
 * - محدودیت ارسال مجدد: هر ۶۰ ثانیه
 */
class OtpService(
    private val sms: SmsSender,
    private val ttlMillis: Long = 2 * 60_000L,
    private val resendWindowMillis: Long = 60_000L
) {
    private val store = ConcurrentHashMap<String, OtpEntry>()
    private val lastSend = ConcurrentHashMap<String, Long>() // برای ریت‌لیمیت

    private fun generateCode(): String = Random.nextInt(100000, 999999).toString()

    private fun isLikelyE164(phone: String): Boolean {
        // چک ساده: + و ارقام، طول معقول
        return phone.startsWith("+") && phone.length in 8..18 && phone.drop(1).all { it.isDigit() }
    }

    suspend fun requestOtp(phone: String): Result<Unit> {
        if (!isLikelyE164(phone)) return Result.failure(IllegalArgumentException("invalid_phone"))

        val now = System.currentTimeMillis()
        val last = lastSend[phone] ?: 0L
        if (now - last < resendWindowMillis) {
            // بی‌صدا موفق برمی‌گردیم تا سمت کلاینت دوباره درخواست نزند (یا می‌توانی failure برگردانی)
            return Result.success(Unit)
        }

        val code = generateCode()
        val expires = now + ttlMillis
        store[phone] = OtpEntry(code, expires)
        lastSend[phone] = now

        sms.send(phone, "کد ورود شما: $code")
        return Result.success(Unit)
    }

    fun verifyOtp(phone: String, code: String): Boolean {
        val entry = store[phone] ?: return false
        val now = System.currentTimeMillis()
        if (now > entry.expiresAt) {
            store.remove(phone); return false
        }
        if (entry.code != code) {
            entry.attempts++
            if (entry.attempts >= 5) store.remove(phone)
            return false
        }
        // موفق: یک‌بار مصرف
        store.remove(phone)
        return true
    }
}
