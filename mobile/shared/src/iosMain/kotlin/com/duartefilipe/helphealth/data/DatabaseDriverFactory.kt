package com.duartefilipe.helphealth.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.duartefilipe.helphealth.db.HelpHealthDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(HelpHealthDatabase.Schema, "helphealth.db")
    }
}
