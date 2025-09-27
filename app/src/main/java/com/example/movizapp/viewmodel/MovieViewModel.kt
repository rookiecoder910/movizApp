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
class MovieViewModel(repository: Repository) : ViewModel() {

var movies by mutableStateOf<List<Movie>>(emptyList())
    private set//only the viewmodel can change the value of movies
  init{
      viewModelScope.launch {
         try {
             moviesFromApi=repository
                 .getPopularMovies("80f9720370f5ec06ee02481601e89a13",1)
             //assign the fetched data to the 'movies' variable
             movies=moviesFromApi
         }
         catch (e:Exception){
             //fetch the data from ROOM DB
             e.printStackTrace()
         }

      }
  }
}
//the online movies
var moviesFromApi by mutableStateOf<List<Movie>>(emptyList())
private set
