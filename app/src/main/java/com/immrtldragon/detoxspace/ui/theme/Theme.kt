package com.immrtldragon.detoxspace.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = lightColorScheme(
    primary = Color(0xFF355E4B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E8DD),
    onPrimaryContainer = Color(0xFF17382A),
    secondary = Color(0xFF655B3E),
    secondaryContainer = Color(0xFFF0E5BC),
    background = Color(0xFFF7F5EF),
    surface = Color(0xFFF7F5EF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFAED0BA),
    onPrimary = Color(0xFF163729),
    primaryContainer = Color(0xFF294F3D),
    secondaryContainer = Color(0xFF514A32),
)

@Composable
fun DetoxSpaceTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else Colors, content = content)
}
