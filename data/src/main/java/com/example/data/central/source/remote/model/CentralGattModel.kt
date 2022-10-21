package com.example.data.central.source.remote.model

import com.example.core.ble.BLELifecycleState

data class CentralGattModel(val isRestartLifecycle : Boolean = false,
                            val log : String = "",
                            val state : BLELifecycleState = BLELifecycleState.Disconnected,
                            val read : String = "",
                            val indicate : String = "",)

