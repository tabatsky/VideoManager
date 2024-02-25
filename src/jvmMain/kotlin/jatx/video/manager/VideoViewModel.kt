package jatx.video.manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sun.jna.Native
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.caprica.vlcjinfo.binding.LibMediaInfo
import java.io.File

class VideoViewModel(
    private val videoRepository: VideoRepository,
    private val coroutineScope: CoroutineScope
) {
    var playlistName by mutableStateOf("")
    var folderPath by mutableStateOf("")
    var folderContents by mutableStateOf(listOf<File>())
    var folderVideoCount by mutableStateOf(0)

    var needShowFolderChooserDialog by mutableStateOf(false)
    var folderChooserDialogShowCounter by mutableStateOf(0)

    var allVideos by mutableStateOf(listOf<VideoEntry>())

    var currentVideo: VideoEntry? by mutableStateOf(null)
    var currentVideoUrl by mutableStateOf("")
    var currentVideoDuration = 0L
    var currentVideoProgressPercent by mutableStateOf(0f)
    var currentVideoProgressMs by mutableStateOf(0L)

    var isPlaying by mutableStateOf(false)

    var expandedPlaylistName by mutableStateOf("")
    var expandedYear by mutableStateOf(0)

    var isVideoContentsDialogVisible by mutableStateOf(false)
    var currentVideoName by mutableStateOf("")
    var currentVideoComment by mutableStateOf("")

    var isAddFolderDialogVisible by mutableStateOf(false)

    fun onAppStart() {
        Native.loadLibrary("mediainfo", LibMediaInfo::class.java)
        coroutineScope.launch {
            allVideos = videoRepository.getAllVideos()
        }
    }

    fun chooseFolder(dir: File) {
        folderPath = dir.absolutePath
        playlistName = dir.name
        folderContents = scanVideoDir(dir)
        folderVideoCount = folderContents.size
    }

    fun addCurrentFolderContents() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                folderContents.forEachIndexed { index, file ->
                    videoRepository.insertVideo(videoEntry = file.toVideoEntry(playlistName))
                    println("${index + 1} of ${folderContents.size}")
                    updateAllVideos()
                }
            }
        }
    }

    private fun updateAllVideos() {
        allVideos = videoRepository.getAllVideos()
    }

    fun playVideoEntry(videoEntry: VideoEntry) {
        println(videoEntry)
        println("duration: ${videoEntry.duration}")
        currentVideo = videoEntry
        currentVideoDuration = videoEntry.duration
        currentVideoName = videoEntry.videoName
        currentVideoUrl = videoEntry.url
        play()
    }

    fun play() {
        isPlaying = true
    }

    fun pause() {
        isPlaying = false
    }

    fun updateCurrentVideoEntry() {
        currentVideo?.let {
            val newVideoEntry = it.copy(
                videoName = currentVideoName,
                comment = currentVideoComment
            )
            videoRepository.updateVideo(newVideoEntry)
            updateAllVideos()
            currentVideo = newVideoEntry
        }
    }

    fun onVideoPlayerTimeChanged(newTime: Long) {
        currentVideoProgressMs = newTime
        currentVideoProgressPercent = 1f * newTime / currentVideoDuration
    }

    fun onVideoFinished() {
        currentVideoUrl = ""
    }

    fun expandPlaylist(playlistName: String) {
        expandedPlaylistName = if (expandedPlaylistName == playlistName) {
            ""
        } else {
            playlistName
        }
    }

    fun expandYear(year: Int) {
        expandedYear = if (expandedYear == year) {
            0
        } else {
            year
        }
    }

    fun showVideoContentsDialog() {
        currentVideoName = currentVideo?.videoName ?: ""
        currentVideoComment = currentVideo?.comment ?: ""
        isVideoContentsDialogVisible = true
    }
}
