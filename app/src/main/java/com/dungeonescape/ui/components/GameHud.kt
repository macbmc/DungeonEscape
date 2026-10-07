package com.dungeonescape.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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

    val criticalPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "critical_pulse"
    )

    // Dynamic 3-state health bar colors
    val healthGradient = when {
        healthPercent > 0.60f -> listOf(DungeonGreen, Color(0xFF69F0AE))
        healthPercent >= 0.30f -> listOf(Color(0xFFFFB300), Color(0xFFFFE082)) // Amber warning
        else -> listOf(Color(0xFFFF1744).copy(alpha = criticalPulseAlpha), Color(0xFFFF5252)) // Critical red pulse
    }

    val healthLabelColor = when {
        healthPercent > 0.60f -> DungeonGreen
        healthPercent >= 0.30f -> Color(0xFFFFB300)
        else -> DungeonRed
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isNarrowScreen = maxWidth < 360.dp
        val hpBarWidth = if (isNarrowScreen) 80.dp else 110.dp
        val horizontalPad = if (isNarrowScreen) 8.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPad, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // TOP LEFT: Health Bar
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DungeonCard.copy(alpha = 0.88f))
                        .border(
                            1.5.dp,
                            if (healthPercent < 0.30f) Color(0xFFFF1744).copy(alpha = criticalPulseAlpha) else Color(0x44FFFFFF),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HP",
                            color = healthLabelColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${player.health.toInt()} / ${Constants.PLAYER_MAX_HEALTH.toInt()}",
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(hpBarWidth)
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
                                .background(Brush.horizontalGradient(healthGradient))
                        )
                    }
                }

                // TOP CENTER: Key Status & Theme Name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DungeonCard.copy(alpha = 0.88f))
                        .border(
                            width = 1.5.dp,
                            color = if (player.hasKey) DungeonGold else Color(0x33FFFFFF),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = gameState.theme.displayName.uppercase(),
                        color = Color(0xFF80D8FF),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (player.hasKey) "KEY: FOUND!" else "KEY: NEEDED",
                        color = if (player.hasKey) DungeonGold else TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = if (player.hasKey) Modifier.scale(pulseScale) else Modifier
                    )
                }

                // TOP RIGHT: Level, Coins & Pause
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DungeonCard.copy(alpha = 0.88f))
                            .border(1.5.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "LVL ${levelState.levelNumber}",
                            color = Color(0xFF00E5FF),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "COINS:",
                                color = DungeonGold,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${player.coinsCollected}",
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // PAUSE BUTTON
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DungeonCard.copy(alpha = 0.92f))
                            .border(1.5.dp, DungeonGold.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                            .clickable { onPauseClick() }
                            .semantics { contentDescription = "Pause game" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "❚❚",
                            color = DungeonGold,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

    // FLOATING HEAL / CLEAR FEEDBACK BANNER
        AnimatedVisibility(
            visible = gameState.healFeedbackText != null,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 }
        ) {
            gameState.healFeedbackText?.let { feedback ->
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DungeonCard.copy(alpha = 0.95f))
                        .border(1.5.dp, DungeonGreen, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💚 $feedback",
                        color = DungeonGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
}

