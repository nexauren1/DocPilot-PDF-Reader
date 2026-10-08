package com.nexauren.docpilot.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B5CE2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E7FF),
    secondary = Color(0xFF6950A6),
    background = Color(0xFFF7F7FB),
    surface = Color.White,
    surfaceVariant = Color(0xFFECECF4),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9B8FF),
    onPrimary = Color(0xFF25255D),
    primaryContainer = Color(0xFF40408A),
    secondary = Color(0xFFD1BCFF),
    background = Color(0xFF111116),
    surface = Color(0xFF19191F),
    surfaceVariant = Color(0xFF2B2B34),
)

@Composable
fun DocPilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
