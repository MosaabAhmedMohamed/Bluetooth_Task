package com.example.data.peripheral.source.remote.model

import android.bluetooth.BluetoothDevice

data class PeripheralGattModel(
    val connectionState: Int = 0,
    val log: String = "",
    val subscribedDevices: Set<BluetoothDevice> = emptySet(),
    val write: String = ""
)
