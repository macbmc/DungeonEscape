package com.dungeonescape.models

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(
    val x: Float = 0f,
    val y: Float = 0f
) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float): Vector2D = if (scalar != 0f) Vector2D(x / scalar, y / scalar) else ZERO

    fun lengthSquared(): Float = x * x + y * y
    fun length(): Float = sqrt(lengthSquared())

    fun distanceTo(other: Vector2D): Float = (this - other).length()

    fun normalized(): Vector2D {
        val len = length()
        return if (len > 0.0001f) Vector2D(x / len, y / len) else ZERO
    }

    fun lerp(target: Vector2D, factor: Float): Vector2D {
        val clampedFactor = factor.coerceIn(0f, 1f)
        return Vector2D(
            x + (target.x - x) * clampedFactor,
            y + (target.y - y) * clampedFactor
        )
    }

    fun toGridPosition(tileSize: Float): Position {
        return Position(
            (x / tileSize).toInt(),
            (y / tileSize).toInt()
        )
    }

    companion object {
        val ZERO = Vector2D(0f, 0f)
        val UP = Vector2D(0f, -1f)
        val DOWN = Vector2D(0f, 1f)
        val LEFT = Vector2D(-1f, 0f)
        val RIGHT = Vector2D(1f, 0f)

        fun fromAngle(angleRadians: Float, length: Float = 1f): Vector2D {
            return Vector2D(cos(angleRadians) * length, sin(angleRadians) * length)
        }
    }
}
