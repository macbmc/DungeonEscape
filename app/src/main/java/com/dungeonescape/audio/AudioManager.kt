package com.dungeonescape.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.os.SystemClock
import android.util.Log
import com.dungeonescape.models.DungeonTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sin

class AudioManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var bgmJob: Job? = null
    private var soundWorkerJob: Job? = null
    private var isMuted: Boolean = false
    private var currentTheme: DungeonTheme = DungeonTheme.ANCIENT_RUINS

    private val sampleRate = 22050
    private val lastPlayedTimeMap = ConcurrentHashMap<SoundType, Long>()
    private val soundQueue = Channel<SoundType>(capacity = 16)

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundMap = ConcurrentHashMap<SoundType, Int>()

    init {
        startSoundWorker()
    }

    private fun startSoundWorker() {
        soundWorkerJob = scope.launch {
            for (type in soundQueue) {
                if (!isActive) break
                try {
                    synthesizeSoundSafe(type)
                } catch (t: Throwable) {
                    Log.w("AudioManager", "Error playing sound $type: ${t.message}")
                }
            }
        }
    }

    fun setTheme(theme: DungeonTheme) {
        currentTheme = theme
    }

    fun playSound(type: SoundType) {
        if (isMuted) return

        val now = SystemClock.uptimeMillis()
        val lastPlayed = lastPlayedTimeMap[type] ?: 0L
        val minIntervalMs = when (type) {
            SoundType.ATTACK -> 100L
            SoundType.ENEMY_HIT -> 80L
            SoundType.PLAYER_DAMAGE -> 120L
            SoundType.COIN_PICKUP -> 50L
            SoundType.DASH -> 150L
            SoundType.SECRET_WALL_HIT -> 100L
            SoundType.TRAP_HIT -> 150L
            else -> 60L
        }

        if (now - lastPlayed < minIntervalMs) {
            return
        }
        lastPlayedTimeMap[type] = now

        val soundId = soundMap[type]
        if (soundId != null && soundId > 0) {
            try {
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            } catch (t: Throwable) {
                Log.w("AudioManager", "SoundPool error: ${t.message}")
            }
        } else {
            soundQueue.trySend(type)
        }
    }

    fun startBgm() {
        if (bgmJob?.isActive == true) return
        bgmJob = scope.launch {
            var noteIndex = 0
            while (isActive) {
                if (!isMuted) {
                    val notes = when (currentTheme) {
                        DungeonTheme.ANCIENT_RUINS -> intArrayOf(110, 130, 146, 110, 98, 110, 146, 164) // A-minor
                        DungeonTheme.HAUNTED_CRYPT -> intArrayOf(92, 110, 123, 92, 82, 110, 123, 146) // Dark crypt
                        DungeonTheme.FROZEN_CAVERNS -> intArrayOf(130, 164, 196, 130, 146, 196, 220, 164) // Crisp cold
                        DungeonTheme.LAVA_FORTRESS -> intArrayOf(73, 87, 110, 73, 65, 87, 110, 130) // Heavy low magma
                        DungeonTheme.SHADOW_REALM -> intArrayOf(65, 77, 98, 65, 55, 77, 98, 116) // Deep void bass
                    }
                    val freq = notes[noteIndex % notes.size].toFloat()
                    try {
                        playToneSafe(freq, 0.40f, 0.08f)
                    } catch (t: Throwable) {
                        Log.w("AudioManager", "BGM note error: ${t.message}")
                    }
                    noteIndex++
                }
                delay(400)
            }
        }
    }

    fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        return isMuted
    }

    private fun synthesizeSoundSafe(type: SoundType) {
        when (type) {
            SoundType.ATTACK -> playSweepSafe(440f, 120f, 0.12f, 0.35f)
            SoundType.ENEMY_HIT -> playSweepSafe(220f, 60f, 0.14f, 0.45f)
            SoundType.PLAYER_DAMAGE -> playSweepSafe(180f, 80f, 0.20f, 0.55f)
            SoundType.COIN_PICKUP -> {
                playToneSafe(987f, 0.07f, 0.35f)
                playToneSafe(1318f, 0.10f, 0.35f)
            }
            SoundType.KEY_PICKUP -> {
                playToneSafe(523f, 0.08f, 0.35f)
                playToneSafe(659f, 0.08f, 0.35f)
                playToneSafe(783f, 0.08f, 0.35f)
                playToneSafe(1046f, 0.20f, 0.45f)
            }
            SoundType.PORTAL_ACTIVATED -> playSweepSafe(200f, 880f, 0.35f, 0.45f)
            SoundType.DASH -> playSweepSafe(600f, 250f, 0.10f, 0.30f)
            SoundType.VICTORY -> {
                playToneSafe(523f, 0.12f, 0.35f)
                playToneSafe(659f, 0.12f, 0.35f)
                playToneSafe(783f, 0.12f, 0.35f)
                playToneSafe(1046f, 0.30f, 0.45f)
            }
            SoundType.GAME_OVER -> playSweepSafe(300f, 90f, 0.50f, 0.55f)
            SoundType.SECRET_WALL_HIT -> playSweepSafe(320f, 180f, 0.10f, 0.40f)
            SoundType.SECRET_WALL_BREAK -> {
                playSweepSafe(280f, 60f, 0.25f, 0.60f)
                playSweepSafe(150f, 40f, 0.30f, 0.50f)
            }
            SoundType.CHEST_OPEN -> {
                playToneSafe(659f, 0.10f, 0.40f)
                playToneSafe(880f, 0.15f, 0.45f)
                playToneSafe(1174f, 0.25f, 0.50f)
            }
            SoundType.POTION_PURCHASE -> {
                playToneSafe(440f, 0.10f, 0.35f)
                playToneSafe(659f, 0.12f, 0.40f)
                playToneSafe(880f, 0.20f, 0.45f)
            }
            SoundType.TRAP_HIT -> playSweepSafe(240f, 100f, 0.15f, 0.55f)
            SoundType.BGM -> {}
        }
    }

    private fun playToneSafe(frequency: Float, durationSec: Float, volume: Float) {
        var audioTrack: AudioTrack? = null
        try {
            val numSamples = (durationSec * sampleRate).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            val angularFreq = (2.0 * Math.PI * frequency) / sampleRate

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val envelope = (1f - progress)
                val sample = (sin(angularFreq * i) * envelope * Short.MAX_VALUE * volume).toInt().toShort()
                buffer[i] = sample
            }

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                Thread.sleep((durationSec * 1000).toLong().coerceAtLeast(10))
            }
        } catch (t: Throwable) {
            Log.w("AudioManager", "playToneSafe exception: ${t.message}")
        } finally {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Throwable) {}
        }
    }

    private fun playSweepSafe(startFreq: Float, endFreq: Float, durationSec: Float, volume: Float) {
        var audioTrack: AudioTrack? = null
        try {
            val numSamples = (durationSec * sampleRate).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            var phase = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val envelope = (1f - progress)
                phase += (2.0 * Math.PI * currentFreq) / sampleRate
                val sample = (sin(phase) * envelope * Short.MAX_VALUE * volume).toInt().toShort()
                buffer[i] = sample
            }

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                Thread.sleep((durationSec * 1000).toLong().coerceAtLeast(10))
            }
        } catch (t: Throwable) {
            Log.w("AudioManager", "playSweepSafe exception: ${t.message}")
        } finally {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Throwable) {}
        }
    }

    fun release() {
        stopBgm()
        soundWorkerJob?.cancel()
        soundQueue.close()
        try {
            soundPool.release()
        } catch (_: Throwable) {}
    }
}
