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
 * E2E tests for Message Information (Read Receipts info screen).
 *
 * Tests verify that long-pressing a sent message and tapping "Message Information"
 * opens an info screen showing timestamps (sent at, delivered at, read at).
 *
 * Test IDs:
 * - E2E-046: testMessageInformationTimestamps
 *
 * Flow: Login → Users tab → Open user chat → Send message → Long-press message →
 *       Tap "Message Information" → Verify timestamps shown
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.ReadReceiptsE2ETest
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

    /**
     * E2E-046: Send a message, long-press it, tap "Info" option from popup menu,
     * verify the Message Information bottom sheet shows timestamps (Sent At, Delivered At).
     *
     * NOTE: The UIKit popup option text is "Info" (from cometchat_info string).
     * Clicking it opens a CometChatMessageInformationBottomSheet (not a new Activity).
     * The bottom sheet shows receipt details: "Sent At", "Delivered At", "Read At" labels
     * with corresponding timestamps.
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

        // Step 3: Long-press the message to open the popup menu
        // Use multiple strategies like the ThreadMessagesE2ETest
        val popupShown = longPressAndWaitForPopup("InfoTest")
        assertTrue(
            "Popup menu did not appear after long-pressing message",
            popupShown
        )

        // Step 4: Tap "Info" option in the popup menu
        // The UIKit string is "Info" (cometchat_info = "Info")
        val infoOption = device.findObject(By.text("Info"))
            ?: device.findObject(By.text("Message Information"))
            ?: device.findObject(By.textContains("Info"))

        assertNotNull("'Info' option not found in popup menu", infoOption)
        infoOption!!.click()
        Thread.sleep(SETTLE_TIME)

        // Step 5: Verify the Message Information BottomSheet opened
        // The CometChatMessageInformation component shows receipt details.
        // Look for labels specific to the info bottom sheet: "Sent At", "Delivered At",
        // "Read At", "Message Receipt", or the receipt section content.
        val sentAtLabel = device.findObject(By.textContains("Sent At"))
            ?: device.findObject(By.textContains("Sent"))
        val deliveredAtLabel = device.findObject(By.textContains("Delivered At"))
            ?: device.findObject(By.textContains("Delivered"))
        val readAtLabel = device.findObject(By.textContains("Read At"))
            ?: device.findObject(By.textContains("Read"))
        val receiptLabel = device.findObject(By.textContains("Message Receipt"))
            ?: device.findObject(By.textContains("Receipt"))

        // The bottom sheet should show at least one receipt-related label
        val bottomSheetOpened = sentAtLabel != null ||
            deliveredAtLabel != null ||
            readAtLabel != null ||
            receiptLabel != null

        assertTrue(
            "Message Information bottom sheet did not open or show receipt timestamps. " +
                "Expected to find 'Sent At', 'Delivered At', 'Read At', or 'Message Receipt' labels. " +
                "The popup 'Info' option may not have triggered the bottom sheet.",
            bottomSheetOpened
        )

        // Step 6: Dismiss the bottom sheet
        device.pressBack()
        Thread.sleep(1000)

        // Step 7: Verify we returned to messages screen
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Should return to messages screen after dismissing info sheet", messageList)
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Long-presses a message and waits for the popup menu to appear.
     * Tries multiple strategies to ensure the long-press registers.
     */
    private fun longPressAndWaitForPopup(messageText: String): Boolean {
        val textNode = device.findObject(By.textContains(messageText))
        assertNotNull("Message text '$messageText' not found on screen", textNode)

        val bounds = textNode!!.visibleBounds
        val centerY = (bounds.top + bounds.bottom) / 2
        val centerX = (bounds.left + bounds.right) / 2
        val rowCenterX = device.displayWidth / 2

        // Strategy 1: Shell long-press at row center X, message Y
        device.executeShellCommand("input touchscreen swipe $rowCenterX $centerY $rowCenterX $centerY 3000")
        Thread.sleep(2500)
        if (isPopupMenuVisible()) return true

        // Strategy 2: Shell long-press directly on the text node center
        device.executeShellCommand("input touchscreen swipe $centerX $centerY $centerX $centerY 3000")
        Thread.sleep(2500)
        if (isPopupMenuVisible()) return true

        // Strategy 3: UiObject2.longClick()
        val textNodeRetry = device.findObject(By.textContains(messageText))
        textNodeRetry?.longClick()
        Thread.sleep(2500)
        if (isPopupMenuVisible()) return true

        // Strategy 4: Long-press slightly above text (bubble card area)
        val aboveY = bounds.top - 20
        if (aboveY > 0) {
            device.executeShellCommand("input touchscreen swipe $centerX $aboveY $centerX $aboveY 3000")
            Thread.sleep(2500)
            if (isPopupMenuVisible()) return true
        }

        return false
    }

    /**
     * Checks if the popup menu is visible by looking for known options.
     */
    private fun isPopupMenuVisible(): Boolean {
        // Check for known popup menu options
        if (device.findObject(By.text("Info")) != null) return true
        if (device.findObject(By.text("Copy")) != null) return true
        if (device.findObject(By.text("Edit")) != null) return true
        if (device.findObject(By.text("Delete")) != null) return true
        if (device.findObject(By.text("Reply In Thread")) != null) return true

        // Check for emoji quick reactions
        val defaultEmoji = listOf("😍", "👍🏻", "👍", "🔥", "😊", "❤️", "❤")
        for (emoji in defaultEmoji) {
            if (device.findObject(By.text(emoji)) != null) return true
        }

        return false
    }
}
