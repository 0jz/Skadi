package com.smiraj.meditation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Teal700,
    onPrimary = Sand,
    primaryContainer = Teal200,
    onPrimaryContainer = Ink,
    secondary = Sage,
    background = Sand,
    onBackground = Ink,
    surface = Sand,
    onSurface = Ink,
    surfaceVariant = Mist,
)

private val DarkColors = darkColorScheme(
    primary = Teal200,
    onPrimary = Ink,
    primaryContainer = Teal500,
    secondary = Sage,
    background = Ink,
    onBackground = Sand,
    surface = Ink,
    onSurface = Sand,
)

@Composable
fun SmirajTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
