package com.example.domain.peripheral.model

import android.bluetooth.BluetoothDevice

data class PeripheralGattDomainModel(
    val connectionState: Int = 0,
    val log: String = "",
    val subscribedDevices: Set<BluetoothDevice> = emptySet(),
    val write: String = ""
)
