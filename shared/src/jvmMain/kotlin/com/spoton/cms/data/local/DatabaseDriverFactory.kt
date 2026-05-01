package com.spoton.cms.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.spoton.cms.db.SpotOnDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".spoton/spoton.db")
        if (!databasePath.parentFile.exists()) {
            databasePath.parentFile.mkdirs()
        }
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        
        // Check if tables exist by trying to run a simple query, or just use Schema.create
        // For JDBC, we often need to manually create the schema on first run
        try {
            SpotOnDatabase.Schema.create(driver)
        } catch (e: Exception) {
            // Probably already exists
        }
        
        return driver
    }
}
