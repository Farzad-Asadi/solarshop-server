package com.example.data

import org.jetbrains.exposed.dao.id.LongIdTable

object SyncDevicesTable : LongIdTable("sync_devices") {

    val deviceId = text("device_id").uniqueIndex()

    val platform = varchar(
        name = "platform",
        length = 50
    )

    val appVersion = integer("app_version")

    val createdAt = long("created_at")

    val lastSeenAt = long("last_seen_at")
}