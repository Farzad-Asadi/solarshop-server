package com.example.data.repository

import com.example.data.table.ProductAttributeValuesTable
import com.example.data.table.ProductsTable
import com.example.server.dto.ProductAttributeValueSyncDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ProductAttributeValueRepository {

    fun getAll(): List<ProductAttributeValueSyncDto> = transaction {
        ProductAttributeValuesTable
            .selectAll()
            .map { it.toDto() }
    }

    fun getChangedSince(since: Long): List<ProductAttributeValueSyncDto> = transaction {
        ProductAttributeValuesTable
            .select {
                ProductAttributeValuesTable.serverUpdatedAt greater since
            }
            .map { it.toDto() }
    }

    fun upsertAll(items: List<ProductAttributeValueSyncDto>) = transaction {
        items.forEach { item ->

            val acceptedAt =
                System.currentTimeMillis()

            val existing = ProductAttributeValuesTable
                .select {
                    ProductAttributeValuesTable.uid eq item.uid
                }
                .singleOrNull()

            if (existing == null) {
                ProductAttributeValuesTable.insert {
                    it[uid] = item.uid
                    it[productUid] = item.productUid
                    it[attributeDefinitionUid] = item.attributeDefinitionUid
                    it[valueText] = item.valueText
                    it[updatedAt] = item.updatedAt
                    it[serverUpdatedAt] = acceptedAt
                    it[deletedAt] = item.deletedAt
                }
            } else {
                val currentUpdatedAt =
                    existing[ProductAttributeValuesTable.updatedAt]

                val currentDeletedAt =
                    existing[ProductAttributeValuesTable.deletedAt]

                val incomingIsDelete = item.deletedAt != null
                val currentIsDeleted = currentDeletedAt != null

                // Delete Wins
                if (currentIsDeleted && !incomingIsDelete) {
                    return@forEach
                }

                val shouldAccept =
                    incomingIsDelete || item.updatedAt > currentUpdatedAt

                if (!shouldAccept) {
                    return@forEach
                }

                ProductAttributeValuesTable.update({
                    ProductAttributeValuesTable.uid eq item.uid
                }) {
                    it[productUid] = item.productUid
                    it[attributeDefinitionUid] = item.attributeDefinitionUid
                    it[valueText] = item.valueText
                    it[updatedAt] = item.updatedAt
                    it[serverUpdatedAt] = acceptedAt
                    it[deletedAt] = item.deletedAt
                }
            }
        }
    }

    private fun ResultRow.toDto(): ProductAttributeValueSyncDto {
        return ProductAttributeValueSyncDto(
            uid = this[ProductAttributeValuesTable.uid],
            productUid = this[ProductAttributeValuesTable.productUid],
            attributeDefinitionUid = this[ProductAttributeValuesTable.attributeDefinitionUid],
            valueText = this[ProductAttributeValuesTable.valueText],
            updatedAt = this[ProductAttributeValuesTable.updatedAt],
            deletedAt = this[ProductAttributeValuesTable.deletedAt]
        )
    }
}