package com.example.data.central.source.local.dao

import com.example.data.central.source.remote.model.CentralGattModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

class CentralLocalDataSource @Inject constructor(private val centralDao: CentralDao) {


    suspend fun getEvents(): Flow<List<CentralGattModel>> {
        return centralDao.getEvents().filter { it.isNotEmpty() }
    }

    suspend fun insertEvent(event: CentralGattModel) {
        centralDao.insertEvent(event)
    }

    suspend fun deleteAllEvents() {
        centralDao.deleteAllEvents()
    }
}