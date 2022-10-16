package com.example.presentation.central.viewmodel

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ble.*
import com.example.core.util.DispatcherProvider
import com.example.domain.central.model.CentralGattDomainModel
import com.example.domain.central.usecase.GattUseCase
import com.example.presentation.central.viewstate.CentralViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BleCentralViewModel @Inject constructor(
    context: Context,
    private val gattUseCase: GattUseCase,
    private val bluetoothAdapter: BluetoothAdapter,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val uiState: MutableStateFlow<CentralViewState> =
        MutableStateFlow(CentralViewState.Initial)

    fun state() = uiState.asStateFlow()

    private var lifecycleState = BLELifecycleState.Disconnected
        set(value) {
            field = value
            appendLog("status = $value")

            viewModelScope.launch(dispatchers.main) {
                uiState.value = CentralViewState.ConnectionLifeCycle(field)
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
        viewModelScope.launch(dispatchers.main) {
            uiState.value = CentralViewState.Log(message)
        }
    }

    init {
        viewModelScope.launch(dispatchers.io) {
            gattUseCase.gattStateCallback()
                .buffer(5)
                .onEach {
                   withContext(dispatchers.main){
                       Log.d("testtestTAG", ":$it ")
                       when (it) {
                           is CentralGattDomainModel.ConnectionLifeCycle -> lifecycleState = it.state
                           is CentralGattDomainModel.Indicate -> {
                               uiState.value = CentralViewState.Indicate(it.message)
                           }
                           CentralGattDomainModel.Initial -> {}
                           is CentralGattDomainModel.Log -> {
                               appendLog(it.message)
                           }
                           is CentralGattDomainModel.Read -> {
                               uiState.value = CentralViewState.Read(it.message)
                           }
                           CentralGattDomainModel.RestartLifecycle -> bleRestartLifecycle()
                       }
                   }
                }
                .launchIn(this)
        }
    }
}