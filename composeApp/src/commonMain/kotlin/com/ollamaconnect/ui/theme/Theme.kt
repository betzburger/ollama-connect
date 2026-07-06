package com.ollamaconnect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate50 = Color(0xFFF8FAFC)
val Slate400 = Color(0xFF94A3B8)

val VioletColor = Color(0xFF8B5CF6)
val IndigoColor = Color(0xFF6366F1)
val BlueColor = Color(0xFF3B82F6)
val CyanColor = Color(0xFF06B6D4)

private val DarkColorScheme = darkColorScheme(
    primary = IndigoColor,
    secondary = VioletColor,
    tertiary = CyanColor,
    background = Slate900,
    surface = Slate800,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Slate50,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate400,
    outline = Slate600
)

private val LightColorScheme = lightColorScheme(
    primary = BlueColor,
    secondary = IndigoColor,
    tertiary = CyanColor,
    background = Color(0xFFF1F5F9),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun OllamaConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
