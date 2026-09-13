package com.dungeonescape.engine

import com.dungeonescape.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameLoop(private val gameEngine: GameEngine) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var loopJob: Job? = null
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true

        loopJob = scope.launch {
            var lastTimeNanos = System.nanoTime()
            val targetFrameTimeNanos = Constants.FRAME_TIME_NANOS

            while (isActive && isRunning) {
                val currentTimeNanos = System.nanoTime()
                val elapsedNanos = currentTimeNanos - lastTimeNanos
                lastTimeNanos = currentTimeNanos

                val deltaTime = (elapsedNanos / 1_000_000_000f).coerceIn(0f, Constants.MAX_DELTA_TIME)

                // Update Engine Systems
                gameEngine.update(deltaTime)

                // Sleep / delay balance to maintain 60 FPS
                val frameDurationNanos = System.nanoTime() - currentTimeNanos
                val sleepNanos = targetFrameTimeNanos - frameDurationNanos

                if (sleepNanos > 1_000_000) {
                    delay(sleepNanos / 1_000_000)
                } else {
                    // Small yield for thread fairness
                    delay(1)
                }
            }
        }
    }

    fun pause() {
        isRunning = false
        loopJob?.cancel()
        loopJob = null
    }

    fun resume() {
        start()
    }

    fun stop() {
        isRunning = false
        loopJob?.cancel()
        loopJob = null
    }
}
