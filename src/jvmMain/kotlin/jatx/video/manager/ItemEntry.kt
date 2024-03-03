package jatx.video.manager

sealed class ItemEntry

data class PlaylistItemEntry(
    val playlistName: String
) : ItemEntry()

data class YearItemEntry(
    val playlistName: String,
    val year: Int
) : ItemEntry()

data class VideoItemEntry(
    val videoEntry: VideoEntry
): ItemEntry()

fun List<VideoEntry>.toItemEntries() = this
    .groupBy { it.playlistName }
    .toList()
    .sortedBy { it.first }
    .flatMap {
        val playlistName = it.first
        val itemEntries = arrayListOf<ItemEntry>()
        itemEntries.add(PlaylistItemEntry(playlistName))
        val otherItemEntries = it.second
            .groupBy { it.year }
            .toList()
            .sortedBy { it.first }
            .reversed()
            .flatMap {
                val innerItemEntries = arrayListOf<ItemEntry>()
                innerItemEntries.add(YearItemEntry(playlistName, it.first))
                val videoItemEntries = it.second
                    .sortedBy { it.actualDate }
                    .reversed()
                    .map { VideoItemEntry(it) }
                innerItemEntries.addAll(videoItemEntries)
                innerItemEntries
            }
        itemEntries.addAll(otherItemEntries)
        itemEntries
    }