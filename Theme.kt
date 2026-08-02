package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SatnaOrangeLight,
    onPrimary = Color.Black,
    primaryContainer = SatnaOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = SatnaTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF004D40),
    tertiary = SatnaAmberTertiary,
    background = SatnaDarkBackground,
    surface = SatnaDarkSurface,
    surfaceVariant = SatnaDarkSurfaceVariant,
    onBackground = Color(0xFFEEEEEE),
    onSurface = Color(0xFFF5F5F5)
)

private val LightColorScheme = lightColorScheme(
    primary = SatnaOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = SatnaOrangeContainer,
    onPrimaryContainer = SatnaOrangeDark,
    secondary = SatnaTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = SatnaTealContainer,
    tertiary = SatnaAmberTertiary,
    background = SatnaLightBackground,
    surface = SatnaLightSurface,
    surfaceVariant = SatnaLightSurfaceVariant,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun SatnaEatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
