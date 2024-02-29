package jatx.video.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            Column {
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                ) {
                    Spinner(
                        modifier = Modifier
                            .height(64.dp)
                            .weight(1f),
                        fontSize = 16.sp,
                        valueList = Injector.viewModel.youtubePlaylistNames.toTypedArray(),
                        initialPosition = 0,
                        onPositionChanged = {
                            Injector.viewModel.youtubeSelectedPlaylistName =
                                Injector.viewModel.youtubePlaylistNames[it]
                        }
                    )
                    Button(
                        onClick = {
                            Injector.viewModel.updateYoutubeVideos()
                        },
                        modifier = Modifier
                            .width(300.dp)
                            .height(64.dp)
                    ) {
                        Text(text = "�������� ��������")
                    }
                }
                LazyColumn {
                    items(Injector.viewModel.youtubeVideos.mapIndexed { index, pair -> index to pair  }) {
                        val index = it.first
                        val item = it.second
                        val youtubeId = item.first.id
                        val youtubeTitle = item.first.title
                        val youtubeFileName = item.first.fileName
                        val videoName = item.second

                        val bgColor = if (videoName != youtubeFileName && videoName != youtubeTitle) {
                            Color.Yellow
                        } else if (videoName == youtubeTitle) {
                            Color.Green
                        } else {
                            Color.Gray
                        }.let { color ->
                            val red = color.red
                            val green = color.green
                            val blue = color.blue
                            val alpha = 0.3f
                            Color(red, green, blue, alpha)
                        }

                        Row(
                            modifier = Modifier
                                .background(bgColor)
                        ) {
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
                            val enabled = videoName.length <= 100 &&
                                    videoName != youtubeTitle &&
                                    videoName != youtubeFileName
                            val buttonText = if (videoName.length <= 100) {
                                "��������"
                            } else {
                                "${videoName.length} / 100"
                            }
                            Button(
                                onClick = {
                                    Injector.viewModel.updateYoutubeTitle(youtubeId, videoName)
                                },
                                enabled = enabled
                            ) {
                                Text(text = buttonText)
                            }
                        }
                    }
                }
            }
        }
    }
}