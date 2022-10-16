package com.example.bluetoothtask.di.peripheral

import com.example.presentation.peripheral.fragment.PeripheralFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [PeripheralModule::class])
abstract class PeripheralFragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun providePeripheralFragment(): PeripheralFragment

}