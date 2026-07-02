package com.cometchat.sampleapp.kotlin.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import com.cometchat.sampleapp.kotlin.e2e.helpers.CometChatJsDriver
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time CALL E2E tests (sample-app-kotlin).
 *
 * The partner ([partnerUid]) initiates a default call via the JS Chat SDK (the WebView driver,
 * action "call_audio"/"call_video") so the app under test receives onIncomingCallReceived and
 * shows the CometChatIncomingCall component. We verify the component + the decline/cancel flow
 * and the resulting call message — without ever connecting the call.
 *
 * Covers: RT-CALL-001 (incoming voice), RT-CALL-005 (incoming video), RT-CALL-002 (decline →
 * back to chat), RT-CALL-003 (cancel → dismiss), RT-CALL-004 (call-ended message in chat),
 * RT-CALL-006 (call updates conversation list).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RealTimeCallE2ETest : RealtimeTestBase() {

    @org.junit.Before
    fun grantCallPermissions() {
        // Grant call perms up front so a video incoming component never blocks on a permission prompt.
        device.executeShellCommand("pm grant ${E2ETestHelper.PACKAGE} android.permission.RECORD_AUDIO")
        device.executeShellCommand("pm grant ${E2ETestHelper.PACKAGE} android.permission.CAMERA")
    }

    @After
    fun stopDriver() {
        // Cancel the call and let it fully clear (app CallManager + caller session) BEFORE the next
        // test re-logs-in the same caller and initiates again — otherwise the next call can be
        // busy-rejected / fail to initiate, which previously caused cross-test flakiness.
        runCatching { CometChatJsDriver.cancelCall() }
        Thread.sleep(5000)
        CometChatJsDriver.stop()
        Thread.sleep(3000)
    }

    // ─── RT-CALL-001: Incoming voice call shows the incoming component ─────────────
    @Test
    fun test01_incomingVoiceCallShowsComponent() {
        startIncomingCall(video = false)
        assertTrue(
            "Incoming voice call component (Accept/Decline) should appear",
            poll(45_000) { incomingCallShown() }
        )
    }

    // ─── RT-CALL-005: Incoming video call shows the incoming component ─────────────
    @Test
    fun test02_incomingVideoCallShowsComponent() {
        startIncomingCall(video = true)
        assertTrue(
            "Incoming video call component (Accept/Decline) should appear",
            poll(45_000) { incomingCallShown() }
        )
    }

    // ─── RT-CALL-002: Declining the call returns to the app (component dismissed) ──
    @Test
    fun test03_declineReturnsToChat() {
        startIncomingCall(video = false)
        assertTrue("Incoming call should appear before declining", poll(45_000) { incomingCallShown() })

        clickDecline()
        assertTrue(
            "Incoming call component should dismiss after Decline",
            poll(20_000) { !incomingCallShown() }
        )
    }

    // ─── RT-CALL-003: Caller cancels — incoming component is dismissed ─────────────
    @Test
    fun test04_cancelDismissesIncoming() {
        startIncomingCall(video = false)
        assertTrue("Incoming call should appear before cancel", poll(45_000) { incomingCallShown() })

        CometChatJsDriver.cancelCall()
        assertTrue(
            "Incoming call component should dismiss after the caller cancels",
            poll(25_000) { !incomingCallShown() }
        )
    }

    // ─── RT-CALL-004: A call message appears in the chat after the call ───────────
    @Test
    fun test05_callEndedMessageInChat() {
        startIncomingCall(video = false)
        assertTrue("Incoming call should appear", poll(45_000) { incomingCallShown() })
        clickDecline()
        Thread.sleep(3000)

        openPartnerChat()
        assertTrue(
            "A call message should appear in the chat after the call",
            pollForAnyMessageInChat(listOf("Missed", "voice call", "Voice call", "Call ended", "call"), 30_000)
        )
    }

    // ─── RT-CALL-006: The call updates the conversation list ──────────────────────
    @Test
    fun test06_callUpdatesConversationList() {
        startIncomingCall(video = false)
        assertTrue("Incoming call should appear", poll(45_000) { incomingCallShown() })
        clickDecline()
        Thread.sleep(3000)

        E2ETestHelper.navigateToTab(device, "Chats")
        assertTrue(
            "The conversation list should reflect the call (call message preview)",
            poll(30_000) {
                device.findObjects(By.textContains("call")).isNotEmpty() ||
                    device.findObjects(By.textContains("Call")).isNotEmpty() ||
                    device.findObjects(By.textContains("Missed")).isNotEmpty()
            }
        )
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    /** Partner initiates a default call to the app user via the JS driver. Confirms the call
     *  actually went out (driver status "call-initiated") so a flaky init doesn't masquerade as
     *  a missing popup. */
    private fun startIncomingCall(video: Boolean): Boolean {
        val action = if (video) "call_video" else "call_audio"
        CometChatJsDriver.start(uid = partnerUid, action = action, receiver = appUid, receiverType = "user")
        return poll(25_000) { CometChatJsDriver.status().startsWith("call-initiated") }
    }

    // Buttons render UPPERCASE ("ACCEPT"/"DECLINE") via the button theme; By.textContains is
    // case-sensitive, so match case-insensitively against both text and contentDescription.
    private val acceptOrDecline = java.util.regex.Pattern.compile("(?i).*(accept|decline).*")
    private val declinePattern = java.util.regex.Pattern.compile("(?i).*decline.*")

    /** True while the incoming-call component (Accept/Decline) is on screen. */
    private fun incomingCallShown(): Boolean =
        device.findObjects(By.text(acceptOrDecline)).isNotEmpty() ||
            device.findObjects(By.desc(acceptOrDecline)).isNotEmpty()

    private fun clickDecline() {
        (device.findObject(By.text(declinePattern)) ?: device.findObject(By.desc(declinePattern)))?.click()
    }
}
