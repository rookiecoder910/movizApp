package com.example.movizapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movizapp.Repository.Repository
import com.example.movizapp.retrofit.Movie
import com.example.movizapp.retrofit.MovieDetails
import com.example.movizapp.retrofit.SeasonDetails
import com.example.movizapp.retrofit.TvShow
import com.example.movizapp.retrofit.TvShowDetails
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MovieViewModel(private val repository: Repository) : ViewModel() {

    // --- Movie States ---
    var movies by mutableStateOf<List<Movie>>(emptyList())
        private set

    var moviesFromApi by mutableStateOf<List<Movie>>(emptyList())
        private set

    var moviesFromRoomDb by mutableStateOf<List<Movie>>(emptyList())
        private set

    var searchResults by mutableStateOf<List<Movie>>(emptyList())

    var isSearching by mutableStateOf(false)
        private set

    var movieDetails by mutableStateOf<MovieDetails?>(null)
        private set

    var isDetailLoading by mutableStateOf(false)
        private set

    // --- TV Series States ---
    var tvShows by mutableStateOf<List<TvShow>>(emptyList())
        private set

    var tvSearchResults by mutableStateOf<List<TvShow>>(emptyList())

    var tvShowDetails by mutableStateOf<TvShowDetails?>(null)
        private set

    var seasonDetails by mutableStateOf<SeasonDetails?>(null)
        private set

    var isTvDetailLoading by mutableStateOf(false)
        private set

    var isSeasonLoading by mutableStateOf(false)
        private set

    // API constant values
    private val API_KEY = "80f9720370f5ec06ee02481601e89a13"
    private val PAGE = 1

    // Debouncing Jobs
    private var searchJob: Job? = null
    private var tvSearchJob: Job? = null

    init {
        // Load popular movies
        viewModelScope.launch {
            try {
                repository.refreshMovies(API_KEY, PAGE)
                moviesFromRoomDb = repository.moviesFromDB()
                movies = moviesFromRoomDb
            } catch (e: Exception) {
                moviesFromRoomDb = repository.moviesFromDB()
                movies = moviesFromRoomDb
            }
        }

        // Load popular TV shows
        viewModelScope.launch {
            try {
                tvShows = repository.getPopularTvShows(API_KEY, PAGE)
            } catch (e: Exception) {
                tvShows = emptyList()
            }
        }
    }

    // --- Movie Methods ---
    fun searchMovies(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            isSearching = false
            searchJob?.cancel()
            return
        }
        searchJob?.cancel()
        isSearching = true
        searchJob = viewModelScope.launch {
            delay(500L)
            try {
                searchResults = repository.searchMovies(API_KEY, query)
            } catch (e: Exception) {
                searchResults = emptyList()
            } finally {
                if (searchJob?.isCancelled == false) {
                    isSearching = false
                }
            }
        }
    }

    fun fetchMovieDetails(movieId: Int) {
        movieDetails = null
        isDetailLoading = true
        viewModelScope.launch {
            try {
                movieDetails = repository.getMovieDetails(API_KEY, movieId)
            } catch (e: Exception) {
                // Handle error
            } finally {
                isDetailLoading = false
            }
        }
    }

    // --- TV Series Methods ---
    fun searchTvShows(query: String) {
        if (query.isBlank()) {
            tvSearchResults = emptyList()
            tvSearchJob?.cancel()
            return
        }
        tvSearchJob?.cancel()
        tvSearchJob = viewModelScope.launch {
            delay(500L)
            try {
                tvSearchResults = repository.searchTvShows(API_KEY, query)
            } catch (e: Exception) {
                tvSearchResults = emptyList()
            }
        }
    }

    fun fetchTvShowDetails(tvId: Int) {
        tvShowDetails = null
        seasonDetails = null
        isTvDetailLoading = true
        viewModelScope.launch {
            try {
                tvShowDetails = repository.getTvShowDetails(API_KEY, tvId)
                // Auto-load first season if available
                val details = tvShowDetails
                if (details != null && details.seasons.isNotEmpty()) {
                    val firstRealSeason = details.seasons.firstOrNull { it.season_number > 0 }
                        ?: details.seasons.first()
                    fetchSeasonDetails(tvId, firstRealSeason.season_number)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isTvDetailLoading = false
            }
        }
    }

    fun fetchSeasonDetails(tvId: Int, seasonNumber: Int) {
        isSeasonLoading = true
        viewModelScope.launch {
            try {
                seasonDetails = repository.getSeasonDetails(API_KEY, tvId, seasonNumber)
            } catch (e: Exception) {
                seasonDetails = null
            } finally {
                isSeasonLoading = false
            }
        }
    }

    // Combined search for both movies and TV
    fun searchAll(query: String) {
        searchMovies(query)
        searchTvShows(query)
    }

    // Clear all search results
    fun clearSearchResults() {
        searchResults = emptyList()
        tvSearchResults = emptyList()
        isSearching = false
    }
}
