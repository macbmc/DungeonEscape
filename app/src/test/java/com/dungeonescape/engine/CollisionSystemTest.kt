package com.dungeonescape.engine

import com.dungeonescape.entities.Coin
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.Player
import com.dungeonescape.entities.Skeleton
import com.dungeonescape.models.Tile
import com.dungeonescape.models.TileType
import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollisionSystemTest {

    private val collisionSystem = CollisionSystem()

    @Test
    fun testEntityWallCollisionSliding() {
        val size = 5
        val tiles = Array(size) { y ->
            Array(size) { x ->
                if (x == 0 || y == 0 || x == size - 1 || y == size - 1) {
                    Tile(x, y, TileType.WALL)
                } else {
                    Tile(x, y, TileType.FLOOR)
                }
            }
        }

        val startPos = Vector2D(1.5f, 1.5f)
        // Move towards top-left wall diagonally
        val vel = Vector2D(-10f, 0f)
        val nextPos = collisionSystem.resolveEntityWallCollision(
            currentPos = startPos,
            velocity = vel,
            deltaTime = 0.1f,
            radius = 0.35f,
            tiles = tiles
        )

        // Must not penetrate left wall (x=0)
        assertTrue("Entity position X should be >= 1.0", nextPos.x >= 1.0f)
    }

    @Test
    fun testCoinPickupCollision() {
        val playerPos = Vector2D(2.0f, 2.0f)
        val coins = listOf(
            Coin(0, Vector2D(2.1f, 2.0f)), // Close -> pickup
            Coin(1, Vector2D(8.0f, 8.0f))  // Far -> no pickup
        )

        var collectedCount = 0
        collisionSystem.checkCoinPickups(playerPos, coins) {
            collectedCount++
        }

        assertEquals(1, collectedCount)
        assertTrue(coins[0].isCollected)
        assertFalse(coins[1].isCollected)
    }

    @Test
    fun testKeyAndPortalInteraction() {
        val playerPos = Vector2D(5.0f, 5.0f)
        val key = KeyItem(Vector2D(5.2f, 5.1f))
        var keyCollected = false

        collisionSystem.checkKeyPickup(playerPos, key) {
            keyCollected = true
        }
        assertTrue("Key should be collected", keyCollected)

        val portal = ExitPortal(Vector2D(5.0f, 5.0f), isActive = false)
        assertFalse("Inactive portal should not trigger level clear", collisionSystem.checkPortalEntry(playerPos, portal))

        portal.isActive = true
        assertTrue("Active portal within radius should trigger level clear", collisionSystem.checkPortalEntry(playerPos, portal))
    }

    @Test
    fun testPlayerAttackCollision() {
        val player = Player(
            position = Vector2D(3.0f, 3.0f),
            facingDirection = Vector2D.RIGHT,
            isAttacking = true
        )

        val inFrontSkeleton = Skeleton(0, Vector2D(3.8f, 3.0f))
        val behindSkeleton = Skeleton(1, Vector2D(2.0f, 3.0f))
        val skeletons = listOf(inFrontSkeleton, behindSkeleton)

        val hitIds = mutableListOf<Int>()
        collisionSystem.checkPlayerAttack(player, skeletons) { skeleton, _ ->
            hitIds.add(skeleton.id)
        }

        assertTrue("Enemy in front should be hit", hitIds.contains(0))
        assertFalse("Enemy behind should not be hit by frontal arc", hitIds.contains(1))
    }
}
