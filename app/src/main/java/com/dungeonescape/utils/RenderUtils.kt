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
import com.dungeonescape.entities.EnemyVariant
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.HealthPotion
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.MerchantAltar
import com.dungeonescape.entities.Particle
import com.dungeonescape.entities.Player
import com.dungeonescape.entities.PotionType
import com.dungeonescape.entities.Skeleton
import com.dungeonescape.entities.TrapSpike
import com.dungeonescape.entities.TreasureChest
import com.dungeonescape.models.ThemeAssets
import com.dungeonescape.models.Tile
import com.dungeonescape.models.TileType
import com.dungeonescape.models.Vector2D
import kotlin.math.atan2

object RenderUtils {

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
    private val EliteAura = Color(0xFFFFD700)
    private val GuardianAura = Color(0xFFFF6D00)
    private val ShadowEye = Color(0xFFEA80FC)

    /**
     * Renders a single dungeon tile with theme-specific textures and secret wall indicators.
     */
    fun drawTile(
        drawScope: DrawScope,
        tile: Tile,
        screenX: Float,
        screenY: Float,
        tileSize: Float,
        themeAssets: ThemeAssets
    ) {
        if (!tile.isExplored) {
            drawScope.drawRect(
                color = themeAssets.ambientDarkColor,
                topLeft = Offset(screenX, screenY),
                size = Size(tileSize, tileSize)
            )
            return
        }

        val alphaMultiplier = if (tile.isVisible) tile.lightLevel.coerceIn(0.2f, 1f) else 0.18f

        when (tile.type) {
            TileType.WALL, TileType.SECRET_WALL -> {
                // Wall body
                drawScope.drawRect(
                    color = themeAssets.wallColor.copy(alpha = alphaMultiplier),
                    topLeft = Offset(screenX, screenY),
                    size = Size(tileSize, tileSize)
                )
                // 3D Highlight
                drawScope.drawRect(
                    color = themeAssets.wallHighlightColor.copy(alpha = alphaMultiplier),
                    topLeft = Offset(screenX, screenY),
                    size = Size(tileSize, tileSize * 0.22f)
                )
                // Shadow
                drawScope.drawRect(
                    color = themeAssets.wallShadowColor.copy(alpha = alphaMultiplier),
                    topLeft = Offset(screenX, screenY + tileSize * 0.82f),
                    size = Size(tileSize, tileSize * 0.18f)
                )

                // Secret Wall subtle crack clue
                if (tile.type == TileType.SECRET_WALL && tile.isVisible) {
                    val crackColor = Color(0x66FFFFFF)
                    val crackPath = Path().apply {
                        moveTo(screenX + tileSize * 0.3f, screenY + tileSize * 0.25f)
                        lineTo(screenX + tileSize * 0.5f, screenY + tileSize * 0.55f)
                        lineTo(screenX + tileSize * 0.42f, screenY + tileSize * 0.78f)
                    }
                    drawScope.drawPath(crackPath, crackColor, style = Stroke(width = if (tile.secretWallHits < 3) 2.5f else 1.5f))
                }
            }
            else -> {
                // Floor Tile with Theme Checkerboard
                val baseColor = if ((tile.x + tile.y) % 2 == 0) themeAssets.floorColor1 else themeAssets.floorColor2
                drawScope.drawRect(
                    color = baseColor.copy(alpha = alphaMultiplier),
                    topLeft = Offset(screenX, screenY),
                    size = Size(tileSize, tileSize)
                )
                // Grid borders
                drawScope.drawRect(
                    color = themeAssets.floorGridLine.copy(alpha = alphaMultiplier * 0.8f),
                    topLeft = Offset(screenX, screenY),
                    size = Size(tileSize, tileSize),
                    style = Stroke(width = 1.2f)
                )
            }
        }
    }

