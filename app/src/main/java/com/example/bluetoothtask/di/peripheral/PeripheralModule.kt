package com.example.bluetoothtask.di.peripheral

import android.app.Application
import android.bluetooth.BluetoothManager
import com.example.data.peripheral.repository.PeripheralRepositoryImpl
import com.example.data.peripheral.source.remote.PeripheralGattDataSource
import com.example.domain.peripheral.repository.PeripheralRepository
import dagger.Module
import dagger.Provides

@Module
class PeripheralModule {

    @Provides
    fun providePeripheralGattDataSource(
        context: Application,
        bluetoothManager: BluetoothManager
    ): PeripheralGattDataSource =
        PeripheralGattDataSource(
            context,
            bluetoothManager
        )

    @Provides
    fun providePeripheralRepository(
        gattDataSource: PeripheralGattDataSource
    ): PeripheralRepository =
        PeripheralRepositoryImpl(gattDataSource)

}