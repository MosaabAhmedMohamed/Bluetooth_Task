package com.example.data.central.source.mapping

import com.example.data.central.source.remote.model.CentralGattModel
import com.example.domain.central.model.CentralGattDomainModel

fun CentralGattModel.mapToDomain() = CentralGattDomainModel(
    isRestartLifecycle,
    log,
    state,
    read,
    indicate,
)