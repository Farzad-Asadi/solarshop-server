package com.example.data.repository

import com.example.data.table.ProductCategoriesTable
import com.example.server.dto.CategorySyncDto
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ProductCategoryRepository {

    fun getAll(): List<CategorySyncDto> {
        return transaction {
            ProductCategoriesTable
                .selectAll()
                .map { row ->
                    CategorySyncDto(
                        uid = row[ProductCategoriesTable.uid],
                        name = row[ProductCategoriesTable.name],
                        imageFileName = row[ProductCategoriesTable.imageFileName],
                        sortOrder = row[ProductCategoriesTable.sortOrder],
                        updatedAt = row[ProductCategoriesTable.updatedAt],
                        deletedAt = row[ProductCategoriesTable.deletedAt]
                    )
                }
        }
    }

    fun upsertAll(categories: List<CategorySyncDto>) {
        transaction {
            categories.forEach { category ->

                val existing = ProductCategoriesTable
                    .select { ProductCategoriesTable.uid eq category.uid }
                    .singleOrNull()

                if (existing == null) {
                    ProductCategoriesTable.insert {
                        it[uid] = category.uid
                        it[name] = category.name
                        it[imageFileName] = category.imageFileName
                        it[sortOrder] = category.sortOrder
                        it[updatedAt] = category.updatedAt
                        it[deletedAt] = null
                        it[deletedAt] = category.deletedAt
                    }
                } else {
                    val currentUpdatedAt = existing[ProductCategoriesTable.updatedAt]

                    if (category.updatedAt <= currentUpdatedAt) {
                        return@forEach
                    }
                    ProductCategoriesTable.update(
                        where = { ProductCategoriesTable.uid eq category.uid }
                    ) {
                        it[name] = category.name
                        it[imageFileName] = category.imageFileName
                        it[sortOrder] = category.sortOrder
                        it[updatedAt] = category.updatedAt
                        it[deletedAt] = category.deletedAt
                    }
                }
            }
        }
    }

    fun getChangedSince(since: Long): List<CategorySyncDto> {
        return transaction {
            ProductCategoriesTable
                .select {
                    ProductCategoriesTable.updatedAt greater since
                }
                .map { row ->
                    CategorySyncDto(
                        uid = row[ProductCategoriesTable.uid],
                        name = row[ProductCategoriesTable.name],
                        imageFileName = row[ProductCategoriesTable.imageFileName],
                        sortOrder = row[ProductCategoriesTable.sortOrder],
                        updatedAt = row[ProductCategoriesTable.updatedAt],
                        deletedAt = row[ProductCategoriesTable.deletedAt]
                    )
                }
        }
    }



}