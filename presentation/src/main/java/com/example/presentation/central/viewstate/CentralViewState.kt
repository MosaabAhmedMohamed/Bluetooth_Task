package com.example.presentation.central.viewstate

import com.example.core.ble.BLELifecycleState

data class CentralViewState(
    val logs: MutableList<String> = mutableListOf(),
    val state: BLELifecycleState = BLELifecycleState.Disconnected,
    val read: String = "",
    val indicate: String = "",
    val userWantsToScanAndConnect: Boolean = false
)