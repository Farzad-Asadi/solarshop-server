package com.example.data.repository

import com.example.data.table.CurrencyRatesTable
import com.example.data.table.InventoryTransactionsTable
import com.example.server.dto.InventoryTransactionSyncDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class InventoryTransactionRepository {

    fun getAll(): List<InventoryTransactionSyncDto> = transaction {
        InventoryTransactionsTable
            .selectAll()
            .map { it.toDto() }
    }

    fun getChangedSince(since: Long): List<InventoryTransactionSyncDto> = transaction {
        InventoryTransactionsTable
            .select {
                InventoryTransactionsTable.serverUpdatedAt greater since
            }
            .map { it.toDto() }
    }

    fun upsertAll(items: List<InventoryTransactionSyncDto>) = transaction {
        items.forEach { item ->
            val acceptedAt =
                System.currentTimeMillis()

            val existing = InventoryTransactionsTable
                .select {
                    InventoryTransactionsTable.uid eq item.uid
                }
                .singleOrNull()



            if (existing == null) {
                InventoryTransactionsTable.insert {
                    it[uid] = item.uid
                    it[productUid] = item.productUid
                    it[quantity] = item.quantity
                    it[transactionType] = item.transactionType
                    it[note] = item.note
                    it[createdAt] = item.createdAt
                    it[updatedAt] = item.updatedAt
                    it[serverUpdatedAt] = acceptedAt
                    it[deletedAt] = item.deletedAt
                }
            } else {
                val currentUpdatedAt = existing[InventoryTransactionsTable.updatedAt]
                val currentDeletedAt = existing[InventoryTransactionsTable.deletedAt]

                val incomingIsDelete = item.deletedAt != null
                val currentIsDeleted = currentDeletedAt != null

                if (currentIsDeleted && !incomingIsDelete) {
                    return@forEach
                }

                val shouldAccept =
                    incomingIsDelete || item.updatedAt > currentUpdatedAt

                if (!shouldAccept) {
                    return@forEach
                }

                InventoryTransactionsTable.update({
                    InventoryTransactionsTable.uid eq item.uid
                }) {
                    it[productUid] = item.productUid
                    it[quantity] = item.quantity
                    it[transactionType] = item.transactionType
                    it[note] = item.note
                    it[createdAt] = item.createdAt
                    it[updatedAt] = item.updatedAt
                    it[serverUpdatedAt] = acceptedAt
                    it[deletedAt] = item.deletedAt
                }
            }
        }
    }

    private fun ResultRow.toDto(): InventoryTransactionSyncDto {
        return InventoryTransactionSyncDto(
            uid = this[InventoryTransactionsTable.uid],
            productUid = this[InventoryTransactionsTable.productUid],
            quantity = this[InventoryTransactionsTable.quantity],
            transactionType = this[InventoryTransactionsTable.transactionType],
            note = this[InventoryTransactionsTable.note],
            createdAt = this[InventoryTransactionsTable.createdAt],
            updatedAt = this[InventoryTransactionsTable.updatedAt],
            deletedAt = this[InventoryTransactionsTable.deletedAt]
        )
    }
}