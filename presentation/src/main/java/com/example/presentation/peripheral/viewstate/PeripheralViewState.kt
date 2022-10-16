package com.example.presentation.peripheral.viewstate


sealed class PeripheralViewState {

    object Initial : PeripheralViewState()
    data class ConnectionState(val state: Int) : PeripheralViewState()
    data class Log(val message: String?) : PeripheralViewState()
    data class Write(val message: String?) : PeripheralViewState()
    data class Subscribers(val subscribedDevices: String) : PeripheralViewState()
    data class Advertising(val isAdvertising: Boolean) : PeripheralViewState()
}