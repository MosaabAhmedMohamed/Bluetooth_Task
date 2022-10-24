package com.example.bluetoothtask

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.presentation.base.ui.NavManager
import com.example.presentation.base.ui.theme.BluetoothTaskTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavHostActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BluetoothTaskTheme {
                NavManager()
            }
        }
    }
}