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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import com.example.presentation.R
import com.example.presentation.base.ui.CustomDialogLocation
import com.google.accompanist.permissions.*


fun centralWantedPermissions() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    listOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
    )
} else {
    emptyList()
}

fun peripheralWantedPermissions() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    listOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
    )
} else {
    emptyList()
}

fun locationPermission() = listOf(Manifest.permission.ACCESS_FINE_LOCATION)


fun Context.isBluetoothCentralPermissionGranted(): Boolean {
    val connectPermission =
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
    val scanPermission =
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
    return connectPermission == PackageManager.PERMISSION_GRANTED &&
            scanPermission == PackageManager.PERMISSION_GRANTED ||
            hasPermissions(centralWantedPermissions())

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
        launcher.launch(peripheralWantedPermissions().toTypedArray())
}


fun Context.isLocationPermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // BLUETOOTH_SCAN permission has flag "neverForLocation", so location not needed
        true
    } else Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            hasPermissions(locationPermission())
}

@Composable
fun Context.isBluetoothPeripheralPermissionGranted(
    launcher: ActivityResultLauncher<Array<String>>
): Boolean {
    if (isBluetoothPeripheralPermissionGranted()) {
        return true
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


@ExperimentalPermissionsApi
@Composable
fun RequestPermission(
    permissions: List<String>,
    rationaleMessage: String = stringResource(id = R.string.permission_request_desc),
    onPermissionGranted: () -> Unit
) {
    val permissionState = rememberMultiplePermissionsState(permissions)

    HandleRequest(
        permissionState = permissionState,
        deniedContent = { shouldShowRationale ->
            PermissionDeniedContent(
                rationaleMessage = rationaleMessage,
                shouldShowRationale = shouldShowRationale
            ) { permissionState.launchMultiplePermissionRequest() }
        }
    ) {
        onPermissionGranted()
    }
}

@ExperimentalPermissionsApi
@Composable
fun HandleRequest(
    permissionState: MultiplePermissionsState,
    deniedContent: @Composable (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    if (permissionState.allPermissionsGranted) {
        content()
    } else {
        deniedContent(permissionState.shouldShowRationale)
    }
}

@Composable
fun Content(showButton: Boolean = true, onClick: () -> Unit) {
    if (showButton) {
        val enableLocation = remember { mutableStateOf(true) }
        if (enableLocation.value) {
            CustomDialogLocation(
                title = stringResource(id = R.string.permission_dialog_title),
                desc = stringResource(id = R.string.permission_dialog_desc),
                enableLocation,
                onClick
            )
        }
    }
}

@ExperimentalPermissionsApi
@Composable
fun PermissionDeniedContent(
    rationaleMessage: String,
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    if (shouldShowRationale) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = stringResource(id = R.string.permission_request),
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.body2.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(rationaleMessage)
            },
            confirmButton = {
                Button(onClick = onRequestPermission) {
                    Text(stringResource(id = R.string.give_permission))
                }
            }
        )
    } else {
        Content(onClick = onRequestPermission)
    }

}


enum class AskType {
    AskOnce,
    InsistUntilSuccess
}

