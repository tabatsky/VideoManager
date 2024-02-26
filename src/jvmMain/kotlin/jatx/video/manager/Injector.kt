package jatx.video.manager

import jatx.video.manager.db.AppDatabase
import kotlinx.coroutines.CoroutineScope

class Injector(
    databaseDriverFactory: DatabaseDriverFactory,
    coroutineScope: CoroutineScope
) {
    private val driver = databaseDriverFactory.createDriver()
    private val appDatabase = AppDatabase.invoke(driver)
    private val videoRepository = VideoRepository(appDatabase)
    private val viewModel = VideoViewModel(videoRepository, coroutineScope).also {
        if (databaseDriverFactory.wasUpgraded) {
            println("db was upgraded")
            it.onDbUpgraded()
        }
    }

    companion object {
        private lateinit var INSTANCE: Injector

        val viewModel: VideoViewModel
            get() = INSTANCE.viewModel

        fun init(
            databaseDriverFactory: DatabaseDriverFactory,
            coroutineScope: CoroutineScope
        ) {
            INSTANCE = Injector(databaseDriverFactory, coroutineScope)
        }
    }
}