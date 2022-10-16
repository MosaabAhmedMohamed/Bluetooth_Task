package com.example.bluetoothtask.di



import com.example.bluetoothtask.NavHostActivity
import com.example.bluetoothtask.di.centeral.CentralFragmentBuilderModule
import com.example.bluetoothtask.di.peripheral.PeripheralFragmentBuilderModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {


    @ContributesAndroidInjector(
        modules = [
            CentralFragmentBuilderModule::class,
            PeripheralFragmentBuilderModule::class
        ]
    )
    abstract fun provideNavHostActivity(): NavHostActivity
}