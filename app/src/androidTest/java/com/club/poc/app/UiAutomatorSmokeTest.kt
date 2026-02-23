package com.club.poc.app

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiAutomatorSmokeTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun inviteScreenVisible() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val hasInviteText = device.wait(Until.hasObject(By.textContains("Invite-only")), 5_000)
        assertTrue("Expected invite text on launch screen", hasInviteText)
    }

    @Test
    fun canNavigateInviteToProfileAndBackHome() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)

        tapIfVisible(device, "Accept Invite and Continue")
        tapIfVisible(device, "Continue to Club Home")
        tapByText(device, "Profile / Role Toggles")
        assertTrue(
            "Expected profile heading",
            device.wait(Until.hasObject(By.text("Role & Feature Controls")), 5_000),
        )
        tapByText(device, "Back to Home")
        assertTrue(
            "Expected home heading",
            device.wait(Until.hasObject(By.text("North Ridge Alpine Club")), 5_000),
        )
    }

    @Test
    fun canNavigateHomeToBookingAndBack() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)

        tapIfVisible(device, "Accept Invite and Continue")
        tapIfVisible(device, "Continue to Club Home")
        tapByText(device, "Booking")
        assertTrue(
            "Expected booking heading",
            device.wait(Until.hasObject(By.text("Bed Booking Checkout")), 5_000),
        )
        device.pressBack()
        assertTrue(
            "Expected home heading",
            device.wait(Until.hasObject(By.text("North Ridge Alpine Club")), 5_000),
        )
    }

    private fun tapIfVisible(device: UiDevice, text: String) {
        val target = device.wait(Until.findObject(By.text(text)), 1_500)
        target?.click()
    }

    private fun tapByText(device: UiDevice, text: String) {
        val target: UiObject2 = device.wait(Until.findObject(By.text(text)), 5_000)
            ?: throw AssertionError("Expected tappable text: $text")
        target.click()
    }
}
