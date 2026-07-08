package com.mason.milkteastatistics.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF8B4A1F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC2),
    onPrimaryContainer = Color(0xFF2E1500),
    secondary = Color(0xFF6F5B48),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFADEC5),
    onSecondaryContainer = Color(0xFF281806),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF201A17),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF201A17),
    surfaceVariant = Color(0xFFF2DFD1),
    onSurfaceVariant = Color(0xFF51443B),
    error = Color(0xFFBA1A1A),
    outline = Color(0xFF837469),
    outlineVariant = Color(0xFFD6C3B6),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB781),
    onPrimary = Color(0xFF4F2500),
    primaryContainer = Color(0xFF6F3608),
    onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = Color(0xFFDDC2A9),
    onSecondary = Color(0xFF3E2D1D),
    secondaryContainer = Color(0xFF564333),
    onSecondaryContainer = Color(0xFFFADEC5),
    background = Color(0xFF201A17),
    onBackground = Color(0xFFEDE0D8),
    surface = Color(0xFF201A17),
    onSurface = Color(0xFFEDE0D8),
    surfaceVariant = Color(0xFF51443B),
    onSurfaceVariant = Color(0xFFD6C3B6),
    error = Color(0xFFFFB4AB),
    outline = Color(0xFF9F8D80),
    outlineVariant = Color(0xFF51443B),
)

@Composable
fun MilkTeaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
