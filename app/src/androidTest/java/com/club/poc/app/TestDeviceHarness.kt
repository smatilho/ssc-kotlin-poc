package com.club.poc.app

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

internal object TestDeviceHarness {
    const val AppPackage = "com.club.poc"
    private const val MainActivityComponent = "com.club.poc/com.club.poc.app.MainActivity"

    fun resetAppAndLaunch(device: UiDevice) {
        wakeAndUnlock(device)
        device.pressHome()
        device.waitForIdle()
        // Do not force-stop/clear the target package here.
        // These tests run inside instrumentation for the same target app process and can
        // terminate the instrumentation process itself. Orchestrator + clearPackageData
        // handles per-test state isolation.
        device.executeShellCommand("am start -W -n $MainActivityComponent")

        check(device.wait(Until.hasObject(By.pkg(AppPackage)), 15_000)) {
            "App package did not appear after launch"
        }
        device.waitForIdle()
    }

    private fun wakeAndUnlock(device: UiDevice) {
        runCatching {
            if (!device.isScreenOn) device.wakeUp()
        }
        runCatching { device.executeShellCommand("input keyevent KEYCODE_WAKEUP") }
        runCatching { device.executeShellCommand("wm dismiss-keyguard") }

        val x = device.displayWidth / 2
        val startY = (device.displayHeight * 8) / 10
        val endY = device.displayHeight / 5
        runCatching { device.swipe(x, startY, x, endY, 20) }
        device.waitForIdle()
    }
}
