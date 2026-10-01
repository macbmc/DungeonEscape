package com.dungeonescape.entities

import com.dungeonescape.models.Vector2D
import kotlin.math.sin

data class MerchantAltar(
    val position: Vector2D,
    var animationTimer: Float = 0f
) {
    fun update(deltaTime: Float) {
        animationTimer += deltaTime * 2.5f
    }

    val glowAlpha: Float
        get() = 0.6f + 0.4f * sin(animationTimer)
}
