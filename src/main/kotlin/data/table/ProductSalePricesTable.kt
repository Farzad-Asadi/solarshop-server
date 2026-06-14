package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object ProductSalePricesTable :
    LongIdTable("product_sale_prices") {

    val uid =
        varchar("uid", 100).uniqueIndex()

    val productUid =
        varchar("product_uid", 100)

    val priceType =
        varchar("price_type", 30)

    val salePriceToman =
        long("sale_price_toman")

    val profitPercent =
        double("profit_percent").nullable()

    val baseDollarPrice =
        double("base_dollar_price").nullable()

    val dollarRateToman =
        long("dollar_rate_toman").nullable()

    val basePurchasePriceToman =
        long("base_purchase_price_toman").nullable()

    val note =
        text("note").default("")

    val isActive =
        bool("is_active").default(true)

    val createdAt =
        long("created_at")

    val updatedAt =
        long("updated_at")

    val deletedAt =
        long("deleted_at").nullable()
}