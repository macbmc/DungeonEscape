package com.dungeonescape.engine

import com.dungeonescape.entities.Coin
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.Player
import com.dungeonescape.entities.Skeleton
import com.dungeonescape.models.Position
import com.dungeonescape.models.Tile
import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.Constants
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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
        var nextX = currentPos.x + velocity.x * deltaTime
        var nextY = currentPos.y + velocity.y * deltaTime

        // Test X movement
        if (checkCircleWallCollision(nextX, currentPos.y, radius, tiles, gridSize)) {
            // Block X movement, slide along Y
            nextX = currentPos.x
        }

        // Test Y movement
        if (checkCircleWallCollision(nextX, nextY, radius, tiles, gridSize)) {
            // Block Y movement
            nextY = currentPos.y
        }

        // Final boundary clamp
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
                    // AABB vs Circle check
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
     * Checks if player collides with any coins.
     */
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
     * Checks if player collects the key.
     */
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

    /**
     * Checks if player reaches the activated portal.
     */
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

    /**
     * Checks player attack against enemies in attack arc/radius.
     */
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
                // Check if enemy is in front cone (~160 degree FOV for melee swing)
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

    /**
     * Checks enemy contact damage on player (player takes damage if not dashing and within contact range).
     */
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
