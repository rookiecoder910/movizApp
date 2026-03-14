package com.example.movizapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.movizapp.auth.AuthViewModel
import com.example.movizapp.sync.FirestoreSyncManager
import com.example.movizapp.ui.theme.DarkBackground
import com.example.movizapp.ui.theme.DarkCard
import com.example.movizapp.ui.theme.DarkSurface
import com.example.movizapp.ui.theme.GoldRating
import com.example.movizapp.ui.theme.NetflixRed
import com.example.movizapp.ui.theme.TextGrey
import com.example.movizapp.viewmodel.MovieViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MovieViewModel,
    authViewModel: AuthViewModel,
    syncManager: FirestoreSyncManager,
    navController: NavController
) {
    val watchlist by viewModel.watchlist.collectAsState()
    val watchlistCount by viewModel.watchlistCount.collectAsState()
    val recentHistory by viewModel.recentHistory.collectAsState()
    val historyCount by viewModel.historyCount.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val isSignedIn = authState != null
    val scope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // --- Profile Header ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Profile Picture
                if (isSignedIn && authState?.photoUrl != null) {
                    AsyncImage(
                        model = authState?.photoUrl.toString(),
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(DarkCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSignedIn) authState?.displayName?.take(1)?.uppercase() ?: "M" else "M",
                            color = NetflixRed,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // User name
                Text(
                    text = if (isSignedIn) authState?.displayName ?: "User" else "Guest",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (isSignedIn) {
                    Text(
                        text = authState?.email ?: "",
                        color = TextGrey,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Auth Actions
                if (isSignedIn) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Sync button
                        OutlinedButton(
                            onClick = {
                                isSyncing = true
                                scope.launch {
                                    try { syncManager.syncWatchlist() } catch (_: Exception) {}
                                    isSyncing = false
                                }
                            },
                            enabled = !isSyncing,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = NetflixRed,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Syncing...", color = Color.White, fontSize = 12.sp)
                            } else {
                                Text("☁ Sync Watchlist", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        // Sign out
                        OutlinedButton(
                            onClick = { authViewModel.signOut() },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = TextGrey, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sign Out", color = TextGrey, fontSize = 12.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier.fillMaxWidth(0.7f).height(44.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Stats Row ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(label = "Watchlist", count = watchlistCount, icon = Icons.Default.Favorite)
                StatCard(label = "Watched", count = historyCount, icon = Icons.Default.Star)
            }
            Spacer(Modifier.height(16.dp))
        }

        // --- My Watchlist ---
        if (watchlist.isNotEmpty()) {
            item {
                SectionHeader(title = "My Watchlist")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(watchlist) { item ->
                        PosterCard(
                            posterPath = item.posterPath,
                            title = item.title,
                            rating = item.voteAverage,
                            onClick = {
                                if (item.mediaType == "movie") {
                                    navController.navigate("movieDetail/${item.tmdbId}")
                                } else {
                                    navController.navigate("tvDetail/${item.tmdbId}")
                                }
                            }
                        )
                    }
                }
            }
        }

        // --- Watch History ---
        if (recentHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Watch History",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    TextButton(onClick = { viewModel.clearWatchHistory() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = TextGrey, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear", color = TextGrey, fontSize = 12.sp)
                    }
                }
            }
            items(recentHistory) { item ->
                HistoryRow(
                    item = item,
                    onClick = {
                        if (item.mediaType == "movie") {
                            navController.navigate("movieDetail/${item.tmdbId}")
                        } else {
                            navController.navigate("tvDetail/${item.tmdbId}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = NetflixRed, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(text = "$count", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = TextGrey, fontSize = 12.sp)
        }
    }
}

@Composable
fun HistoryRow(
    item: com.example.movizapp.room.WatchHistoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (item.posterPath != null) "https://image.tmdb.org/t/p/w92${item.posterPath}" else null,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = buildString {
                    append(if (item.mediaType == "movie") "Movie" else "TV Show")
                    if (item.season != null) append(" · S${item.season} E${item.episode}")
                }
                Text(text = subtitle, color = TextGrey, fontSize = 12.sp)
            }
        }
    }
}
