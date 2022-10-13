package com.example.bluetoothtask.di



import com.example.bluetoothtask.NavHostActivity
import com.example.bluetoothtask.di.centeral.CentralFragmentBuilderModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {


    @ContributesAndroidInjector(
        modules = [
            CentralFragmentBuilderModule::class
        ]
    )
    abstract fun provideNavHostActivity(): NavHostActivity
}