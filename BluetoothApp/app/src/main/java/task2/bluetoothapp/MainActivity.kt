package task2.bluetoothapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import task2.bluetoothapp.ui.navigation.Navigation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //The whole part of the BLE-connection, -scanning and the permissions are taken from
            //this tutorial: https://proandroiddev.com/android-bluetooth-and-ble-the-modern-way-a-complete-guide-4e95138998a0
            //and this repository: https://github.com/tdcolvin/BLEClient/tree/master
            Navigation()
        }
    }
}