package com.dungeonescape.engine

import com.dungeonescape.entities.Coin
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.HealthPotion
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.MerchantAltar
import com.dungeonescape.entities.Player
import com.dungeonescape.entities.Skeleton
import com.dungeonescape.entities.TrapSpike
import com.dungeonescape.entities.TreasureChest
import com.dungeonescape.models.Position
import com.dungeonescape.models.Tile
import com.dungeonescape.models.TileType
import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.Constants
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class CollisionSystem {

    /**
     * Resolves circle vs tile grid collision for moving entities (Player, Skeleton).
     * Provides smooth sliding along walls if moving diagonally.
     */
    fun resolveEntityWallCollision(
        currentPos: Vector2D,
        velocity: Vector2D,
        deltaTime: Float,
        radius: Float,
        tiles: Array<Array<Tile>>
    ): Vector2D {
        val gridSize = tiles.size
        if (gridSize == 0) return currentPos

        var nextX = currentPos.x + velocity.x * deltaTime
        var nextY = currentPos.y + velocity.y * deltaTime

        // Test X movement
        if (checkCircleWallCollision(nextX, currentPos.y, radius, tiles, gridSize)) {
            nextX = currentPos.x
        }

        // Test Y movement
        if (checkCircleWallCollision(nextX, nextY, radius, tiles, gridSize)) {
            nextY = currentPos.y
        }

        val clampedX = nextX.coerceIn(radius, gridSize - radius)
        val clampedY = nextY.coerceIn(radius, gridSize - radius)

        return Vector2D(clampedX, clampedY)
    }

    private fun checkCircleWallCollision(
        cx: Float,
        cy: Float,
        radius: Float,
        tiles: Array<Array<Tile>>,
        gridSize: Int
    ): Boolean {
        val minTileX = (cx - radius).toInt().coerceIn(0, gridSize - 1)
        val maxTileX = (cx + radius).toInt().coerceIn(0, gridSize - 1)
        val minTileY = (cy - radius).toInt().coerceIn(0, gridSize - 1)
        val maxTileY = (cy + radius).toInt().coerceIn(0, gridSize - 1)

        for (ty in minTileY..maxTileY) {
            for (tx in minTileX..maxTileX) {
                if (!tiles[ty][tx].isWalkable) {
                    val closestX = cx.coerceIn(tx.toFloat(), tx + 1f)
                    val closestY = cy.coerceIn(ty.toFloat(), ty + 1f)
                    val dx = cx - closestX
                    val dy = cy - closestY
                    if (dx * dx + dy * dy < radius * radius) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Checks if player's attack strikes and breaks adjacent Secret Walls.
     */
    fun checkSecretWallAttack(
        player: Player,
        tiles: Array<Array<Tile>>,
        onWallHit: (Tile, Boolean) -> Unit
    ) {
        if (!player.isAttacking) return
        val gridSize = tiles.size
        val attackRadius = Constants.PLAYER_ATTACK_RADIUS
        val facingAngle = atan2(player.facingDirection.y, player.facingDirection.x)

        val minTx = (player.position.x - attackRadius).toInt().coerceIn(0, gridSize - 1)
        val maxTx = (player.position.x + attackRadius).toInt().coerceIn(0, gridSize - 1)
        val minTy = (player.position.y - attackRadius).toInt().coerceIn(0, gridSize - 1)
        val maxTy = (player.position.y + attackRadius).toInt().coerceIn(0, gridSize - 1)

        for (ty in minTy..maxTy) {
            for (tx in minTx..maxTx) {
                val tile = tiles[ty][tx]
                if (tile.type == TileType.SECRET_WALL) {
                    val tileCenter = Vector2D(tx + 0.5f, ty + 0.5f)
                    val diff = tileCenter - player.position
                    val distSq = diff.lengthSquared()

                    if (distSq <= attackRadius * attackRadius) {
                        val angle = atan2(diff.y, diff.x)
                        var angleDiff = abs(angle - facingAngle)
                        if (angleDiff > Math.PI) {
                            angleDiff = ((2 * Math.PI) - angleDiff).toFloat()
                        }

                        if (angleDiff <= Math.PI * 0.6f) {
                            tile.secretWallHits--
                            val isBroken = tile.secretWallHits <= 0
                            if (isBroken) {
                                tile.type = TileType.FLOOR
                            }
                            onWallHit(tile, isBroken)
                        }
                    }
                }
            }
        }
    }

    /**
     * Soft separation force between enemies.
     */
    fun resolveEnemySeparation(skeletons: List<Skeleton>) {
        val minDistance = Constants.SKELETON_COLLISION_RADIUS * 2f
        val minDistanceSq = minDistance * minDistance

        for (i in skeletons.indices) {
            val s1 = skeletons[i]
            if (!s1.isAlive) continue

            for (j in i + 1 until skeletons.size) {
                val s2 = skeletons[j]
                if (!s2.isAlive) continue

                val diff = s1.position - s2.position
                val distSq = diff.lengthSquared()

                if (distSq < minDistanceSq) {
                    val dist = sqrt(distSq)
                    val pushDir = if (dist > 0.001f) {
                        diff * (1f / dist)
                    } else {
                        Vector2D(0.1f, 0.1f).normalized()
                    }
                    val overlap = (minDistance - dist).coerceAtLeast(0.01f) * 0.5f
                    s1.position = s1.position + pushDir * overlap
                    s2.position = s2.position - pushDir * overlap
                }
            }
        }
    }

    fun checkChestInteraction(
        playerPos: Vector2D,
        chests: List<TreasureChest>,
        pickupRadius: Float = 0.85f,
        onChestOpened: (TreasureChest, Int) -> Unit
    ) {
        val radiusSq = pickupRadius * pickupRadius
        for (chest in chests) {
            if (!chest.isOpen && (playerPos - chest.position).lengthSquared() <= radiusSq) {
                val coins = chest.open()
                onChestOpened(chest, coins)
            }
        }
    }

    fun checkTrapCollisions(
        player: Player,
        traps: List<TrapSpike>,
        onTrapTriggered: (TrapSpike, Float) -> Unit
    ) {
        if (player.isDashing || player.isInvulnerable || player.isDead) return
        val trapRadiusSq = 0.5f * 0.5f

        for (trap in traps) {
            if (trap.isExtended && (player.position - trap.position).lengthSquared() <= trapRadiusSq) {
                onTrapTriggered(trap, trap.damage)
                break
            }
        }
    }

    fun checkMerchantProximity(playerPos: Vector2D, altar: MerchantAltar?): Boolean {
        if (altar == null) return false
        val distSq = (playerPos - altar.position).lengthSquared()
        return distSq <= 1.4f * 1.4f
    }

    fun checkCoinPickups(
        playerPos: Vector2D,
        coins: List<Coin>,
        pickupRadius: Float = 0.6f,
        onCoinCollected: (Coin) -> Unit
    ) {
        val radiusSq = pickupRadius * pickupRadius
        for (coin in coins) {
            if (!coin.isCollected && (playerPos - coin.position).lengthSquared() <= radiusSq) {
                coin.isCollected = true
                onCoinCollected(coin)
            }
        }
    }

    /**
     * Checks if player can pick up a health potion.
     * Potions are ONLY consumed if the player is injured (health < maxHealth).
     * If player is at full health, the potion is preserved on the ground.
     */
    fun checkPotionPickups(
        player: Player,
        potions: List<HealthPotion>,
        pickupRadius: Float = 0.65f,
        onPotionCollected: (HealthPotion) -> Unit
    ) {
        // Do not consume if already at full health
        if (player.health >= player.maxHealth || player.isDead) return

        val radiusSq = pickupRadius * pickupRadius
        for (potion in potions) {
            if (!potion.isCollected && (player.position - potion.position).lengthSquared() <= radiusSq) {
                potion.isCollected = true
                onPotionCollected(potion)
                // Consume one potion per frame to avoid duplicate pickups
                break
            }
        }
    }

    fun checkKeyPickup(
        playerPos: Vector2D,
        key: KeyItem?,
        pickupRadius: Float = 0.7f,
        onKeyCollected: () -> Unit
    ) {
        if (key != null && !key.isCollected) {
            if ((playerPos - key.position).lengthSquared() <= pickupRadius * pickupRadius) {
                key.isCollected = true
                onKeyCollected()
            }
        }
    }

    fun checkPortalEntry(
        playerPos: Vector2D,
        portal: ExitPortal?,
        entryRadius: Float = 0.65f
    ): Boolean {
        if (portal != null && portal.isActive) {
            return (playerPos - portal.position).lengthSquared() <= entryRadius * entryRadius
        }
        return false
    }

    fun checkPlayerAttack(
        player: Player,
        skeletons: List<Skeleton>,
        onEnemyHit: (Skeleton, Float) -> Unit
    ) {
        if (!player.isAttacking) return
        val attackRadiusSq = Constants.PLAYER_ATTACK_RADIUS * Constants.PLAYER_ATTACK_RADIUS
        val facingAngle = atan2(player.facingDirection.y, player.facingDirection.x)

        for (skeleton in skeletons) {
            if (!skeleton.isAlive) continue
            val diff = skeleton.position - player.position
            val distSq = diff.lengthSquared()

            if (distSq <= attackRadiusSq) {
                val enemyAngle = atan2(diff.y, diff.x)
                var angleDiff = abs(enemyAngle - facingAngle)
                if (angleDiff > Math.PI) {
                    angleDiff = ((2 * Math.PI) - angleDiff).toFloat()
                }

                if (angleDiff <= Math.PI * 0.55f || distSq <= 0.35f * 0.35f) {
                    onEnemyHit(skeleton, Constants.PLAYER_ATTACK_DAMAGE)
                }
            }
        }
    }

    fun checkEnemyPlayerContact(
        player: Player,
        skeletons: List<Skeleton>,
        onPlayerDamaged: (Float) -> Unit
    ) {
        if (player.isDashing || player.isInvulnerable || player.isDead) return

        val contactRadiusSq = (Constants.PLAYER_COLLISION_RADIUS + Constants.SKELETON_COLLISION_RADIUS) *
                (Constants.PLAYER_COLLISION_RADIUS + Constants.SKELETON_COLLISION_RADIUS)

        for (skeleton in skeletons) {
            if (!skeleton.isAlive) continue
            if (skeleton.attackCooldownTimer <= 0f) {
                val distSq = (player.position - skeleton.position).lengthSquared()
                if (distSq <= contactRadiusSq) {
                    skeleton.attackCooldownTimer = Constants.SKELETON_ATTACK_COOLDOWN
                    onPlayerDamaged(skeleton.damage)
                    break
                }
            }
        }
    }
}
