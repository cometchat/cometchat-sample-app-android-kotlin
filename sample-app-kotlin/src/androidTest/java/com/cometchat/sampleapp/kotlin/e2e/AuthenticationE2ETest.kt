package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for Authentication flows.
 *
 * Test IDs:
 * - E2E-001: testValidLoginNavigatesToHome
 * - E2E-002: testInvalidCredentialsShowsError
 * - E2E-003: testLogoutReturnsToLogin
 * - E2E-004: testExistingSessionSkipsLogin
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.AuthenticationE2ETest
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
        val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<com.cometchat.sampleapp.kotlin.app.SampleApplication>()
        app.onSDKInitialized()
        E2ETestHelper.logoutIfNeeded()
    }

    /**
     * E2E-001: Valid login with correct UID navigates to Home screen.
     */
    @Test
    fun test01_validLoginNavigatesToHome() {
        E2ETestHelper.launchApp(device)
        E2ETestHelper.loginViaUI(device)

        // Verify Home screen is displayed (bottom nav visible)
        val bottomNav = device.findObject(By.res(PACKAGE, "bottomNavigationView"))
        assertNotNull("Home screen bottom navigation not found after valid login", bottomNav)

        // Verify Chats tab content is loaded (conversationList component)
        val chatsList = E2ETestHelper.waitForObject(device, By.res(PACKAGE, "conversationList"))
        assertNotNull("Conversations list not found on Home screen", chatsList)
    }

    /**
     * E2E-002: Invalid UID shows an error (toast or remains on login screen).
     */
    @Test
    fun test02_invalidCredentialsShowsError() {
        E2ETestHelper.launchApp(device)

        // Wait for login screen
        val uidField = device.wait(
            Until.findObject(By.res(PACKAGE, "etUid")),
            TIMEOUT
        )
        assertNotNull("Login UID field not found", uidField)

        // Type an invalid UID
        uidField!!.clear()
        uidField.text = "invalid_uid_that_does_not_exist_xyz_12345"

        // Tap Continue
        val continueBtn = device.findObject(By.res(PACKAGE, "btnContinue"))
        assertNotNull("Continue button not found", continueBtn)
        continueBtn!!.click()

        // Wait a few seconds for the error to appear
        Thread.sleep(5000)

        // Verify we're still on the login screen (UID field still visible, no bottom nav)
        val uidFieldStill = device.findObject(By.res(PACKAGE, "etUid"))
        val bottomNav = device.findObject(By.res(PACKAGE, "bottomNavigationView"))

        assertTrue(
            "Expected to remain on login screen after invalid credentials",
            uidFieldStill != null || bottomNav == null
        )
    }

    /**
     * E2E-003: Logout returns to the Login screen.
     *
     * Flow: Home (Chats tab) → tap CometChatAvatar (overflow menu, a MaterialCardView)
     * → popup_user_menu shows → tap "Logout" → navigates to LoginActivity.
     */
    @Test
    fun test03_logoutReturnsToLogin() {
        // First, login successfully
        E2ETestHelper.launchApp(device)
        E2ETestHelper.loginViaUI(device)

        // Verify on Home screen
        val bottomNav = device.wait(
            Until.findObject(By.res(PACKAGE, "bottomNavigationView")),
            TIMEOUT
        )
        assertNotNull("Not on Home screen before logout test", bottomNav)

        // The overflow menu is a CometChatAvatar (extends MaterialCardView) added to
        // CometChatConversations header. It's a small clickable card (40dp) in the header area.
        // Strategy: Find clickable MaterialCardView instances inside the conversationList
        // component header, or find any clickable view that triggers the popup with "Logout".

        var logoutClicked = false

        // Strategy 1: Find CometChatAvatar by its class name (it extends MaterialCardView)
        // The avatar is inside the CometChatConversations component's action bar area
        val avatarCandidates = device.findObjects(
            By.clazz("com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar")
                .clickable(true)
        )

        if (avatarCandidates.isNotEmpty()) {
            // Click the first CometChatAvatar found (the overflow menu in the header)
            avatarCandidates[0].click()
            Thread.sleep(1500)

            val logoutOption = device.findObject(By.text("Logout"))
            if (logoutOption != null) {
                logoutOption.click()
                logoutClicked = true
            }
        }

        // Strategy 2: If CometChatAvatar class lookup didn't work, try MaterialCardView
        if (!logoutClicked) {
            val cardCandidates = device.findObjects(
                By.clazz("com.google.android.material.card.MaterialCardView").clickable(true)
            )

            for (card in cardCandidates) {
                // Skip large cards (bottom sheet, etc) — avatar is small (40dp ~ <150px)
                if (card.visibleBounds.width() > 200 || card.visibleBounds.height() > 200) continue

                card.click()
                Thread.sleep(1500)

                val logoutOption = device.findObject(By.text("Logout"))
                if (logoutOption != null) {
                    logoutOption.click()
                    logoutClicked = true
                    break
                }

                // Dismiss popup and try next
                device.pressBack()
                Thread.sleep(500)
            }
        }

        // Strategy 3: Fallback — look for any clickable view in the header region
        // (top portion of the screen, above the list items)
        if (!logoutClicked) {
            val allClickables = device.findObjects(By.clickable(true))
            for (clickable in allClickables) {
                // Only consider elements in the top area (first 200px from top of conversationList)
                if (clickable.visibleBounds.top > 300) continue
                if (clickable.visibleBounds.width() > 200 || clickable.visibleBounds.height() > 200) continue

                clickable.click()
                Thread.sleep(1500)

                val logoutOption = device.findObject(By.text("Logout"))
                if (logoutOption != null) {
                    logoutOption.click()
                    logoutClicked = true
                    break
                }

                device.pressBack()
                Thread.sleep(500)
            }
        }

        assertTrue("Could not find and click the avatar/overflow menu to trigger Logout popup", logoutClicked)

        // Wait for login screen to appear after logout
        val loginScreen = device.wait(
            Until.hasObject(By.res(PACKAGE, "etUid")),
            TIMEOUT
        )
        assertTrue("Login screen did not appear after logout", loginScreen)
    }

    /**
     * E2E-004: Existing session skips login (auto-login).
     * After a successful login, relaunching the app should go directly to Home.
     */
    @Test
    fun test04_existingSessionSkipsLogin() {
        // First login
        E2ETestHelper.launchApp(device)
        E2ETestHelper.loginViaUI(device)

        // Verify on Home
        assertTrue(
            "Home screen not loaded for initial login",
            E2ETestHelper.waitUntilExists(device, By.res(PACKAGE, "bottomNavigationView"))
        )

        // Press home to background the app
        device.pressHome()
        Thread.sleep(2000)

        // Re-launch the app
        E2ETestHelper.launchApp(device)

        // Should go directly to Home screen (skip login)
        val homeLoaded = device.wait(
            Until.hasObject(By.res(PACKAGE, "bottomNavigationView")),
            TIMEOUT
        )
        assertTrue("App did not auto-login with existing session — expected Home screen", homeLoaded)
    }

}
