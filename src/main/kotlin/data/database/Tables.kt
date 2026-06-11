package com.example.data.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object UsersTable : IntIdTable("users") {
    val phone = varchar("phone", 20).uniqueIndex()          // E.164
    val createdAt = long("created_at")
    val lastLoginAt = long("last_login_at").nullable()
}

object EntitlementsTable : IntIdTable("entitlements") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val isActive = bool("is_active")
    val plan = varchar("plan", 64).nullable()
    val expiresAt = long("expires_at").nullable()
}

object RefreshTokensTable : IntIdTable("refresh_tokens") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val tokenHash = varchar("token_hash", 256)              // هشِ Refresh
    val issuedAt = long("issued_at")
    val expiresAt = long("expires_at")
    val revoked = bool("revoked").default(false)
    val family = varchar("family", 64).nullable()
}
