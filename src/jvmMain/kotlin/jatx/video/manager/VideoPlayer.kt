package jatx.video.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.awt.Component
import java.util.*

@Composable
fun VideoPlayerImpl(
    url: String,
    modifier: Modifier,
    isPlaying: Boolean,
    seekProgressMs: Long,
    needToSeek: Boolean,
    onSeekDone: () -> Unit,
    onTimeChanged: (Long) -> Unit,
    onFinished: () -> Unit
) {
    val mediaPlayerComponent = remember { initializeMediaPlayerComponent() }
    val mediaPlayer = remember { mediaPlayerComponent.mediaPlayer() }

    val factory = remember { { mediaPlayerComponent } }

    LaunchedEffect(url) { mediaPlayer.media().play(url, "--avcodec-hw=d3d11va") }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            mediaPlayer.controls().play()
        } else {
            mediaPlayer.controls().pause()
        }
    }
    LaunchedEffect(needToSeek) {
        if (needToSeek) {
            mediaPlayer.controls().setTime(seekProgressMs)
            onSeekDone()
        }
    }
    DisposableEffect(Unit) { onDispose(mediaPlayer::release) }
    SwingPanel(
        factory = factory,
        background = Color.Transparent,
        modifier = modifier
    )

    val eventAdapter = object : MediaPlayerEventAdapter() {
        override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {
            super.timeChanged(mediaPlayer, newTime)
            onTimeChanged(newTime)
        }

        override fun finished(mediaPlayer: MediaPlayer?) {
            super.finished(mediaPlayer)
            onFinished()
        }
    }

    mediaPlayer.events().addMediaPlayerEventListener(eventAdapter)
}

private fun initializeMediaPlayerComponent(): Component {
    NativeDiscovery().discover()
    return if (isMacOS()) {
        CallbackMediaPlayerComponent()
    } else {
        EmbeddedMediaPlayerComponent()
    }
}

private fun Component.mediaPlayer() = when (this) {
    is CallbackMediaPlayerComponent -> mediaPlayer()
    is EmbeddedMediaPlayerComponent -> mediaPlayer()
    else -> error("mediaPlayer() can only be called on vlcj player components")
}

private fun isMacOS(): Boolean {
    val os = System
        .getProperty("os.name", "generic")
        .lowercase(Locale.ENGLISH)
    return "mac" in os || "darwin" in os
}