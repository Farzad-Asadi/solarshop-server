package com.example.data.repository

import com.example.data.table.CurrencyRatesTable
import com.example.server.dto.CurrencyRateSyncDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class CurrencyRateRepository {

    fun getAll(): List<CurrencyRateSyncDto> = transaction {
        CurrencyRatesTable
            .selectAll()
            .map { it.toDto() }
    }

    fun getChangedSince(since: Long): List<CurrencyRateSyncDto> = transaction {
        CurrencyRatesTable
            .select {
                CurrencyRatesTable.serverUpdatedAt  greater since
            }
            .map { it.toDto() }
    }

    fun upsertAll(items: List<CurrencyRateSyncDto>) = transaction {
        items.forEach { item ->

            val acceptedAt =
                System.currentTimeMillis()

            val existing =
                CurrencyRatesTable
                    .select {
                        CurrencyRatesTable.uid eq item.uid
                    }
                    .singleOrNull()


            if (existing == null) {
                CurrencyRatesTable.insert {
                    it[uid] = item.uid
                    it[currencyCode] = item.currencyCode
                    it[rateToman] = item.rateToman
                    it[sourceText] = item.source
                    it[note] = item.note
                    it[createdAt] = item.createdAt
                    it[updatedAt] = item.updatedAt
                    it[serverUpdatedAt] = acceptedAt
                    it[deletedAt] = item.deletedAt
                }
            } else {
                val currentUpdatedAt =
                    existing[CurrencyRatesTable.updatedAt]

                val currentDeletedAt =
                    existing[CurrencyRatesTable.deletedAt]

                val incomingIsDelete =
                    item.deletedAt != null

                val currentIsDeleted =
                    currentDeletedAt != null

                // Delete Wins
                if (currentIsDeleted && !incomingIsDelete) {
                    return@forEach
                }

                val shouldAccept =
                    incomingIsDelete || item.updatedAt > currentUpdatedAt

                if (!shouldAccept) {
                    return@forEach
                }


                CurrencyRatesTable.update({
                    CurrencyRatesTable.uid eq item.uid
                }) {
                    it[currencyCode] = item.currencyCode
                    it[rateToman] = item.rateToman
                    it[sourceText] = item.source
                    it[note] = item.note
                    it[createdAt] = item.createdAt
                    it[updatedAt] =item.updatedAt
                    it[serverUpdatedAt] = acceptedAt
                    it[deletedAt] = item.deletedAt
                }
            }
        }
    }

    private fun ResultRow.toDto(): CurrencyRateSyncDto {
        return CurrencyRateSyncDto(
            uid = this[CurrencyRatesTable.uid],
            currencyCode = this[CurrencyRatesTable.currencyCode],
            rateToman = this[CurrencyRatesTable.rateToman],
            source = this[CurrencyRatesTable.sourceText],
            note = this[CurrencyRatesTable.note],
            createdAt = this[CurrencyRatesTable.createdAt],
            updatedAt = this[CurrencyRatesTable.updatedAt],
            deletedAt = this[CurrencyRatesTable.deletedAt]
        )
    }

    fun insertFetchedUsdRate(
        rateToman: Long,
        source: String = "brs_api",
        note: String = "دریافت از BRS API توسط سرور"
    ): CurrencyRateSyncDto {
        val now = System.currentTimeMillis()

        val dto = CurrencyRateSyncDto(
            uid = java.util.UUID.randomUUID().toString(),
            currencyCode = "USD",
            rateToman = rateToman,
            source = source,
            note = note,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )

        upsertAll(listOf(dto))

        return dto
    }
}