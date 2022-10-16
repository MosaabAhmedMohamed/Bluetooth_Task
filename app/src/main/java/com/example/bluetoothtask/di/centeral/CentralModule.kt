package com.example.bluetoothtask.di.centeral

import com.example.data.central.repository.CentralRepositoryImpl
import com.example.data.central.source.remote.CentralGattDataSource
import com.example.domain.central.repository.CentralRepository
import dagger.Module
import dagger.Provides

@Module
class CentralModule {


    @Provides
    fun provideCentralRepository(
        gattDataSource: CentralGattDataSource
    ): CentralRepository =
        CentralRepositoryImpl(gattDataSource)

}