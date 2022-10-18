package com.example.presentation.base.ui.ext

import android.app.Activity
import androidx.appcompat.app.AlertDialog


fun Activity.buildPermissionDialog(onGrantPermissionOk: () -> Unit): AlertDialog.Builder {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Location permission required")
    builder.setMessage("BLE advertising requires location access, starting from Android 6.0")
    builder.setPositiveButton(android.R.string.ok) { _, _ ->
        onGrantPermissionOk()
    }
    builder.setCancelable(false)
    return builder
}