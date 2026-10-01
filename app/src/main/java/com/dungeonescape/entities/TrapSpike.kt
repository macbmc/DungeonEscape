package com.dungeonescape.entities

import com.dungeonescape.models.Vector2D
import kotlin.math.sin

data class TrapSpike(
    val id: Int,
    val position: Vector2D,
    var isExtended: Boolean = false,
    var cycleTimer: Float = 0f,
    val damage: Float = 12f
) {
    fun update(deltaTime: Float) {
        cycleTimer = (cycleTimer + deltaTime) % 3.0f // 3 second cycle: 1.2s extended, 1.8s retracted
        isExtended = cycleTimer < 1.2f
    }

    val spikeProgress: Float
        get() = if (cycleTimer < 0.3f) {
            cycleTimer / 0.3f
        } else if (cycleTimer < 1.0f) {
            1.0f
        } else if (cycleTimer < 1.3f) {
            1.0f - (cycleTimer - 1.0f) / 0.3f
        } else {
            0.0f
        }
}
