package com.sham.neopad.components

import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.graphics.ColorUtils
import kotlin.math.min
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sham.neopad.model.DpadDirection
import kotlin.math.atan2


@Composable
fun ComposeDPad(
    modifier: Modifier = Modifier,
    mainColor: Color = Color.Red,
    isXboxStyle: Boolean = false,
    onDpadDown: (dpadDirection: DpadDirection) -> Unit = {},
    onDpadUp: (dpadDirection: DpadDirection) -> Unit = {},
    disabled : Boolean = false
) {
    val pressedDpadDirection: MutableState<DpadDirection?> = remember { mutableStateOf(null) }

    val borderColor = remember(mainColor) { Color(ColorUtils.blendARGB(mainColor.toArgb(), Color.Black.toArgb(), 0.2f)) }
    val luminance = remember(mainColor) { ColorUtils.calculateLuminance(mainColor.toArgb()) }
    val arrowColor = remember(luminance) { if (luminance < 0.5) Color.White else Color.Black }
    val highlightColor = remember(mainColor) { Color(mainColor.toArgb() xor 0x00FFFFFF).copy(alpha = 0.47f) } // Alpha 120 / 255

    val outwardPadding = 10f
    val crossPadding = 10f

    var finalModifier = modifier
    if (!disabled) {
        finalModifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown()

                val centerX = size.width / 2f
                val centerY = size.height / 2f

                fun detectDirection(x: Float, y: Float): DpadDirection? {
                    val dx = x - centerX
                    val dy = y - centerY
                    val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                    val normalized = (angle + 360) % 360

                    return when {
                        normalized in 337.5..360.0 || normalized in 0.0..22.5 -> DpadDirection.EAST
                        normalized in 22.5..67.5 -> DpadDirection.SOUTH_EAST
                        normalized in 67.5..112.5 -> DpadDirection.SOUTH
                        normalized in 112.5..157.5 -> DpadDirection.SOUTH_WEST
                        normalized in 157.5..202.5 -> DpadDirection.WEST
                        normalized in 202.5..247.5 -> DpadDirection.NORTH_WEST
                        normalized in 247.5..292.5 -> DpadDirection.NORTH
                        normalized in 292.5..337.5 -> DpadDirection.NORTH_EAST
                        else -> null
                    }
                }

                val startDirection = detectDirection(down.position.x, down.position.y)
                if (startDirection != null) {
                    pressedDpadDirection.value = startDirection
                    onDpadDown(startDirection)
                }

                do {
                    val event = awaitPointerEvent()
                    val newPos = event.changes.first().position
                    val newDirection = detectDirection(newPos.x, newPos.y)

                    if (newDirection != pressedDpadDirection.value) {
                        pressedDpadDirection.value?.let { onDpadUp(it) }
                        if (newDirection != null) {
                            pressedDpadDirection.value = newDirection
                            onDpadDown(newDirection)
                        } else {
                            pressedDpadDirection.value = null
                        }
                    }
                } while (event.changes.any { it.pressed })

                pressedDpadDirection.value?.let { released ->
                    onDpadUp(released)
                    pressedDpadDirection.value = null
                }
            }
        }
    }


    Canvas(
        modifier = finalModifier
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        if (isXboxStyle) {
            drawXboxDpad(centerX, centerY, pressedDpadDirection.value, mainColor, borderColor, highlightColor, crossPadding)
        } else {
            drawPS4Dpad(centerX, centerY, pressedDpadDirection.value, mainColor, borderColor, arrowColor, highlightColor, outwardPadding)
        }
    }
}


