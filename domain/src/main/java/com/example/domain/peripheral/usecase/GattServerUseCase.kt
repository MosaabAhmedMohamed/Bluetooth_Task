package com.example.domain.peripheral.usecase

import com.example.domain.peripheral.repository.PeripheralRepository
import javax.inject.Inject

class GattServerUseCase@Inject constructor(private val peripheralRepository: PeripheralRepository) {

    suspend fun gattStateCallback() = peripheralRepository.gattStateCallback()

    suspend fun bleIndicate(text: String) = peripheralRepository.bleIndicate(text)

    suspend fun bleStartGattServer() = peripheralRepository.bleStartGattServer()

    suspend fun bleStopGattServer() = peripheralRepository.bleStopGattServer()

    fun setReadMessage(readMessage: String){
        peripheralRepository.setReadMessage(readMessage)
    }
}