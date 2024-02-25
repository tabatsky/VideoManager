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
    val onDismiss = { Injector.viewModel.isVideoContentsDialogVisible = false }
    if (Injector.viewModel.isVideoContentsDialogVisible) {
        DialogWindow(onCloseRequest = onDismiss) {
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
                val lastModified = Injector.viewModel.currentVideo?.lastModifiedFormatted ?: "неизвестно"
                Text(
                    text = "Изменено: $lastModified",
                    modifier = Modifier
                        .padding(12.dp)
                )
                val duration = Injector.viewModel.currentVideo
                    ?.duration?.formatDuration(true) ?: "неизвестно"
                Text(
                    text = "Длительность: $duration",
                    modifier = Modifier
                        .padding(12.dp)
                )
                Button(
                    onClick = {
                        Injector.viewModel.updateCurrentVideoEntry()
                        onDismiss()
                    }, modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("Сохранить",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}