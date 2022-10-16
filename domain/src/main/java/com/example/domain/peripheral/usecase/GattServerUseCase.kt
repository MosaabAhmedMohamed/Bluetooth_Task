package com.example.domain.peripheral.usecase

import com.example.domain.peripheral.repository.PeripheralRepository
import javax.inject.Inject

class GattServerUseCase@Inject constructor(private val peripheralRepository: PeripheralRepository) {

    suspend fun gattStateCallback() = peripheralRepository.gattStateCallback()

    suspend fun gattServer() = peripheralRepository.gattServer()

    suspend fun charForIndicate() = peripheralRepository.charForIndicate()

    suspend fun bleStartGattServer() = peripheralRepository.bleStartGattServer()

    suspend fun bleStopGattServer() = peripheralRepository.bleStopGattServer()

    fun setReadMessage(readMessage: String){
        peripheralRepository.setReadMessage(readMessage)
    }
}