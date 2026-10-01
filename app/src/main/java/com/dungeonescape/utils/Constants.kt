package com.dungeonescape.utils

object Constants {
    const val DUNGEON_SIZE = 25
    const val TILE_SIZE = 64f

    // Player
    const val PLAYER_MAX_HEALTH = GameBalance.MAX_PLAYER_HEALTH
    const val PLAYER_SPEED = 4.0f // tiles per second
    const val PLAYER_VISION_RADIUS = 6.0f // tiles
    const val PLAYER_COLLISION_RADIUS = 0.35f // tiles
    
    // Combat
    const val PLAYER_ATTACK_RADIUS = 1.35f // tiles
    const val PLAYER_ATTACK_DAMAGE = 35f
    const val PLAYER_ATTACK_COOLDOWN = 0.35f // seconds
    const val PLAYER_INVULNERABILITY_TIME = GameBalance.PLAYER_DAMAGE_INVULNERABILITY_TIME
    
    // Dash
    const val DASH_SPEED_MULTIPLIER = 3.5f
    const val DASH_DURATION = 0.18f // seconds
    const val DASH_COOLDOWN = 3.0f // seconds

    // Enemy (Skeleton)
    const val SKELETON_BASE_HEALTH = GameBalance.SKELETON_BASE_HEALTH
    const val SKELETON_BASE_SPEED = GameBalance.SKELETON_BASE_SPEED
    const val SKELETON_BASE_DAMAGE = GameBalance.SKELETON_BASE_DAMAGE
    const val SKELETON_DETECTION_RADIUS = 5.5f // tiles
    const val SKELETON_COLLISION_RADIUS = 0.35f // tiles
    const val SKELETON_ATTACK_COOLDOWN = GameBalance.SKELETON_ATTACK_COOLDOWN
    const val SKELETON_ATTACK_RANGE = 0.7f // tiles

    // Difficulty scaling per level
    const val BASE_ENEMY_COUNT = GameBalance.BASE_ENEMY_COUNT
    const val ENEMY_COUNT_INCREASE_PER_LEVEL = GameBalance.ENEMY_COUNT_INCREASE_PER_LEVEL
    const val ENEMY_SPEED_INCREASE_RATIO = GameBalance.ENEMY_SPEED_INCREASE_RATIO
    const val ENEMY_HEALTH_INCREASE_RATIO = GameBalance.ENEMY_HEALTH_INCREASE_RATIO

    // Pickups & Scoring
    const val COIN_SCORE_VALUE = GameBalance.COIN_SCORE_VALUE
    const val LEVEL_COMPLETE_BONUS = GameBalance.LEVEL_COMPLETE_BONUS

    // Engine
    const val TARGET_FPS = 60
    const val FRAME_TIME_NANOS = 1_000_000_000L / TARGET_FPS
    const val MAX_DELTA_TIME = 0.05f // clamp delta to 50ms to prevent physics skipping
}
