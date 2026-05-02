package com.example.movizapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ShimmerColors = listOf(
    Color(0xFF1A1A1A),
    Color(0xFF2A2A2A),
    Color(0xFF1A1A1A)
)

@Composable
fun ShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    return Brush.linearGradient(
        colors = ShimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )
}

@Composable
fun ShimmerPosterCard(modifier: Modifier = Modifier) {
    val brush = ShimmerBrush()
    Column(modifier = modifier.width(140.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(10.dp))
                .background(brush)
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
    }
}

@Composable
fun ShimmerHeroBanner() {
    val brush = ShimmerBrush()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(brush)
    )
}

@Composable
fun ShimmerRow() {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            ShimmerPosterCard()
        }
    }
}

@Composable
fun ShimmerSearchResult() {
    val brush = ShimmerBrush()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun ShimmerDetailScreen() {
    val brush = ShimmerBrush()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // Backdrop shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(brush)
        )
        // Content shimmers
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            // Title
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(12.dp))
            // Rating + date
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
            Spacer(Modifier.height(20.dp))
            // Watch button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(20.dp))
            // Genre chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(brush)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            // Overview header
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(12.dp))
            // Overview lines
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (it == 3) 0.6f else 1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

