package com.dungeonescape.engine

import com.dungeonescape.entities.Coin
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.MerchantAltar
import com.dungeonescape.entities.Player
import com.dungeonescape.entities.Skeleton
import com.dungeonescape.entities.TrapSpike
import com.dungeonescape.entities.TreasureChest
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
        val vel = Vector2D(-10f, 0f)
        val nextPos = collisionSystem.resolveEntityWallCollision(
            currentPos = startPos,
            velocity = vel,
            deltaTime = 0.1f,
            radius = 0.35f,
            tiles = tiles
        )

        assertTrue("Entity position X should be >= 1.0", nextPos.x >= 1.0f)
    }

    @Test
    fun testSecretWallAttackAndBreaking() {
        val size = 5
        val tiles = Array(size) { y ->
            Array(size) { x ->
                Tile(x, y, TileType.FLOOR)
            }
        }
        tiles[2][3] = Tile(3, 2, TileType.SECRET_WALL, secretWallHits = 3)

        val player = Player(
            position = Vector2D(2.2f, 2.5f),
            facingDirection = Vector2D.RIGHT,
            isAttacking = true
        )

        // Hit 1
        var broken = false
        collisionSystem.checkSecretWallAttack(player, tiles) { _, isB -> broken = isB }
        assertEquals(2, tiles[2][3].secretWallHits)
        assertFalse(broken)
        assertEquals(TileType.SECRET_WALL, tiles[2][3].type)

        // Hit 2
        collisionSystem.checkSecretWallAttack(player, tiles) { _, isB -> broken = isB }
        assertEquals(1, tiles[2][3].secretWallHits)
        assertFalse(broken)

        // Hit 3 -> Breaks!
        collisionSystem.checkSecretWallAttack(player, tiles) { _, isB -> broken = isB }
        assertEquals(0, tiles[2][3].secretWallHits)
        assertTrue(broken)
        assertEquals(TileType.FLOOR, tiles[2][3].type)
    }

    @Test
    fun testChestInteraction() {
        val playerPos = Vector2D(3.0f, 3.0f)
        val chest = TreasureChest(id = 1, position = Vector2D(3.2f, 3.1f), coinReward = 40)
        val chests = listOf(chest)

        var coinsReceived = 0
        collisionSystem.checkChestInteraction(playerPos, chests) { _, reward ->
            coinsReceived = reward
        }

        assertEquals(40, coinsReceived)
        assertTrue(chest.isOpen)

        // Second check should not re-trigger
        coinsReceived = 0
        collisionSystem.checkChestInteraction(playerPos, chests) { _, reward ->
            coinsReceived = reward
        }
        assertEquals(0, coinsReceived)
    }

    @Test
    fun testMerchantAltarProximity() {
        val altar = MerchantAltar(Vector2D(5.0f, 5.0f))
        val nearPlayer = Vector2D(5.5f, 5.2f)
        val farPlayer = Vector2D(10.0f, 10.0f)

        assertTrue(collisionSystem.checkMerchantProximity(nearPlayer, altar))
        assertFalse(collisionSystem.checkMerchantProximity(farPlayer, altar))
    }

    @Test
    fun testTrapSpikesCollision() {
        val player = Player(position = Vector2D(4.0f, 4.0f), isDashing = false)
        val trap = TrapSpike(id = 1, position = Vector2D(4.1f, 4.0f), isExtended = true)
        val traps = listOf(trap)

        var trapDamage = 0f
        collisionSystem.checkTrapCollisions(player, traps) { _, dmg ->
            trapDamage = dmg
        }
        assertTrue("Extended trap should deal damage", trapDamage > 0f)

        // Retracted trap
        trap.isExtended = false
        trapDamage = 0f
        collisionSystem.checkTrapCollisions(player, traps) { _, dmg ->
            trapDamage = dmg
        }
        assertEquals("Retracted trap should not deal damage", 0f, trapDamage)
    }

    @Test
    fun testEnemySeparationForce() {
        val s1 = Skeleton(1, position = Vector2D(5.0f, 5.0f))
        val s2 = Skeleton(2, position = Vector2D(5.05f, 5.0f)) // Overlapping
        val skeletons = listOf(s1, s2)

        val initialDist = (s1.position - s2.position).length()
        collisionSystem.resolveEnemySeparation(skeletons)
        val separatedDist = (s1.position - s2.position).length()

        assertTrue("Separation should push overlapping enemies apart", separatedDist > initialDist)
    }

    @Test
    fun testPotionPickupWhenInjured() {
        val player = Player(position = Vector2D(5.0f, 5.0f), health = 60f)
        val potion = com.dungeonescape.entities.HealthPotion(
            id = 1,
            position = Vector2D(5.1f, 5.0f),
            healAmount = 20f
        )
        val potions = listOf(potion)

        var collected = false
        collisionSystem.checkPotionPickups(player, potions) {
            collected = true
        }

        assertTrue("Injured player should pick up potion", collected)
        assertTrue("Potion should be marked collected", potion.isCollected)
    }

    @Test
    fun testPotionNotConsumedAtFullHealth() {
        val player = Player(position = Vector2D(5.0f, 5.0f), health = 100f)
        val potion = com.dungeonescape.entities.HealthPotion(
            id = 1,
            position = Vector2D(5.1f, 5.0f),
            healAmount = 20f
        )
        val potions = listOf(potion)

        var collected = false
        collisionSystem.checkPotionPickups(player, potions) {
            collected = true
        }

        assertFalse("Full health player should NOT consume potion", collected)
        assertFalse("Potion should remain uncollected on map", potion.isCollected)
    }
}
