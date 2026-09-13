package com.immrtldragon.detoxspace.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
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

@Composable
fun DetoxSpaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}

