package com.cometchat.sampleapp.compose.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import com.cometchat.sampleapp.compose.e2e.helpers.RestApiHelper
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * One-to-one UI gap E2E tests (sample-app-compose). Mirror of the kotlin OneToOneGapE2ETest.
 *
 * Covers: 1TO1-022 (markdown bold). Others manual / mapped elsewhere — see the kotlin twin:
 * 1TO1-095 → CallButtonsE2ETest.test04b; 1TO1-100 → RealTimeReceiveEdgeE2ETest.test05;
 * 1TO1-073 (canvas ItemDecoration), 1TO1-082 (no Mark-Unread action), 1TO1-005 → MANUAL.
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
        assertTrue(
            "Markdown bold markers should be stripped (rendered bold, no asterisks)",
            device.findObjects(By.textContains("**$token")).isEmpty()
        )
    }
}
