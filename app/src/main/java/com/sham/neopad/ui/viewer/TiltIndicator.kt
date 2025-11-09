package com.sham.neopad.ui.viewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.sham.neopad.model.SettingStore
import com.sham.neopad.model.TiltIndicatorConfig
import com.sham.neopad.model.TiltState

private const val MAX_TILT_ANGLE_DEGREES = 30f

private fun normalizeAngle(angle: Float): Float {
    val clampedAngle = angle.coerceIn(-MAX_TILT_ANGLE_DEGREES, MAX_TILT_ANGLE_DEGREES)
    return clampedAngle / MAX_TILT_ANGLE_DEGREES
}


@Composable
fun Modifier.tiltIndicator(
    tiltState: TiltState,
    config: TiltIndicatorConfig = TiltIndicatorConfig(),
    setting: SettingStore = SettingStore()
): Modifier {

    val normalizedX by remember(tiltState.r) {
        derivedStateOf { normalizeAngle(tiltState.r) }
    }
    val normalizedY by remember(tiltState.p) {
        derivedStateOf { normalizeAngle(tiltState.p) }
    }

    val animatedX = remember { Animatable(0f) }
    val animatedY = remember { Animatable(0f) }

    // Update animations when normalized X changes (from Roll)
    LaunchedEffect(normalizedX) {
        animatedX.animateTo(
            targetValue = normalizedX,
            animationSpec = tween(
                durationMillis = setting.animationDuration,
                easing = FastOutSlowInEasing
            )
        )
    }

    // Update animations when normalized Y changes (from Pitch)
    LaunchedEffect(normalizedY) {
        animatedY.animateTo(
            targetValue = normalizedY,
            animationSpec = tween(
                durationMillis = setting.animationDuration,
                easing = FastOutSlowInEasing
            )
        )
    }

    val glowState by remember {
        derivedStateOf {
            GlowState(
                leftIntensity = if (config.showLeft && animatedX.value < 0)
                    kotlin.math.abs(animatedX.value) else 0f,
                rightIntensity = if (config.showRight && animatedX.value > 0)
                    animatedX.value else 0f,
                upIntensity = if (config.showUp && animatedY.value < 0)
                    kotlin.math.abs(animatedY.value) else 0f,
                downIntensity = if (config.showDown && animatedY.value > 0)
                    animatedY.value else 0f,
                threshold = setting.buttonThreshold
            )
        }
    }

    return this.drawBehind {
        drawGlowIndicators(glowState, setting)
    }
}

// Immutable state for drawing (reduces allocations)
private data class GlowState(
    val leftIntensity: Float,
    val rightIntensity: Float,
    val upIntensity: Float,
    val downIntensity: Float,
    val threshold: Float
)

private fun DrawScope.drawGlowIndicators(
    glowState: GlowState,
    setting: SettingStore
) {
    val glowWidthPx = setting.glowWidth.dp.toPx()

    // Left edge
    if (glowState.leftIntensity > 0) {
        drawEdgeGlow(
            intensity = glowState.leftIntensity,
            isButton = glowState.leftIntensity >= glowState.threshold,
            glowWidthPx = glowWidthPx,
            edge = Edge.LEFT
        )
    }

    // Right edge
    if (glowState.rightIntensity > 0) {
        drawEdgeGlow(
            intensity = glowState.rightIntensity,
            isButton = glowState.rightIntensity >= glowState.threshold,
            glowWidthPx = glowWidthPx,
            edge = Edge.RIGHT
        )
    }

    // Top edge
    if (glowState.upIntensity > 0) {
        drawEdgeGlow(
            intensity = glowState.upIntensity,
            isButton = glowState.upIntensity >= glowState.threshold,
            glowWidthPx = glowWidthPx,
            edge = Edge.TOP
        )
    }

    // Bottom edge
    if (glowState.downIntensity > 0) {
        drawEdgeGlow(
            intensity = glowState.downIntensity,
            isButton = glowState.downIntensity >= glowState.threshold,
            glowWidthPx = glowWidthPx,
            edge = Edge.BOTTOM
        )
    }
}

private enum class Edge { LEFT, RIGHT, TOP, BOTTOM }

private fun DrawScope.drawEdgeGlow(
    intensity: Float,
    isButton: Boolean,
    glowWidthPx: Float,
    edge: Edge
) {
    val color = if (isButton) {
        Color(0, 255, 100, (intensity * 255).toInt())
    } else if (intensity < 0.3f) {
        Color(100, 200, 255, (intensity * 128).toInt())
    } else {
        Color(100, 200, 255, (intensity * 255).toInt())
    }

    val width = if (isButton) glowWidthPx * 1.5f else glowWidthPx

    when (edge) {
        Edge.LEFT -> {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(color, Color.Transparent),
                    startX = 0f,
                    endX = width * 3
                ),
                topLeft = Offset(0f, 0f),
                size = Size(width * 3, size.height)
            )
        }
        Edge.RIGHT -> {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, color),
                    startX = size.width - width * 3,
                    endX = size.width
                ),
                topLeft = Offset(size.width - width * 3, 0f),
                size = Size(width * 3, size.height)
            )
        }
        Edge.TOP -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color, Color.Transparent),
                    startY = 0f,
                    endY = width * 3
                ),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, width * 3)
            )
        }
        Edge.BOTTOM -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, color),
                    startY = size.height - width * 3,
                    endY = size.height
                ),
                topLeft = Offset(0f, size.height - width * 3),
                size = Size(size.width, width * 3)
            )
        }
    }
}



