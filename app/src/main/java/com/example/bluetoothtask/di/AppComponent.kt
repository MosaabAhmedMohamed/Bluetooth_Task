package com.example.bluetoothtask.di

import android.app.Application
import com.example.bluetoothtask.app.BluetoothTaskApp
import com.example.bluetoothtask.di.module.AppModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Singleton
@Component(
    modules = [AndroidSupportInjectionModule::class,
        AppModule::class,
        ActivityBuilder::class]
)
interface AppComponent {
    fun inject(app: BluetoothTaskApp)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}