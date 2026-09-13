package com.dungeonescape.entities

import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.Constants
import kotlin.random.Random

enum class SkeletonState {
    WANDERING,
    CHASING
}

data class Skeleton(
    val id: Int,
    var position: Vector2D,
    var velocity: Vector2D = Vector2D.ZERO,
    var health: Float = Constants.SKELETON_BASE_HEALTH,
    val maxHealth: Float = Constants.SKELETON_BASE_HEALTH,
    var speed: Float = Constants.SKELETON_BASE_SPEED,
    var damage: Float = Constants.SKELETON_BASE_DAMAGE,
    var detectionRadius: Float = Constants.SKELETON_DETECTION_RADIUS,
    
    var state: SkeletonState = SkeletonState.WANDERING,
    var wanderDirection: Vector2D = Vector2D.ZERO,
    var wanderTimer: Float = 0f,
    var attackCooldownTimer: Float = 0f,
    var hitFlashTimer: Float = 0f,
    var isAlive: Boolean = true,
    var walkCycleTimer: Float = 0f
) {
    val isHit: Boolean get() = hitFlashTimer > 0f

    fun takeDamage(amount: Float): Boolean {
        if (!isAlive) return false
        health = (health - amount).coerceAtLeast(0f)
        hitFlashTimer = 0.2f
        if (health <= 0f) {
            isAlive = false
        }
        return true
    }

    fun updateAI(
        deltaTime: Float,
        playerPos: Vector2D,
        isTileWalkable: (Int, Int) -> Boolean
    ) {
        if (!isAlive) return

        // Update timers
        if (hitFlashTimer > 0f) {
            hitFlashTimer = (hitFlashTimer - deltaTime).coerceAtLeast(0f)
        }
        if (attackCooldownTimer > 0f) {
            attackCooldownTimer = (attackCooldownTimer - deltaTime).coerceAtLeast(0f)
        }

        val distToPlayer = position.distanceTo(playerPos)

        if (distToPlayer <= detectionRadius) {
            // Chase Player
            state = SkeletonState.CHASING
            val chaseDir = (playerPos - position).normalized()
            velocity = chaseDir * speed
        } else {
            // Wander randomly
            state = SkeletonState.WANDERING
            wanderTimer -= deltaTime
            if (wanderTimer <= 0f) {
                // Pick a new wander direction or pause
                wanderTimer = Random.nextFloat() * 2f + 1f // 1 to 3 seconds
                if (Random.nextFloat() < 0.3f) {
                    wanderDirection = Vector2D.ZERO
                } else {
                    val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
                    wanderDirection = Vector2D.fromAngle(angle)
                }
            }
            velocity = wanderDirection * (speed * 0.6f)
        }

        // Avoid walking straight into walls during wander
        if (velocity.lengthSquared() > 0.01f) {
            val futurePos = position + velocity * deltaTime
            val tileX = futurePos.x.toInt()
            val tileY = futurePos.y.toInt()
            if (!isTileWalkable(tileX, tileY)) {
                // If hitting wall, negate or randomize wander
                wanderDirection = Vector2D(-wanderDirection.x, -wanderDirection.y)
                velocity = Vector2D.ZERO
            }
        }

        if (velocity.lengthSquared() > 0.01f) {
            walkCycleTimer += deltaTime * 6f
        }
    }
}
