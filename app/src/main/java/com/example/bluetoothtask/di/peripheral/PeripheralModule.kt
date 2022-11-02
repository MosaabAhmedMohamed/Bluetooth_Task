package com.example.bluetoothtask.di.peripheral

import android.app.Application
import android.bluetooth.BluetoothManager
import com.example.core.util.DispatcherProvider
import com.example.data.peripheral.repository.PeripheralRepositoryImpl
import com.example.data.peripheral.source.remote.PeripheralGattDataSource
import com.example.domain.peripheral.repository.PeripheralRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class PeripheralModule {

    @Provides
    fun providePeripheralGattDataSource(
        context: Application,
        bluetoothManager: BluetoothManager,
        dispatcherProvider: DispatcherProvider
    ): PeripheralGattDataSource =
        PeripheralGattDataSource(
            context,
            bluetoothManager,
            dispatcherProvider
        )

    @Provides
    fun providePeripheralRepository(
        gattDataSource: PeripheralGattDataSource
    ): PeripheralRepository =
        PeripheralRepositoryImpl(gattDataSource)

}