package com.tripdm.agency.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DeepNavy,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = M3PrimaryContainer,
    secondary = TextSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = M3SecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DeepNavy,
    onSurface = DarkTextWhite,
    onBackground = DarkTextWhite,
    outline = SlateGray,
    error = M3Error
)

private val LightColorScheme = lightColorScheme(
    primary = M3Primary,
    onPrimary = M3OnPrimary,
    primaryContainer = M3PrimaryContainer,
    onPrimaryContainer = DeepNavy,
    secondary = M3Secondary,
    secondaryContainer = M3SecondaryContainer,
    onSecondaryContainer = DeepNavy,
    background = M3Background,
    surface = M3Surface,
    surfaceVariant = M3SurfaceVariant,
    onSurface = M3OnSurface,
    onBackground = M3OnSurface,
    outline = M3Outline,
    error = M3Error
)

@Composable
fun TripDMAgencyTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
