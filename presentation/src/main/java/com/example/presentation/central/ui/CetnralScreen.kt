package com.example.presentation.central.ui

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
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
import com.example.presentation.base.ui.ext.*
import com.example.presentation.base.ui.logs.LogsView
import com.example.presentation.base.ui.theme.purple200
import com.example.presentation.central.viewmodel.BleCentralViewModel
import com.example.presentation.central.viewstate.CentralSideEffect
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.*

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
            CentralScreenBody(viewModel = viewModel)
        }
    }
}

@Composable
private fun CentralScreenBody(viewModel: BleCentralViewModel) {
    val viewState = viewModel.state().collectAsState().value
    val sideEffect = viewModel.sideEffect().collectAsState().value
    val currentSideEffect =
        remember { mutableStateOf<CentralSideEffect>(CentralSideEffect.Initial) }
    val requestBluetoothLauncher = rememberBluetoothLauncher {
        viewModel.askingForEnableBluetoothStatus(false)
        if (it)
            viewModel.onPermissionGranted()
        else
            viewModel.onScanAndConnectChanged(false)
    }

    ConstraintLayout(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
    ) {
        val (autoconnectTv, autoconnectSwitch, connectionStateTv, subscriptionStateTv, notificationMessageTv, logsView) = createRefs()

        if (currentSideEffect.value != sideEffect) {
            currentSideEffect.value = sideEffect
            when (sideEffect) {
                is CentralSideEffect.Initial -> {

                }
                is CentralSideEffect.OnBleRestartLifecycle -> {
                    BleRestartLifecycle(viewModel.isBluetoothEnabled(),
                        onBleRestartLifecycle = {
                            viewModel.bleRestartLifecycle(viewState.isUserWantsToScanAndConnect)
                        })
                }
                is CentralSideEffect.OnPermissionGranted -> {
                    PrepareAndStartBleScan(
                        isBluetoothEnabled = viewModel.isBluetoothEnabled(),
                        onInitViewModel = {
                            viewModel.init()
                        })
                }
                is CentralSideEffect.BleOnOffState -> {
                    when (sideEffect.bleState) {
                        BluetoothAdapter.STATE_ON -> {
                            if (viewState.state == BLELifecycleState.Disconnected) {
                                viewModel.restartLifecycle()
                            }
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            if (viewState.isUserWantsToScanAndConnect.not()) {
                                viewModel.bleEndLifecycle()
                            }
                        }
                    }
                }
            }
        }

        BleBroadcastReceiver(
            isChecked = viewState.isUserWantsToScanAndConnect,
            onBleEvent = { intent ->
                val bleState = intent?.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.STATE_OFF
                ) ?: BluetoothAdapter.STATE_OFF

                viewModel.onBleStateChanged(bleState)
            },
            onBleRestartLifecycle = {
                if (viewState.isUserWantsToScanAndConnect)
                    viewModel.restartLifecycle()
                else
                    viewModel.bleEndLifecycle()

            })

        Text(
            text = stringResource(R.string.text_static_autoconnect),
            style = MaterialTheme.typography.body2,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier
                .constrainAs(autoconnectTv) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
                .padding(start = 16.dp, top = 12.dp)
        )

        Switch(
            checked = viewState.isUserWantsToScanAndConnect,
            onCheckedChange = {
                viewModel.onScanAndConnectChanged(it)
            },
            modifier = Modifier
                .constrainAs(autoconnectSwitch) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
                .padding(end = 16.dp)
        )

        Text(
            text = stringResource(id = R.string.text_state).plus(viewState.state.name),
            style = MaterialTheme.typography.body2,
            modifier = Modifier
                .constrainAs(connectionStateTv) {
                    top.linkTo(autoconnectTv.bottom)
                    start.linkTo(parent.start)
                }
                .padding(16.dp)
        )

        Text(
            text = stringResource(id = R.string.text_subscription_state).plus(
                subscriptionMsg(
                    viewState.state
                )
            ),
            style = MaterialTheme.typography.body2,
            modifier = Modifier
                .constrainAs(subscriptionStateTv) {
                    top.linkTo(connectionStateTv.bottom)
                    start.linkTo(parent.start)
                }
                .padding(16.dp)
        )


        Text(
            text = stringResource(id = R.string.text_static_received_not_hint).plus(" ${viewState.indicate}"),
            style = MaterialTheme.typography.body2,
            modifier = Modifier
                .constrainAs(notificationMessageTv) {
                    top.linkTo(subscriptionStateTv.bottom)
                    start.linkTo(parent.start)
                }
                .padding(16.dp)
        )

        if (viewState.logs.isNotEmpty())
            LogsView(modifier = Modifier
                .constrainAs(logsView) {
                    top.linkTo(notificationMessageTv.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                }
                .padding(top = 16.dp), logs = viewState.logs,
                onClearLogClicked = {
                    viewModel.clearLog()
                })

        checkPermissionRequest(viewState.isUserWantsToScanAndConnect,
            onPermissionGranted = { viewModel.onPermissionGranted() },
            onDismissRequest = { viewModel.onScanAndConnectChanged(false) })

        checkForEnablingBluetooth(viewModel.isAskForEnableBluetooth(), requestBluetoothLauncher) {
            viewModel.askingForEnableBluetoothStatus(true)
        }
    }
}

