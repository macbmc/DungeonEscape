package com.dungeonescape.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sin

class AudioManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var bgmJob: Job? = null
    private var isMuted: Boolean = false

    private val sampleRate = 22050
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
        // Pre-warm or link sound resources if present
    }

    fun playSound(type: SoundType) {
        if (isMuted) return

        val soundId = soundMap[type]
        if (soundId != null && soundId > 0) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            // High performance procedural sound generator fallback
            scope.launch {
                synthesizeSound(type)
            }
        }
    }

    fun startBgm() {
        if (bgmJob?.isActive == true) return
        bgmJob = scope.launch {
            val bassNotes = intArrayOf(110, 130, 146, 110, 98, 110, 146, 164)
            var noteIndex = 0
            while (isActive) {
                if (!isMuted) {
                    val freq = bassNotes[noteIndex % bassNotes.size].toFloat()
                    playTone(freq, 0.45f, 0.12f)
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

    private fun synthesizeSound(type: SoundType) {
        when (type) {
            SoundType.ATTACK -> {
                // Swoosh: frequency drops quickly
                playSweep(440f, 120f, 0.15f, 0.4f)
            }
            SoundType.ENEMY_HIT -> {
                // Crunchy thump
                playSweep(220f, 60f, 0.18f, 0.5f)
            }
            SoundType.PLAYER_DAMAGE -> {
                // Low grunt / alarm
                playSweep(180f, 80f, 0.25f, 0.6f)
            }
            SoundType.COIN_PICKUP -> {
                // Dual tone sparkle (Ding!)
                playTone(987f, 0.08f, 0.4f) // B5
                playTone(1318f, 0.12f, 0.4f) // E6
            }
            SoundType.KEY_PICKUP -> {
                // Magical fanfare
                playTone(523f, 0.1f, 0.4f) // C5
                playTone(659f, 0.1f, 0.4f) // E5
                playTone(783f, 0.1f, 0.4f) // G5
                playTone(1046f, 0.25f, 0.5f) // C6
            }
            SoundType.PORTAL_ACTIVATED -> {
                // Rising resonance
                playSweep(200f, 880f, 0.4f, 0.5f)
            }
            SoundType.DASH -> {
                // Quick whoosh
                playSweep(600f, 250f, 0.12f, 0.35f)
            }
            SoundType.VICTORY -> {
                playTone(523f, 0.15f, 0.4f)
                playTone(659f, 0.15f, 0.4f)
                playTone(783f, 0.15f, 0.4f)
                playTone(1046f, 0.4f, 0.5f)
            }
            SoundType.GAME_OVER -> {
                playSweep(300f, 90f, 0.6f, 0.6f)
            }
            SoundType.BGM -> {}
        }
    }

    private fun playTone(frequency: Float, durationSec: Float, volume: Float) {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val angularFreq = (2.0 * Math.PI * frequency) / sampleRate

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val envelope = (1f - progress) // Linear decay envelope
            val sample = (sin(angularFreq * i) * envelope * Short.MAX_VALUE * volume).toInt().toShort()
            buffer[i] = sample
        }

        val audioTrack = AudioTrack.Builder()
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

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        audioTrack.release()
    }

    private fun playSweep(startFreq: Float, endFreq: Float, durationSec: Float, volume: Float) {
        val numSamples = (durationSec * sampleRate).toInt()
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

        val audioTrack = AudioTrack.Builder()
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

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        audioTrack.release()
    }

    fun release() {
        stopBgm()
        soundPool.release()
    }
}
