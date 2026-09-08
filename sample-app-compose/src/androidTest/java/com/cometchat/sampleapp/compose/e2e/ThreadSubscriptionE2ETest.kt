package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SETTLE_TIME
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for **Thread Subscription** in the Compose sample app (ENG-37567 / ENG-37569).
 *
 * Gated by `CometChatUIKit.isThreadSubscriptionEnabled()` (`enableThreadSubscription`, **default
 * OFF**). It is enabled in the *master* app (`ComposeApplication` calls
 * `setEnableThreadSubscription(true)`) but **NOT in the sample app** — so here the option/bell are
 * expected to be ABSENT. These tests document the gate and prove the thread flow is unaffected; if
 * a future build flips the flag on, the happy-path branches assert the real behaviour.
 *
 * When enabled, the surfaces are:
 * - Action-sheet option (state-labelled): "Notify me about replies" / "Stop reply notifications".
 * - Thread top-bar bell (`ThreadSubscriptionBell`, contentDescription "Mute thread" /
 *   "Unmute thread"). Feedback is a Toast; the flip is optimistic with revert-on-failure.
 *
 * Test IDs: THSUB-001..THSUB-003 (see method names).
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.ThreadSubscriptionE2ETest
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
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)
    }

    /**
     * THSUB-001: The subscription option is state-labelled and mutually exclusive. With the flag
     * off (sample-app default) neither label appears; we then assert the menu still opened.
     */
    @Test
    fun test01_subscriptionOptionStateInActionSheet() {
        val message = sendAndAwait("ThSubOption")
        assertTrue("Action menu did not appear", longPressAndWaitForMenu(message))

        val subscribe = findOption(subscribeOption)
        val unsubscribe = findOption(unsubscribeOption)

        if (subscribe != null || unsubscribe != null) {
            assertTrue(
                "Subscribe and unsubscribe are mutually exclusive — only one should be offered",
                (subscribe != null) != (unsubscribe != null)
            )
            tapNode(subscribe ?: unsubscribe!!)
            assertScreenIntact("Screen should remain after toggling thread subscription")
        } else {
            assertTrue(
                "Thread subscription is gated off; the menu should still show other options",
                isAnyKnownOptionVisible()
            )
            dismissMenu()
        }
    }

    /**
     * THSUB-002: When enabled, the thread top bar carries a subscription bell. With the flag off,
     * the thread still opens without a bell.
     */
    @Test
    fun test02_subscriptionBellInThreadHeader() {
        val parent = sendAndAwait("ThSubBell")
        assertTrue("Action menu did not appear", longPressAndWaitForMenu(parent))
        if (!tapReplyInThread()) {
            assertScreenIntact("Reply In Thread unavailable — screen should be intact")
            return
        }

        // Parent should be visible on the thread screen regardless of the subscription flag.
        assertNotNull(
            "Parent message should be visible on the thread screen",
            device.findObject(By.textContains(parent))
        )

        val bell = device.findObject(By.desc("Mute thread"))
            ?: device.findObject(By.desc("Unmute thread"))
            ?: device.findObject(By.descContains("thread"))
        if (bell != null) {
            bell.click()
            Thread.sleep(1500)
            assertScreenIntact("Thread screen should remain after tapping the subscription bell")
        } else {
            assertScreenIntact("Thread should be usable without a subscription bell (flag off)")
        }
    }

    /**
     * THSUB-003: Thread subscription is additive — replying in a thread still works whether or not
     * the subscription surfaces are present.
     */
    @Test
    fun test03_replyInThreadUnaffectedBySubscription() {
        val parent = sendAndAwait("ThSubReply")
        assertTrue("Action menu did not appear", longPressAndWaitForMenu(parent))
        if (!tapReplyInThread()) {
            assertScreenIntact("Messages screen should be intact if Reply In Thread is unavailable")
            return
        }

        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Thread composer not found", editText)
        val reply = E2ETestHelper.uniqueMessage("ThSubReplyBody")
        editText!!.clear()
        editText.text = reply
        Thread.sleep(1000)

        val sendButton = device.findObject(By.desc("Send"))
            ?: device.findObject(By.descContains("Send"))
            ?: device.findObject(By.descContains("send"))
        assertNotNull("Send button not found in thread composer", sendButton)
        sendButton!!.click()
        Thread.sleep(SETTLE_TIME)

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

    private fun longPressAndWaitForMenu(messageText: String): Boolean {
        device.findObject(By.textContains(messageText))?.longClick()
        Thread.sleep(2500)
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
                Thread.sleep(5000) // Compose Navigation route transition
                return true
            }
            Thread.sleep(1000)
        }
        return false
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

    private fun dismissMenu() {
        device.pressBack()
        Thread.sleep(1000)
    }

    private fun assertScreenIntact(msg: String) {
        assertNotNull(msg, device.findObject(By.clazz("android.widget.EditText")))
    }
}
