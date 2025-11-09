package com.sham.neopad.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sham.neopad.model.ButtonType
import com.sham.neopad.model.ControllerType

@Composable
fun GamepadButtons(
    modifier: Modifier = Modifier,
    controllerType: ControllerType = ControllerType.PS4,
    mainColor: Color = Color.Red,
    squareMode: Boolean = false,
    onButtonUp: (ButtonType) -> Unit = {},
    onButtonDown : (ButtonType) -> Unit = {},
    disabled : Boolean = false
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (controllerType) {
            ControllerType.PS4 -> PS4ButtonLayout(
                mainColor = mainColor,
                squareMode = squareMode,
                onButtonUp = onButtonUp,
                onButtonDown = onButtonDown,
                disabled = disabled
            )
            ControllerType.XBOX -> XboxButtonLayout(
                mainColor = mainColor,
                squareMode = squareMode,
                onButtonUp = onButtonUp,
                onButtonDown = onButtonDown,
                disabled = disabled
            )
        }
    }
}

@Composable
private fun PS4ButtonLayout(
    mainColor: Color,
    squareMode: Boolean,
    onButtonUp: (ButtonType) -> Unit,
    onButtonDown : (ButtonType) -> Unit,
    disabled: Boolean
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val size = minOf(maxWidth, maxHeight)
        val offset = size / 3.1f
        val buttonSize = size / 3.01f

        // Top - Triangle
        ComposeControllerButton(
            modifier = Modifier
                .size(buttonSize)
                .offset(y = -offset),
            buttonType = ButtonType.Triangle,
            mainColor = mainColor,
            squareMode = squareMode,
            onButtonDown = { onButtonDown(ButtonType.Triangle) },
            onButtonUp = { onButtonUp(ButtonType.Triangle) },
            disabled = disabled
        )

        // Right - Circle
        ComposeControllerButton(
            modifier = Modifier
                .size(buttonSize)
                .offset(x = offset),
            buttonType = ButtonType.Circle,
            mainColor = mainColor,
            squareMode = squareMode,
            onButtonDown = { onButtonDown(ButtonType.Circle) },
            onButtonUp = { onButtonUp(ButtonType.Circle) },
            disabled = disabled
        )

        // Bottom - Cross
        ComposeControllerButton(
            modifier = Modifier
                .size(buttonSize)
                .offset(y = offset),
            buttonType = ButtonType.Cross,
            mainColor = mainColor,
            squareMode = squareMode,
            onButtonDown = { onButtonDown(ButtonType.Cross) },
            onButtonUp = { onButtonUp(ButtonType.Cross) },
            disabled = disabled
        )

        // Left - Square
        ComposeControllerButton(
            modifier = Modifier
                .size(buttonSize)
                .offset(x = -offset),
            buttonType = ButtonType.Square,
            mainColor = mainColor,
            squareMode = squareMode,
            onButtonDown = { onButtonDown(ButtonType.Square) },
            onButtonUp = { onButtonUp(ButtonType.Square) },
            disabled = disabled
        )
    }
}

@Composable
private fun XboxButtonLayout(
    mainColor: Color,
    squareMode: Boolean,
    onButtonUp: (ButtonType) -> Unit,
    onButtonDown : (ButtonType) -> Unit,
    disabled: Boolean
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val size = minOf(maxWidth, maxHeight)
        val offset = size / 3.13f
        val buttonSize = size / 3.2f

        // Top - Y
        ComposeControllerButton(
            modifier = Modifier
                .size(buttonSize)
                .offset(y = -offset),
            buttonType = ButtonType.Y,
            mainColor = mainColor,
            squareMode = squareMode,
            onButtonDown = { onButtonDown(ButtonType.Y) },
            onButtonUp = { onButtonUp(ButtonType.Y) },
            disabled = disabled
        )

        // Right - B
        ComposeControllerButton(
            modifier = Modifier
                .size(buttonSize)
                .offset(x = offset),
            buttonType = ButtonType.B,
            mainColor = mainColor,
            squareMode = squareMode,
            onButtonDown = { onButtonDown(ButtonType.B) },
            onButtonUp = { onButtonUp(ButtonType.B) },
            disabled = disabled
        )

        // Bottom - A
        ComposeControllerButton(
            modifier = Modifier
                .size(buttonSize)
                .offset(y = offset),
            buttonType = ButtonType.A,
            mainColor = mainColor,
            squareMode = squareMode,
            onButtonDown = { onButtonDown(ButtonType.A) },
            onButtonUp = { onButtonUp(ButtonType.A) },
            disabled = disabled
        )

        // Left - X
        ComposeControllerButton(
            modifier = Modifier
                .size(buttonSize)
                .offset(x = -offset),
            buttonType = ButtonType.X,
            mainColor = mainColor,
            squareMode = squareMode,
            onButtonDown = { onButtonDown(ButtonType.X) },
            onButtonUp = { onButtonUp(ButtonType.X) },
            disabled = disabled
        )
    }
}

// Usage example:
@Preview(showBackground = true)
@Composable
fun PreviewGamepadButtons() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // PS4 Layout
        GamepadButtons(
            modifier = Modifier
                .size(300.dp)
                .background(Color.LightGray),
            controllerType = ControllerType.PS4,
            mainColor = Color(0xFFF4F9FF),
            squareMode = true,
            onButtonUp = {},
            onButtonDown = {}
        )

        // Xbox Layout
        GamepadButtons(
            modifier = Modifier
                .size(300.dp)
                .background(Color.LightGray),
            controllerType = ControllerType.XBOX,
            mainColor = Color(0xFF107C10),
            squareMode = true,
            onButtonUp = {},
            onButtonDown = {}
        )
    }
}