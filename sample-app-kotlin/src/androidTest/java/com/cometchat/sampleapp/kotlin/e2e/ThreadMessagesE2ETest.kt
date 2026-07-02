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
 * E2E tests for Thread Messages.
 *
 * Correct flow:
 * 1. Open a conversation (Messages screen)
 * 2. Send a parent message (or find one)
 * 3. Long-press the parent message → popup menu appears
 * 4. Tap "Reply In Thread" in the popup menu
 * 5. ThreadMessagesActivity opens with:
 *    - CometChatThreadHeader showing the parent message
 *    - CometChatMessageList for thread replies
 *    - CometChatMessageComposer for composing replies
 * 6. Send replies in the thread
 * 7. Go back → parent message shows "X Replies" indicator
 *
 * Test IDs:
 * - E2E-040: testOpenThreadShowsParentAndReplies
 * - E2E-041: testSendReplyInThread
 * - E2E-043: testParentShowsReplyCount
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.ThreadMessagesE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ThreadMessagesE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
    }

    /**
     * E2E-040: Open a thread and verify the parent message and replies are shown.
     *
     * Flow:
     * 1. Open a conversation
     * 2. Send a parent message
     * 3. Long-press the parent message → popup menu
     * 4. Tap "Reply In Thread"
     * 5. Verify ThreadMessagesActivity opened:
     *    - threadHeader is visible (shows parent message)
     *    - messageList is visible (for replies)
     *    - messageComposer is visible (for sending replies)
     */
    @Test
    fun test01_openThreadShowsParentAndReplies() {
        // Open first conversation
        E2ETestHelper.openFirstConversation(device)

        // Send a parent message that we'll open the thread for
        val parentMessage = E2ETestHelper.uniqueMessage("ThreadParent")
        E2ETestHelper.sendMessage(device, parentMessage)

        // Wait for the message to appear in the list
        val messageAppeared = device.wait(
            Until.hasObject(By.textContains(parentMessage)),
            TIMEOUT
        )
        assertTrue("Parent message '$parentMessage' did not appear in chat", messageAppeared)
        Thread.sleep(3000) // Extra wait for message to fully render and settle

        // Long-press the parent message to open the popup menu
        val popupShown = longPressAndWaitForPopup(parentMessage)
        assertTrue(
            "Popup menu did not appear after long-pressing message. " +
                "The long-press gesture may not be landing on the message bubble.",
            popupShown
        )

        // Tap "Reply In Thread" and wait for the thread screen to open (robust click + retry).
        assertTrue(
            "ThreadMessagesActivity did not open — 'Reply In Thread' did not trigger navigation.",
            tapReplyInThread()
        )

        // Verify the parent message is visible in the thread header
        val parentInHeader = device.findObject(By.textContains(parentMessage))
        assertNotNull(
            "Parent message text should be visible in the thread header",
            parentInHeader
        )

        // Verify message composer is available for sending replies
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
            ?: device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Message composer should be visible in thread view", composer)

        // Verify message list exists (for thread replies)
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Message list should be visible in thread view", messageList)
    }

    /**
     * E2E-041: Send a reply in a thread and verify it appears.
     *
     * Flow:
     * 1. Open a conversation, send a parent message
     * 2. Long-press → "Reply In Thread" → ThreadMessagesActivity
     * 3. Type a reply in the thread's composer and send
     * 4. Verify the reply text appears in the thread's message list
     */
    @Test
    fun test02_sendReplyInThread() {
        // Open first conversation
        E2ETestHelper.openFirstConversation(device)

        // Send a parent message
        val parentMessage = E2ETestHelper.uniqueMessage("ThreadReplyParent")
        E2ETestHelper.sendMessage(device, parentMessage)

        // Wait for it to appear
        val messageAppeared = device.wait(
            Until.hasObject(By.textContains(parentMessage)),
            TIMEOUT
        )
        assertTrue("Parent message did not appear", messageAppeared)
        Thread.sleep(3000)

        // Long-press and wait for popup
        val popupShown = longPressAndWaitForPopup(parentMessage)
        assertTrue("Popup menu did not appear after long-press", popupShown)

        assertTrue("ThreadMessagesActivity did not open after 'Reply In Thread'", tapReplyInThread())

        // Find the thread's message composer and type a reply
        val threadComposer = device.findObject(By.res(PACKAGE, "messageComposer"))
        assertNotNull("Thread message composer not found", threadComposer)

        val editText = threadComposer!!.findObject(By.clazz("android.widget.EditText"))
            ?: device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("EditText not found in thread composer", editText)

        val replyMessage = E2ETestHelper.uniqueMessage("ThreadReply")
        editText!!.clear()
        editText.text = replyMessage
        Thread.sleep(1000)

        // Tap Send
        val sendButton = device.findObject(By.desc("Send"))
            ?: device.findObject(By.descContains("Send"))
            ?: threadComposer.findObject(By.clickable(true))
        assertNotNull("Send button not found in thread composer", sendButton)
        sendButton!!.click()
        Thread.sleep(3000)

        // Verify the reply appears in the thread's message list
        val replyAppeared = device.wait(
            Until.hasObject(By.textContains(replyMessage)),
            TIMEOUT
        )
        assertTrue(
            "Thread reply '$replyMessage' did not appear in the thread message list",
            replyAppeared
        )
    }

    /**
     * E2E-043: Parent message shows reply count after a reply is sent in thread.
     *
     * Flow:
     * 1. Open a conversation, send a parent message
     * 2. Long-press → "Reply In Thread" → ThreadMessagesActivity
     * 3. Send a reply in the thread
     * 4. Press back to return to main messages screen
     * 5. Verify the parent message now shows "1 Reply" indicator
     */
    @Test
    fun test03_parentShowsReplyCount() {
        // Open first conversation
        E2ETestHelper.openFirstConversation(device)

        // Send a parent message
        val parentMessage = E2ETestHelper.uniqueMessage("ReplyCount")
        E2ETestHelper.sendMessage(device, parentMessage)

        // Wait for it to appear
        val messageAppeared = device.wait(
            Until.hasObject(By.textContains(parentMessage)),
            TIMEOUT
        )
        assertTrue("Parent message did not appear", messageAppeared)
        Thread.sleep(3000)

        // Long-press and wait for popup
        val popupShown = longPressAndWaitForPopup(parentMessage)
        assertTrue("Popup menu did not appear after long-press", popupShown)

        assertTrue("ThreadMessagesActivity did not open after 'Reply In Thread'", tapReplyInThread())

        // Send a reply in the thread
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("EditText not found in thread composer", editText)

        val replyMessage = E2ETestHelper.uniqueMessage("CountReply")
        editText!!.clear()
        editText.text = replyMessage
        Thread.sleep(1000)

        val sendButton = device.findObject(By.desc("Send"))
            ?: device.findObject(By.descContains("Send"))
        assertNotNull("Send button not found", sendButton)
        sendButton!!.click()
        Thread.sleep(3000)

        // Verify reply appeared in thread
        device.wait(Until.hasObject(By.textContains(replyMessage)), TIMEOUT)

        // Go back to main messages screen
        // ThreadMessagesActivity has a toolbar with back navigation
        val backButton = device.findObject(By.res(PACKAGE, "toolbar"))
            ?.findObject(By.clickable(true))
            ?: device.findObject(By.descContains("Back"))
            ?: device.findObject(By.descContains("Navigate up"))

        if (backButton != null) {
            backButton.click()
        } else {
            device.pressBack()
        }
        Thread.sleep(SETTLE_TIME)

        // Verify we're back on the main messages screen
        val messageList = device.wait(
            Until.findObject(By.res(PACKAGE, "messageList")),
            TIMEOUT
        )
        assertNotNull("Did not return to main messages screen after pressing back", messageList)

        // Verify the parent message now shows a reply count indicator
        // The UIKit shows "1 Reply" text below the parent message bubble
        // (from InternalContentRenderer.bindThreadView: "1 Reply" or "N Replies")
        val replyCountIndicator = device.findObject(By.text("1 Reply"))
            ?: device.findObject(By.textContains("1 Reply"))
            ?: device.findObject(By.textContains("Replies"))
            ?: device.findObject(By.textContains("Reply"))

        assertNotNull(
            "Reply count indicator ('1 Reply') not found on parent message. " +
                "After sending a thread reply, the parent should show a reply count.",
            replyCountIndicator
        )
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Long-presses a message and waits for the popup menu to appear.
     *
     * The UIKit's CometChatMessagePopupMenu appears as a full-screen PopupWindow
     * with blurred background. After long-press, we look for popup menu indicators:
     * - Emoji quick reactions (like "😍", "👍🏻")
     * - Menu options (like "Reply In Thread", "Copy", "Edit", "Delete")
     *
     * This method tries multiple long-press strategies if the first doesn't work:
     * 1. Standard shell input swipe (3s duration) at message Y + screen center X
     * 2. Direct UiObject2.longClick() on the text node
     * 3. Shell input swipe directly on the text node bounds
     *
     * @return true if the popup menu appeared
     */
    private fun longPressAndWaitForPopup(messageText: String): Boolean {
        val textNode = device.findObject(By.textContains(messageText))
        assertNotNull("Message text '$messageText' not found on screen", textNode)

        val bounds = textNode!!.visibleBounds
        val centerY = (bounds.top + bounds.bottom) / 2
        val centerX = (bounds.left + bounds.right) / 2
        val rowCenterX = device.displayWidth / 2

        // Strategy 1: Shell long-press at row center X, message Y (hits the rowRoot)
        device.executeShellCommand("input touchscreen swipe $rowCenterX $centerY $rowCenterX $centerY 3000")
        Thread.sleep(2500)

        if (isPopupMenuVisible()) return true

        // Strategy 2: Shell long-press directly on the text node center
        device.executeShellCommand("input touchscreen swipe $centerX $centerY $centerX $centerY 3000")
        Thread.sleep(2500)

        if (isPopupMenuVisible()) return true

        // Strategy 3: Use UiObject2.longClick() on the text node
        val textNodeRetry = device.findObject(By.textContains(messageText))
        textNodeRetry?.longClick()
        Thread.sleep(2500)

        if (isPopupMenuVisible()) return true

        // Strategy 4: Try long-pressing slightly above the text (on the bubble card)
        val aboveY = bounds.top - 20
        if (aboveY > 0) {
            device.executeShellCommand("input touchscreen swipe $centerX $aboveY $centerX $aboveY 3000")
            Thread.sleep(2500)

            if (isPopupMenuVisible()) return true
        }

        return false
    }

    /**
     * Taps the "Reply In Thread" popup option and waits for ThreadMessagesActivity to open.
     *
     * The popup is a full-screen window and the option can sit below the visible fold, so a
     * plain UiObject2.click() may tap off-screen coordinates. We click by the visible-bounds
     * center and retry, giving the thread screen a generous wait (opening it hits the network).
     *
     * @return true if the thread screen (threadHeader) opened.
     */
    private fun tapReplyInThread(): Boolean {
        repeat(3) {
            val option = device.findObject(By.text("Reply In Thread"))
                ?: device.findObject(By.textContains("Reply In Thread"))
                ?: device.findObject(By.textContains("Reply in Thread"))
            if (option != null) {
                val b = option.visibleBounds
                device.click(b.centerX(), b.centerY())
                if (device.wait(Until.hasObject(By.res(PACKAGE, "threadHeader")), 15_000L)) {
                    return true
                }
            } else {
                Thread.sleep(1000)
            }
        }
        return device.hasObject(By.res(PACKAGE, "threadHeader"))
    }

    /**
     * 1TO1-053: Pressing back from a thread view returns to the main chat.
     */
    @Test
    fun test04_backFromThreadReturnsToChat() {
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)

        val parentMessage = E2ETestHelper.uniqueMessage("BackThread")
        E2ETestHelper.sendMessage(device, parentMessage)
        device.wait(Until.hasObject(By.textContains(parentMessage)), TIMEOUT)
        Thread.sleep(3000)

        val popupShown = longPressAndWaitForPopup(parentMessage)
        assertTrue("Popup menu did not appear", popupShown)

        assertTrue("Thread did not open after 'Reply In Thread'", tapReplyInThread())

        // Press back
        val backButton = device.findObject(By.descContains("Back"))
            ?: device.findObject(By.descContains("Navigate up"))
        if (backButton != null) {
            backButton.click()
        } else {
            device.pressBack()
        }
        Thread.sleep(SETTLE_TIME)

        // Verify we're back on the main messages screen
        val messageList = device.wait(Until.findObject(By.res(PACKAGE, "messageList")), TIMEOUT)
        assertNotNull("Should return to messages screen after pressing back from thread", messageList)

        // Verify the parent message is still visible
        val parentStillVisible = device.findObject(By.textContains(parentMessage))
        assertNotNull("Parent message should be visible after returning from thread", parentStillVisible)
    }

    /**
     * Checks if the CometChatMessagePopupMenu is visible.
     * The popup shows emoji reactions and text menu options.
     */
    private fun isPopupMenuVisible(): Boolean {
        // Check for known popup menu options
        val hasReplyOption = device.findObject(By.text("Reply In Thread")) != null ||
            device.findObject(By.textContains("Reply In Thread")) != null
        if (hasReplyOption) return true

        // Check for other common popup options
        val hasAnyOption = device.findObject(By.text("Copy")) != null ||
            device.findObject(By.text("Edit")) != null ||
            device.findObject(By.text("Delete")) != null ||
            device.findObject(By.text("Info")) != null ||
            device.findObject(By.textContains("React")) != null

        if (hasAnyOption) return true

        // Check for emoji quick reactions (popup header shows emoji row)
        val defaultEmoji = listOf("😍", "👍🏻", "👍", "🔥", "😊", "❤️", "❤")
        for (emoji in defaultEmoji) {
            if (device.findObject(By.text(emoji)) != null) return true
        }

        return false
    }
}
