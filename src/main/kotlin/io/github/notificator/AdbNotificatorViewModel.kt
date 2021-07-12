package io.github.notificator

import com.intellij.openapi.project.Project
import io.github.notificator.model.Device
import io.github.notificator.model.NotificationField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdbNotificatorViewModel(val project: Project, private val adbController: AdbController) {

    private val _availableDevice = MutableStateFlow<List<Device>>(emptyList())
    val availableDevice: StateFlow<List<Device>> = _availableDevice

    private var _selectedDevice = MutableStateFlow<Device?>(null);
    val selectedDevice: Flow<Device?> = _selectedDevice

    init {
        GlobalScope.launch(Dispatchers.IO) {
            adbController.availableDevices
                .map { devices -> devices.filterIsInstance<Device.DeviceReady>() }
                .collect {
                    _availableDevice.value = it
                }
        }
    }

    fun onSelectDevice(deviceSerial: String) {
        if(deviceSerial.isNotEmpty()) {
            _selectedDevice.value = availableDevice.value
                .filterIsInstance<Device.DeviceReady>()
                .find { it.serial == deviceSerial }
                ?.copy(
                    processes = adbController.processesOfDevice(deviceSerial)
                )
        }
    }

    suspend fun sendPushNotification(fields: List<NotificationField>) : Boolean {
        delay(3000)
        return true
    }

    suspend fun enableDeviceRoot() {
        _selectedDevice.value?.let {
            adbController.enableDeviceRoot(it.serial)
        }
    }

    fun onSendPushClicked(jsonContent: String, deviceSerial: String, processPid: String) {

    }

}