package com.sham.neopad.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sham.neopad.components.ComposeControllerButton
import com.sham.neopad.components.ComposeDPad
import com.sham.neopad.components.ComposeJoystick
import com.sham.neopad.components.GamepadButtons
import com.sham.neopad.components.JoystickType
import com.sham.neopad.model.ButtonType
import com.sham.neopad.model.ComponentType
import com.sham.neopad.model.ControllerType


@Composable
fun AddComponentContainer1(
    modifier: Modifier = Modifier,
    type: ControllerType,
    onSelect: (ComponentType) -> Unit = {}
) {
    val buttons = when (type) {
        ControllerType.PS4 -> ButtonType.PS4_BUTTONS
        ControllerType.XBOX -> ButtonType.XBOX_BUTTONS
    }
    val isXBOX = type == ControllerType.XBOX

    Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            color = Color(0xFFD2BBBB),
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = "Select Component",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                // Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    // Joysticks, Dpad, Buttons
                    item {
                        ComposeJoystick(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { onSelect(ComponentType.LeftStick) },
                            joystickType = JoystickType.LEFT,
                            disabled = true
                        )
                    }

                    item {
                        ComposeJoystick(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { onSelect(ComponentType.RightStick) },
                            joystickType = JoystickType.RIGHT,
                            disabled = true
                        )
                    }

                    item {
                        ComposeDPad(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { onSelect(ComponentType.Dpad) },
                            isXboxStyle = isXBOX,
                            disabled = true
                        )
                    }

                    item {
                        GamepadButtons(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { onSelect(ComponentType.GamepadButtons) },
                            controllerType = type,
                            disabled = true
                        )
                    }

                    items(buttons.size) { i ->
                        ComposeControllerButton(
                            modifier = Modifier
                                .size(60.dp)
                                .aspectRatio(1f)
                                .clickable {
                                    onSelect(ComponentType.ControllerButton(buttons[i]))
                                },
                            buttonType = buttons[i],
                            disabled = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddComponentContainer(
    modifier: Modifier = Modifier,
    type: ControllerType,
    onSelect: (ComponentType) -> Unit = {}
) {
    val buttons = when (type) {
        ControllerType.PS4 -> ButtonType.PS4_BUTTONS
        ControllerType.XBOX -> ButtonType.XBOX_BUTTONS
    }
    val isXBOX = type == ControllerType.XBOX

    Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            color = Color(0xFFD2BBBB),
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = "Select Component",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                // Scrollable Column for big items + grid
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Row for the main big components
                    Row(
                        modifier = Modifier
                            .wrapContentWidth() // .horizontalScroll(rememberScrollState())
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ComposeJoystick(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { onSelect(ComponentType.LeftStick) },
                            joystickType = JoystickType.LEFT,
                            disabled = true
                        )

                        ComposeJoystick(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { onSelect(ComponentType.RightStick) },
                            joystickType = JoystickType.RIGHT,
                            disabled = true
                        )

                        ComposeDPad(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { onSelect(ComponentType.Dpad) },
                            isXboxStyle = isXBOX,
                            disabled = true
                        )

                        GamepadButtons(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { onSelect(ComponentType.GamepadButtons) },
                            controllerType = type,
                            disabled = true
                        )
                    }

                    // Lazy grid for smaller buttons
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp) // prevent infinite height
                    ) {
                        items(buttons.size) { i ->
                            ComposeControllerButton(
                                modifier = Modifier
                                    .size(50.dp)
                                    .aspectRatio(1f)
                                    .clickable {
                                        onSelect(ComponentType.ControllerButton(buttons[i]))
                                    },
                                buttonType = buttons[i],
                                disabled = true
                            )
                        }
                    }
                }
            }
        }
    }
}


