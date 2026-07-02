package com.cometchat.sampleapp.compose.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import com.cometchat.sampleapp.compose.e2e.helpers.CometChatJsDriver
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.RestApiHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time receive / edge-case / typing E2E tests (sample-app-compose).
 *
 * Mirror of the kotlin RealTimeReceiveEdgeE2ETest. Covers sheet gaps:
 * - RT-MSG-015, RT-RCPT-005, RT-EDGE-004/007/008/010, RT-TYPE-003/004
 *
 * NOTE: RT-PRES-003/004 and RT-MSG-013/014 are tracked as manual (presence is a colored dot,
 * unread is a count badge not tied to a row — neither is reliably readable by UIAutomator).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RealTimeReceiveEdgeE2ETest : RealtimeTestBase() {

    @After
    fun stopDriver() {
        CometChatJsDriver.stop()
    }

    // ─── RT-MSG-015: New conversation appears ─────────────────────────────────────
    @Test
    fun test01_newConversationAppears() {
        E2ETestHelper.navigateToTab(device, "Chats")
        Thread.sleep(2000)
        val tag = "NewConv_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(secondPartnerUid, appUid, tag)
        assertTrue(
            "A conversation with the new message should appear in Chats",
            verifyConversationPreview(tag)
        )
    }

    // ─── RT-RCPT-005: No read receipt if chat not opened ──────────────────────────
    @Test
    fun test02_noReadReceiptIfChatNotOpened() {
        val tag = "Unread_${System.currentTimeMillis()}"
        val id = RestApiHelper.sendMessage(appUid, partnerUid, tag)
        Thread.sleep(8000)
        assertTrue(
            "Message must NOT be marked read when the partner never opened the chat",
            !RestApiHelper.isRead(appUid, id)
        )
    }

    // ─── RT-EDGE-004: 50-message burst received without crashing ──────────────────
    @Test
    fun test03_fiftyMessageBurstReceived() {
        openPartnerChat()
        val marker = "Burst_${System.currentTimeMillis()}_"
        for (i in 0 until 50) {
            RestApiHelper.sendMessage(partnerUid, appUid, "$marker$i")
        }
        // A sentinel sent right AFTER the flood proves the app processed the whole burst and is
        // still receiving — a robust "received without crashing" check vs. matching the 50th bubble.
        val sentinel = "BurstDone_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, appUid, sentinel)
        assertTrue(
            "A sentinel sent after the 50-message burst should arrive within 120s",
            pollForMessageInChat(sentinel, 120_000)
        )
        assertTrue(
            "App should remain functional after a 50-message burst",
            device.findObject(By.clazz("android.widget.EditText")) != null
        )
    }

    // ─── RT-EDGE-007: Message arrives while scrolled up ───────────────────────────
    @Test
    fun test04_messageArrivesWhileScrolledUp() {
        openPartnerChat()
        repeat(3) { RestApiHelper.sendMessage(partnerUid, appUid, "hist_${System.currentTimeMillis()}_$it") }
        Thread.sleep(3000)
        repeat(3) { E2ETestHelper.scrollUp(device) }

        val tag = "Scrolled_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, appUid, tag)
        assertTrue(
            "A message arriving while scrolled up should still be reachable",
            pollForMessageInChat(tag, 45_000)
        )
    }

    // ─── RT-EDGE-008: App resume after background ──────────────────────────────────
    @Test
    fun test05_appResumeAfterBackground() {
        openPartnerChat()
        device.pressHome()
        Thread.sleep(2000)

        val tag = "Resume_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, appUid, tag)
        Thread.sleep(2000)

        E2ETestHelper.launchApp(device)
        Thread.sleep(E2ETestHelper.SETTLE_TIME)
        openPartnerChat()
        assertTrue(
            "Message received while backgrounded should be present after resume",
            pollForMessageInChat(tag, 45_000)
        )
    }

    // ─── RT-EDGE-010: Same message is not duplicated ──────────────────────────────
    @Test
    fun test06_sameMessageDoesNotDuplicate() {
        openPartnerChat()
        val tag = "NoDup_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, appUid, tag)
        assertTrue("Message should arrive", pollForMessageInChat(tag, 45_000))
        Thread.sleep(4000)
        assertEquals(
            "The same message must appear exactly once (no duplicate bubble)",
            1, device.findObjects(By.textContains(tag)).size
        )
    }

    // ─── RT-TYPE-003: Typing then send — indicator clears ─────────────────────────
    @Test
    fun test07_typingThenSendClearsIndicator() {
        openPartnerChat()
        CometChatJsDriver.start(uid = partnerUid, action = "typing", receiver = appUid, receiverType = "user")
        assertTrue("Typing indicator should appear first", pollForText("yping", 30_000))

        CometChatJsDriver.stopTyping()
        val tag = "TypeSend_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, appUid, tag)
        assertTrue("The sent message should appear", pollForMessageInChat(tag, 30_000))
        assertTrue("Typing indicator should clear after the message is sent",
            pollForTextGone("yping", 20_000))
    }

    // ─── RT-TYPE-004: Typing indicator shown in the conversation list ─────────────
    @Test
    fun test08_typingIndicatorOnConversationList() {
        openPartnerChat()
        device.pressBack(); Thread.sleep(1500)
        E2ETestHelper.navigateToTab(device, "Chats")
        Thread.sleep(1500)

        CometChatJsDriver.start(uid = partnerUid, action = "typing", receiver = appUid, receiverType = "user")
        assertTrue(
            "The conversation list should show a typing indicator for the partner",
            pollForText("yping", 30_000)
        )
    }
}
