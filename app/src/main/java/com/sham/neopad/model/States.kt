package com.sham.neopad.model



sealed class MainViewModelEvent {
    object Connect: MainViewModelEvent()
    object Disconnect : MainViewModelEvent()
    data class ViewLayout(val dto: LayoutViewerDTO) : MainViewModelEvent()
    data class ControllerCreator(val name: String,val type: ControllerType,val layoutId : String): MainViewModelEvent()
    data class OpenController(val dto : VirtualController) : MainViewModelEvent()
    data class DeleteController(val dto : VirtualController) : MainViewModelEvent()
    data class LayoutCreator(val dto : LayoutCreatorDTO) : MainViewModelEvent()
    data class LayoutEditor(val dto: LayoutCreatorDTO) : MainViewModelEvent()
    data class DeleteLayout(val l: ControllerLayoutData) : MainViewModelEvent()
    data class ChangeUsername(val u: String) : MainViewModelEvent()
    data class ChangeSettings(val s: SettingStore) : MainViewModelEvent()
    object TiltCalibration: MainViewModelEvent()
}

sealed class PairViewModelEvent {
    data class ConnectToService(val conn: DiscoveredConnection): PairViewModelEvent()
    data class CopyUrl(val conn: DiscoveredConnection): PairViewModelEvent()
    object CloseActivity : PairViewModelEvent()
}

enum class ControllerType { PS4, XBOX }

// Connection status sealed class
sealed class ConnectionStatus {
    object NotConnected: ConnectionStatus()
    object Disconnected : ConnectionStatus()
    object Connecting : ConnectionStatus()
    object Connected: ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()

    override fun toString(): String = when (this) {
        is NotConnected -> "Not Connected"
        is Disconnected -> "Disconnected"
        is Connecting -> "Connecting..."
        is Connected -> "Connected"
        is Error -> "Connection Failed"
    }

    fun trimmed() = when (this) {
        is NotConnected -> "Not Connected"
        is Disconnected -> "Disconnected"
        is Connecting -> "Connecting..."
        is Connected -> "Connected"
        is Error -> "Connection Failed"
    }
}

enum class ClosingStatus { NONE, HUB, SELF}
