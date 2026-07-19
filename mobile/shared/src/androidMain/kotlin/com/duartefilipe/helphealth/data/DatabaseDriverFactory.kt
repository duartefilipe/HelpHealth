package com.duartefilipe.helphealth.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.duartefilipe.helphealth.db.HelpHealthDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(HelpHealthDatabase.Schema, context, "helphealth.db")
    }
}
