package jatx.video.manager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VideoItem(videoItemEntry: VideoItemEntry) {
    val playlistMatches =
        (Injector.viewModel.expandedPlaylistName == videoItemEntry.videoEntry.playlistName &&
                !videoItemEntry.videoEntry.deleted) ||
                (Injector.viewModel.expandedPlaylistName == "Корзина" &&
                        videoItemEntry.videoEntry.deleted)

    if (playlistMatches &&
        Injector.viewModel.expandedYear == videoItemEntry.videoEntry.year) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
                .clickable {
                    Injector.viewModel.playVideoEntry(videoItemEntry.videoEntry)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .width(64.dp)
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val thumbnailPngFile = Injector.viewModel
                        .thumbnails[videoItemEntry.videoEntry.id]
                    thumbnailPngFile?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            modifier = Modifier
                                .width(64.dp)
                                .height(36.dp)
                        )
                    }
                    Text(
                        text = videoItemEntry.videoEntry.duration.formatDuration(),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = videoItemEntry.videoEntry.videoName,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun YearItem(yearItemEntry: YearItemEntry) {
    if (Injector.viewModel.expandedPlaylistName == yearItemEntry.playlistName) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
                .padding(4.dp)
                .background(Color.LightGray)
                .padding(20.dp)
                .clickable {
                    Injector.viewModel.expandYear(yearItemEntry.year)
                }
        ) {
            Text(
                text = yearItemEntry.year.toString(),
                modifier = Modifier
                    .align(Alignment.CenterStart)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistItem(playlistItemEntry: PlaylistItemEntry) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)
            .padding(4.dp)
            .background(Color.Gray)
            .padding(20.dp)
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Primary),
                onClick = {
                    Injector.viewModel.expandPlaylist(playlistItemEntry.playlistName)
                }
            )
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Secondary),
                onClick = {
                    Injector.viewModel.showRenamePlaylistDialog(playlistItemEntry.playlistName)
                }
            )
    ) {
        Text(
            text = playlistItemEntry.playlistName,
            modifier = Modifier
                .align(Alignment.CenterStart)
        )
    }
}
