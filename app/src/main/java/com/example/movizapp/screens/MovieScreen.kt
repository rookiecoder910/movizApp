package com.example.movizapp.screens
import MovieItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import com.airbnb.lottie.compose.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.movizapp.viewmodel.MovieViewModel
//Composable function to display the list of movies
//no need for observe as state .we are not using
//livedata we are using mutable state of
@Composable
fun MovieScreen(viewModel: MovieViewModel) {
    // Get the movie list state (observe changes)
    val movies = viewModel.movies

    // Lottie Composition setup (assuming you put the Lottie file in assets)
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("loading.json")
    )

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    // Conditional Display
    if (movies.isEmpty()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(200.dp)
            )
        }
    } else {
        // --- DISPLAY MOVIES STATE ---
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(movies.size) { index ->
                // This is where your MovieItem is used!
                MovieItem(movie = movies[index])
            }
        }
    }
}