package com.example.movizapp.screens

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.PlayArrow
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
import coil.compose.rememberAsyncImagePainter
import com.example.movizapp.retrofit.MovieDetails
import com.example.movizapp.ui.theme.DarkBackground
import com.example.movizapp.ui.theme.DarkCard
import com.example.movizapp.ui.theme.GoldRating
import com.example.movizapp.ui.theme.NetflixRed
import com.example.movizapp.ui.theme.TextGrey
import com.example.movizapp.viewmodel.MovieViewModel
import com.google.accompanist.flowlayout.FlowRow
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    viewModel: MovieViewModel,
    navController: NavController
) {
    LaunchedEffect(movieId) {
        viewModel.fetchMovieDetails(movieId)
    }

    val movie: MovieDetails? = viewModel.movieDetails
    val isLoading = viewModel.isDetailLoading
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    if (movie == null || isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NetflixRed, modifier = Modifier.size(50.dp))
        }
        return
    }

    val shareMovie: () -> Unit = {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out: ${movie.title}! (https://www.themoviedb.org/movie/${movie.id})"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Movie"))
    }

    var isWatchlisted by remember { mutableStateOf(false) }

    Scaffold(containerColor = DarkBackground) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {
                // --- HEADER IMAGE (use backdrop if available) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                ) {
                    val imageUrl = if (movie.backdrop_path != null) {
                        "https://image.tmdb.org/t/p/w1280${movie.backdrop_path}"
                    } else {
                        "https://image.tmdb.org/t/p/w780${movie.poster_path}"
                    }
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlay - taller for readability
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .align(Alignment.BottomStart)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, DarkBackground)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 2
                        )

                        movie.tagline?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = GoldRating,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = String.format("%.1f", movie.vote_average),
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = movie.release_date,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                            )
                            movie.runtime?.takeIf { it > 0 }?.let {
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = "${it / 60}h ${it % 60}m",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                                )
                            }
                        }
                    }
                }

                // --- CONTENT ---
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                    // --- Watch Now ---
                    Button(
                        onClick = { navController.navigate("player/movie/${movie.id}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Watch", modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Watch Now", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    // --- Action Row ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            icon = if (isWatchlisted) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                            label = if (isWatchlisted) "Watchlisted" else "Watchlist",
                            tint = if (isWatchlisted) NetflixRed else TextGrey,
                            onClick = { isWatchlisted = !isWatchlisted }
                        )
                        ActionButton(
                            icon = Icons.Default.Share,
                            label = "Share",
                            tint = TextGrey,
                            onClick = shareMovie
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- Genres ---
                    if (movie.genres.isNotEmpty()) {
                        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                            movie.genres.forEach { genre ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(genre.name, color = Color.White) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = DarkCard
                                    ),
                                    border = null
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // --- Overview ---
                    Text("Overview", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text(movie.overview, style = MaterialTheme.typography.bodyMedium, color = TextGrey)
                    Spacer(Modifier.height(24.dp))

                    // --- Languages ---
                    if (movie.spoken_languages.isNotEmpty()) {
                        Text("Languages", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            movie.spoken_languages.joinToString(" · ") { it.name },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGrey
                        )
                        Spacer(Modifier.height(24.dp))
                    }

                    // --- Production ---
                    if (movie.production_companies.isNotEmpty()) {
                        Text("Production", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            movie.production_companies.joinToString(" · ") { it.name },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGrey
                        )
                        Spacer(Modifier.height(24.dp))
                    }

                    // Budget & Revenue
                    val formatCurrency: (Long?) -> String = { value ->
                        value?.takeIf { it > 0 }?.let {
                            NumberFormat.getCurrencyInstance(Locale.US).format(it)
                        } ?: "N/A"
                    }

                    if ((movie.budget ?: 0L) > 0 || (movie.revenue ?: 0L) > 0) {
                        Text("Financials", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Budget", style = MaterialTheme.typography.labelMedium, color = TextGrey)
                                Text(formatCurrency(movie.budget), color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Revenue", style = MaterialTheme.typography.labelMedium, color = TextGrey)
                                Text(formatCurrency(movie.revenue), color = Color.White)
                            }
                        }
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }

            // Back Button
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(50)
                            )
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(28.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}
