package task2.bluetoothapp.ble

import android.R.attr
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.ByteBuffer
import java.util.UUID


@Suppress("DEPRECATION")
class BLEDeviceConnection @RequiresPermission("PERMISSION_BLUETOOTH_CONNECT") constructor(
    private val context: Context,
    private val bluetoothDevice: BluetoothDevice
) {
    val isConnected = MutableStateFlow(false)
    val currentValue = MutableStateFlow<String?>(null)
    val services = MutableStateFlow<List<BluetoothGattService>>(emptyList())

    private val callback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.w("bt", "onConnectionStateChange");
            super.onConnectionStateChange(gatt, status, newState)
            val connected = newState == BluetoothGatt.STATE_CONNECTED
            if (connected) {
                //read the list of services
                services.value = gatt.services
            }
            isConnected.value = connected
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.w("bt", "onServicesDiscovered");
            super.onServicesDiscovered(gatt, status)
            services.value = gatt.services
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.w("bt", "onCharacteristicRead");

            super.onCharacteristicRead(gatt, characteristic, status)
            currentValue.value = String(characteristic.value)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.w("bt", "onCharacteristicWrite");

            super.onCharacteristicWrite(gatt, characteristic, status)

            // TODO
        }
    }

    private var gatt: BluetoothGatt? = null

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun connect() {
        gatt = bluetoothDevice.connectGatt(context, false, callback)
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun discoverServices() {
        gatt?.discoverServices()
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun readCharacteristic(service: UUID, characteristic: UUID) {
        val c = gatt?.getService(service)?.getCharacteristic(characteristic)
        if (c != null) {
            val success = gatt?.readCharacteristic(c)
            Log.v("bluetooth", "Read status: $success")
        }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun writeCharacteristic(service: UUID, characteristic: UUID, value: Short) {
        val c = gatt?.getService(service)?.getCharacteristic(characteristic)
        if (c != null) {
            val buffer = ByteBuffer.allocate(2)
            buffer.putShort(value)

            c.value = buffer.array()
            val success = gatt?.writeCharacteristic(c)
            Log.v("bluetooth", "Write status: $success")
        }
    }

    fun readPassword() {
        TODO("Not yet implemented")
    }
}