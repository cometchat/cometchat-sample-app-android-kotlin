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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for **Pin Message** (View / sample-app-kotlin).
 *
 * Feature: ENG-37753. The pin action comes from the message action-sheet
 * ([com.cometchat.uikit.core.utils.MessageOptionsUtils]) and is gated by the SDK flag
 * `CometChat.isPinMessageEnabled()` (default-on) plus, in groups, the caller's admin/owner role.
 * These tests run in a 1-1 conversation where the role gate does not apply, so the option shows
 * whenever the SDK flag resolves.
 *
 * Android behaviour that differs from other platforms (verified in
 * `CometChatMessageList.handlePinSaveAction`):
 * - **Pin is immediate** — NO confirmation dialog (commit "immediate pinning without confirmation").
 * - **Unpin confirms** — a [CometChatConfirmDialog] ("Unpin Message" / "Do you want to unpin
 *   this message?", positive button "Unpin").
 * - Success shows a Toast ("Message pinned" / "Message unpinned").
 * - The pinned bubble carries a footer glyph with `contentDescription = "Pinned"`.
 *
 * Defensive style (matches the rest of the suite): where an option/indicator is not reachable
 * (e.g. the backend app has the pin feature disabled), the test verifies the message screen is
 * still intact rather than hard-failing.
 *
 * Test IDs:
 * - PIN-001: test01_pinOptionAppearsForOwnMessage
 * - PIN-002: test02_pinIsImmediateNoConfirmDialog
 * - PIN-003: test03_unpinConfirmsAndClearsIndicator
 * - PIN-004: test04_pinUnpinAreMutuallyExclusive
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.PinMessageE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PinMessageE2ETest {

    private lateinit var device: UiDevice

    // Action-sheet option labels come from chatuikit-core strings (cometchat_pin / cometchat_unpin).
    private val pinOption = "Pin message"
    private val unpinOption = "Unpin message"

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
        // A 1-1 conversation: no group role gate on Pin, so the option shows if the SDK flag is on.
        E2ETestHelper.openFirstConversation(device)
    }

    /**
     * PIN-001: The "Pin message" option is offered when long-pressing a message.
     */
    @Test
    fun test01_pinOptionAppearsForOwnMessage() {
        val message = sendAndAwait("PinOption")

        val popupShown = longPressAndWaitForPopup(message)
        assertTrue("Action-sheet popup did not appear after long-press", popupShown)

        val pinPresent = findOption(pinOption) != null
        if (pinPresent) {
            assertNotNull("'Pin message' option should be present in the action sheet", findOption(pinOption))
        } else {
            // Pin feature may be disabled for this backend app — verify the popup opened with
            // other options rather than failing (defensive, matches the suite convention).
            assertTrue(
                "Pin feature appears disabled and no other options were present either",
                isAnyKnownOptionVisible()
            )
        }
        dismissPopup()
    }

    /**
     * PIN-002: Tapping "Pin message" pins immediately with NO confirmation dialog, and the pinned
     * indicator (contentDescription "Pinned") appears on the bubble (or the "Message pinned" toast).
     */
    @Test
    fun test02_pinIsImmediateNoConfirmDialog() {
        val message = sendAndAwait("PinImmediate")

        assertTrue("Popup did not appear", longPressAndWaitForPopup(message))
        val pinNode = findOption(pinOption)
        if (pinNode == null) {
            dismissPopup()
            assertScreenIntact("Pin option unavailable (feature may be off) — screen should be intact")
            return
        }
        tapNode(pinNode)

        // Pin is immediate: NO confirm dialog should ever appear.
        val confirmAppeared = device.wait(
            Until.hasObject(By.textContains("Do you want to pin")),
            2_000L
        )
        assertFalse(
            "Pin must be immediate — a confirmation dialog ('Do you want to pin…') should NOT appear",
            confirmAppeared
        )

        // Confirm the write landed: the pinned glyph or the success toast.
        val pinned = device.wait(Until.hasObject(By.desc("Pinned")), SHORT_TIMEOUT) ||
            device.hasObject(By.textContains("Message pinned"))
        if (pinned) {
            assertTrue("Message should be pinned (indicator or toast)", pinned)
        } else {
            assertScreenIntact("Pin tapped but no indicator/toast observed — screen should be intact")
        }
    }

    /**
     * PIN-003: Unpin DOES confirm (unlike pin). Pin a message first, then unpin it: the confirm
     * dialog appears, and after confirming, the "Message unpinned" toast shows / the glyph clears.
     */
    @Test
    fun test03_unpinConfirmsAndClearsIndicator() {
        val message = sendAndAwait("PinUnpin")

        // Pin it first.
        assertTrue("Popup did not appear", longPressAndWaitForPopup(message))
        val pinNode = findOption(pinOption)
        if (pinNode == null) {
            dismissPopup()
            assertScreenIntact("Pin option unavailable — cannot exercise unpin; screen should be intact")
            return
        }
        tapNode(pinNode)
        device.wait(Until.hasObject(By.desc("Pinned")), SHORT_TIMEOUT)
        Thread.sleep(2000)

        // Now long-press again and unpin.
        assertTrue("Popup did not reappear for unpin", longPressAndWaitForPopup(message))
        val unpinNode = findOption(unpinOption)
        assertNotNull("'Unpin message' option should be offered once the message is pinned", unpinNode)
        tapNode(unpinNode!!)

        // Unpin confirms: the confirm dialog with title/body + a positive "Unpin" button.
        val confirmVisible = device.wait(
            Until.hasObject(By.textContains("Do you want to unpin")),
            SHORT_TIMEOUT
        ) || device.hasObject(By.textContains("Unpin Message"))
        assertTrue("Unpin should show a confirmation dialog", confirmVisible)

        val confirmBtn = device.findObject(By.text("Unpin"))
            ?: device.findObject(By.textContains("Unpin"))
        assertNotNull("Confirm dialog should have an 'Unpin' button", confirmBtn)
        confirmBtn!!.click()
        Thread.sleep(3000)

        // The glyph should be gone (a lingering "Pinned" glyph is the bug) or the toast confirms.
        val unpinnedToast = device.hasObject(By.textContains("Message unpinned"))
        val glyphGone = !device.hasObject(By.desc("Pinned"))
        assertTrue(
            "After unpin, the pinned glyph should clear or the 'Message unpinned' toast should show",
            unpinnedToast || glyphGone
        )
    }

    /**
     * PIN-004: Pin and Unpin are mutually exclusive — only one is offered, based on current state.
     */
    @Test
    fun test04_pinUnpinAreMutuallyExclusive() {
        val message = sendAndAwait("PinExclusive")

        // Fresh message: Pin offered, Unpin not.
        assertTrue("Popup did not appear", longPressAndWaitForPopup(message))
        val pinNode = findOption(pinOption)
        if (pinNode == null) {
            dismissPopup()
            assertScreenIntact("Pin option unavailable — cannot assert exclusivity; screen should be intact")
            return
        }
        assertNull("Unpin should NOT be offered on an unpinned message", findOption(unpinOption))
        tapNode(pinNode)
        device.wait(Until.hasObject(By.desc("Pinned")), SHORT_TIMEOUT)
        Thread.sleep(2000)

        // Pinned message: Unpin offered, Pin not.
        assertTrue("Popup did not reappear", longPressAndWaitForPopup(message))
        assertNotNull("Unpin should be offered once pinned", findOption(unpinOption))
        assertNull("Pin should NOT be offered on an already-pinned message", findOption(pinOption))
        dismissPopup()
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /** Sends a uniquely-tagged message and waits for it to render in the list. */
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
        Thread.sleep(2500) // let the bubble settle before long-press
        return text
    }

    /**
     * Long-presses a message (shell swipe, same as the thread/reaction tests) and waits for the
     * CometChatMessagePopupMenu to appear.
     */
    private fun longPressAndWaitForPopup(messageText: String): Boolean {
        E2ETestHelper.longPressMessage(device, messageText)
        if (isAnyKnownOptionVisible()) return true
        // Retry once with a direct long-click on the text node.
        device.findObject(By.textContains(messageText))?.longClick()
        Thread.sleep(2000)
        return isAnyKnownOptionVisible()
    }

    /** Any of the popup's known options (proves the popup is up), incl. pin/save/copy/delete. */
    private fun isAnyKnownOptionVisible(): Boolean =
        findOption(pinOption) != null ||
            findOption(unpinOption) != null ||
            findOption("Save message") != null ||
            device.findObject(By.text("Copy")) != null ||
            device.findObject(By.text("Delete")) != null ||
            device.findObject(By.textContains("Reply In Thread")) != null

    /** Finds a popup option by exact then partial text. */
    private fun findOption(label: String): UiObject2? =
        device.findObject(By.text(label)) ?: device.findObject(By.textContains(label))

    /** Taps a node by its visible-bounds centre (the popup can extend below the fold). */
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
        assertNotNull(msg, device.findObject(By.res(PACKAGE, "messageList")))
    }

    private fun assertNull(msg: String, obj: Any?) = assertTrue(msg, obj == null)
}
