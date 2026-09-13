package com.dungeonescape.models

import kotlin.math.abs
import kotlin.math.sqrt

data class Position(val x: Int, val y: Int) {
    fun distanceTo(other: Position): Float {
        val dx = (x - other.x).toFloat()
        val dy = (y - other.y).toFloat()
        return sqrt(dx * dx + dy * dy)
    }

    fun manhattanDistance(other: Position): Int {
        return abs(x - other.x) + abs(y - other.y)
    }

    fun toVector2D(tileSize: Float = 1f): Vector2D {
        return Vector2D(x * tileSize + tileSize / 2f, y * tileSize + tileSize / 2f)
    }
}
