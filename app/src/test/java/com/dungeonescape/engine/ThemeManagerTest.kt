package com.dungeonescape.engine

import com.dungeonescape.models.DungeonTheme
import com.dungeonescape.models.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemeManagerTest {

    private val themeManager = ThemeManager()

    @Test
    fun testProgressiveThemeSelectionByLevel() {
        assertEquals(DungeonTheme.ANCIENT_RUINS, themeManager.getThemeForLevel(1, ThemeMode.PROGRESSIVE))
        assertEquals(DungeonTheme.ANCIENT_RUINS, themeManager.getThemeForLevel(5, ThemeMode.PROGRESSIVE))
        assertEquals(DungeonTheme.HAUNTED_CRYPT, themeManager.getThemeForLevel(6, ThemeMode.PROGRESSIVE))
        assertEquals(DungeonTheme.HAUNTED_CRYPT, themeManager.getThemeForLevel(10, ThemeMode.PROGRESSIVE))
        assertEquals(DungeonTheme.FROZEN_CAVERNS, themeManager.getThemeForLevel(11, ThemeMode.PROGRESSIVE))
        assertEquals(DungeonTheme.FROZEN_CAVERNS, themeManager.getThemeForLevel(15, ThemeMode.PROGRESSIVE))
        assertEquals(DungeonTheme.LAVA_FORTRESS, themeManager.getThemeForLevel(16, ThemeMode.PROGRESSIVE))
        assertEquals(DungeonTheme.LAVA_FORTRESS, themeManager.getThemeForLevel(20, ThemeMode.PROGRESSIVE))
        assertEquals(DungeonTheme.SHADOW_REALM, themeManager.getThemeForLevel(21, ThemeMode.PROGRESSIVE))
        assertEquals(DungeonTheme.SHADOW_REALM, themeManager.getThemeForLevel(50, ThemeMode.PROGRESSIVE))
    }

    @Test
    fun testLockedThemeSelection() {
        val chosen = DungeonTheme.FROZEN_CAVERNS
        assertEquals(chosen, themeManager.getThemeForLevel(1, ThemeMode.LOCKED, chosen))
        assertEquals(chosen, themeManager.getThemeForLevel(25, ThemeMode.LOCKED, chosen))
    }

    @Test
    fun testThemeAssetsPreloading() {
        for (theme in DungeonTheme.values()) {
            val assets = themeManager.getAssets(theme)
            assertNotNull(assets)
            assertEquals(theme, assets.theme)
        }
    }
}
