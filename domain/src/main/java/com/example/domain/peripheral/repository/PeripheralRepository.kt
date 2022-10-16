package com.example.domain.peripheral.repository

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import com.example.domain.peripheral.model.PeripheralGattDomainModel
import kotlinx.coroutines.flow.Flow

interface PeripheralRepository {
    suspend fun gattStateCallback(): Flow<PeripheralGattDomainModel>

    suspend fun gattServer(): BluetoothGattServer?

    suspend fun charForIndicate(): BluetoothGattCharacteristic?

    suspend fun bleStartGattServer()

    suspend fun bleStopGattServer()

    fun setReadMessage(readMessage: String)
}