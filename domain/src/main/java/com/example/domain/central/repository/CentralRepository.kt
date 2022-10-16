package com.example.domain.central.repository

import android.bluetooth.BluetoothGattCallback
import com.example.domain.central.model.CentralGattDomainModel
import kotlinx.coroutines.flow.Flow

interface CentralRepository {

    suspend fun gattStateCallback(): Flow<CentralGattDomainModel>

    suspend fun gattCallback(): BluetoothGattCallback?

    fun isGattNotInitialized(): Boolean

    fun disconnectGatt()

    fun closeGatt()

    fun setConnectedGattToNull()

    suspend fun onTapRead()

    fun onTapWrite(message: ByteArray)
}