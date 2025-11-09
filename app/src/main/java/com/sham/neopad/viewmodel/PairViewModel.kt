package com.sham.neopad.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sham.neopad.model.ConnectionInfo
import com.sham.neopad.model.ConnectionStatus
import com.sham.neopad.model.DiscoveredConnection
import com.sham.neopad.model.PairViewModelEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PairViewModel: ViewModel() {
    var isPortrait by mutableStateOf(true)
    private var _events = MutableSharedFlow<PairViewModelEvent>(extraBufferCapacity = 20)
    val events = _events.asSharedFlow()

    fun emit(et: PairViewModelEvent) = viewModelScope.launch { _events.emit(et) }

    var connectionStatus: ConnectionStatus by mutableStateOf(ConnectionStatus.NotConnected)
    var connectionInfo : ConnectionInfo by mutableStateOf(ConnectionInfo())
    var availableConnections: List<DiscoveredConnection> by mutableStateOf(emptyList())


}