package com.example.domain.central.usecase

import com.example.domain.central.repository.CentralRepository
import javax.inject.Inject

class GattUseCase @Inject constructor(private val centralRepository: CentralRepository) {

    suspend fun gattStateCallback() = centralRepository.gattStateCallback()

    suspend fun gattCallback() = centralRepository.gattCallback()

    fun isGattNotInitialized() = centralRepository.isGattNotInitialized()

    fun disconnectGatt() {
        centralRepository.disconnectGatt()
    }

    fun closeGatt() {
        centralRepository.closeGatt()
    }

    fun setConnectedGattToNull() = centralRepository.setConnectedGattToNull()

    suspend fun onTapRead() = centralRepository.onTapRead()

    fun onTapWrite(message: ByteArray) = centralRepository.onTapWrite(message)
}