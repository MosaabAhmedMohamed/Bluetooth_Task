package com.example.bluetoothtask.di.centeral


import com.example.presentation.central.fragment.CentralFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module(includes = [CentralModule::class])
abstract class CentralFragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun provideCentralFragment(): CentralFragment

}