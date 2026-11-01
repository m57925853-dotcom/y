package com.focusquest.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val Gold = Color(0xFFFFC857)

private val Scheme = darkColorScheme(
    primary = Color(0xFF8B7CFF),
    onPrimary = Color.White,
    secondary = Gold,
    background = Color(0xFF0F0E1A),
    onBackground = Color(0xFFEDEBFF),
    surface = Color(0xFF1A1830),
    onSurface = Color(0xFFEDEBFF),
    surfaceVariant = Color(0xFF26234A),
    onSurfaceVariant = Color(0xFFC9C5F0)
)

@Composable
fun FocusTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = Scheme, content = content)
    }
}
