package com.example.presentation.central.viewmodel

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ble.*
import com.example.core.util.DispatcherProvider
import com.example.domain.central.model.CentralGattDomainModel
import com.example.domain.central.usecase.GattUseCase
import com.example.presentation.central.viewstate.CentralSideEffect
import com.example.presentation.central.viewstate.CentralViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class BleCentralViewModel @Inject constructor(
    private val context: Application,
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
            viewModelScope.launch(dispatchers.io) {
                field = value
                appendLog("status = $value")
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

        viewModelScope.launch(dispatchers.io) {
            bleScanner.isScanning = true
            lifecycleState = BLELifecycleState.Scanning
            bleScanner.startScan(scanCallback())
        }
    }

    fun isBluetoothEnabled() = bluetoothAdapter.isEnabled

    fun isAskForEnableBluetooth() = isBluetoothEnabled().not() &&
                uiState.value.isUserWantsToScanAndConnect &&
                uiState.value.isAskingForEnableBluetooth.not()

    fun getBluetoothOnOffState() = if (bluetoothAdapter.isEnabled) {
        BluetoothAdapter.STATE_ON
    } else {
        BluetoothAdapter.STATE_OFF
    }

    private suspend fun scanCallback() = suspendCoroutine<ScanCallback> { cont ->
        viewModelScope.launch(dispatchers.io) {
            val scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val name: String? = result.scanRecord?.deviceName ?: result.device.name
                    appendLog("onScanResult name=$name address= ${result.device?.address}")
                    bleScanner.safeStopBleScan(this)
                    lifecycleState = BLELifecycleState.Connecting
                    viewModelScope.launch(dispatchers.io) {
                        gattUseCase.gattCallback()
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
            cont.resume(scanCallback)
        }
    }
    
    fun bleEndLifecycle() {
        viewModelScope.launch(dispatchers.io) {
            bleScanner.safeStopBleScan(scanCallback())
            gattUseCase.closeGatt()
            gattUseCase.setConnectedGattToNull()
            lifecycleState = BLELifecycleState.Disconnected
            sideEffectState.value = CentralSideEffect.Initial
        }
    }

    fun bleRestartLifecycle(userWantsToScanAndConnect: Boolean = true) {
        viewModelScope.launch(dispatchers.io) {
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
        viewModelScope.launch(dispatchers.io) {
            gattUseCase.onTapRead()
        }
    }

    fun onTapWrite(message: ByteArray) = gattUseCase.onTapWrite(message)

    private fun appendLog(message: String) {
        viewModelScope.launch {
            uiState.update {
                val logs = it.logs
                logs.add(0, message)
                it.copy(logs = logs)
            }
        }
    }

    fun clearLog() {
        viewModelScope.launch(dispatchers.io) {
            uiState.update {
                it.copy(logs = mutableListOf())
            }
        }
    }

    fun onScanAndConnectChanged(userWantsToScanAndConnect: Boolean) {
        uiState.update {
            it.copy(
                isUserWantsToScanAndConnect = userWantsToScanAndConnect
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

    fun askingForEnableBluetoothStatus(isAsking: Boolean) {
        uiState.update {
            it.copy(isAskingForEnableBluetooth = isAsking)
        }
    }

    private fun updateUiState(domainState: CentralGattDomainModel) {
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

    init {
        viewModelScope.launch(dispatchers.io) {
            gattUseCase.gattStateCallback()
                .collectLatest { domainState ->
                    updateUiState(domainState)
                }
        }
    }
}