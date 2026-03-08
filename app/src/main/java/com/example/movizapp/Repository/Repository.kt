package com.example.movizapp.Repository

import android.content.Context
import com.example.movizapp.retrofit.ApiService
import com.example.movizapp.retrofit.Movie
import com.example.movizapp.retrofit.MovieDetails
import com.example.movizapp.retrofit.RetrofitInstance
import com.example.movizapp.retrofit.SeasonDetails
import com.example.movizapp.retrofit.TvShow
import com.example.movizapp.retrofit.TvShowDetails
import com.example.movizapp.room.MovieDAO
import com.example.movizapp.room.MoviesDb

// Repository contains all methods to fetch data from online and offline
class Repository(context: Context) {

    // Use the cached API instance
    private val api: ApiService = RetrofitInstance.getApi(context)

    // fetching data from online API
    suspend fun getPopularMovies(apiKey: String, page: Int): List<Movie> {
        return api.getPopularMovies(apiKey, page).results
    }

    // fetching data from offline database
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
        return api.searchMovies(apiKey, query, page).results
    }

    suspend fun getMovieDetails(apiKey: String, movieId: Int): MovieDetails {
        return api.getMovieDetails(movieId, apiKey)
    }

    // --- TV Series Methods ---
    suspend fun getPopularTvShows(apiKey: String, page: Int): List<TvShow> {
        return api.getPopularTvShows(apiKey, page).results
    }

    suspend fun searchTvShows(apiKey: String, query: String, page: Int = 1): List<TvShow> {
        return api.searchTvShows(apiKey, query, page).results
    }

    suspend fun getTvShowDetails(apiKey: String, tvId: Int): TvShowDetails {
        return api.getTvShowDetails(tvId, apiKey)
    }

    suspend fun getSeasonDetails(apiKey: String, tvId: Int, seasonNumber: Int): SeasonDetails {
        return api.getSeasonDetails(tvId, seasonNumber, apiKey)
    }
}