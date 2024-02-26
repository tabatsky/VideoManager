package jatx.video.manager

import java.io.File
import java.io.PrintWriter
import java.util.Scanner

val settingsFile = File("settings.txt")

const val appVersionKey = "APP_VERSION="
const val lastDirPathKey = "LAST_DIR_PATH="

class Settings {
    var appVersion = 0
    var lastDirPath = ""

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
            }
        }
        sc.close()
    }

    fun saveSettings() {
        val pw = PrintWriter(settingsFile)
        pw.println("${appVersionKey}${appVersion}")
        pw.println("${lastDirPathKey}${lastDirPath}")
        pw.flush()
        pw.close()
    }
}