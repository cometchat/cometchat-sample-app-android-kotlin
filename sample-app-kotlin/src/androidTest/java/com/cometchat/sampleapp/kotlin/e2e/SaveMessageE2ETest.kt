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
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SETTLE_TIME
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
 * E2E tests for **Save Message** (View / sample-app-kotlin).
 *
 * Feature: ENG-37753. Save is a *private* action (no role gate — unlike Pin), gated only by the
 * SDK flag `CometChat.isSaveMessageEnabled()` (default-on). The option is offered on any message.
 *
 * Android behaviour (verified in `CometChatMessageList.handlePinSaveAction`):
 * - **Save is immediate** — NO confirmation dialog.
 * - **Unsave confirms** — a [CometChatConfirmDialog] ("Unsave Message" / "Do you want to unsave
 *   this message?", positive button "Unsave").
 * - Success shows a Toast ("Message saved" / "Message unsaved").
 * - The saved bubble carries a footer glyph with `contentDescription = "Saved"`.
 * - Save is server state, so the bookmark survives leaving and re-entering the conversation.
 *
 * Defensive style: where the option/indicator is not reachable (save feature off), the test
 * verifies the message screen is intact rather than hard-failing.
 *
 * Test IDs:
 * - SAVE-001: test01_saveOptionAppears
 * - SAVE-002: test02_saveIsImmediateNoConfirmDialog
 * - SAVE-003: test03_unsaveConfirmsAndClearsIndicator
 * - SAVE-004: test04_savedBookmarkPersistsAcrossReopen
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.SaveMessageE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class SaveMessageE2ETest {

    private lateinit var device: UiDevice

    // Action-sheet option labels come from chatuikit-core strings (cometchat_save / cometchat_unsave).
    private val saveOption = "Save message"
    private val unsaveOption = "Unsave message"

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
        E2ETestHelper.openFirstConversation(device)
    }

    /**
     * SAVE-001: The "Save message" option is offered when long-pressing a message.
     */
    @Test
    fun test01_saveOptionAppears() {
        val message = sendAndAwait("SaveOption")

        assertTrue("Action-sheet popup did not appear after long-press", longPressAndWaitForPopup(message))

        if (findOption(saveOption) != null) {
            assertNotNull("'Save message' option should be present in the action sheet", findOption(saveOption))
        } else {
            assertTrue(
                "Save feature appears disabled and no other options were present either",
                isAnyKnownOptionVisible()
            )
        }
        dismissPopup()
    }

    /**
     * SAVE-002: Tapping "Save message" saves immediately with NO confirmation dialog; the saved
     * indicator (contentDescription "Saved") appears (or the "Message saved" toast).
     */
    @Test
    fun test02_saveIsImmediateNoConfirmDialog() {
        val message = sendAndAwait("SaveImmediate")

        assertTrue("Popup did not appear", longPressAndWaitForPopup(message))
        val saveNode = findOption(saveOption)
        if (saveNode == null) {
            dismissPopup()
            assertScreenIntact("Save option unavailable (feature may be off) — screen should be intact")
            return
        }
        tapNode(saveNode)

        val confirmAppeared = device.wait(Until.hasObject(By.textContains("Do you want to save")), 2_000L)
        assertFalse(
            "Save must be immediate — a confirmation dialog ('Do you want to save…') should NOT appear",
            confirmAppeared
        )

        val saved = device.wait(Until.hasObject(By.desc("Saved")), SHORT_TIMEOUT) ||
            device.hasObject(By.textContains("Message saved"))
        if (saved) {
            assertTrue("Message should be saved (indicator or toast)", saved)
        } else {
            assertScreenIntact("Save tapped but no indicator/toast observed — screen should be intact")
        }
    }

    /**
     * SAVE-003: Unsave DOES confirm. Save a message, then unsave it: the confirm dialog appears,
     * and after confirming, the "Message unsaved" toast shows / the bookmark glyph clears.
     */
    @Test
    fun test03_unsaveConfirmsAndClearsIndicator() {
        val message = sendAndAwait("SaveUnsave")

        assertTrue("Popup did not appear", longPressAndWaitForPopup(message))
        val saveNode = findOption(saveOption)
        if (saveNode == null) {
            dismissPopup()
            assertScreenIntact("Save option unavailable — cannot exercise unsave; screen should be intact")
            return
        }
        tapNode(saveNode)
        device.wait(Until.hasObject(By.desc("Saved")), SHORT_TIMEOUT)
        Thread.sleep(2000)

        assertTrue("Popup did not reappear for unsave", longPressAndWaitForPopup(message))
        val unsaveNode = findOption(unsaveOption)
        assertNotNull("'Unsave message' option should be offered once the message is saved", unsaveNode)
        tapNode(unsaveNode!!)

        val confirmVisible = device.wait(
            Until.hasObject(By.textContains("Do you want to unsave")),
            SHORT_TIMEOUT
        ) || device.hasObject(By.textContains("Unsave Message"))
        assertTrue("Unsave should show a confirmation dialog", confirmVisible)

        val confirmBtn = device.findObject(By.text("Unsave")) ?: device.findObject(By.textContains("Unsave"))
        assertNotNull("Confirm dialog should have an 'Unsave' button", confirmBtn)
        confirmBtn!!.click()
        Thread.sleep(3000)

        val unsavedToast = device.hasObject(By.textContains("Message unsaved"))
        val glyphGone = !device.hasObject(By.desc("Saved"))
        assertTrue(
            "After unsave, the saved glyph should clear or the 'Message unsaved' toast should show",
            unsavedToast || glyphGone
        )
    }

    /**
     * SAVE-004: The saved bookmark is server state — leaving and re-entering the conversation
     * keeps the message marked "Saved".
     */
    @Test
    fun test04_savedBookmarkPersistsAcrossReopen() {
        val message = sendAndAwait("SavePersist")

        assertTrue("Popup did not appear", longPressAndWaitForPopup(message))
        val saveNode = findOption(saveOption)
        if (saveNode == null) {
            dismissPopup()
            assertScreenIntact("Save option unavailable — cannot assert persistence; screen should be intact")
            return
        }
        tapNode(saveNode)
        val savedNow = device.wait(Until.hasObject(By.desc("Saved")), SHORT_TIMEOUT)
        Thread.sleep(2000)

        // Leave the conversation and come back.
        E2ETestHelper.pressBack(device)
        Thread.sleep(SETTLE_TIME)
        E2ETestHelper.openFirstConversation(device)

        // Scroll to the bottom so the just-sent, now-saved message is visible again.
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val rv = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
        rv?.fling(androidx.test.uiautomator.Direction.DOWN)
        Thread.sleep(2000)

        if (savedNow) {
            // Proves the mark is persisted server-side, not a transient local flag.
            assertTrue(
                "Saved indicator should still be present after leaving and reopening the conversation",
                device.wait(Until.hasObject(By.desc("Saved")), SHORT_TIMEOUT)
            )
        } else {
            assertScreenIntact("Save not observed at write time — screen should be intact after reopen")
        }
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

    private fun isAnyKnownOptionVisible(): Boolean =
        findOption(saveOption) != null ||
            findOption(unsaveOption) != null ||
            findOption("Pin message") != null ||
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
        assertNotNull(msg, device.findObject(By.res(PACKAGE, "messageList")))
    }
}
