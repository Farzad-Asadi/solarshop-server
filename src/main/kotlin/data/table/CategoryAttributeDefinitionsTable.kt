package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object CategoryAttributeDefinitionsTable :
    LongIdTable("category_attribute_definitions") {

    val uid =
        varchar("uid", 100).uniqueIndex()

    val categoryUid =
        varchar("category_uid", 100)

    val title =
        varchar("title", 255)

    val key =
        varchar("key", 255)

    val description =
        text("description").default("")

    val valueType =
        varchar("value_type", 50)

    val unit =
        varchar("unit", 100).nullable()

    val isRequired =
        bool("is_required")

    val sortOrder =
        integer("sort_order")

    val enumOptions =
        text("enum_options").nullable()

    val isActive =
        bool("is_active").default(true)

    val createdAt =
        long("created_at")

    val updatedAt =
        long("updated_at")

    val deletedAt =
        long("deleted_at").nullable()
}