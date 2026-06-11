package com.example.data.database

import com.example.server.dto.EntitlementDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class EntitlementRepository {
    fun getForUser(userId: Int): EntitlementDto? = transaction {
        EntitlementsTable.select { EntitlementsTable.userId eq userId }
            .firstOrNull()
            ?.let {
                EntitlementDto(
                    isActive = it[EntitlementsTable.isActive],
                    plan = it[EntitlementsTable.plan],
                    expiresAt = it[EntitlementsTable.expiresAt]
                )
            }
    }

    fun upsert(userId: Int, ent: EntitlementDto) = transaction {
        val count = EntitlementsTable.update({ EntitlementsTable.userId eq userId }) {
            it[isActive] = ent.isActive
            it[plan] = ent.plan
            it[expiresAt] = ent.expiresAt
        }
        if (count == 0) {
            EntitlementsTable.insert {
                it[EntitlementsTable.userId] = userId
                it[isActive] = ent.isActive
                it[plan] = ent.plan
                it[expiresAt] = ent.expiresAt
            }
        }
    }
}
