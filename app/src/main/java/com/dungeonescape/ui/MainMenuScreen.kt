package com.dungeonescape.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dungeonescape.R
import com.dungeonescape.engine.GameStateManager
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
    onToggleAudio: () -> Unit,
    onExit: () -> Unit
) {
    var showInstructions by remember { mutableStateOf(false) }

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
        modifier = Modifier.fillMaxSize(),
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
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "DUNGEON",
                fontSize = 44.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = DungeonPrimary,
                modifier = Modifier.scale(titleScale)
            )
            Text(
                text = "ESCAPE",
                fontSize = 42.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = DungeonAccent,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Procedural Action Rogue Crawler",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(26.dp))

            // High score card
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DungeonCard.copy(alpha = 0.90f))
                    .border(1.5.dp, Color(0x44FFFFFF), RoundedCornerShape(12.dp))
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

            Spacer(modifier = Modifier.height(30.dp))

            // RESUME GAME BUTTON (If saved session exists)
            if (savedSession != null) {
                Button(
                    onClick = onResumeGame,
                    colors = ButtonDefaults.buttonColors(containerColor = DungeonGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(230.dp)
                        .height(50.dp)
                ) {
                    Text(
                        text = "RESUME (LVL ${savedSession.level})",
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = DungeonDark
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // START NEW GAME BUTTON (Direct progressive start)
            Button(
                onClick = onStartNewGame,
                colors = ButtonDefaults.buttonColors(containerColor = DungeonPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(230.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "NEW GAME",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showInstructions = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(230.dp)
                    .height(46.dp)
            ) {
                Text(
                    text = "INSTRUCTIONS",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonAccent
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onExit,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(230.dp)
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
                    border = BorderStroke(2.dp, DungeonAccent),
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
                        Spacer(modifier = Modifier.height(12.dp))

                        val instructions = listOf(
                            "🕹️ Left Joystick: Move your hero smoothly.",
                            "⚔️ Attack Button: Slash skeletons & crack Secret Walls!",
                            "💨 Dash: Burst through enemies (cannot cross walls).",
                            "🧱 Secret Walls: Look for subtle wall cracks! Break with 3 hits to discover Hidden Treasure Rooms.",
                            "💰 Treasure Rooms: Contain coin chests, Merchant Altars, and Guardian Elites.",
                            "🍷 Merchant Altar: Spend coins for instant health potions.",
                            "🔑 Key & Portal: Find key to open the exit portal to ascend.",
                            "⚡ Progressive Themes: Themes naturally evolve with your level depth!"
                        )

                        instructions.forEach { item ->
                            Text(
                                text = item,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary,
                                modifier = Modifier.padding(vertical = 3.dp),
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showInstructions = false },
                            colors = ButtonDefaults.buttonColors(containerColor = DungeonAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "READY!",
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
