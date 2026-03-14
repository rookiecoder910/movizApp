package com.example.movizapp.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history_table")
data class WatchHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String, // "movie" or "tv"
    val season: Int? = null,
    val episode: Int? = null,
    val watchedAt: Long = System.currentTimeMillis()
)
