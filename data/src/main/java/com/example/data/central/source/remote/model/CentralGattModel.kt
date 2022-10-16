package com.example.data.central.source.remote.model

import com.example.core.ble.BLELifecycleState

sealed class CentralGattModel {

    object Initial : CentralGattModel()
    object RestartLifecycle : CentralGattModel()
    data class Log(val message: String) : CentralGattModel()
    data class ConnectionLifeCycle(val state: BLELifecycleState) : CentralGattModel()
    data class Read(val message: String?) : CentralGattModel()
    data class Indicate(val message: String?) : CentralGattModel()

}
