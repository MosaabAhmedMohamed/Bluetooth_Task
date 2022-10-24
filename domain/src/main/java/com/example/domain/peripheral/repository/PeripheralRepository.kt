package com.example.domain.peripheral.repository


import com.example.domain.peripheral.model.PeripheralGattDomainModel
import kotlinx.coroutines.flow.Flow

interface PeripheralRepository {
    suspend fun gattStateCallback(): Flow<PeripheralGattDomainModel>

    suspend fun bleIndicate(text: String)

    suspend fun bleStartGattServer()

    suspend fun bleStopGattServer()

    fun setReadMessage(readMessage: String)
}