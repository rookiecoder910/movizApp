package com.example.movizapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movizapp.Repository.Repository
import com.example.movizapp.retrofit.Movie
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
        private set

    // New state to track if a search is active (optional, but useful for UI)
    var isSearching by mutableStateOf(false)
        private set

// ... your existing API_KEY and PAGE_ONE constants

    // New function to perform the search
    fun searchMovies(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            isSearching = false
            return
        }

        isSearching = true
        viewModelScope.launch {
            try {
                // Call the new Repository function
                val results = repository.searchMovies(API_KEY, query)

                // Update the search results state
                searchResults = results

            } catch (e: Exception) {
                // Handle error, maybe show an error message
                searchResults = emptyList()
                // Log.e("Search", "Search failed: ${e.message}")
            } finally {
                isSearching = false
            }
        }
    }
}