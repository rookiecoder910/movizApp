import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.movizapp.viewmodel.MovieViewModel

// Define a darker color palette for the gradient overlay
private val DarkOverlay = Color.Black.copy(alpha = 0.8f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    viewModel: MovieViewModel,
    navController: NavController
) {
    //   In a production app, you would fetch details via a dedicated API call here.
    val movie = viewModel.movies.find { it.id == movieId }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    //  Implement Share Logic
    val shareMovie = {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out: ${movie?.title}! (https://www.themoviedb.org/movie/${movie?.id})")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Movie"))
    }

    if (movie == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Movie not found")
        }
        return
    }

    //  Use an updated state for watchlist button visual feedback
    var isWatchlisted by remember { mutableStateOf(false) }


    Scaffold(
        // Remove TopAppBar from Scaffold to allow the image to fill the top space
        // and add a floating TopAppBar later for better effect.
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->

        // Main Scrollable Content
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {
                // Image Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                ) {
                    // Poster Image
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w780${movie.poster_path}",
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    //  Gradient Overlay for a cinematic fade-out effect and title readability
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .align(Alignment.BottomStart)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface) // Fade to main background color
                                )
                            )
                    )

                    //  Title/Rating/Date placed directly on the image fade-out area
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            movie.title,
                            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White , fontWeight = FontWeight.Bold), fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = String.format("%.1f", movie.vote_average),
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Release Date",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Release: ${movie.release_date}",
                                style = MaterialTheme.typography.titleSmall.copy(color = Color.White),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                // Overview and Actions Section (Below the Image)
                Column(modifier = Modifier.padding(16.dp)) {

                    //  Floating Action Row (Watchlist and Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Watchlist Button
                        Button(
                            onClick = { isWatchlisted = !isWatchlisted },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWatchlisted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inversePrimary
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(
                                imageVector = if (isWatchlisted) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Toggle Watchlist",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (isWatchlisted) "WATCHLISTED" else "ADD TO WATCHLIST", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.width(8.dp))
                        // Share Button
                        IconButton(
                            onClick = shareMovie,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Movie")
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text("Overview", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        movie.overview,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }


            TopAppBar(
                title = { }, // Title is handled in the image area
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    }
}