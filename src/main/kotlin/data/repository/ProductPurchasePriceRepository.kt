package com.example.data.repository

import com.example.data.table.ProductPurchasePricesTable
import com.example.data.table.ProductsTable
import com.example.server.dto.ProductPurchasePriceSyncDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ProductPurchasePriceRepository {

    fun getAll(): List<ProductPurchasePriceSyncDto> = transaction {
        ProductPurchasePricesTable
            .selectAll()
            .map { it.toDto() }
    }

    fun getChangedSince(since: Long): List<ProductPurchasePriceSyncDto> = transaction {
        ProductPurchasePricesTable
            .select {
                ProductPurchasePricesTable.serverUpdatedAt greater since
            }
            .map { it.toDto() }
    }

    fun upsertAll(items: List<ProductPurchasePriceSyncDto>) = transaction {
        items.forEach { item ->

            val acceptedAt =
                System.currentTimeMillis()

            val existing = ProductPurchasePricesTable
                .select {
                    ProductPurchasePricesTable.uid eq item.uid
                }
                .singleOrNull()

            if (existing == null) {
                ProductPurchasePricesTable.insert {
                    it[uid] = item.uid
                    it[productUid] = item.productUid
                    it[buyPriceDollar] = item.buyPriceDollar
                    it[buyPriceToman] = item.buyPriceToman
                    it[dollarRateToman] = item.dollarRateToman
                    it[quantity] = item.quantity
                    it[purchasedAt] = item.purchasedAt
                    it[note] = item.note
                    it[isActive] = item.isActive
                    it[createdAt] = item.createdAt
                    it[updatedAt] = item.updatedAt
                    it[serverUpdatedAt] = acceptedAt
                    it[deletedAt] = item.deletedAt
                }
            } else {
                val currentUpdatedAt =
                    existing[ProductPurchasePricesTable.updatedAt]

                val currentDeletedAt =
                    existing[ProductPurchasePricesTable.deletedAt]

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

                ProductPurchasePricesTable.update({
                    ProductPurchasePricesTable.uid eq item.uid
                }) {
                    it[productUid] = item.productUid
                    it[buyPriceDollar] = item.buyPriceDollar
                    it[buyPriceToman] = item.buyPriceToman
                    it[dollarRateToman] = item.dollarRateToman
                    it[quantity] = item.quantity
                    it[purchasedAt] = item.purchasedAt
                    it[note] = item.note
                    it[isActive] = item.isActive
                    it[createdAt] = item.createdAt
                    it[updatedAt] = item.updatedAt
                    it[serverUpdatedAt] = acceptedAt
                    it[deletedAt] = item.deletedAt
                }
            }
        }
    }

    private fun ResultRow.toDto(): ProductPurchasePriceSyncDto {
        return ProductPurchasePriceSyncDto(
            uid = this[ProductPurchasePricesTable.uid],
            productUid = this[ProductPurchasePricesTable.productUid],
            buyPriceDollar = this[ProductPurchasePricesTable.buyPriceDollar],
            buyPriceToman = this[ProductPurchasePricesTable.buyPriceToman],
            dollarRateToman = this[ProductPurchasePricesTable.dollarRateToman],
            quantity = this[ProductPurchasePricesTable.quantity],
            purchasedAt = this[ProductPurchasePricesTable.purchasedAt],
            note = this[ProductPurchasePricesTable.note],
            isActive = this[ProductPurchasePricesTable.isActive],
            createdAt = this[ProductPurchasePricesTable.createdAt],
            updatedAt = this[ProductPurchasePricesTable.updatedAt],
            deletedAt = this[ProductPurchasePricesTable.deletedAt]
        )
    }
}