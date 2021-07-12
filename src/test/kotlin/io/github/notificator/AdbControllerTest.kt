package io.github.notificator

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File
import java.util.concurrent.TimeUnit

internal class AdbControllerTest {

    private lateinit var bridge: AndroidDebugBridge

    @BeforeEach
    fun setUp() {
        bridge = mockBridge()
    }

    @Test
    fun testDeviceConnected() = runBlocking {
        val controller = AdbController(bridge, File(""));
        val devices = mutableListOf<List<IDevice>>()

        launch { controller.availableDevices.toList(devices) }

        assert(devices.flatten().isEmpty())

        val device = mockDevice("123456789")
        AndroidDebugBridge.deviceConnected(device)

        launch { controller.availableDevices.toList(devices) }

        assert(devices.flatten().size == 1)
    }

    @AfterEach
    fun tearDown() {
    }

    private fun mockBridge() : AndroidDebugBridge {
        return AndroidDebugBridge.getBridge()
    }

    private fun mockDevice(serial: String?) = mockDevice(serial, false)
    private fun mockDeviceWithRoot(serial: String?) = mockDevice(serial, true)

    private fun mockDevice(serial: String?, rootEnabled: Boolean): IDevice {
        return mock {
            on { serialNumber } doReturn serial
            on { isRoot } doReturn rootEnabled
        }
    }
}