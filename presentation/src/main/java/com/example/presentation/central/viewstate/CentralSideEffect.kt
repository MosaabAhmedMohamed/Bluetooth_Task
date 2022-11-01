package com.example.presentation.central.viewstate

sealed class CentralSideEffect {
    object Initial : CentralSideEffect()
    object NONE : CentralSideEffect()
    object OnBleRestartLifecycle : CentralSideEffect()
    object OnPermissionGranted : CentralSideEffect()
    class BleOnOffState(val bleState: Int) : CentralSideEffect()
    class BackgroundServiceState(val isAllowedToRun: Boolean) : CentralSideEffect()
}

