package com.example.movizapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movizapp.BuildConfig
import com.example.movizapp.repository.Repository
import com.example.movizapp.retrofit.Movie
import com.example.movizapp.retrofit.MovieDetails
import com.example.movizapp.retrofit.SeasonDetails
import com.example.movizapp.retrofit.TvShow
import com.example.movizapp.retrofit.TvShowDetails
import com.example.movizapp.room.WatchHistoryItem
import com.example.movizapp.room.WatchlistItem
import com.example.movizapp.util.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val repository: Repository,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    // --- Network State ---
    val isOnline: StateFlow<Boolean> = connectivityObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // --- Error State ---
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // --- Refresh State ---
    var isRefreshing by mutableStateOf(false)
        private set

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

    // --- Extra Sections ---
    var trendingMovies by mutableStateOf<List<Movie>>(emptyList())
        private set
    var topRatedMovies by mutableStateOf<List<Movie>>(emptyList())
        private set
    var nowPlayingMovies by mutableStateOf<List<Movie>>(emptyList())
        private set
    var upcomingMovies by mutableStateOf<List<Movie>>(emptyList())
        private set
    var topRatedTvShows by mutableStateOf<List<TvShow>>(emptyList())
        private set
    var trendingTvShows by mutableStateOf<List<TvShow>>(emptyList())
        private set

    // --- Similar Content ---
    var similarMovies by mutableStateOf<List<Movie>>(emptyList())
        private set
    var similarTvShows by mutableStateOf<List<TvShow>>(emptyList())
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
    var isLoadingMoreMovies by mutableStateOf(false)
        private set
    var isLoadingMoreTvShows by mutableStateOf(false)
        private set

    // Debouncing Jobs
    private var searchJob: Job? = null


    init {
        loadAllSections()
    }

    /**
     * Loads all home screen data sections.
     * Called on init and on pull-to-refresh.
     */
    private fun loadAllSections() {
        // Load popular movies
        viewModelScope.launch {
            try {
                repository.refreshMovies(API_KEY, 1)
                moviesFromRoomDb = repository.moviesFromDB()
                movies = moviesFromRoomDb
                errorMessage = null
            } catch (e: Exception) {
                moviesFromRoomDb = repository.moviesFromDB()
                movies = moviesFromRoomDb
                if (movies.isEmpty()) {
                    errorMessage = "Failed to load movies. Check your connection."
                }
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

        // Load extra sections
        viewModelScope.launch {
            try { trendingMovies = repository.getTrendingMovies(API_KEY) } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try { topRatedMovies = repository.getTopRatedMovies(API_KEY) } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try { nowPlayingMovies = repository.getNowPlayingMovies(API_KEY) } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try { upcomingMovies = repository.getUpcomingMovies(API_KEY) } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try { topRatedTvShows = repository.getTopRatedTvShows(API_KEY) } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try { trendingTvShows = repository.getTrendingTvShows(API_KEY) } catch (_: Exception) {}
        }
    }

    /**
     * Pull-to-refresh: reloads all sections and resets pagination.
     */
    fun refreshAll() {
        isRefreshing = true
        errorMessage = null
        currentMoviePage = 1
        currentTvPage = 1
        loadAllSections()
        viewModelScope.launch {
            // Small delay so the refresh indicator is visible
            delay(800)
            isRefreshing = false
        }
    }

    fun dismissError() {
        errorMessage = null
    }

    // --- Pagination ---
    fun loadMoreMovies() {
        if (isLoadingMoreMovies) return
        isLoadingMoreMovies = true
        viewModelScope.launch {
            try {
                currentMoviePage++
                val more = repository.getPopularMovies(API_KEY, currentMoviePage)
                movies = movies + more
            } catch (e: Exception) {
                currentMoviePage--
            } finally {
                isLoadingMoreMovies = false
            }
        }
    }

    fun loadMoreTvShows() {
        if (isLoadingMoreTvShows) return
        isLoadingMoreTvShows = true
        viewModelScope.launch {
            try {
                currentTvPage++
                val more = repository.getPopularTvShows(API_KEY, currentTvPage)
                tvShows = tvShows + more
            } catch (e: Exception) {
                currentTvPage--
            } finally {
                isLoadingMoreTvShows = false
            }
        }
    }

    // --- Movie Methods ---
    fun searchMovies(query: String) {
        searchAll(query)
    }


    fun fetchMovieDetails(movieId: Int) {
        movieDetails = null
        similarMovies = emptyList()
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
        // Fetch similar movies in parallel
        viewModelScope.launch {
            try {
                similarMovies = repository.getSimilarMovies(API_KEY, movieId)
            } catch (_: Exception) {}
        }
    }

    // --- TV Series Methods ---
    fun searchTvShows(query: String) {
        searchAll(query)
    }


    fun fetchTvShowDetails(tvId: Int) {
        tvShowDetails = null
        seasonDetails = null
        similarTvShows = emptyList()
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
        // Fetch similar TV shows in parallel
        viewModelScope.launch {
            try {
                similarTvShows = repository.getSimilarTvShows(API_KEY, tvId)
            } catch (_: Exception) {}
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
        if (query.isBlank()) {
            searchResults = emptyList()
            tvSearchResults = emptyList()
            isSearching = false
            searchJob?.cancel()
            return
        }
        searchJob?.cancel()
        isSearching = true
        searchJob = viewModelScope.launch {
            delay(500L) // debounce
            try {
                searchResults = repository.searchMovies(API_KEY, query)
                tvSearchResults = repository.searchTvShows(API_KEY, query)
            } catch (_: Exception) {
                searchResults = emptyList()
                tvSearchResults = emptyList()
            } finally {
                isSearching = false
            }
        }
    }

    // Clear all search results
    fun clearSearchResults() {
        searchJob?.cancel()
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

    // Cached watchlist check flows to prevent coroutine leaks on recomposition
    private val _watchlistFlowCache = mutableMapOf<String, StateFlow<Boolean>>()

    fun isInWatchlist(tmdbId: Int, mediaType: String): StateFlow<Boolean> {
        val key = "${mediaType}_${tmdbId}"
        return _watchlistFlowCache.getOrPut(key) {
            repository.isInWatchlist(tmdbId, mediaType)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        }
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
