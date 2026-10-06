package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ClockDarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = AmberAccent,
    onSecondary = Color.Black,
    tertiary = EmeraldAccent,
    onTertiary = Color.Black,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = Slate600,
    outlineVariant = Slate700
)

@Composable
fun MyApplicationTheme(
    accentColor: Color = CyanAccent,
    content: @Composable () -> Unit
) {
    val dynamicTheme = ClockDarkColorScheme.copy(
        primary = accentColor
    )
    MaterialTheme(
        colorScheme = dynamicTheme,
        typography = Typography,
        content = content
    )
}
