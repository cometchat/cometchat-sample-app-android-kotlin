package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for Authentication flows in the Compose sample app.
 *
 * Test IDs:
 * - E2E-001: testValidLoginNavigatesToHome
 * - E2E-002: testInvalidCredentialsShowsError
 * - E2E-003: testLogoutReturnsToLogin
 * - E2E-004: testExistingSessionSkipsLogin
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.AuthenticationE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AuthenticationE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.initSdk()
        E2ETestHelper.logoutIfNeeded()
    }

    /**
     * E2E-001: Valid login with correct UID navigates to Home screen.
     */
    @Test
    fun test01_validLoginNavigatesToHome() {
        E2ETestHelper.launchApp(device)
        E2ETestHelper.loginViaUI(device)

        // Verify Home screen is displayed (bottom nav with "Chats" tab visible)
        val chatsTab = device.findObject(By.desc("Chats"))
            ?: device.findObject(By.text("Chats"))
        assertNotNull("Home screen bottom navigation 'Chats' tab not found after valid login", chatsTab)

        // Verify we are on the Home screen
        assertTrue("Not on Home screen after login", E2ETestHelper.isOnHomeScreen(device))
    }

    /**
     * E2E-002: Invalid UID shows an error (toast or remains on login screen).
     */
    @Test
    fun test02_invalidCredentialsShowsError() {
        E2ETestHelper.launchApp(device)

        // Wait for login screen (Enter UID hint visible)
        val loginReady = device.wait(
            Until.hasObject(By.text("Enter UID")),
            TIMEOUT
        ) || device.wait(
            Until.hasObject(By.text("Continue")),
            SHORT_TIMEOUT
        )
        assertTrue("Login screen did not appear", loginReady)

        // Type an invalid UID using the same strategy as loginViaUI
        val invalidUid = "invalid_uid_xyz_99999"

        var uidField = device.findObject(By.clazz("android.widget.EditText"))
        if (uidField != null) {
            uidField.clear()
            uidField.text = invalidUid
        } else {
            // Click the placeholder to focus the BasicTextField, then use shell input
            val placeholder = device.findObject(By.text("Enter UID"))
                ?: device.findObject(By.textContains("Enter UID"))
            assertNotNull("Enter UID placeholder not found", placeholder)
            placeholder!!.click()
            Thread.sleep(1000)

            uidField = device.findObject(By.clazz("android.widget.EditText"))
            if (uidField != null) {
                uidField.clear()
                uidField.text = invalidUid
            } else {
                device.executeShellCommand("input text $invalidUid")
            }
        }

        // Wait for Continue button to become enabled
        Thread.sleep(1500)

        // Tap Continue
        val continueBtn = device.findObject(By.text("Continue"))
            ?: device.findObject(By.textContains("Continue"))
        assertNotNull("Continue button not found", continueBtn)
        continueBtn!!.click()

        // Wait for the error/toast to appear (login attempt with invalid UID fails)
        Thread.sleep(7000)

        // Verify we're STILL on the login screen (not navigated to Home)
        // The app should show error toast and remain on login
        val chatsTab = device.findObject(By.desc("Chats"))
            ?: device.findObject(By.text("Chats"))
        val stillOnLogin = device.findObject(By.text("Enter UID")) != null ||
            device.findObject(By.text("Continue")) != null ||
            device.findObject(By.textContains("Enter UID")) != null ||
            device.findObject(By.clazz("android.widget.EditText")) != null

        assertTrue(
            "Expected to remain on login screen after invalid credentials. " +
                "Home screen should NOT have appeared.",
            stillOnLogin && chatsTab == null
        )
    }

    /**
     * E2E-003: Logout returns to the Login screen.
     *
     * Flow: Home (Chats tab) → find profile/avatar menu → tap "Logout" → navigates to Login.
     */
    @Test
    fun test03_logoutReturnsToLogin() {
        // First, login successfully
        E2ETestHelper.launchApp(device)
        E2ETestHelper.loginViaUI(device)

        // Verify on Home screen
        assertTrue("Not on Home screen before logout test", E2ETestHelper.isOnHomeScreen(device))

        // In the Compose app, the overflow/profile menu is typically a small clickable
        // element in the header area (avatar or icon). It triggers a popup with "Logout".
        var logoutClicked = false

        // Strategy 1: Look for clickable elements in the top header area
        // In Compose, the header area renders as clickable View nodes
        val allClickables = device.findObjects(By.clickable(true))
        for (clickable in allClickables) {
            val bounds = clickable.visibleBounds
            // Only consider small elements in the top area (header region)
            if (bounds.top > 200) continue
            if (bounds.width() > 200 || bounds.height() > 200) continue

            clickable.click()
            Thread.sleep(1500)

            val logoutOption = device.findObject(By.text("Logout"))
                ?: device.findObject(By.textContains("Logout"))
                ?: device.findObject(By.textContains("Log out"))
            if (logoutOption != null) {
                logoutOption.click()
                logoutClicked = true
                break
            }

            // Dismiss popup and try next
            device.pressBack()
            Thread.sleep(500)
        }

        // Strategy 2: Look for any element with "Logout" text directly (some UIs show it differently)
        if (!logoutClicked) {
            val logoutDirect = device.findObject(By.text("Logout"))
                ?: device.findObject(By.textContains("Logout"))
            if (logoutDirect != null) {
                logoutDirect.click()
                logoutClicked = true
            }
        }

        assertTrue("Could not find and click the avatar/overflow menu to trigger Logout", logoutClicked)

        // Wait for login screen to appear after logout
        Thread.sleep(3000)
        val onLogin = device.wait(
            Until.hasObject(By.text("Enter UID")),
            TIMEOUT
        )
        // Fallback: check for "Login" text or "Continue" button
        val loginAppeared = onLogin ||
            device.findObject(By.text("Login")) != null ||
            device.findObject(By.text("Continue")) != null

        assertTrue("Login screen did not appear after logout", loginAppeared)
    }

    /**
     * E2E-004: Existing session skips login (auto-login).
     * After a successful login, relaunching the app should go directly to Home.
     * Same approach as kotlin: login first, press home, relaunch, verify Home appears without login.
     */
    @Test
    fun test04_existingSessionSkipsLogin() {
        // First login normally
        E2ETestHelper.launchApp(device)
        E2ETestHelper.loginViaUI(device)

        // Verify on Home
        assertTrue(
            "Home screen not loaded for initial login",
            E2ETestHelper.isOnHomeScreen(device)
        )

        // Press home to background the app (don't logout — session stays)
        device.pressHome()
        Thread.sleep(2000)

        // Re-launch the app — should skip login and go directly to Home
        E2ETestHelper.launchApp(device)

        // Should go directly to Home screen (skip login) since session exists
        val homeLoaded = device.wait(
            Until.hasObject(By.desc("Chats")),
            TIMEOUT
        ) || device.wait(
            Until.hasObject(By.text("Chats")),
            SHORT_TIMEOUT
        )

        // Make sure we did NOT land on the login screen
        val onLogin = device.findObject(By.text("Enter UID")) != null ||
            device.findObject(By.text("Continue")) != null

        assertTrue(
            "App did not auto-login with existing session — expected Home screen directly, " +
                "but got login screen instead.",
            homeLoaded && !onLogin
        )
    }
}
