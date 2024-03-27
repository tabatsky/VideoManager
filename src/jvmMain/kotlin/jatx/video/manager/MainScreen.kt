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
import androidx.compose.ui.unit.Dp
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
        YoutubeDialog()
        PlaylistRightClickDialog()
        RenamePlaylistDialog()

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            val boxWidthDp = W * 0.7f
            val boxHeightDp = boxWidthDp * 9f / 16f

            Column(
                modifier = Modifier
                    .width(boxWidthDp)
                    .fillMaxHeight()
            ) {
                VideoBox(boxHeightDp)
                SliderRow()
                ControlsAndInfoRow()
            }

            VideoListColumn()
        }
    }
}

@Composable
private fun VideoBox(boxHeightDp: Dp) {
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
                seekProgressMs = Injector.viewModel.seekProgressMs,
                needToSeek = Injector.viewModel.needToSeek,
                onSeekDone = Injector.viewModel::seekDone,
                onTimeChanged = Injector.viewModel::onVideoPlayerTimeChanged,
                onFinished = Injector.viewModel::onVideoFinished
            )
        }
    }
}

@Composable
private fun SliderRow() {
    Row(
        modifier = Modifier
            .height(32.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = Injector.viewModel.currentVideoProgressMs.formatDuration(),
            modifier = Modifier
                .padding(4.dp)
        )
        Slider(
            value = Injector.viewModel.currentVideoProgressPercent,
            onValueChange = Injector.viewModel::seek,
            modifier = Modifier
                .height(20.dp)
                .weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color.Black,
                inactiveTickColor = Color.Gray
            )
        )
        Text(
            text = Injector.viewModel.currentVideoDuration.formatDuration(),
            modifier = Modifier
                .padding(4.dp)
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ColumnScope.ControlsAndInfoRow() {
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
            .padding(10.dp)
        ) {
            Text("Подробнее")
        }
    }
}

@Composable
private fun RowScope.VideoListColumn() {
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
            items(Injector.viewModel.allVideos.toItemEntries(
                Injector.viewModel.filterText
            )) { itemEntry ->
                when (itemEntry) {
                    is VideoItemEntry -> {
                        VideoItem(itemEntry)
                    }

                    is YearItemEntry -> {
                        YearItem(itemEntry)
                    }

                    is PlaylistItemEntry -> {
                        PlaylistItem(itemEntry)
                    }
                }
            }
        }
        TextField(
            value = Injector.viewModel.filterText,
            onValueChange = {
                Injector.viewModel.filterText = it
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        Row {
            Button(onClick = {
                Injector.viewModel.isAddFolderDialogVisible = true
            }, modifier = Modifier
                .weight(1f)
                .padding(4.dp)
            ) {
                Text("Добавить папку")
            }
            Button(onClick = {
                Injector.viewModel.openYoutubeDialog()
            }, modifier = Modifier
                .weight(1f)
                .padding(4.dp)
            ) {
                Text("Youtube")
            }
        }
    }
}
