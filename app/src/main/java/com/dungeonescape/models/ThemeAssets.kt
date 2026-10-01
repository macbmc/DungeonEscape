package com.dungeonescape.models

import androidx.compose.ui.graphics.Color

data class ThemeAssets(
    val theme: DungeonTheme,
    val wallColor: Color,
    val wallHighlightColor: Color,
    val wallShadowColor: Color,
    val floorColor1: Color,
    val floorColor2: Color,
    val floorGridLine: Color,
    val torchLightTint: Color,
    val ambientDarkColor: Color,
    val particleAmbienceColor: Color
)
