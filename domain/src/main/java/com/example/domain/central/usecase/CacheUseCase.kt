package com.example.domain.central.usecase

import com.example.domain.central.repository.CentralRepository
import javax.inject.Inject

class CacheUseCase @Inject constructor(private val centralRepository: CentralRepository) {

    suspend fun clearCache() = centralRepository.clearCache()

}