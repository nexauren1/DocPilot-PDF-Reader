package com.nexauren.docpilot.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F63D8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5EDFF),
    onPrimaryContainer = Color(0xFF173575),
    secondary = Color(0xFF7250E8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE7FF),
    onSecondaryContainer = Color(0xFF302064),
    tertiary = Color(0xFF16856B),
    background = Color(0xFFF5F7FC),
    onBackground = Color(0xFF18243A),
    surface = Color.White,
    onSurface = Color(0xFF18243A),
    surfaceVariant = Color(0xFFEAF0F8),
    onSurfaceVariant = Color(0xFF63718A),
    outline = Color(0xFFD5DDEB),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9AB7FF),
    onPrimary = Color(0xFF09295F),
    primaryContainer = Color(0xFF234A9F),
    onPrimaryContainer = Color(0xFFE0E8FF),
    secondary = Color(0xFFC4B0FF),
    onSecondary = Color(0xFF342067),
    secondaryContainer = Color(0xFF4B3680),
    onSecondaryContainer = Color(0xFFECE4FF),
    tertiary = Color(0xFF73D8BB),
    background = Color(0xFF0E1422),
    onBackground = Color(0xFFE4EAF6),
    surface = Color(0xFF171F30),
    onSurface = Color(0xFFE4EAF6),
    surfaceVariant = Color(0xFF252F43),
    onSurfaceVariant = Color(0xFFB3BED2),
    outline = Color(0xFF3E4A60),
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
