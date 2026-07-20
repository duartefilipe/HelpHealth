package com.duartefilipe.helphealth.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class DatabaseSyncManager(
    private val backendBaseUrl: String = "http://192.168.31.183:8090"
) {
    fun getSyncUrl(): String = "$backendBaseUrl/api/v1/database/download"
    fun getVersionUrl(): String = "$backendBaseUrl/api/v1/database/version"
    fun getMedicinesUrl(page: Int, size: Int): String = "$backendBaseUrl/api/v1/database/medicines?page=$page&size=$size"

    suspend fun checkServerOnline(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(getMedicinesUrl(0, 1))
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode in 200..299
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchMedicinesJson(page: Int, size: Int): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(getMedicinesUrl(page, size))
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            if (connection.responseCode == 200) {
                val text = connection.inputStream.bufferedReader().readText()
                connection.disconnect()
                text
            } else {
                connection.disconnect()
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
