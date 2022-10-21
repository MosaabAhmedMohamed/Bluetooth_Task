package com.example.presentation.peripheral.viewmodel

import android.bluetooth.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ble.BleAdvertiserManager
import com.example.core.util.DispatcherProvider
import com.example.core.util.update
import com.example.domain.peripheral.usecase.GattServerUseCase
import com.example.presentation.peripheral.viewstate.PeripheralActionState
import com.example.presentation.peripheral.viewstate.PeripheralDataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BlePeripheralViewModel @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val gattServerUseCase: GattServerUseCase,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val uiState: MutableStateFlow<PeripheralDataState> =
        MutableStateFlow(PeripheralDataState(actionState = PeripheralActionState.Initial))

    fun state() = uiState.asStateFlow()

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
        viewModelScope.launch(dispatchers.main) {
            uiState.update { it.copy(log = message) }
        }
    }

    fun setReadMessage(readMessage: String) {
        gattServerUseCase.setReadMessage(readMessage)
    }

    fun onStartAdvAdvertisingChanged(advertisingState: Boolean) {
        uiState.update {
            it.copy(
                actionState = PeripheralActionState.OnStartAdvAdvertisingClicked(
                    advertisingState
                )
            )
        }
    }

    fun onPermissionGranted() {
        uiState.update { it.copy(actionState = PeripheralActionState.OnPermissionGranted) }
    }

    init {
        viewModelScope.launch(dispatchers.io) {
            gattServerUseCase.gattStateCallback()
                .collectLatest { domainState ->
                    withContext(dispatchers.main) {
                        subscribedDevices = domainState.subscribedDevices
                        updateSubscribersUI()

                        uiState.update {
                            it.copy(
                                connectionState = domainState.connectionState,
                                log = domainState.log,
                                write = domainState.write
                            )
                        }
                    }
                }

        }
    }
}

