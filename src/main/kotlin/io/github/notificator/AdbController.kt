package io.github.notificator

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.intellij.openapi.project.Project
import io.github.notificator.model.DebugProcess
import io.github.notificator.model.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.jetbrains.android.sdk.AndroidSdkUtils
import java.io.File

class AdbController(private val adbBridge: AndroidDebugBridge?,
                    private val adbPath: File?) {

    companion object {
        fun fromProject(project: Project): AdbController {
            return AdbController(AndroidSdkUtils.getDebugBridge(project), AndroidSdkUtils.getAdb(project))
        }
    }

    private val _availableDevices: MutableStateFlow<List<Device>> = MutableStateFlow(listOf())
    val availableDevices: Flow<List<Device>> = _availableDevices

    private val deviceChangeCallback = object : AndroidDebugBridge.IDeviceChangeListener {
        override fun deviceConnected(device: IDevice?) {
            device?.let {
                _availableDevices.value = adbBridge?.devices?.map(Device::fromDebugDevice).orEmpty().toList()
            }
        }

        override fun deviceDisconnected(device: IDevice?) {
            device?.let {
                _availableDevices.value = adbBridge?.devices?.map(Device::fromDebugDevice).orEmpty().toList()
            }
        }

        override fun deviceChanged(device: IDevice?, changeMask: Int) {
            println("Device changed ${device?.state}")
            device?.let {
                val update = adbBridge?.devices?.map(Device::fromDebugDevice).orEmpty().toList()
                println("Equals ${update == _availableDevices.value}")
                _availableDevices.value = update
            }
        }
    }
    private val debugBridgeChangeCallback = AndroidDebugBridge.IDebugBridgeChangeListener { bridge ->
        _availableDevices.value = bridge?.devices?.map(Device::fromDebugDevice).orEmpty().toList()
    }

    init {
        AndroidDebugBridge.addDeviceChangeListener(deviceChangeCallback)
        AndroidDebugBridge.addDebugBridgeChangeListener(debugBridgeChangeCallback)

        println("initDeviceList bridge0 ${adbBridge?.isConnected}")
    }

    suspend fun processesOfDevice(deviceSerial: String) : List<DebugProcess> {
        val device = adbBridge?.devices?.find { it.serialNumber == deviceSerial }
        return withContext(Dispatchers.IO) {
            device?.clients
                ?.map { it.clientData }
                ?.map { DebugProcess(it.pid, it?.packageName, it?.clientDescription) }
                .orEmpty()
        }
    }

    suspend fun enableDeviceRoot(deviceSerial: String) : Boolean {
        val device = adbBridge?.devices?.find { it.serialNumber == deviceSerial }
        if (device?.isRoot != true && !adbPath?.absolutePath.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                kotlin.runCatching { Runtime.getRuntime().exec("$adbPath -s $deviceSerial root").onExit().await() }
            }
        }
        return device?.isRoot ?: false
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