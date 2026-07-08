package com.mason.milkteastatistics.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ColorSchemeMode

@Composable
fun MilkTeaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val controller = ThemeController(
        colorSchemeMode = if (dynamicColor) {
            ColorSchemeMode.MonetSystem
        } else if (darkTheme) {
            ColorSchemeMode.Dark
        } else {
            ColorSchemeMode.Light
        }
    )
    
    MiuixTheme(controller = controller) {
        val miuixColors = MiuixTheme.colorScheme
        val materialColors = if (darkTheme) {
            darkColorScheme(
                primary = miuixColors.primary,
                onPrimary = Color.White,
                primaryContainer = miuixColors.primary.copy(alpha = 0.24f),
                onPrimaryContainer = miuixColors.primary,
                secondary = miuixColors.primary,
                onSecondary = Color.White,
                secondaryContainer = miuixColors.primary.copy(alpha = 0.18f),
                onSecondaryContainer = miuixColors.primary,
                surface = miuixColors.surface,
                onSurface = miuixColors.onSurface,
                surfaceVariant = miuixColors.surface,
                onSurfaceVariant = miuixColors.onSurfaceVariantSummary,
                error = miuixColors.error,
                outline = miuixColors.outline,
                outlineVariant = miuixColors.outline.copy(alpha = 0.56f),
            )
        } else {
            lightColorScheme(
                primary = miuixColors.primary,
                onPrimary = Color.White,
                primaryContainer = miuixColors.primary.copy(alpha = 0.16f),
                onPrimaryContainer = miuixColors.primary,
                secondary = miuixColors.primary,
                onSecondary = Color.White,
                secondaryContainer = miuixColors.primary.copy(alpha = 0.12f),
                onSecondaryContainer = miuixColors.primary,
                surface = miuixColors.surface,
                onSurface = miuixColors.onSurface,
                surfaceVariant = miuixColors.surface,
                onSurfaceVariant = miuixColors.onSurfaceVariantSummary,
                error = miuixColors.error,
                outline = miuixColors.outline,
                outlineVariant = miuixColors.outline.copy(alpha = 0.56f),
            )
        }

        MaterialTheme(
            colorScheme = materialColors,
            content = content,
        )
    }
}
