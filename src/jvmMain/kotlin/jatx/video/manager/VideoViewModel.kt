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
    var allVideoRenamings by mutableStateOf(mapOf<String, String>())
    var allVideoReverseRenamings by mutableStateOf(mapOf<String, String>())

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
    var youtubeVideos: List<Pair<YoutubeVideo?, String>> by mutableStateOf(listOf())
    var youtubePlaylistNames: List<String> by mutableStateOf(listOf())
    var youtubeSelectedPlaylistName by mutableStateOf("")

    var filterText by mutableStateOf("")

    var isPlaylistRightClickDialogVisible by mutableStateOf(false)
    var isRenamePlaylistDialogVisible by mutableStateOf(false)
    var isExportPlaylistDialogVisible by mutableStateOf(false)
    var newPlaylistName by mutableStateOf("")
    var rightClickPlaylistName by mutableStateOf("")

    var folderChooserMode by mutableStateOf(FolderChooserMode.addFolder)

    var exportButtonEnabled by mutableStateOf(true)
    var exportProgress by mutableStateOf(0f)

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
                youtubeSelectedPlaylistName = settings.lastYoutubePlaylist
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

    private fun fetchYoutubeData(): List<Pair<YoutubeVideo?, String>> {
        val result1 = arrayListOf<Pair<YoutubeVideo, String>>()
        val result2 = arrayListOf<Pair<YoutubeVideo, String>>()
        val result3 = arrayListOf<Pair<YoutubeVideo, String>>()

        val youtubeData = YoutubeAPI.fetchPlaylistVideos(youtubeSelectedPlaylistName)
        val videoEntryNames = videoRepository.getAllVideos().associate { it.videoName to it.file.name }
        youtubeData.forEach {
            val youtubeTitle = it.title
            if (youtubeTitle in videoEntryNames.keys) {
                result1.add(it to youtubeTitle)
            } else if (youtubeTitle in allVideoReverseRenamings.keys) {
                val newName = allVideoReverseRenamings[youtubeTitle]!!
                result2.add(it to newName)
            } else {
                result3.add(it to "null")
            }
        }

        val result = result1 + result2 + result3
        val result4 = arrayListOf<Pair<YoutubeVideo?, String>>()
        videoRepository.getAllVideos()
            .filter { it.playlistName == youtubeSelectedPlaylistName }
            .forEach { videoEntry ->
                val oldVideoName = allVideoRenamings[videoEntry.videoName]
                if (result.count { it.first.title == videoEntry.videoName || it.first.title == oldVideoName } == 0) {
                    result4.add(null to videoEntry.videoName)
                }
            }

        return result4 + result
    }

    private fun makeThumbnails() {
        allVideos.sortedBy { it.id }.forEach {
            ThumbnailMaker.makeThumbnail(it) { videoEntry, pngFile ->
                if (!thumbnails.containsKey(videoEntry.id)) {
                    val bitmap = loadImageBitmap(pngFile.inputStream())
                    thumbnails[videoEntry.id] = bitmap
                }
                //println("thumbnail loaded: ${pngFile.name}")
            }
        }
        println("all thumbnails loaded")
    }

    fun onDbUpgraded(newVersion: Int, onSuccess: () -> Unit) {
        if (newVersion < 6) {
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    val allVideosFromDB = videoRepository.getAllVideos()
                    var index = 0
                    allVideosFromDB.sortedBy { it.file.absolutePath }.forEach { videoEntry ->
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
        } else if (newVersion == 6) {
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    val allVideosFromDB = videoRepository.getAllVideos()
                    allVideosFromDB.forEach { videoEntry ->
                        val oldName = videoEntry.file.name
                            .split(".")
                            .first()
                            .replace("_", " ")
                            .replace("-", " ")
                        videoRepository.addVideoRenaming(oldName, videoEntry.videoName)
                    }
                    videoRepository.getAllVideoRenamings().forEach { s, s2 ->
                        println("$s $s2")
                    }
                    Injector.confirmDbUpgrade()
                    onSuccess()
                }
            }
        }
    }

    fun chooseFolder(dir: File) {
        settings.lastDirPath = dir.absolutePath
        folderPath = dir.absolutePath
        when (folderChooserMode) {
            FolderChooserMode.addFolder -> {
                playlistName = dir.name
                folderContents = scanVideoDir(dir)
                folderVideoCount = folderContents.size
            }
            FolderChooserMode.exportPlaylist -> Unit
        }
    }

    fun addCurrentFolderContents() {
        coroutineScope.launch {
            withContext(Dispatchers.Main) {
                val allPaths = videoRepository.getAllVideos().map { it.file.absolutePath }.toSet()
                val allCrc32 = videoRepository.getAllVideos().associateBy { it.crc32 }
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
                withContext(Dispatchers.IO) {
                    makeThumbnails()
                }
            }
        }
    }

    private fun updateAllVideos() {
        allVideos = videoRepository.getAllVideos()
        allVideoRenamings = videoRepository.getAllVideoRenamings()
        allVideoReverseRenamings = videoRepository.getAllVideoReverseRenamings()
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
            val oldName = if (allVideoRenamings.containsKey(it.videoName)) {
                it.videoName
            } else {
                it.file.name
                    .split(".")
                    .first()
                    .replace("_", " ")
                    .replace("-", " ")
            }
            videoRepository.addVideoRenaming(oldName, newVideoName)
            updateAllVideos()
            currentVideo = newVideoEntry
        }
    }

    fun toggleCurrentVideoDeleted() {
        currentVideo?.let {
            val newVideoEntry = it.copy(
                deleted = !it.deleted
            )
            videoRepository.updateVideoDeleted(newVideoEntry)
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

    fun showPlaylistRightClickDialog(playlistName: String) {
        rightClickPlaylistName = playlistName
        isPlaylistRightClickDialogVisible = true
    }

    fun showRenamePlaylistDialog() {
        newPlaylistName = rightClickPlaylistName
        isRenamePlaylistDialogVisible = true
    }

    fun showExportPlaylistDialog() {
        isExportPlaylistDialogVisible = true
    }

    fun applyNewPlaylistName() {
        videoRepository.renamePlaylist(rightClickPlaylistName, newPlaylistName)
        updateAllVideos()
    }

    fun exportPlaylistToFolder() {
        val playlistVideos = allVideos.filter {
            it.playlistName == rightClickPlaylistName && !it.deleted
        }
        val count = playlistVideos.size
        val outDir = File(folderPath)

        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                exportButtonEnabled = false
                exportProgress = 0f

                var done = 0

                playlistVideos.forEach {
                    val fileName = it.file.name
                    val outFile = File(outDir, fileName)
                    it.file.copyTo(outFile, overwrite = true)
                    done += 1
                    exportProgress = 1f * done / count
                }

                exportButtonEnabled = true
            }
        }
    }
}
