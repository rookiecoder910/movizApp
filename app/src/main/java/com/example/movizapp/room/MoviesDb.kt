package com.example.movizapp.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.movizapp.retrofit.Movie
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
//used to prevent multiple instances of database opening at the same time
@Database(entities = [Movie::class], version = 1)
abstract class MoviesDb : RoomDatabase() {
    abstract val movieDao: MovieDAO

    companion object {
        //volatile means it is immediately visible to all threads
        //prevents any possible race conditions in multithreading
        @Volatile
        private var INSTANCE: MoviesDb? = null
        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): MoviesDb {
            synchronized(lock = this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context = context.applicationContext,
                        MoviesDb::class.java,
                        "movies_db"
                    ).build()
                }

                return instance

            }
        }
    }
}
