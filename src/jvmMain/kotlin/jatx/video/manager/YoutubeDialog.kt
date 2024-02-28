package jatx.video.manager

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

@Composable
fun YoutubeDialog() {
    val onDismiss = { Injector.viewModel.isYoutubeDialogVisible = false }

    if (Injector.viewModel.isYoutubeDialogVisible) {
        val dialogState = rememberDialogState(width = 800.dp, height = 600.dp)

        DialogWindow(
            onCloseRequest = onDismiss,
            state = dialogState
        ) {
            LazyColumn {
                items(Injector.viewModel.youtubeVideos.mapIndexed { index, pair -> index to pair  }) {
                    val index = it.first
                    val item = it.second
                    val youtubeId = item.first.id
                    val youtubeTitle = item.first.title
                    val youtubeFileName = item.first.fileName
                    val videoName = item.second
                    Row {
                        Text(
                            text = "${index + 1}.",
                            modifier = Modifier
                                .weight(0.1f)
                        )
                        Text(
                            text = youtubeTitle,
                            modifier = Modifier
                                .weight(1f)
                        )
                        Text(
                            text = videoName,
                            modifier = Modifier
                                .weight(1f)
                        )
                        Button(
                            onClick = {
                                Injector.viewModel.updateYoutubeTitle(youtubeId, videoName)
                            },
                            enabled = videoName.length <= 100 &&
                                    videoName != youtubeTitle &&
                                    videoName != youtubeFileName
                        ) {
                            Text(text = "Обновить")
                        }
                    }
                }
            }
        }
    }
}