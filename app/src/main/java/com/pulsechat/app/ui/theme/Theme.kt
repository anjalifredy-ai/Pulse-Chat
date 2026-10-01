package com.pulsechat.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PulseBlue,
    onPrimary = Color.White,
    primaryContainer = PulseBlue.copy(alpha = 0.12f),
    secondary = PulseGreen,
    onSecondary = Color.White,
    tertiary = PulseOrange,
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
    primaryContainer = PulseBlue.copy(alpha = 0.2f),
    secondary = PulseGreen,
    onSecondary = Color.Black,
    tertiary = PulseOrange,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = PulseRed,
    outline = DarkOnSurfaceVariant.copy(alpha = 0.4f)
)

@Composable
fun PulseChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
