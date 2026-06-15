package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object ProductUnitsTable :
    LongIdTable("product_units") {

    val uid =
        varchar("uid", 100).uniqueIndex()

    val name =
        varchar("name", 255)

    val symbol =
        varchar("symbol", 100)

    val isActive =
        bool("is_active").default(true)

    val createdAt =
        long("created_at")

    val updatedAt =
        long("updated_at")

    val deletedAt =
        long("deleted_at").nullable()
}