package com.example.presentation.base.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Radio
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.presentation.R

enum class HomeBottomTab(
    @StringRes val title: Int,
    val icon: ImageVector
) {
    CENTRAL(R.string.central, Icons.Filled.Home),
    PERIPHERAL(R.string.peripheral, Icons.Filled.Radio);

    companion object {
        fun getTabFromResource(@StringRes resource: Int): HomeBottomTab {
            return when (resource) {
                R.string.peripheral -> PERIPHERAL
                else -> CENTRAL
            }
        }
    }
}