package com.example.data.table

import org.jetbrains.exposed.dao.id.LongIdTable

object CurrencyRatesTable :
    LongIdTable("currency_rates") {

    val uid =
        varchar("uid", 100).uniqueIndex()

    val currencyCode =
        varchar("currency_code", 20)

    val rateToman =
        long("rate_toman")

    val sourceText =
        text("source").default("")

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