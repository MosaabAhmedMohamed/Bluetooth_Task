package com.example.domain.central.model

import com.example.core.ble.BLELifecycleState

sealed class CentralGattDomainModel {

    object Initial : CentralGattDomainModel()
    object RestartLifecycle : CentralGattDomainModel()
    data class Log(val message: String) : CentralGattDomainModel()
    data class ConnectionLifeCycle(val state: BLELifecycleState) : CentralGattDomainModel()
    data class Read(val message: String?) : CentralGattDomainModel()
    data class Indicate(val message: String?) : CentralGattDomainModel()

}