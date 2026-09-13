package com.dungeonescape.engine

import com.dungeonescape.entities.Coin
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.Skeleton
import com.dungeonescape.models.Position
import com.dungeonescape.models.Tile
import com.dungeonescape.models.TileType
import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.Constants
import java.util.ArrayDeque
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class DungeonData(
    val tiles: Array<Array<Tile>>,
    val playerSpawn: Vector2D,
    val key: KeyItem,
    val portal: ExitPortal,
    val coins: List<Coin>,
    val skeletons: List<Skeleton>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DungeonData

        if (!tiles.contentDeepEquals(other.tiles)) return false
        if (playerSpawn != other.playerSpawn) return false
        if (key != other.key) return false
        if (portal != other.portal) return false
        if (coins != other.coins) return false
        if (skeletons != other.skeletons) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tiles.contentDeepHashCode()
        result = 31 * result + playerSpawn.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + portal.hashCode()
        result = 31 * result + coins.hashCode()
        result = 31 * result + skeletons.hashCode()
        return result
    }
}

class DungeonGenerator(private val size: Int = Constants.DUNGEON_SIZE) {

    private data class Room(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    ) {
        val centerX: Int get() = x + width / 2
        val centerY: Int get() = y + height / 2

        fun intersects(other: Room, margin: Int = 1): Boolean {
            return !(x + width + margin < other.x ||
                    other.x + other.width + margin < x ||
                    y + height + margin < other.y ||
                    other.y + other.height + margin < y)
        }
    }

    fun generateDungeon(level: Int): DungeonData {
        val maxAttempts = 10
        for (attempt in 0 until maxAttempts) {
            val result = tryGenerate(level)
            if (result != null) return result
        }
        // Fallback guaranteed layout
        return generateGuaranteedDungeon(level)
    }

    private fun tryGenerate(level: Int): DungeonData? {
        val tiles = Array(size) { y ->
            Array(size) { x ->
                Tile(x, y, TileType.WALL)
            }
        }

        val rooms = mutableListOf<Room>()
        val targetRoomCount = Random.nextInt(4, 8)
        val minRoomSize = 4
        val maxRoomSize = 7

        // Place rooms
        for (i in 0 until 40) {
            if (rooms.size >= targetRoomCount) break

            val w = Random.nextInt(minRoomSize, maxRoomSize + 1)
            val h = Random.nextInt(minRoomSize, maxRoomSize + 1)
            val x = Random.nextInt(1, size - w - 1)
            val y = Random.nextInt(1, size - h - 1)

            val newRoom = Room(x, y, w, h)
            val overlaps = rooms.any { it.intersects(newRoom) }

            if (!overlaps) {
                rooms.add(newRoom)
                // Carve room floor
                for (ry in y until (y + h)) {
                    for (rx in x until (x + w)) {
                        tiles[ry][rx].type = TileType.FLOOR
                    }
                }
            }
        }

        if (rooms.size < 2) return null

        // Connect rooms with corridors
        for (i in 0 until rooms.size - 1) {
            val r1 = rooms[i]
            val r2 = rooms[i + 1]

            carveCorridor(tiles, r1.centerX, r1.centerY, r2.centerX, r2.centerY)
        }

        // Connect last room to first room for loops / alternative paths
        val first = rooms.first()
        val last = rooms.last()
        carveCorridor(tiles, last.centerX, last.centerY, first.centerX, first.centerY)

        // Find all reachable floor tiles using BFS from first room center
        val startPos = Position(first.centerX, first.centerY)
        val reachableTiles = findReachableFloors(tiles, startPos)

        if (reachableTiles.size < 20) return null

        // Pick player spawn in room 0
        val playerSpawn = Vector2D(startPos.x + 0.5f, startPos.y + 0.5f)

        // Find farthest reachable positions for Key and Portal
        val sortedByDist = reachableTiles
            .filter { it != startPos }
            .sortedByDescending { it.distanceTo(startPos) }

        if (sortedByDist.size < 10) return null

        val portalPos = sortedByDist.first()
        val keyCandidates = sortedByDist.drop(1).filter { it.distanceTo(portalPos) > 4f }
        val keyPos = if (keyCandidates.isNotEmpty()) {
            keyCandidates[Random.nextInt(keyCandidates.size.coerceAtMost(5))]
        } else {
            sortedByDist[sortedByDist.size / 2]
        }

        // Mark tiles
        tiles[portalPos.y][portalPos.x].type = TileType.EXIT_PORTAL
        tiles[keyPos.y][keyPos.x].type = TileType.KEY

        val portal = ExitPortal(Vector2D(portalPos.x + 0.5f, portalPos.y + 0.5f))
        val keyItem = KeyItem(Vector2D(keyPos.x + 0.5f, keyPos.y + 0.5f))

        // Populate Coins on remaining reachable floors
        val availableForItems = reachableTiles.toMutableList().apply {
            remove(startPos)
            remove(portalPos)
            remove(keyPos)
        }
        availableForItems.shuffle()

        val coinCount = Random.nextInt(5, 12)
        val coins = mutableListOf<Coin>()
        for (i in 0 until min(coinCount, availableForItems.size)) {
            val coinPos = availableForItems[i]
            tiles[coinPos.y][coinPos.x].type = TileType.COIN
            coins.add(
                Coin(
                    id = i,
                    position = Vector2D(coinPos.x + 0.5f, coinPos.y + 0.5f)
                )
            )
        }

        // Calculate enemy count with level scaling (+1 enemy per level)
        val enemyCount = Constants.BASE_ENEMY_COUNT + (level - 1) * Constants.ENEMY_COUNT_INCREASE_PER_LEVEL
        val speedMultiplier = 1f + (level - 1) * Constants.ENEMY_SPEED_INCREASE_RATIO
        val healthMultiplier = 1f + (level - 1) * Constants.ENEMY_HEALTH_INCREASE_RATIO

        // Place enemies far from player spawn
        val skeletonCandidates = availableForItems.drop(coins.size).filter { it.distanceTo(startPos) > 5f }.shuffled()
        val skeletons = mutableListOf<Skeleton>()

        for (i in 0 until min(enemyCount, skeletonCandidates.size)) {
            val sPos = skeletonCandidates[i]
            val skeletonHealth = Constants.SKELETON_BASE_HEALTH * healthMultiplier
            val skeletonSpeed = Constants.SKELETON_BASE_SPEED * speedMultiplier

            skeletons.add(
                Skeleton(
                    id = i,
                    position = Vector2D(sPos.x + 0.5f, sPos.y + 0.5f),
                    health = skeletonHealth,
                    maxHealth = skeletonHealth,
                    speed = skeletonSpeed,
                    damage = Constants.SKELETON_BASE_DAMAGE
                )
            )
        }

        return DungeonData(
            tiles = tiles,
            playerSpawn = playerSpawn,
            key = keyItem,
            portal = portal,
            coins = coins,
            skeletons = skeletons
        )
    }

