package com.dungeonescape.engine

import android.content.Context
import android.content.SharedPreferences

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

    fun recordScore(score: Int, level: Int) {
        if (score > highScore) {
            highScore = score
        }
        if (level > highestLevel) {
            highestLevel = level
        }
    }
}
