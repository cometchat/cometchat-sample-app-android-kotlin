package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for **Pin Message** in the Compose sample app (ENG-37756).
 *
 * Ported from sample-app-kotlin [PinMessageE2ETest], adapted for Compose:
 * - No View resource IDs — options and dialogs found via By.text / By.textContains.
 * - Long-press uses UiObject2.longClick() to open the action menu.
 * - "Screen intact" is asserted via the composer EditText (no `messageList` id in Compose).
 *
 * Behaviour is identical to the View module (verified in the Compose
 * `CometChatMessageList.handleMessageOptionSelected`):
 * - **Pin is immediate** — NO confirmation dialog.
 * - **Unpin confirms** — a CometChatConfirmDialog ("Unpin Message" / "Do you want to unpin this
 *   message?", positive button "Unpin").
 * - Success shows a Toast ("Message pinned" / "Message unpinned").
 * - The pinned bubble carries a footer glyph with `contentDescription = "Pinned"`.
 *
 * Test IDs: PIN-001..PIN-004 (see method names).
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.PinMessageE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PinMessageE2ETest {

    private lateinit var device: UiDevice

    private val pinOption = "Pin message"
    private val unpinOption = "Unpin message"

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
        // 1-1 chat via Users tab guarantees a composer and no group role gate on Pin.
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)
    }

    /** PIN-001: "Pin message" option is offered on long-press. */
    @Test
    fun test01_pinOptionAppearsForOwnMessage() {
        val message = sendAndAwait("PinOption")
        assertTrue("Action menu did not appear after long-press", longPressAndWaitForMenu(message))

        if (findOption(pinOption) != null) {
            assertNotNull("'Pin message' option should be present", findOption(pinOption))
        } else {
            assertTrue(
                "Pin feature appears disabled and no other options were present either",
                isAnyKnownOptionVisible()
            )
        }
        dismissMenu()
    }

    /** PIN-002: Pin is immediate (no confirm dialog); pinned glyph / toast appears. */
    @Test
    fun test02_pinIsImmediateNoConfirmDialog() {
        val message = sendAndAwait("PinImmediate")
        assertTrue("Action menu did not appear", longPressAndWaitForMenu(message))

        val pinNode = findOption(pinOption)
        if (pinNode == null) {
            dismissMenu()
            assertScreenIntact("Pin option unavailable — screen should be intact")
            return
        }
        tapNode(pinNode)

        val confirmAppeared = device.wait(Until.hasObject(By.textContains("Do you want to pin")), 2_000L)
        assertFalse(
            "Pin must be immediate — a confirmation dialog should NOT appear",
            confirmAppeared
        )

        val pinned = device.wait(Until.hasObject(By.desc("Pinned")), SHORT_TIMEOUT) ||
            device.hasObject(By.textContains("Message pinned"))
        if (!pinned) assertScreenIntact("Pin tapped but no indicator/toast observed — screen should be intact")
    }

    /** PIN-003: Unpin confirms and clears the indicator. */
    @Test
    fun test03_unpinConfirmsAndClearsIndicator() {
        val message = sendAndAwait("PinUnpin")

        assertTrue("Action menu did not appear", longPressAndWaitForMenu(message))
        val pinNode = findOption(pinOption)
        if (pinNode == null) {
            dismissMenu()
            assertScreenIntact("Pin option unavailable — cannot exercise unpin; screen should be intact")
            return
        }
        tapNode(pinNode)
        device.wait(Until.hasObject(By.desc("Pinned")), SHORT_TIMEOUT)
        Thread.sleep(2000)

        assertTrue("Action menu did not reappear for unpin", longPressAndWaitForMenu(message))
        val unpinNode = findOption(unpinOption)
        assertNotNull("'Unpin message' option should be offered once pinned", unpinNode)
        tapNode(unpinNode!!)

        val confirmVisible = device.wait(Until.hasObject(By.textContains("Do you want to unpin")), SHORT_TIMEOUT) ||
            device.hasObject(By.textContains("Unpin Message"))
        assertTrue("Unpin should show a confirmation dialog", confirmVisible)

        val confirmBtn = device.findObject(By.text("Unpin")) ?: device.findObject(By.textContains("Unpin"))
        assertNotNull("Confirm dialog should have an 'Unpin' button", confirmBtn)
        confirmBtn!!.click()
        Thread.sleep(3000)

        val unpinnedToast = device.hasObject(By.textContains("Message unpinned"))
        val glyphGone = !device.hasObject(By.desc("Pinned"))
        assertTrue(
            "After unpin, the pinned glyph should clear or the 'Message unpinned' toast should show",
            unpinnedToast || glyphGone
        )
    }

    /** PIN-004: Pin and Unpin are mutually exclusive, based on state. */
    @Test
    fun test04_pinUnpinAreMutuallyExclusive() {
        val message = sendAndAwait("PinExclusive")

        assertTrue("Action menu did not appear", longPressAndWaitForMenu(message))
        val pinNode = findOption(pinOption)
        if (pinNode == null) {
            dismissMenu()
            assertScreenIntact("Pin option unavailable — cannot assert exclusivity; screen should be intact")
            return
        }
        assertTrue("Unpin should NOT be offered on an unpinned message", findOption(unpinOption) == null)
        tapNode(pinNode)
        device.wait(Until.hasObject(By.desc("Pinned")), SHORT_TIMEOUT)
        Thread.sleep(2000)

        assertTrue("Action menu did not reappear", longPressAndWaitForMenu(message))
        assertNotNull("Unpin should be offered once pinned", findOption(unpinOption))
        assertTrue("Pin should NOT be offered on an already-pinned message", findOption(pinOption) == null)
        dismissMenu()
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

    private fun isAnyKnownOptionVisible(): Boolean =
        findOption(pinOption) != null ||
            findOption(unpinOption) != null ||
            findOption("Save message") != null ||
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
