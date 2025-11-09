package com.sham.neopad.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sham.neopad.model.ButtonType
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.model.SettingStore
import com.sham.neopad.model.TiltIndicatorConfig
import com.sham.neopad.model.TiltState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class LayoutViewerModel: ViewModel() {
    var isPortrait by mutableStateOf(true)
    var showInfo by mutableStateOf(false)
    lateinit var layoutData :ControllerLayoutData
    var isReady by mutableStateOf(false)

    lateinit var onClose: () -> Unit

    fun closeApp() {
        if (::onClose.isInitialized)
            onClose.invoke()
    }


    var tiltSettings by mutableStateOf(SettingStore())

    val tiltConfig by derivedStateOf {
        layoutData.let {
            TiltIndicatorConfig(
                showLeft = it.specialButtons.left != ButtonType.None,
                showRight = it.specialButtons.right != ButtonType.None,
                showUp  = it.specialButtons.up != ButtonType.None,
                showDown = it.specialButtons.down != ButtonType.None,
            )
        }
    }

    var tiltState by mutableStateOf(TiltState())

    fun updateTilt(a: Float, p: Float, r: Float) {
        val adjustedAzimuth = a - tiltSettings.neutralAzimuth
        val adjustedPitch = p - tiltSettings.neutralPitch
        val adjustedRoll = r - tiltSettings.neutralRoll
        tiltState = TiltState(a = adjustedAzimuth,p = adjustedPitch,r = adjustedRoll)
    }


    // Optional: Reset tilt to center
    fun resetTilt() {
        tiltState =  TiltState(0f, 0f)
    }
}