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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dungeonescape.BuildConfig
import com.dungeonescape.ui.theme.DungeonAccent
import com.dungeonescape.ui.theme.DungeonCard
import com.dungeonescape.ui.theme.DungeonDark
import com.dungeonescape.ui.theme.DungeonGold
import com.dungeonescape.ui.theme.DungeonPrimary
import com.dungeonescape.ui.theme.TextPrimary
import com.dungeonescape.ui.theme.TextSecondary

enum class AboutTab(val label: String) {
    ABOUT("ABOUT"),
    PRIVACY("PRIVACY POLICY")
}

@Composable
fun AboutCreditsDialog(
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AboutTab.ABOUT) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DungeonCard,
            border = BorderStroke(2.dp, DungeonAccent),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DUNGEON ESCAPE",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonPrimary
                )
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // TAB SELECTORS
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DungeonDark)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AboutTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        TextButton(
                            onClick = { selectedTab = tab },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (isSelected) DungeonAccent else Color.Transparent,
                                contentColor = if (isSelected) DungeonDark else TextSecondary
                            ),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // TAB CONTENT
                Box(
                    modifier = Modifier
                        .heightIn(max = 280.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        AboutTab.ABOUT -> AboutSection()
                        AboutTab.PRIVACY -> PrivacySection()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DungeonAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    Text(
                        text = "CLOSE",
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
private fun AboutSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "DEVELOPED BY",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = DungeonGold,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Makry Games",
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "LEGAL DISCLAIMER",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = DungeonGold,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Dungeon Escape is a fictional fantasy game. Characters, creatures, places and events appearing in the game are fictional and created for entertainment purposes. Any resemblance to real persons, locations or events is coincidental unless explicitly stated otherwise.",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun PrivacySection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "PRIVACY POLICY",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = DungeonGold,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Dungeon Escape is designed to operate completely offline on the user's device.\n\n" +
                    "• The application does not require account creation.\n" +
                    "• Dungeon Escape does not collect, store, sell, or share personally identifiable information.\n" +
                    "• Gameplay progress, high scores, and settings are stored locally on your device.\n\n" +
                    "Contact: macflutter17@gmail.com\n" +
                    "Last updated: October 2, 2026",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary,
            lineHeight = 15.sp
        )
    }
}
