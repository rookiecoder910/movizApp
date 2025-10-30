import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.movizapp.screens.MovieDetailScreen
import com.example.movizapp.screens.MovieScreen
import com.example.movizapp.viewmodel.MovieViewModel

@Composable
fun MovizNavGraph(
    viewModel: MovieViewModel,
    navController: NavHostController
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
         MovieScreen(viewModel = viewModel, navController = navController)
        }

        composable(
            "movieDetail/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
            MovieDetailScreen(movieId = movieId, viewModel = viewModel, navController = navController)
        }
    }
}
