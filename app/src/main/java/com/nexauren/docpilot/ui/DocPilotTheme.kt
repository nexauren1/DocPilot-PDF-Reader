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
    primary = Color(0xFF5146E5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE7E8FF),
    onPrimaryContainer = Color(0xFF24205D),
    secondary = Color(0xFF0F8F8A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD8F5F1),
    onSecondaryContainer = Color(0xFF0A4240),
    tertiary = Color(0xFFB34CCB),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF8E3FC),
    onTertiaryContainer = Color(0xFF53225C),
    background = Color(0xFFF5F6FC),
    onBackground = Color(0xFF171B2C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171B2C),
    surfaceVariant = Color(0xFFEAECF6),
    onSurfaceVariant = Color(0xFF626B86),
    outline = Color(0xFFD3D7E7),
    outlineVariant = Color(0xFFE1E4F0),
    error = Color(0xFFB42318),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE4E2),
    onErrorContainer = Color(0xFF7A271A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9B6FF),
    onPrimary = Color(0xFF21195D),
    primaryContainer = Color(0xFF302A6B),
    onPrimaryContainer = Color(0xFFE5E3FF),
    secondary = Color(0xFF72DAD0),
    onSecondary = Color(0xFF073B38),
    secondaryContainer = Color(0xFF174B4A),
    onSecondaryContainer = Color(0xFFCEFFF8),
    tertiary = Color(0xFFE8A8F5),
    onTertiary = Color(0xFF49280F),
    tertiaryContainer = Color(0xFF52285E),
    onTertiaryContainer = Color(0xFFFFDFFC),
    background = Color(0xFF0E1222),
    onBackground = Color(0xFFEEF0FF),
    surface = Color(0xFF171C30),
    onSurface = Color(0xFFEEF0FF),
    surfaceVariant = Color(0xFF242942),
    onSurfaceVariant = Color(0xFFBBC1DE),
    outline = Color(0xFF424964),
    outlineVariant = Color(0xFF303650),
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
