package com.example.presentation.peripheral.viewstate

sealed class PeripheralActionState {
    object Initial : PeripheralActionState()
    object OnPermissionGranted : PeripheralActionState()
    data class OnStartAdvAdvertisingClicked(val state: Boolean) : PeripheralActionState()
}

data class PeripheralDataState(
    val connectionState: Int = -1,
    val log: String = "",
    val write: String = "",
    val subscribers: String = "0",
    val isAdvertising: Boolean = false,
    val actionState: PeripheralActionState
)