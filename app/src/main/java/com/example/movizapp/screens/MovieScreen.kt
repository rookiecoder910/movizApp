package com.example.movizapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

import com.example.movizapp.retrofit.Movie
import com.example.movizapp.retrofit.TvShow
import com.example.movizapp.ui.theme.DarkBackground
import com.example.movizapp.ui.theme.DarkCard
import com.example.movizapp.ui.theme.GoldRating
import com.example.movizapp.ui.theme.NetflixRed
import com.example.movizapp.ui.theme.TextGrey
import com.example.movizapp.viewmodel.MovieViewModel

// Stable file-level constants — avoids Color.copy() allocation on every recomposition
// of PosterCard items during LazyRow scrolling (runs at ~60fps)
private val PosterRatingBgColor = Color(0xBF000000)      // Black at 75% alpha
private val PageIndicatorInactive = Color(0x66FFFFFF)    // White at 40% alpha
private val OfflineBannerBg = Color(0xE6E50914)          // NetflixRed at 90% alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieScreen(
    viewModel: MovieViewModel,
    navController: NavController
) {
    val movies = viewModel.movies
    val tvShows = viewModel.tvShows
    val recentHistory by viewModel.recentHistory.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val errorMessage = viewModel.errorMessage

    // Show error state with retry when no data loaded
    if (movies.isEmpty() && tvShows.isEmpty() && errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = NetflixRed.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = TextGrey,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.refreshAll() },
                    colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Retry", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }

    if (movies.isEmpty() && tvShows.isEmpty()) {
        // Shimmer loading state
        val shimmerBrush = rememberShimmerBrush()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item { ShimmerHeroBanner(brush = shimmerBrush) }
            item {
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .width(160.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(Modifier.height(12.dp))
            }
            item { ShimmerRow() }
            item {
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .width(160.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(Modifier.height(12.dp))
            }
            item { ShimmerRow() }
        }
        return
    }


    // Pull-to-refresh wrapper
    PullToRefreshBox(
        isRefreshing = viewModel.isRefreshing,
        onRefresh = { viewModel.refreshAll() },
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
    Column(modifier = Modifier.fillMaxSize()) {
        // --- Offline Banner ---
        AnimatedVisibility(
            visible = !isOnline,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OfflineBannerBg)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You're offline — showing cached content",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // --- Hero Pager (Top 5 Trending) ---
        val heroMovies = viewModel.trendingMovies.take(5).ifEmpty { movies.take(5) }
        if (heroMovies.isNotEmpty()) {
            item {
                HeroPager(movies = heroMovies, navController = navController)
            }
        }

        // --- Continue Watching Section ---
        if (recentHistory.isNotEmpty()) {
            item {
                SectionHeader(title = "Continue Watching")
            }
            item {
                ContinueWatchingRow(
                    items = recentHistory,
                    navController = navController
                )
            }
        }

        // --- Popular Movies Section ---

        if (movies.isNotEmpty()) {
            item {
                SectionHeader(title = "Popular Movies")
            }
            item {
                val movieListState = rememberLazyListState()
                val shouldLoadMoreMovies by remember {
                    derivedStateOf {
                        val last = movieListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        last >= movies.size - 3 && !viewModel.isLoadingMoreMovies
                    }
                }
                LaunchedEffect(shouldLoadMoreMovies) {
                    if (shouldLoadMoreMovies) viewModel.loadMoreMovies()
                }
                LazyRow(
                    state = movieListState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(movies, key = { it.id }) { movie ->
                        PosterCard(
                            posterPath = movie.poster_path,
                            title = movie.title,
                            rating = movie.vote_average,
                            onClick = { navController.navigate("movieDetail/${movie.id}") }
                        )
                    }
                    if (viewModel.isLoadingMoreMovies) {
                        item {
                            Box(
                                modifier = Modifier.width(60.dp).aspectRatio(2f / 3f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = com.example.movizapp.ui.theme.NetflixRed, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }

        // --- Popular TV Series Section ---
        if (tvShows.isNotEmpty()) {
            item {
                SectionHeader(title = "Popular TV Series")
            }
            item {
                val tvListState = rememberLazyListState()
                val shouldLoadMoreTv by remember {
                    derivedStateOf {
                        val last = tvListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        last >= tvShows.size - 3 && !viewModel.isLoadingMoreTvShows
                    }
                }
                LaunchedEffect(shouldLoadMoreTv) {
                    if (shouldLoadMoreTv) viewModel.loadMoreTvShows()
                }
                LazyRow(
                    state = tvListState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tvShows, key = { it.id }) { tvShow ->
                        PosterCard(
                            posterPath = tvShow.poster_path,
                            title = tvShow.name,
                            rating = tvShow.vote_average,
                            onClick = { navController.navigate("tvDetail/${tvShow.id}") }
                        )
                    }
                    if (viewModel.isLoadingMoreTvShows) {
                        item {
                            Box(
                                modifier = Modifier.width(60.dp).aspectRatio(2f / 3f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = com.example.movizapp.ui.theme.NetflixRed, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }

        // --- 🔥 Trending Movies ---
        if (viewModel.trendingMovies.isNotEmpty()) {
            item { SectionHeader(title = " Trending This Week") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.trendingMovies, key = { it.id }) { movie ->
                        PosterCard(posterPath = movie.poster_path, title = movie.title, rating = movie.vote_average,
                            onClick = { navController.navigate("movieDetail/${movie.id}") })
                    }
                }
            }
        }

        // --- ⭐ Top Rated Movies ---
        if (viewModel.topRatedMovies.isNotEmpty()) {
            item { SectionHeader(title = " Top Rated Movies") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.topRatedMovies, key = { it.id }) { movie ->
                        PosterCard(posterPath = movie.poster_path, title = movie.title, rating = movie.vote_average,
                            onClick = { navController.navigate("movieDetail/${movie.id}") })
                    }
                }
            }
        }

        // --- 🎬 Now Playing ---
        if (viewModel.nowPlayingMovies.isNotEmpty()) {
            item { SectionHeader(title = " Now Playing") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.nowPlayingMovies, key = { it.id }) { movie ->
                        PosterCard(posterPath = movie.poster_path, title = movie.title, rating = movie.vote_average,
                            onClick = { navController.navigate("movieDetail/${movie.id}") })
                    }
                }
            }
        }

        // --- 🔜 Coming Soon ---
        if (viewModel.upcomingMovies.isNotEmpty()) {
            item { SectionHeader(title = " Coming Soon") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.upcomingMovies, key = { it.id }) { movie ->
                        PosterCard(posterPath = movie.poster_path, title = movie.title, rating = movie.vote_average,
                            onClick = { navController.navigate("movieDetail/${movie.id}") })
                    }
                }
            }
        }

        // --- ⭐ Top Rated TV ---
        if (viewModel.topRatedTvShows.isNotEmpty()) {
            item { SectionHeader(title = " Top Rated TV Shows") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.topRatedTvShows, key = { it.id }) { tvShow ->
                        PosterCard(posterPath = tvShow.poster_path, title = tvShow.name, rating = tvShow.vote_average,
                            onClick = { navController.navigate("tvDetail/${tvShow.id}") })
                    }
                }
            }
        }

        // --- 🔥 Trending TV ---
        if (viewModel.trendingTvShows.isNotEmpty()) {
            item { SectionHeader(title = " Trending TV Shows") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.trendingTvShows, key = { it.id }) { tvShow ->
                        PosterCard(posterPath = tvShow.poster_path, title = tvShow.name, rating = tvShow.vote_average,
                            onClick = { navController.navigate("tvDetail/${tvShow.id}") })
                    }
                }
            }
        }

        // Bottom spacer
        item { Spacer(Modifier.height(16.dp)) }
    }
    } // end Column
    } // end PullToRefreshBox
}

