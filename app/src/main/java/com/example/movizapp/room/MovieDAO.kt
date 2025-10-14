package com.example.movizapp.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.movizapp.retrofit.Movie
//used to set up the database operations
@Dao
interface MovieDAO {
    @Insert
    suspend fun insert(movie: Movie)
    @Insert
    suspend fun insertMoviesList(movies: List<Movie>)
    @Query("DELETE FROM movies_table")
    suspend fun deleteAllMovies()
    @Query("SELECT * FROM  movies_table")
    suspend fun getAllMovieSInDB(): List<Movie>
}