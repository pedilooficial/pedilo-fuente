package com.pedilo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PediloColors = darkColorScheme(
    primary = PediloOrange,
    onPrimary = PediloInk,
    primaryContainer = PediloOrangeDark,
    onPrimaryContainer = PediloText,
    background = PediloInk,
    onBackground = PediloText,
    surface = PediloPanel,
    onSurface = PediloText,
    surfaceVariant = PediloPanelSoft,
    onSurfaceVariant = PediloMuted,
    error = PediloDanger
)

@Composable
fun PediloTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PediloColors,
        typography = PediloTypography,
        content = content
    )
}
