package com.example.movizapp.screens

import androidx.compose.runtime.Composable
import com.example.movizapp.viewmodel.MovieViewModel
//Composable function to display the list of movies
//no need for observe as state .we are not using
//livedata we are using mutable state of
@Composable
fun MovieScreen(viewModel: MovieViewModel) {
    val moviesList = viewModel.movies
    MovieList(movies=moviesList)



}