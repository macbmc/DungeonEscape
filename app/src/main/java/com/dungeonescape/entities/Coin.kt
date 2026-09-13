package com.dungeonescape.entities

import com.dungeonescape.models.Vector2D
import kotlin.math.sin

data class Coin(
    val id: Int,
    val position: Vector2D,
    var isCollected: Boolean = false,
    var animationTimer: Float = 0f
) {
    fun update(deltaTime: Float) {
        animationTimer += deltaTime * 4f
    }

    val visualOffsetY: Float
        get() = sin(animationTimer) * 0.1f // subtle floating bobbing effect
}
