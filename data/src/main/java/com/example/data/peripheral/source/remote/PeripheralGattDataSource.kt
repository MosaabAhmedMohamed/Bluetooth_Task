package com.example.data.peripheral.source.remote


import android.app.Application
import android.bluetooth.*
import com.example.core.ble.BleExt
import com.example.data.peripheral.source.remote.model.PeripheralGattModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

class PeripheralGattDataSource @Inject constructor(
    private val context: Application,
    private val bluetoothManager: BluetoothManager,
) {

    private val gattState: MutableStateFlow<PeripheralGattModel> =
        MutableStateFlow(PeripheralGattModel())

    fun state() = gattState.asStateFlow()
    var readMessage: String = ""

    var gattServer: BluetoothGattServer? = null

    private val subscribedDevices = mutableSetOf<BluetoothDevice>()

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            gattState.update { it.copy(connectionState = newState) }

            if (newState != BluetoothProfile.STATE_CONNECTED) {
                subscribedDevices.remove(device)
                gattState.update { it.copy(subscribedDevices = subscribedDevices) }
            }
        }

        override fun onNotificationSent(device: BluetoothDevice, status: Int) {
            appendLog("onNotificationSent status=$status")
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            var log = "onCharacteristicRead offset=$offset"
            if (characteristic.uuid == UUID.fromString(BleExt.CHAR_FOR_READ_UUID)) {
                val strValue = readMessage
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    strValue.toByteArray(Charsets.UTF_8)
                )
                log += "\nresponse=success, value=\"$strValue\""
                appendLog(log)
            } else {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                log += "\nresponse=failure, unknown UUID\n${characteristic.uuid}"
                appendLog(log)
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            var log =
                "onCharacteristicWrite offset=$offset responseNeeded=$responseNeeded preparedWrite=$preparedWrite"
            if (characteristic.uuid == UUID.fromString(BleExt.CHAR_FOR_WRITE_UUID)) {
                val strValue = value?.toString(Charsets.UTF_8) ?: ""
                log += if (responseNeeded) {
                    gattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        strValue.toByteArray(Charsets.UTF_8)
                    )
                    "\nresponse=success, value=\"$strValue\""
                } else {
                    "\nresponse=notNeeded, value=\"$strValue\""
                }
                gattState.update { it.copy(write = strValue) }
            } else {
                log += if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                    "\nresponse=failure, unknown UUID\n${characteristic.uuid}"
                } else {
                    "\nresponse=notNeeded, unknown UUID\n${characteristic.uuid}"
                }
            }
            appendLog(log)
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor
        ) {
            var log = "onDescriptorReadRequest"
            if (descriptor.uuid == UUID.fromString(BleExt.CCC_DESCRIPTOR_UUID)) {
                val returnValue = if (subscribedDevices.contains(device)) {
                    log += " CCCD response=ENABLE_NOTIFICATION"
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                } else {
                    log += " CCCD response=DISABLE_NOTIFICATION"
                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                }
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    returnValue
                )
            } else {
                log += " unknown uuid=${descriptor.uuid}"
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
            }
            appendLog(log)
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            var strLog = "onDescriptorWriteRequest"
            if (descriptor.uuid == UUID.fromString(BleExt.CCC_DESCRIPTOR_UUID)) {
                var status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED
                if (descriptor.characteristic.uuid == UUID.fromString(BleExt.CHAR_FOR_INDICATE_UUID)) {
                    if (Arrays.equals(value, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
                        subscribedDevices.add(device)
                        status = BluetoothGatt.GATT_SUCCESS
                        strLog += ", subscribed"
                    } else if (Arrays.equals(
                            value,
                            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                        )
                    ) {
                        subscribedDevices.remove(device)
                        status = BluetoothGatt.GATT_SUCCESS
                        strLog += ", unsubscribed"
                    }
                }
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, status, 0, null)
                }
                gattState.update { it.copy(subscribedDevices = subscribedDevices) }
            } else {
                strLog += " unknown uuid=${descriptor.uuid}"
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                }
            }
            appendLog(strLog)
        }
    }

    val charForIndicate
        get() = gattServer?.getService(UUID.fromString(BleExt.SERVICE_UUID))?.getCharacteristic(
            UUID.fromString(BleExt.CHAR_FOR_INDICATE_UUID)
        )


    fun bleStartGattServer() {
        val gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        val service = BluetoothGattService(
            UUID.fromString(BleExt.SERVICE_UUID),
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        val charForRead = BluetoothGattCharacteristic(
            UUID.fromString(BleExt.CHAR_FOR_READ_UUID),
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        val charForWrite = BluetoothGattCharacteristic(
            UUID.fromString(BleExt.CHAR_FOR_WRITE_UUID),
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        val charForIndicate = BluetoothGattCharacteristic(
            UUID.fromString(BleExt.CHAR_FOR_INDICATE_UUID),
            BluetoothGattCharacteristic.PROPERTY_INDICATE,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        val charConfigDescriptor = BluetoothGattDescriptor(
            UUID.fromString(BleExt.CCC_DESCRIPTOR_UUID),
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        charForIndicate.addDescriptor(charConfigDescriptor)

        service.addCharacteristic(charForRead)
        service.addCharacteristic(charForWrite)
        service.addCharacteristic(charForIndicate)

        val result = gattServer?.addService(service)
        this.gattServer = gattServer
        appendLog(
            "addService " + when (result) {
                true -> "OK"
                else -> "fail"
            }
        )
    }

    fun bleStopGattServer() {
        gattServer?.close()
        gattServer = null
        appendLog("gattServer closed")
        gattState.update { it.copy(connectionState = -1) }
    }

    private fun appendLog(message: String) {
        gattState.update { it.copy(log = message) }
    }

    fun setReadMsg(readMessage: String) {
        this.readMessage = readMessage
    }

}