package com.example.data.peripheral.source.mapping

import com.example.data.peripheral.source.remote.model.PeripheralGattModel
import com.example.domain.peripheral.model.PeripheralGattDomainModel


fun PeripheralGattModel.mapToDomain(): PeripheralGattDomainModel {
    return when (this) {
        is PeripheralGattModel.ConnectionState -> PeripheralGattDomainModel.ConnectionState(this.state)
        PeripheralGattModel.Initial -> PeripheralGattDomainModel.Initial
        is PeripheralGattModel.Log -> PeripheralGattDomainModel.Log(this.message)
        is PeripheralGattModel.OnSubscribersChanged -> PeripheralGattDomainModel.OnSubscribersChanged(
            this.subscribedDevices
        )
        is PeripheralGattModel.Write -> PeripheralGattDomainModel.Write(this.message)
    }
}