package com.example.presentation.peripheral.viewmodel

import android.bluetooth.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ble.BleAdvertiserManager
import com.example.core.util.DispatcherProvider
import com.example.domain.peripheral.model.PeripheralGattDomainModel
import com.example.domain.peripheral.usecase.GattServerUseCase
import com.example.presentation.peripheral.viewstate.PeripheralViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BlePeripheralViewModel @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val gattServerUseCase: GattServerUseCase,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val uiState: MutableStateFlow<PeripheralViewState> =
        MutableStateFlow(PeripheralViewState.Initial)

    fun state() = uiState.asStateFlow()

    private var subscribedDevices = emptySet<BluetoothDevice>()

    private val bleAdvertiser by lazy {
        BleAdvertiserManager(bluetoothAdapter, {
            appendLog(it)
        }, {
            viewModelScope.launch(dispatchers.main) {
                uiState.value = PeripheralViewState.Advertising(it)
            }
        })
    }

    fun isBluetoothEnabled() = bluetoothAdapter.isEnabled

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
            uiState.value = PeripheralViewState.Subscribers(strSubscribers)
        }
    }

    private fun appendLog(message: String) {
        viewModelScope.launch(dispatchers.main) {
            uiState.value = PeripheralViewState.Log(message)
        }
    }

    fun setReadMessage(readMessage: String) {
        gattServerUseCase.setReadMessage(readMessage)
    }

    init {
        viewModelScope.launch(dispatchers.io) {
            gattServerUseCase.gattStateCallback()
                .buffer(5)
                .collect {
                    withContext(dispatchers.main) {
                        when (it) {
                            is PeripheralGattDomainModel.ConnectionState -> uiState.value =
                                PeripheralViewState.ConnectionState(it.state)
                            PeripheralGattDomainModel.Initial -> {}
                            is PeripheralGattDomainModel.Log -> appendLog(it.message)
                            is PeripheralGattDomainModel.OnSubscribersChanged -> {
                                subscribedDevices = it.subscribedDevices
                                updateSubscribersUI()
                            }
                            is PeripheralGattDomainModel.Write -> uiState.value =
                                PeripheralViewState.Write(it.message)
                        }
                    }
                }
        }

    }
}