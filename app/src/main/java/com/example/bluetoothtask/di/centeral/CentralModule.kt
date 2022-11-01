package com.example.bluetoothtask.di.centeral

import com.example.core.util.DispatcherProvider
import com.example.data.central.repository.CentralRepositoryImpl
import com.example.data.central.source.local.dao.CentralLocalDataSource
import com.example.data.central.source.remote.CentralGattDataSource
import com.example.data.db.BluetoothTaskDatabase
import com.example.domain.central.repository.CentralRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CentralModule {

    @Provides
    fun provideCentralLocalDataSource(
        db: BluetoothTaskDatabase
    ): CentralLocalDataSource =
        CentralLocalDataSource(db.centralDao())

    @Singleton
    @Provides
    fun provideCentralRepository(
        gattDataSource: CentralGattDataSource,
        centralLocalDataSource: CentralLocalDataSource,
        dispatcherProvider: DispatcherProvider
    ): CentralRepository =
        CentralRepositoryImpl(gattDataSource, centralLocalDataSource, dispatcherProvider)

}