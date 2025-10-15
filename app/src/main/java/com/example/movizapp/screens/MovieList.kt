package com.example.movizapp.screens

import MovieItem
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.example.movizapp.retrofit.Movie


@Composable
fun MovieList(
    movies: List<Movie>,
    onMovieClick: (Int) -> Unit
) {
    LazyColumn {
        items(movies) { movie ->
            MovieItem(
                movie = movie,
                onClick = { onMovieClick(movie.id) }
            )
        }
    }
}
