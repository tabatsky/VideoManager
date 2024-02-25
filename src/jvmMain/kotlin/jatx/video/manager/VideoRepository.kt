package jatx.video.manager

import jatx.video.manager.db.AppDatabase
import java.io.File
import java.util.*

class VideoRepository(
        private val appDatabase: AppDatabase
) {
    fun getAllVideos() = appDatabase
            .videoEntityQueries
            .selectAll()
            .executeAsList()
            .map {
                it.toVideoEntry()
            }
            .reversed()

    fun insertVideo(videoEntry: VideoEntry) = videoEntry.toVideoEntity().let { videoEntity ->
            appDatabase
                    .videoEntityQueries
                    .insertVideo(
                            filePath = videoEntity.filePath,
                            videoName = videoEntity.videoName,
                            playlistName = videoEntity.playlistName,
                            lastModified = videoEntity.lastModified,
                            duration = videoEntity.duration,
                            comment = videoEntity.comment
                    )
    }

    fun updateVideo(videoEntry: VideoEntry) = videoEntry.toVideoEntity().let { videoEntity ->
            appDatabase
                    .videoEntityQueries
                    .updateVideo(
                            videoName = videoEntity.videoName,
                            comment = videoEntity.comment,
                            id = videoEntity.id
                    )
    }
}