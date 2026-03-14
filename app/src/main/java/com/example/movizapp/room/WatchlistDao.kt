package com.example.movizapp.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistItem)

    @Query("DELETE FROM watchlist_table WHERE tmdbId = :tmdbId AND mediaType = :mediaType")
    suspend fun deleteByTmdbId(tmdbId: Int, mediaType: String)

    @Query("SELECT * FROM watchlist_table ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchlistItem>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_table WHERE tmdbId = :tmdbId AND mediaType = :mediaType)")
    fun isInWatchlist(tmdbId: Int, mediaType: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM watchlist_table")
    fun getCount(): Flow<Int>
}
