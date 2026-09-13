package com.dungeonescape.entities

import com.dungeonescape.models.Vector2D
import kotlin.math.sin

data class ExitPortal(
    val position: Vector2D,
    var isActive: Boolean = false,
    var rotationAngle: Float = 0f,
    var pulseTimer: Float = 0f
) {
    fun update(deltaTime: Float) {
        if (isActive) {
            rotationAngle = (rotationAngle + deltaTime * 120f) % 360f
            pulseTimer += deltaTime * 5f
        } else {
            rotationAngle = (rotationAngle + deltaTime * 30f) % 360f
        }
    }

    val pulseScale: Float
        get() = if (isActive) 1f + 0.15f * sin(pulseTimer) else 0.9f
}
