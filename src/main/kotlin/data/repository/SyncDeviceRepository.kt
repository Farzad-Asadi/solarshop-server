package com.example.data.repository




import com.example.data.SyncDevicesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class SyncDeviceRepository {

    fun registerOrUpdate(
        deviceId: String,
        platform: String,
        appVersion: Int
    ) {
        val now = System.currentTimeMillis()

        transaction {
            val exists = SyncDevicesTable
                .select { SyncDevicesTable.deviceId eq deviceId }
                .singleOrNull()

            if (exists == null) {
                SyncDevicesTable.insert {
                    it[SyncDevicesTable.deviceId] = deviceId
                    it[SyncDevicesTable.platform] = platform
                    it[SyncDevicesTable.appVersion] = appVersion
                    it[createdAt] = now
                    it[lastSeenAt] = now
                }
            } else {
                SyncDevicesTable.update(
                    where = { SyncDevicesTable.deviceId eq deviceId }
                ) {
                    it[SyncDevicesTable.platform] = platform
                    it[SyncDevicesTable.appVersion] = appVersion
                    it[lastSeenAt] = now
                }
            }
        }
    }


}