package com.example.data.central.repository

import com.example.data.central.source.mapping.mapToDomain
import com.example.data.central.source.remote.CentralGattDataSource
import com.example.domain.central.repository.CentralRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CentralRepositoryImpl @Inject constructor(
    private val gattDataSource: CentralGattDataSource
) : CentralRepository {

    override suspend fun gattStateCallback() = gattDataSource.state()
        .map { it.mapToDomain() }

    override suspend fun gattCallback() = gattDataSource.gattCallback()

    override fun isGattNotInitialized() = gattDataSource.isGattNotInitialized()

    override fun disconnectGatt() {
        gattDataSource.disconnectGatt()
    }

    override fun closeGatt() {
        gattDataSource.closeGatt()
    }

    override fun setConnectedGattToNull() = gattDataSource.setConnectedGattToNull()

    override suspend fun onTapRead() = gattDataSource.onTapRead()

    override fun onTapWrite(message: ByteArray) = gattDataSource.onTapWrite(message)
}