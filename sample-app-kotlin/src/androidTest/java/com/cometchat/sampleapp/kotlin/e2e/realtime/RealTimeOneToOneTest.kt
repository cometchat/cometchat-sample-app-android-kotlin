package com.cometchat.sampleapp.kotlin.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.sampleapp.kotlin.e2e.helpers.CometChatJsDriver
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.RestApiHelper
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time 1:1 E2E tests (sample-app-kotlin).
 *
 * Revamp of multidevice/RealTimeOneToOneTest — partner ([partnerUid]) actions via REST.
 *
 * Covers:
 * - 1TO1-027: Receive multiple messages in order
 * - 1TO1-029: Receive message while on a different tab
 * - 1TO1-030: Conversation preview updates on new message
 * - 1TO1-054: Typing indicator shows  (NOT automatable single-device — skipped)
 * - 1TO1-055: Typing indicator disappears  (NOT automatable single-device — skipped)
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RealTimeOneToOneTest : RealtimeTestBase() {

    // ─── 1TO1-027: Receive multiple messages in order ────────────────────────────

    @Test
    fun test01_receiveMultipleMessagesInOrder() {
        openPartnerChat()
        val run = System.currentTimeMillis()

        RestApiHelper.sendMessage(partnerUid, appUid, "Order1_$run")
        Thread.sleep(1500)
        RestApiHelper.sendMessage(partnerUid, appUid, "Order2_$run")
        Thread.sleep(1500)
        RestApiHelper.sendMessage(partnerUid, appUid, "Order3_$run")

        // Order is guaranteed by sequential REST sends; verify all three arrived.
        assertTrue("Order1 not received in real time", pollForMessageInChat("Order1_$run", 45_000))
        assertTrue("Order2 not received in real time", pollForMessageInChat("Order2_$run", 30_000))
        assertTrue("Order3 not received in real time", pollForMessageInChat("Order3_$run", 30_000))
    }

    // ─── 1TO1-029: Receive message while on a different tab ───────────────────────

    @Test
    fun test02_receiveMessageWhileInDifferentTab() {
        // Sit on a different tab, receive a 1:1 message, then verify it appears in Chats.
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(2000)

        val tag = "DiffTab_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, appUid, tag)
        Thread.sleep(3000)

        E2ETestHelper.navigateToTab(device, "Chats")
        assertTrue(
            "Message received while on another tab did not appear in Chats",
            verifyConversationPreview(tag)
        )
    }

    // ─── 1TO1-030: Conversation preview updates on new message ────────────────────

    @Test
    fun test03_conversationUpdatesPreviewOnNewMessage() {
        E2ETestHelper.navigateToTab(device, "Chats")
        Thread.sleep(2000)

        val tag = "Preview_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, appUid, tag)

        assertTrue(
            "Conversation preview did not update with the new message",
            verifyConversationPreview(tag)
        )
    }

    // ─── 1TO1-054 / 1TO1-055: Typing (driven by the in-test JS SDK WebView) ───────

    @After
    fun stopDriver() {
        CometChatJsDriver.stop()
    }

    @Test
    fun test04_typingIndicatorShowsWhenPeerTypes() {
        openPartnerChat()

        val driving = CometChatJsDriver.start(
            uid = partnerUid, action = "typing", receiver = appUid, receiverType = "user"
        )
        assertTrue("JS driver could not log in as $partnerUid (status=${CometChatJsDriver.status()})", driving)

        assertTrue("Typing indicator did not appear", pollForText("yping", 30_000))
    }

    @Test
    fun test05_typingIndicatorDisappearsAfterStop() {
        openPartnerChat()

        val driving = CometChatJsDriver.start(
            uid = partnerUid, action = "typing", receiver = appUid, receiverType = "user"
        )
        assertTrue("JS driver could not log in as $partnerUid (status=${CometChatJsDriver.status()})", driving)
        assertTrue("Typing indicator did not appear", pollForText("yping", 30_000))

        // Partner stops typing (sends endTyping) → the indicator should clear.
        CometChatJsDriver.stopTyping()
        assertTrue(
            "Typing indicator did not disappear after the partner stopped typing",
            pollForTextGone("yping", 30_000)
        )
    }
}
