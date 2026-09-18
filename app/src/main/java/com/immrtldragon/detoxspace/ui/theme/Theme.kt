package com.immrtldragon.detoxspace.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

private val LightColors = lightColorScheme(
    primary = Color(0xFF173B36),
    onPrimary = Color(0xFFFDFBF6),
    primaryContainer = Color(0xFFDCE9E3),
    onPrimaryContainer = Color(0xFF102C28),
    secondary = Color(0xFF9A4E3B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF7DFD7),
    onSecondaryContainer = Color(0xFF542619),
    tertiary = Color(0xFF657970),
    background = Color(0xFFF4F1EA),
    onBackground = Color(0xFF1D211F),
    surface = Color(0xFFFBF9F4),
    onSurface = Color(0xFF1D211F),
    surfaceVariant = Color(0xFFE8E5DE),
    onSurfaceVariant = Color(0xFF5F6662),
    outline = Color(0xFFC9CCC7),
    outlineVariant = Color(0xFFE0E1DC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB7D5C9),
    onPrimary = Color(0xFF08201C),
    primaryContainer = Color(0xFF214C44),
    onPrimaryContainer = Color(0xFFD6EEE5),
    secondary = Color(0xFFFFB5A1),
    onSecondary = Color(0xFF5D1F10),
    secondaryContainer = Color(0xFF713624),
    background = Color(0xFF101412),
    onBackground = Color(0xFFE7E9E4),
    surface = Color(0xFF171C19),
    onSurface = Color(0xFFE7E9E4),
    surfaceVariant = Color(0xFF252B28),
    onSurfaceVariant = Color(0xFFBFC7C2),
    outline = Color(0xFF87908B),
    outlineVariant = Color(0xFF383E3B),
)

private val DetoxTypography = Typography(
    headlineLarge = TextStyle(fontSize = 36.sp, lineHeight = 42.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.6).sp),
    headlineMedium = TextStyle(fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.35).sp),
    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
)

private val DetoxShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
)

@Composable
fun DetoxSpaceTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = DetoxTypography,
        shapes = DetoxShapes,
        content = content,
    )
}
