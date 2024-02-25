package jatx.video.manager

sealed class ItemEntry

data class PlaylistItemEntry(
    val playlistName: String
) : ItemEntry()

data class VideoItemEntry(
    val videoEntry: VideoEntry
): ItemEntry()

fun List<VideoEntry>.toItemEntries() = this
    .groupBy { it.playlistName }
    .flatMap {
        val itemEntries = arrayListOf<ItemEntry>()
        itemEntries.add(PlaylistItemEntry(it.key))
        itemEntries.addAll(it.value.map { VideoItemEntry(it) })
        itemEntries
    }