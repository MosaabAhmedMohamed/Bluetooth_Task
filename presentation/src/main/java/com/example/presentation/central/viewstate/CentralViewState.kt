package com.example.presentation.central.viewstate

import com.example.core.ble.BLELifecycleState


sealed class CentralViewState {

    object Initial : CentralViewState()
    object OnBleRestartLifecycle : CentralViewState()
    object OnPermissionGranted : CentralViewState()
    data class Log(val message: String?) : CentralViewState()
    data class ConnectionLifeCycle(val state: BLELifecycleState) : CentralViewState()
    data class Read(val message: String?) : CentralViewState()
    data class Indicate(val message: String?) : CentralViewState()
    data class UserWantsToScanAndConnect(val state: Boolean) : CentralViewState()
}