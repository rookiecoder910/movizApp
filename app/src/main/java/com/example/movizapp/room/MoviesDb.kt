package com.example.movizapp.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.movizapp.retrofit.Movie
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized


// 1. INCREMENT VERSION to 3
@Database(entities = [Movie::class], version = 3)
abstract class MoviesDb : RoomDatabase() {
    abstract val movieDao: MovieDAO

    companion object {
        @Volatile
        private var INSTANCE: MoviesDb? = null


        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // SQL command to add the new 'vote_average' column.

                db.execSQL("ALTER TABLE movies_table ADD COLUMN vote_average REAL NOT NULL DEFAULT 0.0")
            }
        }


        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("ALTER TABLE movies_table ADD COLUMN release_date TEXT NOT NULL DEFAULT ''")
            }
        }

        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): MoviesDb {
            synchronized(lock = this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context = context.applicationContext,
                        MoviesDb::class.java,
                        "movies_db"
                    )

                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}