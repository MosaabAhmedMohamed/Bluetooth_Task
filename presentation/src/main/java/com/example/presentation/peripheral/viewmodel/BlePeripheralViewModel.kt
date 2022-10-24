package com.example.presentation.peripheral.viewmodel

import android.bluetooth.*
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
        BleAdvertiserManager(bluetoothAdapter.bluetoothLeAdvertiser, {
            appendLog(it)
        }) { advertising ->
            viewModelScope.launch(dispatchers.main) {
                uiState.update { it.copy(isAdvertising = advertising) }
            }
        }
    }

    fun isBluetoothEnabled() = bluetoothAdapter.isEnabled

    fun isAskForEnableBluetooth() = isBluetoothEnabled().not() &&
            uiState.value.isUserWantsToStartAdvertising &&
            uiState.value.isAskingForEnableBluetooth.not()

    fun getBluetoothOnOffState() = if (bluetoothAdapter.isEnabled) {
        BluetoothAdapter.STATE_ON
    } else {
        BluetoothAdapter.STATE_OFF
    }

    fun onUserWantsToStartAdvertisingChanged(userWantsToStartAdvertising: Boolean) {
        uiState.update {
            it.copy(
                isUserWantsToStartAdvertising = userWantsToStartAdvertising
            )
        }
        onStartAdvertisingChanged()
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

    private fun onStartAdvertisingChanged() {
        sideEffectState.value = PeripheralSideEffect.OnStartAdvertisingClicked
    }

    fun onPermissionGranted() {
        onStartAdvertisingChanged()
    }

    fun onDisconnected() {
        sideEffectState.value = PeripheralSideEffect.OnDisconnected
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

    fun askingForEnableBluetoothStatus(isAsking: Boolean) {
        uiState.update {
            it.copy(isAskingForEnableBluetooth = isAsking)
        }
    }

    init {
        viewModelScope.launch(dispatchers.io) {
            gattServerUseCase.gattStateCallback()
                .collectLatest { domainState ->
                    updateUiState(domainState)
                }

        }
    }
}

