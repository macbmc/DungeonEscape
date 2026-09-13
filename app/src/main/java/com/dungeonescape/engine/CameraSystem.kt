package com.dungeonescape.engine

import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.Constants
import kotlin.math.max
import kotlin.math.min

class CameraSystem {
    var position: Vector2D = Vector2D.ZERO
        private set

    var targetPosition: Vector2D = Vector2D.ZERO
    private val smoothingSpeed: Float = 10f // lerp speed factor

    fun reset(initialPos: Vector2D) {
        position = initialPos
        targetPosition = initialPos
    }

    fun update(target: Vector2D, deltaTime: Float) {
        targetPosition = target
        // Exponential smoothing for buttery smooth 60fps tracking without stutter
        val factor = (1f - Math.exp((-smoothingSpeed * deltaTime).toDouble())).toFloat()
        position = position.lerp(targetPosition, factor)
    }

    /**
     * Computes the visible grid tile coordinate range based on screen dimensions and camera center.
     */
    fun getVisibleTileBounds(
        screenWidthPx: Float,
        screenHeightPx: Float,
        tileSizePx: Float,
        dungeonSize: Int
    ): VisibleBounds {
        val halfVisibleTilesX = (screenWidthPx / (2f * tileSizePx)) + 1.5f
        val halfVisibleTilesY = (screenHeightPx / (2f * tileSizePx)) + 1.5f

        val minX = max(0, (position.x - halfVisibleTilesX).toInt())
        val maxX = min(dungeonSize - 1, (position.x + halfVisibleTilesX).toInt())
        val minY = max(0, (position.y - halfVisibleTilesY).toInt())
        val maxY = min(dungeonSize - 1, (position.y + halfVisibleTilesY).toInt())

        return VisibleBounds(minX, maxX, minY, maxY)
    }

    data class VisibleBounds(
        val minX: Int,
        val maxX: Int,
        val minY: Int,
        val maxY: Int
    )
}
