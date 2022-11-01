package com.example.data.central.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.central.source.remote.model.CentralGattModel
import kotlinx.coroutines.flow.Flow


@Dao
interface CentralDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(files: CentralGattModel)

    @Query("select * from CentralGattModel")
    fun getEvents(): Flow<List<CentralGattModel>>

    @Query("delete from CentralGattModel")
    suspend fun deleteAllEvents()

}