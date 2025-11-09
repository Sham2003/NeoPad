package com.sham.neopad.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sham.neopad.appLog
import com.sham.neopad.model.MainViewModelEvent
import com.sham.neopad.model.MainViewModelEvent.TiltCalibration
import com.sham.neopad.ui.dialogs.TiltCalibrationDialog
import com.sham.neopad.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    mvm: MainViewModel
) {

    // New: Username edit state
    var isEditingUsername by remember { mutableStateOf(false) }
    var tempUsername by remember { mutableStateOf(mvm.username) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Settings",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Username",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (isEditingUsername) {
                            OutlinedTextField(
                                value = tempUsername,
                                onValueChange = { tempUsername = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                            Button(
                                onClick = {
                                    mvm.emitEvent(MainViewModelEvent.ChangeUsername(tempUsername))
                                    isEditingUsername = false
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Save")
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mvm.username,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(onClick = {
                                    tempUsername = mvm.username
                                    isEditingUsername = true
                                }) {
                                    Text("Edit")
                                }
                            }
                        }
                    }
                }

                // Haptic Feedback
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Touch Vibration",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = mvm.settingsData.hapticFeedback,
                            onCheckedChange = { mvm.changeHapticFeedback(it) }
                        )
                    }
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Set Neutral Tilt",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { mvm.showTiltCalibrator() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Open")
                        }
                    }
                }


                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Soft Trigger",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = mvm.settingsData.softTrigger,
                            onCheckedChange = { mvm.changeSoftTrigger(it) }
                        )
                    }
                }

                // 🔹 Special Button Config
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Special Button Config",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Animation Duration
                        SettingSliderItem(
                            label = "Animation Duration",
                            value = mvm.settingsData.animationDuration.toFloat(),
                            onValueChange = { mvm.changeAnimationDuration(it) },
                            valueRange = 50f..200f,
                            unit = "ms"
                        )

                        // Glow Width
                        SettingSliderItem(
                            label = "Glow Width",
                            value = mvm.settingsData.glowWidth,
                            onValueChange = { mvm.changeGlowWidth(it) },
                            valueRange = 5f..20f,
                            unit = "dp"
                        )

                        // Threshold
                        SettingSliderItem(
                            label = "Threshold",
                            value = mvm.settingsData.buttonThreshold,
                            onValueChange = { mvm.changeButtonThreshold(it) },
                            valueRange = 0.2f..1.0f,
                            unit = "x"
                        )
                    }
                }
            }
        }

        if (mvm.showTiltCalibrationDialog) {
            TiltCalibrationDialog(
                onSave = { azimuth,pitch, roll ->
                    appLog("Saving neutral tilt = $azimuth , $pitch , $roll")
                    mvm.changeNeutralTilt(pitch,roll)
                    mvm.closeTiltCalibrator()
                },
                onCancel = { mvm.closeTiltCalibrator() }
            )
        }
    }
}

@Composable
private fun SettingSliderItem(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text("${"%.1f".format(value)} $unit", style = MaterialTheme.typography.bodyMedium)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = 10,
            modifier = Modifier.fillMaxWidth()
        )
    }
}



@Preview
@Composable
fun SettingsScreenStateful() {
    val mvm = MainViewModel.EMPTY
    SettingsScreen(mvm)
}