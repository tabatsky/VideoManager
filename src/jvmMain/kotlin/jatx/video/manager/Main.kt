package jatx.video.manager

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.*

fun main() = application {
    val factory = DatabaseDriverFactory()
    Injector.init(factory, rememberCoroutineScope())

    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)

    Window(
        onCloseRequest = {
            Injector.clean()
            exitApplication()
        },
        state = windowState
    ) {
        FolderChooserDialogWrapper(
            onFolderOpened = { dir ->
                Injector.viewModel.chooseFolder(dir)
            }
        )
        MaterialTheme {
            MainScreen(
                onChooseFolder = {
                    Injector.viewModel.needShowFolderChooserDialog = true
                    Injector.viewModel.folderChooserDialogShowCounter += 1
                }
            )
        }
    }
}


