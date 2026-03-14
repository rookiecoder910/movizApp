package com.example.movizapp.Repository

import com.example.movizapp.retrofit.ApiService
import com.example.movizapp.retrofit.Movie
import com.example.movizapp.retrofit.MovieDetails
import com.example.movizapp.retrofit.SeasonDetails
import com.example.movizapp.retrofit.TvShow
import com.example.movizapp.retrofit.TvShowDetails
import com.example.movizapp.room.MovieDAO
import com.example.movizapp.room.WatchHistoryDao
import com.example.movizapp.room.WatchHistoryItem
import com.example.movizapp.room.WatchlistDao
import com.example.movizapp.room.WatchlistItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val api: ApiService,
    private val movieDao: MovieDAO,
    private val watchlistDao: WatchlistDao,
    private val watchHistoryDao: WatchHistoryDao
) {
    // --- Movies API ---
    suspend fun getPopularMovies(apiKey: String, page: Int): List<Movie> {
        return api.getPopularMovies(apiKey, page).results
    }

    suspend fun moviesFromDB(): List<Movie> {
        return movieDao.getAllMovieSInDB()
    }

    suspend fun insertMoviesIntoDB(movies: List<Movie>) {
        return movieDao.insertMoviesList(movies)
    }

    suspend fun clearAllMovies() {
        movieDao.deleteAllMovies()
    }

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

    // --- TV Series ---
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

    // --- Extra Sections ---
    suspend fun getTrendingMovies(apiKey: String): List<Movie> = api.getTrendingMovies(apiKey).results
    suspend fun getTopRatedMovies(apiKey: String): List<Movie> = api.getTopRatedMovies(apiKey).results
    suspend fun getNowPlayingMovies(apiKey: String): List<Movie> = api.getNowPlayingMovies(apiKey).results
    suspend fun getUpcomingMovies(apiKey: String): List<Movie> = api.getUpcomingMovies(apiKey).results
    suspend fun getTopRatedTvShows(apiKey: String): List<TvShow> = api.getTopRatedTvShows(apiKey).results
    suspend fun getTrendingTvShows(apiKey: String): List<TvShow> = api.getTrendingTvShows(apiKey).results

    // --- Watchlist ---
    suspend fun addToWatchlist(item: WatchlistItem) = watchlistDao.insert(item)
    suspend fun removeFromWatchlist(tmdbId: Int, mediaType: String) = watchlistDao.deleteByTmdbId(tmdbId, mediaType)
    fun getAllWatchlist(): Flow<List<WatchlistItem>> = watchlistDao.getAll()
    fun isInWatchlist(tmdbId: Int, mediaType: String): Flow<Boolean> = watchlistDao.isInWatchlist(tmdbId, mediaType)
    fun getWatchlistCount(): Flow<Int> = watchlistDao.getCount()

    // --- Watch History ---
    suspend fun addToHistory(item: WatchHistoryItem) = watchHistoryDao.insert(item)
    fun getRecentHistory(limit: Int = 20): Flow<List<WatchHistoryItem>> = watchHistoryDao.getRecent(limit)
    suspend fun clearHistory() = watchHistoryDao.clearAll()
    fun getHistoryCount(): Flow<Int> = watchHistoryDao.getCount()
}