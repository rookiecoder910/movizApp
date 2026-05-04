package com.example.movizapp.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchHistoryItem)

    @Query("SELECT * FROM watch_history_table ORDER BY watchedAt DESC LIMIT :limit")
    fun getRecent(limit: Int = 20): Flow<List<WatchHistoryItem>>

    @Query("DELETE FROM watch_history_table")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM watch_history_table")
    fun getCount(): Flow<Int>

    /**
     * Updates the watch progress (0.0f – 1.0f) for a specific entry.
     * Called when the user stops watching to persist how far they got.
     */
    @Query("UPDATE watch_history_table SET watchProgress = :progress WHERE id = :id")
    suspend fun updateProgress(id: Int, progress: Float)

    /**
     * Removes duplicate entries — keeps only the most recently watched
     * entry per tmdbId+mediaType combination.
     */
    @Query("""
        DELETE FROM watch_history_table 
        WHERE id NOT IN (
            SELECT MAX(id) FROM watch_history_table 
            GROUP BY tmdbId, mediaType
        )
    """)
    suspend fun removeDuplicates()
}
