package jatx.video.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.WindowScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser

@Composable
fun WindowScope.FolderChooserDialogWrapper(
    onFolderOpened: (File) -> Unit
) {
    if (Injector.viewModel.needShowFolderChooserDialog) {
        FolderChooserDialog(
            coroutineScope = rememberCoroutineScope(),
            onFolderOpened = {
                Injector.viewModel.needShowFolderChooserDialog = false
                onFolderOpened(it)
            },
            onDialogDispose = {
                Injector.viewModel.needShowFolderChooserDialog = false
            },
            showCounter = Injector.viewModel.folderChooserDialogShowCounter
        )
    }
}

@Composable
private fun WindowScope.FolderChooserDialog(
    coroutineScope: CoroutineScope,
    onFolderOpened: (File) -> Unit,
    onDialogDispose: () -> Unit,
    showCounter: Int
) {
    DisposableEffect(showCounter) {
        val job = coroutineScope.launch {
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = "Открыть папку"
            fileChooser.isMultiSelectionEnabled = false
            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

            val result = fileChooser.showOpenDialog(window)
            when (result) {
                JFileChooser.APPROVE_OPTION -> {
                    onFolderOpened(fileChooser.selectedFile)
                }
                else -> {}
            }
        }

        onDispose {
            job.cancel()
            onDialogDispose()
        }
    }
}
