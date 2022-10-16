package com.example.domain.peripheral.model

import android.bluetooth.BluetoothDevice

sealed class PeripheralGattDomainModel {

    object Initial : PeripheralGattDomainModel()
    data class ConnectionState(val state: Int) : PeripheralGattDomainModel()
    data class Log(val message: String) : PeripheralGattDomainModel()
    data class OnSubscribersChanged(val subscribedDevices: Set<BluetoothDevice>) : PeripheralGattDomainModel()
    data class Write(val message: String?) : PeripheralGattDomainModel()
}