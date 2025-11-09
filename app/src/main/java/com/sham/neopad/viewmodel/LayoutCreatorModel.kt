package com.sham.neopad.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.PathNode
import androidx.lifecycle.ViewModel
import com.sham.neopad.appLog
import com.sham.neopad.model.ComponentDimension
import com.sham.neopad.model.ComponentType
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.GamepadComponent
import com.sham.neopad.model.SpecialButtons
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CloseOptions {NIL , SAVE , EXIT}

class LayoutCreatorModel: ViewModel() {
    var isPortrait by mutableStateOf(true)
    var showExpandedControls by mutableStateOf(true)
    var showComponentCatalog by mutableStateOf(false)
    var showColorChooser by mutableStateOf(false)
    var showSpecialSettings by mutableStateOf(false)
    var showSaveDialog by mutableStateOf(false)

    var layoutId by mutableStateOf("")
    var layoutName by mutableStateOf("")
    var controllerType by mutableStateOf(ControllerType.PS4)
    var specialButtons by mutableStateOf(SpecialButtons())
    val components = mutableStateListOf<GamepadComponent>()

    fun getLayoutData(filename: String): ControllerLayoutData {
        return ControllerLayoutData(
            layoutId = layoutId,
            layoutName = layoutName,
            controllerType = controllerType,
            specialButtons =  specialButtons,
            components = components.toList(),
            isCustom = true,
            filename = filename
        )
    }

    var selectedComponent by mutableStateOf<GamepadComponent?>(null)

    private val _saveEvent = MutableStateFlow<CloseOptions>(CloseOptions.NIL)
    val emitter = _saveEvent.asStateFlow()

    fun changeSpecialSettings(special: SpecialButtons) {
        specialButtons = special
    }

    fun saveLayout() {
        _saveEvent.value = CloseOptions.SAVE
    }

    fun closeCreator() {
        _saveEvent.value = CloseOptions.EXIT
    }

    fun selectComponent(component: GamepadComponent?) {
        if (component == null) {
            selectedComponent = null
            return
        }
        if (!checkIfExists(component)) return
        selectedComponent = component
    }

    fun checkIfExists(component: GamepadComponent): Boolean {
        return components.indexOfFirst { it.creatorId == component.creatorId } != -1
    }

    fun addGamepadComponent(type: ComponentType) {
        components.add(GamepadComponent(
            type = type,
            x = 0.5f,
            y = 0.5f,
            color = Color.Red.toArgb(),
            dimension = ComponentDimension.SameSize(
                size = if (type.isBig()) 0.3f else 0.16f
            ),
            squareMode = false
        ))
    }


    var screenHeight by mutableIntStateOf(0)
    var screenWidth by mutableIntStateOf(0)

    fun updateComponentPosition(newX: Float, newY: Float) {
        if (selectedComponent == null) return

        val comp = selectedComponent!!
        val aspectRatio = screenHeight.toFloat() / screenWidth.toFloat()

        val halfWidthNormalized = when (comp.dimension) {
            is ComponentDimension.DoubleSize -> comp.dimension.width / 2f
            is ComponentDimension.SameSize -> {
                (comp.dimension.size * aspectRatio) / 2f
            }
        }

        val halfHeightNormalized = when (comp.dimension) {
            is ComponentDimension.DoubleSize -> comp.dimension.height / 2f
            is ComponentDimension.SameSize -> comp.dimension.size / 2f
        }

        // Constrain position so component stays fully on screen
        val constrainedX = newX.coerceIn(halfWidthNormalized, 1f - halfWidthNormalized)
        val constrainedY = newY.coerceIn(halfHeightNormalized, 1f - halfHeightNormalized)

        val selectedIndex = components.indexOf(comp)
        selectedComponent = comp.copy(
            x = constrainedX,
            y = constrainedY
        )
        if (selectedIndex != -1)
            components[selectedIndex] = selectedComponent!!
    }


    fun changeColor(color: Color) {
        if (selectedComponent == null) return
        val selectedIndex = components.indexOf(selectedComponent!!)
        selectedComponent = selectedComponent!!.copy(
            color = color.toArgb()
        )
        if (selectedIndex != -1)
            components[selectedIndex] = selectedComponent!!
    }

    fun deleteSelected() {
        if (selectedComponent == null) return
        val selectedIndex = components.indexOf(selectedComponent!!)
        if (selectedIndex != -1)
            components.removeAt(selectedIndex)

        selectedComponent = null
    }

    fun resetSelectedShape() {
        if (selectedComponent == null) return
        val selectedIndex = components.indexOf(selectedComponent!!)
        selectedComponent = selectedComponent!!.copy(
            dimension = ComponentDimension.SameSize(0.1f),
            squareMode = false,
            color = Color.Red.toArgb()
        )
        if (selectedIndex != -1)
            components[selectedIndex] = selectedComponent!!
    }

    fun changeDimension() {
        if (selectedComponent == null) return
        selectedComponent?.let { comp ->
            if ((comp.type is ComponentType.ControllerButton).not()) return
            val selectedIndex = components.indexOf(comp)
            val oldDimension = comp.dimension
            val newDimension = oldDimension.toggleDimension(screenWidth,screenHeight)
            val newComponent = comp.copy(
                dimension = newDimension
            )
            if (selectedIndex != -1) {
                components[selectedIndex] = newComponent
                selectedComponent = newComponent
            }
        }
    }

