package com.sham.neopad.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sham.neopad.layout.DefaultLayoutManager
import com.sham.neopad.model.ConnectionInfo
import com.sham.neopad.model.ConnectionStatus
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.VirtualController
import com.sham.neopad.model.LayoutCreatorDTO
import com.sham.neopad.model.LayoutViewerDTO
import com.sham.neopad.model.MainViewModelEvent
import com.sham.neopad.model.MainViewModelEvent.TiltCalibration
import com.sham.neopad.model.SettingStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    var isPortrait by mutableStateOf(true)


    private var _events = MutableSharedFlow<MainViewModelEvent>(extraBufferCapacity = 20)
    val events = _events.asSharedFlow()

    var connectionStatus: ConnectionStatus by mutableStateOf(ConnectionStatus.NotConnected)
    var connectionInfo: ConnectionInfo by mutableStateOf(ConnectionInfo())

    val connected : Boolean
        get() = connectionStatus == ConnectionStatus.Connected

    fun emitEvent(et : MainViewModelEvent) {
        viewModelScope.launch {
            _events.emit(et)
        }
    }

    fun openEditMode(l: ControllerLayoutData) {
        val dto = LayoutCreatorDTO(
            layoutName = l.layoutName,
            layoutType = l.controllerType,
            isDefault = false,
            selectedLayout = l.filename,
            isEditMode = true
        )
        emitEvent(MainViewModelEvent.LayoutEditor(dto))
    }

    fun openViewer(l: ControllerLayoutData) {
        val dto = LayoutViewerDTO(
            isDefault = !l.isCustom,
            layoutId = l.layoutId,
            filename = l.filename
        )
        emitEvent(MainViewModelEvent.ViewLayout(dto))
    }

    val controllers = mutableStateListOf<VirtualController>()



    fun createController(name: String,type: ControllerType, layout: ControllerLayoutData) {
        emitEvent(MainViewModelEvent.ControllerCreator(name,type,layout.layoutId))
    }

    fun openController(controller: VirtualController) {
        emitEvent(MainViewModelEvent.OpenController(controller))
    }

    fun deleteController(controller: VirtualController) {
        emitEvent(MainViewModelEvent.DeleteController(controller))
    }

    fun createLayout(name: String, type : ControllerType, layout : ControllerLayoutData) {
        val dto = LayoutCreatorDTO(
            layoutName = name,
            layoutType = type,
            isDefault = !layout.isCustom,
            selectedLayout = if (layout.isCustom) layout.filename else layout.layoutId,
            isEditMode = false
        )
        emitEvent(MainViewModelEvent.LayoutCreator(dto))
    }

    // Dialogs
    var showControllerCreator by mutableStateOf(false)
    var showLayoutCreator by mutableStateOf(false)
    var showTiltCalibrationDialog by mutableStateOf(false)

    fun showTiltCalibrator() {
        showTiltCalibrationDialog = true
        emitEvent(TiltCalibration)
    }

    fun closeTiltCalibrator() {
        showTiltCalibrationDialog = false
        emitEvent(TiltCalibration)
    }
    //Layouts
    val myLayouts = mutableStateListOf<ControllerLayoutData>()

    fun clearLayouts() {
        myLayouts.removeAll { true }
    }
    val ps4Layouts : List<ControllerLayoutData>
        get() = myLayouts.filter { it.controllerType == ControllerType.PS4 }

    val xboxLayouts : List<ControllerLayoutData>
        get() = myLayouts.filter { it.controllerType == ControllerType.XBOX }


    var username  by mutableStateOf("Unknown")
    var settingsData by mutableStateOf(SettingStore())
    private var saveJob: Job? = null


    fun changeHapticFeedback(v: Boolean) {
        settingsData = settingsData.copy(hapticFeedback = v)
        debounceSave()
    }

    fun changeSoftTrigger(v: Boolean) {
        settingsData = settingsData.copy(softTrigger = v)
        debounceSave()
    }

    fun changeAnimationDuration(v: Float) {
        settingsData = settingsData.copy(animationDuration = v.toInt())
        debounceSave()
    }



    fun changeGlowWidth(v: Float) {
        settingsData = settingsData.copy(glowWidth = v)
        debounceSave()
    }

    fun changeButtonThreshold(v: Float) {
        settingsData = settingsData.copy(buttonThreshold = v)
        debounceSave()
    }

    private fun debounceSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(1000)
            emitEvent(MainViewModelEvent.ChangeSettings(settingsData))
        }
    }

    fun changeNeutralTilt(pitch: Float, roll: Float) {
        settingsData = settingsData.copy(neutralPitch = pitch, neutralRoll = roll)
        debounceSave()
    }


    companion object {
        val EMPTY = MainViewModel().apply {
            controllers.add(VirtualController())
            controllers.add(VirtualController())
            controllers.add(VirtualController())
            controllers.add(VirtualController())
            controllers.add(VirtualController())

            myLayouts.addAll(DefaultLayoutManager.getDummyLayout())
        }
    }
}

