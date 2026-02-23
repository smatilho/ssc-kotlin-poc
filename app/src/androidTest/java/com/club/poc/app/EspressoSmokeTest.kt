package com.club.poc.app

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onIdle
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EspressoSmokeTest {
    @Test
    fun rootViewVisible() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestDeviceHarness.resetAppAndLaunch(device)

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onIdle()
            val appVisible = device.wait(Until.hasObject(By.pkg(TestDeviceHarness.AppPackage)), 10_000)
            var decorVisible = false
            scenario.onActivity { activity ->
                decorVisible = activity.window?.decorView?.isShown == true
            }
            assertTrue("Expected app package visible", appVisible)
            assertTrue("Expected activity decor view visible", decorVisible)
        }
    }
}
