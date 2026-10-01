package com.dungeonescape.utils

import com.dungeonescape.entities.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameBalanceTest {

    @Test
    fun testPlayerSurvivabilityLevel1Hits() {
        val maxHp = GameBalance.MAX_PLAYER_HEALTH
        val skeletonDmg = GameBalance.SKELETON_BASE_DAMAGE

        val hitsToDie = (maxHp / skeletonDmg).toInt()
        assertTrue(
            "Player must survive 8-10 hits in Level 1 (current hits: $hitsToDie)",
            hitsToDie in 8..10
        )
    }

    @Test
    fun testInvulnerabilityWindow() {
        assertEquals(
            "Invulnerability time must be 0.85s (850ms)",
            0.85f,
            GameBalance.PLAYER_DAMAGE_INVULNERABILITY_TIME,
            0.001f
        )
    }

    @Test
    fun testLevelCompletionRecovery() {
        assertEquals(
            "Level complete heal must be +10 HP",
            10f,
            GameBalance.LEVEL_COMPLETE_HEAL,
            0.001f
        )
    }

    @Test
    fun testPotionRecoveryValues() {
        assertEquals(20f, GameBalance.SMALL_WORLD_POTION_HEAL, 0.001f)
        assertEquals(30f, GameBalance.SMALL_SECRET_POTION_HEAL, 0.001f)
        assertEquals(70f, GameBalance.LARGE_SECRET_POTION_HEAL, 0.001f)
        assertEquals(30, GameBalance.SMALL_POTION_COST)
        assertEquals(65, GameBalance.LARGE_POTION_COST)
    }

    @Test
    fun testPlayerHealingClampsAtMax() {
        val player = Player(health = 90f)
        player.heal(20f)
        assertEquals(GameBalance.MAX_PLAYER_HEALTH, player.health, 0.001f)
    }
}
