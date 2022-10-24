package com.example.data.peripheral.source.mapping

import com.example.data.peripheral.source.remote.model.PeripheralGattModel
import com.example.domain.peripheral.model.PeripheralGattDomainModel


fun PeripheralGattModel.mapToDomain() = PeripheralGattDomainModel(
    connectionState,
    log,
    subscribedDevices,
    write
)
