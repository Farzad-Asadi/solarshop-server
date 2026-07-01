package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object ProductSaleTransactionsTable :
    LongIdTable("product_sale_transactions") {

    val uid =
        varchar("uid", 100).uniqueIndex()

    val productUid =
        varchar("product_uid", 100)

    val inventoryTransactionUid =
        varchar("inventory_transaction_uid", 100).nullable()

    val quantity =
        double("quantity")

    // consumer / colleague / manual
    val priceType =
        varchar("price_type", 30)

    val unitSalePriceToman =
        long("unit_sale_price_toman")

    val totalSalePriceToman =
        long("total_sale_price_toman")

    val saleDollarRateToman =
        long("sale_dollar_rate_toman").nullable()

    val purchasePriceUid =
        varchar("purchase_price_uid", 100).nullable()

    val salePriceUid =
        varchar("sale_price_uid", 100).nullable()

    val buyPriceDollar =
        double("buy_price_dollar").nullable()

    val buyPriceToman =
        long("buy_price_toman").nullable()

    val purchaseDollarRateToman =
        long("purchase_dollar_rate_toman").nullable()

    val unitSalePriceDollar =
        double("unit_sale_price_dollar").nullable()

    val unitProfitToman =
        long("unit_profit_toman").nullable()

    val totalProfitToman =
        long("total_profit_toman").nullable()

    val unitProfitDollar =
        double("unit_profit_dollar").nullable()

    val totalProfitDollar =
        double("total_profit_dollar").nullable()

    val profitPercentByToman =
        double("profit_percent_by_toman").nullable()

    val profitPercentByDollar =
        double("profit_percent_by_dollar").nullable()

    val soldAt =
        long("sold_at")

    val note =
        text("note").default("")

    val createdAt =
        long("created_at")

    val updatedAt =
        long("updated_at")

    val serverUpdatedAt =
        long("server_updated_at").default(0L)

    val deletedAt =
        long("deleted_at").nullable()
}