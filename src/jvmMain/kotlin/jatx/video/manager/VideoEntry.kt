package jatx.video.manager

import jatx.video.manager.db.VideoEntity
import uk.co.caprica.vlcjinfo.MediaInfo
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.CRC32

data class VideoEntry(
    val id: Long = 0,
    val file: File,
    val videoName: String,
    val playlistName: String,
    val lastModified: Date,
    val duration: Long,
    var recorded: Date,
    val crc32: Long,
    val comment: String = ""
) {
    val url = file.toURI()
        .toString()
        .replace("file:/", "file:///")

    val lastModifiedFormatted = lastModified.format()
    val recordedFormatted = recorded.format()

    val actualDate = recorded.takeIf { it.time != 0L } ?: lastModified
    val year = actualDate.year()

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
        duration = this.duration,
        recorded = this.recordedDate,
        crc32 = this.calculateCRC32()
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
        recorded = Date(it.recorded),
        crc32 = it.crc32,
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
        recorded = it.recorded.time,
        crc32 = it.crc32,
        comment = it.comment
    )
}

val File.duration: Long
    get() {
        val mediaInfo = MediaInfo.mediaInfo(this.absolutePath)
        val video = mediaInfo.first("Video")
        return video.value("Duration").parseDuration()
    }

val File.recordedDate: Date
    get() {
        val mediaInfo = MediaInfo.mediaInfo(this.absolutePath)
        val general = mediaInfo.first("General")
        val recordedDateStr = general.value("Recorded date") ?: return Date(0L)
        val dateStr = recordedDateStr.substring(0, 10)
        val timeStr = recordedDateStr.substring(12, 20)
        val formattedDateStr = "$dateStr $timeStr"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.parse(formattedDateStr)
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

fun String.tryParseDate(): Date {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
    return sdf.parse(this)
}

fun Date.year(): Int {
    val sdf = SimpleDateFormat("yyyy")
    return sdf.format(this).toInt()
}

fun File.calculateCRC32(): Long {
    val crc32Calculator = CRC32()
    crc32Calculator.update(this.readBytes())
    return crc32Calculator.value
}