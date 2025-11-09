package com.sham.neopad.ui.creator

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.sham.neopad.components.ComposeControllerButton
import com.sham.neopad.components.ComposeDPad
import com.sham.neopad.components.ComposeJoystick
import com.sham.neopad.components.GamepadButtons
import com.sham.neopad.components.JoystickType
import com.sham.neopad.model.ComponentDimension
import com.sham.neopad.model.ComponentType
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.GamepadComponent
import com.sham.neopad.viewmodel.LayoutCreatorModel


@Composable
fun LayoutCreator(
    lcm: LayoutCreatorModel,
    modifier: Modifier = Modifier,
) {

    var screenWidth by remember { mutableIntStateOf(0) }
    var screenHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                screenWidth = it.width
                screenHeight = it.height
                lcm.screenWidth = it.width
                lcm.screenHeight = it.height
            }
            .detectLayoutGestures(lcm, screenWidth, screenHeight,)
    ) {
        if (screenWidth > 0 && screenHeight > 0){
            lcm.components.forEach { comp ->
                val compColor = Color(comp.color)
                val isSelected = lcm.selectedComponent?.creatorId == comp.creatorId
                val finalModifier = Modifier.componentVisuals(comp, isSelected, screenWidth, screenHeight)

                when (comp.type) {
                    ComponentType.LeftStick -> ComposeJoystick(
                        modifier = finalModifier,
                        mainColor = compColor,
                        joystickType = JoystickType.LEFT,
                        disabled = true
                    )

                    ComponentType.RightStick -> ComposeJoystick(
                        modifier = finalModifier,
                        mainColor = compColor,
                        joystickType = JoystickType.RIGHT,
                        disabled = true
                    )

                    ComponentType.Dpad -> ComposeDPad(
                        modifier = finalModifier,
                        mainColor = compColor,
                        isXboxStyle = lcm.controllerType == ControllerType.XBOX,
                        disabled = true
                    )

                    ComponentType.GamepadButtons -> GamepadButtons(
                        modifier = finalModifier,
                        controllerType = lcm.controllerType,
                        mainColor = compColor,
                        squareMode = comp.squareMode,
                        disabled = true
                    )

                    is ComponentType.ControllerButton -> ComposeControllerButton(
                        modifier = finalModifier,
                        buttonType = comp.type.button,
                        mainColor = compColor,
                        squareMode = comp.squareMode,
                        disabled = true
                    )
                }
            }
        }

    }
}



@Composable
fun Modifier.detectLayoutGestures(
    lcm: LayoutCreatorModel,
    screenWidth: Int,
    screenHeight: Int,
): Modifier = pointerInput(Unit) {
    var dragStartPosition: Offset? = null
    var componentStartPosition: Offset? = null

    detectDragGestures(
        onDragStart = { offset ->
            // Find and select the touched component
            val touched = lcm.findTouchedComponent(offset.x, offset.y, screenWidth, screenHeight)
            lcm.selectComponent(touched)

            // Store the starting positions
            dragStartPosition = offset
            componentStartPosition = touched?.let { Offset(it.x, it.y) }

        },
        onDrag = { change, dragAmount ->
            change.consume()

            if (lcm.selectedComponent == null) return@detectDragGestures
            val startDrag = dragStartPosition ?: return@detectDragGestures
            val startComp = componentStartPosition ?: return@detectDragGestures

            // USE change.position for ABSOLUTE position, not accumulated dragAmount
            val currentTouchX = change.position.x
            val currentTouchY = change.position.y

            val totalOffsetPxX = currentTouchX - startDrag.x
            val totalOffsetPxY = currentTouchY - startDrag.y

            val normalizedDeltaX = totalOffsetPxX / screenWidth
            val normalizedDeltaY = totalOffsetPxY / screenHeight

            val newX = (startComp.x + normalizedDeltaX).coerceIn(0f, 1f)
            val newY = (startComp.y + normalizedDeltaY).coerceIn(0f, 1f)

            lcm.updateComponentPosition(newX, newY)
        },
        onDragEnd = {
            dragStartPosition = null
            componentStartPosition = null
        },
        onDragCancel = {
            dragStartPosition = null
            componentStartPosition = null
        }
    )
}.pointerInput(lcm.components) {
    // Separate tap gesture to handle selection without dragging
    detectTapGestures { offset ->
        val touched = lcm.findTouchedComponent(offset.x, offset.y, screenWidth, screenHeight)
        lcm.selectComponent(touched)
    }
}



private fun LayoutCreatorModel.findTouchedComponent(
    touchX: Float,
    touchY: Float,
    screenWidth: Int,
    screenHeight: Int,
): GamepadComponent? {
    //appLog("Find Touched Component $touchX, $touchY $screenWidth,$screenHeight")

    return components.findLast { comp ->
        // Compute size in pixels
        val wPx = when (comp.dimension) {
            is ComponentDimension.DoubleSize -> comp.dimension.width * screenWidth
            is ComponentDimension.SameSize -> comp.dimension.size * screenHeight
        }
        val hPx = when (comp.dimension) {
            is ComponentDimension.DoubleSize -> comp.dimension.height * screenHeight
            is ComponentDimension.SameSize -> comp.dimension.size * screenHeight
        }

        // Compute top-left corner (center-based positioning)
        val xPx = comp.x * screenWidth - wPx / 2
        val yPx = comp.y * screenHeight - hPx / 2

        touchX in xPx..(xPx + wPx) && touchY in yPx..(yPx + hPx)
    }
}


@SuppressLint("ModifierFactoryUnreferencedReceiver")
@Composable
fun Modifier.componentVisuals(
    comp: GamepadComponent,
    isSelected: Boolean,
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
        .then(
            if (isSelected)
                Modifier.border(
                    width = 3.dp,
                    brush = Brush.linearGradient(listOf(Color.Cyan, Color.Blue, Color.Magenta)),
                    shape = RoundedCornerShape(1.dp)
                )
            else Modifier
        )
}

