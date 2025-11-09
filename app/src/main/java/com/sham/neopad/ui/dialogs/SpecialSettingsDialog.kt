package com.sham.neopad.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.sham.neopad.R
import com.sham.neopad.model.ButtonType
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.SpecialButtons
import com.sham.neopad.viewmodel.LayoutCreatorModel


@Composable
fun ButtonSelector(
    label: String,
    icon: @Composable () -> Unit,
    type: ControllerType,
    selectedButton: ButtonType,
    onButtonSelected: (ButtonType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectableButtons = when(type) {
        ControllerType.PS4 -> ButtonType.PS4_BUTTONS + ButtonType.None
        ControllerType.XBOX -> ButtonType.XBOX_BUTTONS + ButtonType.None
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Label with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(modifier = Modifier.size(24.dp)) {
                icon()
            }
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }

        // Dropdown button
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    selectedButton.label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                selectableButtons.forEach { button ->
                    DropdownMenuItem(
                        text = { Text(button.label) },
                        leadingIcon = {
                            if (button.isDrawable) {
                                Icon(
                                    painter = painterResource(button.drawable),
                                    contentDescription = button.label,
                                    modifier = Modifier.size(40.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Games,
                                    contentDescription = "Others"
                                )
                            }
                        },
                        onClick = {
                            onButtonSelected(button)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SpecialButtonsDialog(
    modifier: Modifier = Modifier,
    lcm: LayoutCreatorModel,
    onDismissRequest: () -> Unit
) {
    val initialButtons = lcm.specialButtons
    var left by remember { mutableStateOf(initialButtons.left) }
    var right by remember { mutableStateOf(initialButtons.right) }
    var up by remember { mutableStateOf(initialButtons.up) }
    var down by remember { mutableStateOf(initialButtons.down) }
    var volumeUp by remember { mutableStateOf(initialButtons.volumeUp) }
    var volumeDown by remember { mutableStateOf(initialButtons.volumeDown) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                "Special Buttons",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        text = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Direction controls
                Text(
                    "Navigation",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                ButtonSelector(
                    label = "Left",
                    icon = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) },
                    selectedButton = left,
                    onButtonSelected = { left = it },
                    type = lcm.controllerType
                )

                ButtonSelector(
                    label = "Right",
                    icon = { Icon(painterResource(R.drawable.ic_tilt_right), null) },
                    selectedButton = right,
                    onButtonSelected = { right = it },
                    type = lcm.controllerType
                )

                ButtonSelector(
                    label = "Up",
                    icon = { Icon(painterResource(R.drawable.ic_tilt_left), null) },
                    selectedButton = up,
                    onButtonSelected = { up = it },
                    type = lcm.controllerType
                )

                ButtonSelector(
                    label = "Down",
                    icon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                    selectedButton = down,
                    onButtonSelected = { down = it },
                    type = lcm.controllerType
                )

                HorizontalDivider()

                // Volume controls
                Text(
                    "Volume",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                ButtonSelector(
                    label = "Volume Up",
                    icon = { Icon(Icons.AutoMirrored.Filled.VolumeUp, null) },
                    selectedButton = volumeUp,
                    onButtonSelected = { volumeUp = it },
                    type = lcm.controllerType
                )

                ButtonSelector(
                    label = "Volume Down",
                    icon = { Icon(Icons.AutoMirrored.Filled.VolumeDown, null) },
                    selectedButton = volumeDown,
                    onButtonSelected = { volumeDown = it },
                    type = lcm.controllerType
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    lcm.changeSpecialSettings(
                        SpecialButtons(left, right, up, down, volumeUp, volumeDown)
                    )
                    onDismissRequest()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp)
    )
}