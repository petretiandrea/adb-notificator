package io.github.notificator.model

import com.android.ddmlib.IDevice
import io.github.notificator.model.Adb.Device

object Adb {


    data class Device(val serial: String, val name: String, val isRoot: Boolean) {
        companion object {
            fun fromDebugDevice(device: IDevice) = Device(device.serialNumber, device.name, device.isRoot)
        }
    }


    data class DebugProcess(val pid: Int, val packageName: String?, val clientDescription: String?) {
        fun clientKey() : String {
            return "$packageName$clientDescription"
        }
    }


    data class NotificationData(val serialDevice: String, val packageName: String)

}