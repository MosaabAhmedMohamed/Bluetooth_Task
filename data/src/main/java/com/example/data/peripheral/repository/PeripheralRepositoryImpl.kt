package com.example.data.peripheral.repository


import com.example.data.peripheral.source.mapping.mapToDomain
import com.example.data.peripheral.source.remote.PeripheralGattDataSource
import com.example.domain.peripheral.repository.PeripheralRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PeripheralRepositoryImpl @Inject constructor(
    private val gattDataSource: PeripheralGattDataSource
) : PeripheralRepository {

    override suspend fun gattStateCallback() = gattDataSource.state()
        .map {
            it.mapToDomain()
        }

    override suspend fun bleIndicate(text: String) = gattDataSource.bleIndicate(text)

    override suspend fun bleStartGattServer() = gattDataSource.bleStartGattServer()

    override suspend fun bleStopGattServer() = gattDataSource.bleStopGattServer()

    override fun setReadMessage(readMessage: String) {
        gattDataSource.setReadMsg(readMessage)
    }
}