package com.dungeonescape.engine

import android.content.Context
import android.content.SharedPreferences
import com.dungeonescape.models.DungeonTheme
import com.dungeonescape.models.ThemeMode

data class SavedGameSession(
    val level: Int,
    val score: Int,
    val playerHealth: Float,
    val coins: Int,
    val themeMode: ThemeMode,
    val lockedTheme: DungeonTheme
)

class GameStateManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dungeon_escape_prefs", Context.MODE_PRIVATE)

    var highScore: Int
        get() = prefs.getInt("high_score", 0)
        set(value) {
            if (value > highScore) {
                prefs.edit().putInt("high_score", value).apply()
            }
        }

    var highestLevel: Int
        get() = prefs.getInt("highest_level", 1)
        set(value) {
            if (value > highestLevel) {
                prefs.edit().putInt("highest_level", value).apply()
            }
        }

    var highestCoins: Int
        get() = prefs.getInt("highest_coins", 0)
        set(value) {
            if (value > highestCoins) {
                prefs.edit().putInt("highest_coins", value).apply()
            }
        }

    var musicVolume: Float
        get() = prefs.getFloat("setting_music_volume", 0.8f)
        set(value) {
            prefs.edit().putFloat("setting_music_volume", value.coerceIn(0f, 1f)).apply()
        }

    var sfxVolume: Float
        get() = prefs.getFloat("setting_sfx_volume", 1.0f)
        set(value) {
            prefs.edit().putFloat("setting_sfx_volume", value.coerceIn(0f, 1f)).apply()
        }

    var musicEnabled: Boolean
        get() = prefs.getBoolean("setting_music_enabled", true)
        set(value) {
            prefs.edit().putBoolean("setting_music_enabled", value).apply()
        }

    var sfxEnabled: Boolean
        get() = prefs.getBoolean("setting_sfx_enabled", true)
        set(value) {
            prefs.edit().putBoolean("setting_sfx_enabled", value).apply()
        }

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean("setting_vibration_enabled", true)
        set(value) {
            prefs.edit().putBoolean("setting_vibration_enabled", value).apply()
        }

    var tutorialCompleted: Boolean
        get() = prefs.getBoolean("tutorial_completed", false)
        set(value) {
            prefs.edit().putBoolean("tutorial_completed", value).apply()
        }

    fun recordScore(score: Int, level: Int, coins: Int = 0) {
        if (score > highScore) {
            highScore = score
        }
        if (level > highestLevel) {
            highestLevel = level
        }
        if (coins > highestCoins) {
            highestCoins = coins
        }
        clearSavedGameSession()
    }

    val hasSavedGame: Boolean
        get() = prefs.getBoolean("has_saved_game", false)

    fun saveGameSession(
        level: Int,
        score: Int,
        playerHealth: Float,
        coins: Int,
        themeMode: ThemeMode,
        lockedTheme: DungeonTheme
    ) {
        prefs.edit()
            .putBoolean("has_saved_game", true)
            .putInt("saved_level", level)
            .putInt("saved_score", score)
            .putFloat("saved_health", playerHealth)
            .putInt("saved_coins", coins)
            .putString("saved_theme_mode", themeMode.name)
            .putString("saved_locked_theme", lockedTheme.name)
            .apply()
    }

    fun loadSavedGameSession(): SavedGameSession? {
        if (!hasSavedGame) return null
        val level = prefs.getInt("saved_level", 1)
        val score = prefs.getInt("saved_score", 0)
        val health = prefs.getFloat("saved_health", 100f)
        val coins = prefs.getInt("saved_coins", 0)
        val themeModeStr = prefs.getString("saved_theme_mode", ThemeMode.PROGRESSIVE.name) ?: ThemeMode.PROGRESSIVE.name
        val lockedThemeStr = prefs.getString("saved_locked_theme", DungeonTheme.ANCIENT_RUINS.name) ?: DungeonTheme.ANCIENT_RUINS.name

        val themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (_: Exception) { ThemeMode.PROGRESSIVE }
        val lockedTheme = try { DungeonTheme.valueOf(lockedThemeStr) } catch (_: Exception) { DungeonTheme.ANCIENT_RUINS }

        return SavedGameSession(level, score, health, coins, themeMode, lockedTheme)
    }

    fun clearSavedGameSession() {
        prefs.edit().putBoolean("has_saved_game", false).apply()
    }
}
