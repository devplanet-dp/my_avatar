package com.devplanet.myavatar

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    // JVMField needed!
    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun testTakeScreenshot() {
        activityRule.launchActivity(null)
        //prepares to take a screenshot of the app
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

        Espresso.onView(ViewMatchers.withId(R.id.generateButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //Takes screenshot of the first screen
        Screengrab.screenshot("myAvatar_before_click")

        //Trigger the generate button onClick function
        Espresso.onView(ViewMatchers.withId(R.id.generateButton))
            .perform(ViewActions.click())

        //Takes another screenshot
        Screengrab.screenshot("myAvatar_after_click")
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.devplanet.myavatar", appContext.packageName)
    }
}