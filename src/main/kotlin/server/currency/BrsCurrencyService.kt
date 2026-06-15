package com.example.server.currency

import com.example.server.dto.BrsMarketResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class BrsCurrencyService(
    private val client: HttpClient,
    private val apiKey: String
) {

    suspend fun fetchUsdRateToman(): Long? {
        val response: BrsMarketResponseDto =
            client.get("https://Api.BrsApi.ir/Market/Gold_Currency.php") {
                parameter("key", apiKey)
            }.body()

        val usdItem =
            response.currency.firstOrNull {
                it.symbol.equals("USD", ignoreCase = true)
            } ?: response.currency.firstOrNull {
                it.name?.contains("دلار") == true
            }

        return usdItem?.price
    }
}