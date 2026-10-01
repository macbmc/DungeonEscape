package com.dungeonescape.engine

import com.dungeonescape.models.Position
import com.dungeonescape.models.TileType
import com.dungeonescape.utils.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.ArrayDeque

class DungeonGeneratorTest {

    private val generator = DungeonGenerator()

    @Test
    fun testDungeonGenerationGridSize() {
        val dungeon = generator.generateDungeon(level = 1)
        assertEquals(Constants.DUNGEON_SIZE, dungeon.tiles.size)
        assertEquals(Constants.DUNGEON_SIZE, dungeon.tiles[0].size)
    }

    @Test
    fun testDungeonIsSolvableAndAllKeyItemsReachable() {
        for (lvl in 1..20) {
            val dungeon = generator.generateDungeon(level = lvl)
            val tiles = dungeon.tiles
            val size = tiles.size

            val startPos = Position(dungeon.playerSpawn.x.toInt(), dungeon.playerSpawn.y.toInt())
            val keyPos = Position(dungeon.key.position.x.toInt(), dungeon.key.position.y.toInt())
            val portalPos = Position(dungeon.portal.position.x.toInt(), dungeon.portal.position.y.toInt())

            assertTrue("Player spawn must be on walkable floor", tiles[startPos.y][startPos.x].isWalkable)
            assertTrue("Key must be on walkable floor", tiles[keyPos.y][keyPos.x].isWalkable)
            assertTrue("Portal must be on walkable floor", tiles[portalPos.y][portalPos.x].isWalkable)

            // BFS from Player to Key and Portal (without relying on secret walls)
            val visited = Array(size) { BooleanArray(size) }
            val queue = ArrayDeque<Position>()
            queue.add(startPos)
            visited[startPos.y][startPos.x] = true

            val dx = intArrayOf(0, 0, 1, -1)
            val dy = intArrayOf(1, -1, 0, 0)

            while (queue.isNotEmpty()) {
                val curr = queue.poll() ?: break
                for (d in 0 until 4) {
                    val nx = curr.x + dx[d]
                    val ny = curr.y + dy[d]
                    if (nx in 0 until size && ny in 0 until size) {
                        if (!visited[ny][nx] && tiles[ny][nx].isWalkable && tiles[ny][nx].type != TileType.SECRET_WALL) {
                            visited[ny][nx] = true
                            queue.add(Position(nx, ny))
                        }
                    }
                }
            }

            assertTrue("Level $lvl: Key must be reachable from player spawn without secret walls", visited[keyPos.y][keyPos.x])
            assertTrue("Level $lvl: Portal must be reachable from player spawn without secret walls", visited[portalPos.y][portalPos.x])
        }
    }

    @Test
    fun testDifficultyScalingAndVariants() {
        val lvl1 = generator.generateDungeon(1)
        val lvl12 = generator.generateDungeon(12)

        assertTrue(
            "Higher levels should have more enemies",
            lvl12.skeletons.size >= lvl1.skeletons.size
        )

        if (lvl12.skeletons.isNotEmpty() && lvl1.skeletons.isNotEmpty()) {
            assertTrue(
                "Level 12 skeletons should have increased speed",
                lvl12.skeletons.first().speed > lvl1.skeletons.first().speed
            )
        }
    }

    @Test
    fun testLevel1HasZeroPotionsAndNoSecretRooms() {
        for (i in 1..10) {
            val lvl1 = generator.generateDungeon(level = 1)
            assertEquals("Level 1 must never spawn world potions", 0, lvl1.potions.size)
            assertEquals("Level 1 must never spawn secret room altars", null, lvl1.merchantAltar)
            assertEquals("Level 1 must never spawn secret room chests", 0, lvl1.chests.size)
        }
    }

    @Test
    fun testLevel2HasNoSecretRooms() {
        for (i in 1..10) {
            val lvl2 = generator.generateDungeon(level = 2)
            assertEquals("Level 2 must not spawn secret room altars", null, lvl2.merchantAltar)
            assertEquals("Level 2 must not spawn secret room chests", 0, lvl2.chests.size)
        }
    }

    @Test
    fun testLowHealthBoostsLevel2PotionSpawn() {
        // Player with low health (< 35 HP) entering level 2 should get guaranteed potion
        val lowHpDungeon = generator.generateDungeon(level = 2, playerHealthEnteringLevel = 25f)
        assertTrue("Low health player in Level 2 should spawn potion", lowHpDungeon.potions.isNotEmpty())
    }

    @Test
    fun testLevel2AndLevel3PotionHighAvailability() {
        var lvl2PotionsCount = 0
        var lvl3PotionsCount = 0
        val sampleSize = 50

        for (i in 0 until sampleSize) {
            val d2 = generator.generateDungeon(level = 2, playerHealthEnteringLevel = 100f)
            if (d2.potions.isNotEmpty()) lvl2PotionsCount++

            val d3 = generator.generateDungeon(level = 3, playerHealthEnteringLevel = 100f)
            if (d3.potions.isNotEmpty()) lvl3PotionsCount++
        }

        val lvl2Rate = lvl2PotionsCount.toFloat() / sampleSize
        val lvl3Rate = lvl3PotionsCount.toFloat() / sampleSize

        assertTrue(
            "Level 2 potion spawn rate should be ~80% (actual: $lvl2Rate)",
            lvl2Rate >= 0.70f
        )
        assertTrue(
            "Level 3 potion spawn rate should be ~90% (actual: $lvl3Rate)",
            lvl3Rate >= 0.75f
        )
    }
}
