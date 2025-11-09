package com.sham.neopad.ui.gamepad

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.sham.neopad.appLog
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
import com.sham.neopad.ui.viewer.tiltIndicator
import com.sham.neopad.viewmodel.GamepadViewModel


@Composable
fun GamepadViewer(
    lvm: GamepadViewModel,
    layoutData: ControllerLayoutData,
    modifier: Modifier = Modifier,
    preview : Boolean = false
) {

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
                    Modifier.Companion.tiltIndicator(lvm.tiltState, lvm.tiltConfig,lvm.tiltSettings)
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
                        onMove = { x,y ->
                            lvm.gamepad.stickLeftX = x
                            lvm.gamepad.stickLeftY = y
                        }
                    )

                    ComponentType.RightStick -> ComposeJoystick(
                        modifier = finalModifier,
                        mainColor = compColor,
                        joystickType = JoystickType.RIGHT,
                        onMove = { x,y ->
                            lvm.gamepad.stickRightX = x
                            lvm.gamepad.stickRightY = y
                        }
                    )

                    ComponentType.Dpad -> ComposeDPad(
                        modifier = finalModifier,
                        mainColor = compColor,
                        isXboxStyle = layoutData.controllerType == ControllerType.XBOX,
                        onDpadDown = {
                            lvm.gamepad.setDpad(it,true)
                        },
                        onDpadUp = {
                            lvm.gamepad.setDpad(it,false)
                        },
                    )

                    ComponentType.GamepadButtons -> GamepadButtons(
                        modifier = finalModifier,
                        controllerType = layoutData.controllerType,
                        mainColor = compColor,
                        squareMode = comp.squareMode,
                        onButtonDown = {
                            appLog("Setting Button Down $it")
                            lvm.gamepad.setButton(it,true)
                        },
                        onButtonUp = {
                            appLog("Setting Button Up $it")
                            lvm.gamepad.setButton(it,false)
                        }
                    )

                    is ComponentType.ControllerButton -> ComposeControllerButton(
                        modifier = finalModifier,
                        buttonType = comp.type.button,
                        mainColor = compColor,
                        squareMode = comp.squareMode,
                        onButtonDown = {
                            lvm.gamepad.setButton(comp.type.button,true)
                        },
                        onButtonUp = {
                            lvm.gamepad.setButton(comp.type.button,false)
                        }
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

    // Compute size in pixels
    val wPx = when (comp.dimension) {
        is ComponentDimension.DoubleSize -> comp.dimension.width * screenWidth
        is ComponentDimension.SameSize -> comp.dimension.size * screenHeight
    }
    val hPx = when (comp.dimension) {
        is ComponentDimension.DoubleSize -> comp.dimension.height * screenHeight
        is ComponentDimension.SameSize -> comp.dimension.size * screenHeight
    }

    // Compute top-left corner (center-based)
    val xPos = comp.x * screenWidth - wPx / 2
    val yPos = comp.y * screenHeight - hPx / 2


    // Convert pixels → dp
    val widthDp = with(density) { wPx.toDp() }
    val heightDp = with(density) { hPx.toDp() }
    val xDp = with(density) { xPos.toDp() }
    val yDp = with(density) { yPos.toDp() }

    return Modifier
        .absoluteOffset(x = xDp, y = yDp)
        .size(widthDp, heightDp)
        .padding(3.dp)
}

