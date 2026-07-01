package com.example.data.database

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager

object ServerSyncMigration {

    private val syncTableNames =
        listOf(
            "product_categories",
            "product_brands",
            "products",
            "product_images",
            "inventory_transactions",
            "product_purchase_prices",
            "product_sale_prices",
            "product_sale_transactions",
            "category_attribute_definitions",
            "product_attribute_values",
            "product_units",
            "currency_rates"
        )

    fun run() {
        val now =
            System.currentTimeMillis()

        syncTableNames.forEach { tableName ->

            execSql(
                """
                ALTER TABLE $tableName
                ADD COLUMN IF NOT EXISTS server_updated_at BIGINT NOT NULL DEFAULT 0
                """.trimIndent()
            )

            execSql(
                """
                UPDATE $tableName
                SET server_updated_at = $now
                WHERE server_updated_at = 0
                """.trimIndent()
            )

            execSql(
                """
                CREATE INDEX IF NOT EXISTS idx_${tableName}_server_updated_at
                ON $tableName(server_updated_at)
                """.trimIndent()
            )
        }
    }

    private fun execSql(
        sql: String
    ) {
        TransactionManager.current()
            .exec(sql)
    }
}