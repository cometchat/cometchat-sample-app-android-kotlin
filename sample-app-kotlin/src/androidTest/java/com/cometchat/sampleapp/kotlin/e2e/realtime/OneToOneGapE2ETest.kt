package com.cometchat.sampleapp.kotlin.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import com.cometchat.sampleapp.kotlin.e2e.helpers.RestApiHelper
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * One-to-one UI gap E2E tests (sample-app-kotlin).
 *
 * Covers sheet gap:
 * - 1TO1-022  Markdown bold renders (asterisks stripped)
 *
 * Other one-to-one gaps are tracked as manual / mapped elsewhere:
 * - 1TO1-095 video-call  → CallButtonsE2ETest.test02b_videoCallShowsOutgoing
 * - 1TO1-100 app-resume  → RealTimeReceiveEdgeE2ETest.test05_appResumeAfterBackground (RT-EDGE-008)
 * - 1TO1-073 unread divider → MANUAL: rendered as a canvas ItemDecoration (newMessageIndicatorDecoration),
 *                              so it's not an accessibility node (invisible to UIAutomator).
 * - 1TO1-082 Mark Unread → MANUAL: the conversation long-press menu only exposes DELETE in this app;
 *                          there is no Mark-Unread action.
 * - 1TO1-005 mention-tap → MANUAL: mention-to-open is not a defined 1:1 behavior here.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class OneToOneGapE2ETest : RealtimeTestBase() {

    // ─── 1TO1-022: Markdown bold renders (markers stripped) ───────────────────────
    @Test
    fun test01_markdownBoldRenders() {
        openPartnerChat()
        val token = "MDB${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, appUid, "**$token**")
        assertTrue("Bold message text should appear", pollForMessageInChat(token, 45_000))
        // Markdown processed → the literal asterisks must be gone (rendered bold, not "**token**").
        assertTrue(
            "Markdown bold markers should be stripped (rendered bold, no asterisks)",
            device.findObjects(By.textContains("**$token")).isEmpty()
        )
    }
}
