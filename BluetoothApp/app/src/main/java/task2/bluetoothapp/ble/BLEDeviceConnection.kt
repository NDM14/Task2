package task2.bluetoothapp.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID
//https://github.com/MatthiasKerat/BLETutorialYt/tree/FinalApp/app/src/main/java/com/example/bletutorial/data/ble
//https://www.youtube.com/watch?v=qyG-SDfYNBE&t=1719s
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

        @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.w("bt", "onCharacteristicRead");
            super.onCharacteristicRead(gatt, characteristic, status)

            // if branch: characteristic is temperature measurement
            // else branch: characteristic is humidity
            if (characteristic.getUuid().toString()=="00002a1c-0000-1000-8000-00805f9b34fb"){
                val flags = characteristic.getIntValue(FORMAT_UINT8, 0).toString()
                val unformattedTemp =characteristic.getIntValue(FORMAT_UINT16, 1).toString()
                val formattedTemp = unformattedTemp.replaceRange(2,2,".") + " °C"
                currentValue.value = formattedTemp
                Log.i("flags", flags)
                Log.i("formatted temp", formattedTemp)
                Log.i("Uuid value", characteristic.getUuid().toString())
            } else {
                val unformattedHum =characteristic.getIntValue(FORMAT_UINT16, 0).toString()
                val formattedHum = unformattedHum.replaceRange(2,2,".") + " %"
                currentValue.value = formattedHum
                Log.i("formatted hum", formattedHum)
                Log.i("Uuid value", characteristic.getUuid().toString())
            }
        }

        @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.i("bt","onCharacteristicChanged")

            super.onCharacteristicChanged(gatt, characteristic)
            // if branch: characteristic is temperature measurement
            // else branch: characteristic is humidity
            if (characteristic.getUuid().toString()=="00002a1c-0000-1000-8000-00805f9b34fb"){
                val flags = characteristic.getIntValue(FORMAT_UINT8, 0).toString()
                val unformattedTemp = characteristic.getIntValue(FORMAT_UINT16, 1).toString()
                val formattedTemp = unformattedTemp.replaceRange(2,2,".") + " °C"
                currentValue.value = formattedTemp
                Log.i("formatted temp", formattedTemp)
            } else {
                val unformattedHum = characteristic.getIntValue(FORMAT_UINT16, 0).toString()
                val formattedHum = unformattedHum.replaceRange(2,2,".") + " %"
                currentValue.value = formattedHum
                Log.i("formatted hum", formattedHum)
            }
        }

        @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,

        ) {
            Log.w("bt", "onCharacteristicWrite")
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.v("bt", characteristic.properties.toString())

        }

        // checks whether the subscription to the characteristic was successful
        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            Log.w("bt", "onDescriptorWrite")
            super.onDescriptorWrite(gatt, descriptor, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.v("bt", "Descriptor ${descriptor.uuid} of characteristic ${descriptor.characteristic.uuid}: write success")
            }
            else {
                Log.v("bt", "Descriptor ${descriptor.uuid} of characteristic ${descriptor.characteristic.uuid}: write fail (status=$status)")
            }
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
        val s = gatt?.getService(service)
        val c = s?.getCharacteristic(characteristic)

        if (c != null) {
            // use these 2 lines to update the values whenever the button is clicked
            //val success = gatt?.readCharacteristic(c)
            //Log.v("bluetooth", "Read status: $success")

            // use these lines to update the values whenever they change (doesn't work yet)
            gatt?.setCharacteristicNotification(c, true)
            val CLIENT_CONFIG_DESCRIPTOR = characteristic
            Log.i("bt", "descriptor: $CLIENT_CONFIG_DESCRIPTOR")
            val desc = c.getDescriptor(CLIENT_CONFIG_DESCRIPTOR)
            Log.i("bt", "desc: $desc")
            desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            gatt?.writeDescriptor(desc)
            Log.i("bt","wrote descriptor")
        }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun writeCharacteristic(service: UUID, characteristic: UUID, value: Int) {
        val c = gatt?.getService(service)?.getCharacteristic(characteristic)
        if (c != null) {
            c.setValue(value, FORMAT_UINT16, 0)
            val success = gatt?.writeCharacteristic(c)
            Log.v("bt", "Write status: $success")
        }
    }

    fun readPassword() {
        //TODO
    }
}