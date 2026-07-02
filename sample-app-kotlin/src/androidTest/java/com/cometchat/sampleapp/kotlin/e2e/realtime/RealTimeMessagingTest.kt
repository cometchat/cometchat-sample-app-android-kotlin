package com.cometchat.sampleapp.kotlin.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.CometChatJsDriver
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.kotlin.e2e.helpers.RestApiHelper
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time messaging E2E tests (sample-app-kotlin).
 *
 * Revamp of multidevice/RealTimeMessagingTest: instead of a second emulator acting as the
 * "sender", the app stays logged in as the primary user (`dhruv`) with a live WebSocket and
 * the partner user (`d1`) is driven via [RestApiHelper] (CometChat REST API). The server
 * pushes each partner action to the app's socket → the UI updates in real time → we verify.
 *
 * No `-e role`, no `@Assume` gating, no second device — runs in the normal connected suite.
 *
 * Covers:
 * - E2E-006: New message moves conversation to top
 * - E2E-020: Receive text message in real-time
 * - E2E-029: Typing indicator appears  (NOT automatable single-device — see test03)
 * - E2E-037: Partner reaction appears in real-time
 * - E2E-045: Delivered receipt (verified server-side; icon not in a11y tree)
 * - E2E-047: Read receipt (verified server-side; icon not in a11y tree)
 * - E2E-077: Receive media message in real-time
 * - 1TO1-039: Partner deletes message in real-time
 *
 * Requires a fullAccess REST API Key (hardcoded default in [RestApiHelper], overridable via
 * -Pandroid.testInstrumentationRunnerArguments.restApiKey=...).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RealTimeMessagingTest {

    private lateinit var device: UiDevice

    private val appUid: String get() = E2ETestConfig.LOGGED_IN_UID
    private val partnerUid: String get() = E2ETestConfig.ONE_TO_ONE_UID

    // A reachable public file used for the media test (the displayed name comes from `name`).
    private val mediaUrl = E2ETestConfig.MEDIA_FILE_URL

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device, uid = appUid)
    }

    @After
    fun stopDriver() {
        CometChatJsDriver.stop()
    }

    // ─── E2E-006: Conversation moves to top on new message ───────────────────────

    @Test
    fun test01_conversationMovesToTopOnNewMessage() {
        E2ETestHelper.navigateToTab(device, "Chats")
        Thread.sleep(2000)

        val tag = "MvTop_${System.currentTimeMillis()}"
        val baseline = device.findObjects(By.textContains("MvTop_")).size

        RestApiHelper.sendMessage(partnerUid, appUid, tag)

        assertTrue(
            "Conversation preview did not update with the new message within 45s",
            pollForText("MvTop_", timeoutMs = 45_000, baseline = baseline)
        )

        // Verify it bubbled to the TOP: first conversation row should contain the message.
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val rv = device.findObject(By.res(uikitPackage, "recyclerview_conversations_list"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_conversations_list"))
        assertNotNull("Conversations RecyclerView not found", rv)
        val firstRow = rv!!.children.firstOrNull()
        assertNotNull("Conversations list is empty", firstRow)
        val topHasMessage = firstRow!!.findObjects(By.clazz("android.widget.TextView"))
            .any { it.text?.contains("MvTop_") == true }
        assertTrue("Conversation with the new message should be at the TOP of the list", topHasMessage)
    }

    // ─── E2E-020: Receive text message in real-time ──────────────────────────────

    @Test
    fun test02_receiveTextMessageRealTime() {
        openPartnerChat()

        val tag = "RT_${System.currentTimeMillis()}"
        val baseline = device.findObjects(By.textContains(tag)).size

        RestApiHelper.sendMessage(partnerUid, appUid, tag)

        assertTrue(
            "Real-time text message did not appear in the open chat within 45s",
            pollForText(tag, timeoutMs = 45_000, baseline = baseline)
        )
    }

    // ─── E2E-029: Typing indicator appears ───────────────────────────────────────

    /**
     * Typing has no REST endpoint, so the partner is driven by [CometChatJsDriver], which runs
     * the CometChat JS SDK in an in-test WebView (an independent second session). We open the
     * partner chat first, then the driver logs in as the partner and loops startTyping → the
     * app's header shows the typing indicator ("Typing…").
     */
    @Test
    fun test03_typingIndicatorAppears() {
        openPartnerChat()

        val driving = CometChatJsDriver.start(
            uid = partnerUid, action = "typing", receiver = appUid, receiverType = "user"
        )
        assertTrue(
            "JS driver could not log in as $partnerUid (status=${CometChatJsDriver.status()})",
            driving
        )

        assertTrue(
            "Typing indicator did not appear in the header",
            pollForText("yping", timeoutMs = 30_000)
        )
    }

    // ─── E2E-037: Partner reaction appears in real-time ──────────────────────────

    @Test
    fun test04_partnerReactionUpdates() {
        openPartnerChat()

        val tag = "Rct_${System.currentTimeMillis()}"
        val messageId = RestApiHelper.sendMessage(partnerUid, appUid, tag)
        assertTrue("Reaction-target message did not arrive within 45s", pollForText(tag, 45_000))

        RestApiHelper.addReaction(partnerUid, messageId, "👍")

        assertTrue(
            "Partner's reaction did not appear on the message within 30s",
            pollForText("👍", timeoutMs = 30_000)
        )
    }

    // ─── E2E-045: Delivered receipt updates ──────────────────────────────────────

    /**
     * The receipt icon (sent/delivered/read) is a pure drawable with no contentDescription
     * or testTag, so it is invisible to UIAutomator. We instead verify the receipt
     * server-side: the app (dhruv) sends to the partner, the partner marks the conversation
     * read via REST (which also sets deliveredAt), and we poll the message until delivered.
     */
    @Test
    fun test05_deliveredIndicatorUpdates() {
        openPartnerChat()

        val tag = "Dlvr_${System.currentTimeMillis()}"
        val messageId = RestApiHelper.sendMessage(appUid, partnerUid, tag) // outgoing from app user
        assertTrue("Own outgoing message not visible within 45s", pollForText(tag, 45_000))

        // No REST mark-delivered endpoint exists; mark-read also sets deliveredAt.
        RestApiHelper.markUserConversationRead(onBehalfOf = partnerUid, withUid = appUid, messageId = messageId)

        assertTrue(
            "Message was not marked delivered after the partner's receipt within 30s",
            poll(30_000) { RestApiHelper.isDelivered(appUid, messageId) }
        )
    }

    // ─── E2E-047: Read receipt updates ───────────────────────────────────────────

    @Test
    fun test06_readIndicatorUpdates() {
        openPartnerChat()

        val tag = "RdMe_${System.currentTimeMillis()}"
        val messageId = RestApiHelper.sendMessage(appUid, partnerUid, tag) // outgoing from app user
        assertTrue("Own outgoing message not visible within 45s", pollForText(tag, 45_000))

        RestApiHelper.markUserConversationRead(onBehalfOf = partnerUid, withUid = appUid, messageId = messageId)

        assertTrue(
            "Message was not marked read after the partner's receipt within 30s",
            poll(30_000) { RestApiHelper.isRead(appUid, messageId) }
        )
    }

    // ─── E2E-077: Receive media message in real-time ─────────────────────────────

    @Test
    fun test07_receiveMediaRealTime() {
        openPartnerChat()

        // Use a file-type message so the bubble shows the file name (assertable as text).
        val name = "RTmedia_${System.currentTimeMillis()}.pdf"
        RestApiHelper.sendMediaMessage(
            sender = partnerUid,
            receiver = appUid,
            fileUrl = mediaUrl,
            type = "file",
            name = name,
            mimeType = "application/pdf"
        )

        assertTrue(
            "Real-time media (file) message did not appear within 60s",
            pollForText("RTmedia_", timeoutMs = 60_000)
        )
    }

    // ─── 1TO1-039: Partner deletes message in real-time ──────────────────────────

    @Test
    fun test08_peerDeletesMessageRealTime() {
        openPartnerChat()

        val tag = "DelRT_${System.currentTimeMillis()}"
        val messageId = RestApiHelper.sendMessage(partnerUid, appUid, tag)
        assertTrue("Message did not arrive before delete within 45s", pollForText(tag, 45_000))

        RestApiHelper.deleteMessage(partnerUid, messageId)

        val deleted = poll(30_000) {
            device.findObject(By.textContains(tag)) == null ||
                device.findObject(By.textContains("deleted")) != null ||
                device.findObject(By.textContains("This message was deleted")) != null
        }
        assertTrue("Message should show a deleted state in real time within 30s", deleted)
    }

    // ─── E2E-042: Partner's thread reply appears in real-time (1:1) ───────────────

    @Test
    fun test09_partnerThreadReplyAppearsRealTime() {
        openPartnerChat()

        // Parent posted by the app user so the partner (d1) can reply in its thread
        // (d1 threading its OWN 1:1 message is rejected with HTTP 403 ERR_MESSAGE_NO_ACCESS).
        // Unique text so we open OUR parent's thread, not an older message that also has replies.
        val parentText = "ThreadParent_${System.currentTimeMillis()}"
        val parentId = RestApiHelper.sendMessage(appUid, partnerUid, parentText)
        assertTrue("Thread parent message not visible", pollForText(parentText, 45_000))
        Thread.sleep(2500) // let the parent become thread-accessible before replying

        val replyText = "ThreadReply_${System.currentTimeMillis()}"
        RestApiHelper.sendThreadReply(partnerUid, parentId, appUid, replyText, receiverType = "user")

        // Open the thread of OUR parent specifically: tap the reply indicator located directly
        // below our parent bubble (not any older message's thread), then verify the reply inside.
        var replyVisible = false
        val deadline = System.currentTimeMillis() + 75_000
        while (System.currentTimeMillis() < deadline && !replyVisible) {
            if (device.findObjects(By.textContains(replyText)).isNotEmpty()) { replyVisible = true; break }
            val pb = device.findObject(By.textContains(parentText))?.let { runCatching { it.visibleBounds }.getOrNull() }
            val indicator = if (pb != null) {
                device.findObjects(By.textContains("Repl"))
                    .mapNotNull { runCatching { it.visibleBounds }.getOrNull() }
                    .filter { it.top in (pb.bottom - 20)..(pb.bottom + 250) } // indicator just below our parent
                    .minByOrNull { it.top }
            } else null
            if (indicator != null) {
                device.click(indicator.centerX(), indicator.centerY())
                Thread.sleep(3000)
            } else {
                Thread.sleep(2000)
            }
        }
        assertTrue("Partner's thread reply not visible in the thread of the message we sent", replyVisible)
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /**
     * Opens the 1:1 chat with the partner. Seeds a message via REST first so the
     * conversation exists and is at the top of the Chats list, then opens it.
     */
    private fun openPartnerChat() {
        RestApiHelper.sendMessage(partnerUid, appUid, "seed ${System.currentTimeMillis()}")
        Thread.sleep(2000)
        E2ETestHelper.navigateToTab(device, "Chats")
        E2ETestHelper.openFirstConversation(device)
    }

    /** Polls [cond] until true or [timeoutMs] elapses. Exceptions in [cond] are treated as false. */
    private fun poll(timeoutMs: Long, intervalMs: Long = 1500, cond: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            try {
                if (cond()) return true
            } catch (_: Exception) { /* retry */ }
            Thread.sleep(intervalMs)
        }
        return false
    }

    /** Polls until the number of on-screen nodes containing [snippet] exceeds [baseline]. */
    private fun pollForText(snippet: String, timeoutMs: Long = 30_000, baseline: Int = 0): Boolean =
        poll(timeoutMs) { device.findObjects(By.textContains(snippet)).size > baseline }
}
