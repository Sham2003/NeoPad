package com.sham.neopad.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.sham.neopad.model.ButtonType
import kotlin.math.min


@Composable
fun ComposeControllerButton(
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.None,
    mainColor: Color = Color.Red,
    squareMode: Boolean = false,
    onButtonUp: () -> Unit = {},
    onButtonDown: () -> Unit = {},
    disabled: Boolean = false
) {
    var isPressedState by remember { mutableStateOf(false) }

    val borderColor = remember(mainColor) {
        Color(ColorUtils.blendARGB(mainColor.toArgb(), android.graphics.Color.BLACK, 0.3f))
    }

    val textColor = remember(mainColor) {
        val luminance = ColorUtils.calculateLuminance(mainColor.toArgb())
        if (luminance < 0.5) Color.White else Color.Black
    }
    
    val textMeasurer = rememberTextMeasurer()

    val drawable = if (buttonType.isDrawable && buttonType.drawable != 0) {
        painterResource(id = buttonType.drawable)
    } else null

    var finalModifier = modifier
    if (!disabled) {
        finalModifier =  modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onButtonDown()
                        isPressedState = true

                        // Wait for release
                        tryAwaitRelease()

                        onButtonUp()
                        isPressedState = false
                    },
                    onTap = {}
                )
            }
    }

    Canvas(
        modifier = finalModifier
    ) {
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height
        val minDimension = min(canvasWidth, canvasHeight)
        val scale = minDimension / 50f
        val textSizeSp = (25f * scale * 0.4f).sp
        val borderWidthPx = 2f * scale
        val halfBorder = borderWidthPx / 2f

        val contentLeft = halfBorder
        val contentTop = halfBorder
        val contentWidth = canvasWidth - borderWidthPx
        val contentHeight = canvasHeight - borderWidthPx

        val useCircle = !squareMode
        if (isPressedState) {
            val glowColor = mainColor.copy(alpha = 0.6f)
            if (useCircle) {
                if (canvasWidth == canvasHeight) {
                    val radius = canvasWidth / 2f - halfBorder
                    val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

                    // Multiple glow layers for neon effect
                    drawCircle(
                        color = glowColor,
                        radius = radius + 8f,
                        center = center,
                        blendMode = BlendMode.Screen
                    )
                    drawCircle(
                        color = glowColor,
                        radius = radius + 16f,
                        center = center,
                        blendMode = BlendMode.Screen
                    )
                } else {
                    drawOval(
                        color = glowColor,
                        topLeft = Offset(contentLeft - 8f, contentTop - 8f),
                        size = Size(contentWidth + 16f, contentHeight + 16f),
                        blendMode = BlendMode.Screen
                    )
                    drawOval(
                        color = glowColor,
                        topLeft = Offset(contentLeft - 16f, contentTop - 16f),
                        size = Size(contentWidth + 32f, contentHeight + 32f),
                        blendMode = BlendMode.Screen
                    )
                }
            } else {
                val cornerRadius = min(canvasWidth, canvasHeight) * 0.1f
                drawRoundRect(
                    color = glowColor,
                    topLeft = Offset(contentLeft - 8f, contentTop - 8f),
                    size = Size(contentWidth + 16f, contentHeight + 16f),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    blendMode = BlendMode.Screen
                )
                drawRoundRect(
                    color = glowColor,
                    topLeft = Offset(contentLeft - 16f, contentTop - 16f),
                    size = Size(contentWidth + 32f, contentHeight + 32f),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    blendMode = BlendMode.Screen
                )
            }
        }

        // Draw background
        if (useCircle) {
            if (canvasWidth == canvasHeight) {
                val radius = canvasWidth / 2f - halfBorder
                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

                // Background
                drawCircle(
                    color = mainColor,
                    radius = radius,
                    center = center
                )

                // Gloss effect when pressed
                if (isPressedState) {
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.2f),
                        radius = radius,
                        center = center
                    )
                }

                // Border
                drawCircle(
                    color = borderColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = borderWidthPx)
                )
            } else {
                // Oval
                drawOval(
                    color = mainColor,
                    topLeft = Offset(contentLeft, contentTop),
                    size = Size(contentWidth, contentHeight)
                )

                if (isPressedState) {
                    drawOval(
                        color = Color.Black.copy(alpha = 0.2f),
                        topLeft = Offset(contentLeft, contentTop),
                        size = Size(contentWidth, contentHeight)
                    )
                }

                drawOval(
                    color = borderColor,
                    topLeft = Offset(contentLeft, contentTop),
                    size = Size(contentWidth, contentHeight),
                    style = Stroke(width = borderWidthPx)
                )
            }
        } else {
            // Rounded rectangle
            val cornerRadius = min(canvasWidth, canvasHeight) * 0.1f

            drawRoundRect(
                color = mainColor,
                topLeft = Offset(contentLeft, contentTop),
                size = Size(contentWidth, contentHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )

            if (isPressedState) {
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.2f),
                    topLeft = Offset(contentLeft, contentTop),
                    size = Size(contentWidth, contentHeight),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }

            drawRoundRect(
                color = borderColor,
                topLeft = Offset(contentLeft, contentTop),
                size = Size(contentWidth, contentHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = borderWidthPx)
            )
        }

        // Draw content (drawable or text)
        if (buttonType.isDrawable && drawable != null) {
            val iconSize = min(canvasWidth, canvasHeight) * 0.6f
            val iconLeft = (canvasWidth - iconSize) / 2
            val iconTop = (canvasHeight - iconSize) / 2

            translate(iconLeft, iconTop) {
                with(drawable) {
                    draw(Size(iconSize, iconSize))
                }
            }
        } else if (buttonType.label.isNotEmpty()) {
            val textLayoutResult = textMeasurer.measure(
                text = buttonType.label,
                style = TextStyle(
                    color = textColor,
                    fontSize = textSizeSp
                )
            )

            val textX = (canvasWidth - textLayoutResult.size.width) / 2
            val textY = (canvasHeight - textLayoutResult.size.height) / 2

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(textX, textY)
            )
        }
    }
}
