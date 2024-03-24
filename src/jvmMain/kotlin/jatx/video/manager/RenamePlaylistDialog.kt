package jatx.video.manager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogWindow

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
                Button(onClick = {
                    Injector.viewModel.applyNewPlaylistName()
                }) {
                    Text("Применить")
                }
            }
        }
    }
}