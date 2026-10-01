package com.dungeonescape.entities

import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.GameBalance
import kotlin.math.sin

enum class PotionType {
    SMALL,
    LARGE
}

data class HealthPotion(
    val id: Int,
    val position: Vector2D,
    val type: PotionType = PotionType.SMALL,
    val healAmount: Float = GameBalance.SMALL_WORLD_POTION_HEAL,
    var isCollected: Boolean = false,
    var animationTimer: Float = 0f
) {
    fun update(deltaTime: Float) {
        animationTimer += deltaTime * 3.5f
    }

    val visualOffsetY: Float
        get() = sin(animationTimer) * 0.08f

    val glowAlpha: Float
        get() = 0.5f + 0.5f * sin(animationTimer * 1.5f)
}
