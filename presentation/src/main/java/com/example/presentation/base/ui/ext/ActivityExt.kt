package com.example.presentation.base.ui.ext

import com.example.presentation.R
import com.example.presentation.base.ui.BaseActivity
import com.example.presentation.base.ui.NavManager


fun BaseActivity.initNavManager() {
    NavManager.setOnNavEvent {

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)

        currentFragment?.navigateSafe(it)
    }
}