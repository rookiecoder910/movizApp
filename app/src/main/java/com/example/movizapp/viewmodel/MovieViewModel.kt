package com.example.movizapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movizapp.BuildConfig
import com.example.movizapp.Repository.Repository
import com.example.movizapp.retrofit.Movie
import com.example.movizapp.retrofit.MovieDetails
import com.example.movizapp.retrofit.SeasonDetails
import com.example.movizapp.retrofit.TvShow
import com.example.movizapp.retrofit.TvShowDetails
import com.example.movizapp.room.WatchHistoryItem
import com.example.movizapp.room.WatchlistItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

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

    // --- Watchlist & History (Flow-based) ---
    val watchlist: StateFlow<List<WatchlistItem>> = repository.getAllWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlistCount: StateFlow<Int> = repository.getWatchlistCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentHistory: StateFlow<List<WatchHistoryItem>> = repository.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyCount: StateFlow<Int> = repository.getHistoryCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // API constant values
    private val API_KEY = BuildConfig.TMDB_API_KEY
    private var currentMoviePage = 1
    private var currentTvPage = 1
    var isLoadingMore by mutableStateOf(false)
        private set

    // Debouncing Jobs
    private var searchJob: Job? = null
    private var tvSearchJob: Job? = null

    init {
        // Load popular movies
        viewModelScope.launch {
            try {
                repository.refreshMovies(API_KEY, 1)
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
                tvShows = repository.getPopularTvShows(API_KEY, 1)
            } catch (e: Exception) {
                tvShows = emptyList()
            }
        }
    }

    // --- Pagination ---
    fun loadMoreMovies() {
        if (isLoadingMore) return
        isLoadingMore = true
        viewModelScope.launch {
            try {
                currentMoviePage++
                val more = repository.getPopularMovies(API_KEY, currentMoviePage)
                movies = movies + more
            } catch (e: Exception) {
                currentMoviePage--
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun loadMoreTvShows() {
        if (isLoadingMore) return
        isLoadingMore = true
        viewModelScope.launch {
            try {
                currentTvPage++
                val more = repository.getPopularTvShows(API_KEY, currentTvPage)
                tvShows = tvShows + more
            } catch (e: Exception) {
                currentTvPage--
            } finally {
                isLoadingMore = false
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

    // --- Watchlist Methods ---
    fun toggleWatchlist(tmdbId: Int, title: String, posterPath: String?, mediaType: String, voteAverage: Double) {
        viewModelScope.launch {
            val currentList = watchlist.value
            val isInList = currentList.any { it.tmdbId == tmdbId && it.mediaType == mediaType }
            if (isInList) {
                repository.removeFromWatchlist(tmdbId, mediaType)
            } else {
                repository.addToWatchlist(
                    WatchlistItem(
                        tmdbId = tmdbId,
                        title = title,
                        posterPath = posterPath,
                        mediaType = mediaType,
                        voteAverage = voteAverage
                    )
                )
            }
        }
    }

    fun isInWatchlist(tmdbId: Int, mediaType: String): StateFlow<Boolean> {
        return repository.isInWatchlist(tmdbId, mediaType)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    }

    // --- Watch History Methods ---
    fun recordWatch(tmdbId: Int, title: String, posterPath: String?, mediaType: String, season: Int? = null, episode: Int? = null) {
        viewModelScope.launch {
            repository.addToHistory(
                WatchHistoryItem(
                    tmdbId = tmdbId,
                    title = title,
                    posterPath = posterPath,
                    mediaType = mediaType,
                    season = season,
                    episode = episode
                )
            )
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
