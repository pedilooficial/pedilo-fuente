package com.pedilo.app.ui.publicuser

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PediloOrange = Color(0xFFFF6A00)
val PediloOrangeDark = Color(0xFFB94400)
val PediloBg = Color(0xFF020B10)
val PediloPanel = Color(0xFF0B171D)
val PediloPanelSoft = Color(0xFF111F26)
val PediloLine = Color(0xFF263A43)
val PediloText = Color(0xFFF7F7F7)
val PediloMuted = Color(0xFFB4BEC3)
val PediloGreen = Color(0xFF69D125)
val PediloOrangeSoft = Color(0xFFFF8A2A)
val PediloWarning = Color(0xFFFFB54A)
val PediloOverlay = Color(0x66111F26)

private val PublicScheme = darkColorScheme(
    primary = PediloOrange,
    secondary = PediloOrange,
    background = PediloBg,
    surface = PediloPanel,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = PediloText,
    onSurface = PediloText,
)

@Composable
fun PublicTheme(content: @Composable () -> Unit) {
    val unused = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = PublicScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}
