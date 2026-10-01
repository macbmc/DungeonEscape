package com.dungeonescape.models

enum class TileType(val isWalkable: Boolean) {
    WALL(isWalkable = false),
    SECRET_WALL(isWalkable = false),
    FLOOR(isWalkable = true),
    COIN(isWalkable = true),
    KEY(isWalkable = true),
    EXIT_PORTAL(isWalkable = true),
    TREASURE_CHEST(isWalkable = false),
    MERCHANT_ALTAR(isWalkable = false),
    TRAP_SPIKE(isWalkable = true),
    HEALTH_POTION(isWalkable = true)
}
