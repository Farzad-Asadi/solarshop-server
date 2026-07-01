package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object ProductBrandsTable : LongIdTable("product_brands") {

    val uid = varchar("uid", 100).uniqueIndex()

    val name = varchar("name", 255)

    val description = text("description").default("")

    val imageFileName = varchar("image_file_name", 255).nullable()

    val isActive = bool("is_active").default(true)

    val updatedAt = long("updated_at")

    val serverUpdatedAt =
        long("server_updated_at").default(0L)

    val deletedAt = long("deleted_at").nullable()
}