package task2.bluetoothapp.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import task2.bluetoothapp.ui.viewmodel.BLEViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import task2.bluetoothapp.ui.screens.PermissionsRequiredScreen
import task2.bluetoothapp.ui.screens.haveAllPermissions
import task2.bluetoothapp.ui.screens.DeviceScreen
import task2.bluetoothapp.ui.screens.ScanningScreen

@SuppressLint("MissingPermission")
@Composable
fun Navigation(viewModel: BLEViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var allPermissionsGranted by remember {
        mutableStateOf (haveAllPermissions(context))
    }

    if (!allPermissionsGranted){
        PermissionsRequiredScreen { allPermissionsGranted = true }
    }
    else if (uiState.activeDevice == null) {
        ScanningScreen(
            isScanning = uiState.isScanning,
            foundDevices = uiState.foundDevices,
            startScanning = viewModel::startScanning,
            stopScanning = viewModel::stopScanning,
            selectDevice = { device ->
                viewModel.stopScanning()
                viewModel.setActiveDevice(device)
            }
        )
    }
    else {
        DeviceScreen(
            unselectDevice = {
                viewModel.disconnectActiveDevice()
                viewModel.setActiveDevice(null)
            },
            isDeviceConnected = uiState.isDeviceConnected,
            discoveredCharacteristics = uiState.discoveredCharacteristics,
            password = uiState.password,
            nameWrittenTimes = uiState.nameWrittenTimes,
            connect = viewModel::connectActiveDevice,
            discoverServices = viewModel::discoverActiveDeviceServices,
            readPassword = viewModel::readPasswordFromActiveDevice,
            writeName = viewModel::writeNameToActiveDevice
        )
    }

}