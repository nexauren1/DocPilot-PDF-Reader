package com.nexauren.docpilot.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF315DEB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7ECFF),
    onPrimaryContainer = Color(0xFF172F8A),
    secondary = Color(0xFF7652E8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0E9FF),
    onSecondaryContainer = Color(0xFF38206F),
    tertiary = Color(0xFF0F8A72),
    onTertiary = Color.White,
    error = Color(0xFFB4233A),
    background = Color(0xFFF5F7FD),
    onBackground = Color(0xFF17213B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17213B),
    surfaceVariant = Color(0xFFEDF0F8),
    onSurfaceVariant = Color(0xFF66728B),
    outline = Color(0xFFD7DEED),
    outlineVariant = Color(0xFFE5E9F2),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB2C2FF),
    onPrimary = Color(0xFF142B83),
    primaryContainer = Color(0xFF2D469F),
    onPrimaryContainer = Color(0xFFE2E8FF),
    secondary = Color(0xFFD0BFFF),
    onSecondary = Color(0xFF38246F),
    secondaryContainer = Color(0xFF503B87),
    onSecondaryContainer = Color(0xFFF0E9FF),
    tertiary = Color(0xFF74D9BE),
    onTertiary = Color(0xFF00382E),
    error = Color(0xFFFFB4BC),
    background = Color(0xFF0C1120),
    onBackground = Color(0xFFE9EDFA),
    surface = Color(0xFF131B2D),
    onSurface = Color(0xFFE9EDFA),
    surfaceVariant = Color(0xFF202A40),
    onSurfaceVariant = Color(0xFFB4BED4),
    outline = Color(0xFF46516A),
    outlineVariant = Color(0xFF303B52),
)

private val DocPilotTypography = Typography(
    displayLarge = TextStyle(fontSize = 36.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.1).sp),
    headlineLarge = TextStyle(fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 25.sp, lineHeight = 31.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun DocPilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = DocPilotTypography,
        content = content,
    )
}
