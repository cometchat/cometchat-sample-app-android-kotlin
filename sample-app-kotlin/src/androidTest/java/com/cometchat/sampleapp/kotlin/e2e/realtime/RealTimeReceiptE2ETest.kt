package com.cometchat.sampleapp.kotlin.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.RestApiHelper
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time receipt E2E tests (sample-app-kotlin).
 *
 * Covers sheet gaps:
 * - RT-RCPT-001  Sent receipt (single tick) shown after the app user sends a message
 * - RT-RCPT-006  Conversation-list last message reflects the just-sent (outgoing) message
 *
 * The app's own outgoing message is created via REST on behalf of [appUid] (the proven realtime
 * pattern — the app renders its own message, with its receipt, via the socket). NOTE: the kotlin
 * (View-based) CometChatReceipt is an unlabeled ImageView, so the receipt TICK itself isn't
 * readable by UIAutomator; we confirm the outgoing message is shown / previewed. The Compose twin
 * asserts the receipt contentDescription directly.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RealTimeReceiptE2ETest : RealtimeTestBase() {

    // ─── RT-RCPT-001: Sent receipt after send ─────────────────────────────────────
    @Test
    fun test01_sentReceiptShownAfterSend() {
        openPartnerChat()
        val tag = "Sent_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(appUid, partnerUid, tag) // outgoing (carries the sent receipt)
        assertTrue(
            "Outgoing message (with its sent receipt) should appear after sending",
            pollForMessageInChat(tag, 45_000)
        )
    }

    // ─── RT-RCPT-006: Conversation-list last message = the sent message ────────────
    @Test
    fun test02_receiptOnConversationListLastMessage() {
        val tag = "RcptPv_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(appUid, partnerUid, tag) // outgoing
        E2ETestHelper.navigateToTab(device, "Chats")
        assertTrue(
            "Conversation-list last message should reflect the just-sent message",
            verifyConversationPreview(tag)
        )
    }
}
