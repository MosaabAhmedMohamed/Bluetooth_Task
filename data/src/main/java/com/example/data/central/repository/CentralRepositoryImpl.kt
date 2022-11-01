package com.example.data.central.repository

import com.example.core.util.DispatcherProvider
import com.example.data.central.source.local.dao.CentralLocalDataSource
import com.example.data.central.source.mapping.mapToDomain
import com.example.data.central.source.remote.CentralGattDataSource
import com.example.domain.central.repository.CentralRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class CentralRepositoryImpl @Inject constructor(
    private val gattDataSource: CentralGattDataSource,
    private val centralLocalDataSource: CentralLocalDataSource,
    private val dispatchers: DispatcherProvider
) : CentralRepository {

    private val scope = CoroutineScope(dispatchers.io + SupervisorJob())

    override suspend fun gattStateCallback() = centralLocalDataSource.getEvents()
        .map { it.map { it.mapToDomain() } }

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

    override suspend fun clearCache() = centralLocalDataSource.deleteAllEvents()

    override fun onTapWrite(message: ByteArray) = gattDataSource.onTapWrite(message)

    init {
        scope.launch {
            gattDataSource.state()
                .onEach {
                    centralLocalDataSource.insertEvent(it)
                }
                .collect()
        }

    }
}