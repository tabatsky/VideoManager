package jatx.video.manager

import jatx.video.manager.db.VideoEntity
import uk.co.caprica.vlcjinfo.MediaInfo
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*

data class VideoEntry(
    val id: Long = 0,
    val file: File,
    val videoName: String,
    val playlistName: String,
    val lastModified: Date,
    val duration: Long,
    val comment: String = ""
) {
    val url = file.toURI()
        .toString()
        .replace("file:/", "file:///")

    val lastModifiedFormatted = lastModified.format()
    val year = lastModified.year()

    override fun toString() = "$url $playlistName $lastModified"
}

fun File.toVideoEntry(playlistName: String): VideoEntry {
    val attr = Files.readAttributes(Paths.get(this.absolutePath), BasicFileAttributes::class.java)
    val lastModifiedDate = Date(attr.lastModifiedTime().toMillis())

    return VideoEntry(
        file = this,
        videoName = this.name,
        playlistName = playlistName,
        lastModified = lastModifiedDate,
        duration = this.duration
    )
}

fun VideoEntity.toVideoEntry() = let {
    VideoEntry(
        id = it.id,
        file = File(it.filePath),
        videoName = it.videoName,
        playlistName = it.playlistName,
        lastModified = Date(it.lastModified),
        duration = duration,
        comment = it.comment
    )
}

fun VideoEntry.toVideoEntity() = let {
    VideoEntity(
        id = it.id,
        filePath = it.file.absolutePath,
        videoName = it.videoName,
        playlistName = it.playlistName,
        lastModified = it.lastModified.time,
        duration = it.duration,
        comment = it.comment
    )
}

val File.duration: Long
    get() {
        val mediaInfo = MediaInfo.mediaInfo(this.absolutePath)
        val video = mediaInfo.first("Video")
        return video.value("Duration").parseDuration()
    }

fun String.parseDuration(): Long {
    val durationStr = this.split(" ")[0]
    val msStr = durationStr.split(".")[1]
    val hmsStrList = durationStr.split(".")[0].split(":")
    val hourStr = hmsStrList[0]
    val minStr = hmsStrList[1]
    val secStr = hmsStrList[2]
    val ms = msStr.toLong()
    val hour = hourStr.toLong()
    val min = minStr.toLong()
    val sec = secStr.toLong()
    var duration = hour
    duration *= 60
    duration += min
    duration *= 60
    duration += sec
    duration *= 1000
    duration += ms
    return duration
}

fun Long.formatDuration(withMs: Boolean = false): String {
    var duration = this
    val ms = duration % 1000
    duration /= 1000
    val sec = duration % 60
    duration /= 60
    val min = duration % 60
    duration /= 60
    val hour = duration
    val msStr = ms.toString().padStart(3, '0')
    val secStr = sec.toString().padStart(2, '0')
    val minStr = min.toString().padStart(2, '0')
    return if (withMs) {
        "$hour:$minStr:$secStr.$msStr"
    } else {
        "$hour:$minStr:$secStr"
    }
}

fun Date.format(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
    return sdf.format(this)
}

fun Date.year(): Int {
    val sdf = SimpleDateFormat("yyyy")
    return sdf.format(this).toInt()
}