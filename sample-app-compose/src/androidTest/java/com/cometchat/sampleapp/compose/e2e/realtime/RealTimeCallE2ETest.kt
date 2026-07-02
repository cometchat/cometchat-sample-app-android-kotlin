package com.cometchat.sampleapp.compose.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import com.cometchat.sampleapp.compose.e2e.helpers.CometChatJsDriver
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time CALL E2E tests (sample-app-compose).
 *
 * Mirror of the verified kotlin RealTimeCallE2ETest. The partner ([partnerUid]) initiates a
 * default call via the JS Chat SDK (WebView driver) so the app receives onIncomingCallReceived
 * and shows the CometChatIncomingCall component — verified flow modeled on master-app-jetpack
 * (Application.addCallListener → launchIncomingCallPopup → CometChatIncomingCall overlay).
 *
 * IMPORTANT: sample-app-compose does NOT yet wire incoming calls (no addCallListener /
 * CometChatIncomingCall) — that's a known app gap to be added by the senior dev (master-app-jetpack
 * already has it). Until then, the incoming-UI tests (test01/02/03/04) will fail; the call-message
 * tests (test05/06) may pass since the call message is created server-side regardless of the UI.
 *
 * Covers: RT-CALL-001/005 (incoming voice/video), RT-CALL-002 (decline), RT-CALL-003 (cancel),
 * RT-CALL-004 (call-ended message), RT-CALL-006 (conversation-list update).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RealTimeCallE2ETest : RealtimeTestBase() {

    @Before
    fun grantCallPermissions() {
        device.executeShellCommand("pm grant ${E2ETestHelper.PACKAGE} android.permission.RECORD_AUDIO")
        device.executeShellCommand("pm grant ${E2ETestHelper.PACKAGE} android.permission.CAMERA")
    }

    @After
    fun stopDriver() {
        runCatching { CometChatJsDriver.cancelCall() }
        Thread.sleep(5000)
        CometChatJsDriver.stop()
        Thread.sleep(3000)
    }

    @Test
    fun test01_incomingVoiceCallShowsComponent() {
        startIncomingCall(video = false)
        assertTrue(
            "Incoming voice call component (Accept/Decline) should appear",
            poll(45_000) { incomingCallShown() }
        )
    }

    @Test
    fun test02_incomingVideoCallShowsComponent() {
        startIncomingCall(video = true)
        assertTrue(
            "Incoming video call component (Accept/Decline) should appear",
            poll(45_000) { incomingCallShown() }
        )
    }

    @Test
    fun test03_declineReturnsToChat() {
        startIncomingCall(video = false)
        assertTrue("Incoming call should appear before declining", poll(45_000) { incomingCallShown() })
        clickDecline()
        assertTrue("Incoming call component should dismiss after Decline", poll(20_000) { !incomingCallShown() })
    }

    @Test
    fun test04_cancelDismissesIncoming() {
        startIncomingCall(video = false)
        assertTrue("Incoming call should appear before cancel", poll(45_000) { incomingCallShown() })
        CometChatJsDriver.cancelCall()
        assertTrue("Incoming call component should dismiss after the caller cancels", poll(25_000) { !incomingCallShown() })
    }

    @Test
    fun test05_callEndedMessageInChat() {
        startIncomingCall(video = false)
        poll(45_000) { incomingCallShown() }
        clickDecline()
        Thread.sleep(3000)
        openPartnerChat()
        assertTrue(
            "A call message should appear in the chat after the call",
            pollForAnyMessageInChat(listOf("Missed", "voice call", "Voice call", "Call ended", "call"), 30_000)
        )
    }

    @Test
    fun test06_callUpdatesConversationList() {
        startIncomingCall(video = false)
        poll(45_000) { incomingCallShown() }
        clickDecline()
        Thread.sleep(3000)
        E2ETestHelper.navigateToTab(device, "Chats")
        assertTrue(
            "The conversation list should reflect the call (call message preview)",
            poll(30_000) {
                E2ETestHelper.scrollUp(device)
                device.findObjects(By.textContains("call")).isNotEmpty() ||
                    device.findObjects(By.textContains("Call")).isNotEmpty() ||
                    device.findObjects(By.textContains("Missed")).isNotEmpty()
            }
        )
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private fun startIncomingCall(video: Boolean): Boolean {
        val action = if (video) "call_video" else "call_audio"
        CometChatJsDriver.start(uid = partnerUid, action = action, receiver = appUid, receiverType = "user")
        return poll(25_000) { CometChatJsDriver.status().startsWith("call-initiated") }
    }

    // Buttons render UPPERCASE; By.textContains is case-sensitive, so match case-insensitively.
    private val acceptOrDecline = java.util.regex.Pattern.compile("(?i).*(accept|decline).*")
    private val declinePattern = java.util.regex.Pattern.compile("(?i).*decline.*")

    private fun incomingCallShown(): Boolean =
        device.findObjects(By.text(acceptOrDecline)).isNotEmpty() ||
            device.findObjects(By.desc(acceptOrDecline)).isNotEmpty()

    private fun clickDecline() {
        (device.findObject(By.text(declinePattern)) ?: device.findObject(By.desc(declinePattern)))?.click()
    }
}
