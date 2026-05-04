package com.example.movizapp.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.movizapp.retrofit.Movie



@Database(
    entities = [Movie::class, WatchlistItem::class, WatchHistoryItem::class],
    version = 5
)
abstract class MoviesDb : RoomDatabase() {
    abstract val movieDao: MovieDAO
    abstract val watchlistDao: WatchlistDao
    abstract val watchHistoryDao: WatchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: MoviesDb? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE movies_table ADD COLUMN vote_average REAL NOT NULL DEFAULT 0.0")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE movies_table ADD COLUMN release_date TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS watchlist_table (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tmdbId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        posterPath TEXT,
                        mediaType TEXT NOT NULL,
                        voteAverage REAL NOT NULL,
                        addedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS watch_history_table (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tmdbId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        posterPath TEXT,
                        mediaType TEXT NOT NULL,
                        season INTEGER,
                        episode INTEGER,
                        watchedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add watchProgress (0.0–1.0) and rating for the Continue Watching card UI
                db.execSQL("ALTER TABLE watch_history_table ADD COLUMN watchProgress REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE watch_history_table ADD COLUMN rating REAL NOT NULL DEFAULT 0.0")
                // Remove duplicates on migration (keep latest per tmdbId+mediaType)
                db.execSQL("""
                    DELETE FROM watch_history_table 
                    WHERE id NOT IN (
                        SELECT MAX(id) FROM watch_history_table 
                        GROUP BY tmdbId, mediaType
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): MoviesDb {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context = context.applicationContext,
                        MoviesDb::class.java,
                        "movies_db"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}