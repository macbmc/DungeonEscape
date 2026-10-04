package com.dungeonescape.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibrationManager {

    fun vibrateLight(context: Context, enabled: Boolean = true) {
        if (!enabled) return
        vibrate(context, 20L, 50)
    }

    fun vibrateImpact(context: Context, enabled: Boolean = true) {
        if (!enabled) return
        vibrate(context, 45L, 180)
    }

    fun vibrateVictory(context: Context, enabled: Boolean = true) {
        if (!enabled) return
        vibrate(context, 80L, 220)
    }

    private fun vibrate(context: Context, durationMs: Long, amplitude: Int) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(
                        durationMs,
                        amplitude.coerceIn(1, 255)
                    )
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (t: Throwable) {
            AppLogger.w("VibrationManager", "Vibration failed: ${t.message}")
        }
    }
}
