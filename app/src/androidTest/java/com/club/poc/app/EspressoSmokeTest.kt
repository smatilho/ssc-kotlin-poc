package com.club.poc.app

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onIdle
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EspressoSmokeTest {
    @Test
    fun rootViewVisible() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onIdle()
            var decorVisible = false
            scenario.onActivity { activity ->
                decorVisible = activity.window?.decorView?.isShown == true
            }
            assertTrue("Expected activity decor view visible", decorVisible)
        }
    }
}
