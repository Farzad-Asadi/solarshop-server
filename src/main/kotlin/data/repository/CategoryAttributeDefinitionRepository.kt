package com.example.data.repository

import com.example.data.table.CategoryAttributeDefinitionsTable
import com.example.server.dto.CategoryAttributeDefinitionSyncDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class CategoryAttributeDefinitionRepository {

    fun getAll(): List<CategoryAttributeDefinitionSyncDto> = transaction {
        CategoryAttributeDefinitionsTable
            .selectAll()
            .map { it.toDto() }
    }

    fun getChangedSince(since: Long): List<CategoryAttributeDefinitionSyncDto> = transaction {
        CategoryAttributeDefinitionsTable
            .select {
                CategoryAttributeDefinitionsTable.updatedAt greater since
            }
            .map { it.toDto() }
    }

    fun upsertAll(items: List<CategoryAttributeDefinitionSyncDto>) = transaction {
        items.forEach { item ->

            val existing = CategoryAttributeDefinitionsTable
                .select {
                    CategoryAttributeDefinitionsTable.uid eq item.uid
                }
                .singleOrNull()

            if (existing == null) {
                CategoryAttributeDefinitionsTable.insert {
                    it[uid] = item.uid
                    it[categoryUid] = item.categoryUid
                    it[title] = item.title
                    it[key] = item.key
                    it[description] = item.description
                    it[valueType] = item.valueType
                    it[unit] = item.unit
                    it[isRequired] = item.isRequired
                    it[sortOrder] = item.sortOrder
                    it[enumOptions] = item.enumOptions
                    it[isActive] = item.isActive
                    it[createdAt] = item.createdAt
                    it[updatedAt] = item.updatedAt
                    it[deletedAt] = item.deletedAt
                }
            } else {
                val currentUpdatedAt =
                    existing[CategoryAttributeDefinitionsTable.updatedAt]

                val currentDeletedAt =
                    existing[CategoryAttributeDefinitionsTable.deletedAt]

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

                CategoryAttributeDefinitionsTable.update({
                    CategoryAttributeDefinitionsTable.uid eq item.uid
                }) {
                    it[categoryUid] = item.categoryUid
                    it[title] = item.title
                    it[key] = item.key
                    it[description] = item.description
                    it[valueType] = item.valueType
                    it[unit] = item.unit
                    it[isRequired] = item.isRequired
                    it[sortOrder] = item.sortOrder
                    it[enumOptions] = item.enumOptions
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

    private fun ResultRow.toDto(): CategoryAttributeDefinitionSyncDto {
        return CategoryAttributeDefinitionSyncDto(
            uid = this[CategoryAttributeDefinitionsTable.uid],
            categoryUid = this[CategoryAttributeDefinitionsTable.categoryUid],
            title = this[CategoryAttributeDefinitionsTable.title],
            key = this[CategoryAttributeDefinitionsTable.key],
            description = this[CategoryAttributeDefinitionsTable.description],
            valueType = this[CategoryAttributeDefinitionsTable.valueType],
            unit = this[CategoryAttributeDefinitionsTable.unit],
            isRequired = this[CategoryAttributeDefinitionsTable.isRequired],
            sortOrder = this[CategoryAttributeDefinitionsTable.sortOrder],
            enumOptions = this[CategoryAttributeDefinitionsTable.enumOptions],
            isActive = this[CategoryAttributeDefinitionsTable.isActive],
            createdAt = this[CategoryAttributeDefinitionsTable.createdAt],
            updatedAt = this[CategoryAttributeDefinitionsTable.updatedAt],
            deletedAt = this[CategoryAttributeDefinitionsTable.deletedAt]
        )
    }
}