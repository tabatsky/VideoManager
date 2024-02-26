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
import java.util.*

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

    var seekProgressMs by mutableStateOf(0L)
    var needToSeek by mutableStateOf(false)

    init {
        Native.load("mediainfo", LibMediaInfo::class.java)
    }

    fun onAppStart() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                updateAllVideos()
            }
        }
    }

    fun onDbUpgraded() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val allVideosFromDB = videoRepository.getAllVideos()
                allVideosFromDB.forEachIndexed { index, videoEntry ->
                    val newVideoEntry = videoEntry.file
                        .toVideoEntry(videoEntry.playlistName)
                        .copy(id = videoEntry.id)
                    videoRepository.updateVideoRecordedDate(newVideoEntry)
                    videoRepository.updateVideoCrc32(newVideoEntry)
                    println("updated: ${index + 1} of ${allVideosFromDB.size}")
                    updateAllVideos()
                }
                println("updated: all")
                Injector.confirmDbUpgrade()
            }
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
                val allPaths = videoRepository.getAllVideos().map { it.file.absolutePath }.toSet()
                val allCrc32 = videoRepository.getAllVideos().associate { it.crc32 to it }
                val nonDuplicateFolderContents = folderContents.filter { it.absolutePath !in allPaths }
                nonDuplicateFolderContents.forEachIndexed { index, file ->
                    val videoEntry = file.toVideoEntry(playlistName)
                    if (allCrc32.keys.contains(videoEntry.crc32)) {
                        val existingVideoEntry = allCrc32[videoEntry.crc32]!!
                        val existingPath = existingVideoEntry.file.absolutePath
                        val newPath = file.absolutePath
                        if (existingVideoEntry.file.exists()) {
                            println("duplicate found:\n$existingPath\n$newPath")
                        } else {
                            val newVideoEntry = existingVideoEntry.copy(file = file)
                            videoRepository.updateVideoFilePath(newVideoEntry)
                            println("file replaced:\n$existingPath\n$newPath")
                            updateAllVideos()
                        }
                    } else {
                        videoRepository.insertVideo(videoEntry = videoEntry)
                        println("inserted: ${index + 1} of ${nonDuplicateFolderContents.size}")
                        updateAllVideos()
                    }
                }
                println("inserted: all")
            }
        }
    }

    private fun updateAllVideos() {
        allVideos = videoRepository.getAllVideos()
    }

    fun playVideoEntry(videoEntry: VideoEntry) {
        println(videoEntry)
        println("duration: ${videoEntry.duration}")
        println("actual date: ${videoEntry.actualDate}")
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

    fun updateCurrentVideoEntry(newRecordedDate: Date) {
        currentVideo?.let {
            val newVideoEntry = it.copy(
                videoName = currentVideoName,
                comment = currentVideoComment,
                recorded = newRecordedDate
            )
            videoRepository.updateVideoNameAndComment(newVideoEntry)
            videoRepository.updateVideoRecordedDate(newVideoEntry)
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

    fun seek(progress: Float) {
        seekProgressMs = (currentVideoDuration * progress).toLong()
        needToSeek = true
    }

    fun seekDone() {
        seekProgressMs = 0L
        needToSeek = false
    }
}