    fun drawTreasureChest(
        drawScope: DrawScope,
        chest: TreasureChest,
        screenX: Float,
        screenY: Float,
        tileSize: Float,
        isVisible: Boolean
    ) {
        if (!isVisible) return
        val w = tileSize * 0.65f
        val h = tileSize * 0.50f
        val left = screenX - w / 2f
        val top = screenY - h / 2f

        val chestWood = if (chest.isOpen) Color(0xFF6D4C41) else Color(0xFF8D6E63)
        val chestGold = Color(0xFFFFD700)

        // Chest Body
        drawScope.drawRect(
            color = chestWood,
            topLeft = Offset(left, top + h * 0.3f),
            size = Size(w, h * 0.7f)
        )
        // Gold bands
        drawScope.drawRect(
            color = chestGold,
            topLeft = Offset(left + w * 0.15f, top + h * 0.3f),
            size = Size(w * 0.15f, h * 0.7f)
        )
        drawScope.drawRect(
            color = chestGold,
            topLeft = Offset(left + w * 0.70f, top + h * 0.3f),
            size = Size(w * 0.15f, h * 0.7f)
        )

        // Chest Lid (Rotates up if open)
        if (chest.isOpen) {
            drawScope.drawRect(
                color = Color(0xFF4E342E),
                topLeft = Offset(left, top - h * 0.2f),
                size = Size(w, h * 0.35f)
            )
            // Inner gold glow
            drawScope.drawCircle(
                color = Color(0x99FFD700),
                radius = w * 0.35f,
                center = Offset(screenX, top + h * 0.2f)
            )
        } else {
            drawScope.drawRect(
                color = Color(0xFFA1887F),
                topLeft = Offset(left, top),
                size = Size(w, h * 0.35f)
            )
            // Keyhole
            drawScope.drawCircle(
                color = chestGold,
                radius = 4f,
                center = Offset(screenX, top + h * 0.45f)
            )
        }
    }

    fun drawMerchantAltar(
        drawScope: DrawScope,
        altar: MerchantAltar,
        screenX: Float,
        screenY: Float,
        tileSize: Float,
        isVisible: Boolean
    ) {
        if (!isVisible) return
        val radius = tileSize * 0.40f

        // Magical Rune Aura
        drawScope.drawCircle(
            color = Color(0x6600E676).copy(alpha = altar.glowAlpha * 0.5f),
            radius = radius * 1.3f,
            center = Offset(screenX, screenY)
        )

        // Stone Pedestal
        drawScope.drawCircle(
            color = Color(0xFF37474F),
            radius = radius,
            center = Offset(screenX, screenY)
        )
        drawScope.drawCircle(
            color = Color(0xFF00E676),
            radius = radius,
            center = Offset(screenX, screenY),
            style = Stroke(width = 2.5f)
        )

        // Potion Crystal Flask in Center
        drawScope.drawCircle(
            color = Color(0xFF00E676),
            radius = radius * 0.45f,
            center = Offset(screenX, screenY)
        )
        drawScope.drawCircle(
            color = Color.White,
            radius = radius * 0.2f,
            center = Offset(screenX - radius * 0.1f, screenY - radius * 0.1f)
        )
    }

    fun drawTrapSpike(
        drawScope: DrawScope,
        trap: TrapSpike,
        screenX: Float,
        screenY: Float,
        tileSize: Float,
        isVisible: Boolean
    ) {
        if (!isVisible) return
        val w = tileSize * 0.7f
        val left = screenX - w / 2f
        val top = screenY - w / 2f

        // Floor Grate
        drawScope.drawRect(
            color = Color(0xFF263238),
            topLeft = Offset(left, top),
            size = Size(w, w)
        )

        // Extending Spikes
        val spikeProg = trap.spikeProgress
        if (spikeProg > 0.05f) {
            val spikeColor = if (trap.isExtended) Color(0xFFFF1744) else Color(0xFFB0BEC5)
            val spikeLen = (w * 0.35f) * spikeProg

            for (i in -1..1) {
                for (j in -1..1) {
                    val sx = screenX + i * (w * 0.3f)
                    val sy = screenY + j * (w * 0.3f)
                    drawScope.drawCircle(
                        color = spikeColor,
                        radius = 3.5f * spikeProg,
                        center = Offset(sx, sy)
                    )
                }
            }
        }
    }

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

