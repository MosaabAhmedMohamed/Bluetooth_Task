package com.example.data.peripheral.source.remote.model

import android.bluetooth.BluetoothDevice

sealed class PeripheralGattModel {

    object Initial : PeripheralGattModel()
    data class ConnectionState(val state: Int) : PeripheralGattModel()
    data class Log(val message: String) : PeripheralGattModel()
    data class OnSubscribersChanged(val subscribedDevices: Set<BluetoothDevice>) : PeripheralGattModel()
    data class Write(val message: String?) : PeripheralGattModel()

}