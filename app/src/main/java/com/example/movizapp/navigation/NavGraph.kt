package com.example.movizapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.movizapp.screens.MovieDetailScreen
import com.example.movizapp.screens.MovieScreen
import com.example.movizapp.screens.PlayerScreen
import com.example.movizapp.screens.ProfileScreen
import com.example.movizapp.screens.SearchScreen
import com.example.movizapp.screens.TvShowDetailScreen
import com.example.movizapp.viewmodel.MovieViewModel

@Composable
fun MovizNavGraph(
    viewModel: MovieViewModel,
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            MovieScreen(viewModel = viewModel, navController = navController)
        }
        composable("search") {
            SearchScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            "movieDetail/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
            MovieDetailScreen(movieId = movieId, viewModel = viewModel, navController = navController)
        }
        composable(
            "tvDetail/{tvId}",
            arguments = listOf(navArgument("tvId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tvId = backStackEntry.arguments?.getInt("tvId") ?: 0
            TvShowDetailScreen(tvId = tvId, viewModel = viewModel, navController = navController)
        }
        composable(
            "player/movie/{tmdbId}",
            arguments = listOf(navArgument("tmdbId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tmdbId = backStackEntry.arguments?.getInt("tmdbId") ?: 0
            PlayerScreen(
                mediaType = "movie",
                tmdbId = tmdbId,
                navController = navController
            )
        }
        composable(
            "player/tv/{tmdbId}/{season}/{episode}",
            arguments = listOf(
                navArgument("tmdbId") { type = NavType.IntType },
                navArgument("season") { type = NavType.IntType },
                navArgument("episode") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val tmdbId = backStackEntry.arguments?.getInt("tmdbId") ?: 0
            val season = backStackEntry.arguments?.getInt("season") ?: 1
            val episode = backStackEntry.arguments?.getInt("episode") ?: 1
            PlayerScreen(
                mediaType = "tv",
                tmdbId = tmdbId,
                season = season,
                episode = episode,
                navController = navController
            )
        }
        composable("profile") {
            ProfileScreen()
        }
    }
}
