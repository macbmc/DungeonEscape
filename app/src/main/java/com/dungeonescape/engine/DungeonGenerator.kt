package com.dungeonescape.engine

import android.util.Log
import com.dungeonescape.entities.Coin
import com.dungeonescape.entities.EnemyVariant
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.HealthPotion
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.MerchantAltar
import com.dungeonescape.entities.PotionType
import com.dungeonescape.entities.Skeleton
import com.dungeonescape.entities.TrapSpike
import com.dungeonescape.entities.TreasureChest
import com.dungeonescape.models.Position
import com.dungeonescape.models.Tile
import com.dungeonescape.models.TileType
import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.Constants
import com.dungeonescape.utils.GameBalance
import java.util.ArrayDeque
import kotlin.math.min
import kotlin.random.Random

data class DungeonData(
    val tiles: Array<Array<Tile>>,
    val playerSpawn: Vector2D,
    val key: KeyItem,
    val portal: ExitPortal,
    val coins: List<Coin>,
    val skeletons: List<Skeleton>,
    val potions: List<HealthPotion> = emptyList(),
    val chests: List<TreasureChest> = emptyList(),
    val merchantAltar: MerchantAltar? = null,
    val traps: List<TrapSpike> = emptyList()
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
        if (potions != other.potions) return false
        if (chests != other.chests) return false
        if (merchantAltar != other.merchantAltar) return false
        if (traps != other.traps) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tiles.contentDeepHashCode()
        result = 31 * result + playerSpawn.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + portal.hashCode()
        result = 31 * result + coins.hashCode()
        result = 31 * result + skeletons.hashCode()
        result = 31 * result + potions.hashCode()
        result = 31 * result + chests.hashCode()
        result = 31 * result + (merchantAltar?.hashCode() ?: 0)
        result = 31 * result + traps.hashCode()
        return result
    }
}

class DungeonGenerator(private val size: Int = Constants.DUNGEON_SIZE) {

    private data class Room(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val isTreasureRoom: Boolean = false
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

    fun generateDungeon(level: Int, playerHealthEnteringLevel: Float = 100f): DungeonData {
        val maxAttempts = 12
        for (attempt in 0 until maxAttempts) {
            val result = tryGenerate(level, playerHealthEnteringLevel)
            if (result != null) return result
        }
        return generateGuaranteedDungeon(level)
    }

    private fun tryGenerate(level: Int, playerHealthEnteringLevel: Float = 100f): DungeonData? {
        val tiles = Array(size) { y ->
            Array(size) { x ->
                Tile(x, y, TileType.WALL)
            }
        }

        val rooms = mutableListOf<Room>()
        val targetRoomCount = Random.nextInt(4, 7)
        val minRoomSize = 4
        val maxRoomSize = 6

        // 1. Place Main Rooms
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
                for (ry in y until (y + h)) {
                    for (rx in x until (x + w)) {
                        tiles[ry][rx].type = TileType.FLOOR
                    }
                }
            }
        }

        if (rooms.size < 2) return null

        // 2. Connect Main Rooms with Corridors
        for (i in 0 until rooms.size - 1) {
            val r1 = rooms[i]
            val r2 = rooms[i + 1]
            carveCorridor(tiles, r1.centerX, r1.centerY, r2.centerX, r2.centerY)
        }
        carveCorridor(tiles, rooms.last().centerX, rooms.last().centerY, rooms.first().centerX, rooms.first().centerY)

        // 3. Solvability BFS Verification
        val first = rooms.first()
        val startPos = Position(first.centerX, first.centerY)
        val reachableTiles = findReachableFloors(tiles, startPos)

        if (reachableTiles.size < 25) return null

        // 4. Place Key and Portal
        val playerSpawn = Vector2D(startPos.x + 0.5f, startPos.y + 0.5f)
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

        tiles[portalPos.y][portalPos.x].type = TileType.EXIT_PORTAL
        tiles[keyPos.y][keyPos.x].type = TileType.KEY

        val portal = ExitPortal(Vector2D(portalPos.x + 0.5f, portalPos.y + 0.5f))
        val keyItem = KeyItem(Vector2D(keyPos.x + 0.5f, keyPos.y + 0.5f))

        // 5. Procedural Secret Treasure Room Generation (Level 3+ Only)
        val treasureRoomChance = when {
            level < 3 -> 0.0f
            level <= 5 -> 0.45f
            level <= 10 -> 0.70f
            else -> 0.90f
        }

        val chests = mutableListOf<TreasureChest>()
        var merchantAltar: MerchantAltar? = null
        val traps = mutableListOf<TrapSpike>()
        val extraSkeletons = mutableListOf<Skeleton>()
        val extraCoins = mutableListOf<Coin>()