    fun changeFirstDimension(sw: Float) {
        if (selectedComponent == null) return
        val comp = selectedComponent!!
        val selectedIndex = components.indexOf(comp)

        val oldDimension = comp.dimension
        var newDimension: ComponentDimension = ComponentDimension.SameSize(sw)
        if (oldDimension is ComponentDimension.DoubleSize) {
            newDimension = ComponentDimension.DoubleSize(sw, oldDimension.height)
        }

        // Calculate aspect ratio for SameSize conversion
        val aspectRatio = screenHeight.toFloat() / screenWidth.toFloat()

        // Calculate new half-width in normalized coordinates
        val newHalfWidthNormalized = when (newDimension) {
            is ComponentDimension.DoubleSize -> newDimension.width / 2f
            is ComponentDimension.SameSize -> (newDimension.size * aspectRatio) / 2f
        }

        // Keep height calculation for completeness
        val halfHeightNormalized = when (newDimension) {
            is ComponentDimension.DoubleSize -> newDimension.height / 2f
            is ComponentDimension.SameSize -> newDimension.size / 2f
        }

        // Adjust position if new size would push component out of bounds
        var adjustedX = comp.x
        var adjustedY = comp.y

        // Check horizontal bounds
        if (adjustedX - newHalfWidthNormalized < 0f) {
            adjustedX = newHalfWidthNormalized // Push right
        } else if (adjustedX + newHalfWidthNormalized > 1f) {
            adjustedX = 1f - newHalfWidthNormalized // Push left
        }

        // Check vertical bounds (in case height also changed for SameSize)
        if (adjustedY - halfHeightNormalized < 0f) {
            adjustedY = halfHeightNormalized // Push down
        } else if (adjustedY + halfHeightNormalized > 1f) {
            adjustedY = 1f - halfHeightNormalized // Push up
        }


        selectedComponent = comp.copy(
            dimension = newDimension,
            x = adjustedX,
            y = adjustedY
        )
        if (selectedIndex != -1)
            components[selectedIndex] = selectedComponent!!
    }

    fun changeSecondDimension(height: Float) {
        if (selectedComponent == null) return
        val comp = selectedComponent!!
        if (comp.dimension is ComponentDimension.SameSize) {
            return// Can't change height independently
        }

        val selectedIndex = components.indexOf(comp)
        val oldDimension = comp.dimension as ComponentDimension.DoubleSize
        val newDimension = ComponentDimension.DoubleSize(oldDimension.width, height)

        // Calculate new half-height in normalized coordinates
        val halfWidthNormalized = newDimension.width / 2f
        val newHalfHeightNormalized = newDimension.height / 2f

        // Adjust position if new size would push component out of bounds
        var adjustedX = comp.x
        var adjustedY = comp.y

        // Check horizontal bounds (shouldn't change, but check for safety)
        if (adjustedX - halfWidthNormalized < 0f) {
            adjustedX = halfWidthNormalized // Push right
        } else if (adjustedX + halfWidthNormalized > 1f) {
            adjustedX = 1f - halfWidthNormalized // Push left
        }

        // Check vertical bounds
        if (adjustedY - newHalfHeightNormalized < 0f) {
            adjustedY = newHalfHeightNormalized // Push down
        } else if (adjustedY + newHalfHeightNormalized > 1f) {
            adjustedY = 1f - newHalfHeightNormalized // Push up
        }

        selectedComponent = comp.copy(
            dimension = newDimension,
            x = adjustedX,
            y = adjustedY
        )
        if (selectedIndex != -1)
            components[selectedIndex] = selectedComponent!!
    }

    fun toggleShape() {
        if (selectedComponent == null) return
        val compType = selectedComponent!!.type
        if (compType.isSquareNotCompatible()) return
        val selectedIndex = components.indexOf(selectedComponent!!)
        val oldSquareMode = selectedComponent!!.squareMode
        selectedComponent = selectedComponent!!.copy(
            squareMode = !oldSquareMode
        )
        if (selectedIndex != -1)
            components[selectedIndex] = selectedComponent!!
    }

}

fun ComponentDimension.toggleDimension(screenWidth: Int, screenHeight: Int) : ComponentDimension {
    return when(this) {
        is ComponentDimension.DoubleSize -> {
            val newWidth = width * screenWidth
            val newHeight = height * screenHeight
            ComponentDimension.SameSize(size = if (newWidth > newHeight) height else width)
        }
        is ComponentDimension.SameSize -> {
            val newSize = size * minOf(screenWidth,screenHeight)
            ComponentDimension.DoubleSize(width = newSize/ screenWidth , height = newSize/screenHeight)
        }
    }
}

private fun ComponentType.isSquareNotCompatible(): Boolean {
    return when(this) {
        ComponentType.Dpad,
        ComponentType.LeftStick,
        ComponentType.RightStick -> true
        ComponentType.GamepadButtons,
        is ComponentType.ControllerButton -> false
    }
}

private fun ComponentType.isBig(): Boolean {
    return when(this) {
        is ComponentType.ControllerButton -> false
        ComponentType.Dpad,
        ComponentType.GamepadButtons,
        ComponentType.LeftStick,
        ComponentType.RightStick -> true
    }
}
