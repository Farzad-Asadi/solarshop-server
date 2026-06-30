package com.example.data.repository

import com.example.data.table.ProductSaleTransactionsTable
import com.example.server.dto.ProductSaleTransactionSyncDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ProductSaleTransactionRepository {

    fun getAll(): List<ProductSaleTransactionSyncDto> =
        transaction {
            ProductSaleTransactionsTable
                .selectAll()
                .map { it.toDto() }
        }

    fun getChangedSince(
        since: Long
    ): List<ProductSaleTransactionSyncDto> =
        transaction {
            ProductSaleTransactionsTable
                .select {
                    ProductSaleTransactionsTable.updatedAt greater since
                }
                .map { it.toDto() }
        }

    fun upsertAll(
        items: List<ProductSaleTransactionSyncDto>
    ) = transaction {
        items.forEach { item ->

            val existing =
                ProductSaleTransactionsTable
                    .select {
                        ProductSaleTransactionsTable.uid eq item.uid
                    }
                    .singleOrNull()

            if (existing == null) {
                ProductSaleTransactionsTable.insert {
                    it[uid] = item.uid

                    it[productUid] = item.productUid

                    it[inventoryTransactionUid] =
                        item.inventoryTransactionUid

                    it[quantity] = item.quantity

                    it[priceType] = item.priceType

                    it[unitSalePriceToman] =
                        item.unitSalePriceToman

                    it[totalSalePriceToman] =
                        item.totalSalePriceToman

                    it[saleDollarRateToman] =
                        item.saleDollarRateToman

                    it[purchasePriceUid] =
                        item.purchasePriceUid

                    it[salePriceUid] =
                        item.salePriceUid

                    it[buyPriceDollar] =
                        item.buyPriceDollar

                    it[buyPriceToman] =
                        item.buyPriceToman

                    it[purchaseDollarRateToman] =
                        item.purchaseDollarRateToman

                    it[unitSalePriceDollar] =
                        item.unitSalePriceDollar

                    it[unitProfitToman] =
                        item.unitProfitToman

                    it[totalProfitToman] =
                        item.totalProfitToman

                    it[unitProfitDollar] =
                        item.unitProfitDollar

                    it[totalProfitDollar] =
                        item.totalProfitDollar

                    it[profitPercentByToman] =
                        item.profitPercentByToman

                    it[profitPercentByDollar] =
                        item.profitPercentByDollar

                    it[soldAt] =
                        item.soldAt

                    it[note] =
                        item.note

                    it[createdAt] =
                        item.createdAt

                    it[updatedAt] =
                        item.updatedAt

                    it[deletedAt] =
                        item.deletedAt
                }
            } else {
                val currentUpdatedAt =
                    existing[ProductSaleTransactionsTable.updatedAt]

                val currentDeletedAt =
                    existing[ProductSaleTransactionsTable.deletedAt]

                val incomingIsDelete =
                    item.deletedAt != null

                val currentIsDeleted =
                    currentDeletedAt != null

                if (currentIsDeleted && !incomingIsDelete) {
                    return@forEach
                }

                val shouldAccept =
                    incomingIsDelete || item.updatedAt > currentUpdatedAt

                if (!shouldAccept) {
                    return@forEach
                }

                ProductSaleTransactionsTable.update({
                    ProductSaleTransactionsTable.uid eq item.uid
                }) {
                    it[productUid] =
                        item.productUid

                    it[inventoryTransactionUid] =
                        item.inventoryTransactionUid

                    it[quantity] =
                        item.quantity

                    it[priceType] =
                        item.priceType

                    it[unitSalePriceToman] =
                        item.unitSalePriceToman

                    it[totalSalePriceToman] =
                        item.totalSalePriceToman

                    it[saleDollarRateToman] =
                        item.saleDollarRateToman

                    it[purchasePriceUid] =
                        item.purchasePriceUid

                    it[salePriceUid] =
                        item.salePriceUid

                    it[buyPriceDollar] =
                        item.buyPriceDollar

                    it[buyPriceToman] =
                        item.buyPriceToman

                    it[purchaseDollarRateToman] =
                        item.purchaseDollarRateToman

                    it[unitSalePriceDollar] =
                        item.unitSalePriceDollar

                    it[unitProfitToman] =
                        item.unitProfitToman

                    it[totalProfitToman] =
                        item.totalProfitToman

                    it[unitProfitDollar] =
                        item.unitProfitDollar

                    it[totalProfitDollar] =
                        item.totalProfitDollar

                    it[profitPercentByToman] =
                        item.profitPercentByToman

                    it[profitPercentByDollar] =
                        item.profitPercentByDollar

                    it[soldAt] =
                        item.soldAt

                    it[note] =
                        item.note

                    it[createdAt] =
                        item.createdAt

                    it[updatedAt] =
                        if (incomingIsDelete) {
                            System.currentTimeMillis()
                        } else {
                            item.updatedAt
                        }

                    it[deletedAt] =
                        item.deletedAt
                }
            }
        }
    }

    private fun ResultRow.toDto(): ProductSaleTransactionSyncDto {
        return ProductSaleTransactionSyncDto(
            uid =
            this[ProductSaleTransactionsTable.uid],

            productUid =
            this[ProductSaleTransactionsTable.productUid],

            inventoryTransactionUid =
            this[ProductSaleTransactionsTable.inventoryTransactionUid],

            quantity =
            this[ProductSaleTransactionsTable.quantity],

            priceType =
            this[ProductSaleTransactionsTable.priceType],

            unitSalePriceToman =
            this[ProductSaleTransactionsTable.unitSalePriceToman],

            totalSalePriceToman =
            this[ProductSaleTransactionsTable.totalSalePriceToman],

            saleDollarRateToman =
            this[ProductSaleTransactionsTable.saleDollarRateToman],

            purchasePriceUid =
            this[ProductSaleTransactionsTable.purchasePriceUid],

            salePriceUid =
            this[ProductSaleTransactionsTable.salePriceUid],

            buyPriceDollar =
            this[ProductSaleTransactionsTable.buyPriceDollar],

            buyPriceToman =
            this[ProductSaleTransactionsTable.buyPriceToman],

            purchaseDollarRateToman =
            this[ProductSaleTransactionsTable.purchaseDollarRateToman],

            unitSalePriceDollar =
            this[ProductSaleTransactionsTable.unitSalePriceDollar],

            unitProfitToman =
            this[ProductSaleTransactionsTable.unitProfitToman],

            totalProfitToman =
            this[ProductSaleTransactionsTable.totalProfitToman],

            unitProfitDollar =
            this[ProductSaleTransactionsTable.unitProfitDollar],

            totalProfitDollar =
            this[ProductSaleTransactionsTable.totalProfitDollar],

            profitPercentByToman =
            this[ProductSaleTransactionsTable.profitPercentByToman],

            profitPercentByDollar =
            this[ProductSaleTransactionsTable.profitPercentByDollar],

            soldAt =
            this[ProductSaleTransactionsTable.soldAt],

            note =
            this[ProductSaleTransactionsTable.note],

            createdAt =
            this[ProductSaleTransactionsTable.createdAt],

            updatedAt =
            this[ProductSaleTransactionsTable.updatedAt],

            deletedAt =
            this[ProductSaleTransactionsTable.deletedAt]
        )
    }
}