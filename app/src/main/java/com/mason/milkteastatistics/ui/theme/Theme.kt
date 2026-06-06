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

// Warm, healing color palette for Milk Tea Statistics
// Light Theme Colors
private val MilkTeaBrown = Color(0xFFD4A574) // Primary - warm, inviting
private val CreamWhite = Color(0xFFFFF8E7) // Background - soft, gentle
private val MatchaGreen = Color(0xFF8FBC8F) // Accent - fresh, natural

// Derived colors for light theme
private val MilkTeaBrownDark = Color(0xFFB8956A) // Darker variant for text/contrast
private val MilkTeaBrownLight = Color(0xFFE8C9A8) // Lighter variant for containers
private val CreamWhiteDark = Color(0xFFF5E6D3) // Slightly darker for surfaces
private val MatchaGreenDark = Color(0xFF6B9B6B) // Darker variant for emphasis

// Dark Theme Colors
private val DarkBackground = Color(0xFF1A1410) // Deep warm dark
private val DarkSurface = Color(0xFF2D2520) // Slightly lighter surface
private val DarkMilkTeaBrown = Color(0xFFE8C9A8) // Lighter in dark mode for contrast
private val DarkMatchaGreen = Color(0xFFA8D4A8) // Lighter matcha for dark theme

private val LightColorScheme = lightColorScheme(
    primary = MilkTeaBrown,
    onPrimary = Color(0xFF4A3728), // Dark text on primary for contrast
    primaryContainer = MilkTeaBrownLight,
    onPrimaryContainer = Color(0xFF3D2E22),
    
    secondary = MatchaGreen,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC8E6C8),
    onSecondaryContainer = Color(0xFF1B3D1B),
    
    tertiary = MilkTeaBrownDark,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC4),
    onTertiaryContainer = Color(0xFF2D1F15),
    
    background = CreamWhite,
    onBackground = Color(0xFF3D2E22),
    
    surface = CreamWhite,
    onSurface = Color(0xFF3D2E22),
    surfaceVariant = CreamWhiteDark,
    onSurfaceVariant = Color(0xFF5D4E42),
    
    outline = Color(0xFF8B7B6F),
    outlineVariant = Color(0xFFD4C4B8),
    
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    inverseSurface = Color(0xFF3D2E22),
    inverseOnSurface = Color(0xFFFFF4ED),
    inversePrimary = Color(0xFFE8C9A8),
    
    scrim = Color(0xFF000000),
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkMilkTeaBrown,
    onPrimary = Color(0xFF4A3728),
    primaryContainer = MilkTeaBrownDark,
    onPrimaryContainer = Color(0xFFFFDCC4),
    
    secondary = DarkMatchaGreen,
    onSecondary = Color(0xFF1B3D1B),
    secondaryContainer = MatchaGreenDark,
    onSecondaryContainer = Color(0xFFC8E6C8),
    
    tertiary = Color(0xFFE8C9A8),
    onTertiary = Color(0xFF2D1F15),
    tertiaryContainer = MilkTeaBrownDark,
    onTertiaryContainer = Color(0xFFFFDCC4),
    
    background = DarkBackground,
    onBackground = Color(0xFFF5E6D3),
    
    surface = DarkSurface,
    onSurface = Color(0xFFF5E6D3),
    surfaceVariant = Color(0xFF3D3530),
    onSurfaceVariant = Color(0xFFD4C4B8),
    
    outline = Color(0xFF9D8D81),
    outlineVariant = Color(0xFF5D4E42),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    inverseSurface = Color(0xFFF5E6D3),
    inverseOnSurface = Color(0xFF3D2E22),
    inversePrimary = MilkTeaBrown,
    
    scrim = Color(0xFF000000),
)

@Composable
fun MilkTeaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= 31 -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
