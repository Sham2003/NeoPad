package com.sham.neopad.ui.viewer

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sham.neopad.components.ComposeControllerButton
import com.sham.neopad.components.ComposeDPad
import com.sham.neopad.components.ComposeJoystick
import com.sham.neopad.components.GamepadButtons
import com.sham.neopad.components.JoystickType
import com.sham.neopad.model.ComponentDimension
import com.sham.neopad.model.ComponentType
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.GamepadComponent
import com.sham.neopad.viewmodel.LayoutViewerModel


@Composable
fun LayoutViewer(
    lvm: LayoutViewerModel,
    layoutData: ControllerLayoutData,
    modifier: Modifier = Modifier,
) {
    val preview = false
    var screenWidth by remember { mutableIntStateOf(0) }
    var screenHeight by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                screenWidth = it.width
                screenHeight = it.height
            }.then(
                if (!preview) {
                    Modifier.tiltIndicator(lvm.tiltState, lvm.tiltConfig,lvm.tiltSettings)
                } else {
                    Modifier
                }
            )
    )
    {
        if (screenWidth > 0 && screenHeight > 0) {
            layoutData.components.forEach { comp ->
                val compColor = Color(comp.color)
                val finalModifier = Modifier.componentViewer(comp, screenWidth, screenHeight)

                when (comp.type) {
                    ComponentType.LeftStick -> ComposeJoystick(
                        modifier = finalModifier,
                        mainColor = compColor,
                        joystickType = JoystickType.LEFT,
                        disabled = preview
                    )

                    ComponentType.RightStick -> ComposeJoystick(
                        modifier = finalModifier,
                        mainColor = compColor,
                        joystickType = JoystickType.RIGHT,
                        disabled = preview
                    )

                    ComponentType.Dpad -> ComposeDPad(
                        modifier = finalModifier,
                        mainColor = compColor,
                        isXboxStyle = layoutData.controllerType == ControllerType.XBOX,
                        disabled = preview
                    )

                    ComponentType.GamepadButtons -> GamepadButtons(
                        modifier = finalModifier,
                        controllerType = layoutData.controllerType,
                        mainColor = compColor,
                        squareMode = comp.squareMode,
                        disabled = preview,
                    )

                    is ComponentType.ControllerButton -> ComposeControllerButton(
                        modifier = finalModifier,
                        buttonType = comp.type.button,
                        mainColor = compColor,
                        squareMode = comp.squareMode,
                        disabled = preview
                    )
                }
            }

            if (!preview) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(0.3f)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Tilt: x=${"%.2f".format(lvm.tiltState.r)}°, y=${"%.2f".format(lvm.tiltState.p)}°",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@SuppressLint("ModifierFactoryUnreferencedReceiver")
@Composable
fun Modifier.componentViewer(
    comp: GamepadComponent,
    screenWidth: Int,
    screenHeight: Int
): Modifier {
    val density = LocalDensity.current

    val wPx = when (comp.dimension) {
        is ComponentDimension.DoubleSize -> comp.dimension.width * screenWidth
        is ComponentDimension.SameSize -> comp.dimension.size * screenHeight
    }
    val hPx = when (comp.dimension) {
        is ComponentDimension.DoubleSize -> comp.dimension.height * screenHeight
        is ComponentDimension.SameSize -> comp.dimension.size * screenHeight
    }

    val xPos = comp.x * screenWidth - wPx / 2
    val yPos = comp.y * screenHeight - hPx / 2


    val widthDp = with(density) { wPx.toDp() }
    val heightDp = with(density) { hPx.toDp() }
    val xDp = with(density) { xPos.toDp() }
    val yDp = with(density) { yPos.toDp() }

    return Modifier
        .absoluteOffset(x = xDp, y = yDp)
        .size(widthDp, heightDp)
}

