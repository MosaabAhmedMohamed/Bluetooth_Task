package com.example.presentation.peripheral.viewstate



sealed class PeripheralSideEffect {
    object Initial : PeripheralSideEffect()
    object OnPermissionGranted : PeripheralSideEffect()
    object OnDisconnected : PeripheralSideEffect()
    data class OnStartAdvAdvertisingClicked(val state: Boolean) : PeripheralSideEffect()
}

