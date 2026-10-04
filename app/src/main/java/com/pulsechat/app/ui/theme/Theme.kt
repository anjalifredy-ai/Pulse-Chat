package com.pulsechat.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PulseBlue,
    onPrimary = Color.White,
    primaryContainer = PulseBlue.copy(alpha = 0.12f),
    secondary = PulsePurple,
    onSecondary = Color.White,
    tertiary = PulsePink,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = PulseRed,
    outline = LightOnSurfaceVariant.copy(alpha = 0.4f)
)

private val DarkColorScheme = darkColorScheme(
    primary = PulseBlue,
    onPrimary = Color.White,
    primaryContainer = PulsePurple.copy(alpha = 0.25f),
    secondary = PulsePurple,
    onSecondary = Color.White,
    tertiary = PulsePink,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = PulseRed,
    outline = DarkOnSurfaceVariant.copy(alpha = 0.35f)
)

@Composable
fun PulseChatTheme(
    darkTheme: Boolean = true, // premium dark by default
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
