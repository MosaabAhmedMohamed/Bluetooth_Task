package com.example.bluetoothtask.di.module

import com.example.core.util.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DispatcherProviderModule {

    @Provides
    @Singleton
    fun provideDatabase(): DispatcherProvider {
        return DispatcherProvider()
    }

}