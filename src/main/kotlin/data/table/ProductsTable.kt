package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object ProductsTable : LongIdTable("products") {

    val uid = varchar("uid", 100).uniqueIndex()

    val categoryUid = varchar("category_uid", 100).nullable()

    val brandUid = varchar("brand_uid", 100).nullable()

    val name = varchar("name", 255)

    val model = varchar("model", 255).default("")

    val description = text("description").default("")

    val isArchived = bool("is_archived").default(false)

    val updatedAt = long("updated_at")

    val deletedAt = long("deleted_at").nullable()
}