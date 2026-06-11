package com.example.data.database

import com.example.server.dto.UserDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {

    fun getOrCreateByPhone(phone: String): UserDto = transaction {
        val existing = UsersTable
            .select { UsersTable.phone eq phone }
            .limit(1).firstOrNull()

        val row = existing ?: UsersTable.insertAndGetId {
            it[UsersTable.phone] = phone
            it[UsersTable.createdAt] = System.currentTimeMillis()
        }.let { id ->
            UsersTable.select { UsersTable.id eq id }.first()
        }

        row.toUserDto()
    }

    fun touchLastLogin(userId: Int) = transaction {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[lastLoginAt] = System.currentTimeMillis()
        }
    }

    fun getById(userId: Int): UserDto? = transaction {
        UsersTable.select { UsersTable.id eq userId }.firstOrNull()?.toUserDto()
    }

    private fun ResultRow.toUserDto() = UserDto(
        id = this[UsersTable.id].value,
        phone = this[UsersTable.phone],
        createdAt = this[UsersTable.createdAt]
    )
}
