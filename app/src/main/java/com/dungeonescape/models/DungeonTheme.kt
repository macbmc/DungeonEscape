package com.dungeonescape.models

enum class DungeonTheme(val displayName: String, val levelRangeDescription: String) {
    ANCIENT_RUINS("Ancient Ruins", "Levels 1–5"),
    HAUNTED_CRYPT("Haunted Crypt", "Levels 6–10"),
    FROZEN_CAVERNS("Frozen Caverns", "Levels 11–15"),
    LAVA_FORTRESS("Lava Fortress", "Levels 16–20"),
    SHADOW_REALM("Shadow Realm", "Levels 21+")
}

enum class ThemeMode {
    PROGRESSIVE,
    LOCKED
}
