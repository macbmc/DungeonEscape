package com.dungeonescape.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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

    Box(
        modifier = Modifier
            .fillMaxSize()
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

            Spacer(modifier = Modifier.height(28.dp))

            // Score details card
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(DungeonCard)
                    .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isNewHighScore) {
                    Text(
                        text = "★ NEW HIGH SCORE! ★",
                        color = DungeonGold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "FINAL SCORE:", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Text(text = "${gameState.score}", color = DungeonGold, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "LEVEL REACHED:", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Text(text = "${gameState.levelState.levelNumber}", color = DungeonAccent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "COINS COLLECTED:", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Text(text = "${gameState.player.coinsCollected}", color = TextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = DungeonPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "RETRY",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onMainMenu,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(46.dp)
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
