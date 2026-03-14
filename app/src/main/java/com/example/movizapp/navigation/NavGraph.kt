package com.example.movizapp

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.movizapp.auth.AuthViewModel
import com.example.movizapp.screens.LoginScreen
import com.example.movizapp.screens.MovieDetailScreen
import com.example.movizapp.screens.MovieScreen
import com.example.movizapp.screens.PlayerScreen
import com.example.movizapp.screens.ProfileScreen
import com.example.movizapp.screens.SearchScreen
import com.example.movizapp.screens.TvShowDetailScreen
import com.example.movizapp.sync.FirestoreSyncManager
import com.example.movizapp.viewmodel.MovieViewModel

private val fadeIn = fadeIn(animationSpec = tween(300))
private val fadeOut = fadeOut(animationSpec = tween(300))
private val slideInRight = slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = tween(300))
private val slideOutRight = slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = tween(300))
private val slideInLeft = slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300))

@Composable
fun MovizNavGraph(
    viewModel: MovieViewModel,
    authViewModel: AuthViewModel,
    syncManager: FirestoreSyncManager,
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = "home") {
        // --- Bottom Nav Tabs (fade transition) ---
        composable("home", enterTransition = { fadeIn }, exitTransition = { fadeOut }) {
            MovieScreen(viewModel = viewModel, navController = navController)
        }
        composable("search", enterTransition = { fadeIn }, exitTransition = { fadeOut }) {
            SearchScreen(viewModel = viewModel, navController = navController)
        }
        composable("profile", enterTransition = { fadeIn }, exitTransition = { fadeOut }) {
            ProfileScreen(
                viewModel = viewModel,
                authViewModel = authViewModel,
                syncManager = syncManager,
                navController = navController
            )
        }

        // --- Login ---
        composable(
            "login",
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut }
        ) {
            LoginScreen(authViewModel = authViewModel, navController = navController)
        }

        // --- Detail Screens ---
        composable(
            "movieDetail/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.IntType }),
            enterTransition = { slideInRight + fadeIn },
            exitTransition = { fadeOut },
            popEnterTransition = { slideInLeft + fadeIn },
            popExitTransition = { slideOutRight + fadeOut }
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
            MovieDetailScreen(movieId = movieId, viewModel = viewModel, navController = navController)
        }
        composable(
            "tvDetail/{tvId}",
            arguments = listOf(navArgument("tvId") { type = NavType.IntType }),
            enterTransition = { slideInRight + fadeIn },
            exitTransition = { fadeOut },
            popEnterTransition = { slideInLeft + fadeIn },
            popExitTransition = { slideOutRight + fadeOut }
        ) { backStackEntry ->
            val tvId = backStackEntry.arguments?.getInt("tvId") ?: 0
            TvShowDetailScreen(tvId = tvId, viewModel = viewModel, navController = navController)
        }

        // --- Player Screens ---
        composable(
            "player/movie/{tmdbId}",
            arguments = listOf(navArgument("tmdbId") { type = NavType.IntType }),
            enterTransition = { slideInVertically(initialOffsetY = { it }) + fadeIn },
            exitTransition = { slideOutVertically(targetOffsetY = { it }) + fadeOut }
        ) { backStackEntry ->
            val tmdbId = backStackEntry.arguments?.getInt("tmdbId") ?: 0
            PlayerScreen(mediaType = "movie", tmdbId = tmdbId, navController = navController, viewModel = viewModel)
        }
        composable(
            "player/tv/{tmdbId}/{season}/{episode}",
            arguments = listOf(
                navArgument("tmdbId") { type = NavType.IntType },
                navArgument("season") { type = NavType.IntType },
                navArgument("episode") { type = NavType.IntType }
            ),
            enterTransition = { slideInVertically(initialOffsetY = { it }) + fadeIn },
            exitTransition = { slideOutVertically(targetOffsetY = { it }) + fadeOut }
        ) { backStackEntry ->
            val tmdbId = backStackEntry.arguments?.getInt("tmdbId") ?: 0
            val season = backStackEntry.arguments?.getInt("season") ?: 1
            val episode = backStackEntry.arguments?.getInt("episode") ?: 1
            PlayerScreen(mediaType = "tv", tmdbId = tmdbId, season = season, episode = episode, navController = navController, viewModel = viewModel)
        }
    }
}