        if (Random.nextFloat() < treasureRoomChance) {
            // Try to carve a secret room adjacent to an existing room
            val secretRoomSize = Random.nextInt(4, 6)
            for (r in rooms.shuffled()) {
                val secretX = (r.x + r.width + 1).coerceAtMost(size - secretRoomSize - 1)
                val secretY = r.y.coerceIn(1, size - secretRoomSize - 1)

                val testRoom = Room(secretX, secretY, secretRoomSize, secretRoomSize, isTreasureRoom = true)
                // Check that area is free of floors
                var isAreaClear = true
                for (ty in secretY until secretY + secretRoomSize) {
                    for (tx in secretX until secretX + secretRoomSize) {
                        if (tiles[ty][tx].type != TileType.WALL) {
                            isAreaClear = false
                            break
                        }
                    }
                    if (!isAreaClear) break
                }

                if (isAreaClear && secretX > r.x + r.width) {
                    // Carve secret room floor
                    for (ty in secretY until secretY + secretRoomSize) {
                        for (tx in secretX until secretX + secretRoomSize) {
                            tiles[ty][tx].type = TileType.FLOOR
                        }
                    }

                    // Place Secret Breakable Wall between r and testRoom
                    val wallX = r.x + r.width
                    val wallY = (r.y + 1).coerceAtMost(size - 2)
                    if (wallX in 0 until size && wallY in 0 until size) {
                        tiles[wallY][wallX].type = TileType.SECRET_WALL
                        tiles[wallY][wallX].secretWallHits = 3

                        // Fill Secret Room with Altar, Chest, Extra Coins & Guardian
                        val altarPos = Vector2D(secretX + 1.5f, secretY + 1.5f)
                        merchantAltar = MerchantAltar(altarPos)

                        val chestPos = Vector2D(secretX + secretRoomSize - 1.5f, secretY + 1.5f)
                        chests.add(TreasureChest(id = 100, position = chestPos, coinReward = 40))

                        // Extra coins
                        extraCoins.add(Coin(id = 200, position = Vector2D(secretX + 1.5f, secretY + secretRoomSize - 1.5f)))
                        extraCoins.add(Coin(id = 201, position = Vector2D(secretX + secretRoomSize - 1.5f, secretY + secretRoomSize - 1.5f)))

                        // Risk vs Reward: Guardian or Traps
                        if (Random.nextBoolean()) {
                            // Ancient Guardian
                            extraSkeletons.add(
                                Skeleton(
                                    id = 999,
                                    position = Vector2D(secretX + secretRoomSize / 2f + 0.5f, secretY + secretRoomSize / 2f + 0.5f),
                                    health = Constants.SKELETON_BASE_HEALTH * 2.0f,
                                    maxHealth = Constants.SKELETON_BASE_HEALTH * 2.0f,
                                    speed = Constants.SKELETON_BASE_SPEED * 1.15f,
                                    damage = Constants.SKELETON_BASE_DAMAGE * 1.2f,
                                    variant = EnemyVariant.ANCIENT_GUARDIAN
                                )
                            )
                        } else {
                            // Spike Traps in secret room entryway
                            traps.add(TrapSpike(id = 1, position = Vector2D(secretX + 0.5f, wallY + 0.5f)))
                        }
                        break
                    }
                }
            }
        }

        // 6. Populate Coins on Main Floor
        val availableForItems = reachableTiles.toMutableList().apply {
            remove(startPos)
            remove(portalPos)
            remove(keyPos)
        }
        availableForItems.shuffle()

        val coinCount = Random.nextInt(6, 12)
        val coins = mutableListOf<Coin>()
        for (i in 0 until min(coinCount, availableForItems.size)) {
            val cPos = availableForItems[i]
            tiles[cPos.y][cPos.x].type = TileType.COIN
            coins.add(Coin(id = i, position = Vector2D(cPos.x + 0.5f, cPos.y + 0.5f)))
        }
        coins.addAll(extraCoins)

