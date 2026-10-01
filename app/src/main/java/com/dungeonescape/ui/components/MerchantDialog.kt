package com.dungeonescape.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.dungeonescape.models.GameState
import com.dungeonescape.ui.theme.DungeonCard
import com.dungeonescape.ui.theme.DungeonDark
import com.dungeonescape.ui.theme.DungeonGold
import com.dungeonescape.ui.theme.DungeonGreen
import com.dungeonescape.ui.theme.DungeonRed
import com.dungeonescape.ui.theme.TextPrimary
import com.dungeonescape.ui.theme.TextSecondary
import com.dungeonescape.utils.Constants
import com.dungeonescape.utils.GameBalance
import kotlin.math.min

@Composable
fun MerchantDialog(
    gameState: GameState,
    onBuySmallPotion: () -> Unit,
    onBuyLargePotion: () -> Unit,
    onClose: () -> Unit
) {
    val player = gameState.player
    val isFullHealth = player.health >= player.maxHealth

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DungeonCard,
            border = BorderStroke(2.dp, DungeonGreen),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HEALING ALTAR",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonGreen
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Spend coins for restorative potions",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Coin & Health summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DungeonDark.copy(alpha = 0.8f))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "COINS: ", color = DungeonGold, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${player.coinsCollected}", color = TextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "HP: ", color = DungeonRed, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${player.health.toInt()} / ${Constants.PLAYER_MAX_HEALTH.toInt()}", color = TextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Potion 1: Small (30 Coins -> +30 HP)
                val smallNominal = GameBalance.SMALL_SECRET_POTION_HEAL.toInt()
                val smallActual = min(smallNominal, (player.maxHealth - player.health).toInt())
                val smallDesc = if (isFullHealth) {
                    "+$smallNominal HP (At Full Health)"
                } else if (smallActual < smallNominal) {
                    "+$smallNominal HP (Actual: +$smallActual HP)"
                } else {
                    "+$smallNominal Health"
                }

                PotionOfferRow(
                    name = "Small Potion",
                    description = smallDesc,
                    cost = GameBalance.SMALL_POTION_COST,
                    playerCoins = player.coinsCollected,
                    isFullHealth = isFullHealth,
                    onBuy = onBuySmallPotion
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Potion 2: Large (65 Coins -> +70 HP)
                val largeNominal = GameBalance.LARGE_SECRET_POTION_HEAL.toInt()
                val largeActual = min(largeNominal, (player.maxHealth - player.health).toInt())
                val largeDesc = if (isFullHealth) {
                    "+$largeNominal HP (At Full Health)"
                } else if (largeActual < largeNominal) {
                    "+$largeNominal HP (Actual: +$largeActual HP)"
                } else {
                    "+$largeNominal Health"
                }

                PotionOfferRow(
                    name = "Large Potion",
                    description = largeDesc,
                    cost = GameBalance.LARGE_POTION_COST,
                    playerCoins = player.coinsCollected,
                    isFullHealth = isFullHealth,
                    onBuy = onBuyLargePotion
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(
                        text = "CLOSE",
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PotionOfferRow(
    name: String,
    description: String,
    cost: Int,
    playerCoins: Int,
    isFullHealth: Boolean,
    onBuy: () -> Unit
) {
    val canAfford = playerCoins >= cost
    val isEnabled = canAfford && !isFullHealth

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1B202A))
            .border(
                1.dp,
                if (isEnabled) DungeonGreen.copy(alpha = 0.5f) else Color(0x22FFFFFF),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = if (isEnabled) TextPrimary else TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = description,
                color = if (isFullHealth) TextSecondary else DungeonGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onBuy,
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = DungeonGold,
                disabledContainerColor = Color(0xFF37474F)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isFullHealth) "FULL" else if (!canAfford) "$cost 💰 (Need Coins)" else "$cost 💰",
                color = if (isEnabled) DungeonDark else TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
