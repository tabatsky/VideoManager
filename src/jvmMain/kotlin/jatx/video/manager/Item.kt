package jatx.video.manager

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VideoItem(videoItemEntry: VideoItemEntry) {
    if (Injector.viewModel.expandedPlaylistName == videoItemEntry.videoEntry.playlistName &&
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
            .clickable {
                Injector.viewModel.expandPlaylist(playlistItemEntry.playlistName)
            }
    ) {
        Text(
            text = playlistItemEntry.playlistName,
            modifier = Modifier
                .align(Alignment.CenterStart)
        )
    }
}
