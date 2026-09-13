package com.dungeonescape.entities

import androidx.compose.ui.graphics.Color
import com.dungeonescape.models.Vector2D

data class Particle(
    var position: Vector2D,
    var velocity: Vector2D,
    var color: Color,
    var size: Float,
    var maxLifeTime: Float,
    var lifeTime: Float = 0f,
    var isAlive: Boolean = true
) {
    fun update(deltaTime: Float) {
        if (!isAlive) return
        lifeTime += deltaTime
        if (lifeTime >= maxLifeTime) {
            isAlive = false
            return
        }
        position += velocity * deltaTime
        velocity *= 0.94f // friction decay
    }

    val alpha: Float
        get() = ((1f - (lifeTime / maxLifeTime))).coerceIn(0f, 1f)
}
