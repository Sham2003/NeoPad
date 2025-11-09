package com.sham.neopad.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sham.neopad.misc.OrientationSensorManager

/**
 * A composable that reads live phone orientation (pitch/roll)
 * and displays a 3D preview with Save/Cancel buttons.
 */
@Composable
fun OrientationVisualizer(azimuth: Float, pitch: Float, roll: Float) {
    val boxSize = 200.dp
    val phoneSize = boxSize * 0.75f
    val density = LocalDensity.current.density

    Box(
        modifier = Modifier
            .size(boxSize)
            .clip(RoundedCornerShape(32.dp))
            .border(4.dp, Color(0xFF333333), RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Phone representation (Emoji)
        Text(
            text = "📱",
            modifier = Modifier
                .size(phoneSize)
                .graphicsLayer {
                    rotationZ = 0f // Azimuth (Yaw)
                    rotationX = pitch   // Pitch
                    rotationY = roll    // Roll

                    // Set camera distance for a proper 3D perspective effect
                    cameraDistance = 12 * density
                }
                .align(Alignment.Center),
            color = Color(0xFF424242), // Dark gray color for the emoji text
            fontSize = (phoneSize.value * 0.5f).sp
        )
    }
}


@Composable
fun TiltCalibrationDialog(
    onSave: (azimuth: Float, pitch: Float, roll: Float) -> Unit, // Updated signature to include all 3 angles
    onCancel: () -> Unit
) {
    val (azimuth, pitch, roll) = rememberOrientationSensorValues()

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onSave(azimuth, pitch, roll) }, // Pass all three values
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        },
        title = {
            Text(
                "Set Default Orientation",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Hold your phone in the position you want to treat as 'level' (e.g., portrait or landscape), then tap Save.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                // Optional: Show the current measured values inside the dialog for debugging/confirmation
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current Azimuth: ${"%.2f".format(azimuth)}°", style = MaterialTheme.typography.labelMedium)
                    Text("Current Pitch: ${"%.2f".format(pitch)}°", style = MaterialTheme.typography.labelMedium)
                    Text("Current Roll: ${"%.2f".format(roll)}°", style = MaterialTheme.typography.labelMedium)

                }
            }
        }
    )
}


@Composable
fun rememberOrientationSensorValues(): Triple<Float, Float, Float> {
    val context = LocalContext.current

    var azimuth by remember { mutableFloatStateOf(0f) }
    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }

    val sensorManager = remember {
        OrientationSensorManager(context) { newAzimuth, newPitch, newRoll ->
            azimuth = newAzimuth
            pitch = newPitch
            roll = newRoll
        }
    }

    // 3. Manage the sensor lifecycle
    DisposableEffect(sensorManager) {
        sensorManager.start()

        onDispose {
            sensorManager.stop()
        }
    }

    // 4. Return the latest values
    return Triple(azimuth, pitch, roll)
}
