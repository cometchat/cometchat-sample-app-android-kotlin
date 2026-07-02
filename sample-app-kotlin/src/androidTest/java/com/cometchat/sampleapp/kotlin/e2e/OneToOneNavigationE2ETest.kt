package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SETTLE_TIME
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for 1:1 conversation navigation flows (sample-app-kotlin).
 *
 * Tests navigation into and out of 1:1 chat screens.
 *
 * Test IDs:
 * - 1TO1-003: testOpenChatFromContactsPicker
 * - 1TO1-004: testBackButtonReturnsToHome
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.OneToOneNavigationE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class OneToOneNavigationE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
    }

    /**
     * 1TO1-003: Open a 1:1 chat by navigating to Users tab and tapping a contact.
     * Verifies that tapping a user in the Users list opens the messages screen.
     */
    @Test
    fun test01_openChatFromContactsPicker() {
        // Navigate to Users tab
        E2ETestHelper.navigateToTab(device, "Users")

        // Open the first user from the list
        E2ETestHelper.openFirstUser(device)

        // Verify messages screen loaded (messageList component present)
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen did not load after tapping user in contacts", messageList)

        // Verify composer is present (can type messages)
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        assertNotNull("Message composer not found on 1:1 messages screen", composer)
    }

    /**
     * 1TO1-004: Pressing back from a 1:1 chat returns to the home screen.
     * Opens a chat, presses back, and verifies the bottom navigation reappears.
     */
    @Test
    fun test02_backButtonReturnsToHome() {
        // Navigate to Users tab and open first user
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)

        // Verify we're on the messages screen
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen did not load", messageList)

        // Press back
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Verify we're back on the home screen (bottom navigation visible)
        val bottomNav = device.findObject(By.res(PACKAGE, "bottomNavigationView"))
        assertNotNull("Bottom navigation not visible after pressing back from chat", bottomNav)

        // Verify the Users tab is still active (we came from Users)
        val usersTabContent = device.findObject(By.desc("Users"))
            ?: device.findObject(By.text("Users"))
        assertNotNull("Users tab content not found after returning from chat", usersTabContent)
    }
}
