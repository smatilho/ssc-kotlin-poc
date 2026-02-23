package com.club.poc.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiAutomatorSmokeTest {
    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
        TestDeviceHarness.resetAppAndLaunch(device)
    }

    @Test
    fun inviteScreenVisible() {
        val hasInviteText = device.wait(Until.hasObject(By.textContains("Invite-only")), 15_000)
        assertTrue("Expected invite text on launch screen", hasInviteText)
    }

    @Test
    fun canNavigateInviteToProfileAndBackHome() {
        tapByText(device, "Accept Invite and Continue")
        tapByText(device, "Continue to Club Home")
        tapByText(device, "Profile / Role Toggles")
        assertTrue(
            "Expected profile heading",
            device.wait(Until.hasObject(By.text("Role & Feature Controls")), 10_000),
        )
        tapByText(device, "Back to Home")
        assertTrue(
            "Expected home heading",
            device.wait(Until.hasObject(By.text("North Ridge Alpine Club")), 10_000),
        )
    }

    @Test
    fun canNavigateHomeToBookingAndBack() {
        tapByText(device, "Accept Invite and Continue")
        tapByText(device, "Continue to Club Home")
        tapByText(device, "Booking")
        assertTrue(
            "Expected booking heading",
            device.wait(Until.hasObject(By.text("Bed Booking Checkout")), 10_000),
        )
        device.pressBack()
        assertTrue(
            "Expected home heading",
            device.wait(Until.hasObject(By.text("North Ridge Alpine Club")), 10_000),
        )
    }

    private fun tapByText(device: UiDevice, text: String) {
        val target: UiObject2 = device.wait(Until.findObject(By.text(text)), 10_000)
            ?: throw AssertionError("Expected tappable text: $text")
        target.click()
    }
}
