package com.dungeonescape.engine

import androidx.compose.ui.graphics.Color
import com.dungeonescape.entities.Particle
import com.dungeonescape.models.Vector2D
import kotlin.random.Random

class ParticleSystem {
    private val particles = mutableListOf<Particle>()

    fun getActiveParticles(): List<Particle> = particles

    fun update(deltaTime: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.update(deltaTime)
            if (!p.isAlive) {
                iterator.remove()
            }
        }
    }

    fun clear() {
        particles.clear()
    }

    fun spawnCoinPickup(position: Vector2D) {
        for (i in 0 until 12) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * 3f + 1.5f
            particles.add(
                Particle(
                    position = position,
                    velocity = Vector2D.fromAngle(angle, speed),
                    color = Color(0xFFFFD700), // Gold
                    size = Random.nextFloat() * 4f + 3f,
                    maxLifeTime = 0.4f
                )
            )
        }
    }

    fun spawnDashTrail(position: Vector2D, direction: Vector2D) {
        for (i in 0 until 6) {
            val offsetAngle = (Random.nextFloat() - 0.5f) * 0.8f
            val baseAngle = Math.atan2((-direction.y).toDouble(), (-direction.x).toDouble()).toFloat()
            val vel = Vector2D.fromAngle(baseAngle + offsetAngle, Random.nextFloat() * 2f + 1f)
            particles.add(
                Particle(
                    position = position + Vector2D((Random.nextFloat() - 0.5f) * 0.3f, (Random.nextFloat() - 0.5f) * 0.3f),
                    velocity = vel,
                    color = Color(0xFF00E5FF), // Cyan spark
                    size = Random.nextFloat() * 4f + 2f,
                    maxLifeTime = 0.25f
                )
            )
        }
    }

    fun spawnPortalActivation(position: Vector2D) {
        for (i in 0 until 20) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * 4f + 2f
            particles.add(
                Particle(
                    position = position,
                    velocity = Vector2D.fromAngle(angle, speed),
                    color = if (Random.nextBoolean()) Color(0xFFB388FF) else Color(0xFF64FFDA), // Purple / Teal
                    size = Random.nextFloat() * 5f + 3f,
                    maxLifeTime = 0.6f
                )
            )
        }
    }

    fun spawnEnemyDeath(position: Vector2D) {
        for (i in 0 until 18) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * 3.5f + 1f
            particles.add(
                Particle(
                    position = position,
                    velocity = Vector2D.fromAngle(angle, speed),
                    color = if (Random.nextBoolean()) Color(0xFFE0E0E0) else Color(0xFFFF5252), // Bone / Red
                    size = Random.nextFloat() * 4f + 2f,
                    maxLifeTime = 0.5f
                )
            )
        }
    }

    fun spawnPlayerHit(position: Vector2D) {
        for (i in 0 until 10) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * 3f + 1.5f
            particles.add(
                Particle(
                    position = position,
                    velocity = Vector2D.fromAngle(angle, speed),
                    color = Color(0xFFFF1744), // Crimson hit
                    size = Random.nextFloat() * 4f + 3f,
                    maxLifeTime = 0.35f
                )
            )
        }
    }
}
