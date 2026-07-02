package com.cometchat.sampleapp.kotlin.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.RestApiHelper
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time E2E tests — extended gap coverage (sample-app-kotlin).
 *
 * Same model as [RealTimeMessagingTest]: the app stays logged in as [appUid] with a live
 * WebSocket; partner ([partnerUid]) actions are driven via the CometChat REST API, so the
 * server pushes events to the app and the UI updates in real time.
 *
 * Covers the following sheet gaps:
 * - RT-EDIT-001  Partner edits message — app sees the new text live
 * - RT-EDIT-002  Edited message shows the "Edited" label
 * - RT-EDIT-003  Edit updates the conversation-list preview
 * - RT-REACT-003 Remove reaction — peer sees the removal
 * - RT-MSG-004   Long text message (1000+ chars) received
 * - RT-MSG-005   Emoji-only message received
 * - RT-DEL-004   Delete updates the conversation-list preview
 * - RT-DEL-005   Delete a message that has thread replies
 * - RT-THREAD-002 Thread reply does NOT appear in the main message list
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RealTimeExtendedE2ETest {

    private lateinit var device: UiDevice
    private val appUid: String get() = E2ETestConfig.LOGGED_IN_UID
    private val partnerUid: String get() = E2ETestConfig.ONE_TO_ONE_UID

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device, uid = appUid)
    }

    // ─── RT-EDIT-001: Partner edits message — app sees new text live ──────────────
    @Test
    fun test01_partnerEditAppearsRealTime() {
        openPartnerChat()
        val original = "EditOrig_${System.currentTimeMillis()}"
        val id = RestApiHelper.sendMessage(partnerUid, appUid, original)
        assertTrue("Original message not visible within 45s", pollForText(original, 45_000))

        val edited = "EditNew_${System.currentTimeMillis()}"
        RestApiHelper.editMessage(partnerUid, id, edited)
        assertTrue("Edited text did not appear in real time within 30s", pollForText(edited, 30_000))
    }

    // ─── RT-EDIT-002: Edited message shows the "Edited" label ─────────────────────
    @Test
    fun test02_editedMessageShowsLabel() {
        openPartnerChat()
        val original = "EdLbl_${System.currentTimeMillis()}"
        val id = RestApiHelper.sendMessage(partnerUid, appUid, original)
        assertTrue("Original message not visible within 45s", pollForText(original, 45_000))

        RestApiHelper.editMessage(partnerUid, id, "EdLblNew_${System.currentTimeMillis()}")
        assertTrue("'Edited' label did not appear after edit within 30s", pollForText("Edited", 30_000))
    }

    // ─── RT-EDIT-003: Edit updates the conversation-list preview ──────────────────
    @Test
    fun test03_editUpdatesConversationPreview() {
        openPartnerChat()
        val original = "EdPv_${System.currentTimeMillis()}"
        val id = RestApiHelper.sendMessage(partnerUid, appUid, original)
        assertTrue("Original message not visible within 45s", pollForText(original, 45_000))

        val edited = "EdPvNew_${System.currentTimeMillis()}"
        RestApiHelper.editMessage(partnerUid, id, edited)

        // Back to the Chats list and verify the preview reflects the edited text.
        device.pressBack(); Thread.sleep(1500)
        E2ETestHelper.navigateToTab(device, "Chats")
        assertTrue("Conversation preview did not update to the edited text within 30s",
            pollForText(edited, 30_000))
    }

    // ─── RT-REACT-003: Remove reaction — peer sees the removal ────────────────────
    @Test
    fun test04_removeReactionPeerSeesRemoval() {
        openPartnerChat()
        val tag = "RmRct_${System.currentTimeMillis()}"
        val id = RestApiHelper.sendMessage(partnerUid, appUid, tag)
        assertTrue("Reaction-target message not visible within 45s", pollForText(tag, 45_000))

        RestApiHelper.addReaction(partnerUid, id, "👍")
        assertTrue("Reaction did not appear within 30s", pollForText("👍", 30_000))

        RestApiHelper.removeReaction(partnerUid, id, "👍")
        assertTrue("Reaction did not disappear after removal within 30s", pollForTextGone("👍", 30_000))
    }

    // ─── RT-MSG-004: Long text message (1000+ chars) received ─────────────────────
    @Test
    fun test05_longTextMessageReceived() {
        openPartnerChat()
        val marker = "ENDLONG${System.currentTimeMillis()}"
        // Realistic long text: spaced words (so the TextView wraps normally) totalling >1000 chars,
        // with a unique marker word at the end that lands on the last line (visible after auto-scroll).
        val longText = "lorem ipsum dolor sit amet ".repeat(45) + marker // ~1215 chars + marker
        RestApiHelper.sendMessage(partnerUid, appUid, longText)
        assertTrue("Long (1000+ char) message did not appear within 45s", pollForText(marker, 45_000))
    }

    // ─── RT-MSG-005: Emoji-only message received ──────────────────────────────────
    @Test
    fun test06_emojiOnlyMessageReceived() {
        openPartnerChat()
        val baseline = device.findObjects(By.textContains("🎉")).size
        RestApiHelper.sendMessage(partnerUid, appUid, "🎉🎊🥳")
        assertTrue("Emoji-only message did not appear within 45s",
            pollForTextBaseline("🎉", 45_000, baseline))
    }

    // ─── RT-DEL-004: Delete updates the conversation-list preview ──────────────────
    @Test
    fun test07_deleteUpdatesConversationPreview() {
        openPartnerChat()
        val tag = "DelPv_${System.currentTimeMillis()}"
        val id = RestApiHelper.sendMessage(partnerUid, appUid, tag)
        assertTrue("Message not visible within 45s", pollForText(tag, 45_000))

        device.pressBack(); Thread.sleep(1500)
        E2ETestHelper.navigateToTab(device, "Chats")
        assertTrue("Preview should first show the message", pollForText(tag, 30_000))

        RestApiHelper.deleteMessage(partnerUid, id)
        val updated = poll(30_000) {
            device.findObjects(By.textContains(tag)).isEmpty() ||
                device.findObject(By.textContains("deleted")) != null
        }
        assertTrue("Conversation preview did not update after delete within 30s", updated)
    }

    // ─── RT-DEL-005: Delete a message that has thread replies ─────────────────────
    @Test
    fun test08_deleteMessageWithReplies() {
        openPartnerChat()
        // Parent posted by the app user so the partner can reply in its thread (1:1 own-thread is 403).
        val parent = "DelRepl_${System.currentTimeMillis()}"
        val parentId = RestApiHelper.sendMessage(appUid, partnerUid, parent)
        assertTrue("Parent message not visible within 45s", pollForText(parent, 45_000))
        Thread.sleep(2500)

        RestApiHelper.sendThreadReply(
            partnerUid, parentId, appUid, "Rep_${System.currentTimeMillis()}", receiverType = "user"
        )
        Thread.sleep(2500)

        RestApiHelper.deleteMessage(appUid, parentId)
        val deleted = poll(30_000) {
            device.findObjects(By.textContains(parent)).isEmpty() ||
                device.findObject(By.textContains("deleted")) != null
        }
        assertTrue("Parent (with replies) did not show a deleted state within 30s", deleted)
    }

    // ─── RT-THREAD-002: Thread reply does NOT appear in the main message list ──────
    @Test
    fun test09_threadReplyNotInMainList() {
        openPartnerChat()
        val parent = "ThParent_${System.currentTimeMillis()}"
        val parentId = RestApiHelper.sendMessage(appUid, partnerUid, parent)
        assertTrue("Parent message not visible within 45s", pollForText(parent, 45_000))
        Thread.sleep(2500)

        val reply = "ThReply_${System.currentTimeMillis()}"
        RestApiHelper.sendThreadReply(partnerUid, parentId, appUid, reply, receiverType = "user")

        // The reply registers on the parent (reply-count indicator appears) ...
        assertTrue("Thread reply indicator did not appear on the parent within 45s",
            pollForText("Repl", 45_000))

        // ... but the reply text itself must NOT show in the main message list.
        Thread.sleep(2000)
        assertTrue(
            "Thread reply should NOT appear in the main message list",
            device.findObjects(By.textContains(reply)).isEmpty()
        )
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────
    private fun openPartnerChat() {
        RestApiHelper.sendMessage(partnerUid, appUid, "seed ${System.currentTimeMillis()}")
        Thread.sleep(2000)
        E2ETestHelper.navigateToTab(device, "Chats")
        E2ETestHelper.openFirstConversation(device)
    }

    private fun poll(timeoutMs: Long, intervalMs: Long = 1500, cond: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            try { if (cond()) return true } catch (_: Exception) { /* retry */ }
            Thread.sleep(intervalMs)
        }
        return false
    }

    private fun pollForText(snippet: String, timeoutMs: Long = 30_000): Boolean =
        poll(timeoutMs) { device.findObjects(By.textContains(snippet)).isNotEmpty() }

    private fun pollForTextBaseline(snippet: String, timeoutMs: Long, baseline: Int): Boolean =
        poll(timeoutMs) { device.findObjects(By.textContains(snippet)).size > baseline }

    private fun pollForTextGone(snippet: String, timeoutMs: Long): Boolean =
        poll(timeoutMs) { device.findObjects(By.textContains(snippet)).isEmpty() }
}
