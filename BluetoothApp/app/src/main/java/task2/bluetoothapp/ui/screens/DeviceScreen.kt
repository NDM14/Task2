package task2.bluetoothapp.ui.screens

import android.os.ParcelUuid

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import android.util.Log

var WEATHER_SERVICE_UUID: String = "00000002-0000-0000-fdfd-fdfdfdfdfdfd"
var FAN_SERVICE_UUID: String = "00000001-0000-0000-fdfd-fdfdfdfdfdfd"
var TEMP_UUID_S: String = "00002a1c-0000-1000-8000-00805f9b34fb"
var TEMP_UUID: UUID = UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb")
var HUM_UUID: UUID = UUID.fromString("00002a6f-0000-1000-8000-00805f9b34fb")
@Composable
fun DeviceScreen(
    unselectDevice: () -> Unit,
    isDeviceConnected: Boolean,
    discoveredCharacteristics: Map<String, List<String>>,
    currentValue: String?,
    connect: () -> Unit,
    discoverServices: () -> Unit,
    readCharacteristic: (UUID, UUID) -> Unit,
    writeCharacteristic: (UUID, UUID, Short) -> Unit,
) {
    val weatherService = discoveredCharacteristics.get(WEATHER_SERVICE_UUID);
    val fanService = discoveredCharacteristics.get(FAN_SERVICE_UUID);
    val temperature = null;
    val humidity = null;

    Column(
        Modifier.scrollable(rememberScrollState(), Orientation.Vertical)
    ) {
        Button(onClick = connect) {
            Text("1. Connect")
        }
        Text("Device connected: $isDeviceConnected")
        Button(onClick = discoverServices, enabled = isDeviceConnected) {
            Text("2. Discover Services")
        }
        LazyColumn {
            items(discoveredCharacteristics.keys.sorted()) { serviceUuid ->
                Text(
                    text = when (serviceUuid) {
                        WEATHER_SERVICE_UUID -> {
                            "[Weather]"
                        }
                        FAN_SERVICE_UUID -> {
                            "[Fan]"
                        }
                        else -> {
                            "[Unknown] $serviceUuid"
                        }
                    }, fontWeight = FontWeight.Black
                )
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    discoveredCharacteristics[serviceUuid]?.forEach {
                        Text(
                            it
                        )
                    }
                }
            }
        }
        Button(onClick = { readCharacteristic(UUID.fromString(WEATHER_SERVICE_UUID), TEMP_UUID) }, enabled = weatherService != null) {
            Text("Read Temperature")
        }
        Button(onClick = { readCharacteristic(UUID.fromString(WEATHER_SERVICE_UUID), HUM_UUID) }, enabled = weatherService != null) {
            Text("Read Humidity")

        }

        if (currentValue != null) {
            Text("Last value: $currentValue" )
            Log.i("Last value", currentValue)
        }

        OutlinedButton(modifier = Modifier.padding(top = 40.dp), onClick = unselectDevice) {
            Text("Disconnect")
        }
    }
}
