package com.example.core.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import java.util.*

class BleScanManager(private val bluetoothAdapter: BluetoothAdapter) {

    var isScanning = false

    private val scanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    fun startScan(scanCallback: ScanCallback) {
        scanner.startScan(mutableListOf(scanFilter),scanSettings,scanCallback)
    }

    private fun stopScan(scanCallback: ScanCallback) {
        scanner.stopScan(scanCallback)
    }

    fun safeStopBleScan(scanCallback: ScanCallback) {
        if (!isScanning) {
            //appendLog("Already stopped")
            return
        }

        //appendLog("Stopping BLE scan")
        isScanning = false
        stopScan(scanCallback)
    }

    private val scanFilter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid(UUID.fromString(BleExt.SERVICE_UUID)))
        .build()

    fun serviceFilter() = scanFilter.serviceUuid?.uuid.toString()

    private val scanSettings: ScanSettings
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                scanSettingsSinceM
            } else {
                scanSettingsBeforeM
            }
        }

    private val scanSettingsBeforeM = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setReportDelay(0)
        .build()

    @RequiresApi(Build.VERSION_CODES.M)
    private val scanSettingsSinceM = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        .setReportDelay(0)
        .build()

}