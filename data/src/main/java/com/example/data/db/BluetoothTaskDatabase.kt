package com.example.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.central.source.local.dao.CentralDao
import com.example.data.central.source.remote.model.CentralGattModel


@Database(
    entities = [CentralGattModel::class],
    version = BluetoothTask_DATABASE_VERSION_NUMBER
)

abstract class BluetoothTaskDatabase : RoomDatabase() {

    abstract fun centralDao(): CentralDao

}


const val DB_NAME = "BluetoothTask.db"
const val BluetoothTask_DATABASE_VERSION_NUMBER = 2