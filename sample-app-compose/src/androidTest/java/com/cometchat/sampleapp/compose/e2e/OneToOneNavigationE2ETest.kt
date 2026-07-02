package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SETTLE_TIME
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for 1:1 conversation navigation flows (sample-app-compose).
 *
 * Tests navigation into and out of 1:1 chat screens.
 *
 * Key Compose differences:
 * - No resource IDs — uses By.desc(), By.text(), By.clazz()
 * - Messages screen detected via By.clazz("android.widget.EditText")
 * - Home screen detected via By.desc("Chats") / By.text("Chats")
 * - StaleObjectException handling with safeGetBounds
 *
 * Test IDs:
 * - 1TO1-003: testOpenChatFromContactsPicker
 * - 1TO1-004: testBackButtonReturnsToHome
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.OneToOneNavigationE2ETest
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

        // Verify messages screen loaded (EditText for composer present)
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen did not load after tapping user in contacts (no EditText)", editText)

        // Verify send button is present (messages screen is functional)
        val sendBtn = device.findObject(By.desc("Send button disabled"))
            ?: device.findObject(By.desc("Send message"))
            ?: device.findObject(By.descContains("Send"))
        assertNotNull("Send button not found on 1:1 messages screen", sendBtn)
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
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen did not load", editText)

        // Press back
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // After pressing back from a chat opened via Users tab, we should be back on Users tab.
        // The home screen has bottom navigation with "Users", "Chats", "Groups", etc.
        val usersTab = device.findObject(By.desc("Users"))
            ?: device.findObject(By.text("Users"))
            ?: device.findObject(By.descContains("Users"))
        val chatsTab = device.findObject(By.desc("Chats"))
            ?: device.findObject(By.text("Chats"))
            ?: device.findObject(By.descContains("Chats"))

        assertTrue(
            "Home screen not displayed after pressing back (neither Users nor Chats tab found)",
            usersTab != null || chatsTab != null
        )
    }
}