    private fun carveCorridor(tiles: Array<Array<Tile>>, x1: Int, y1: Int, x2: Int, y2: Int) {
        // L-shaped corridor
        var cx = x1
        var cy = y1

        val horizontalFirst = Random.nextBoolean()

        if (horizontalFirst) {
            while (cx != x2) {
                tiles[cy][cx].type = TileType.FLOOR
                cx += if (x2 > cx) 1 else -1
            }
            while (cy != y2) {
                tiles[cy][cx].type = TileType.FLOOR
                cy += if (y2 > cy) 1 else -1
            }
        } else {
            while (cy != y2) {
                tiles[cy][cx].type = TileType.FLOOR
                cy += if (y2 > cy) 1 else -1
            }
            while (cx != x2) {
                tiles[cy][cx].type = TileType.FLOOR
                cx += if (x2 > cx) 1 else -1
            }
        }
        tiles[cy][cx].type = TileType.FLOOR
    }

    private fun findReachableFloors(tiles: Array<Array<Tile>>, start: Position): List<Position> {
        val visited = Array(size) { BooleanArray(size) }
        val reachable = mutableListOf<Position>()
        val queue = ArrayDeque<Position>()

        if (!tiles[start.y][start.x].isWalkable) return emptyList()

        queue.add(start)
        visited[start.y][start.x] = true

        val dx = intArrayOf(0, 0, 1, -1)
        val dy = intArrayOf(1, -1, 0, 0)

        while (queue.isNotEmpty()) {
            val curr = queue.poll() ?: break
            reachable.add(curr)

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

        return reachable
    }

    private fun generateGuaranteedDungeon(level: Int): DungeonData {
        val tiles = Array(size) { y ->
            Array(size) { x ->
                if (x in 2..(size - 3) && y in 2..(size - 3)) {
                    Tile(x, y, TileType.FLOOR)
                } else {
                    Tile(x, y, TileType.WALL)
                }
            }
        }

        val playerSpawn = Vector2D(3.5f, 3.5f)
        val keyPos = Vector2D(size - 4.5f, 3.5f)
        val portalPos = Vector2D(size - 4.5f, size - 4.5f)

        tiles[keyPos.y.toInt()][keyPos.x.toInt()].type = TileType.KEY
        tiles[portalPos.y.toInt()][portalPos.x.toInt()].type = TileType.EXIT_PORTAL

        val key = KeyItem(keyPos)
        val portal = ExitPortal(portalPos)

        val coins = listOf(
            Coin(0, Vector2D(7.5f, 7.5f)),
            Coin(1, Vector2D(12.5f, 12.5f)),
            Coin(2, Vector2D(17.5f, 17.5f))
        )

        val skeletons = listOf(
            Skeleton(
                0,
                Vector2D(10.5f, 10.5f),
                health = Constants.SKELETON_BASE_HEALTH,
                maxHealth = Constants.SKELETON_BASE_HEALTH,
                speed = Constants.SKELETON_BASE_SPEED
            )
        )

        return DungeonData(tiles, playerSpawn, key, portal, coins, skeletons)
    }
}
