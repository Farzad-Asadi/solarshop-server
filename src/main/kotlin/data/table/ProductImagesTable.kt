package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object ProductImagesTable : LongIdTable("product_images") {

    val uid = varchar("uid", 100).uniqueIndex()

    val productUid = varchar("product_uid", 100)

    val fileName = varchar("file_name", 255)

    val sortOrder = integer("sort_order").default(0)

    val updatedAt = long("updated_at")

    val serverUpdatedAt =
        long("server_updated_at").default(0L)

    val deletedAt = long("deleted_at").nullable()
}