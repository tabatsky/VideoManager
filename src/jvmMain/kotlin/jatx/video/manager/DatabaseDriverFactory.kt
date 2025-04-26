package jatx.video.manager

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import jatx.video.manager.db.AppDatabase
import java.io.File

const val dbFile = "VideoManager.db"

class DatabaseDriverFactory {
    var wasUpgraded = false
    var version = 0

    fun createDriver(): JdbcSqliteDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbFile")
        if (!File(dbFile).exists()) {
            AppDatabase.Schema.create(driver)
        } else {
            val oldVersion = driver
                .executeQuery(
                    sql = "PRAGMA user_version",
                    identifier = null,
                    parameters = 0)
                .getLong(0)
                ?.toInt() ?: 0

            version = AppDatabase.Schema.version
            println("old version: $oldVersion; new version: $version")
            if (version > oldVersion) {
                wasUpgraded = true
            }
            try {
                AppDatabase.Schema.migrate(
                    driver = driver,
                    oldVersion = oldVersion,
                    newVersion = version
                )
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        return driver
    }
}