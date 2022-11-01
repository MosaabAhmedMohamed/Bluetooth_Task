package com.example.bluetoothtask.di.module

import android.content.Context
import androidx.room.Room
import com.example.data.db.BluetoothTaskDatabase
import com.example.data.db.DB_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DbModule {

    @Provides
    @Singleton
    fun provideDatabase(context: Context): BluetoothTaskDatabase {
        return Room.databaseBuilder(context, BluetoothTaskDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

}