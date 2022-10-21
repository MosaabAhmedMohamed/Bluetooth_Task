package com.example.presentation.base.ui.ext

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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


@Composable
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

@Composable
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


@Composable
fun rememberBluetoothLauncher(onPermissionGranted: () -> Unit)
        : ManagedActivityResultLauncher<Intent, ActivityResult> {
    return androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //granted
            onPermissionGranted()
        } else {
            //deny
        }
    }
}

@Composable
fun rememberPermissionsLauncherForActivityResult(onPermissionGranted: () -> Unit)
        : ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> {
    return androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.contains(false).not()

        if (isGranted) {
            // PERMISSION GRANTED
            onPermissionGranted()
        } else {
            // PERMISSION NOT GRANTED
            //appendLog("ERROR: onRequestPermissionsResult requestCode=$isGranted not handled")
        }
    }
}

enum class AskType {
    AskOnce,
    InsistUntilSuccess
}

