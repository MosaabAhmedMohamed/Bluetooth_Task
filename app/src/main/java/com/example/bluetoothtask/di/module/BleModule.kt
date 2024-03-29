package com.example.bluetoothtask.di.module

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class BleModule {

    @Provides
    fun provideBluetoothManager(
        context: Application
    ): BluetoothManager = context
        .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    @Provides
    fun provideBluetoothAdapter(
        bluetoothManager: BluetoothManager
    ): BluetoothAdapter = bluetoothManager.adapter
}