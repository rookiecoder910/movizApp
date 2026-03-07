package com.example.movizapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.movizapp.retrofit.Episode
import com.example.movizapp.retrofit.TvShowDetails
import com.example.movizapp.ui.theme.DarkBackground
import com.example.movizapp.ui.theme.DarkCard
import com.example.movizapp.ui.theme.DarkSurfaceVariant
import com.example.movizapp.ui.theme.GoldRating
import com.example.movizapp.ui.theme.NetflixRed
import com.example.movizapp.ui.theme.TextGrey
import com.example.movizapp.viewmodel.MovieViewModel
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowDetailScreen(
    tvId: Int,
    viewModel: MovieViewModel,
    navController: NavController
) {
    LaunchedEffect(tvId) {
        viewModel.fetchTvShowDetails(tvId)
    }

    val tvShow: TvShowDetails? = viewModel.tvShowDetails
    val seasonDetails = viewModel.seasonDetails
    val isLoading = viewModel.isTvDetailLoading
    val isSeasonLoading = viewModel.isSeasonLoading
    val scrollState = rememberScrollState()

    if (tvShow == null || isLoading) {
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

    var selectedSeasonNumber by remember {
        mutableStateOf(
            tvShow.seasons.firstOrNull { it.season_number > 0 }?.season_number
                ?: tvShow.seasons.firstOrNull()?.season_number ?: 1
        )
    }

    Scaffold(containerColor = DarkBackground) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {
                // --- HEADER ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {
                    val imageUrl = if (tvShow.backdrop_path != null) {
                        "https://image.tmdb.org/t/p/w1280${tvShow.backdrop_path}"
                    } else if (tvShow.poster_path != null) {
                        "https://image.tmdb.org/t/p/w780${tvShow.poster_path}"
                    } else null

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = tvShow.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

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
                            text = tvShow.name,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 2
                        )

                        tvShow.tagline?.takeIf { it.isNotBlank() }?.let {
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
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldRating, modifier = Modifier.size(18.dp))
                            Text(
                                text = String.format("%.1f", tvShow.vote_average),
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = tvShow.first_air_date ?: "N/A",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "${tvShow.number_of_seasons} Season${if (tvShow.number_of_seasons > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                            )
                        }
                    }
                }

                // --- CONTENT ---
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                    // Watch Now
                    Button(
                        onClick = { navController.navigate("player/tv/$tvId/1/1") },
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

                    Spacer(Modifier.height(20.dp))

                    // Genres
                    if (tvShow.genres.isNotEmpty()) {
                        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                            tvShow.genres.forEach { genre ->
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

                    // Overview
                    Text("Overview", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text(tvShow.overview, style = MaterialTheme.typography.bodyMedium, color = TextGrey)
                    Spacer(Modifier.height(24.dp))

                    // --- Season Selector ---
                    if (tvShow.seasons.isNotEmpty()) {
                        Text("Episodes", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(Modifier.height(12.dp))

                        ScrollableTabRow(
                            selectedTabIndex = tvShow.seasons.indexOfFirst { it.season_number == selectedSeasonNumber }
                                .coerceAtLeast(0),
                            edgePadding = 0.dp,
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            divider = {}
                        ) {
                            tvShow.seasons.forEach { season ->
                                Tab(
                                    selected = season.season_number == selectedSeasonNumber,
                                    onClick = {
                                        selectedSeasonNumber = season.season_number
                                        viewModel.fetchSeasonDetails(tvId, season.season_number)
                                    },
                                    text = {
                                        Text(
                                            season.name,
                                            color = if (season.season_number == selectedSeasonNumber) Color.White else TextGrey,
                                            fontWeight = if (season.season_number == selectedSeasonNumber) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Episode List
                        if (isSeasonLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = NetflixRed)
                            }
                        } else if (seasonDetails != null) {
                            seasonDetails.episodes.forEach { episode ->
                                EpisodeCard(
                                    episode = episode,
                                    tvId = tvId,
                                    seasonNumber = selectedSeasonNumber,
                                    navController = navController
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
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
fun EpisodeCard(
    episode: Episode,
    tvId: Int,
    seasonNumber: Int,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode thumbnail
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(74.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCard)
            ) {
                AsyncImage(
                    model = if (episode.still_path != null)
                        "https://image.tmdb.org/t/p/w300${episode.still_path}"
                    else null,
                    contentDescription = episode.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Play overlay
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(4.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${episode.episode_number}. ${episode.name}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                episode.overview?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = TextGrey
                    )
                }
                episode.air_date?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGrey.copy(alpha = 0.6f)
                    )
                }
            }

            // Play button
            IconButton(
                onClick = {
                    navController.navigate("player/tv/$tvId/$seasonNumber/${episode.episode_number}")
                }
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play Episode",
                    tint = NetflixRed,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
