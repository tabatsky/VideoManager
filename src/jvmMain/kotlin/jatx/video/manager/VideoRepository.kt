package jatx.video.manager

import jatx.video.manager.db.AppDatabase

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
                            comment = videoEntity.comment,
                            recorded = videoEntity.recorded
                    )
    }

    fun updateVideoNameAndComment(videoEntry: VideoEntry) = videoEntry.toVideoEntity().let { videoEntity ->
            appDatabase
                    .videoEntityQueries
                    .updateVideoNameAndComment(
                            videoName = videoEntity.videoName,
                            comment = videoEntity.comment,
                            id = videoEntity.id
                    )
    }

    fun updateVideoRecordedDate(videoEntry: VideoEntry) = videoEntry.toVideoEntity().let { videoEntity ->
        appDatabase
            .videoEntityQueries
            .updateVideoRecordedDate(
                recorded = videoEntity.recorded,
                id = videoEntity.id
            )
    }
}