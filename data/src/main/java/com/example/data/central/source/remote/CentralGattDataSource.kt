package com.example.data.central.source.remote

import android.bluetooth.*
import android.os.Handler
import android.os.Looper
import com.example.core.ble.BLELifecycleState
import com.example.core.ble.BleExt
import com.example.core.ble.isReadable
import com.example.core.ble.isWriteable
import com.example.data.central.source.remote.model.CentralGattModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject

class CentralGattDataSource @Inject constructor() {

    private val gattState: MutableStateFlow<CentralGattModel> =
        MutableStateFlow(CentralGattModel.Initial)

    fun state() = gattState.asStateFlow()

    var connectedGatt: BluetoothGatt? = null
    var characteristicForRead: BluetoothGattCharacteristic? = null
    var characteristicForWrite: BluetoothGattCharacteristic? = null
    var characteristicForIndicate: BluetoothGattCharacteristic? = null

    val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            // TODO: timeout timer: if this callback not called - disconnect(), wait 120ms, close()

            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    appendLog("Connected to $deviceAddress")

                    // TODO: bonding state

                    // recommended on UI thread https://punchthrough.com/android-ble-guide/
                    Handler(Looper.getMainLooper()).post {
                        gattState.value =
                            CentralGattModel.ConnectionLifeCycle(BLELifecycleState.ConnectedDiscovering)

                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    appendLog("Disconnected from $deviceAddress")
                    setConnectedGattToNull()
                    gatt.close()
                    gattState.value =
                        CentralGattModel.ConnectionLifeCycle(BLELifecycleState.Disconnected)
                    gattState.value = CentralGattModel.RestartLifecycle
                }
            } else {
                // TODO: random error 133 - close() and try reconnect

                appendLog("ERROR: onConnectionStateChange status=$status deviceAddress=$deviceAddress, disconnecting")

                setConnectedGattToNull()
                gatt.close()
                gattState.value =
                    CentralGattModel.ConnectionLifeCycle(BLELifecycleState.Disconnected)
                gattState.value = CentralGattModel.RestartLifecycle
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            appendLog("onServicesDiscovered services.count=${gatt.services.size} status=$status")

            if (status == 129 /*GATT_INTERNAL_ERROR*/) {
                // it should be a rare case, this article recommends to disconnect:
                // https://medium.com/@martijn.van.welie/making-android-ble-work-part-2-47a3cdaade07
                appendLog("ERROR: status=129 (GATT_INTERNAL_ERROR), disconnecting")
                gatt.disconnect()
                return
            }

            val service = gatt.getService(UUID.fromString(BleExt.SERVICE_UUID)) ?: run {
                appendLog("ERROR: Service not found ${BleExt.SERVICE_UUID}, disconnecting")
                gatt.disconnect()
                return
            }

            connectedGatt = gatt
            characteristicForRead =
                service.getCharacteristic(UUID.fromString(BleExt.CHAR_FOR_READ_UUID))
            characteristicForWrite =
                service.getCharacteristic(UUID.fromString(BleExt.CHAR_FOR_WRITE_UUID))
            characteristicForIndicate =
                service.getCharacteristic(UUID.fromString(BleExt.CHAR_FOR_INDICATE_UUID))

            characteristicForIndicate?.let {
                gattState.value =
                    CentralGattModel.ConnectionLifeCycle(BLELifecycleState.ConnectedSubscribing)
                subscribeToIndications(it, gatt)
            } ?: run {
                appendLog("WARN: characteristic not found ${BleExt.CHAR_FOR_INDICATE_UUID}")
                gattState.value = CentralGattModel.ConnectionLifeCycle(BLELifecycleState.Connected)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (characteristic.uuid == UUID.fromString(BleExt.CHAR_FOR_READ_UUID)) {
                val strValue = characteristic.value?.toString(Charsets.UTF_8)?:""
                val log = "onCharacteristicRead " + when (status) {
                    BluetoothGatt.GATT_SUCCESS -> "OK, value=\"$strValue\""
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> "not allowed"
                    else -> "error $status"
                }
                appendLog(log)
                gattState.value = CentralGattModel.Read(strValue)
            } else {
                appendLog("onCharacteristicRead unknown uuid $characteristic.uuid")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (characteristic.uuid == UUID.fromString(BleExt.CHAR_FOR_WRITE_UUID)) {
                val log: String = "onCharacteristicWrite " + when (status) {
                    BluetoothGatt.GATT_SUCCESS -> "OK"
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "not allowed"
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> "invalid length"
                    else -> "error $status"
                }
                appendLog(log)
            } else {
                appendLog("onCharacteristicWrite unknown uuid $characteristic.uuid")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == UUID.fromString(BleExt.CHAR_FOR_INDICATE_UUID)) {
                val strValue = characteristic.value.toString(Charsets.UTF_8)
                appendLog("onCharacteristicChanged value=\"$strValue\"")
                gattState.value = CentralGattModel.Indicate(strValue)
            } else {
                appendLog("onCharacteristicChanged unknown uuid $characteristic.uuid")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (descriptor.characteristic.uuid == UUID.fromString(BleExt.CHAR_FOR_INDICATE_UUID)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val value = descriptor.value
                    val isSubscribed = value.isNotEmpty() && value[0].toInt() != 0
                    when (isSubscribed) {
                        true -> gattState.value =
                            CentralGattModel.ConnectionLifeCycle(BLELifecycleState.Connected)
                        false -> gattState.value =
                            CentralGattModel.ConnectionLifeCycle(BLELifecycleState.Disconnected)
                    }
                } else {
                    appendLog("ERROR: onDescriptorWrite status=$status uuid=${descriptor.uuid} char=${descriptor.characteristic.uuid}")
                }

                // subscription processed, consider connection is ready for use
                gattState.value = CentralGattModel.ConnectionLifeCycle(BLELifecycleState.Connected)
            } else {
                appendLog("onDescriptorWrite unknown uuid $descriptor.characteristic.uuid")
            }
        }
    }

    private fun appendLog(message: String) {
        gattState.value = CentralGattModel.Log(message)
    }

    private fun subscribeToIndications(
        characteristic: BluetoothGattCharacteristic,
        gatt: BluetoothGatt
    ) {
        val cccdUuid = UUID.fromString(BleExt.CCC_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                appendLog("ERROR: setNotification(true) failed for ${characteristic.uuid}")
                return
            }
            cccDescriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            gatt.writeDescriptor(cccDescriptor)
        }
    }

    fun onTapRead() {
        connectedGatt?.let { gatt ->
            characteristicForRead?.let {
                if (!it.isReadable()) {
                    appendLog("ERROR: read failed, characteristic not readable ${BleExt.CHAR_FOR_READ_UUID}")
                } else {
                    gatt.readCharacteristic(it)
                }
            }
                ?: appendLog("ERROR: read failed, characteristic unavailable ${BleExt.CHAR_FOR_READ_UUID}")
        } ?: appendLog("ERROR: read failed, no connected device")
    }

    fun onTapWrite(message: ByteArray) {
        connectedGatt?.let { gatt ->
            characteristicForWrite?.let {
                if (!it.isWriteable()) {
                    appendLog("ERROR: write failed, characteristic not writeable ${BleExt.CHAR_FOR_WRITE_UUID}")
                } else {
                    it.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    it.value = message
                    gatt.writeCharacteristic(it)
                }
            }
                ?: appendLog("ERROR: write failed, characteristic unavailable ${BleExt.CHAR_FOR_WRITE_UUID}")
        } ?: appendLog("ERROR: write failed, no connected device")
    }

    private fun unsubscribeFromCharacteristic(characteristic: BluetoothGattCharacteristic) {
        val gatt = connectedGatt ?: return

        val cccdUuid = UUID.fromString(BleExt.CCC_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (!gatt.setCharacteristicNotification(characteristic, false)) {
                appendLog("ERROR: setNotification(false) failed for ${characteristic.uuid}")
                return
            }
            cccDescriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(cccDescriptor)
        }
    }

    fun setConnectedGattToNull() {
        connectedGatt = null
        characteristicForRead = null
        characteristicForWrite = null
        characteristicForIndicate = null
    }

    fun isGattNotInitialized() = connectedGatt == null

    fun disconnectGatt() = connectedGatt?.disconnect()

    fun closeGatt() = connectedGatt?.close()
}