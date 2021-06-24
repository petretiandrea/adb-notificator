package io.github.notificator

import com.intellij.openapi.project.Project
import io.github.notificator.model.Adb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class AdbNotificatorViewModel(val project: Project, private val adbController: AdbController) {

    private var availableDevicesFlow = MutableStateFlow<List<Adb.Device>>(listOf())
    private var attacheDeviceFlow = MutableStateFlow<AttachResult>(NoAttachedDevice)


    val availableDevices: Flow<List<Adb.Device>> = adbController.availableDevices.map { it.map { Adb.Device.fromDebugDevice(it) } }
    val attachedDevice: Flow<AttachResult> = attacheDeviceFlow


    init {

    }

    fun onSelectDevice(deviceSerial: String) {
        if(deviceSerial.isNotEmpty()) {
            attacheDeviceFlow.value = adbController.attachDevice(deviceSerial)
        }
    }

    fun onSendPushClicked(jsonContent: String, deviceSerial: String, processPid: String) {

    }

}