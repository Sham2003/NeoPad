package com.sham.neopad.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sham.neopad.model.ButtonType
import com.sham.neopad.model.ControllerDTO
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.SettingStore
import com.sham.neopad.model.TiltIndicatorConfig
import com.sham.neopad.model.TiltState
import com.sham.neopad.model.VirtualController
import com.sham.neopad.service.GamepadInput
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class GamepadViewModel: ViewModel() {
    var isPortrait by mutableStateOf(true)
    var showInfo by mutableStateOf(false)
    var changeGpad by mutableStateOf(false)

    val myLayouts = mutableStateListOf<ControllerLayoutData>()
    val controllers = mutableStateListOf<VirtualController>()

    lateinit var onClose: () -> Unit
    lateinit var onChange: () -> Unit
    fun closeApp() {
        if (::onClose.isInitialized)
            onClose.invoke()
    }
    fun changeController() {
        if (::onChange.isInitialized)
            onChange.invoke()
    }

    var controllerDTO by mutableStateOf<ControllerDTO?>(null)

    var tiltSettings by mutableStateOf(SettingStore())

    val layoutData by derivedStateOf {
        controllerDTO?.let { dto ->
            myLayouts.first { it.layoutId == dto.layoutId }
        }
    }

    val tiltConfig by derivedStateOf {
        layoutData?.let {
            TiltIndicatorConfig(
                showLeft = it.specialButtons.left != ButtonType.None,
                showRight = it.specialButtons.right != ButtonType.None,
                showUp  = it.specialButtons.up != ButtonType.None,
                showDown = it.specialButtons.down != ButtonType.None,
            )
        } ?: TiltIndicatorConfig()
    }

    var gamepad by mutableStateOf(GamepadInput(0,"", ControllerType.PS4))

    var tiltState by mutableStateOf(TiltState())


    fun updateTilt(a: Float, p: Float, r: Float) {
        val adjustedAzimuth = a - tiltSettings.neutralAzimuth
        val adjustedPitch = p - tiltSettings.neutralPitch
        val adjustedRoll = r - tiltSettings.neutralRoll
        setSpecialButtons(adjustedPitch,adjustedRoll)
        tiltState = TiltState(a = adjustedAzimuth,p = adjustedPitch,r = adjustedRoll)
    }

    private var isLeftPressed: Boolean = false
    private var isRightPressed: Boolean = false
    private var isUpPressed: Boolean = false
    private var isDownPressed: Boolean = false


    private val MAX_TILT_ANGLE_DEGREES = 30f

    /**
     * Converts a raw degree value into a normalized intensity between -1.0f and 1.0f.
     */
    private fun normalizeAngle(angle: Float): Float {
        val clampedAngle = angle.coerceIn(-MAX_TILT_ANGLE_DEGREES, MAX_TILT_ANGLE_DEGREES)
        return clampedAngle / MAX_TILT_ANGLE_DEGREES
    }


    fun setSpecialButtons(p: Float, r: Float) {
        val buttons = layoutData?.specialButtons
        if (buttons == null) {
            // Cannot process tilt without a layout. Clear all states for safety.
            isLeftPressed = false; isRightPressed = false; isUpPressed = false; isDownPressed = false
            return
        }

        val normalizedX = normalizeAngle(p) // Roll -> Horizontal (-1: Left, +1: Right)
        val normalizedY = normalizeAngle(r)
        val threshold = tiltSettings.buttonThreshold


        val shouldPressRight = normalizedX >= threshold
        if (shouldPressRight != isRightPressed) {
            if (buttons.right != ButtonType.None) {
                gamepad.setButton(buttons.right, shouldPressRight) // Send press/release command
            }
            isRightPressed = shouldPressRight // Update internal state
        }

        // B. LEFT Tilt (Roll < 0)
        val shouldPressLeft = normalizedX <= -threshold
        if (shouldPressLeft != isLeftPressed) {
            if (buttons.left != ButtonType.None) {
                gamepad.setButton(buttons.left, shouldPressLeft)
            }
            isLeftPressed = shouldPressLeft
        }

        // C. DOWN Tilt (Pitch > 0)
        val shouldPressDown = normalizedY >= threshold
        if (shouldPressDown != isDownPressed) {
            if (buttons.down != ButtonType.None) {
                gamepad.setButton(buttons.down, shouldPressDown)
            }
            isDownPressed = shouldPressDown
        }

        // D. UP Tilt (Pitch < 0)
        val shouldPressUp = normalizedY <= -threshold
        if (shouldPressUp != isUpPressed) {
            if (buttons.up != ButtonType.None) {
                gamepad.setButton(buttons.up, shouldPressUp)
            }
            isUpPressed = shouldPressUp
        }
    }

    fun resetTilt() {
        tiltState =  TiltState(0f, 0f)
    }
}