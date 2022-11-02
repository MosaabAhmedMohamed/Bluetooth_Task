package com.example.data.central.source.remote.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.ble.BLELifecycleState

@Keep
@Entity(tableName = "CentralGattModel")
data class CentralGattModel(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "isRestartLifecycle")
    val isRestartLifecycle: Boolean = false,

    @ColumnInfo(name = "log")
    val log: String = "",

    @ColumnInfo(name = "state")
    val state: BLELifecycleState = BLELifecycleState.Disconnected,

    @ColumnInfo(name = "read")
    val read: String = "",

    @ColumnInfo(name = "indicate")
    val indicate: String = "",
)

