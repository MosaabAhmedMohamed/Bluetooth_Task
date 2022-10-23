package com.example.presentation.peripheral.viewmodel

import android.bluetooth.*
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ble.BleAdvertiserManager
import com.example.core.util.DispatcherProvider
import com.example.domain.peripheral.model.PeripheralGattDomainModel
import com.example.domain.peripheral.usecase.GattServerUseCase
import com.example.presentation.peripheral.viewstate.PeripheralSideEffect
import com.example.presentation.peripheral.viewstate.PeripheralViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlePeripheralViewModel @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val gattServerUseCase: GattServerUseCase,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val uiState: MutableStateFlow<PeripheralViewState> =
        MutableStateFlow(PeripheralViewState())

    fun state() = uiState.asStateFlow()

    private val sideEffectState: MutableStateFlow<PeripheralSideEffect> =
        MutableStateFlow(PeripheralSideEffect.Initial)

    fun sideEffect() = sideEffectState.asStateFlow()


    private var subscribedDevices = emptySet<BluetoothDevice>()

    private val bleAdvertiser by lazy {
        BleAdvertiserManager(bluetoothAdapter, {
            appendLog(it)
        }, { advertising ->
            viewModelScope.launch(dispatchers.main) {
                uiState.update { it.copy(isAdvertising = advertising) }
            }
        })
    }

    fun isBluetoothEnabled() = bluetoothAdapter.isEnabled

    fun getBluetoothOnOffState() = if (bluetoothAdapter.isEnabled) {
        BluetoothAdapter.STATE_ON
    } else {
        BluetoothAdapter.STATE_OFF
    }

    fun bleStartAdvertising() {
        viewModelScope.launch(dispatchers.main) {
            gattServerUseCase.bleStartGattServer()
            bleAdvertiser.startAdvertising()
        }
    }

    fun bleStopAdvertising() {
        viewModelScope.launch(dispatchers.main) {
            gattServerUseCase.bleStopGattServer()
            bleAdvertiser.stopAdvertising()
        }
    }

    fun bleIndicate(text: String) {
        viewModelScope.launch(dispatchers.main) {
            val data = text.toByteArray(Charsets.UTF_8)
            gattServerUseCase.charForIndicate()?.let {
                it.value = data
                for (device in subscribedDevices) {
                    appendLog("sending indication \"$text\"")
                    gattServerUseCase.gattServer()?.notifyCharacteristicChanged(device, it, true)
                }
            }
        }
    }

    private fun updateSubscribersUI() {
        val strSubscribers = "${subscribedDevices.count()} subscribers"
        viewModelScope.launch(dispatchers.main) {
            uiState.update { it.copy(subscribers = strSubscribers) }
        }
    }

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
        viewModelScope.launch(dispatchers.main) {
            uiState.update {
                it.copy(logs = mutableListOf())
            }
        }
    }

    fun setReadMessage(readMessage: String) {
        gattServerUseCase.setReadMessage(readMessage)
    }

    fun onStartAdvAdvertisingChanged(advertisingState: Boolean) {
        sideEffectState.value = PeripheralSideEffect.OnStartAdvAdvertisingClicked(advertisingState)
    }

    fun onPermissionGranted() {
        sideEffectState.value = PeripheralSideEffect.OnPermissionGranted
    }

    private fun onDisconnected() {
        sideEffectState.value = PeripheralSideEffect.OnDisconnected
    }


    init {
        viewModelScope.launch(dispatchers.io) {
            gattServerUseCase.gattStateCallback()
                .onEach {
                    Log.d("testtestTAG", "   :   ${it}")
                }
                .collectLatest { domainState ->
                    updateUiState(domainState)
                    handleConnectionState(domainState.connectionState)
                }

        }
    }

    private fun updateUiState(domainState: PeripheralGattDomainModel) {
        subscribedDevices = domainState.subscribedDevices
        updateSubscribersUI()
        appendLog(domainState.log)
        uiState.update {
            it.copy(
                connectionState = domainState.connectionState,
                write = domainState.write
            )
        }
    }

    private fun handleConnectionState(connectionState: Int) {
        if (uiState.value.isAdvertising)
            if (connectionState == BluetoothProfile.STATE_DISCONNECTED ||
                connectionState == BluetoothProfile.STATE_DISCONNECTING
            ) {
                onDisconnected()
            }
    }
}

