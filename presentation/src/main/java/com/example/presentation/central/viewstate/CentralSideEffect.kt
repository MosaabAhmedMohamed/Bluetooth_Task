package com.example.presentation.central.viewstate

sealed class CentralSideEffect {
    object Initial : CentralSideEffect()
    object OnBleRestartLifecycle : CentralSideEffect()
    object OnPermissionGranted : CentralSideEffect()
    class BleOnOffState(val bleState: Int) : CentralSideEffect()
}

