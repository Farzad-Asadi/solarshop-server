package com.example.data.repository

import com.example.data.table.ProductUnitsTable
import com.example.server.dto.ProductUnitSyncDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ProductUnitRepository {

    fun getAll(): List<ProductUnitSyncDto> = transaction {
        ProductUnitsTable
            .selectAll()
            .map { it.toDto() }
    }

    fun getChangedSince(since: Long): List<ProductUnitSyncDto> = transaction {
        ProductUnitsTable
            .select {
                ProductUnitsTable.updatedAt greater since
            }
            .map { it.toDto() }
    }

    fun upsertAll(items: List<ProductUnitSyncDto>) = transaction {
        items.forEach { item ->

            val existing =
                ProductUnitsTable
                    .select {
                        ProductUnitsTable.uid eq item.uid
                    }
                    .singleOrNull()

            if (existing == null) {
                ProductUnitsTable.insert {
                    it[uid] = item.uid
                    it[name] = item.name
                    it[symbol] = item.symbol
                    it[isActive] = item.isActive
                    it[createdAt] = item.createdAt
                    it[updatedAt] = item.updatedAt
                    it[deletedAt] = item.deletedAt
                }
            } else {
                val currentUpdatedAt =
                    existing[ProductUnitsTable.updatedAt]

                val currentDeletedAt =
                    existing[ProductUnitsTable.deletedAt]

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

                ProductUnitsTable.update({
                    ProductUnitsTable.uid eq item.uid
                }) {
                    it[name] = item.name
                    it[symbol] = item.symbol
                    it[isActive] = item.isActive
                    it[createdAt] = item.createdAt
                    it[updatedAt] =
                        if (incomingIsDelete) System.currentTimeMillis()
                        else item.updatedAt
                    it[deletedAt] = item.deletedAt
                }
            }
        }
    }

    private fun ResultRow.toDto(): ProductUnitSyncDto {
        return ProductUnitSyncDto(
            uid = this[ProductUnitsTable.uid],
            name = this[ProductUnitsTable.name],
            symbol = this[ProductUnitsTable.symbol],
            isActive = this[ProductUnitsTable.isActive],
            createdAt = this[ProductUnitsTable.createdAt],
            updatedAt = this[ProductUnitsTable.updatedAt],
            deletedAt = this[ProductUnitsTable.deletedAt]
        )
    }
}