        // 6b. Spawn World Health Potions (Level 2+ Only)
        val potions = mutableListOf<HealthPotion>()
        if (level >= 2) {
            val isLowHealth = playerHealthEnteringLevel < GameBalance.LOW_HEALTH_THRESHOLD
            val spawnChance = when {
                isLowHealth -> 1.0f
                level == 2 -> GameBalance.POTION_SPAWN_CHANCE_LEVEL_2 // 80%
                level == 3 -> GameBalance.POTION_SPAWN_CHANCE_LEVEL_3 // 90%
                else -> GameBalance.POTION_SPAWN_CHANCE_LEVEL_4_PLUS // 95%
            }

            if (Random.nextFloat() < spawnChance) {
                val maxPotionCount = when {
                    level == 2 -> 1
                    level == 3 -> if (availableForItems.size > 20 && Random.nextFloat() < 0.25f) 2 else 1
                    else -> if (isLowHealth || Random.nextFloat() < 0.5f) 2 else 1
                }

                // Available tiles excluding already placed coins, start, and portal
                val remainingTiles = availableForItems.drop(coinCount)

                // 1. Primary candidates: not right beside spawn or portal (distance >= 2.0)
                var potionCandidates = remainingTiles
                    .filter { it.distanceTo(startPos) >= 2.0f && it.distanceTo(portalPos) >= 2.0f }
                    .shuffled()

                // 2. Fallback if tight map: just not immediately on spawn or portal (distance >= 1.0)
                if (potionCandidates.isEmpty()) {
                    potionCandidates = remainingTiles
                        .filter { it.distanceTo(startPos) >= 1.0f && it.distanceTo(portalPos) >= 1.0f }
                        .shuffled()
                }

                // 3. Final fallback: any remaining reachable tile
                if (potionCandidates.isEmpty()) {
                    potionCandidates = remainingTiles.shuffled()
                }

                for (i in 0 until min(maxPotionCount, potionCandidates.size)) {
                    val pPos = potionCandidates[i]
                    tiles[pPos.y][pPos.x].type = TileType.HEALTH_POTION
                    potions.add(
                        HealthPotion(
                            id = 500 + i,
                            position = Vector2D(pPos.x + 0.5f, pPos.y + 0.5f),
                            type = PotionType.SMALL,
                            healAmount = GameBalance.SMALL_WORLD_POTION_HEAL
                        )
                    )
                }
            }

            logDebug("Level $level | Potion Chance: ${(spawnChance * 100).toInt()}% | Generated Potions: ${potions.size}")
        } else {
            logDebug("Level 1 | Potion Chance: 0% | Generated Potions: 0")
        }

        // 7. Populate Enemies with Scaling and Variants
        val enemyCount = Constants.BASE_ENEMY_COUNT + (level - 1) * Constants.ENEMY_COUNT_INCREASE_PER_LEVEL
        val speedMultiplier = 1f + (level - 1) * Constants.ENEMY_SPEED_INCREASE_RATIO
        val healthMultiplier = 1f + (level - 1) * Constants.ENEMY_HEALTH_INCREASE_RATIO

        val skeletonCandidates = availableForItems
            .drop(coinCount + potions.size)
            .filter { it.distanceTo(startPos) > 4.5f }
            .shuffled()
        val skeletons = mutableListOf<Skeleton>()

        for (i in 0 until min(enemyCount, skeletonCandidates.size)) {
            val sPos = skeletonCandidates[i]

            // Choose Enemy Variant
            val variant = when {
                level >= 11 && Random.nextFloat() < 0.35f -> EnemyVariant.SHADOW_ELITE
                level >= 4 && Random.nextFloat() < 0.40f -> EnemyVariant.ELITE
                else -> EnemyVariant.NORMAL
            }

            val variantHealthMult = when (variant) {
                EnemyVariant.ELITE -> 1.5f
                EnemyVariant.ANCIENT_GUARDIAN -> 2.0f
                EnemyVariant.SHADOW_ELITE -> 1.25f
                EnemyVariant.NORMAL -> 1.0f
            }

            val variantSpeedMult = when (variant) {
                EnemyVariant.ELITE -> 1.25f
                EnemyVariant.ANCIENT_GUARDIAN -> 1.15f
                EnemyVariant.SHADOW_ELITE -> 1.10f
                EnemyVariant.NORMAL -> 1.0f
            }

            val skeletonHealth = Constants.SKELETON_BASE_HEALTH * healthMultiplier * variantHealthMult
            val skeletonSpeed = Constants.SKELETON_BASE_SPEED * speedMultiplier * variantSpeedMult

            skeletons.add(
                Skeleton(
                    id = i,
                    position = Vector2D(sPos.x + 0.5f, sPos.y + 0.5f),
                    health = skeletonHealth,
                    maxHealth = skeletonHealth,
                    speed = skeletonSpeed,
                    damage = Constants.SKELETON_BASE_DAMAGE,
                    variant = variant
                )
            )
        }
        skeletons.addAll(extraSkeletons)

        return DungeonData(
            tiles = tiles,
            playerSpawn = playerSpawn,
            key = keyItem,
            portal = portal,
            coins = coins,
            skeletons = skeletons,
            potions = potions,
            chests = chests,
            merchantAltar = merchantAltar,
            traps = traps
        )
    }

    private fun carveCorridor(tiles: Array<Array<Tile>>, x1: Int, y1: Int, x2: Int, y2: Int) {
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

        return DungeonData(
            tiles = tiles,
            playerSpawn = playerSpawn,
            key = key,
            portal = portal,
            coins = coins,
            skeletons = skeletons,
            potions = emptyList()
        )
    }

    private fun logDebug(msg: String) {
        try {
            Log.d("DungeonGenerator", msg)
        } catch (_: Throwable) {
            println("[DungeonGenerator] $msg")
        }
    }
}
