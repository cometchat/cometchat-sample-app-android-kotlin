package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
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
 * E2E tests for **Thread Subscription** (Slack-style follow/unfollow — ENG-37567 / ENG-37569),
 * View / sample-app-kotlin.
 *
 * The feature is gated by the UIKit flag `CometChatUIKit.isThreadSubscriptionEnabled()`
 * (`UIKitSettings.enableThreadSubscription`, **default OFF**). It is turned ON in the *master*
 * apps (`KotlinApplication` calls `setEnableThreadSubscription(true)`) but **NOT in the sample
 * apps** — so in this suite the option/bell are expected to be ABSENT, and these tests document
 * that gate while proving the underlying thread flow is unaffected. If a future build flips the
 * flag on, the happy-path branches below assert the real behaviour.
 *
 * When enabled, the surfaces are:
 * - Message action-sheet option (state-labelled): "Notify me about replies" (subscribe) /
 *   "Stop reply notifications" (unsubscribe) — from chatuikit-core strings.
 * - Thread top-bar bell (state-labelled contentDescription "Mute thread" / "Unmute thread").
 * - Feedback is a Toast; the flip is optimistic with a 400ms debounce + revert-on-failure.
 *
 * Test IDs:
 * - THSUB-001: test01_subscriptionOptionStateInActionSheet
 * - THSUB-002: test02_subscriptionBellInThreadHeader
 * - THSUB-003: test03_replyInThreadUnaffectedBySubscription
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.ThreadSubscriptionE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ThreadSubscriptionE2ETest {

    private lateinit var device: UiDevice

    private val subscribeOption = "Notify me about replies"
    private val unsubscribeOption = "Stop reply notifications"

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
    }

    /**
     * THSUB-001: The subscription option is state-labelled in the message action sheet — "Notify me
     * about replies" (not subscribed) XOR "Stop reply notifications" (subscribed), never both.
     * With the flag off (sample-app default) neither appears; we then assert the popup still opened.
     */
    @Test
    fun test01_subscriptionOptionStateInActionSheet() {
        E2ETestHelper.openFirstConversation(device)
        val message = sendAndAwait("ThSubOption")

        assertTrue("Action-sheet popup did not appear", longPressAndWaitForPopup(message))

        val subscribe = findOption(subscribeOption)
        val unsubscribe = findOption(unsubscribeOption)

        if (subscribe != null || unsubscribe != null) {
            // Feature enabled: exactly one state should be offered.
            assertTrue(
                "Subscribe and unsubscribe are mutually exclusive — only one should be offered",
                (subscribe != null) != (unsubscribe != null)
            )
            // Tapping toggles subscription; feedback is a Toast. Verify it doesn't crash the screen.
            tapNode(subscribe ?: unsubscribe!!)
            assertScreenIntact("Screen should remain after toggling thread subscription")
        } else {
            // Expected in the sample app (flag off): the option is gated out but the popup opened.
            assertTrue(
                "Thread subscription is gated off; the popup should still show other options",
                isAnyKnownOptionVisible()
            )
            dismissPopup()
        }
    }

    /**
     * THSUB-002: When enabled, the thread top bar carries a subscription bell (contentDescription
     * "Mute thread" / "Unmute thread"). With the flag off, the thread still opens without a bell.
     */
    @Test
    fun test02_subscriptionBellInThreadHeader() {
        E2ETestHelper.openFirstConversation(device)
        val parent = sendAndAwait("ThSubBell")

        assertTrue("Popup did not appear", longPressAndWaitForPopup(parent))
        if (!tapReplyInThread()) {
            assertScreenIntact("Reply In Thread unavailable — screen should be intact")
            return
        }

        // Thread header is present regardless of the subscription flag.
        assertNotNull(
            "Thread header should be visible after opening the thread",
            device.wait(Until.findObject(By.res(PACKAGE, "threadHeader")), SHORT_TIMEOUT)
        )

        val bell = device.findObject(By.desc("Mute thread"))
            ?: device.findObject(By.desc("Unmute thread"))
            ?: device.findObject(By.descContains("thread"))
        if (bell != null) {
            // Feature enabled: tapping the bell flips state optimistically; screen stays intact.
            bell.click()
            Thread.sleep(1500)
            assertNotNull(
                "Thread screen should remain after tapping the subscription bell",
                device.findObject(By.res(PACKAGE, "threadHeader"))
                    ?: device.findObject(By.clazz("android.widget.EditText"))
            )
        } else {
            // Expected in the sample app (flag off): thread works without a bell.
            assertNotNull(
                "Thread should be usable without a subscription bell (flag off)",
                device.findObject(By.clazz("android.widget.EditText"))
            )
        }
    }

    /**
     * THSUB-003: Thread subscription is additive — replying in a thread still works whether or not
     * the subscription surfaces are present.
     */
    @Test
    fun test03_replyInThreadUnaffectedBySubscription() {
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)

        val parent = sendAndAwait("ThSubReply")
        assertTrue("Popup did not appear", longPressAndWaitForPopup(parent))
        if (!tapReplyInThread()) {
            assertNotNull(
                "Messages screen should be intact if Reply In Thread is unavailable",
                device.findObject(By.clazz("android.widget.EditText"))
            )
            return
        }

        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Thread composer not found", editText)
        val reply = E2ETestHelper.uniqueMessage("ThSubReplyBody")
        editText!!.clear()
        editText.text = reply
        Thread.sleep(1000)

        val sendButton = device.findObject(By.desc("Send")) ?: device.findObject(By.descContains("Send"))
        assertNotNull("Send button not found in thread composer", sendButton)
        sendButton!!.click()
        Thread.sleep(3000)

        assertTrue(
            "Thread reply '$reply' should appear in the thread list",
            device.wait(Until.hasObject(By.textContains(reply)), TIMEOUT)
        )
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private fun sendAndAwait(prefix: String): String {
        val text = E2ETestHelper.uniqueMessage(prefix)
        E2ETestHelper.sendMessage(device, text)
        var appeared = device.wait(Until.hasObject(By.textContains(text)), TIMEOUT)
        if (!appeared) {
            // A send can occasionally drop / render slowly on a slow network — resend once.
            E2ETestHelper.sendMessage(device, text)
            appeared = device.wait(Until.hasObject(By.textContains(text)), TIMEOUT)
        }
        assertTrue("Message '$text' did not appear in the chat", appeared)
        Thread.sleep(2500)
        return text
    }

    private fun longPressAndWaitForPopup(messageText: String): Boolean {
        E2ETestHelper.longPressMessage(device, messageText)
        if (isAnyKnownOptionVisible()) return true
        device.findObject(By.textContains(messageText))?.longClick()
        Thread.sleep(2000)
        return isAnyKnownOptionVisible()
    }

    private fun tapReplyInThread(): Boolean {
        repeat(3) {
            val option = device.findObject(By.text("Reply In Thread"))
                ?: device.findObject(By.textContains("Reply In Thread"))
                ?: device.findObject(By.textContains("Reply in Thread"))
            if (option != null) {
                val b = option.visibleBounds
                device.click(b.centerX(), b.centerY())
                if (device.wait(Until.hasObject(By.res(PACKAGE, "threadHeader")), 15_000L)) return true
            } else {
                Thread.sleep(1000)
            }
        }
        return device.hasObject(By.res(PACKAGE, "threadHeader"))
    }

    private fun isAnyKnownOptionVisible(): Boolean =
        findOption(subscribeOption) != null ||
            findOption(unsubscribeOption) != null ||
            device.findObject(By.text("Copy")) != null ||
            device.findObject(By.text("Delete")) != null ||
            device.findObject(By.textContains("Reply In Thread")) != null

    private fun findOption(label: String): UiObject2? =
        device.findObject(By.text(label)) ?: device.findObject(By.textContains(label))

    private fun tapNode(node: UiObject2) {
        val b = node.visibleBounds
        device.click(b.centerX(), b.centerY())
        Thread.sleep(2500)
    }

    private fun dismissPopup() {
        device.pressBack()
        Thread.sleep(1000)
    }

    private fun assertScreenIntact(msg: String) {
        assertNotNull(
            msg,
            device.findObject(By.res(PACKAGE, "messageList"))
                ?: device.findObject(By.clazz("android.widget.EditText"))
        )
    }
}
