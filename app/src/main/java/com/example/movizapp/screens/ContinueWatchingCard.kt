package com.example.movizapp.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.movizapp.room.WatchHistoryItem
import com.example.movizapp.ui.theme.GoldRating
import com.example.movizapp.ui.theme.NetflixRed

// ──────────────────────────────────────────────────────────────────────────────
// Stable file-level constants — avoids per-recomposition allocations
// ──────────────────────────────────────────────────────────────────────────────
private val CwCardOverlayGradient = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Color(0xE6000000)),
    startY = 100f
)
private val CwRatingBadgeBg   = Color(0xCC000000)   // 80% opaque black
private val CwPlayOverlayBg   = Color(0x66000000)   // 40% opaque black
private val CwProgressTrack   = Color(0xFF3A3A3A)   // dark grey track
private val CwTitleColor      = Color(0xFFF0F0F0)
private val CwSubtitleColor   = Color(0xFF9E9E9E)

// ──────────────────────────────────────────────────────────────────────────────
// ContinueWatchingRow — drop-in replacement for the section in MovieScreen
// ──────────────────────────────────────────────────────────────────────────────
@Composable
fun ContinueWatchingRow(
    items: List<WatchHistoryItem>,
    navController: NavController
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(items, key = { it.id }) { historyItem ->
            val onClick = remember(historyItem.tmdbId, historyItem.mediaType) {
                {
                    if (historyItem.mediaType == "movie") {
                        navController.navigate("movieDetail/${historyItem.tmdbId}")
                    } else {
                        navController.navigate("tvDetail/${historyItem.tmdbId}")
                    }
                }
            }
            ContinueWatchingCard(item = historyItem, onClick = onClick)
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// ContinueWatchingCard
// ──────────────────────────────────────────────────────────────────────────────
@Composable
fun ContinueWatchingCard(
    item: WatchHistoryItem,
    onClick: () -> Unit
) {
    // Scale animation on press — smooth 150ms tap feedback
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "cardScale"
    )

    val formattedRating = remember(item.rating) {
        if (item.rating > 0) String.format("%.1f", item.rating) else null
    }

    // Subtitle text — "Movie" / "TV · S1 E3"
    val subtitle = remember(item.mediaType, item.season, item.episode) {
        if (item.mediaType == "tv" && item.season != null && item.episode != null) {
            "S${item.season} · E${item.episode}"
        } else {
            "Movie"
        }
    }

    Column(
        modifier = Modifier
            .width(148.dp)
            .scale(scale)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        // ── Card: Poster + Overlays ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .shadow(
                    elevation = if (isPressed) 2.dp else 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .clip(RoundedCornerShape(12.dp))
        ) {
            // ── Poster image ─────────────────────────────────────────────────
            AsyncImage(
                model = if (item.posterPath != null)
                    "https://image.tmdb.org/t/p/w342${item.posterPath}"
                else null,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // ── Bottom gradient overlay (title readability) ───────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = CwCardOverlayGradient)
            )

            // ── Play button overlay (center) ──────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(42.dp)
                    .background(color = CwPlayOverlayBg, shape = RoundedCornerShape(50))
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(26.dp)
                )
            }

            // ── Rating badge (top-right) ──────────────────────────────────────
            if (formattedRating != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(color = CwRatingBadgeBg, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldRating,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = formattedRating,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Episode/Type badge (top-left) ─────────────────────────────────
            if (item.mediaType == "tv" && item.season != null) {
                Text(
                    text = subtitle,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(color = NetflixRed, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }

            // ── Progress bar (bottom) ─────────────────────────────────────────
            if (item.watchProgress > 0f) {
                LinearProgressIndicator(
                    progress = { item.watchProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = NetflixRed,
                    trackColor = CwProgressTrack,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp
                )
            }
        }

        // ── Title + sub info below the card ────────────────────────────────
        Spacer(Modifier.height(7.dp))
        Text(
            text = item.title,
            color = CwTitleColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        if (item.watchProgress > 0f) {
            val pct = remember(item.watchProgress) { (item.watchProgress * 100).toInt() }
            Text(
                text = "$pct% watched",
                color = CwSubtitleColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        } else if (item.mediaType == "tv" && item.season != null) {
            Text(
                text = subtitle,
                color = CwSubtitleColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
