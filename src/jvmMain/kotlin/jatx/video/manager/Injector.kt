package jatx.video.manager

import jatx.video.manager.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import java.lang.IllegalStateException

class Injector(
    databaseDriverFactory: DatabaseDriverFactory,
    coroutineScope: CoroutineScope
) {
    private val driver = databaseDriverFactory.createDriver().also {
        val readOnly = it.getConnection().isReadOnly
        println("read only: $readOnly")
    }
    private val appDatabase = AppDatabase.invoke(driver)
    private val videoRepository = VideoRepository(appDatabase)
    private val settings = Settings().also { it.loadSettings() }
    private val viewModel = VideoViewModel(videoRepository, settings, coroutineScope).also {
        if (databaseDriverFactory.wasUpgraded) {
            println("db was upgraded")
            it.onDbUpgraded { it.onAppStart() }
        } else {
            it.onAppStart()
        }
    }

    companion object {
        private var INSTANCE: Injector? = null

        val viewModel: VideoViewModel
            get() = INSTANCE?.viewModel ?: throw IllegalStateException("Injector INSTANCE is null")

        val settings: Settings
            get() = INSTANCE?.settings ?: throw IllegalStateException("Injector INSTANCE is null")

        fun confirmDbUpgrade() {
            val newVersion = AppDatabase.Schema.version

            INSTANCE?.driver
                ?.execute(
                    sql = "PRAGMA user_version = $newVersion",
                    identifier = null,
                    parameters = 0)
        }

        fun init(
            databaseDriverFactory: DatabaseDriverFactory,
            coroutineScope: CoroutineScope
        ) {
            INSTANCE = Injector(databaseDriverFactory, coroutineScope)
        }

        fun clean() {
            println(INSTANCE?.driver?.getConnection())
            INSTANCE?.driver?.getConnection()?.close()
            INSTANCE = null
            println("clean done")
        }
    }
}