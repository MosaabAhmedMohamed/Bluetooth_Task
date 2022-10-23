package com.example.presentation.peripheral.viewstate



sealed class PeripheralSideEffect {
    object Initial : PeripheralSideEffect()
    object NON : PeripheralSideEffect()
    object OnDisconnected : PeripheralSideEffect()
    object OnStartAdvertisingClicked : PeripheralSideEffect()
}