@Composable
private fun checkForEnablingBluetooth(
    isAskForEnableBluetooth: Boolean,
    requestBluetoothLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    askingForEnableBluetoothStatus: () -> Unit
) {
    val context = LocalContext.current
    if (isAskForEnableBluetooth && context.isLocationPermissionGranted()) {
        askingForEnableBluetoothStatus.invoke()
        SideEffect {
            requestBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun checkPermissionRequest(
    isUserWantsToScanAndConnect: Boolean,
    onPermissionGranted: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    if (isUserWantsToScanAndConnect && context.isLocationPermissionGranted().not() ||
        isUserWantsToScanAndConnect && context.isBluetoothCentralPermissionGranted().not()
    ) {
        val allPermissions = mutableListOf<String>()
            .plus(locationPermission())
            .plus(centralWantedPermissions())
        RequestPermission(
            allPermissions,
            onPermissionGranted = { onPermissionGranted() },
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun subscriptionMsg(state: BLELifecycleState): String {
    return if (state == BLELifecycleState.Connected) {
        stringResource(id = R.string.text_subscribed)
    } else {
        stringResource(id = R.string.text_not_subscribed)
    }
}

@Composable
fun BleBroadcastReceiver(
    isChecked: Boolean,
    onBleEvent: (intent: Intent?) -> Unit,
    onBleRestartLifecycle: () -> Unit
) {
    val context = LocalContext.current
    HandleBluetoothBroadcastLifecycle(context, onBleEvent, isChecked)
    onBleRestartLifecycle.invoke()
}

@Composable
private fun HandleBluetoothBroadcastLifecycle(
    context: Context,
    onBleEvent: (intent: Intent?) -> Unit,
    isChecked: Boolean
) {
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

        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(bleBroadcast)
        }
    }
}

@Composable
private fun PrepareAndStartBleScan(isBluetoothEnabled: Boolean, onInitViewModel: () -> Unit) {
    if (ensureBluetoothCanBeUsed(isBluetoothEnabled)) {
        onInitViewModel()
    }
}

@Composable
private fun BleRestartLifecycle(
    isBluetoothEnabled: Boolean,
    onBleRestartLifecycle: () -> Unit
) {
    if (ensureBluetoothCanBeUsed(isBluetoothEnabled)) {
        onBleRestartLifecycle()
    }
}

@Composable
private fun ensureBluetoothCanBeUsed(
    isBluetoothEnabled: Boolean
): Boolean {
    val context = LocalContext.current
    return if (context.isBluetoothCentralPermissionGranted()) {
        if (isBluetoothEnabled) {
            context.isLocationPermissionGranted()
        } else {
            false
        }
    } else {
        false
    }
}






