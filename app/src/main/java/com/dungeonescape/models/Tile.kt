package com.dungeonescape.models

data class Tile(
    val x: Int,
    val y: Int,
    var type: TileType,
    var isExplored: Boolean = false,
    var isVisible: Boolean = false,
    var lightLevel: Float = 0f
) {
    val position: Position get() = Position(x, y)
    val isWalkable: Boolean get() = type.isWalkable
}
