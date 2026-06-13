package com.example.data.repository

import com.example.data.table.ProductBrandsTable
import com.example.server.dto.BrandSyncDto
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ProductBrandRepository {

    fun getAll(): List<BrandSyncDto> {
        return transaction {
            ProductBrandsTable
                .selectAll()
                .map { row ->
                    BrandSyncDto(
                        uid = row[ProductBrandsTable.uid],
                        name = row[ProductBrandsTable.name],
                        description = row[ProductBrandsTable.description],
                        imageFileName = row[ProductBrandsTable.imageFileName],
                        isActive = row[ProductBrandsTable.isActive],
                        updatedAt = row[ProductBrandsTable.updatedAt],
                        deletedAt = row[ProductBrandsTable.deletedAt]
                    )
                }
        }
    }

    fun getChangedSince(since: Long): List<BrandSyncDto> {
        return transaction {
            ProductBrandsTable
                .select { ProductBrandsTable.updatedAt greater since }
                .map { row ->
                    BrandSyncDto(
                        uid = row[ProductBrandsTable.uid],
                        name = row[ProductBrandsTable.name],
                        description = row[ProductBrandsTable.description],
                        imageFileName = row[ProductBrandsTable.imageFileName],
                        isActive = row[ProductBrandsTable.isActive],
                        updatedAt = row[ProductBrandsTable.updatedAt],
                        deletedAt = row[ProductBrandsTable.deletedAt]
                    )
                }
        }
    }

    fun upsertAll(brands: List<BrandSyncDto>) {
        transaction {
            brands.forEach { brand ->

                val existing = ProductBrandsTable
                    .select { ProductBrandsTable.uid eq brand.uid }
                    .singleOrNull()

                if (existing == null) {
                    ProductBrandsTable.insert {
                        it[uid] = brand.uid
                        it[name] = brand.name
                        it[description] = brand.description
                        it[imageFileName] = brand.imageFileName
                        it[isActive] = brand.isActive
                        it[updatedAt] = brand.updatedAt
                        it[deletedAt] = null
                        it[deletedAt] = brand.deletedAt
                    }
                } else {
                    val currentUpdatedAt = existing[ProductBrandsTable.updatedAt]

                    if (brand.updatedAt <= currentUpdatedAt) {
                        return@forEach
                    }
                    ProductBrandsTable.update(
                        where = { ProductBrandsTable.uid eq brand.uid }
                    ) {
                        it[name] = brand.name
                        it[description] = brand.description
                        it[imageFileName] = brand.imageFileName
                        it[isActive] = brand.isActive
                        it[updatedAt] = brand.updatedAt
                        it[deletedAt] = brand.deletedAt
                    }
                }
            }
        }
    }
}