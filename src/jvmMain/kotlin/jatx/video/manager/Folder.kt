package jatx.video.manager

import java.io.File

val videoExtensions = listOf("m2ts", "mp4", "avi", "mov")

val File.isVideo: Boolean
    get() = (this.extension.lowercase() in videoExtensions)

fun scanVideoDir(dir: File): List<File> {
    val result = arrayListOf<File>()

    dir.listFiles().forEach { file ->
        if (file.isDirectory) {
            result.addAll(scanVideoDir(file))
        } else if (file.isVideo) {
            result.add(file)
        }
    }

    return result.sortedBy { it.name }
}