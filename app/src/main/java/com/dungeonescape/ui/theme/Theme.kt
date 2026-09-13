package com.dungeonescape.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DungeonPrimary,
    secondary = DungeonAccent,
    tertiary = DungeonGold,
    background = DungeonDark,
    surface = DungeonSurface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = DungeonDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun DungeonEscapeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
