package com.example.movizapp.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_table")
data class WatchlistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String, // "movie" or "tv"
    val voteAverage: Double,
    val addedAt: Long = System.currentTimeMillis()
)
