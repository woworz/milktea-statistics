package com.mason.milkteastatistics.ui.theme

import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ColorSchemeMode

@Composable
fun MilkTeaTheme(
    darkTheme: Boolean = false,
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
    
    MiuixTheme(
        controller = controller,
        content = content
    )
}
