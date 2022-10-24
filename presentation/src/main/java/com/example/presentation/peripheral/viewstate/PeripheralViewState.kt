package com.example.presentation.peripheral.viewstate

data class PeripheralViewState(
    val connectionState: Int = -1,
    val logs: MutableList<String> = mutableListOf(),
    val write: String = "",
    val subscribers: String = "0",
    val isAdvertising: Boolean = false,
    val isUserWantsToStartAdvertising: Boolean = false,
    val isAskingForEnableBluetooth: Boolean = false
)