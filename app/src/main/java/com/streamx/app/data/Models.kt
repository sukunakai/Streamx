package com.streamx.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Series(
    val id: String = "",
    val title: String = "",
    val thumbnailUrl: String = "",
    val category: String = "",
    val uploadTimestamp: Long = 0L,
    val seasons: List<Season> = emptyList()
)

data class Season(
    val seasonNum: Int = 1,
    val episodes: List<Episode> = emptyList()
)

data class Episode(
    val episodeNum: Int = 1,
    val title: String = "",
    val videoUrl: String = ""
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val seriesId: String,
    val seasonNum: Int,
    val episodeNum: Int,
    val timestamp: Long
)
