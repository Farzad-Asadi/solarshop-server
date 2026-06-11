package com.example.server.sms

interface SmsSender {
    suspend fun send(toE164: String, message: String)
}

class ConsoleSmsSender : SmsSender {
    override suspend fun send(toE164: String, message: String) {
        println("SMS → $toE164 : $message")
    }
}