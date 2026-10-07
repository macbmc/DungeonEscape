package com.dungeonescape.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dungeonescape.engine.GameEngine
import com.dungeonescape.models.GameState
import com.dungeonescape.ui.components.ActionButton
import com.dungeonescape.ui.components.GameHud
import com.dungeonescape.ui.components.MerchantDialog
import com.dungeonescape.ui.components.SettingsDialog
import com.dungeonescape.ui.components.TutorialDialog
import com.dungeonescape.ui.components.VirtualJoystick
import com.dungeonescape.ui.theme.DungeonAccent
import com.dungeonescape.ui.theme.DungeonCard
import com.dungeonescape.ui.theme.DungeonDark
import com.dungeonescape.ui.theme.DungeonGold
import com.dungeonescape.ui.theme.DungeonGreen
import com.dungeonescape.ui.theme.DungeonPrimary
import com.dungeonescape.ui.theme.DungeonRed
import com.dungeonescape.ui.theme.TextPrimary
import com.dungeonescape.utils.Constants
import com.dungeonescape.utils.GameBalance
import com.dungeonescape.utils.RenderUtils

@Composable
fun GameScreen(
    gameState: GameState,
    gameEngine: GameEngine,
    onMainMenu: () -> Unit
) {
    val themeAssets = gameEngine.themeManager.getAssets(gameState.theme)
    var showInGameSettings by remember { mutableStateOf(false) }
    var showFirstTimeTutorial by remember {
        mutableStateOf(!gameEngine.stateManager.tutorialCompleted)
    }

    LaunchedEffect(showFirstTimeTutorial) {
        if (showFirstTimeTutorial) {
            gameEngine.pauseGame()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(themeAssets.ambientDarkColor)
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

                // LAYER 1: Floor & Wall Tiles (Theme-specific)
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
                            tileSize = tileSizePx,
                            themeAssets = themeAssets
                        )
                    }
                }

                // LAYER 2: Objects (Traps, Altar, Chests, Coins, Key, Portal)
                // Spike Traps
                for (trap in gameState.traps) {
                    val tx = trap.position.x.toInt().coerceIn(0, dungeonSize - 1)
                    val ty = trap.position.y.toInt().coerceIn(0, dungeonSize - 1)
                    val isVis = tiles[ty][tx].isVisible
                    val tScreenX = centerX + (trap.position.x - cameraPos.x) * tileSizePx
                    val tScreenY = centerY + (trap.position.y - cameraPos.y) * tileSizePx
                    RenderUtils.drawTrapSpike(this, trap, tScreenX, tScreenY, tileSizePx, isVis)
                }

                // Merchant Altar
                gameState.merchantAltar?.let { altar ->
                    val tx = altar.position.x.toInt().coerceIn(0, dungeonSize - 1)
                    val ty = altar.position.y.toInt().coerceIn(0, dungeonSize - 1)
                    val isVis = tiles[ty][tx].isVisible
                    val aScreenX = centerX + (altar.position.x - cameraPos.x) * tileSizePx
                    val aScreenY = centerY + (altar.position.y - cameraPos.y) * tileSizePx
                    RenderUtils.drawMerchantAltar(this, altar, aScreenX, aScreenY, tileSizePx, isVis)
                }

                // Treasure Chests
                for (chest in gameState.chests) {
                    val tx = chest.position.x.toInt().coerceIn(0, dungeonSize - 1)
                    val ty = chest.position.y.toInt().coerceIn(0, dungeonSize - 1)
                    val isVis = tiles[ty][tx].isVisible
                    val cScreenX = centerX + (chest.position.x - cameraPos.x) * tileSizePx
                    val cScreenY = centerY + (chest.position.y - cameraPos.y) * tileSizePx
                    RenderUtils.drawTreasureChest(this, chest, cScreenX, cScreenY, tileSizePx, isVis)
                }

                // Exit Portal
                gameState.portal?.let { portal ->
                    val tx = portal.position.x.toInt().coerceIn(0, dungeonSize - 1)
                    val ty = portal.position.y.toInt().coerceIn(0, dungeonSize - 1)
                    val isVis = tiles[ty][tx].isVisible
                    val pScreenX = centerX + (portal.position.x - cameraPos.x) * tileSizePx
                    val pScreenY = centerY + (portal.position.y - cameraPos.y) * tileSizePx
                    RenderUtils.drawPortal(this, portal, pScreenX, pScreenY, tileSizePx, isVis)
                }

                // Golden Key
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

                // Health Potions
                for (potion in gameState.potions) {
                    val tx = potion.position.x.toInt().coerceIn(0, dungeonSize - 1)
                    val ty = potion.position.y.toInt().coerceIn(0, dungeonSize - 1)
                    val isVis = tiles[ty][tx].isVisible
                    val pScreenX = centerX + (potion.position.x - cameraPos.x) * tileSizePx
                    val pScreenY = centerY + (potion.position.y - cameraPos.y) * tileSizePx
                    RenderUtils.drawHealthPotion(this, potion, pScreenX, pScreenY, tileSizePx, isVis)
                }

                // LAYER 3: Enemies
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

                // LAYER 6: Dynamic Torchlight Vignette (with Theme lighting tint)
                RenderUtils.drawTorchLighting(
                    drawScope = this,
                    screenWidth = screenWidthPx,
                    screenHeight = screenHeightPx,
                    visionRadiusTiles = Constants.PLAYER_VISION_RADIUS,
                    tileSize = tileSizePx,
                    themeAssets = themeAssets
                )
            }
        }

        // 2. HUD OVERLAY (Safe insets for status bar)
        GameHud(
            gameState = gameState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
            onPauseClick = { gameEngine.pauseGame() }
        )

        // 3. TOUCH CONTROLS (Safe insets for navigation bars)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
        ) {
            // Virtual Joystick
            VirtualJoystick(
                modifier = Modifier.align(Alignment.BottomStart),
                size = 140.dp,
                onMove = { dir -> gameEngine.handleJoystickInput(dir) }
            )

            // Right Action Controls
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Interactive Merchant Altar Button (Appears when near altar)
                AnimatedVisibility(
                    visible = gameState.isMerchantNear,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ActionButton(
                        label = "ALTAR",
                        subLabel = "SHOP",
                        baseColor = DungeonGreen,
                        size = 60.dp,
                        onPress = { gameEngine.openMerchantDialog() }
                    )
                }

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

        // 4. MERCHANT SHOP MODAL
        if (gameState.isMerchantDialogOpen) {
            MerchantDialog(
                gameState = gameState,
                onBuySmallPotion = {
                    gameEngine.buyPotion(
                        GameBalance.SMALL_POTION_COST,
                        GameBalance.SMALL_SECRET_POTION_HEAL
                    )
                },
                onBuyLargePotion = {
                    gameEngine.buyPotion(
                        GameBalance.LARGE_POTION_COST,
                        GameBalance.LARGE_SECRET_POTION_HEAL
                    )
                },
                onClose = { gameEngine.closeMerchantDialog() }
            )
        }

        // 5. FIRST TIME TUTORIAL MODAL
        if (showFirstTimeTutorial) {
            TutorialDialog(
                isFirstTime = true,
                onDismiss = {
                    showFirstTimeTutorial = false
                    gameEngine.stateManager.tutorialCompleted = true
                    gameEngine.resumeGame()
                }
            )
        }

        // 6. IN-GAME SETTINGS MODAL
        if (showInGameSettings) {
            SettingsDialog(
                stateManager = gameEngine.stateManager,
                onSettingsChanged = { gameEngine.applySettings() },
                onDismiss = { showInGameSettings = false }
            )
        }

        // 7. PAUSE MODAL
        if (gameState.isPaused && !showInGameSettings && !showFirstTimeTutorial && !gameState.isMerchantDialogOpen) {
            Dialog(onDismissRequest = { gameEngine.resumeGame() }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DungeonCard,
                    border = BorderStroke(2.dp, DungeonPrimary),
                    modifier = Modifier
                        .padding(16.dp)
                        .widthIn(max = 420.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PAUSED",
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = DungeonPrimary,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // RESUME
                        Button(
                            onClick = { gameEngine.resumeGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = DungeonPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .semantics { contentDescription = "Resume game" }
                        ) {
                            Text(
                                text = "RESUME",
                                color = DungeonDark,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // RESTART LEVEL
                        OutlinedButton(
                            onClick = { gameEngine.restartCurrentLevel() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .semantics { contentDescription = "Restart current level" }
                        ) {
                            Text(
                                text = "RESTART LEVEL",
                                color = DungeonAccent,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // SETTINGS
                        OutlinedButton(
                            onClick = { showInGameSettings = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .semantics { contentDescription = "Open settings from pause menu" }
                        ) {
                            Text(
                                text = "SETTINGS",
                                color = DungeonGold,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // MAIN MENU
                        OutlinedButton(
                            onClick = {
                                gameEngine.resumeGame()
                                onMainMenu()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .semantics { contentDescription = "Save and exit to main menu" }
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
