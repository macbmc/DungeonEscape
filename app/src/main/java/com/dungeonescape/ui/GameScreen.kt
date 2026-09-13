package com.dungeonescape.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dungeonescape.engine.GameEngine
import com.dungeonescape.models.GameState
import com.dungeonescape.models.Vector2D
import com.dungeonescape.ui.components.ActionButton
import com.dungeonescape.ui.components.GameHud
import com.dungeonescape.ui.components.VirtualJoystick
import com.dungeonescape.ui.theme.DungeonAccent
import com.dungeonescape.ui.theme.DungeonCard
import com.dungeonescape.ui.theme.DungeonDark
import com.dungeonescape.ui.theme.DungeonPrimary
import com.dungeonescape.ui.theme.DungeonRed
import com.dungeonescape.ui.theme.TextPrimary
import com.dungeonescape.utils.Constants
import com.dungeonescape.utils.RenderUtils

@Composable
fun GameScreen(
    gameState: GameState,
    gameEngine: GameEngine,
    onMainMenu: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DungeonDark)
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val tileSizePx = Constants.TILE_SIZE

        // 1. GAME CANVAS RENDERING LAYER
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cameraPos = gameState.cameraPosition
            val centerX = screenWidthPx / 2f
            val centerY = screenHeightPx / 2f

            val tiles = gameState.tiles
            val dungeonSize = tiles.size

            if (dungeonSize > 0) {
                val halfVisibleX = (screenWidthPx / (2f * tileSizePx)) + 2f
                val halfVisibleY = (screenHeightPx / (2f * tileSizePx)) + 2f

                val minTileX = (cameraPos.x - halfVisibleX).toInt().coerceAtLeast(0)
                val maxTileX = (cameraPos.x + halfVisibleX).toInt().coerceAtMost(dungeonSize - 1)
                val minTileY = (cameraPos.y - halfVisibleY).toInt().coerceAtLeast(0)
                val maxTileY = (cameraPos.y + halfVisibleY).toInt().coerceAtMost(dungeonSize - 1)

                // LAYER 1: Floor & Wall Tiles
                for (ty in minTileY..maxTileY) {
                    for (tx in minTileX..maxTileX) {
                        val tile = tiles[ty][tx]
                        val screenX = centerX + (tx - cameraPos.x) * tileSizePx
                        val screenY = centerY + (ty - cameraPos.y) * tileSizePx

                        RenderUtils.drawTile(
                            drawScope = this,
                            tile = tile,
                            screenX = screenX,
                            screenY = screenY,
                            tileSize = tileSizePx
                        )
                    }
                }

                // LAYER 2: Objects (Coins, Key, Portal)
                // Portal
                gameState.portal?.let { portal ->
                    val tx = portal.position.x.toInt().coerceIn(0, dungeonSize - 1)
                    val ty = portal.position.y.toInt().coerceIn(0, dungeonSize - 1)
                    val isVis = tiles[ty][tx].isVisible
                    val pScreenX = centerX + (portal.position.x - cameraPos.x) * tileSizePx
                    val pScreenY = centerY + (portal.position.y - cameraPos.y) * tileSizePx
                    RenderUtils.drawPortal(this, portal, pScreenX, pScreenY, tileSizePx, isVis)
                }

                // Key
                gameState.key?.let { key ->
                    val tx = key.position.x.toInt().coerceIn(0, dungeonSize - 1)
                    val ty = key.position.y.toInt().coerceIn(0, dungeonSize - 1)
                    val isVis = tiles[ty][tx].isVisible
                    val kScreenX = centerX + (key.position.x - cameraPos.x) * tileSizePx
                    val kScreenY = centerY + (key.position.y - cameraPos.y) * tileSizePx
                    RenderUtils.drawKey(this, key, kScreenX, kScreenY, tileSizePx, isVis)
                }

                // Coins
                for (coin in gameState.coins) {
                    val tx = coin.position.x.toInt().coerceIn(0, dungeonSize - 1)
                    val ty = coin.position.y.toInt().coerceIn(0, dungeonSize - 1)
                    val isVis = tiles[ty][tx].isVisible
                    val cScreenX = centerX + (coin.position.x - cameraPos.x) * tileSizePx
                    val cScreenY = centerY + (coin.position.y - cameraPos.y) * tileSizePx
                    RenderUtils.drawCoin(this, coin, cScreenX, cScreenY, tileSizePx, isVis)
                }

                // LAYER 3: Enemies (Skeletons)
                for (skeleton in gameState.skeletons) {
                    val tx = skeleton.position.x.toInt().coerceIn(0, dungeonSize - 1)
                    val ty = skeleton.position.y.toInt().coerceIn(0, dungeonSize - 1)
                    val isVis = tiles[ty][tx].isVisible
                    val sScreenX = centerX + (skeleton.position.x - cameraPos.x) * tileSizePx
                    val sScreenY = centerY + (skeleton.position.y - cameraPos.y) * tileSizePx
                    RenderUtils.drawSkeleton(this, skeleton, sScreenX, sScreenY, tileSizePx, isVis)
                }

                // LAYER 4: Player
                val playerScreenX = centerX + (gameState.player.position.x - cameraPos.x) * tileSizePx
                val playerScreenY = centerY + (gameState.player.position.y - cameraPos.y) * tileSizePx
                RenderUtils.drawPlayer(this, gameState.player, playerScreenX, playerScreenY, tileSizePx)

                // LAYER 5: Particles
                RenderUtils.drawParticles(
                    drawScope = this,
                    particles = gameState.particles,
                    cameraPos = cameraPos,
                    screenWidth = screenWidthPx,
                    screenHeight = screenHeightPx,
                    tileSize = tileSizePx
                )

                // LAYER 6: Torchlight & Dynamic Fog of War
                RenderUtils.drawTorchLighting(
                    drawScope = this,
                    screenWidth = screenWidthPx,
                    screenHeight = screenHeightPx,
                    visionRadiusTiles = Constants.PLAYER_VISION_RADIUS,
                    tileSize = tileSizePx
                )
            }
        }

        // 2. HUD OVERLAY
        GameHud(
            gameState = gameState,
            modifier = Modifier.align(Alignment.TopCenter),
            onPauseClick = { gameEngine.pauseGame() }
        )

        // 3. CONTROLS LAYER (Virtual Joystick + Attack & Dash Buttons)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
        ) {
            // Virtual Joystick on Bottom-Left
            VirtualJoystick(
                modifier = Modifier.align(Alignment.BottomStart),
                size = 140.dp,
                onMove = { dir -> gameEngine.handleJoystickInput(dir) }
            )

            // Right Action Controls (Dash above Attack)
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dash Button
                val dashCd = gameState.player.dashCooldownTimer
                val dashProgress = (dashCd / Constants.DASH_COOLDOWN).coerceIn(0f, 1f)
                val dashSecText = if (dashCd > 0f) String.format("%.1fs", dashCd) else null

                ActionButton(
                    label = "DASH",
                    subLabel = dashSecText,
                    baseColor = DungeonAccent,
                    cooldownProgress = dashProgress,
                    size = 64.dp,
                    onPress = { gameEngine.handleDash() }
                )

                // Attack Button
                val attackCd = gameState.player.attackCooldownTimer
                val attackProgress = (attackCd / Constants.PLAYER_ATTACK_COOLDOWN).coerceIn(0f, 1f)

                ActionButton(
                    label = "ATTACK",
                    baseColor = DungeonRed,
                    cooldownProgress = attackProgress,
                    size = 76.dp,
                    onPress = { gameEngine.handleAttack() }
                )
            }
        }

        // 4. PAUSE MODAL
        if (gameState.isPaused) {
            Dialog(onDismissRequest = { gameEngine.resumeGame() }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DungeonCard,
                    border = androidx.compose.foundation.BorderStroke(2.dp, DungeonPrimary),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = DungeonPrimary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { gameEngine.resumeGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = DungeonPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text(
                                text = "RESUME",
                                color = DungeonDark,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                gameEngine.resumeGame()
                                onMainMenu()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text(
                                text = "MAIN MENU",
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
