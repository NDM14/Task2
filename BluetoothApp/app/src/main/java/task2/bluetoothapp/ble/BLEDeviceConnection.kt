package task2.bluetoothapp.ble

import android.R.attr
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT32
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.ByteBuffer
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

            if (characteristic.getUuid().toString()=="00002a1c-0000-1000-8000-00805f9b34fb"){
                val flags = characteristic.getIntValue(FORMAT_UINT8, 0).toString()
                val unformattedTemp =characteristic.getIntValue(FORMAT_UINT16, 1).toString()
                val formattedTemp = unformattedTemp.replaceRange(2,2,".") + " °C"
                currentValue.value = formattedTemp
                Log.w("flag",characteristic.getIntValue(FORMAT_UINT8, 0).toString())
                Log.w("formatted temp ",formattedTemp)
                Log.w("Uuid value", characteristic.getUuid().toString())

                val CLIENT_CONFIG_DESCRIPTOR = UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb")
                val desc = characteristic.getDescriptor(CLIENT_CONFIG_DESCRIPTOR)
                desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                gatt.writeDescriptor(desc)
            }else {
                val unformattedHum =characteristic.getIntValue(FORMAT_UINT16, 0).toString()
                val formattedHum = unformattedHum.replaceRange(2,2,".") + " %"
                currentValue.value = formattedHum
                Log.w("formatted hum ",formattedHum)
                Log.w("Uuid value", characteristic.getUuid().toString())



            }



        }
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt:BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ){
            Log.i("in characteristic changed","in characteristic changed")

            super.onCharacteristicChanged(gatt, characteristic)

            if (characteristic.getUuid().toString()=="00002a1c-0000-1000-8000-00805f9b34fb"){
                val flags = characteristic.getIntValue(FORMAT_UINT8, 0).toString()
                val unformattedTemp =characteristic.getIntValue(FORMAT_UINT16, 1).toString()
                val formattedTemp = unformattedTemp.replaceRange(2,2,".")
                currentValue.value = formattedTemp
                Log.i("notification",formattedTemp )
            }
            else{
                val unformattedHum =characteristic.getIntValue(FORMAT_UINT16, 0).toString()
                val formattedHum = unformattedHum.replaceRange(2,2,".")
                currentValue.value = formattedHum
                Log.i("formatted hum ",formattedHum)
            }


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

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.v("bluetooth", "Descriptor ${descriptor.uuid} of characteristic ${descriptor.characteristic.uuid}: write success")
            }
            else {
                Log.v("bluetooth", "Descriptor ${descriptor.uuid} of characteristic ${descriptor.characteristic.uuid}: write fail (status=$status)")
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
            val success = gatt?.readCharacteristic(c)
            Log.v("bluetooth", "Read status: $success")

            gatt?.setCharacteristicNotification(c, true)
            val CLIENT_CONFIG_DESCRIPTOR = characteristic
            val desc = c.getDescriptor(CLIENT_CONFIG_DESCRIPTOR)
            desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            gatt?.writeDescriptor(desc)
            Log.i("hum","hum")



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