private fun DrawScope.drawPS4Dpad(
    centerX: Float,
    centerY: Float,
    pressedDpadDirection: DpadDirection?,
    mainColor: Color,
    borderColor: Color,
    arrowColor: Color,
    highlightColor: Color,
    outwardPadding: Float
) {
    val wh = min(size.width, size.height)
    val teethSize = ((wh / 2) * 0.96f - outwardPadding) / 1.86f
    val teethAltitude = teethSize * 0.86f
    val arrowSize = (20f / 200f) * wh

    fun drawPS4Button(arrow: String, currentDpadDirection: DpadDirection) {
        var glossStartX = 0f
        var glossStartY = 0f
        var glossEndX = 0f
        var glossEndY = 0f

        val path = Path()
        val s = teethSize
        val a = teethAltitude
        var cx = centerX
        var cy = centerY
        var xChange = floatArrayOf()
        var yChange = floatArrayOf()
        val glossOffset = s * 1.2f

        when (arrow) {
            "▼" -> {
                cy += outwardPadding
                xChange = floatArrayOf(-s / 2, -s / 2, s / 2, s / 2, 0f)
                yChange = floatArrayOf(a, a + s, a + s, a, 0f)
                glossStartX = cx; glossStartY = cy - glossOffset
                glossEndX = cx; glossEndY = cy + glossOffset
            }
            "▲" -> {
                cy -= outwardPadding
                xChange = floatArrayOf(-s / 2, -s / 2, s / 2, s / 2, 0f)
                yChange = floatArrayOf(-a, -a - s, -a - s, -a, 0f)
                glossStartX = cx; glossStartY = cy + glossOffset
                glossEndX = cx; glossEndY = cy - glossOffset
            }
            "◄" -> {
                cx -= outwardPadding
                yChange = floatArrayOf(-s / 2, -s / 2, s / 2, s / 2, 0f)
                xChange = floatArrayOf(-a, -a - s, -a - s, -a, 0f)
                glossStartX = cx - glossOffset; glossStartY = cy
                glossEndX = cx + glossOffset; glossEndY = cy
            }
            "►" -> {
                cx += outwardPadding
                yChange = floatArrayOf(-s / 2, -s / 2, s / 2, s / 2, 0f)
                xChange = floatArrayOf(a, a + s, a + s, a, 0f)
                glossStartX = cx + glossOffset; glossStartY = cy
                glossEndX = cx - glossOffset; glossEndY = cy
            }
        }

        path.moveTo(cx, cy)
        for (i in xChange.indices) {
            path.lineTo(cx + xChange[i], cy + yChange[i])
        }
        path.close()

        // 1. Draw Fill
        drawPath(path, color = mainColor, style = Fill)

        val buttonCenterX = cx
        val buttonCenterY = cy

        val gradientRadius = min(s + a, s * 2f) * 0.5f

        // 2. Draw Gloss (Requires native Canvas for LinearGradient)
        drawIntoCanvas {
            val glossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    buttonCenterX,
                    buttonCenterY,
                    gradientRadius,
                    Color.White.toArgb(),
                    Color.Transparent.toArgb(),
                    Shader.TileMode.CLAMP
                )
                alpha = 100
            }
            it.nativeCanvas.drawPath(path.asAndroidPath(), glossPaint)
        }

        drawPath(path, color = borderColor, style = Stroke(width = 6f))


        if (pressedDpadDirection != null &&
            pressedDpadDirection.bits.intersect(currentDpadDirection.bits).isNotEmpty()
        ) {
            drawPath(path, color = highlightColor, style = Fill)
        }


        val arrowPath = Path()
        val arrowPadding = 0.70f * (s + a)
        when (arrow){
            "▲" -> {
                arrowPath.moveTo(cx, cy - arrowSize - arrowPadding)
                arrowPath.lineTo(cx - arrowSize / 2, cy - arrowPadding)
                arrowPath.lineTo(cx + arrowSize / 2, cy - arrowPadding)
            }
            "▼" -> {
                arrowPath.moveTo(cx, cy + arrowSize + arrowPadding)
                arrowPath.lineTo(cx - arrowSize / 2, cy + arrowPadding)
                arrowPath.lineTo(cx + arrowSize / 2, cy + arrowPadding)
            }
            "◄" -> {
                arrowPath.moveTo(cx - arrowSize - arrowPadding, cy)
                arrowPath.lineTo(cx - arrowPadding, cy - arrowSize / 2)
                arrowPath.lineTo(cx - arrowPadding, cy + arrowSize / 2)
            }
            "►" -> {
                arrowPath.moveTo(cx + arrowSize + arrowPadding, cy)
                arrowPath.lineTo(cx + arrowPadding, cy - arrowSize / 2)
                arrowPath.lineTo(cx + arrowPadding, cy + arrowSize / 2)
            }
        }
        arrowPath.close()
        drawPath(arrowPath, color = arrowColor, style = Fill)
    }

    drawPS4Button("▲", DpadDirection.NORTH)
    drawPS4Button("▼", DpadDirection.SOUTH)
    drawPS4Button("◄", DpadDirection.WEST)
    drawPS4Button("►", DpadDirection.EAST)
}

private fun DrawScope.drawXboxDpad(
    centerX: Float,
    centerY: Float,
    pressedDpadDirection: DpadDirection?,
    mainColor: Color,
    borderColor: Color,
    highlightColor: Color,
    crossPadding: Float
) {
    val wh = min(size.width, size.height)
    val crossSize = (wh - (2 * crossPadding)) / 3f
    val a = crossSize * 1.5f
    val b = crossSize / 2f

    val path = Path()
    path.moveTo(centerX - b, centerY - b)
    path.lineTo(centerX - b, centerY - a)
    path.lineTo(centerX + b, centerY - a)
    path.lineTo(centerX + b, centerY - b)
    path.lineTo(centerX + a, centerY - b)
    path.lineTo(centerX + a, centerY + b)
    path.lineTo(centerX + b, centerY + b)
    path.lineTo(centerX + b, centerY + a)
    path.lineTo(centerX - b, centerY + a)
    path.lineTo(centerX - b, centerY + b)
    path.lineTo(centerX - a, centerY + b)
    path.lineTo(centerX - a, centerY - b)
    path.lineTo(centerX - b, centerY - b)
    path.close()

    drawPath(path, color = mainColor, style = Fill)

    drawIntoCanvas {
        val sizeF = min(size.width, size.height)
        val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                centerX, centerY, sizeF *0.5f,
                Color.LightGray.toArgb(), mainColor.toArgb(), Shader.TileMode.CLAMP
            )
        }
        it.nativeCanvas.drawPath(path.asAndroidPath(), gradientPaint)
    }

    if (pressedDpadDirection != null) {
        drawPath(path, color = highlightColor, style = Fill)
    }

    drawPath(path, color = borderColor, style = Stroke(width = 6f))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
fun DPadComparisonPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "PS4 Style", color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                ComposeDPad(
                    modifier = Modifier.size(120.dp),
                    mainColor = Color(0xFFFF5252), // A deep blue color
                    isXboxStyle = false, // Set to false for the separated button style
                    onDpadUp = { /* Preview: No action */ },
                    onDpadDown = { /* Preview: No action */ }
                )
            }

            // ### 2. XBOX Style D-Pad ###
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Xbox Style", color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                ComposeDPad(
                    modifier = Modifier.size(120.dp),
                    mainColor = Color(0xFF4C8C4A), // A dark green color
                    isXboxStyle = true, // Set to true for the cross style
                    onDpadUp = { /* Preview: No action */ },
                    onDpadDown = { /* Preview: No action */ }
                )
            }
        }
    }
}