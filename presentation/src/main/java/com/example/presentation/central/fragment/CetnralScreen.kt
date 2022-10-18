package com.example.presentation.central.fragment


import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.core.ble.BLELifecycleState
import com.example.presentation.R
import com.example.presentation.base.ui.HomeBottomTab
import com.example.presentation.base.ui.ext.AskType
import com.example.presentation.base.ui.ext.isBluetoothCentralPermissionGranted
import com.example.presentation.base.ui.ext.isLocationPermissionRequired
import com.example.presentation.base.ui.ext.locationPermission
import com.example.presentation.base.ui.theme.purple200
import com.example.presentation.central.viewmodel.BleCentralViewModel
import com.example.presentation.central.viewstate.CentralViewState
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach

@Composable
fun CentralScreen(
    viewModel: BleCentralViewModel,
    goToPeripheral: () -> Unit
) {

    val tabs = HomeBottomTab.values()

    ConstraintLayout {
        val (body, bottomBar) = createRefs()
        Scaffold(
            backgroundColor = MaterialTheme.colors.primarySurface,
            modifier = Modifier
                .constrainAs(body) {
                    top.linkTo(parent.top)
                },
            bottomBar = {
                BottomNavigation(
                    backgroundColor = purple200,
                    modifier = Modifier
                        .constrainAs(bottomBar) {
                            bottom.linkTo(parent.bottom)
                        }
                        .navigationBarsHeight(56.dp))
                {
                    tabs.forEach { tab ->
                        BottomNavigationItem(
                            icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                            label = { Text(text = stringResource(tab.title), color = Color.White) },
                            selected = tab == HomeBottomTab.CENTRAL,
                            onClick = { if (tab == HomeBottomTab.PERIPHERAL) goToPeripheral() },
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
private fun PosterDetailsBody(viewModel: BleCentralViewModel) {

    ConstraintLayout(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
    ) {
        val (autoconnect, autoconnectSwitch, connectionState, subscriptionState, notificationMessage) = createRefs()
        val checkedState = remember { mutableStateOf(false) }
        val bleOnOffState = remember { mutableStateOf(viewModel.getBluetoothOnOffState()) }

        val state = viewModel.state()
            .onEach {
                //appendLog("state$it" )
            }
            .collectAsState(CentralViewState.Initial).value

        when (state) {
            is CentralViewState.ConnectionLifeCycle -> {
                Text(
                    text = "State: ${state.state.name}",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .constrainAs(connectionState) {
                            top.linkTo(autoconnect.bottom)
                        }
                        .padding(16.dp)
                )

                val subscriptionMsg = if (state.state != BLELifecycleState.Connected) {
                    stringResource(id = R.string.text_not_subscribed)
                } else if (state.state == BLELifecycleState.Connected) {
                    stringResource(id = R.string.text_subscribed)
                } else {
                    stringResource(id = R.string.text_not_subscribed)
                }

                Text(
                    text = subscriptionMsg,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .constrainAs(subscriptionState) {
                            top.linkTo(connectionState.bottom)
                        }
                        .padding(16.dp)
                )

            }
            is CentralViewState.Indicate -> {
                Text(
                    text = stringResource(id = R.string.text_static_indicate_hint).plus(" ${state.message}"),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .constrainAs(notificationMessage) {
                            top.linkTo(subscriptionState.bottom)
                        }
                        .padding(16.dp)
                )
            }
            is CentralViewState.Initial -> {}
            is CentralViewState.Log -> {
                state.message?.let { appendLog(it) }
            }
            is CentralViewState.Read -> {}
            is CentralViewState.UserWantsToScanAndConnect -> {
                SystemBroadcastReceiver(isChecked = state.state, onBleEvent = { intent ->
                    bleOnOffState.value = intent?.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF
                    ) ?: BluetoothAdapter.STATE_OFF
                }, onBleRestartLifecycle = {
                    if (checkedState.value)
                        viewModel.restartLifecycle()
                })
                checkedState.value = state.state
            }
            is CentralViewState.OnBleRestartLifecycle -> {
                appendLog("state : OnBleRestartLifecycle" )

                    bleRestartLifecycle(viewModel.isBluetoothEnabled(),
                        onPermissionGranted = {
                            viewModel.onPermissionGranted()
                        }, onBleRestartLifecycle = {
                            viewModel.bleRestartLifecycle(checkedState.value)
                        })
            }
            is CentralViewState.OnPermissionGranted -> {
                prepareAndStartBleScan(
                    isBluetoothEnabled = viewModel.isBluetoothEnabled(),
                    onInitViewModel = {
                        viewModel.init()
                    })
            }

        }

       /* when (bleOnOffState.value) {
            BluetoothAdapter.STATE_ON -> {
                appendLog("onReceive: Bluetooth ON")
                if (state == CentralViewState.ConnectionLifeCycle(BLELifecycleState.Disconnected) && checkedState.value && viewModel.isBluetoothEnabled().not()) {
                    viewModel.restartLifecycle()
                }
            }
            BluetoothAdapter.STATE_OFF -> {
                if (checkedState.value.not() || viewModel.isBluetoothEnabled()) {
                    viewModel.bleEndLifecycle()
                }
                appendLog("onReceive: Bluetooth OFF")
            }
        }*/

        Text(
            text = stringResource(R.string.text_static_autoconnect),
            style = MaterialTheme.typography.body2,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier
                .constrainAs(autoconnect) {
                    top.linkTo(parent.top)
                }
                .padding(start = 16.dp, top = 12.dp)
        )

        Switch(
            checked = checkedState.value,
            onCheckedChange = {
                checkedState.value = it

                viewModel.onScanAndConnectChanged(it)
            },
            modifier = Modifier
                .constrainAs(autoconnectSwitch) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
                .padding(end = 16.dp)
        )
    }
}

@Composable
fun SystemBroadcastReceiver(
    isChecked: Boolean,
    onBleEvent: (intent: Intent?) -> Unit,
    onBleRestartLifecycle: () -> Unit
) {


    // Grab the current context in this part of the UI tree
    val context = LocalContext.current


    // If either context or systemAction changes, unregister and register again
    DisposableEffect(context) {

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        val bleBroadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                onBleEvent.invoke(intent)
            }
        }

        when (isChecked) {
            true -> {
                context.registerReceiver(bleBroadcast, filter)
            }
            false -> {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(bleBroadcast)
            }
        }
        // When the effect leaves the Composition, remove the callback
        onDispose {
            // context.unregisterReceiver(bleBroadcast)
        }
    }

    onBleRestartLifecycle.invoke()
}

@Composable
private fun prepareAndStartBleScan(isBluetoothEnabled: Boolean, onInitViewModel: () -> Unit) {
    if (ensureBluetoothCanBeUsed(isBluetoothEnabled) {}) {
        onInitViewModel()
    }

}

@Composable
private fun bleRestartLifecycle(
    isBluetoothEnabled: Boolean,
    onPermissionGranted: () -> Unit,
    onBleRestartLifecycle: () -> Unit
) {
    if (ensureBluetoothCanBeUsed(isBluetoothEnabled, onPermissionGranted)) {
        onBleRestartLifecycle()
    }
}

@Composable
private fun ensureBluetoothCanBeUsed(
    isBluetoothEnabled: Boolean,
    onPermissionGranted: () -> Unit
): Boolean {

    appendLog("ensureBluetoothCanBeUsed")

    val requestBluetooth = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //granted
            //prepareAndStartBleScan()
            onPermissionGranted()
        } else {
            //deny
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.contains(false).not()

        if (isGranted) {
            // PERMISSION GRANTED
            //prepareAndStartBleScan()
            onPermissionGranted()
        } else {
            // PERMISSION NOT GRANTED
            appendLog("ERROR: onRequestPermissionsResult requestCode=$isGranted not handled")
        }
    }


    val context = LocalContext.current
    if (context.isBluetoothCentralPermissionGranted(AskType.AskOnce, requestPermissionLauncher)) {
        return if (isBluetoothEnabled) {
            if (context.isLocationPermissionRequired(AskType.AskOnce, onGrantPermissionOk = {
                    requestPermissionLauncher.launch(locationPermission())
                })) {
                appendLog("Bluetooth ON, permissions OK, ready")
                true
            } else {
                appendLog("Location permission denied")
                false
            }
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
        return false
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


