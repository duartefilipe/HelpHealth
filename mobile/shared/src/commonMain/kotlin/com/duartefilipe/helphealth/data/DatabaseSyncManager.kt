package com.duartefilipe.helphealth.data

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun checkServerOnline(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(getVersionUrl())
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 2000
            connection.readTimeout = 2000
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            responseCode in 200..299
        } catch (e: Exception) {
            false
        }
    }
}
