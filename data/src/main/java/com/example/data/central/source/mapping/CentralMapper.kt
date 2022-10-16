package com.example.data.central.source.mapping

import com.example.data.central.source.remote.model.CentralGattModel
import com.example.domain.central.model.CentralGattDomainModel

fun CentralGattModel.mapToDomain(): CentralGattDomainModel {
    return when (this) {
        is CentralGattModel.ConnectionLifeCycle -> CentralGattDomainModel.ConnectionLifeCycle(this.state)
        is CentralGattModel.Indicate -> CentralGattDomainModel.Indicate(this.message)
        CentralGattModel.Initial -> CentralGattDomainModel.Initial
        is CentralGattModel.Log -> CentralGattDomainModel.Log(this.message)
        is CentralGattModel.Read -> CentralGattDomainModel.Read(this.message)
        CentralGattModel.RestartLifecycle -> CentralGattDomainModel.RestartLifecycle
    }
}