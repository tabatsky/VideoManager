package jatx.video.manager

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow

@Composable
fun VideoContentsDialog() {
    if (Injector.viewModel.isVideoContentsDialogVisible) {
        DialogWindow(onCloseRequest = { Injector.viewModel.isVideoContentsDialogVisible = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TextField(
                    value = Injector.viewModel.currentVideoName,
                    onValueChange = { Injector.viewModel.currentVideoName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                TextField(
                    value = Injector.viewModel.currentVideoComment,
                    onValueChange = { Injector.viewModel.currentVideoComment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Button(
                    onClick = {
                        Injector.viewModel.updateCurrentVideoEntry()
                    }, modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("���������",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}