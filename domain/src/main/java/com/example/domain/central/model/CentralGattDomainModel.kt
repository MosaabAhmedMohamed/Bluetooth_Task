package com.example.domain.central.model

import com.example.core.ble.BLELifecycleState

data class CentralGattDomainModel(val isRestartLifecycle : Boolean = false,
                            val log : String = "",
                            val state : BLELifecycleState = BLELifecycleState.Disconnected,
                            val read : String = "",
                            val indicate : String = "",)