        drawScope.drawCircle(
            color = Color(0x55FFD700),
            radius = radius * 1.3f,
            center = Offset(screenX, centerY)
        )
        drawScope.drawCircle(
            color = CoinGold,
            radius = radius,
            center = Offset(screenX, centerY)
        )
        drawScope.drawCircle(
            color = CoinBorder,
            radius = radius * 0.7f,
            center = Offset(screenX, centerY),
            style = Stroke(width = 2f)
        )
    }

    fun drawHealthPotion(
        drawScope: DrawScope,
        potion: HealthPotion,
        screenX: Float,
        screenY: Float,
        tileSize: Float,
        isVisible: Boolean
    ) {
        if (potion.isCollected || !isVisible) return
        val centerY = screenY + (potion.visualOffsetY * tileSize)
        val baseRadius = if (potion.type == PotionType.LARGE) tileSize * 0.26f else tileSize * 0.20f

        val liquidColor = if (potion.type == PotionType.LARGE) Color(0xFF00E676) else Color(0xFFFF1744)
        val glowColor = if (potion.type == PotionType.LARGE) Color(0x6600E676) else Color(0x66FF1744)
        val corkColor = Color(0xFF8D6E63)

        // 1. Soft Pulsing Aura Glow
        drawScope.drawCircle(
            color = glowColor.copy(alpha = potion.glowAlpha * 0.6f),
            radius = baseRadius * 1.5f,
            center = Offset(screenX, centerY)
        )

        // 2. Glass Flask Bulb
        drawScope.drawCircle(
            color = Color(0x33FFFFFF),
            radius = baseRadius,
            center = Offset(screenX, centerY + baseRadius * 0.15f)
        )
        drawScope.drawCircle(
            color = Color(0xAAFFFFFF),
            radius = baseRadius,
            center = Offset(screenX, centerY + baseRadius * 0.15f),
            style = Stroke(width = 2f)
        )

        // 3. Liquid inside Flask
        drawScope.drawCircle(
            color = liquidColor,
            radius = baseRadius * 0.72f,
            center = Offset(screenX, centerY + baseRadius * 0.22f)
        )

        // 4. Flask Neck & Cork Stopper
        val neckWidth = baseRadius * 0.65f
        val neckHeight = baseRadius * 0.5f
        // Glass Neck
        drawScope.drawRect(
            color = Color(0x88FFFFFF),
            topLeft = Offset(screenX - neckWidth / 2f, centerY - baseRadius * 0.65f),
            size = Size(neckWidth, neckHeight)
        )
        // Cork
        drawScope.drawRect(
            color = corkColor,
            topLeft = Offset(screenX - neckWidth * 0.45f, centerY - baseRadius * 0.95f),
            size = Size(neckWidth * 0.9f, neckHeight * 0.7f)
        )

        // 5. Specular highlight (glimmer)
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = baseRadius * 0.22f,
            center = Offset(screenX - baseRadius * 0.35f, centerY - baseRadius * 0.15f)
        )
    }

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

        drawScope.drawCircle(
            color = KeyGlow.copy(alpha = key.glowAlpha * 0.6f),
            radius = tileSize * 0.35f,
            center = Offset(screenX, centerY)
        )

        drawScope.drawCircle(
            color = KeyGold,
            radius = headRadius,
            center = Offset(screenX, centerY - headRadius * 0.5f),
            style = Stroke(width = 4f)
        )

        drawScope.drawLine(
            color = KeyGold,
            start = Offset(screenX, centerY + headRadius * 0.5f),
            end = Offset(screenX, centerY + headRadius * 2.2f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        drawScope.drawLine(
            color = KeyGold,
            start = Offset(screenX, centerY + headRadius * 1.8f),
            end = Offset(screenX + headRadius * 0.8f, centerY + headRadius * 1.8f),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )
    }

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
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    listOf(PortalInner, PortalOuter, Color.Transparent),
                    center = Offset(screenX, screenY),
                    radius = baseRadius * 1.5f
                ),
                radius = baseRadius * 1.5f,
                center = Offset(screenX, screenY)
            )

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

    fun drawPlayer(
        drawScope: DrawScope,
        player: Player,
        screenX: Float,
        screenY: Float,
        tileSize: Float
    ) {
        val playerRadius = tileSize * 0.32f

        if (player.isInvulnerable && ((player.invulnerabilityTimer * 20).toInt() % 2 == 0)) {
            return
        }

        if (player.isDashing) {
            drawScope.drawCircle(
                color = PlayerDashColor.copy(alpha = 0.5f),
                radius = playerRadius * 1.4f,
                center = Offset(screenX, screenY)
            )
        }

        val bodyColor = if (player.isDashing) PlayerDashColor else PlayerBody
        drawScope.drawCircle(
            color = bodyColor,
            radius = playerRadius,
            center = Offset(screenX, screenY)
        )

        val lookOffset = player.facingDirection * (playerRadius * 0.3f)
        drawScope.drawCircle(
            color = PlayerHead,
            radius = playerRadius * 0.55f,
            center = Offset(screenX + lookOffset.x, screenY + lookOffset.y)
        )

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

    fun drawSkeleton(
        drawScope: DrawScope,
        skeleton: Skeleton,
        screenX: Float,
        screenY: Float,
        tileSize: Float,
        isVisible: Boolean
    ) {
        if (!skeleton.isAlive || !isVisible) return
        val radius = tileSize * 0.30f
        val alpha = skeleton.shadowAlpha

        // Elite Golden Aura / Guardian Fiery Aura
        when (skeleton.variant) {
            EnemyVariant.ELITE -> {
                drawScope.drawCircle(
                    color = EliteAura.copy(alpha = 0.35f * alpha),
                    radius = radius * 1.45f,
                    center = Offset(screenX, screenY)
                )
                drawScope.drawCircle(
                    color = EliteAura.copy(alpha = 0.7f * alpha),
                    radius = radius * 1.25f,
                    center = Offset(screenX, screenY),
                    style = Stroke(width = 2f)
                )
            }
            EnemyVariant.ANCIENT_GUARDIAN -> {
                drawScope.drawCircle(
                    color = GuardianAura.copy(alpha = 0.45f * alpha),
                    radius = radius * 1.55f,
                    center = Offset(screenX, screenY)
                )
                drawScope.drawCircle(
                    color = GuardianAura.copy(alpha = 0.8f * alpha),
                    radius = radius * 1.35f,
                    center = Offset(screenX, screenY),
                    style = Stroke(width = 3f)
                )
            }
            else -> {}
        }

        val baseColor = if (skeleton.isHit) Color(0xFFFF5252) else SkeletonBody
        val skullColor = baseColor.copy(alpha = alpha)

        // Skull body
        drawScope.drawCircle(
            color = skullColor,
            radius = radius,
            center = Offset(screenX, screenY)
        )

        // Jaw
        drawScope.drawRect(
            color = skullColor,
            topLeft = Offset(screenX - radius * 0.6f, screenY + radius * 0.4f),
            size = Size(radius * 1.2f, radius * 0.5f)
        )

        // Glowing eyes
        val eyeColor = if (skeleton.variant == EnemyVariant.SHADOW_ELITE) ShadowEye else SkeletonEye
        val eyeRadius = radius * 0.22f
        val eyeSpacing = radius * 0.35f
        drawScope.drawCircle(
            color = eyeColor.copy(alpha = alpha),
            radius = eyeRadius,
            center = Offset(screenX - eyeSpacing, screenY - radius * 0.15f)
        )
        drawScope.drawCircle(
            color = eyeColor.copy(alpha = alpha),
            radius = eyeRadius,
            center = Offset(screenX + eyeSpacing, screenY - radius * 0.15f)
        )

        // Health bar
        if (skeleton.health < skeleton.maxHealth && alpha > 0.4f) {
            val barWidth = tileSize * 0.75f
            val barHeight = 4.5f
            val barTop = screenY - radius - 12f
            val barLeft = screenX - barWidth / 2f
            val healthPercent = (skeleton.health / skeleton.maxHealth).coerceIn(0f, 1f)

            drawScope.drawRect(
                color = Color(0xAA000000),
                topLeft = Offset(barLeft, barTop),
                size = Size(barWidth, barHeight)
            )
            drawScope.drawRect(
                color = if (skeleton.variant == EnemyVariant.ANCIENT_GUARDIAN) Color(0xFFFF6D00) else Color(0xFFFF1744),
                topLeft = Offset(barLeft, barTop),
                size = Size(barWidth * healthPercent, barHeight)
            )
        }
    }

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

    fun drawTorchLighting(
        drawScope: DrawScope,
        screenWidth: Float,
        screenHeight: Float,
        visionRadiusTiles: Float,
        tileSize: Float,
        themeAssets: ThemeAssets
    ) {
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        val lightRadiusPx = visionRadiusTiles * tileSize

        // Torchlight radial falloff with theme lighting tint
        drawScope.drawRect(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to themeAssets.torchLightTint,
                    0.45f to Color.Transparent,
                    0.75f to Color(0x880A0C10),
                    1.0f to themeAssets.ambientDarkColor
                ),
                center = Offset(centerX, centerY),
                radius = lightRadiusPx
            ),
            topLeft = Offset.Zero,
            size = Size(screenWidth, screenHeight)
        )
    }
}
