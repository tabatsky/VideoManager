package jatx.video.manager

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import jatx.videomanager.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MainScreen(
    onChooseFolder: () -> Unit
) {
    BoxWithConstraints (
        modifier = Modifier
            .fillMaxSize()
    ) {
        val W = maxWidth

        AddFolderDialog(
            onChooseFolder = onChooseFolder
        )

        VideoContentsDialog()

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            val boxWidthDp = W * 0.75f
            val boxHeightDp = boxWidthDp * 9f / 16f

            Column(
                modifier = Modifier
                    .width(boxWidthDp)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(boxHeightDp)
                        .background(Color.Gray)
                ) {
                    if (Injector.viewModel.currentVideoUrl.isNotEmpty()) {
                        VideoPlayerImpl(
                            url = Injector.viewModel.currentVideoUrl,
                            modifier = Modifier
                                .fillMaxSize(),
                            isPlaying = Injector.viewModel.isPlaying,
                            onTimeChanged = Injector.viewModel::onVideoPlayerTimeChanged,
                            onFinished = Injector.viewModel::onVideoFinished
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = Injector.viewModel.currentVideoProgress,
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                        .background(Color.Gray),
                    color = Color.Black
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                    ) {
                        if (!Injector.viewModel.isPlaying) {
                            IconButton(
                                onClick = {
                                    Injector.viewModel.play()
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.ic_play),
                                    contentDescription = null
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    Injector.viewModel.pause()
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.ic_pause),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    TextField(
                        value = Injector.viewModel.currentVideo?.videoName ?: "",
                        onValueChange = {  },
                        modifier = Modifier
                            .weight(1f)
                    )
                    Button(onClick = {
                        Injector.viewModel.showVideoContentsDialog()
                    }, modifier = Modifier
                        .width(160.dp)
                        .padding(20.dp)
                    ) {
                        Text("���������")
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(10.dp)
            ) {
                LazyColumn (
                    modifier = Modifier
                        .weight(1f)
                ) {
                    items(Injector.viewModel.allVideos.toItemEntries()) { itemEntry ->
                        if (itemEntry is VideoItemEntry) {
                            VideoItem(itemEntry)
                        } else if (itemEntry is PlaylistItemEntry) {
                            PlaylistItem(itemEntry)
                        }
                    }
                }
                Button(onClick = {
                    Injector.viewModel.isAddFolderDialogVisible = true
                }, modifier = Modifier
                    .fillMaxWidth()
                ) {
                    Text("�������� �����")
                }
            }
        }
    }
}

