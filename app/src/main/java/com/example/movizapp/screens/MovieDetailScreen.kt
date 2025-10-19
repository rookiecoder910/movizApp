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
    // Fetch on launch
    LaunchedEffect(movieId) {
        viewModel.fetchMovieDetails(movieId)
    }

    val movie: MovieDetails? = viewModel.movieDetails
    val isLoading = viewModel.isDetailLoading
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    if (movie == null || isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(50.dp))
        }
        return
    }

    // --- Sharing ---
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

    Scaffold(containerColor = MaterialTheme.colorScheme.surface) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {

                // --- HEADER IMAGE ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                ) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w780${movie.poster_path}",
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlay
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .align(Alignment.BottomStart)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                                )
                            )
                    )

                    // Title + rating + date
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 2
                        )

                        movie.tagline?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

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
                                text = movie.release_date,
                                style = MaterialTheme.typography.titleSmall.copy(color = Color.White),
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            movie.runtime?.takeIf { it > 0 }?.let {
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = "${it} min",
                                    style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                                )
                            }
                        }
                    }
                }

                // --- MAIN DETAILS ---
                Column(modifier = Modifier.padding(16.dp)) {

                    // --- Action Buttons ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { isWatchlisted = !isWatchlisted },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWatchlisted)
                                    MaterialTheme.colorScheme.inversePrimary
                                else
                                    MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(
                                imageVector = if (isWatchlisted)
                                    Icons.Filled.Favorite
                                else
                                    Icons.Default.FavoriteBorder,
                                contentDescription = "Toggle Watchlist",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isWatchlisted) "WATCHLISTED" else "ADD TO WATCHLIST",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.width(8.dp))

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

                    // --- Genres ---
                    if (movie.genres.isNotEmpty()) {
                        Text("Genres", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                            movie.genres.forEach { genre ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(genre.name) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // --- Overview ---
                    Text("Overview", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(movie.overview, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(24.dp))

                    // --- Spoken Languages ---
                    if (movie.spoken_languages.isNotEmpty()) {
                        Text("Languages", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                            movie.spoken_languages.forEach {
                                SuggestionChip(onClick = {}, label = { Text(it.name) })
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // --- Production Companies ---
                    if (movie.production_companies.isNotEmpty()) {
                        Text("Production Companies", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        FlowRow(mainAxisSpacing = 12.dp, crossAxisSpacing = 12.dp) {
                            movie.production_companies.forEach { company ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    company.logo_path?.let { logo ->
                                        Image(
                                            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w185$logo"),
                                            contentDescription = company.name,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        company.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // --- Production Countries ---
                    if (movie.production_countries.isNotEmpty()) {
                        Text("Countries", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            movie.production_countries.joinToString { it.name },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(24.dp))
                    }

                    // --- Budget & Revenue ---
                    val formatCurrency: (Long?) -> String = { value ->
                        value?.let {
                            NumberFormat.getCurrencyInstance(Locale.US).format(it)
                        } ?: "N/A"
                    }

                    if (movie.budget != null || movie.revenue != null) {
                        Text("Financials", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Column {
                            Text("Budget: ${formatCurrency(movie.budget)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Revenue: ${formatCurrency(movie.revenue)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(32.dp))
                    }

                    // --- Status ---
                    movie.status?.let {
                        Text("Status", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Back Button
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    }
}
