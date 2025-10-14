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


@Database(entities = [Movie::class], version = 2)
abstract class MoviesDb : RoomDatabase() {
    abstract val movieDao: MovieDAO

    companion object {
        @Volatile
        private var INSTANCE: MoviesDb? = null


        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // SQL command to add the new 'vote_average' column.
                // It must be nullable or have a DEFAULT value, as existing rows will be missing it.
                db.execSQL("ALTER TABLE movies_table ADD COLUMN vote_average REAL NOT NULL DEFAULT 0.0")
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

                        .addMigrations(MIGRATION_1_2)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}