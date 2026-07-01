package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object ProductPurchasePricesTable : LongIdTable("product_purchase_prices") {

    val uid = varchar("uid", 100).uniqueIndex()

    val productUid = varchar("product_uid", 100)

    val buyPriceDollar = double("buy_price_dollar").nullable()

    val buyPriceToman = long("buy_price_toman").nullable()

    val dollarRateToman = long("dollar_rate_toman").nullable()

    val quantity = double("quantity").nullable()

    val purchasedAt = long("purchased_at")

    val note = text("note").default("")

    val isActive = bool("is_active").default(true)

    val createdAt = long("created_at")

    val updatedAt = long("updated_at")

    val serverUpdatedAt =
        long("server_updated_at").default(0L)

    val deletedAt = long("deleted_at").nullable()
}