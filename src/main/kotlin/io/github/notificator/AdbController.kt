package io.github.notificator

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.intellij.openapi.project.Project
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import org.jetbrains.android.sdk.AndroidSdkUtils
import org.jetbrains.annotations.NotNull

data class DebugProcess(val pid: Int, val packageName: String?, val clientDescription: String?) {
    fun clientKey() : String {
        return "$packageName$clientDescription"
    }
}


interface AttachResult
data class AttachedDevice(val device: IDevice, val currentProcess: DebugProcess?, val process: List<DebugProcess>) : AttachResult
object NoAttachedDevice : AttachResult

class AdbController(project: Project) {

    private val availableDeviceFlow: MutableStateFlow<List<IDevice>> =
        MutableStateFlow(listOf())

    val availableDevices: Flow<List<IDevice>> = availableDeviceFlow

    private val deviceChangeCallback = object : AndroidDebugBridge.IDeviceChangeListener {
        override fun deviceConnected(device: IDevice?) {
            device?.let {
                availableDeviceFlow.value = AndroidDebugBridge.getBridge()?.devices.orEmpty().toList()
            }
        }

        override fun deviceDisconnected(device: IDevice?) {
            device?.let {
                availableDeviceFlow.value = AndroidDebugBridge.getBridge()?.devices.orEmpty().toList()
            }
        }

        override fun deviceChanged(device: IDevice?, changeMask: Int) {
            device?.let {
                availableDeviceFlow.value = AndroidDebugBridge.getBridge()?.devices.orEmpty().toList()
            }
        }
    }

    private val debugBridgeChangeCallback = AndroidDebugBridge.IDebugBridgeChangeListener { bridge ->
        val devices = bridge?.devices.orEmpty()
        availableDeviceFlow.value = devices.toList()
    }

    init {
        AndroidDebugBridge.addDeviceChangeListener(deviceChangeCallback)
        AndroidDebugBridge.addDebugBridgeChangeListener(debugBridgeChangeCallback)

        val bridge: AndroidDebugBridge? = AndroidSdkUtils.getDebugBridge(project)
        println("initDeviceList bridge0 ${bridge?.isConnected}")
    }

    fun attachDevice(serial: String): AttachResult {
        val device = availableDeviceFlow.value.find { it.serialNumber == serial }
        val debugProcessList =  device?.clients
            ?.map { it.clientData }
            ?.map { DebugProcess(it.pid, it?.packageName, it?.clientDescription) }
            .orEmpty()
        val defaultSelection: DebugProcess? = debugProcessList.firstOrNull()

        return if (device != null) AttachedDevice(device, defaultSelection, debugProcessList) else NoAttachedDevice
    }

    fun selectDevice(device: IDevice?) {
        device?.let {

        }
    }

    fun sendNotification(deviceSerial: String, processPid: Int, content: Map<String, String>) {
        val payload = content.map { "--es \"${it.key}\" \"${it.value}\"" }.reduce { acc, es -> "$acc $es" }
        // 1. find device using serial
        // 2. find process from device using pid
        val packageAppName = ""
        // 3. send shell command
        val command = "adb shell am broadcast " +
                "-n $packageAppName/com.google.firebase.iid.FirebaseInstanceIdReceiver " +
                "-a \"com.google.android.c2dm.intent.RECEIVE\" " +
                payload

        println(command)
    }

    fun dispose() {
        AndroidDebugBridge.removeDeviceChangeListener(deviceChangeCallback)
        AndroidDebugBridge.removeDebugBridgeChangeListener(debugBridgeChangeCallback)
    }
}