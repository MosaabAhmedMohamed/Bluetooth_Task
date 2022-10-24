package com.example.presentation.base.ui.ext

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat


fun Context.hasPermissions(permissions: List<String>): Boolean = permissions.all {
    ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}
