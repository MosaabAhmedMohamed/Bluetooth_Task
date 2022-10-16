package com.example.presentation.central.viewstate

import com.example.core.ble.BLELifecycleState


sealed class CentralViewState {

    object Initial : CentralViewState()
    data class Log(val message: String?) : CentralViewState()
    data class ConnectionLifeCycle(val state: BLELifecycleState) : CentralViewState()
    data class Read(val message: String?) : CentralViewState()
    data class Indicate(val message: String?) : CentralViewState()
}