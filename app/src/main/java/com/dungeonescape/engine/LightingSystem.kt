package com.dungeonescape.engine

import com.dungeonescape.models.Position
import com.dungeonescape.models.Tile
import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.Constants
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class LightingSystem {

    /**
     * Updates visibility and light levels for all tiles based on player's position.
     * Implements dynamic line-of-sight raycasting and smooth radial falloff.
     */
    fun updateLighting(
        playerPos: Vector2D,
        tiles: Array<Array<Tile>>,
        visionRadius: Float = Constants.PLAYER_VISION_RADIUS
    ) {
        val gridSize = tiles.size
        val playerTileX = playerPos.x.toInt().coerceIn(0, gridSize - 1)
        val playerTileY = playerPos.y.toInt().coerceIn(0, gridSize - 1)

        val minX = max(0, (playerPos.x - visionRadius - 1).toInt())
        val maxX = min(gridSize - 1, (playerPos.x + visionRadius + 1).toInt())
        val minY = max(0, (playerPos.y - visionRadius - 1).toInt())
        val maxY = min(gridSize - 1, (playerPos.y + visionRadius + 1).toInt())

        // First reset isVisible for all tiles (tiles keep isExplored = true if previously seen)
        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                tiles[y][x].isVisible = false
                tiles[y][x].lightLevel = if (tiles[y][x].isExplored) 0.15f else 0f
            }
        }

        // Raycast / distance check within bounding box
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val tileCenter = Vector2D(x + 0.5f, y + 0.5f)
                val dist = playerPos.distanceTo(tileCenter)

                if (dist <= visionRadius) {
                    if (hasLineOfSight(playerPos.x, playerPos.y, tileCenter.x, tileCenter.y, tiles, gridSize)) {
                        tiles[y][x].isVisible = true
                        tiles[y][x].isExplored = true

                        // Smooth radial falloff: 1.0 at center, fades to 0.2 at edges
                        val falloff = (1f - (dist / visionRadius)).coerceIn(0f, 1f)
                        val smoothFalloff = falloff * falloff * (3f - 2f * falloff) // Smoothstep
                        tiles[y][x].lightLevel = (0.25f + 0.75f * smoothFalloff).coerceIn(0f, 1f)
                    }
                }
            }
        }
    }

    /**
     * Bresenham-style Line of Sight test from source to destination.
     * Stops at wall tiles (the wall tile itself is visible).
     */
    private fun hasLineOfSight(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        tiles: Array<Array<Tile>>,
        gridSize: Int
    ): Boolean {
        val dx = abs(x1 - x0)
        val dy = abs(y1 - y0)

        val sx = if (x0 < x1) 0.2f else -0.2f
        val sy = if (y0 < y1) 0.2f else -0.2f

        var currX = x0
        var currY = y0

        val steps = max((dx / 0.2f).toInt(), (dy / 0.2f).toInt()).coerceAtLeast(1)
        val stepX = (x1 - x0) / steps
        val stepY = (y1 - y0) / steps

        for (i in 0 until steps - 1) {
            currX += stepX
            currY += stepY

            val tx = currX.toInt()
            val ty = currY.toInt()

            if (tx in 0 until gridSize && ty in 0 until gridSize) {
                if (!tiles[ty][tx].isWalkable) {
                    // Blocked by wall before reaching target
                    return false
                }
            }
        }
        return true
    }
}
