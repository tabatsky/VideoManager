package jatx.video.manager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow

@Composable
fun PlaylistRightClickDialog() {
    val onDismiss = { Injector.viewModel.isPlaylistRightClickDialogVisible = false }

    if (Injector.viewModel.isPlaylistRightClickDialogVisible) {
        DialogWindow(onCloseRequest = { onDismiss() }) {
            Column {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        onDismiss()
                        Injector.viewModel.showRenamePlaylistDialog()
                    }) {
                    Text("ѕереименовать")
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        onDismiss()
                        Injector.viewModel.showExportPlaylistDialog()
                    }) {
                    Text("Ёкспортировать")
                }
            }
        }
    }
}

@Composable
fun RenamePlaylistDialog() {
    val onDismiss = { Injector.viewModel.isRenamePlaylistDialogVisible = false }

    if (Injector.viewModel.isRenamePlaylistDialogVisible) {
        DialogWindow(onCloseRequest = { onDismiss() }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TextField(
                    value = Injector.viewModel.newPlaylistName,
                    onValueChange = { Injector.viewModel.newPlaylistName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                    onDismiss()
                    Injector.viewModel.applyNewPlaylistName()
                }) {
                    Text("ѕрименить")
                }
            }
        }
    }
}

@Composable
fun ExportPlaylistDialog(
    onChooseFolder: () -> Unit
) {
    val onDismiss = { Injector.viewModel.isExportPlaylistDialogVisible = false }

    if (Injector.viewModel.isExportPlaylistDialogVisible) {
        DialogWindow(onCloseRequest = { onDismiss() }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row {
                    Text(
                        text = Injector.viewModel.folderPath,
                        modifier = Modifier
                            .weight(4f)
                    )
                    Button(
                        onClick = {
                            onChooseFolder()
                        }, modifier = Modifier
                            .weight(1f)
                    ) {
                        Text("...",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    progress = Injector.viewModel.exportProgress
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    enabled = Injector.viewModel.exportButtonEnabled,
                    onClick = {
                        Injector.viewModel.exportPlaylistToFolder()
                    }
                ) {
                    Text("Ёкспортировать")
                }
            }
        }
    }
}