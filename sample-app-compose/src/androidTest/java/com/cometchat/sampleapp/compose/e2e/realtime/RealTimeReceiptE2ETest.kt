package com.cometchat.sampleapp.compose.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.RestApiHelper
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time receipt E2E tests (sample-app-compose).
 *
 * Covers sheet gaps:
 * - RT-RCPT-001  Sent receipt (single tick) shown after the app user sends a message
 * - RT-RCPT-006  Conversation-list last message reflects the just-sent (outgoing) message
 *
 * The app's own outgoing message is created via REST on behalf of [appUid]. Compose receipts
 * expose a contentDescription ("Message sent" / "Message delivered" / "Message read"), so the
 * receipt tick IS readable here (unlike the kotlin View twin).
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
        RestApiHelper.sendMessage(appUid, partnerUid, tag) // outgoing
        assertTrue("Outgoing message should appear after sending", pollForMessageInChat(tag, 45_000))

        assertTrue(
            "A receipt tick (sent/delivered/read) should be shown on the outgoing message",
            poll(30_000) {
                E2ETestHelper.scrollDown(device)
                device.findObjects(By.descContains("Message sent")).isNotEmpty() ||
                    device.findObjects(By.descContains("Message delivered")).isNotEmpty() ||
                    device.findObjects(By.descContains("Message read")).isNotEmpty()
            }
        )
    }

    // ─── RT-RCPT-006: Conversation-list last message = the sent message ────────────
    @Test
    fun test02_receiptOnConversationListLastMessage() {
        val tag = "RcptPv_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(appUid, partnerUid, tag) // outgoing
        E2ETestHelper.navigateToTab(device, "Chats")
        Thread.sleep(2000)
        // Check the preview directly (scroll to the top of the list) — avoids the tab-toggling
        // fallback in verifyConversationPreview which can hit the Compose "Chats tab" nav race.
        assertTrue(
            "Conversation-list last message should reflect the just-sent message",
            poll(45_000) {
                E2ETestHelper.scrollUp(device)
                device.findObjects(By.textContains(tag)).isNotEmpty()
            }
        )
    }
}
