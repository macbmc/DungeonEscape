package com.dungeonescape.engine

import com.dungeonescape.models.DungeonTheme
import com.dungeonescape.models.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GameStateManagerTest {

    @Test
    fun testSavedGameSessionDataModel() {
        val session = SavedGameSession(
            level = 8,
            score = 450,
            playerHealth = 85f,
            coins = 34,
            themeMode = ThemeMode.PROGRESSIVE,
            lockedTheme = DungeonTheme.HAUNTED_CRYPT
        )

        assertEquals(8, session.level)
        assertEquals(450, session.score)
        assertEquals(85f, session.playerHealth, 0.01f)
        assertEquals(34, session.coins)
        assertEquals(ThemeMode.PROGRESSIVE, session.themeMode)
        assertEquals(DungeonTheme.HAUNTED_CRYPT, session.lockedTheme)
    }
}
