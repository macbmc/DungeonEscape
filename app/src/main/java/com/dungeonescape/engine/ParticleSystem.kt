package com.dungeonescape.engine

import androidx.compose.ui.graphics.Color
import com.dungeonescape.entities.Particle
import com.dungeonescape.models.Vector2D
import kotlin.random.Random

class ParticleSystem {
    private val lock = Any()
    private val particles = mutableListOf<Particle>()
    private val maxParticles = 120

    fun getActiveParticles(): List<Particle> = synchronized(lock) {
        particles.toList()
    }

    fun update(deltaTime: Float) = synchronized(lock) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.update(deltaTime)
            if (!p.isAlive) {
                iterator.remove()
            }
        }
    }

    fun clear() = synchronized(lock) {
        particles.clear()
    }

    private fun addParticleSafe(particle: Particle) = synchronized(lock) {
        if (particles.size >= maxParticles) {
            particles.removeAt(0)
        }
        particles.add(particle)
    }

    fun spawnCoinPickup(position: Vector2D) {
        for (i in 0 until 10) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * 3f + 1.5f
            addParticleSafe(
                Particle(
                    position = position,
                    velocity = Vector2D.fromAngle(angle, speed),
                    color = Color(0xFFFFD700),
                    size = Random.nextFloat() * 4f + 3f,
                    maxLifeTime = 0.35f
                )
            )
        }
    }

    fun spawnChestOpen(position: Vector2D) {
        for (i in 0 until 18) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * 4f + 2f
            addParticleSafe(
                Particle(
                    position = position,
                    velocity = Vector2D.fromAngle(angle, speed),
                    color = if (Random.nextBoolean()) Color(0xFFFFD700) else Color(0xFFFFAB00),
                    size = Random.nextFloat() * 5f + 3f,
                    maxLifeTime = 0.5f
                )
            )
        }
    }

    fun spawnHealEffect(position: Vector2D) {
        for (i in 0 until 12) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * 2.5f + 1f
            addParticleSafe(
                Particle(
                    position = position + Vector2D((Random.nextFloat() - 0.5f) * 0.2f, (Random.nextFloat() - 0.5f) * 0.2f),
                    velocity = Vector2D.fromAngle(angle, speed),
                    color = Color(0xFF00E676), // Healing Green
                    size = Random.nextFloat() * 4f + 2.5f,
                    maxLifeTime = 0.45f
                )
            )
        }
    }

    fun spawnWallHitDebris(position: Vector2D, isBroken: Boolean) {
        val count = if (isBroken) 16 else 6
        for (i in 0 until count) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * 3f + 1f
            addParticleSafe(
                Particle(
                    position = position,
                    velocity = Vector2D.fromAngle(angle, speed),
                    color = if (Random.nextBoolean()) Color(0xFF78909C) else Color(0xFFB0BEC5),
                    size = Random.nextFloat() * 4f + 2f,
                    maxLifeTime = 0.4f
                )
            )
        }
    }

    fun spawnDashTrail(position: Vector2D, direction: Vector2D) {
        for (i in 0 until 4) {
            val offsetAngle = (Random.nextFloat() - 0.5f) * 0.8f
            val baseAngle = Math.atan2((-direction.y).toDouble(), (-direction.x).toDouble()).toFloat()
            val vel = Vector2D.fromAngle(baseAngle + offsetAngle, Random.nextFloat() * 2f + 1f)
            addParticleSafe(
                Particle(
                    position = position + Vector2D((Random.nextFloat() - 0.5f) * 0.3f, (Random.nextFloat() - 0.5f) * 0.3f),
                    velocity = vel,
                    color = Color(0xFF00E5FF),
                    size = Random.nextFloat() * 4f + 2f,
                    maxLifeTime = 0.2f
                )
            )
        }
    }

    fun spawnPortalActivation(position: Vector2D) {
        for (i in 0 until 16) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * 4f + 2f
            addParticleSafe(
                Particle(
                    position = position,
                    velocity = Vector2D.fromAngle(angle, speed),
                    color = if (Random.nextBoolean()) Color(0xFFB388FF) else Color(0xFF64FFDA),
                    size = Random.nextFloat() * 5f + 3f,
                    maxLifeTime = 0.5f
                )
            )
        }
    }

    fun spawnEnemyDeath(position: Vector2D, isElite: Boolean = false) {
        val count = if (isElite) 20 else 12
        for (i in 0 until count) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * 3.5f + 1f
            val color = if (isElite) {
                if (Random.nextBoolean()) Color(0xFFFFD700) else Color(0xFFFF5252)
            } else {
                if (Random.nextBoolean()) Color(0xFFE0E0E0) else Color(0xFFFF5252)
            }
            addParticleSafe(
                Particle(
                    position = position,
                    velocity = Vector2D.fromAngle(angle, speed),
                    color = color,
                    size = Random.nextFloat() * 4f + 2f,
                    maxLifeTime = 0.4f
                )
            )
        }
    }

    fun spawnPlayerHit(position: Vector2D) {
        for (i in 0 until 8) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * 3f + 1.5f
            addParticleSafe(
                Particle(
                    position = position,
                    velocity = Vector2D.fromAngle(angle, speed),
                    color = Color(0xFFFF1744),
                    size = Random.nextFloat() * 4f + 3f,
                    maxLifeTime = 0.3f
                )
            )
        }
    }
}
