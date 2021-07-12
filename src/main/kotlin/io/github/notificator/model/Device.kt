package io.github.notificator.model

import com.android.ddmlib.IDevice

interface Device {
    val serial: String
    val name: String
    val isRoot: Boolean
    val processes: List<DebugProcess>

    companion object {
        fun fromDebugDevice(device: IDevice): Device = when(device.state) {
            IDevice.DeviceState.ONLINE -> DeviceReady(device.serialNumber, device.name, device.isRoot)
            else -> DeviceNotReady
        }
    }

    data class DeviceReady(
        override val serial: String,
        override val name: String,
        override val isRoot: Boolean,
        override val processes: List<DebugProcess> = emptyList()) : Device

    object DeviceNotReady : Device {
        override val serial: String = ""
        override val name: String = ""
        override val isRoot: Boolean = false
        override val processes: List<DebugProcess> = emptyList()
    }
}

data class DebugProcess(val pid: Int, val packageName: String?, val clientDescription: String?) {
    fun clientKey() : String {
        return "$packageName$clientDescription"
    }
}