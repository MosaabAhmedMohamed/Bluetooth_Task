package com.example.bluetoothtask.di.centeral

import com.example.data.central.repository.CentralRepositoryImpl
import com.example.data.central.source.remote.CentralGattDataSource
import com.example.domain.central.repository.CentralRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class CentralModule {


    @Provides
    fun provideCentralRepository(
        gattDataSource: CentralGattDataSource
    ): CentralRepository =
        CentralRepositoryImpl(gattDataSource)

}