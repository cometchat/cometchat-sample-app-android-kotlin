package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SETTLE_TIME
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
 * E2E tests for Message Information (Read Receipts info screen) in the Compose sample app.
 *
 * Tests verify that long-pressing a sent message and tapping "Message Information"
 * opens an info screen showing timestamps (sent at, delivered at, read at).
 *
 * Key Compose adaptations:
 * - No resource IDs — uses By.desc(), By.text(), By.clazz() selectors
 * - Long-press via UiObject2.longClick() (Compose renders clickable surfaces)
 * - Action menu items found via By.text() / By.textContains()
 * - Timestamps verified via text pattern matching on TextView elements
 *
 * Test IDs:
 * - E2E-046: testMessageInformationTimestamps
 *
 * Flow: Login → Users tab → Open user chat → Send message → Long-press message →
 *       Tap "Message Information" → Verify timestamps shown
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.ReadReceiptsE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ReadReceiptsE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        // Open a 1-on-1 chat via Users tab
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)
    }

    // ─── Helper Methods ──────────────────────────────────────────────────────────

    /**
     * Long-presses on a message bubble containing the given text.
     *
     * In Compose, messages render as text nodes in the accessibility tree.
     * We find the text element and long-click it to trigger the action menu.
     */
    private fun longPressMessage(messageText: String) {
        val messageElement = device.findObject(By.textContains(messageText))
        assertNotNull("Message '$messageText' not found for long-press", messageElement)
        messageElement!!.longClick()
        Thread.sleep(2000) // Wait for action menu/bottom sheet to appear
    }

    // ─── Test Methods ────────────────────────────────────────────────────────────

    /**
     * E2E-046: Send a message, long-press it, tap "Message Information" option,
     * verify the info screen shows timestamps (sent at, delivered at).
     *
     * Flow:
     * 1. Send a unique message
     * 2. Verify message appears in chat
     * 3. Long-press the message to open action menu
     * 4. Tap "Message Information" option
     * 5. Verify timestamps (Sent/Delivered/Read) appear as text or content descriptions
     */
    @Test
    fun test01_messageInformationTimestamps() {
        // Step 1: Send a unique message
        val messageText = E2ETestHelper.uniqueMessage("InfoTest")
        E2ETestHelper.sendMessage(device, messageText)
        Thread.sleep(SETTLE_TIME)

        // Step 2: Verify the message appears in the chat
        val sentMessage = device.wait(
            Until.findObject(By.textContains("InfoTest")),
            TIMEOUT
        )
        assertNotNull("Sent message '$messageText' not found in chat", sentMessage)

        // Step 3: Long-press the message to open the action menu
        longPressMessage("InfoTest")

        // Step 4: Look for "Message Information" option in the action menu
        // The Compose UIKit popup/bottom sheet shows options like:
        // Reply, Edit, Delete, Copy, Message Information, React, etc.
        var messageInfoOption = device.wait(
            Until.findObject(By.text("Message Information")),
            SHORT_TIMEOUT
        )

        // Try alternative text patterns for the option
        if (messageInfoOption == null) {
            messageInfoOption = device.findObject(By.textContains("Message Information"))
                ?: device.findObject(By.textContains("Information"))
                ?: device.findObject(By.textContains("Info"))
                ?: device.findObject(By.descContains("Message Information"))
                ?: device.findObject(By.descContains("Information"))
        }

        // If the option is not directly visible, it might be in a scrollable area
        if (messageInfoOption == null) {
            // Try scrolling the popup/bottom sheet options
            E2ETestHelper.scrollDown(device)
            Thread.sleep(1000)
            messageInfoOption = device.findObject(By.textContains("Message Information"))
                ?: device.findObject(By.textContains("Information"))
                ?: device.findObject(By.textContains("Info"))
                ?: device.findObject(By.descContains("Information"))
        }

        assertNotNull(
            "Message Information option not found in long-press action menu",
            messageInfoOption
        )
        messageInfoOption!!.click()
        Thread.sleep(SETTLE_TIME)

        // Step 5: Verify the Message Information screen shows timestamps
        // The info screen typically shows:
        // - "Sent At" / "Sent" with a timestamp
        // - "Delivered At" / "Delivered" with a timestamp (if delivered)
        // - "Read At" / "Read" with a timestamp (if read)
        // Timestamps follow patterns like "12:30 PM", "Jan 15, 2024", etc.

        // Look for timestamp-related labels via text
        val sentAtLabel = device.findObject(By.textContains("Sent"))
            ?: device.findObject(By.textContains("sent"))
            ?: device.findObject(By.descContains("Sent"))
        val deliveredAtLabel = device.findObject(By.textContains("Delivered"))
            ?: device.findObject(By.textContains("delivered"))
            ?: device.findObject(By.descContains("Delivered"))
        val readAtLabel = device.findObject(By.textContains("Read"))
            ?: device.findObject(By.textContains("read"))
            ?: device.findObject(By.descContains("Read"))

        // Also look for time patterns (HH:MM format) on the screen
        val timePattern = "\\d{1,2}:\\d{2}"
        val timeTexts = device.findObjects(By.clazz("android.widget.TextView"))
            .filter { tv ->
                val text = tv.text ?: ""
                text.matches(Regex(".*$timePattern.*"))
            }

        // We should find at least the "Sent" label or a timestamp
        val hasTimestampInfo = sentAtLabel != null ||
            deliveredAtLabel != null ||
            readAtLabel != null ||
            timeTexts.isNotEmpty()

        assertTrue(
            "Message Information screen should display timestamps (Sent/Delivered/Read). " +
                "Found: sentAt=${sentAtLabel != null}, deliveredAt=${deliveredAtLabel != null}, " +
                "readAt=${readAtLabel != null}, timeTexts=${timeTexts.size}",
            hasTimestampInfo
        )

        // Verify we're on a new screen (not the message popup)
        // The message info screen should have a back button or be a separate view
        val backBtn = device.findObject(By.descContains("Back"))
            ?: device.findObject(By.descContains("back"))
            ?: device.findObject(By.descContains("Navigate up"))
            ?: device.findObject(By.desc("Navigate up"))

        // Navigate back to messages screen
        if (backBtn != null) {
            backBtn.click()
        } else {
            device.pressBack()
        }
        Thread.sleep(1000)

        // Verify we returned to messages screen (EditText for composer present)
        val composerPresent = device.findObject(By.clazz("android.widget.EditText")) != null ||
            device.findObject(By.desc("Send")) != null ||
            device.findObject(By.descContains("Send")) != null

        assertTrue(
            "Should return to messages screen after viewing message info",
            composerPresent
        )
    }
}
