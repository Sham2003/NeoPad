package com.sham.neopad.components

import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import android.graphics.Color as AndroidColor

@Composable
fun RoundColorPicker(
    modifier: Modifier = Modifier,
    selectedColor: Color = Color.Red,
    onColorSelected: (Color) -> Unit,
    onDismissRequest: () -> Unit
) {
    var pointerPosition by remember { mutableStateOf<Offset?>(null) }
    var centerOffset by remember { mutableStateOf(Offset.Zero) }
    var radiusValue by remember { mutableFloatStateOf(0f) }

    // Get screen dimensions to calculate max size
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidth = with(density) {
        windowInfo.containerSize.width.toDp()
    }
    val screenHeight = with(density) {
        windowInfo.containerSize.height.toDp()
    }

    // Calculate max available size for the color wheel (leaving room for other elements)
    val maxColorWheelSize = min(screenWidth.value - 80, screenHeight.value - 280).dp

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 8.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Select Color",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Display the currently selected color (smaller box)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Display selected color hex value
                Text(
                    text = "#${selectedColor.toArgb().toUInt().toString(16).uppercase().takeLast(6)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Circular color selection area with constrained size
                Box(
                    modifier = Modifier
                        .size(maxColorWheelSize.coerceAtMost(300.dp))
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val borderColor = MaterialTheme.colorScheme.outline
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp) // Inner padding to ensure wheel stays inside
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        handleTouch(
                                            offset,
                                            centerOffset,
                                            radiusValue,
                                            onPositionUpdate = { pointerPosition = it },
                                            onColorSelected
                                        )
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        handleTouch(
                                            change.position,
                                            centerOffset,
                                            radiusValue,
                                            onPositionUpdate = { pointerPosition = it },
                                            onColorSelected
                                        )
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    handleTouch(
                                        offset,
                                        centerOffset,
                                        radiusValue,
                                        onPositionUpdate = { pointerPosition = it },
                                        onColorSelected
                                    )
                                }
                            }
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2f - 2.dp.toPx() // Extra margin for border

                        // Update state only if changed
                        if (centerOffset != center) centerOffset = center
                        if (radiusValue != radius) radiusValue = radius

                        // Only draw if radius is valid
                        if (radius > 0) {
                            drawIntoCanvas { canvas ->
                                // Draw hue sweep gradient
                                val colors = IntArray(361)
                                for (i in 0..360) {
                                    colors[i] = AndroidColor.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
                                }
                                val sweepGradient = SweepGradient(
                                    center.x, center.y, colors, null
                                )
                                val paint = Paint().apply {
                                    shader = sweepGradient
                                    style = Paint.Style.FILL
                                    isAntiAlias = true
                                }
                                canvas.nativeCanvas.drawCircle(center.x, center.y, radius, paint)

                                // Draw white radial gradient for saturation
                                val radialGradient = RadialGradient(
                                    center.x, center.y, radius,
                                    intArrayOf(AndroidColor.WHITE, AndroidColor.TRANSPARENT),
                                    floatArrayOf(0f, 1f),
                                    Shader.TileMode.CLAMP
                                )
                                val saturationPaint = Paint().apply {
                                    shader = radialGradient
                                    style = Paint.Style.FILL
                                    isAntiAlias = true
                                }
                                canvas.nativeCanvas.drawCircle(center.x, center.y, radius, saturationPaint)
                            }

                            // Draw border
                            drawCircle(
                                color = borderColor,
                                radius = radius,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Draw pointer indicator
                            pointerPosition?.let { position ->
                                // White outer ring
                                drawCircle(
                                    color = Color.White,
                                    radius = 12.dp.toPx(),
                                    center = position,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                // Colored inner circle
                                drawCircle(
                                    color = selectedColor,
                                    radius = 9.dp.toPx(),
                                    center = position
                                )
                                // Black shadow ring
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    radius = 9.dp.toPx(),
                                    center = position,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tap or drag to select a color",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper function to handle touch events
private fun handleTouch(
    offset: Offset,
    center: Offset,
    radius: Float,
    onPositionUpdate: (Offset) -> Unit,
    onColorSelected: (Color) -> Unit
) {
    if (radius <= 0) return

    val dx = offset.x - center.x
    val dy = offset.y - center.y
    val distance = sqrt(dx * dx + dy * dy)

    // Clamp position to circle bounds
    val constrainedPosition = if (distance > radius) {
        val angle = atan2(dy, dx)
        Offset(
            center.x + radius * cos(angle),
            center.y + radius * sin(angle)
        )
    } else {
        offset
    }

    onPositionUpdate(constrainedPosition)
    val color = getColorFromPosition(constrainedPosition, center, radius)
    onColorSelected(color)
}

// Helper function to get color from position
private fun getColorFromPosition(position: Offset, center: Offset, radius: Float): Color {
    if (radius <= 0) return Color.Red

    val dx = position.x - center.x
    val dy = position.y - center.y
    val distance = sqrt(dx * dx + dy * dy)

    // Calculate hue from angle
    var angle = atan2(dy, dx) * (180f / PI.toFloat())
    if (angle < 0) angle += 360f

    // Calculate saturation from distance (clamped to valid range)
    val saturation = (distance / radius).coerceIn(0f, 1f)

    // Convert HSV to RGB
    val hsv = floatArrayOf(angle, saturation, 1f)
    val argb = AndroidColor.HSVToColor(hsv)

    return Color(argb)
}



// Example usage
@Preview(showBackground = true)
@Composable
fun MyColorPickerScreen() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var selectedColor by remember { mutableStateOf(Color.White) }
            RoundColorPicker(
                selectedColor = selectedColor,
                modifier = Modifier.padding(16.dp),
                onColorSelected = { color ->
                    selectedColor = color
                    println("Selected color: $color")
                },
                onDismissRequest = {}
            )
        }
    }
}



