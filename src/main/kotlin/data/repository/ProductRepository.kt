package com.example.data.repository

import com.example.data.table.ProductsTable
import com.example.server.dto.ProductSyncDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ProductRepository {

    fun getAll(): List<ProductSyncDto> {
        return transaction {
            ProductsTable
                .selectAll()
                .map(::toDto)
        }
    }

    fun getChangedSince(
        since: Long
    ): List<ProductSyncDto> {
        return transaction {
            ProductsTable
                .select {
                    ProductsTable.updatedAt greater since
                }
                .map(::toDto)
        }
    }

    fun upsertAll(
        products: List<ProductSyncDto>
    ) {
        transaction {

            products.forEach { product ->

                val existing =
                    ProductsTable
                        .select {
                            ProductsTable.uid eq product.uid
                        }
                        .singleOrNull()

                if (existing == null) {

                    ProductsTable.insert {
                        it[uid] = product.uid
                        it[categoryUid] = product.categoryUid
                        it[brandUid] = product.brandUid
                        it[name] = product.name
                        it[model] = product.model
                        it[description] = product.description
                        it[isArchived] = product.isArchived
                        it[updatedAt] = product.updatedAt
                        it[deletedAt] = product.deletedAt
                    }

                } else {
                    val currentUpdatedAt = existing[ProductsTable.updatedAt]
                    val currentDeletedAt = existing[ProductsTable.deletedAt]

                    val incomingIsDelete = product.deletedAt != null
                    val currentIsDeleted = currentDeletedAt != null

// اگر روی سرور حذف شده، و ورودی فقط ویرایش معمولی است، اجازه زنده شدن نده
                    if (currentIsDeleted && !incomingIsDelete) {
                        return@forEach
                    }

// اگر ورودی حذف است، حذف باید حتی روی ویرایش جدیدتر هم برنده شود
                    val shouldAccept = incomingIsDelete || product.updatedAt > currentUpdatedAt

                    if (!shouldAccept) {
                        return@forEach
                    }

                    ProductsTable.update(
                        { ProductsTable.uid eq product.uid }
                    ) {
                        it[categoryUid] = product.categoryUid
                        it[brandUid] = product.brandUid
                        it[name] = product.name
                        it[model] = product.model
                        it[description] = product.description
                        it[isArchived] = product.isArchived
                        it[updatedAt] = if (incomingIsDelete) System.currentTimeMillis() else product.updatedAt
                        it[deletedAt] = product.deletedAt
                    }
                }
            }
        }
    }

    private fun toDto(
        row: ResultRow
    ): ProductSyncDto {

        return ProductSyncDto(
            uid = row[ProductsTable.uid],
            categoryUid = row[ProductsTable.categoryUid],
            brandUid = row[ProductsTable.brandUid],
            name = row[ProductsTable.name],
            model = row[ProductsTable.model],
            description = row[ProductsTable.description],
            isArchived = row[ProductsTable.isArchived],
            updatedAt = row[ProductsTable.updatedAt],
            deletedAt = row[ProductsTable.deletedAt]
        )
    }
}