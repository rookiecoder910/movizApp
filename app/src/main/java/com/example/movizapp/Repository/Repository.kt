package com.example.movizapp.Repository

import android.content.Context
import com.example.movizapp.retrofit.Movie
import com.example.movizapp.retrofit.MovieDetails
//import com.example.movizapp.retrofit.MovieDetails
import com.example.movizapp.retrofit.RetrofitInstance
import com.example.movizapp.room.MovieDAO
import com.example.movizapp.room.MoviesDb

//repository contains all methods to fetch data from online and offline
class Repository(context: Context) {

    //fetching data from online API
    suspend fun getPopularMovies(apiKey: String,page:Int): List<Movie> {
        return RetrofitInstance.api.getPopularMovies(apiKey, page ).results

    }
    //fetching data from offline database
    private val db= MoviesDb.getInstance(context)
    private val movieDao: MovieDAO=db.movieDao

    suspend fun moviesFromDB(): List<Movie> {
        return movieDao.getAllMovieSInDB()
    }

    suspend fun insertMoviesIntoDB(movies: List<Movie>) {
        return movieDao.insertMoviesList(movies)
    }



    // New function to clear all existing movies from the database
    suspend fun clearAllMovies() {
        movieDao.deleteAllMovies()
    }

    // Function to fetch fresh data, clear old data, and insert fresh data
    suspend fun refreshMovies(apiKey: String, page: Int) {
        val freshMovies = getPopularMovies(apiKey, page)
        clearAllMovies()
        insertMoviesIntoDB(freshMovies)
    }
    suspend fun searchMovies(apiKey: String, query: String, page: Int = 1): List<Movie> {
        // This calls the new function in your Retrofit interface
        return RetrofitInstance.api.searchMovies(apiKey, query, page).results
    }
    //fetching data from tmdb api and present it in moviedetailscreen
    suspend fun getMovieDetails(apiKey: String, movieId: Int): MovieDetails {
        return RetrofitInstance.api.getMovieDetails(movieId, apiKey)
    }


}