package com.duartefilipe.helphealth.data

data class RemoteDatabaseVersion(
    val fileName: String,
    val sizeBytes: Long,
    val sha256: String,
    val lastModified: Long
)

class DatabaseSyncManager(
    private val backendBaseUrl: String = "http://192.168.31.229:8090"
) {

    fun getSyncUrl(): String {
        return "$backendBaseUrl/api/v1/database/download"
    }

    fun getVersionUrl(): String {
        return "$backendBaseUrl/api/v1/database/version"
    }

    fun getManualSyncUrl(): String {
        return "$backendBaseUrl/api/v1/database/sync-now"
    }
}
