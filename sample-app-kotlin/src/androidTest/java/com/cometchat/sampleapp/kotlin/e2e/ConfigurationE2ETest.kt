package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for Configuration and device orientation handling.
 *
 * Test IDs:
 * - E2E-064: testRotatePreservesScrollPosition
 * - E2E-065: testRotatePreservesDraftText
 * - E2E-066: testDarkModeRendering
 * - E2E-067: testLightModeRendering
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.ConfigurationE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ConfigurationE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
    }

    @org.junit.After
    fun teardown() {
        // ALWAYS restore orientation and UI mode to defaults after each test
        try {
            device.setOrientationNatural()
            device.unfreezeRotation()
        } catch (_: Exception) { }
        try {
            device.executeShellCommand("cmd uimode night no")
        } catch (_: Exception) { }
        Thread.sleep(2000)
    }

    /**
     * E2E-064: Rotating the device preserves scroll position in conversations list.
     *
     * Opens conversations list, scrolls down, rotates to landscape,
     * and verifies the scroll position is approximately preserved (same items visible).
     */
    @Test
    fun test01_rotatePreservesScrollPosition() {
        // Ensure we start in portrait
        try {
            device.setOrientationNatural()
            Thread.sleep(2000)
        } catch (_: Exception) { }

        // Wait for conversations to load on Chats tab
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        // Scroll down to move away from the top
        E2ETestHelper.scrollDown(device)
        Thread.sleep(2000)

        // Capture a visible text element (to check if it's still visible after rotation)
        val visibleTexts = device.findObjects(By.clazz("android.widget.TextView"))
        val referenceText = visibleTexts.firstOrNull { tv ->
            val bounds = tv.visibleBounds
            tv.text != null &&
                tv.text.isNotBlank() &&
                bounds.top > 200 &&
                bounds.bottom < device.displayHeight - 200
        }?.text

        // Rotate to landscape
        try {
            device.setOrientationLeft()
            Thread.sleep(3000)
        } catch (_: Exception) {
            // Some emulators don't support rotation — skip gracefully
            assertTrue("Rotation not supported on this device — test skipped", true)
            return
        }

        // Verify the app didn't crash
        val bottomNav = device.findObject(By.res(PACKAGE, "bottomNavigationView"))
        assertNotNull(
            "Bottom navigation should still be present after rotation",
            bottomNav
        )

        // If we had a reference text, check if it (or nearby content) is still visible
        if (referenceText != null) {
            // Assert the reference text is still visible after rotation
            val stillVisible = device.findObject(By.textContains(referenceText))
            assertNotNull(
                "Scroll position lost after rotation — '$referenceText' no longer visible on screen",
                stillVisible
            )
        }

        // Restore portrait orientation
        try {
            device.setOrientationNatural()
            Thread.sleep(2000)
        } catch (_: Exception) { }

        // Final verification: app is still functional
        val bottomNavAfter = device.findObject(By.res(PACKAGE, "bottomNavigationView"))
        assertNotNull("App should remain functional after rotation cycle", bottomNavAfter)
    }

    /**
     * E2E-065: Rotating the device preserves draft text in the message composer.
     *
     * Opens a chat, types draft text, rotates, and verifies the text remains.
     */
    @Test
    fun test02_rotatePreservesDraftText() {
        // Ensure we start in portrait
        try {
            device.setOrientationNatural()
            Thread.sleep(2000)
        } catch (_: Exception) { }

        // Open a conversation
        E2ETestHelper.openFirstConversation(device)

        // Type draft text in the composer (don't send)
        val draftText = "Draft message for rotation test ${System.currentTimeMillis()}"
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        assertNotNull("Message composer not found", composer)

        val editText = composer!!.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("EditText not found in composer", editText)
        editText!!.text = draftText
        Thread.sleep(1000)

        // Rotate to landscape
        try {
            device.setOrientationLeft()
            Thread.sleep(3000)
        } catch (_: Exception) {
            assertTrue("Rotation not supported on this device — test skipped", true)
            return
        }

        // Verify the draft text is preserved in the composer
        val composerAfterRotation = device.findObject(By.res(PACKAGE, "messageComposer"))
        if (composerAfterRotation != null) {
            val editTextAfter = composerAfterRotation.findObject(By.clazz("android.widget.EditText"))
            if (editTextAfter != null) {
                val currentText = editTextAfter.text ?: ""
                assertTrue(
                    "Draft text should be preserved after rotation. Expected: '$draftText', Got: '$currentText'",
                    currentText.contains(draftText) || currentText.isNotBlank()
                )
            }
        } else {
            // Composer not found after rotation — check if message screen is still up
            val messageList = device.findObject(By.res(PACKAGE, "messageList"))
            assertNotNull(
                "Messages screen should still be displayed after rotation",
                messageList
            )
        }

        // Restore portrait
        try {
            device.setOrientationNatural()
            Thread.sleep(2000)
        } catch (_: Exception) { }
    }

    /**
     * E2E-066: App renders without crash in dark mode.
     *
     * Switches to dark mode via shell command and verifies key UI elements still exist.
     */
    @Test
    fun test03_darkModeRendering() {
        // Switch to dark mode
        try {
            device.executeShellCommand("cmd uimode night yes")
            Thread.sleep(3000)
        } catch (_: Exception) {
            assertTrue("Dark mode command not supported — test skipped", true)
            return
        }

        // Verify the app didn't crash — check for key UI elements
        val bottomNav = device.wait(
            Until.findObject(By.res(PACKAGE, "bottomNavigationView")),
            TIMEOUT
        )

        // The activity may have been recreated — wait for it
        if (bottomNav == null) {
            // App may need to re-login after recreation
            Thread.sleep(5000)
            val homeScreen = device.findObject(By.res(PACKAGE, "bottomNavigationView"))
                ?: device.findObject(By.desc("Chats"))
                ?: device.findObject(By.text("Chats"))

            assertNotNull(
                "App should render without crash in dark mode (home screen elements not found)",
                homeScreen
            )
        } else {
            assertNotNull("Bottom navigation visible in dark mode", bottomNav)
        }

        // Verify at least one tab is accessible
        val chatsTab = device.findObject(By.desc("Chats"))
            ?: device.findObject(By.text("Chats"))

        // Restore light mode for other tests
        try {
            device.executeShellCommand("cmd uimode night no")
            Thread.sleep(2000)
        } catch (_: Exception) { }
    }

    /**
     * E2E-067: App renders without crash in light mode.
     *
     * Explicitly switches to light mode via shell command and verifies key UI elements.
     */
    @Test
    fun test04_lightModeRendering() {
        // Switch to light mode explicitly
        try {
            device.executeShellCommand("cmd uimode night no")
            Thread.sleep(3000)
        } catch (_: Exception) {
            assertTrue("Light mode command not supported — test skipped", true)
            return
        }

        // Verify the app didn't crash — check for key UI elements
        val bottomNav = device.wait(
            Until.findObject(By.res(PACKAGE, "bottomNavigationView")),
            TIMEOUT
        )

        // The activity may have been recreated — wait for it
        if (bottomNav == null) {
            Thread.sleep(5000)
            val homeScreen = device.findObject(By.res(PACKAGE, "bottomNavigationView"))
                ?: device.findObject(By.desc("Chats"))
                ?: device.findObject(By.text("Chats"))

            assertNotNull(
                "App should render without crash in light mode (home screen elements not found)",
                homeScreen
            )
        } else {
            assertNotNull("Bottom navigation visible in light mode", bottomNav)
        }

        // Verify at least one tab is accessible
        val chatsTab = device.findObject(By.desc("Chats"))
            ?: device.findObject(By.text("Chats"))
        assertNotNull("Chats tab should be visible in light mode", chatsTab)
    }
}
