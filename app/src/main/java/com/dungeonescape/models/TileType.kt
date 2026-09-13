package com.dungeonescape.models

enum class TileType(val isWalkable: Boolean) {
    WALL(isWalkable = false),
    FLOOR(isWalkable = true),
    COIN(isWalkable = true),
    KEY(isWalkable = true),
    EXIT_PORTAL(isWalkable = true)
}
