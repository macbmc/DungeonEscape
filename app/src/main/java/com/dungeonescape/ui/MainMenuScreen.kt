package com.dungeonescape.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dungeonescape.R
import com.dungeonescape.engine.GameStateManager
import com.dungeonescape.ui.components.AboutCreditsDialog
import com.dungeonescape.ui.components.SettingsDialog
import com.dungeonescape.ui.components.TutorialDialog
import com.dungeonescape.ui.theme.DungeonAccent
import com.dungeonescape.ui.theme.DungeonCard
import com.dungeonescape.ui.theme.DungeonDark
import com.dungeonescape.ui.theme.DungeonGold
import com.dungeonescape.ui.theme.DungeonGreen
import com.dungeonescape.ui.theme.DungeonPrimary
import com.dungeonescape.ui.theme.DungeonRed
import com.dungeonescape.ui.theme.TextPrimary
import com.dungeonescape.ui.theme.TextSecondary

@Composable
fun MainMenuScreen(
    stateManager: GameStateManager,
    onResumeGame: () -> Unit,
    onStartNewGame: () -> Unit,
    onSettingsChanged: () -> Unit,
    onExit: () -> Unit
) {
    var showHowToPlay by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showAboutCredits by remember { mutableStateOf(false) }

    val savedSession = remember { stateManager.loadSavedGameSession() }

    val infiniteTransition = rememberInfiniteTransition(label = "title_pulse")
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "title_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        // ATMOSPHERIC BACKGROUND IMAGE
        Image(
            painter = painterResource(id = R.drawable.bg_main_menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // DARK GRADIENT VIGNETTE OVERLAY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xE60D0F14), // Dark top for title
                            Color(0x990D0F14), // Translucent middle showing lanterns & archway
                            Color(0xF20D0F14)  // Dark bottom for buttons
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "DUNGEON",
                fontSize = 42.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = DungeonPrimary,
                modifier = Modifier.scale(titleScale)
            )
            Text(
                text = "ESCAPE",
                fontSize = 40.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = DungeonAccent,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Procedural Action Rogue Crawler",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // STATS BANNER
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DungeonCard.copy(alpha = 0.90f))
                    .border(1.5.dp, Color(0x44FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HIGH SCORE",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DungeonGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stateManager.highScore}",
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MAX LEVEL",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DungeonAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stateManager.highestLevel}",
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MAX COINS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DungeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stateManager.highestCoins}",
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // RESUME GAME BUTTON (If saved session exists)
            if (savedSession != null) {
                Button(
                    onClick = onResumeGame,
                    colors = ButtonDefaults.buttonColors(containerColor = DungeonGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(230.dp)
                        .height(46.dp)
                        .semantics { contentDescription = "Resume saved game at level ${savedSession.level}" }
                ) {
                    Text(
                        text = "RESUME (LVL ${savedSession.level})",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = DungeonDark
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // START NEW GAME BUTTON
            Button(
                onClick = onStartNewGame,
                colors = ButtonDefaults.buttonColors(containerColor = DungeonPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(230.dp)
                    .height(46.dp)
                    .semantics { contentDescription = "Start a new game" }
            ) {
                Text(
                    text = "NEW GAME",
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // HOW TO PLAY BUTTON
            OutlinedButton(
                onClick = { showHowToPlay = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(230.dp)
                    .height(42.dp)
                    .semantics { contentDescription = "View How to Play tutorial" }
            ) {
                Text(
                    text = "HOW TO PLAY",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonAccent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SETTINGS BUTTON
            OutlinedButton(
                onClick = { showSettings = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(230.dp)
                    .height(42.dp)
                    .semantics { contentDescription = "Open game settings" }
            ) {
                Text(
                    text = "SETTINGS",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ABOUT & PRIVACY BUTTON
            OutlinedButton(
                onClick = { showAboutCredits = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(230.dp)
                    .height(42.dp)
                    .semantics { contentDescription = "About game and privacy policy" }
            ) {
                Text(
                    text = "ABOUT & PRIVACY",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // EXIT BUTTON
            OutlinedButton(
                onClick = onExit,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(230.dp)
                    .height(40.dp)
                    .semantics { contentDescription = "Exit game application" }
            ) {
                Text(
                    text = "EXIT",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonRed
                )
            }
        }

        // HOW TO PLAY DIALOG
        if (showHowToPlay) {
            TutorialDialog(
                isFirstTime = false,
                onDismiss = { showHowToPlay = false }
            )
        }

        // SETTINGS DIALOG
        if (showSettings) {
            SettingsDialog(
                stateManager = stateManager,
                onSettingsChanged = onSettingsChanged,
                onDismiss = { showSettings = false }
            )
        }

        // ABOUT & CREDITS DIALOG
        if (showAboutCredits) {
            AboutCreditsDialog(
                onDismiss = { showAboutCredits = false }
            )
        }
    }
}
