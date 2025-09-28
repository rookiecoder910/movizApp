package com.example.movizapp.screens

import MovieItem
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import com.example.movizapp.retrofit.Movie
import androidx.compose.foundation.lazy.items

@Composable
fun MovieList(movies: List<Movie>) {
    LazyColumn {
        //for each 'movie' in 'movies'list
        //a 'movieitem' composable is created
        //'movie' object is passed as a parameter
        // to the 'MovieItem' compsable allowing it to display the details of the 'movie'
        items(movies){
            movie->MovieItem(movie)
        }
    }
}