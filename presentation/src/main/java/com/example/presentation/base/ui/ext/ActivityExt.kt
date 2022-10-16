package com.example.presentation.base.ui.ext

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
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

fun Activity.buildPermissionDialog(launcher: ActivityResultLauncher<Array<String>>): AlertDialog.Builder {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Location permission required")
    builder.setMessage("BLE advertising requires location access, starting from Android 6.0")
    builder.setPositiveButton(android.R.string.ok) { _, _ ->
        launcher.launch(locationPermission())
    }
    builder.setCancelable(false)
    return builder
}