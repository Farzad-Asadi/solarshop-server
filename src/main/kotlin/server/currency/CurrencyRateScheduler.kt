package com.example.server.currency

import com.example.data.repository.CurrencyRateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CurrencyRateScheduler(
    private val brsCurrencyService: BrsCurrencyService,
    private val currencyRateRepository: CurrencyRateRepository,
    private val intervalMillis: Long = 30 * 60 * 1000L
) {

    fun start(scope: CoroutineScope): Job {
        return scope.launch(Dispatchers.IO) {
            while (isActive) {
                fetchAndSaveNow()
                delay(intervalMillis)
            }
        }
    }

    suspend fun fetchAndSaveNow(): Long? {
        return try {
            val rate =
                brsCurrencyService.fetchUsdRateToman()

            if (rate == null || rate <= 0L) {
                println("CurrencyRateScheduler: USD rate not found. Keeping last saved rate.")
                return null
            }

            currencyRateRepository.insertFetchedUsdRate(
                rateToman = rate
            )

            println("CurrencyRateScheduler: USD rate saved = $rate")
            rate

        } catch (e: Exception) {
            println("CurrencyRateScheduler failed: ${e.message}. Keeping last saved rate.")
            null
        }
    }
}