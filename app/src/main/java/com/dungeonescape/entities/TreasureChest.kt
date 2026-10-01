package com.dungeonescape.entities

import com.dungeonescape.models.Vector2D

data class TreasureChest(
    val id: Int,
    val position: Vector2D,
    var isOpen: Boolean = false,
    val coinReward: Int = 35,
    var openProgress: Float = 0f
) {
    fun open(): Int {
        if (!isOpen) {
            isOpen = true
            return coinReward
        }
        return 0
    }

    fun update(deltaTime: Float) {
        if (isOpen && openProgress < 1f) {
            openProgress = (openProgress + deltaTime * 4f).coerceAtMost(1f)
        }
    }
}
