package com.example.presentation.base.ui.ext

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

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


fun Fragment.isBluetoothCentralPermissionGranted(): Boolean {
    val connectPermission =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
    val scanPermission =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
    return connectPermission == PackageManager.PERMISSION_GRANTED &&
            scanPermission == PackageManager.PERMISSION_GRANTED ||
            requireContext().hasPermissions(centralWantedPermissions())

}


fun askForBluetoothCentralPermission(launcher: ActivityResultLauncher<Array<String>>) {
    if (centralWantedPermissions().isNotEmpty())
        launcher.launch(
            centralWantedPermissions()
        )
}

fun Fragment.isBluetoothPeripheralPermissionGranted(): Boolean {
    val connectPermission =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
    val scanAdvertise =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADVERTISE)
    return connectPermission == PackageManager.PERMISSION_GRANTED &&
            scanAdvertise == PackageManager.PERMISSION_GRANTED ||
            requireContext().hasPermissions(peripheralWantedPermissions())

}


fun askForBluetoothPeripheralPermission(launcher: ActivityResultLauncher<Array<String>>) {
    if (peripheralWantedPermissions().isNotEmpty())
        launcher.launch(peripheralWantedPermissions())
}


fun Fragment.isLocationPermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // BLUETOOTH_SCAN permission has flag "neverForLocation", so location not needed
        true
    } else Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            requireContext().hasPermissions(locationPermission())
}


fun Fragment.askForLocationPermission() {
    ActivityCompat.requestPermissions(
        requireActivity(),
        locationPermission(),
        PermissionExt.LOCATION_PERMISSION_REQUEST_CODE
    )
}

enum class AskType {
    AskOnce,
    InsistUntilSuccess
}

