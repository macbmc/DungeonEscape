package com.dungeonescape.engine

import androidx.compose.ui.graphics.Color
import com.dungeonescape.models.DungeonTheme
import com.dungeonescape.models.ThemeAssets
import com.dungeonescape.models.ThemeMode

class ThemeManager {

    private val themeAssetsMap: Map<DungeonTheme, ThemeAssets> = mapOf(
        DungeonTheme.ANCIENT_RUINS to ThemeAssets(
            theme = DungeonTheme.ANCIENT_RUINS,
            wallColor = Color(0xFF2B2D2F),
            wallHighlightColor = Color(0xFF4A4E51),
            wallShadowColor = Color(0xFF1B1C1D),
            floorColor1 = Color(0xFF3D4143),
            floorColor2 = Color(0xFF45494C),
            floorGridLine = Color(0xFF26292B),
            torchLightTint = Color(0x33FFA726), // Warm orange torch
            ambientDarkColor = Color(0xFA0D0E11),
            particleAmbienceColor = Color(0xFFFFB74D)
        ),
        DungeonTheme.HAUNTED_CRYPT to ThemeAssets(
            theme = DungeonTheme.HAUNTED_CRYPT,
            wallColor = Color(0xFF1E2522),
            wallHighlightColor = Color(0xFF2E3D35),
            wallShadowColor = Color(0xFF121715),
            floorColor1 = Color(0xFF293830),
            floorColor2 = Color(0xFF31443A),
            floorGridLine = Color(0xFF18221D),
            torchLightTint = Color(0x3369F0AE), // Spectral green
            ambientDarkColor = Color(0xFA0A120E),
            particleAmbienceColor = Color(0xFFB9F6CA)
        ),
        DungeonTheme.FROZEN_CAVERNS to ThemeAssets(
            theme = DungeonTheme.FROZEN_CAVERNS,
            wallColor = Color(0xFF1A2634),
            wallHighlightColor = Color(0xFF2E435B),
            wallShadowColor = Color(0xFF101923),
            floorColor1 = Color(0xFF2A3B4E),
            floorColor2 = Color(0xFF33475E),
            floorGridLine = Color(0xFF1B2632),
            torchLightTint = Color(0x3300E5FF), // Crisp cyan frost
            ambientDarkColor = Color(0xFA08111A),
            particleAmbienceColor = Color(0xFF80D8FF)
        ),
        DungeonTheme.LAVA_FORTRESS to ThemeAssets(
            theme = DungeonTheme.LAVA_FORTRESS,
            wallColor = Color(0xFF331A1A),
            wallHighlightColor = Color(0xFF542525),
            wallShadowColor = Color(0xFF200F0F),
            floorColor1 = Color(0xFF462323),
            floorColor2 = Color(0xFF552B2B),
            floorGridLine = Color(0xFF271313),
            torchLightTint = Color(0x44FF3D00), // Blazing red-orange magma
            ambientDarkColor = Color(0xFA160A0A),
            particleAmbienceColor = Color(0xFFFF6E40)
        ),
        DungeonTheme.SHADOW_REALM to ThemeAssets(
            theme = DungeonTheme.SHADOW_REALM,
            wallColor = Color(0xFF1C1427),
            wallHighlightColor = Color(0xFF34234C),
            wallShadowColor = Color(0xFF100B17),
            floorColor1 = Color(0xFF281C39),
            floorColor2 = Color(0xFF332349),
            floorGridLine = Color(0xFF160E21),
            torchLightTint = Color(0x33D500F9), // Void purple aura
            ambientDarkColor = Color(0xFA0E0814),
            particleAmbienceColor = Color(0xFFEA80FC)
        )
    )

    fun getThemeForLevel(level: Int, mode: ThemeMode, lockedTheme: DungeonTheme = DungeonTheme.ANCIENT_RUINS): DungeonTheme {
        if (mode == ThemeMode.LOCKED) {
            return lockedTheme
        }
        return when {
            level in 1..5 -> DungeonTheme.ANCIENT_RUINS
            level in 6..10 -> DungeonTheme.HAUNTED_CRYPT
            level in 11..15 -> DungeonTheme.FROZEN_CAVERNS
            level in 16..20 -> DungeonTheme.LAVA_FORTRESS
            else -> DungeonTheme.SHADOW_REALM
        }
    }

    fun getAssets(theme: DungeonTheme): ThemeAssets {
        return themeAssetsMap[theme] ?: themeAssetsMap.values.first()
    }
}
