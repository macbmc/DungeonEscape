package com.dungeonescape.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dungeonescape.engine.GameStateManager
import com.dungeonescape.models.GameState
import com.dungeonescape.ui.theme.DungeonAccent
import com.dungeonescape.ui.theme.DungeonCard
import com.dungeonescape.ui.theme.DungeonDark
import com.dungeonescape.ui.theme.DungeonGold
import com.dungeonescape.ui.theme.DungeonPrimary
import com.dungeonescape.ui.theme.DungeonRed
import com.dungeonescape.ui.theme.TextPrimary
import com.dungeonescape.ui.theme.TextSecondary

@Composable
fun GameOverScreen(
    gameState: GameState,
    stateManager: GameStateManager,
    onRetry: () -> Unit,
    onMainMenu: () -> Unit
) {
    val isNewHighScore = gameState.score >= stateManager.highScore && gameState.score > 0

    val infiniteTransition = rememberInfiniteTransition(label = "highscore_pulse")
    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(DungeonDark.copy(alpha = 0.96f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "GAME OVER",
                fontSize = 38.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = DungeonRed,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "You succumbed to the dungeon...",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Score details card
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(DungeonCard)
                    .border(1.5.dp, if (isNewHighScore) DungeonGold else Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isNewHighScore) {
                    Text(
                        text = "★ NEW HIGH SCORE! ★",
                        color = DungeonGold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.scale(badgeScale)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "FINAL SCORE:", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Text(text = "${gameState.score}", color = DungeonGold, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "BEST SCORE:", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Text(text = "${stateManager.highScore}", color = TextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "LEVEL REACHED:", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Text(text = "${gameState.levelState.levelNumber}", color = DungeonAccent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "COINS COLLECTED:", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Text(text = "${gameState.player.coinsCollected}", color = DungeonGold, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = DungeonPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp)
                    .semantics { contentDescription = "Retry and start a new game" }
            ) {
                Text(
                    text = "RETRY",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onMainMenu,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(44.dp)
                    .semantics { contentDescription = "Return to main menu" }
            ) {
                Text(
                    text = "MAIN MENU",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}
