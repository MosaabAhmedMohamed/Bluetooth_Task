package com.example.bluetoothtask.di.module

import com.example.core.util.DispatcherProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DispatcherProviderModule {

    @Provides
    @Singleton
    fun provideDatabase(): DispatcherProvider {
        return DispatcherProvider()
    }

}