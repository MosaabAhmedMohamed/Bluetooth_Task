package com.example.presentation.central.viewstate

import com.example.core.ble.BLELifecycleState

sealed class CentralActionState {

    object Initial : CentralActionState()
    object OnBleRestartLifecycle : CentralActionState()
    object OnPermissionGranted : CentralActionState()
}

data class CentralDataState(
    val log: String = "",
    val state: BLELifecycleState = BLELifecycleState.Disconnected,
    val read: String = "",
    val indicate: String = "",
    val userWantsToScanAndConnect: Boolean = false,
    val actionState: CentralActionState
)