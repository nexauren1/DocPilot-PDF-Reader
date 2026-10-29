package com.nexauren.docpilot.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val DocPilotTypography = Typography().let { base ->
    base.copy(
        displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val DocPilotShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(11.dp),
    medium = RoundedCornerShape(15.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF244F91),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE8F8),
    onPrimaryContainer = Color(0xFF142E55),
    secondary = Color(0xFF2C7378),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD8EEED),
    onSecondaryContainer = Color(0xFF123D40),
    tertiary = Color(0xFF9A633A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF5E4D8),
    onTertiaryContainer = Color(0xFF492B18),
    background = Color(0xFFF3F5F8),
    onBackground = Color(0xFF192434),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF192434),
    surfaceVariant = Color(0xFFE9EDF3),
    onSurfaceVariant = Color(0xFF5D6B80),
    outline = Color(0xFFD4DCE7),
    outlineVariant = Color(0xFFE5EAF1),
    error = Color(0xFFB42318),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE4E2),
    onErrorContainer = Color(0xFF7A271A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA7C4FF),
    onPrimary = Color(0xFF0D2852),
    primaryContainer = Color(0xFF1E3E73),
    onPrimaryContainer = Color(0xFFDDE8FF),
    secondary = Color(0xFF8BD0D0),
    onSecondary = Color(0xFF073B3E),
    secondaryContainer = Color(0xFF205356),
    onSecondaryContainer = Color(0xFFD4F5F4),
    tertiary = Color(0xFFEAB58F),
    onTertiary = Color(0xFF49280F),
    tertiaryContainer = Color(0xFF68452C),
    onTertiaryContainer = Color(0xFFFFE6D4),
    background = Color(0xFF101722),
    onBackground = Color(0xFFE7ECF4),
    surface = Color(0xFF182231),
    onSurface = Color(0xFFE7ECF4),
    surfaceVariant = Color(0xFF242F40),
    onSurfaceVariant = Color(0xFFB1BDCE),
    outline = Color(0xFF435064),
    outlineVariant = Color(0xFF303C4E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun DocPilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = DocPilotTypography,
        shapes = DocPilotShapes,
        content = content,
    )
}
