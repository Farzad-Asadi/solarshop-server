package com.example.data.repository

import com.example.data.table.ProductSalePricesTable
import com.example.server.dto.ProductSalePriceSyncDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ProductSalePriceRepository {

    fun getAll(): List<ProductSalePriceSyncDto> = transaction {
        ProductSalePricesTable
            .selectAll()
            .map { it.toDto() }
    }

    fun getChangedSince(since: Long): List<ProductSalePriceSyncDto> = transaction {
        ProductSalePricesTable
            .select {
                ProductSalePricesTable.updatedAt greater since
            }
            .map { it.toDto() }
    }

    fun upsertAll(items: List<ProductSalePriceSyncDto>) = transaction {
        items.forEach { item ->

            val existing = ProductSalePricesTable
                .select {
                    ProductSalePricesTable.uid eq item.uid
                }
                .singleOrNull()

            if (existing == null) {
                ProductSalePricesTable.insert {
                    it[uid] = item.uid
                    it[productUid] = item.productUid
                    it[priceType] = item.priceType
                    it[salePriceToman] = item.salePriceToman
                    it[profitPercent] = item.profitPercent
                    it[baseDollarPrice] = item.baseDollarPrice
                    it[dollarRateToman] = item.dollarRateToman
                    it[basePurchasePriceToman] = item.basePurchasePriceToman
                    it[note] = item.note
                    it[isActive] = item.isActive
                    it[createdAt] = item.createdAt
                    it[updatedAt] = item.updatedAt
                    it[deletedAt] = item.deletedAt
                }
            } else {
                val currentUpdatedAt =
                    existing[ProductSalePricesTable.updatedAt]

                val currentDeletedAt =
                    existing[ProductSalePricesTable.deletedAt]

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

                ProductSalePricesTable.update({
                    ProductSalePricesTable.uid eq item.uid
                }) {
                    it[productUid] = item.productUid
                    it[priceType] = item.priceType
                    it[salePriceToman] = item.salePriceToman
                    it[profitPercent] = item.profitPercent
                    it[baseDollarPrice] = item.baseDollarPrice
                    it[dollarRateToman] = item.dollarRateToman
                    it[basePurchasePriceToman] = item.basePurchasePriceToman
                    it[note] = item.note
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

    private fun ResultRow.toDto(): ProductSalePriceSyncDto {
        return ProductSalePriceSyncDto(
            uid = this[ProductSalePricesTable.uid],
            productUid = this[ProductSalePricesTable.productUid],
            priceType = this[ProductSalePricesTable.priceType],
            salePriceToman = this[ProductSalePricesTable.salePriceToman],
            profitPercent = this[ProductSalePricesTable.profitPercent],
            baseDollarPrice = this[ProductSalePricesTable.baseDollarPrice],
            dollarRateToman = this[ProductSalePricesTable.dollarRateToman],
            basePurchasePriceToman = this[ProductSalePricesTable.basePurchasePriceToman],
            note = this[ProductSalePricesTable.note],
            isActive = this[ProductSalePricesTable.isActive],
            createdAt = this[ProductSalePricesTable.createdAt],
            updatedAt = this[ProductSalePricesTable.updatedAt],
            deletedAt = this[ProductSalePricesTable.deletedAt]
        )
    }
}