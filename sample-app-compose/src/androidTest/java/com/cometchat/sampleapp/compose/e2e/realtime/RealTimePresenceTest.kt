package com.cometchat.sampleapp.compose.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.sampleapp.compose.e2e.helpers.CometChatJsDriver
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time presence E2E tests (sample-app-compose).
 *
 * The partner ([partnerUid]) is brought ONLINE by [CometChatJsDriver], which runs the CometChat
 * JavaScript SDK in an in-test WebView (an independent second session on the same emulator). The
 * app, logged in as the primary user with presence subscription, should then show the partner as
 * "Online".
 *
 * Covers:
 * - E2E-013: Presence updates online (partner comes online)
 * - E2E-028: Online status updates in message header
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RealTimePresenceTest : RealtimeTestBase() {

    @After
    fun stopDriver() {
        CometChatJsDriver.stop()
    }

    // ─── E2E-013: Partner comes online ───────────────────────────────────────────

    @Test
    fun test01_presenceUpdatesOnline() {
        // Open the partner's chat FIRST (subscribe to presence), THEN bring the partner online —
        // the app receives the onUserOnline event live → header flips to "Online".
        openPartnerChat()

        val loggedIn = CometChatJsDriver.start(uid = partnerUid, action = "presence")
        assertTrue(
            "JS driver could not log in as $partnerUid (status=${CometChatJsDriver.status()})",
            loggedIn
        )

        assertTrue(
            "Partner did not show as Online (presence event not received in header)",
            pollForText("Online", 30_000)
        )
    }

    // ─── E2E-028: Online status in message header ────────────────────────────────

    @Test
    fun test02_onlineStatusUpdatesInHeader() {
        openPartnerChat()

        val loggedIn = CometChatJsDriver.start(uid = partnerUid, action = "presence")
        assertTrue(
            "JS driver could not log in as $partnerUid (status=${CometChatJsDriver.status()})",
            loggedIn
        )

        assertTrue(
            "Online status not visible in the message header after partner came online",
            pollForText("Online", 30_000)
        )
    }
}
