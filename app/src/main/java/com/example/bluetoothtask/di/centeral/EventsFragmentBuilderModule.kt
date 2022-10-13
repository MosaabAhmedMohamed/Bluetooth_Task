package com.example.bluetoothtask.di.centeral


import com.example.presentation.central.CentralFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module(includes = [EventsModule::class])
abstract class CentralFragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun provideCentralFragment(): CentralFragment

}