package com.dungeonescape.utils

import com.dungeonescape.models.Position
import com.dungeonescape.models.Vector2D
import kotlin.math.floor

fun Float.clamp(min: Float, max: Float): Float = when {
    this < min -> min
    this > max -> max
    else -> this
}

fun Vector2D.toTileCoord(): Position {
    return Position(floor(this.x).toInt(), floor(this.y).toInt())
}

fun Position.toWorldCenter(): Vector2D {
    return Vector2D(this.x + 0.5f, this.y + 0.5f)
}
