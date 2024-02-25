package jatx.video.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VideoItem(videoItemEntry: VideoItemEntry) {
    if (Injector.viewModel.expandedPlaylistName == videoItemEntry.videoEntry.playlistName) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(12.dp)
                .clickable {
                    Injector.viewModel.playVideoEntry(videoItemEntry.videoEntry)
                }
        ) {
            Text(
                text = videoItemEntry.videoEntry.videoName,
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