package jatx.video.manager

import java.io.File
import java.io.PrintWriter
import java.util.Scanner

val settingsFile = File("settings.txt")

const val appVersionKey = "APP_VERSION="
const val lastDirPathKey = "LAST_DIR_PATH="
const val lastYoutubePlaylistKey = "LAST_YOUTUBE_PLAYLIST="

class Settings {
    var appVersion = 0
        set(value) {
            field = value
            saveSettings()
        }

    var lastDirPath = ""
        set(value) {
            field = value
            saveSettings()
        }

    var lastYoutubePlaylist = ""
        set(value) {
            field = value
            saveSettings()
        }

    fun loadSettings() {
        if (!settingsFile.exists()) return
        val sc = Scanner(settingsFile)
        while (sc.hasNextLine()) {
            val line = sc.nextLine()
            when {
                line.startsWith(appVersionKey) -> {
                    appVersion = line.replace(appVersionKey, "").toInt()
                }
                line.startsWith(lastDirPathKey) -> {
                    lastDirPath = line.replace(lastDirPathKey, "")
                }
                line.startsWith(lastYoutubePlaylistKey) -> {
                    lastYoutubePlaylist = line.replace(lastYoutubePlaylistKey, "")
                }
            }
        }
        sc.close()
    }

    private fun saveSettings() {
        val pw = PrintWriter(settingsFile)
        pw.println("${appVersionKey}${appVersion}")
        pw.println("${lastDirPathKey}${lastDirPath}")
        pw.println("${lastYoutubePlaylistKey}${lastYoutubePlaylist}")
        pw.flush()
        pw.close()
    }
}