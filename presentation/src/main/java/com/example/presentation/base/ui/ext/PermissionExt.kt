package com.example.presentation.base.ui.ext

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionExt {

    companion object {
        const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
        const val LOCATION_PERMISSION_REQUEST_CODE = 2
        const val BLUETOOTH_ALL_PERMISSIONS_REQUEST_CODE = 3
    }
}

fun centralWantedPermissions() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
    )
} else {
    emptyArray()
}

fun peripheralWantedPermissions() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
    )
} else {
    emptyArray()
}

fun locationPermission() = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)


fun Context.isBluetoothCentralPermissionGranted(): Boolean {
    val connectPermission =
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
    val scanPermission =
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
    return connectPermission == PackageManager.PERMISSION_GRANTED &&
            scanPermission == PackageManager.PERMISSION_GRANTED ||
            hasPermissions(centralWantedPermissions())

}

@Composable
fun askForBluetoothCentralPermission(launcher: ActivityResultLauncher<Array<String>>) {
    SideEffect {
        if (centralWantedPermissions().isNotEmpty())
            launcher.launch(
                centralWantedPermissions()
            )
    }


}

fun Context.isBluetoothPeripheralPermissionGranted(): Boolean {
    val connectPermission =
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
    val scanAdvertise =
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE)
    return connectPermission == PackageManager.PERMISSION_GRANTED &&
            scanAdvertise == PackageManager.PERMISSION_GRANTED ||
            hasPermissions(peripheralWantedPermissions())

}


fun askForBluetoothPeripheralPermission(launcher: ActivityResultLauncher<Array<String>>) {
    if (peripheralWantedPermissions().isNotEmpty())
        launcher.launch(peripheralWantedPermissions())
}


fun Context.isLocationPermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // BLUETOOTH_SCAN permission has flag "neverForLocation", so location not needed
        true
    } else Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            hasPermissions(locationPermission())
}


fun Context.askForLocationPermission() {
    ActivityCompat.requestPermissions(
        this as Activity,
        locationPermission(),
        PermissionExt.LOCATION_PERMISSION_REQUEST_CODE
    )
}


@Composable
fun Context.isLocationPermissionRequired(askType: AskType, onGrantPermissionOk: () -> Unit): Boolean {
    return if (isLocationPermissionGranted()) {
        true
    } else if (askType == AskType.InsistUntilSuccess) {
        (this as Activity).buildPermissionDialog(onGrantPermissionOk).create().show()
        isLocationPermissionRequired(AskType.InsistUntilSuccess,onGrantPermissionOk)
    } else {
        // prepare motivation message show motivation message
        (this as Activity).buildPermissionDialog(onGrantPermissionOk).create().show()
        false
    }
}

@Composable
fun Context.isBluetoothCentralPermissionGranted(askType: AskType, launcher: ActivityResultLauncher<Array<String>>): Boolean {
    if (isBluetoothCentralPermissionGranted()) {
        return true
    } else if (askType == AskType.InsistUntilSuccess) {
        askForBluetoothCentralPermission(launcher)
        isBluetoothCentralPermissionGranted(AskType.InsistUntilSuccess,launcher)
    } else {
        askForBluetoothCentralPermission(launcher)
    }
    return false
}

fun Context.isBluetoothPeripheralPermissionGranted(askType: AskType, launcher: ActivityResultLauncher<Array<String>>): Boolean {
    if (isBluetoothPeripheralPermissionGranted()) {
        return true
    } else if (askType == AskType.InsistUntilSuccess) {
        askForBluetoothPeripheralPermission(launcher)
        isBluetoothPeripheralPermissionGranted(AskType.InsistUntilSuccess,launcher)
    } else {
        askForBluetoothPeripheralPermission(launcher)
    }
    return false
}

enum class AskType {
    AskOnce,
    InsistUntilSuccess
}

