package com.dungeonescape.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dungeonescape.models.GameState
import com.dungeonescape.ui.theme.DungeonCard
import com.dungeonescape.ui.theme.DungeonGold
import com.dungeonescape.ui.theme.DungeonGreen
import com.dungeonescape.ui.theme.DungeonRed
import com.dungeonescape.ui.theme.TextPrimary
import com.dungeonescape.ui.theme.TextSecondary
import com.dungeonescape.utils.Constants

@Composable
fun GameHud(
    gameState: GameState,
    modifier: Modifier = Modifier,
    onPauseClick: () -> Unit
) {
    val player = gameState.player
    val levelState = gameState.levelState
    val healthPercent = (player.health / Constants.PLAYER_MAX_HEALTH).coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "key_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // TOP LEFT: Health Bar
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DungeonCard.copy(alpha = 0.85f))
                .border(1.5.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "HP",
                    color = DungeonRed,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${player.health.toInt()} / ${Constants.PLAYER_MAX_HEALTH.toInt()}",
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Health Bar Graphic
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF1E222A))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(healthPercent)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    if (healthPercent > 0.3f) DungeonGreen else DungeonRed,
                                    if (healthPercent > 0.3f) Color(0xFF69F0AE) else Color(0xFFFF5252)
                                )
                            )
                        )
                )
            }
        }

        // TOP CENTER: Key Status
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DungeonCard.copy(alpha = 0.85f))
                .border(
                    width = 1.5.dp,
                    color = if (player.hasKey) DungeonGold else Color(0x33FFFFFF),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (player.hasKey) "KEY: FOUND!" else "KEY: NEEDED",
                    color = if (player.hasKey) DungeonGold else TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = if (player.hasKey) Modifier.scale(pulseScale) else Modifier
                )
            }
        }

        // TOP RIGHT: Level & Coins & Pause
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(DungeonCard.copy(alpha = 0.85f))
                    .border(1.5.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "LVL ${levelState.levelNumber}",
                    color = Color(0xFF00E5FF),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "COINS:",
                        color = DungeonGold,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${player.coinsCollected}",
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Pause Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DungeonCard.copy(alpha = 0.85f))
                    .border(1.5.dp, Color(0x55FFFFFF), CircleShape)
                    .clickable { onPauseClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "II",
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
