package jatx.video.manager

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import jatx.video.manager.db.AppDatabase
import java.io.File

const val dbFile = "VideoManager.db"

class DatabaseDriverFactory {
    fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbFile")
        if (!File(dbFile).exists()) {
            AppDatabase.Schema.create(driver)
        }
        return driver
    }
}