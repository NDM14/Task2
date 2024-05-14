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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withRunningRecomposer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import java.lang.NumberFormatException

var WEATHER_SERVICE_UUID: String = "00000002-0000-0000-fdfd-fdfdfdfdfdfd"
var FAN_SERVICE_UUID: String = "00000001-0000-0000-fdfd-fdfdfdfdfdfd"
var FAN_CHARA_UUID: UUID = UUID.fromString("10000001-0000-0000-FDFD-FDFDFDFDFDFD")
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
    writeCharacteristic: (UUID, UUID, Int) -> Unit,

) {
    val weatherService = discoveredCharacteristics.get(WEATHER_SERVICE_UUID);
    val fanService = discoveredCharacteristics.get(FAN_SERVICE_UUID);

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
                    }, fontWeight = FontWeight.Light
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

        if (weatherService != null) {
            WeatherDisplay(currentValue, readCharacteristic)
        }

        if (fanService != null) {
            FanControl(writeCharacteristic)
            Log.v("DEVICE", connect.javaClass.typeName)
        }

        OutlinedButton(modifier = Modifier.padding(top = 40.dp), onClick = unselectDevice) {
            Text("Disconnect")
        }
    }
}

@Composable
fun WeatherDisplay(currentValue: String?, readCharacteristic: (UUID, UUID) -> Unit) {
    Button(onClick = { readCharacteristic(UUID.fromString(WEATHER_SERVICE_UUID), TEMP_UUID) }) {
        Text("Read Temperature")
    }
    Button(onClick = { readCharacteristic(UUID.fromString(WEATHER_SERVICE_UUID), HUM_UUID) }) {
        Text("Read Humidity")
    }

    if (currentValue != null) {
        Text("Last value: $currentValue")
        Log.i("Last value", currentValue)
    }
}

@Composable
fun FanControl(writeCharacteristic: (UUID, UUID, Int) -> Unit) {
    var text by remember { mutableStateOf("") }

    var textAsInt: Int? = null;

    try {
        textAsInt = text.toInt();
    } catch (_: NumberFormatException) {
    }
    TextField(
        value = text,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        onValueChange = { text = it },
        label = { Text("Fan speed") })

    Button(
        enabled = textAsInt != null,
        onClick = {
            if (textAsInt != null) {
                writeCharacteristic(
                    UUID.fromString(FAN_SERVICE_UUID),
                    FAN_CHARA_UUID,
                    textAsInt
                )

            }
        }) {
        Text("Set speed")
    }


}

@Preview
@Composable
fun PreviewDeviceScreen() {
    DeviceScreen(
        currentValue = "0",
        connect = {},
        isDeviceConnected = true,
        unselectDevice = {},
        discoveredCharacteristics = mapOf(),
        discoverServices = {},
        readCharacteristic = { a, b -> },
        writeCharacteristic = { a, b, c -> })
}