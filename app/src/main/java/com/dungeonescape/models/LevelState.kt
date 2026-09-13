package com.dungeonescape.models

data class LevelState(
    val levelNumber: Int = 1,
    val dungeonSize: Int = 25,
    val hasKey: Boolean = false,
    val portalActive: Boolean = false,
    val totalCoinsInLevel: Int = 0,
    val coinsCollectedInLevel: Int = 0,
    val enemiesDefeatedInLevel: Int = 0
)
