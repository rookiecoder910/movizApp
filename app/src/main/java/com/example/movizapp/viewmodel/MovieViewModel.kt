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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.refreshMovies(API_KEY, 1)
                val dbMovies = repository.moviesFromDB()
                withContext(Dispatchers.Main) {
                    moviesFromRoomDb = dbMovies
                    movies = dbMovies
                    errorMessage = null
                }
            } catch (e: Exception) {
                val dbMovies = repository.moviesFromDB()
                withContext(Dispatchers.Main) {
                    moviesFromRoomDb = dbMovies
                    movies = dbMovies
                    if (movies.isEmpty()) {
                        errorMessage = "Failed to load movies. Check your connection."
                    }
                }
            }
        }

        // Load popular TV shows
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val shows = repository.getPopularTvShows(API_KEY, 1)
                withContext(Dispatchers.Main) {
                    tvShows = shows
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvShows = emptyList()
                }
            }
        }

        // Load all extra sections in parallel within a single coroutine
        viewModelScope.launch(Dispatchers.IO) {
            coroutineScope {
                val trending = async { runCatching { repository.getTrendingMovies(API_KEY) }.getOrDefault(emptyList()) }
                val topRated = async { runCatching { repository.getTopRatedMovies(API_KEY) }.getOrDefault(emptyList()) }
                val nowPlaying = async { runCatching { repository.getNowPlayingMovies(API_KEY) }.getOrDefault(emptyList()) }
                val upcoming = async { runCatching { repository.getUpcomingMovies(API_KEY) }.getOrDefault(emptyList()) }
                val topRatedTv = async { runCatching { repository.getTopRatedTvShows(API_KEY) }.getOrDefault(emptyList()) }
                val trendingTv = async { runCatching { repository.getTrendingTvShows(API_KEY) }.getOrDefault(emptyList()) }

                val trendingResult = trending.await()
                val topRatedResult = topRated.await()
                val nowPlayingResult = nowPlaying.await()
                val upcomingResult = upcoming.await()
                val topRatedTvResult = topRatedTv.await()
                val trendingTvResult = trendingTv.await()

                withContext(Dispatchers.Main) {
                    trendingMovies = trendingResult
                    topRatedMovies = topRatedResult
                    nowPlayingMovies = nowPlayingResult
                    upcomingMovies = upcomingResult
                    topRatedTvShows = topRatedTvResult
                    trendingTvShows = trendingTvResult
                }
            }
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                currentMoviePage++
                val more = repository.getPopularMovies(API_KEY, currentMoviePage)
                val combined = buildList {
                    addAll(movies)
                    addAll(more)
                }
                withContext(Dispatchers.Main) {
                    movies = combined
                }
            } catch (e: Exception) {
                currentMoviePage--
            } finally {
                withContext(Dispatchers.Main) {
                    isLoadingMoreMovies = false
                }
            }
        }
    }

    fun loadMoreTvShows() {
        if (isLoadingMoreTvShows) return
        isLoadingMoreTvShows = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                currentTvPage++
                val more = repository.getPopularTvShows(API_KEY, currentTvPage)
                val combined = buildList {
                    addAll(tvShows)
                    addAll(more)
                }
                withContext(Dispatchers.Main) {
                    tvShows = combined
                }
            } catch (e: Exception) {
                currentTvPage--
            } finally {
                withContext(Dispatchers.Main) {
                    isLoadingMoreTvShows = false
                }
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val details = repository.getMovieDetails(API_KEY, movieId)
                withContext(Dispatchers.Main) {
                    movieDetails = details
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                withContext(Dispatchers.Main) {
                    isDetailLoading = false
                }
            }
        }
        // Fetch similar movies in parallel
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val similar = repository.getSimilarMovies(API_KEY, movieId)
                withContext(Dispatchers.Main) {
                    similarMovies = similar
                }
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val details = repository.getTvShowDetails(API_KEY, tvId)
                withContext(Dispatchers.Main) {
                    tvShowDetails = details
                }
                if (details.seasons.isNotEmpty()) {
                    val firstRealSeason = details.seasons.firstOrNull { it.season_number > 0 }
                        ?: details.seasons.first()
                    val seasonDetail = repository.getSeasonDetails(API_KEY, tvId, firstRealSeason.season_number)
                    withContext(Dispatchers.Main) {
                        seasonDetails = seasonDetail
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                withContext(Dispatchers.Main) {
                    isTvDetailLoading = false
                }
            }
        }
        // Fetch similar TV shows in parallel
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val similar = repository.getSimilarTvShows(API_KEY, tvId)
                withContext(Dispatchers.Main) {
                    similarTvShows = similar
                }
            } catch (_: Exception) {}
        }
    }

    fun fetchSeasonDetails(tvId: Int, seasonNumber: Int) {
        isSeasonLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val details = repository.getSeasonDetails(API_KEY, tvId, seasonNumber)
                withContext(Dispatchers.Main) {
                    seasonDetails = details
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    seasonDetails = null
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isSeasonLoading = false
                }
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
            withContext(Dispatchers.IO) {
                coroutineScope {
                    try {
                        val movieSearch = async { repository.searchMovies(API_KEY, query) }
                        val tvSearch = async { repository.searchTvShows(API_KEY, query) }
                        val movieResults = movieSearch.await()
                        val tvResults = tvSearch.await()
                        withContext(Dispatchers.Main) {
                            searchResults = movieResults
                            tvSearchResults = tvResults
                        }
                    } catch (_: Exception) {
                        withContext(Dispatchers.Main) {
                            searchResults = emptyList()
                            tvSearchResults = emptyList()
                        }
                    } finally {
                        withContext(Dispatchers.Main) {
                            isSearching = false
                        }
                    }
                }
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
    fun recordWatch(
        tmdbId: Int,
        title: String,
        posterPath: String?,
        mediaType: String,
        season: Int? = null,
        episode: Int? = null,
        rating: Double = 0.0
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addToHistory(
                WatchHistoryItem(
                    tmdbId = tmdbId,
                    title = title,
                    posterPath = posterPath,
                    mediaType = mediaType,
                    season = season,
                    episode = episode,
                    rating = rating,
                    watchProgress = 0f   // reset progress on new watch
                )
            )
            // Removes duplicates — keeps only the latest entry per tmdbId+mediaType
            repository.removeDuplicateHistory()
        }
    }

    /**
     * Persists the user's watch progress (0.0–1.0) for a history entry.
     * Called from PlayerScreen when the user leaves the player.
     */
    fun updateWatchProgress(historyId: Int, progress: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWatchProgress(historyId, progress.coerceIn(0f, 1f))
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }
}
