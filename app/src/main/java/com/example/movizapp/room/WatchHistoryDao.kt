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
}