@Composable
fun HeroPager(movies: List<Movie>, navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { movies.size })

    // Auto-scroll every 4 seconds
    LaunchedEffect(pagerState) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            val nextPage = (pagerState.currentPage + 1) % movies.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    val heroGradient = remember {
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, DarkBackground)
        )
    }

    Box(modifier = Modifier.fillMaxWidth().height(440.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            val movie = movies[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { navController.navigate("movieDetail/${movie.id}") }
            ) {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w780/${movie.poster_path}",
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Bottom gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.BottomCenter)
                        .background(heroGradient)
                )

                // Title overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 36.dp)
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldRating,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = remember(movie.vote_average) { String.format("%.1f", movie.vote_average) },
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = movie.release_date,
                            color = TextGrey,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Page indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(movies.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (pagerState.currentPage == index) NetflixRed
                            else PageIndicatorInactive
                        )
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = Color.White,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
    )
}

@Composable
fun PosterCard(
    posterPath: String?,
    title: String,
    rating: Double,
    onClick: () -> Unit
) {
    val bottomGradient = remember {
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
        )
    }
    val ratingBgColor = remember { Color.Black.copy(alpha = 0.75f) }
    val formattedRating = remember(rating) { String.format("%.1f", rating) }

    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(10.dp))
        ) {
            AsyncImage(
                model = if (posterPath != null) "https://image.tmdb.org/t/p/w185/$posterPath" else null,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Rating badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(
                        color = ratingBgColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldRating,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = formattedRating,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bottom gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(bottomGradient)
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
