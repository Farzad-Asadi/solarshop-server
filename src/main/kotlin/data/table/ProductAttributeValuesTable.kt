package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object ProductAttributeValuesTable :
    LongIdTable("product_attribute_values") {

    val uid =
        varchar("uid", 100).uniqueIndex()

    val productUid =
        varchar("product_uid", 100)

    val attributeDefinitionUid =
        varchar("attribute_definition_uid", 100)

    val valueText =
        text("value_text")

    val updatedAt =
        long("updated_at")

    val deletedAt =
        long("deleted_at").nullable()
}