package com.sham.neopad.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.hypot
import kotlin.math.min

enum class JoystickType { LEFT, RIGHT }

@Composable
fun ComposeJoystick(
    modifier: Modifier = Modifier,
    mainColor: Color = Color.Red,
    joystickType: JoystickType = JoystickType.LEFT,
    showLabel: Boolean = true,
    onMove: (stickX: Short, stickY: Short) -> Unit = { x, y -> },
    disabled: Boolean = false
) {
    val density = LocalDensity.current.density

    val maxShortValue = 32767.0f
    val hatPosition: MutableState<Offset> = remember { mutableStateOf(Offset(0f, 0f)) }

    val baseRadiusPx = remember { mutableFloatStateOf(0f) }
    val hatRadiusPx = remember { mutableFloatStateOf(0f) }

    val borderColor = remember(mainColor) {
        Color(ColorUtils.blendARGB(mainColor.toArgb(), Color.Black.toArgb(), 0.3f))
    }
    val luminance = remember(mainColor) { ColorUtils.calculateLuminance(mainColor.toArgb()) }
    val hatColor = remember(luminance) { if (luminance < 0.5) Color.White else Color.Black }
    val shadowColor = remember(luminance) { if (luminance < 0.5) Color.Gray else Color.LightGray }

    var finalModifier = modifier

    if (!disabled) {
        finalModifier  = modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown()

                // DRAG GESTURE
                drag(down.id) { change ->
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    // Calculate drag delta from the center of the component
                    val dragX = change.position.x - centerX
                    val dragY = change.position.y - centerY

                    val distance = hypot(dragX, dragY)
                    val baseR = baseRadiusPx.floatValue // Use the current calculated radius
                    val ratio = if (distance > baseR) baseR / distance else 1f

                    hatPosition.value = Offset(dragX * ratio, dragY * ratio)

                    val stickXFloat = hatPosition.value.x / baseR
                    val stickYFloat = -hatPosition.value.y / baseR

                    val stickXShort = (stickXFloat * maxShortValue).toInt().toShort()
                    val stickYShort = (stickYFloat * maxShortValue).toInt().toShort()

                    onMove(stickXShort, stickYShort)
                    change.consume()
                }

                hatPosition.value = Offset(0f, 0f)
                onMove(0,0)
            }
        }
    }


    Canvas(
        modifier = finalModifier
    ) {
        // --- DRAWING LOGIC (Replaces onDraw) ---

        val sizePx = min(size.width, size.height)
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Update radii for use in gesture and drawing
        baseRadiusPx.floatValue = sizePx / 3
        hatRadiusPx.floatValue = sizePx / 6

        val hatX = centerX + hatPosition.value.x
        val hatY = centerY + hatPosition.value.y

        // 1. Draw Base
        drawCircle(color = mainColor, radius = baseRadiusPx.floatValue, center = Offset(centerX, centerY))

        // 2. Draw Border (Simulated with Stroke style)
        drawCircle(
            color = borderColor,
            radius = baseRadiusPx.floatValue,
            center = Offset(centerX, centerY),
            style = Stroke(width = 8f * density)
        )

        // 3. Draw Moving Hat Shadow
        drawCircle(
            color = shadowColor.copy(alpha = 0.4f),
            radius = hatRadiusPx.floatValue,
            center = Offset(hatX, hatY)
        )

        // 4. Draw Moving Hat
        drawCircle(color = hatColor, radius = hatRadiusPx.floatValue, center = Offset(hatX, hatY))

        // 5. Gloss Effect and 6. Label (Using native Canvas as in your original View)
        drawIntoCanvas {
            val nativeCanvas = it.nativeCanvas

            // Gloss Effect
            val glossGradient = RadialGradient(
                hatX, hatY - hatRadiusPx.floatValue / 2f,
                hatRadiusPx.floatValue,
                Color.White.toArgb(),
                Color.Transparent.toArgb(),
                Shader.TileMode.CLAMP
            )
            val glossPaint = Paint().apply {
                isAntiAlias = true
                shader = glossGradient
                alpha = 120
            }
            nativeCanvas.drawCircle(hatX, hatY, hatRadiusPx.floatValue, glossPaint)

            // Label
            if (showLabel) {
                val textPaint = Paint().apply {
                    isAntiAlias = true
                    color = mainColor.toArgb()
                    textAlign = Paint.Align.CENTER
                    textSize = hatRadiusPx.floatValue // Size proportional to hat radius
                }
                nativeCanvas.drawText(
                    joystickType.name.take(1).uppercase(),
                    hatX,
                    hatY + textPaint.textSize / 3, // Adjust for text baseline
                    textPaint
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun JoystickPreview() {
    // 1. Use a Box to take up the space and center the child.
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center // Centers the joystick horizontally and vertically
    ) {
        Column {
            ComposeJoystick(
                modifier = Modifier.size(150.dp),
                mainColor = Color.Red,
                joystickType = JoystickType.LEFT,
                showLabel = true,

                onMove = { angle, strength ->
                    // No action here
                }
            )
            ComposeJoystick(
                // Use a specific size for the preview
                modifier = Modifier.size(150.dp),

                // Set required parameters
                mainColor = Color.Red,
                joystickType = JoystickType.RIGHT,
                showLabel = true,

                onMove = { angle, strength ->
                    // No action here
                }
            )
        }
    }
}
