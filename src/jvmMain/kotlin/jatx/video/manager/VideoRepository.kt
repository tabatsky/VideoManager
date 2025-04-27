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

    fun updateVideoCrc32(videoEntry: VideoEntry) = videoEntry.toVideoEntity().let { videoEntity ->
        appDatabase
            .videoEntityQueries
            .updateVideoCrc32(
                crc32 = videoEntity.crc32,
                id = videoEntity.id
            )
    }

    fun updateVideoFilePath(videoEntry: VideoEntry) = videoEntry.toVideoEntity().let { videoEntity ->
        appDatabase
            .videoEntityQueries
            .updateVideoFilePath(
                filePath = videoEntity.filePath,
                id = videoEntity.id
            )
    }

    fun updateVideoDeleted(videoEntry: VideoEntry) = videoEntry.toVideoEntity().let { videoEntity ->
        appDatabase
            .videoEntityQueries
            .updateVideoDeleted(
                deleted = videoEntity.deleted,
                id = videoEntity.id
            )
    }

    fun renamePlaylist(oldName: String, newName: String) = appDatabase
        .videoEntityQueries
        .renamePlaylist(newName, oldName)


    fun getAllVideoRenamings() = appDatabase
        .videoRenamingsQueries
        .selectAll()
        .executeAsList()
        .groupBy { it.newName }
        .map { it.key to it.value.map { it.oldName } }
        .toMap()

    fun getAllVideoReverseRenamings() = appDatabase
        .videoRenamingsQueries
        .selectAll()
        .executeAsList()
        .groupBy { it.oldName }
        .map { it.key to it.value.map { it.newName } }
        .toMap()

    fun addVideoRenaming(oldName: String, newName: String) = appDatabase
        .videoRenamingsQueries
        .insertRenaming(oldName, newName)
}