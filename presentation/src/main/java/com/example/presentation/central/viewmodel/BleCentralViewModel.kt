package com.example.presentation.central.viewmodel

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ble.*
import com.example.core.util.DispatcherProvider
import com.example.domain.central.usecase.GattUseCase
import com.example.presentation.central.viewstate.CentralSideEffect
import com.example.presentation.central.viewstate.CentralViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleCentralViewModel @Inject constructor(
    context: Context,
    private val gattUseCase: GattUseCase,
    private val bluetoothAdapter: BluetoothAdapter,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val uiState: MutableStateFlow<CentralViewState> =
        MutableStateFlow(CentralViewState())

    fun state() = uiState.asStateFlow()

    private val sideEffectState: MutableStateFlow<CentralSideEffect> =
        MutableStateFlow(CentralSideEffect.Initial)

    fun sideEffect() = sideEffectState.asStateFlow()

    private var lifecycleState = BLELifecycleState.Disconnected
        set(value) {
            field = value
            appendLog("status = $value")

            viewModelScope.launch(dispatchers.main) {
                uiState.update {
                    it.copy(state = field)
                }
            }
        }

    private val bleScanner by lazy { BleScanManager(bluetoothAdapter) }

    fun init() {
        if (bleScanner.isScanning) {
            appendLog("Already scanning")
            return
        }

        appendLog("Starting BLE scan, filter: ${bleScanner.serviceFilter()}")

        bleScanner.isScanning = true
        lifecycleState = BLELifecycleState.Scanning
        bleScanner.startScan(scanCallback)
    }

    fun isBluetoothEnabled() = bluetoothAdapter.isEnabled

    fun getBluetoothOnOffState() = if (bluetoothAdapter.isEnabled) {
        BluetoothAdapter.STATE_ON
    } else {
        BluetoothAdapter.STATE_OFF
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val name: String? = result.scanRecord?.deviceName ?: result.device.name
            appendLog("onScanResult name=$name address= ${result.device?.address}")
            bleScanner.safeStopBleScan(this)
            lifecycleState = BLELifecycleState.Connecting
            viewModelScope.launch(dispatchers.main) {
                result.device.connectGatt(context, false, gattUseCase.gattCallback())
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            appendLog("onBatchScanResults, ignoring")
        }

        override fun onScanFailed(errorCode: Int) {
            appendLog("onScanFailed errorCode=$errorCode")
            bleScanner.safeStopBleScan(this)
            lifecycleState = BLELifecycleState.Disconnected
            bleRestartLifecycle()
        }
    }

    fun bleEndLifecycle() {
        bleScanner.safeStopBleScan(scanCallback)
        gattUseCase.closeGatt()
        gattUseCase.setConnectedGattToNull()
        lifecycleState = BLELifecycleState.Disconnected
        sideEffectState.value = CentralSideEffect.Initial
    }

    fun bleRestartLifecycle(userWantsToScanAndConnect: Boolean = true) {
        viewModelScope.launch(dispatchers.main) {
            if (userWantsToScanAndConnect) {
                if (gattUseCase.isGattNotInitialized()) {
                    init()
                } else {
                    gattUseCase.disconnectGatt()
                }
            } else {
                bleEndLifecycle()
            }
        }
    }

    fun onTapRead() {
        viewModelScope.launch(dispatchers.main) {
            gattUseCase.onTapRead()
        }
    }

    fun onTapWrite(message: ByteArray) = gattUseCase.onTapWrite(message)

    private fun appendLog(message: String) {
        viewModelScope.launch{
            uiState.update {
                val logs = it.logs
                logs.add(0, message)
                it.copy(logs = logs)
            }
        }
    }

    fun clearLog() {
        viewModelScope.launch(dispatchers.main) {
            uiState.update {
                it.copy(logs = mutableListOf())
            }
        }
    }

    fun onScanAndConnectChanged(userWantsToScanAndConnect: Boolean) {
        uiState.update {
            it.copy(
                userWantsToScanAndConnect = userWantsToScanAndConnect
            )
        }
    }

    fun restartLifecycle() {
        sideEffectState.value = CentralSideEffect.OnBleRestartLifecycle
    }

    fun onPermissionGranted() {
        if (isBluetoothEnabled())
            sideEffectState.value = CentralSideEffect.OnPermissionGranted
    }

    fun onBleStateChanged(bleState: Int) {
        sideEffectState.value = CentralSideEffect.BleOnOffState(bleState)
    }

    init {
        viewModelScope.launch(dispatchers.io) {
            gattUseCase.gattStateCallback()
                .collectLatest { domainState ->
                    lifecycleState = domainState.state
                    appendLog(domainState.log)
                    if (domainState.isRestartLifecycle) bleRestartLifecycle()
                    uiState.update {
                        it.copy(
                            indicate = domainState.indicate,
                            read = domainState.read
                        )
                    }
                }
        }
    }
}