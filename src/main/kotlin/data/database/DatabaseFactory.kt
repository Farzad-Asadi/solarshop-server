package com.example.data.database

import com.example.data.SyncDevicesTable
import com.example.data.table.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(jdbcUrl: String, driver: String, user: String? = null, pass: String? = null) {
        val cfg = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.driverClassName = driver
            this.username = user
            this.password = pass
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val ds = HikariDataSource(cfg)
        Database.connect(ds)

        transaction {
            SchemaUtils.create(UsersTable,
                EntitlementsTable,
                RefreshTokensTable,
                SyncDevicesTable,
                ProductCategoriesTable,
                ProductBrandsTable,
                ProductsTable,
                ProductImagesTable,
                InventoryTransactionsTable,
                ProductPurchasePricesTable,
                ProductSalePricesTable,




            )
        }
    }
}
