package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object InventoryTransactionsTable : LongIdTable("inventory_transactions") {

    val uid = varchar("uid", 100).uniqueIndex()

    val productUid = varchar("product_uid", 100)

    val quantity = double("quantity")

    val transactionType = varchar("transaction_type", 50)

    val note = text("note").default("")

    val createdAt = long("created_at")

    val updatedAt = long("updated_at")

    val deletedAt = long("deleted_at").nullable()
}
