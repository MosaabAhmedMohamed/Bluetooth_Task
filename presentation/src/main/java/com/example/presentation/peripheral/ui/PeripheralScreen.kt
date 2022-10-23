package com.example.presentation.peripheral.ui


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.presentation.R
import com.example.presentation.base.ui.HomeBottomTab
import com.example.presentation.base.ui.ext.*
import com.example.presentation.base.ui.logs.LogsView
import com.example.presentation.base.ui.theme.purple200
import com.example.presentation.peripheral.viewmodel.BlePeripheralViewModel
import com.example.presentation.peripheral.viewstate.PeripheralSideEffect
import com.example.presentation.peripheral.viewstate.PeripheralViewState
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding

@Composable
fun PeripheralScreen(
    viewModel: BlePeripheralViewModel,
    goToCentral: () -> Unit
) {
    val tabs = HomeBottomTab.values()

    ConstraintLayout {
        val (body, _) = createRefs()
        Scaffold(
            backgroundColor = MaterialTheme.colors.primarySurface,
            modifier = Modifier.constrainAs(body) {
                top.linkTo(parent.top)
            },
            bottomBar = {
                BottomNavigation(
                    backgroundColor = purple200,
                    modifier = Modifier
                        .navigationBarsHeight(56.dp)
                ) {
                    tabs.forEach { tab ->
                        BottomNavigationItem(
                            icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                            label = { Text(text = stringResource(tab.title), color = Color.White) },
                            selected = tab == HomeBottomTab.PERIPHERAL,
                            onClick = { if (tab == HomeBottomTab.CENTRAL) goToCentral() },
                            selectedContentColor = LocalContentColor.current,
                            unselectedContentColor = LocalContentColor.current,
                            modifier = Modifier.navigationBarsPadding()
                        )
                    }
                }
            }
        ) {
            PeripheralScreenBody(viewModel)
        }
    }

}

@Composable
private fun PeripheralScreenBody(viewModel: BlePeripheralViewModel) {
    val checkedState = remember { mutableStateOf(false) }
    var editTextCharForIndicate by rememberSaveable { mutableStateOf("Android indication") }
    val viewState = viewModel.state().collectAsState(PeripheralViewState()).value
    val sideEffect = viewModel.sideEffect().collectAsState().value
    val currentSideEffect = remember { mutableStateOf<PeripheralSideEffect>(PeripheralSideEffect.Initial) }

    ConstraintLayout(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
    ) {
        val (advertising, switchAdvertising, connectionState, subscriptionState, notificationMessage, indicateTf, notifyBtn, logsView) = createRefs()

        Text(
            text = stringResource(R.string.text_static_is_advertising),
            style = MaterialTheme.typography.body2,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier
                .constrainAs(advertising) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
                .padding(start = 16.dp, top = 12.dp)
        )

        Switch(
            checked = checkedState.value,
            onCheckedChange = {
                checkedState.value = it
                viewModel.onStartAdvAdvertisingChanged(it)
            },
            modifier = Modifier
                .constrainAs(switchAdvertising) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
                .padding(end = 16.dp)
        )

        Text(
            text = viewState.subscribers,
            style = MaterialTheme.typography.body2,
            modifier = Modifier
                .constrainAs(subscriptionState) {
                    top.linkTo(connectionState.bottom)
                }
                .padding(16.dp)
        )

        Text(
            text = stringResource(id = R.string.text_state).plus(subscriptionMsg(viewState)),
            style = MaterialTheme.typography.body2,
            modifier = Modifier
                .constrainAs(connectionState) {
                    top.linkTo(advertising.bottom)
                }
                .padding(16.dp)
        )

        TextField(
            value = editTextCharForIndicate,
            onValueChange = {
                editTextCharForIndicate = it
            },
            label = { Text(stringResource(id = R.string.send_notificatin)) },
            modifier = Modifier
                .constrainAs(indicateTf) {
                    top.linkTo(subscriptionState.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        )

        Button(onClick = {
            viewModel.bleIndicate(editTextCharForIndicate)
        }, elevation = ButtonDefaults.elevation(
            defaultElevation = 10.dp,
            pressedElevation = 15.dp,
            disabledElevation = 0.dp
        ), colors = ButtonDefaults.buttonColors(
            contentColor = Color.White
        ), modifier = Modifier
            .constrainAs(notifyBtn) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(indicateTf.bottom)
            }
            .padding(start = 16.dp, end = 16.dp, top = 16.dp,)
            .fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.notify))
        }

        LogsView(modifier = Modifier
            .constrainAs(logsView) {
                top.linkTo(notifyBtn.bottom)
                end.linkTo(parent.end)
                start.linkTo(parent.start)
            }
            .padding(top = 16.dp),
            logs = viewState.logs,
            onClearLogClicked = {
                viewModel.clearLog()
            })

        if (currentSideEffect.value != sideEffect) {
            currentSideEffect.value = sideEffect
            when (sideEffect) {
                PeripheralSideEffect.Initial -> {
                    Text(
                        text =  stringResource(id = R.string.text_state).plus(stringResource(id = R.string.text_disconnected)) ,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier
                            .constrainAs(connectionState) {
                                top.linkTo(advertising.bottom)
                            }
                            .padding(16.dp)
                    )
                }
                PeripheralSideEffect.OnPermissionGranted -> viewModel.onStartAdvAdvertisingChanged(
                    checkedState.value
                )
                is PeripheralSideEffect.OnStartAdvAdvertisingClicked -> {
                    if (sideEffect.state) {
                        PrepareAndStartAdvertising(
                            isBluetoothEnabled = viewModel.isBluetoothEnabled(),
                            onPermissionGranted = {
                                viewModel.onPermissionGranted()
                            },
                            isAdvertisingAllowed = {
                                if (it) {
                                    viewModel.bleStartAdvertising()
                                } else {
                                    viewModel.bleStopAdvertising()
                                }
                            }
                        )
                    } else {
                        viewModel.bleStopAdvertising()
                    }
                }
            }
        }
    }
}

@Composable
private fun subscriptionMsg(viewState: PeripheralViewState) =
    if (viewState.connectionState == BluetoothProfile.STATE_CONNECTED)
        stringResource(id = R.string.text_connected)
    else
        stringResource(id = R.string.text_disconnected)


@Composable
private fun PrepareAndStartAdvertising(
    isBluetoothEnabled: Boolean,
    onPermissionGranted: () -> Unit,
    isAdvertisingAllowed: (state: Boolean) -> Unit
) {
    if (ensureBluetoothCanBeUsed(isBluetoothEnabled, onPermissionGranted)) {
        isAdvertisingAllowed(true)
    } else {
        isAdvertisingAllowed(false)
    }
}

@Composable
private fun ensureBluetoothCanBeUsed(
    isBluetoothEnabled: Boolean,
    onPermissionGranted: () -> Unit
): Boolean {
    val requestBluetooth = rememberBluetoothLauncher(onPermissionGranted)
    val requestPermissionLauncher =
        rememberPermissionsLauncherForActivityResult(onPermissionGranted)

    val context = LocalContext.current

    return if (context.isBluetoothPeripheralPermissionGranted(requestPermissionLauncher)) {
        if (isBluetoothEnabled) {
            //appendLog("BLE ready for use")
            true
        } else {
            // start activity for the request
            SideEffect {
                requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
            // appendLog("Bluetooth OFF")
            false
        }
    } else {
        //appendLog("Bluetooth permissions denied")
        false
    }
}

