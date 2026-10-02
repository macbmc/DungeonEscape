package com.dungeonescape.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dungeonescape.engine.GameStateManager
import com.dungeonescape.ui.theme.DungeonAccent
import com.dungeonescape.ui.theme.DungeonCard
import com.dungeonescape.ui.theme.DungeonDark
import com.dungeonescape.ui.theme.DungeonGold
import com.dungeonescape.ui.theme.DungeonPrimary
import com.dungeonescape.ui.theme.TextPrimary
import com.dungeonescape.ui.theme.TextSecondary

@Composable
fun SettingsDialog(
    stateManager: GameStateManager,
    onSettingsChanged: () -> Unit,
    onDismiss: () -> Unit
) {
    var musicVolume by remember { mutableFloatStateOf(stateManager.musicVolume) }
    var sfxVolume by remember { mutableFloatStateOf(stateManager.sfxVolume) }
    var musicEnabled by remember { mutableStateOf(stateManager.musicEnabled) }
    var sfxEnabled by remember { mutableStateOf(stateManager.sfxEnabled) }
    var vibrationEnabled by remember { mutableStateOf(stateManager.vibrationEnabled) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DungeonCard,
            border = BorderStroke(2.dp, DungeonPrimary),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SETTINGS",
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DungeonPrimary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // MUSIC SECTION
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MUSIC",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Switch(
                            checked = musicEnabled,
                            onCheckedChange = {
                                musicEnabled = it
                                stateManager.musicEnabled = it
                                onSettingsChanged()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DungeonPrimary,
                                checkedTrackColor = DungeonPrimary.copy(alpha = 0.5f),
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = DungeonDark
                            ),
                            modifier = Modifier.semantics {
                                contentDescription = "Toggle background music"
                            }
                        )
                    }

                    if (musicEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${(musicVolume * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Slider(
                                value = musicVolume,
                                onValueChange = {
                                    musicVolume = it
                                    stateManager.musicVolume = it
                                    onSettingsChanged()
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = DungeonPrimary,
                                    activeTrackColor = DungeonPrimary,
                                    inactiveTrackColor = DungeonDark
                                ),
                                modifier = Modifier.semantics {
                                    contentDescription = "Music volume slider"
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SFX SECTION
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOUND EFFECTS",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Switch(
                            checked = sfxEnabled,
                            onCheckedChange = {
                                sfxEnabled = it
                                stateManager.sfxEnabled = it
                                onSettingsChanged()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DungeonAccent,
                                checkedTrackColor = DungeonAccent.copy(alpha = 0.5f),
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = DungeonDark
                            ),
                            modifier = Modifier.semantics {
                                contentDescription = "Toggle sound effects"
                            }
                        )
                    }

                    if (sfxEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${(sfxVolume * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Slider(
                                value = sfxVolume,
                                onValueChange = {
                                    sfxVolume = it
                                    stateManager.sfxVolume = it
                                    onSettingsChanged()
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = DungeonAccent,
                                    activeTrackColor = DungeonAccent,
                                    inactiveTrackColor = DungeonDark
                                ),
                                modifier = Modifier.semantics {
                                    contentDescription = "Sound effects volume slider"
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // VIBRATION SECTION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VIBRATION",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            vibrationEnabled = it
                            stateManager.vibrationEnabled = it
                            onSettingsChanged()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DungeonGold,
                            checkedTrackColor = DungeonGold.copy(alpha = 0.5f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DungeonDark
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = "Toggle haptic vibration"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DungeonPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(
                        text = "CLOSE",
                        color = DungeonDark,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
