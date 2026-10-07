package com.dungeonescape.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
fun TutorialDialog(
    isFirstTime: Boolean = false,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DungeonCard,
            border = BorderStroke(2.dp, if (isFirstTime) DungeonPrimary else DungeonAccent),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .widthIn(max = 460.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isFirstTime) "WELCOME HERO!" else "HOW TO PLAY",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isFirstTime) DungeonPrimary else DungeonGold
                )

                Text(
                    text = if (isFirstTime) "Quick Survival Guide" else "Game Instructions",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TutorialStep("1", "🕹️ MOVE", "Use the left virtual joystick to explore the dark corridors.")
                        TutorialStep("2", "⚔️ ATTACK", "Tap ATTACK to slash skeletons and crack secret walls.")
                        TutorialStep("3", "💨 DASH", "Tap DASH for a quick invulnerable burst through enemies.")
                        TutorialStep("4", "💰 COINS & CHESTS", "Gather gold coins and open chests for points & shop gold.")
                        TutorialStep("5", "🧱 SECRET WALLS", "Spot subtle cracked walls! Strike 3 times to open hidden treasure rooms.")
                        TutorialStep("6", "🍷 MERCHANT ALTAR", "Find Altars in secret rooms to purchase instant healing potions.")
                        TutorialStep("7", "🔑 KEY & PORTAL", "Find the golden key to activate the ancient exit portal and ascend!")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFirstTime) DungeonPrimary else DungeonAccent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(
                        text = if (isFirstTime) "START ESCAPE!" else "GOT IT!",
                        color = DungeonDark,
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
private fun TutorialStep(number: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DungeonDark.copy(alpha = 0.6f))
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = DungeonGold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary,
                lineHeight = 14.sp
            )
        }
    }
}
