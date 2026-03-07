package com.example.movizapp.Repository

import android.content.Context
import com.example.movizapp.retrofit.Movie
import com.example.movizapp.retrofit.MovieDetails
import com.example.movizapp.retrofit.RetrofitInstance
import com.example.movizapp.retrofit.SeasonDetails
import com.example.movizapp.retrofit.TvShow
import com.example.movizapp.retrofit.TvShowDetails
import com.example.movizapp.room.MovieDAO
import com.example.movizapp.room.MoviesDb

//repository contains all methods to fetch data from online and offline
class Repository(context: Context) {

    //fetching data from online API
    suspend fun getPopularMovies(apiKey: String, page: Int): List<Movie> {
        return RetrofitInstance.api.getPopularMovies(apiKey, page).results
    }

    //fetching data from offline database
    private val db = MoviesDb.getInstance(context)
    private val movieDao: MovieDAO = db.movieDao

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
        return RetrofitInstance.api.searchMovies(apiKey, query, page).results
    }

    suspend fun getMovieDetails(apiKey: String, movieId: Int): MovieDetails {
        return RetrofitInstance.api.getMovieDetails(movieId, apiKey)
    }

    // --- TV Series Methods ---
    suspend fun getPopularTvShows(apiKey: String, page: Int): List<TvShow> {
        return RetrofitInstance.api.getPopularTvShows(apiKey, page).results
    }

    suspend fun searchTvShows(apiKey: String, query: String, page: Int = 1): List<TvShow> {
        return RetrofitInstance.api.searchTvShows(apiKey, query, page).results
    }

    suspend fun getTvShowDetails(apiKey: String, tvId: Int): TvShowDetails {
        return RetrofitInstance.api.getTvShowDetails(tvId, apiKey)
    }

    suspend fun getSeasonDetails(apiKey: String, tvId: Int, seasonNumber: Int): SeasonDetails {
        return RetrofitInstance.api.getSeasonDetails(tvId, seasonNumber, apiKey)
    }
}