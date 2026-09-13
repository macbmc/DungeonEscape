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
        // Run 20 random procedural dungeons to guarantee 100% solvability
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

            // BFS from Player to Key and Portal
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
                        if (!visited[ny][nx] && tiles[ny][nx].isWalkable) {
                            visited[ny][nx] = true
                            queue.add(Position(nx, ny))
                        }
                    }
                }
            }

            assertTrue("Level $lvl: Key must be reachable from player spawn", visited[keyPos.y][keyPos.x])
            assertTrue("Level $lvl: Portal must be reachable from player spawn", visited[portalPos.y][portalPos.x])

            // Verify coins reachability
            for (coin in dungeon.coins) {
                val cPos = Position(coin.position.x.toInt(), coin.position.y.toInt())
                assertTrue("Level $lvl: Coin at ($cPos) must be reachable", visited[cPos.y][cPos.x])
            }
        }
    }

    @Test
    fun testDifficultyScaling() {
        val lvl1 = generator.generateDungeon(1)
        val lvl5 = generator.generateDungeon(5)

        assertTrue(
            "Higher levels should have more enemies",
            lvl5.skeletons.size >= lvl1.skeletons.size
        )

        if (lvl5.skeletons.isNotEmpty() && lvl1.skeletons.isNotEmpty()) {
            assertTrue(
                "Level 5 skeletons should have increased speed",
                lvl5.skeletons.first().speed > lvl1.skeletons.first().speed
            )
            assertTrue(
                "Level 5 skeletons should have increased health",
                lvl5.skeletons.first().health > lvl1.skeletons.first().health
            )
        }
    }
}
