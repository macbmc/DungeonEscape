package com.dungeonescape.entities

import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.Constants

data class Player(
    var position: Vector2D = Vector2D.ZERO,
    var velocity: Vector2D = Vector2D.ZERO,
    var health: Float = Constants.PLAYER_MAX_HEALTH,
    val maxHealth: Float = Constants.PLAYER_MAX_HEALTH,
    var speed: Float = Constants.PLAYER_SPEED,
    var coinsCollected: Int = 0,
    var hasKey: Boolean = false,
    
    // Dash system
    var isDashing: Boolean = false,
    var dashTimer: Float = 0f,
    var dashCooldownTimer: Float = 0f,
    var dashDirection: Vector2D = Vector2D.DOWN,

    // Combat system
    var attackCooldownTimer: Float = 0f,
    var isAttacking: Boolean = false,
    var attackTimer: Float = 0f,
    var invulnerabilityTimer: Float = 0f,
    var facingDirection: Vector2D = Vector2D.DOWN,

    // Animation & rendering state
    var walkCycleTimer: Float = 0f,
    var isMoving: Boolean = false
) {
    val isInvulnerable: Boolean get() = invulnerabilityTimer > 0f
    val canDash: Boolean get() = dashCooldownTimer <= 0f && !isDashing
    val canAttack: Boolean get() = attackCooldownTimer <= 0f && !isDashing
    val isDead: Boolean get() = health <= 0f

    fun takeDamage(amount: Float): Boolean {
        if (isInvulnerable || isDashing || isDead) return false
        health = (health - amount).coerceAtLeast(0f)
        invulnerabilityTimer = Constants.PLAYER_INVULNERABILITY_TIME
        return true
    }

    fun heal(amount: Float) {
        health = (health + amount).coerceAtMost(maxHealth)
    }

    fun triggerDash(direction: Vector2D): Boolean {
        if (!canDash) return false
        val dashDir = if (direction.lengthSquared() > 0.01f) direction.normalized() else facingDirection
        isDashing = true
        dashTimer = Constants.DASH_DURATION
        dashCooldownTimer = Constants.DASH_COOLDOWN
        dashDirection = dashDir
        return true
    }

    fun triggerAttack(): Boolean {
        if (!canAttack) return false
        isAttacking = true
        attackTimer = 0.2f
        attackCooldownTimer = Constants.PLAYER_ATTACK_COOLDOWN
        return true
    }

    fun update(deltaTime: Float) {
        // Update invulnerability
        if (invulnerabilityTimer > 0f) {
            invulnerabilityTimer = (invulnerabilityTimer - deltaTime).coerceAtLeast(0f)
        }

        // Update dash cooldown
        if (dashCooldownTimer > 0f) {
            dashCooldownTimer = (dashCooldownTimer - deltaTime).coerceAtLeast(0f)
        }

        // Update active dash
        if (isDashing) {
            dashTimer -= deltaTime
            if (dashTimer <= 0f) {
                isDashing = false
            }
        }

        // Update attack
        if (attackCooldownTimer > 0f) {
            attackCooldownTimer = (attackCooldownTimer - deltaTime).coerceAtLeast(0f)
        }
        if (isAttacking) {
            attackTimer -= deltaTime
            if (attackTimer <= 0f) {
                isAttacking = false
            }
        }

        // Update walking animation cycle
        if (isMoving) {
            walkCycleTimer += deltaTime * 8f
        }
    }
}
