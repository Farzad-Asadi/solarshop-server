package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object ProductCategoriesTable : LongIdTable("product_categories") {

    val uid = varchar("uid", 100).uniqueIndex()

    val name = varchar("name", 255)

    val imageFileName = varchar("image_file_name", 255).nullable()

    val sortOrder = integer("sort_order").default(0)

    val updatedAt = long("updated_at")

    val serverUpdatedAt =
        long("server_updated_at").default(0L)

    val deletedAt = long("deleted_at").nullable()
}