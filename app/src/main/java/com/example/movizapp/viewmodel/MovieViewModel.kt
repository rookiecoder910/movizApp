package com.example.movizapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movizapp.Repository.Repository
import com.example.movizapp.retrofit.Movie
import com.example.movizapp.retrofit.MovieDetails // NEW IMPORT for Movie Details
import kotlinx.coroutines.Job // NEW IMPORT for debouncing
import kotlinx.coroutines.delay // NEW IMPORT for debouncing
import kotlinx.coroutines.launch

//viewmodel stores and manages UI related data
//viewmodel uses mutable state of instead of livedata
//when the value of 'movies' changes, the UI is automatically updated
class MovieViewModel(private val repository: Repository) : ViewModel() {

    var movies by mutableStateOf<List<Movie>>(emptyList())
        private set //only the viewmodel can change the value of movies

    // the online movies
    var moviesFromApi by mutableStateOf<List<Movie>>(emptyList())
        private set

    // the offline movies
    var moviesFromRoomDb by mutableStateOf<List<Movie>>(emptyList())
        private set //only the viewmodel can change the value of movies

    // API constant values
    private val API_KEY = "80f9720370f5ec06ee02481601e89a13"
    private val PAGE = 1

    // --- NEW: Debouncing Job for Search ---
    private var searchJob: Job? = null

    // --- NEW: States for Movie Details Screen ---
    var movieDetails by mutableStateOf<MovieDetails?>(null)
        private set

    var isDetailLoading by mutableStateOf(false)
        private set
    // ---------------------------------------------

    init{
        viewModelScope.launch {
            try {
                // sync data: fetch from api, clear old db, and insert fresh data
                repository.refreshMovies(API_KEY, PAGE)

                // fetch the data from ROOM DB (which now holds the fresh API data)
                moviesFromRoomDb = repository.moviesFromDB()

                // assign the fetched data to the 'movies' variable
                movies = moviesFromRoomDb

            }
            catch (e:Exception){
                // if api fails, fetch the cached data from ROOM DB
                moviesFromRoomDb = repository.moviesFromDB()

                // assign the fetched data to the 'movies' variable
                movies = moviesFromRoomDb
            }

        }
    }


    // New state to hold the search results
    var searchResults by mutableStateOf<List<Movie>>(emptyList())

    // New state to track if a search is active (optional, but useful for UI)
    var isSearching by mutableStateOf(false)
        private set


    // Updated function to perform the search WITH DEBOUNCING
    fun searchMovies(query: String) {
        // 1. Handle blank query immediately
        if (query.isBlank()) {
            searchResults = emptyList()
            isSearching = false
            searchJob?.cancel() // Cancel any pending job
            return
        }

        // 2. Cancel the previous job (CORE DEBOUNCE logic)
        searchJob?.cancel()

        isSearching = true

        // 3. Start a new job that waits 500ms before execution
        searchJob = viewModelScope.launch {
            delay(500L) // Wait for 500 milliseconds before firing the network request

            try {
                // Call the new Repository function (only runs if the delay completes)
                val results = repository.searchMovies(API_KEY, query)

                // Update the search results state
                searchResults = results

            } catch (e: Exception) {
                // Handle error
                searchResults = emptyList()
                // Log.e("Search", "Search failed: ${e.message}")
            } finally {
                // Set isSearching to false only if the job successfully finished (was't cancelled)
                if (searchJob?.isCancelled == false) {
                    isSearching = false
                }
            }
        }
    }

    // --- NEW function to fetch Movie Details ---
    fun fetchMovieDetails(movieId: Int) {
        // Reset state before loading new details
        movieDetails = null
        isDetailLoading = true

        viewModelScope.launch {
            try {
                // Call the existing function in your Repository
                val details = repository.getMovieDetails(API_KEY, movieId)
                movieDetails = details
            } catch (e: Exception) {
                // Handle API error for detail fetching
                // Log.e("MovieDetails", "Failed to fetch details for $movieId: ${e.message}")
            } finally {
                isDetailLoading = false
            }
        }
    }
}
