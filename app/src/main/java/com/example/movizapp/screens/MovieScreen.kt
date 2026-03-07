package com.example.movizapp.screens

import MovieItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.movizapp.viewmodel.MovieViewModel

@Composable
fun MovieScreen(
    viewModel: MovieViewModel,
    navController: NavController
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Movies", "TV Series")

    val movies = viewModel.movies
    val searchResults = viewModel.searchResults
    val tvShows = viewModel.tvShows
    val tvSearchResults = viewModel.tvSearchResults

    val movieDisplayList = if (searchResults.isNotEmpty()) searchResults else movies
    val tvDisplayList = if (tvSearchResults.isNotEmpty()) tvSearchResults else tvShows

    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("Loading2.json")
    )

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
        speed = 0.5f,
        restartOnPlay = false
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        // Content
        when (selectedTabIndex) {
            0 -> {
                // Movies Tab
                if (movieDisplayList.isEmpty()) {
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
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(movieDisplayList.size) { index ->
                            MovieItem(movie = movieDisplayList[index]) { movieId ->
                                navController.navigate("movieDetail/$movieId")
                            }
                        }
                    }
                }
            }

            1 -> {
                // TV Series Tab
                if (tvDisplayList.isEmpty()) {
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
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tvDisplayList.size) { index ->
                            TvShowItem(tvShow = tvDisplayList[index]) { tvId ->
                                navController.navigate("tvDetail/$tvId")
                            }
                        }
                    }
                }
            }
        }
    }
}
