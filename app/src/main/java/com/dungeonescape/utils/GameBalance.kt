package com.dungeonescape.utils

object GameBalance {
    // Player Health & Survival
    const val MAX_PLAYER_HEALTH = 100f
    const val LEVEL_COMPLETE_HEAL = 10f
    const val PLAYER_DAMAGE_INVULNERABILITY_TIME = 0.85f // 850 ms

    // World Health Potions (Level 2+)
    const val SMALL_WORLD_POTION_HEAL = 20f
    const val POTION_SPAWN_CHANCE_LEVEL_2 = 0.80f // 80% chance
    const val POTION_SPAWN_CHANCE_LEVEL_3 = 0.90f // 90% chance
    const val POTION_SPAWN_CHANCE_LEVEL_4_PLUS = 0.95f
    const val LOW_HEALTH_THRESHOLD = 35f

    // Secret Room Healing Altar (Level 3+)
    const val SMALL_SECRET_POTION_HEAL = 30f
    const val SMALL_POTION_COST = 30
    const val LARGE_SECRET_POTION_HEAL = 70f
    const val LARGE_POTION_COST = 65

    // Enemy Balance (Tuned so player survives 8-10 hits in Level 1)
    const val SKELETON_BASE_DAMAGE = 10f
    const val SKELETON_BASE_HEALTH = 35f
    const val SKELETON_BASE_SPEED = 2.0f
    const val SKELETON_ATTACK_COOLDOWN = 1.0f

    // Difficulty Scaling per level
    const val BASE_ENEMY_COUNT = 3
    const val ENEMY_COUNT_INCREASE_PER_LEVEL = 1
    const val ENEMY_SPEED_INCREASE_RATIO = 0.08f // +8% per level
    const val ENEMY_HEALTH_INCREASE_RATIO = 0.05f // +5% per level

    // Economy
    const val COIN_SCORE_VALUE = 10
    const val LEVEL_COMPLETE_BONUS = 100
    const val CHEST_COIN_REWARD = 40
}
