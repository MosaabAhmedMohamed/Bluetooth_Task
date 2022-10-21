package com.example.presentation.peripheral.fragment


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.presentation.base.ui.theme.purple200
import com.example.presentation.peripheral.viewmodel.BlePeripheralViewModel
import com.example.presentation.peripheral.viewstate.PeripheralActionState
import com.example.presentation.peripheral.viewstate.PeripheralDataState
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import kotlinx.coroutines.flow.onEach

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
            PosterDetailsBody(viewModel)
        }
    }

}

@Composable
private fun PosterDetailsBody(viewModel: BlePeripheralViewModel) {

    ConstraintLayout(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
    ) {
        val (advertising, switchAdvertising, connectionState, subscriptionState, notificationMessage, indicateTf, notifyBtn) = createRefs()
        val checkedState = remember { mutableStateOf(false) }
        val actionState = remember { mutableStateOf<PeripheralActionState>(PeripheralActionState.Initial) }
        var editTextCharForIndicate by rememberSaveable { mutableStateOf("Android indication") }


        val viewState = viewModel.state()
            .collectAsState(PeripheralDataState(actionState = PeripheralActionState.Initial)).value

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
        appendLog(viewState.log)
        val subscriptionMsg =
            if (viewState.connectionState == BluetoothProfile.STATE_CONNECTED) {
                appendLog("Central did connect")
                stringResource(id = R.string.text_connected)

            } else {
                appendLog("Central did disconnect")
                stringResource(id = R.string.text_disconnected)
            }

        Text(
            text = "State: $subscriptionMsg",
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
            label = { Text("Indication") },
            modifier = Modifier
                .constrainAs(indicateTf) {
                    top.linkTo(subscriptionState.bottom)
                    start.linkTo(parent.start)
                }
                .padding(end = 16.dp)
        )

        Button(onClick = {
            viewModel.bleIndicate(editTextCharForIndicate)
        }, elevation = ButtonDefaults.elevation(
            defaultElevation = 10.dp,
            pressedElevation = 15.dp,
            disabledElevation = 0.dp
        ), modifier = Modifier
            .constrainAs(notifyBtn) {
                start.linkTo(indicateTf.end)
                end.linkTo(parent.end)
                top.linkTo(subscriptionState.bottom)
            }
            .padding(end = 16.dp)
        ) {
            Text(text = "Notify")
        }
        if (actionState.value != viewState.actionState){
            actionState.value = viewState.actionState
            when (viewState.actionState) {
                PeripheralActionState.Initial -> {
                    Text(
                        text = "State: ${stringResource(id = R.string.text_disconnected)}",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier
                            .constrainAs(connectionState) {
                                top.linkTo(advertising.bottom)
                            }
                            .padding(16.dp)
                    )
                }
                PeripheralActionState.OnPermissionGranted -> viewModel.onStartAdvAdvertisingChanged(
                    checkedState.value
                )
                is PeripheralActionState.OnStartAdvAdvertisingClicked -> {
                    if (viewState.actionState.state) {
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

    return if (context.isBluetoothPeripheralPermissionGranted(
            AskType.AskOnce,
            requestPermissionLauncher
        )
    ) {
        if (isBluetoothEnabled) {
            appendLog("BLE ready for use")
            true
        } else {
            // start activity for the request
            SideEffect {
                requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
            appendLog("Bluetooth OFF")
            false
        }
    } else {
        appendLog("Bluetooth permissions denied")
        false
    }
}

private fun appendLog(message: String) {
    Log.d("appendLog", message)
    /* lifecycleScope.launch {
         val strTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
         binding.textViewLog.text = binding.textViewLog.text.toString() + "\n$strTime $message"

         // scroll after delay, because textView has to be updated first
         binding.scrollViewLog.postDelayed({
             binding.scrollViewLog.fullScroll(View.FOCUS_DOWN)
         }, 16)
     }*/
}
