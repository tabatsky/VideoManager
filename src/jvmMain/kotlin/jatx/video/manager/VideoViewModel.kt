package jatx.video.manager

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import com.sun.jna.Native
import kotlinx.coroutines.*
import uk.co.caprica.vlcjinfo.binding.LibMediaInfo
import java.io.File
import java.util.*

class VideoViewModel(
    private val videoRepository: VideoRepository,
    private val settings: Settings,
    private val coroutineScope: CoroutineScope
) {
    var playlistName by mutableStateOf("")
    var folderPath by mutableStateOf("")
    private var folderContents by mutableStateOf(listOf<File>())
    var folderVideoCount by mutableStateOf(0)

    var needShowFolderChooserDialog by mutableStateOf(false)
    var folderChooserDialogShowCounter by mutableStateOf(0)

    var allVideos by mutableStateOf(listOf<VideoEntry>())

    var currentVideo: VideoEntry? by mutableStateOf(null)
    var currentVideoUrl by mutableStateOf("")
    val currentVideoDuration by derivedStateOf { currentVideo?.duration ?: 0L }
    var currentVideoProgressPercent by mutableStateOf(0f)
    var currentVideoProgressMs by mutableStateOf(0L)

    var isPlaying by mutableStateOf(false)

    var expandedPlaylistName by mutableStateOf("")
    var expandedYear by mutableStateOf(0)

    var isVideoContentsDialogVisible by mutableStateOf(false)

    var isAddFolderDialogVisible by mutableStateOf(false)

    var seekProgressMs by mutableStateOf(0L)
    var needToSeek by mutableStateOf(false)

    var thumbnails = mutableStateMapOf<Long, ImageBitmap>()

    var isYoutubeDialogVisible by mutableStateOf(false)
    var youtubeVideos: List<Pair<YoutubeVideo, String>> by mutableStateOf(listOf())
    var youtubePlaylistNames: List<String> by mutableStateOf(listOf())
    var youtubeSelectedPlaylistName by mutableStateOf("")

    init {
        Native.load("mediainfo", LibMediaInfo::class.java)
    }

    fun onAppStart() {
        coroutineScope.launch {
            updateAllVideos()
            withContext(Dispatchers.IO) {
                makeThumbnails()
            }
        }
    }

    fun updateYoutubeTitle(youtubeVideoId: String, videoName: String) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                println("updating youtube title: $youtubeVideoId; $videoName")
                YoutubeAPI.updateVideo(youtubeVideoId, videoName)
            }
        }
    }

    fun openYoutubeDialog() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                youtubePlaylistNames = YoutubeAPI.fetchPlaylistNames()
                youtubeSelectedPlaylistName = youtubePlaylistNames.getOrElse(0) { "" }
                isYoutubeDialogVisible = true
            }
        }
    }

    fun updateYoutubeVideos() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                youtubeVideos = fetchYoutubeData()
            }
        }
    }

    private fun fetchYoutubeData(): List<Pair<YoutubeVideo, String>> {
        val result = arrayListOf<Pair<YoutubeVideo, String>>()
        val result2 = arrayListOf<Pair<YoutubeVideo, String>>()
        val result3 = arrayListOf<Pair<YoutubeVideo, String>>()

        val youtubeData = YoutubeAPI.fetchPlaylistVideos(youtubeSelectedPlaylistName)
        val videoEntryNames = videoRepository.getAllVideos().associate { it.file.name to it.videoName }
        youtubeData.forEach {
            val fileName = it.fileName
            val youtubeTitle = it.title
            if (fileName in videoEntryNames.keys) {
                val videoName = videoEntryNames[fileName]!!
                println("video: $youtubeTitle; $videoName")
                if (videoName != fileName && videoName != youtubeTitle) {
                    result.add(it to videoName)
                } else if (videoName == youtubeTitle) {
                    result2.add(it to videoName)
                } else if (videoName == fileName) {
                    result3.add(it to videoName)
                }
            } else {
                println("video not found in local DB: $fileName; $youtubeTitle")
            }
        }

        return result + result2 + result3
    }

    private fun makeThumbnails() {
        allVideos.sortedBy { it.id }.forEach {
            ThumbnailMaker.makeThumbnail(it) { videoEntry, pngFile ->
                val bitmap = loadImageBitmap(pngFile.inputStream())
                thumbnails[videoEntry.id] = bitmap
                //println("thumbnail loaded: ${pngFile.name}")
            }
        }
        println("all thumbnails loaded")
    }

    fun onDbUpgraded(onSuccess: () -> Unit) {
        coroutineScope.launch {
            withContext(Dispatchers.Main) {
                val allVideosFromDB = videoRepository.getAllVideos()
                val allVideosIterator = allVideosFromDB.sortedBy { it.file.absolutePath }.iterator()
                var index = 0
                while (allVideosIterator.hasNext()) {
                    val videoEntry = allVideosIterator.next()
                    val newVideoEntry = withContext(Dispatchers.IO) {
                        videoEntry.file
                            .toVideoEntry(videoEntry.playlistName)
                            .copy(id = videoEntry.id)
                    }
                    if (newVideoEntry.recorded.year() > 2012) {
                        videoRepository.updateVideoRecordedDate(newVideoEntry)
                    }
                    videoRepository.updateVideoCrc32(newVideoEntry)
                    println("updated: ${index + 1} of ${allVideosFromDB.size}")
                    index += 1
                    updateAllVideos()
                }
                println("updated: all")
                Injector.confirmDbUpgrade()
                onSuccess()
            }
        }
    }

    fun chooseFolder(dir: File) {
        settings.lastDirPath = dir.absolutePath
        settings.saveSettings()
        folderPath = dir.absolutePath
        playlistName = dir.name
        folderContents = scanVideoDir(dir)
        folderVideoCount = folderContents.size
    }

    fun addCurrentFolderContents() {
        coroutineScope.launch {
            withContext(Dispatchers.Main) {
                val allPaths = videoRepository.getAllVideos().map { it.file.absolutePath }.toSet()
                val allCrc32 = videoRepository.getAllVideos().associate { it.crc32 to it }
                val nonDuplicateFolderContents = folderContents.filter { it.absolutePath !in allPaths }
                nonDuplicateFolderContents.forEachIndexed { index, file ->
                    val videoEntry = withContext(Dispatchers.IO) {
                        file.toVideoEntry(playlistName)
                    }
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
        currentVideoUrl = videoEntry.url
        play()
    }

    fun play() {
        isPlaying = true
    }

    fun pause() {
        isPlaying = false
    }

    fun updateCurrentVideoEntry(newRecordedDate: Date, newVideoName: String, newComment: String) {
        currentVideo?.let {
            val newVideoEntry = it.copy(
                videoName = newVideoName,
                comment = newComment,
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
