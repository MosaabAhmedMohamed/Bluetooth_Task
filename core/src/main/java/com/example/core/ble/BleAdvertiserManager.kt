package com.example.core.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BleAdvertiserManager(
    private val bluetoothAdapter: BluetoothAdapter,
    private val log: (mes: String) -> Unit,
    private val advertisingState: (state: Boolean) -> Unit
) {

    private var isAdvertising = false
        set(value) {
            field = value

            // update visual state of the switch
            advertisingState.invoke(field)
        }

    private val bleAdvertiser by lazy {
        bluetoothAdapter.bluetoothLeAdvertiser
    }

    private val advertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
        .setConnectable(true)
        .build()

    private val advertiseData = AdvertiseData.Builder()
        .setIncludeDeviceName(false) // don't include name, because if name size > 8 bytes, ADVERTISE_FAILED_DATA_TOO_LARGE
        .addServiceUuid(ParcelUuid(UUID.fromString(BleExt.SERVICE_UUID)))
        .build()


   suspend fun startAdvertising() {
        isAdvertising = true
        bleAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback())
    }

    suspend  fun stopAdvertising() {
        if (isAdvertising) {
            isAdvertising = false
            bleAdvertiser.stopAdvertising(advertiseCallback())
        }
    }

    private suspend fun advertiseCallback() = suspendCoroutine<AdvertiseCallback> { cont ->
       val advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                log.invoke("Advertise start success\n${BleExt.SERVICE_UUID}")
            }

            override fun onStartFailure(errorCode: Int) {
                val desc = when (errorCode) {
                    ADVERTISE_FAILED_DATA_TOO_LARGE -> "\nADVERTISE_FAILED_DATA_TOO_LARGE"
                    ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "\nADVERTISE_FAILED_TOO_MANY_ADVERTISERS"
                    ADVERTISE_FAILED_ALREADY_STARTED -> "\nADVERTISE_FAILED_ALREADY_STARTED"
                    ADVERTISE_FAILED_INTERNAL_ERROR -> "\nADVERTISE_FAILED_INTERNAL_ERROR"
                    ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "\nADVERTISE_FAILED_FEATURE_UNSUPPORTED"
                    else -> ""
                }
                log.invoke("Advertise start failed: errorCode=$errorCode $desc")
                isAdvertising = false
            }
        }
        cont.resume(advertiseCallback)
    }
}