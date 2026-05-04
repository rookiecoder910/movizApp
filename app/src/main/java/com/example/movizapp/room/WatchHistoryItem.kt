package com.example.movizapp.room

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "watch_history_table")
data class WatchHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String,       // "movie" or "tv"
    val season: Int? = null,
    val episode: Int? = null,
    val watchedAt: Long = System.currentTimeMillis(),
    // v5: watch progress (0.0f – 1.0f) and rating for the badge
    val watchProgress: Float = 0f,
    val rating: Double = 0.0
)
