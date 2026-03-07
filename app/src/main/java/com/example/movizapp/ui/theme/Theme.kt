package com.example.movizapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Always dark — streaming apps don't have light mode
private val StreamingColorScheme = darkColorScheme(
    primary = NetflixRed,
    onPrimary = Color.White,
    primaryContainer = NetflixRedDark,
    onPrimaryContainer = Color.White,
    secondary = TextGrey,
    onSecondary = Color.White,
    secondaryContainer = DarkElevated,
    onSecondaryContainer = TextWhite,
    tertiary = GoldRating,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextGrey,
    inversePrimary = NetflixRed,
    outline = DarkElevated,
    outlineVariant = DarkCard,
)

@Composable
fun MovizAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = StreamingColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}