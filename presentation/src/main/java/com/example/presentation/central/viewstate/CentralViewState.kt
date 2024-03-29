package com.example.presentation.central.viewstate

import com.example.core.ble.BLELifecycleState

data class CentralViewState(
    val logs: MutableList<String> = mutableListOf(),
    val state: BLELifecycleState = BLELifecycleState.Disconnected,
    val read: String = "",
    val indicate: String = "",
    val isUserWantsToScanAndConnect: Boolean = false,
    val isAskingForEnableBluetooth: Boolean = false,
    val isBackgroundServiceRunning: Boolean = false
)