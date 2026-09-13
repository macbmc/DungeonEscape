package com.dungeonescape.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dungeonescape.engine.GameStateManager
import com.dungeonescape.ui.theme.DungeonAccent
import com.dungeonescape.ui.theme.DungeonCard
import com.dungeonescape.ui.theme.DungeonDark
import com.dungeonescape.ui.theme.DungeonGold
import com.dungeonescape.ui.theme.DungeonPrimary
import com.dungeonescape.ui.theme.DungeonPrimaryDark
import com.dungeonescape.ui.theme.DungeonRed
import com.dungeonescape.ui.theme.TextPrimary
import com.dungeonescape.ui.theme.TextSecondary

@Composable
fun MainMenuScreen(
    stateManager: GameStateManager,
    onStartGame: () -> Unit,
    onToggleAudio: () -> Unit,
    onExit: () -> Unit
) {
    var showInstructions by remember { mutableStateOf(false) }

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
            .background(DungeonDark),
        contentAlignment = Alignment.Center
    ) {
        // Animated background dungeon torches / grid lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 48f
            for (x in 0..(size.width / gridStep).toInt()) {
                drawLine(
                    color = Color(0x11FFFFFF),
                    start = Offset(x * gridStep, 0f),
                    end = Offset(x * gridStep, size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height / gridStep).toInt()) {
                drawLine(
                    color = Color(0x11FFFFFF),
                    start = Offset(0f, y * gridStep),
                    end = Offset(size.width, y * gridStep),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Title Header
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A 60FPS Top-Down Rogue Crawler",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // High score card
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DungeonCard)
                    .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HIGH SCORE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DungeonGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stateManager.highScore}",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MAX LEVEL",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DungeonAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stateManager.highestLevel}",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Buttons
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(containerColor = DungeonPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(52.dp)
            ) {
                Text(
                    text = "START GAME",
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { showInstructions = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = "INSTRUCTIONS",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonAccent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onExit,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(44.dp)
            ) {
                Text(
                    text = "EXIT",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonRed
                )
            }
        }

        // Instructions Modal Dialog
        if (showInstructions) {
            Dialog(onDismissRequest = { showInstructions = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DungeonCard,
                    border = androidx.compose.foundation.BorderStroke(2.dp, DungeonAccent),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HOW TO PLAY",
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = DungeonGold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        val instructions = listOf(
                            "🕹️ Left Joystick: Move your hero through the dark dungeon.",
                            "⚔️ Attack Button: Slash nearby skeletons in your path.",
                            "💨 Dash Button: Burst through enemies (3s cooldown). Cannot pass through walls.",
                            "🔑 Key: Explore to find the golden key to activate the Exit Portal.",
                            "🌀 Portal: Enter the activated portal to ascend to the next dungeon level.",
                            "💰 Coins: Collect scattered coins to boost your high score.",
                            "🔦 Torchlight: Fog of war covers the dungeon. Beware of lurking enemies!"
                        )

                        instructions.forEach { item ->
                            Text(
                                text = item,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary,
                                modifier = Modifier.padding(vertical = 4.dp),
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { showInstructions = false },
                            colors = ButtonDefaults.buttonColors(containerColor = DungeonAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "GOT IT!",
                                color = DungeonDark,
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
