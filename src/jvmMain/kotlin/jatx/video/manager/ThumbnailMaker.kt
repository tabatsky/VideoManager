package jatx.video.manager

import java.io.File
import java.util.concurrent.TimeUnit

const val ffmpegExePath = "ffmpeg.exe"

object ThumbnailMaker {
    fun makeThumbnail(videoEntry: VideoEntry, onSuccess: (VideoEntry, File) -> Unit) {
        val dir = File("thumbnails")
        dir.mkdirs()
        val pngFile = File(dir, "${videoEntry.id}.png")
        if (!pngFile.exists()) {
            val cmdArray = arrayOf(
                ffmpegExePath,
                "-i",
                videoEntry.file.absolutePath,
                "-frames:v",
                "1",
                pngFile.absolutePath
            )
            val process = Runtime.getRuntime().exec(cmdArray)
            try {
                if (process.waitFor(10, TimeUnit.SECONDS)) {
                    println("thumbnail created: ${pngFile.name}")
                    onSuccess(videoEntry, pngFile)
                } else {
                    println("ffmpeg not finished in 10 seconds. aborting")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //delay(Duration.ofMillis(5000L))
        } else {
            onSuccess(videoEntry, pngFile)
        }
    }
}