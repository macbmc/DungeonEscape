package com.dungeonescape.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.dungeonescape.entities.Coin
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.Particle
import com.dungeonescape.entities.Player
import com.dungeonescape.entities.Skeleton
import com.dungeonescape.models.Tile
import com.dungeonescape.models.TileType
import com.dungeonescape.models.Vector2D
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object RenderUtils {

    // Palette Colors
    private val WallColor = Color(0xFF1E222A)
    private val WallHighlightColor = Color(0xFF2E3440)
    private val WallShadowColor = Color(0xFF14171D)
    
    private val FloorColor1 = Color(0xFF3B4252)
    private val FloorColor2 = Color(0xFF434C5E)
    private val FloorGridLine = Color(0xFF2E3440)

    private val CoinGold = Color(0xFFFFD700)
    private val CoinBorder = Color(0xFFFFA000)
    
    private val KeyGold = Color(0xFFFFEA00)
    private val KeyGlow = Color(0x66FFD700)
    
    private val PortalInner = Color(0xFF7C4DFF)
    private val PortalOuter = Color(0xFF00E5FF)
    private val PortalLocked = Color(0xFF546E7A)

    private val PlayerBody = Color(0xFF448AFF)
    private val PlayerHead = Color(0xFF82B1FF)
    private val PlayerDashColor = Color(0xFF00E5FF)
    private val PlayerAttackArc = Color(0xFFE0F7FA)

    private val SkeletonBody = Color(0xFFECEFF1)
    private val SkeletonEye = Color(0xFFFF1744)

    /**
     * Renders a single dungeon tile with high visual polish.
     */
    fun drawTile(
        drawScope: DrawScope,
        tile: Tile,
        screenX: Float,
        screenY: Float,
        tileSize: Float
    ) {
        if (!tile.isExplored) {
            // Unexplored tiles are pitch black
            drawScope.drawRect(
                color = Color(0xFF0A0C10),
                topLeft = Offset(screenX, screenY),
                size = Size(tileSize, tileSize)
            )
            return
        }

        val alphaMultiplier = if (tile.isVisible) tile.lightLevel.coerceIn(0.2f, 1f) else 0.18f

        when (tile.type) {
            TileType.WALL -> {
                // Main Wall body
                drawScope.drawRect(
                    color = WallColor.copy(alpha = alphaMultiplier),
                    topLeft = Offset(screenX, screenY),
                    size = Size(tileSize, tileSize)
                )
                // 3D Wall top highlight
                drawScope.drawRect(
                    color = WallHighlightColor.copy(alpha = alphaMultiplier),
                    topLeft = Offset(screenX, screenY),
                    size = Size(tileSize, tileSize * 0.2f)
                )
                // Wall bottom shadow
                drawScope.drawRect(
                    color = WallShadowColor.copy(alpha = alphaMultiplier),
                    topLeft = Offset(screenX, screenY + tileSize * 0.85f),
                    size = Size(tileSize, tileSize * 0.15f)
                )
            }
            TileType.FLOOR, TileType.COIN, TileType.KEY, TileType.EXIT_PORTAL -> {
                // Checkerboard pattern for dungeon stones
                val baseColor = if ((tile.x + tile.y) % 2 == 0) FloorColor1 else FloorColor2
                drawScope.drawRect(
                    color = baseColor.copy(alpha = alphaMultiplier),
                    topLeft = Offset(screenX, screenY),
                    size = Size(tileSize, tileSize)
                )
                // Tile grid border
                drawScope.drawRect(
                    color = FloorGridLine.copy(alpha = alphaMultiplier * 0.7f),
                    topLeft = Offset(screenX, screenY),
                    size = Size(tileSize, tileSize),
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }

    /**
     * Draws a coin with bobbing and spinning effect.
     */
    fun drawCoin(
        drawScope: DrawScope,
        coin: Coin,
        screenX: Float,
        screenY: Float,
        tileSize: Float,
        isVisible: Boolean
    ) {
        if (coin.isCollected || !isVisible) return
        val centerY = screenY + (coin.visualOffsetY * tileSize)
        val radius = tileSize * 0.22f

        // Outer glow
        drawScope.drawCircle(
            color = Color(0x55FFD700),
            radius = radius * 1.3f,
            center = Offset(screenX, centerY)
        )
        // Main Coin
        drawScope.drawCircle(
            color = CoinGold,
            radius = radius,
            center = Offset(screenX, centerY)
        )
        // Inner rim
        drawScope.drawCircle(
            color = CoinBorder,
            radius = radius * 0.7f,
            center = Offset(screenX, centerY),
            style = Stroke(width = 2f)
        )
    }

    /**
     * Draws the Key item with pulsing magical glow.
     */
    fun drawKey(
        drawScope: DrawScope,
        key: KeyItem,
        screenX: Float,
        screenY: Float,
        tileSize: Float,
        isVisible: Boolean
    ) {
        if (key.isCollected || !isVisible) return
        val centerY = screenY + (key.visualOffsetY * tileSize)
        val headRadius = tileSize * 0.16f

        // Key Glow
        drawScope.drawCircle(
            color = KeyGlow.copy(alpha = key.glowAlpha * 0.6f),
            radius = tileSize * 0.35f,
            center = Offset(screenX, centerY)
        )

        // Key Head (ring)
        drawScope.drawCircle(
            color = KeyGold,
            radius = headRadius,
            center = Offset(screenX, centerY - headRadius * 0.5f),
            style = Stroke(width = 4f)
        )

        // Key Stem
        drawScope.drawLine(
            color = KeyGold,
            start = Offset(screenX, centerY + headRadius * 0.5f),
            end = Offset(screenX, centerY + headRadius * 2.2f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // Key Teeth
        drawScope.drawLine(
            color = KeyGold,
            start = Offset(screenX, centerY + headRadius * 1.8f),
            end = Offset(screenX + headRadius * 0.8f, centerY + headRadius * 1.8f),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )
    }

    /**
     * Draws the Exit Portal with swirling magic vortex.
     */
    fun drawPortal(
        drawScope: DrawScope,
        portal: ExitPortal,
        screenX: Float,
        screenY: Float,
        tileSize: Float,
        isVisible: Boolean
    ) {
        if (!isVisible) return
        val baseRadius = tileSize * 0.42f * portal.pulseScale

        if (portal.isActive) {
            // Active magical portal
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    listOf(PortalInner, PortalOuter, Color.Transparent),
                    center = Offset(screenX, screenY),
                    radius = baseRadius * 1.5f
                ),
                radius = baseRadius * 1.5f,
                center = Offset(screenX, screenY)
            )

            // Rotating vortex rings
            drawScope.rotate(portal.rotationAngle, pivot = Offset(screenX, screenY)) {
                drawCircle(
                    color = PortalOuter,
                    radius = baseRadius,
                    center = Offset(screenX, screenY),
                    style = Stroke(width = 3f)
                )
                drawLine(
                    color = Color.White,
                    start = Offset(screenX - baseRadius * 0.7f, screenY),
                    end = Offset(screenX + baseRadius * 0.7f, screenY),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
        } else {
            // Locked / dormant portal stone
            drawScope.drawCircle(
                color = PortalLocked,
                radius = baseRadius * 0.8f,
                center = Offset(screenX, screenY),
                style = Stroke(width = 3.5f)
            )
            drawScope.drawCircle(
                color = Color(0xFF37474F),
                radius = baseRadius * 0.5f,
                center = Offset(screenX, screenY)
            )
        }
    }

    /**
     * Draws the Player with directional movement, dash effects, sword swing arc, and damage flash.
     */
    fun drawPlayer(
        drawScope: DrawScope,
        player: Player,
        screenX: Float,
        screenY: Float,
        tileSize: Float
    ) {
        val playerRadius = tileSize * 0.32f

        // If invulnerable, flicker
        if (player.isInvulnerable && ((player.invulnerabilityTimer * 20).toInt() % 2 == 0)) {
            // Blink skip frame
            return
        }

        // Dash trail effect aura
        if (player.isDashing) {
            drawScope.drawCircle(
                color = PlayerDashColor.copy(alpha = 0.5f),
                radius = playerRadius * 1.4f,
                center = Offset(screenX, screenY)
            )
        }

        // Player Body
        val bodyColor = if (player.isDashing) PlayerDashColor else PlayerBody
        drawScope.drawCircle(
            color = bodyColor,
            radius = playerRadius,
            center = Offset(screenX, screenY)
        )

        // Player Head / Hat
        val lookOffset = player.facingDirection * (playerRadius * 0.3f)
        drawScope.drawCircle(
            color = PlayerHead,
            radius = playerRadius * 0.55f,
            center = Offset(screenX + lookOffset.x, screenY + lookOffset.y)
        )

        // Melee Attack Slash Arc
        if (player.isAttacking) {
            val facingAngle = atan2(player.facingDirection.y, player.facingDirection.x)
            val attackRadius = Constants.PLAYER_ATTACK_RADIUS * tileSize * 0.85f

            val swingAngleDeg = Math.toDegrees(facingAngle.toDouble()).toFloat()
            drawScope.rotate(swingAngleDeg, pivot = Offset(screenX, screenY)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Color.Transparent, PlayerAttackArc, Color.White),
                        center = Offset(screenX, screenY)
                    ),
                    startAngle = -60f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(screenX - attackRadius, screenY - attackRadius),
                    size = Size(attackRadius * 2, attackRadius * 2),
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }
        }
    }

    /**
     * Draws a Skeleton enemy with skull, glowing red eyes, and hit flash.
     */
    fun drawSkeleton(
        drawScope: DrawScope,
        skeleton: Skeleton,
        screenX: Float,
        screenY: Float,
        tileSize: Float,
        isVisible: Boolean
    ) {
        if (!skeleton.isAlive || !isVisible) return
        val radius = tileSize * 0.3f

        val color = if (skeleton.isHit) Color(0xFFFF5252) else SkeletonBody

        // Skull body
        drawScope.drawCircle(
            color = color,
            radius = radius,
            center = Offset(screenX, screenY)
        )

        // Jaw / Rib detail
        drawScope.drawRect(
            color = color,
            topLeft = Offset(screenX - radius * 0.6f, screenY + radius * 0.4f),
            size = Size(radius * 1.2f, radius * 0.5f)
        )

        // Glowing red eyes
        val eyeRadius = radius * 0.22f
        val eyeSpacing = radius * 0.35f
        drawScope.drawCircle(
            color = SkeletonEye,
            radius = eyeRadius,
            center = Offset(screenX - eyeSpacing, screenY - radius * 0.15f)
        )
        drawScope.drawCircle(
            color = SkeletonEye,
            radius = eyeRadius,
            center = Offset(screenX + eyeSpacing, screenY - radius * 0.15f)
        )

        // Health bar above enemy
        if (skeleton.health < skeleton.maxHealth) {
            val barWidth = tileSize * 0.7f
            val barHeight = 4f
            val barTop = screenY - radius - 10f
            val barLeft = screenX - barWidth / 2f
            val healthPercent = (skeleton.health / skeleton.maxHealth).coerceIn(0f, 1f)

            // Background
            drawScope.drawRect(
                color = Color(0xAA000000),
                topLeft = Offset(barLeft, barTop),
                size = Size(barWidth, barHeight)
            )
            // Health fill
            drawScope.drawRect(
                color = Color(0xFFFF1744),
                topLeft = Offset(barLeft, barTop),
                size = Size(barWidth * healthPercent, barHeight)
            )
        }
    }

    /**
     * Draws particle effects.
     */
    fun drawParticles(
        drawScope: DrawScope,
        particles: List<Particle>,
        cameraPos: Vector2D,
        screenWidth: Float,
        screenHeight: Float,
        tileSize: Float
    ) {
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f

        for (p in particles) {
            if (!p.isAlive) continue
            val screenX = centerX + (p.position.x - cameraPos.x) * tileSize
            val screenY = centerY + (p.position.y - cameraPos.y) * tileSize

            drawScope.drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(screenX, screenY)
            )
        }
    }

    /**
     * Draws the dynamic player torchlight radial vignette overlay.
     */
    fun drawTorchLighting(
        drawScope: DrawScope,
        screenWidth: Float,
        screenHeight: Float,
        visionRadiusTiles: Float,
        tileSize: Float
    ) {
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        val lightRadiusPx = visionRadiusTiles * tileSize

        // Smooth radial darkness gradient from center clear to edge black
        drawScope.drawRect(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent,
                    0.45f to Color.Transparent,
                    0.75f to Color(0x880A0C10),
                    1.0f to Color(0xFA0A0C10)
                ),
                center = Offset(centerX, centerY),
                radius = lightRadiusPx
            ),
            topLeft = Offset.Zero,
            size = Size(screenWidth, screenHeight)
        )
    }
}
