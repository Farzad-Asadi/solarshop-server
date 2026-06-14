package com.example.data.repository

import com.example.data.table.ProductImagesTable
import com.example.server.dto.ProductImageSyncDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ProductImageRepository {

    fun getAll(): List<ProductImageSyncDto> = transaction {
        ProductImagesTable
            .selectAll()
            .map { it.toDto() }
    }

    fun getChangedSince(since: Long): List<ProductImageSyncDto> = transaction {
        ProductImagesTable
            .select {
                ProductImagesTable.updatedAt greater since
            }
            .map { it.toDto() }
    }

    fun upsertAll(images: List<ProductImageSyncDto>) = transaction {
        images.forEach { image ->

            val existing = ProductImagesTable
                .select {
                    ProductImagesTable.uid eq image.uid
                }
                .singleOrNull()

            if (existing == null) {
                ProductImagesTable.insert {
                    it[uid] = image.uid
                    it[productUid] = image.productUid
                    it[fileName] = image.fileName
                    it[sortOrder] = image.sortOrder
                    it[updatedAt] = image.updatedAt
                    it[deletedAt] = image.deletedAt
                }
            } else {
                val currentUpdatedAt = existing[ProductImagesTable.updatedAt]
                val currentDeletedAt = existing[ProductImagesTable.deletedAt]

                val incomingIsDelete = image.deletedAt != null
                val currentIsDeleted = currentDeletedAt != null

                if (currentIsDeleted && !incomingIsDelete) {
                    return@forEach
                }

                val shouldAccept =
                    incomingIsDelete || image.updatedAt > currentUpdatedAt

                if (!shouldAccept) {
                    return@forEach
                }

                ProductImagesTable.update({
                    ProductImagesTable.uid eq image.uid
                }) {
                    it[productUid] = image.productUid
                    it[fileName] = image.fileName
                    it[sortOrder] = image.sortOrder
                    it[updatedAt] =
                        if (incomingIsDelete) System.currentTimeMillis()
                        else image.updatedAt
                    it[deletedAt] = image.deletedAt
                }
            }
        }
    }

    private fun ResultRow.toDto(): ProductImageSyncDto {
        return ProductImageSyncDto(
            uid = this[ProductImagesTable.uid],
            productUid = this[ProductImagesTable.productUid],
            fileName = this[ProductImagesTable.fileName],
            sortOrder = this[ProductImagesTable.sortOrder],
            updatedAt = this[ProductImagesTable.updatedAt],
            deletedAt = this[ProductImagesTable.deletedAt]
        )
    }
}