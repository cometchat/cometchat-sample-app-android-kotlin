package com.cometchat.sampleapp.kotlin.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import com.cometchat.sampleapp.kotlin.e2e.helpers.CometChatJsDriver
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.RestApiHelper
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time block + group-admin E2E tests (sample-app-kotlin).
 *
 * Partner ([partnerUid]) / second partner ([secondPartnerUid]) actions are driven over REST,
 * so the server pushes the resulting events to the app and the UI updates in real time.
 *
 * Covers sheet gaps:
 * - RT-TYPE-005              Typing is suppressed when blocked
 * - RT-GRP-004               Member banned — removed from the group (member count drops)
 * - RT-GRP-005               Member scope changed — action message appears
 * - RT-GRP-007               Kicked user's group conversation is removed from Chats
 *
 * NOTE: RT-BLOCK-001/002 ("partner blocks/unblocks the app user → app shows/recovers a blocked
 * state") are intentionally NOT here: this app surfaces no blocked-BY-OTHER UI — the UIKit's
 * isBlocked() only hides the header presence, the composer has no block handling, and there is
 * no "Unblock" banner unless YOU blocked them. The real-time block effect that IS observable is
 * verified by [test03_typingSuppressedWhenBlocked]. RT-BLOCK-001/002 are tracked as manual.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RealTimeBlockGroupE2ETest : RealtimeTestBase() {

    private val partnerName: String by lazy { E2ETestHelper.getUserName(partnerUid) }

    @After
    fun stopDriver() {
        // Best-effort: leave the partner not-blocking so reruns start clean.
        runCatching { RestApiHelper.unblockUser(partnerUid, appUid) }
        CometChatJsDriver.stop()
    }

    // ─── RT-TYPE-005: Typing suppressed when blocked ──────────────────────────────
    @Test
    fun test03_typingSuppressedWhenBlocked() {
        RestApiHelper.blockUser(partnerUid, appUid) // d1 blocks dhruv
        openUserChatViaSearch(partnerName)

        // Drive the partner to type via the JS SDK; because the partner blocked the app user,
        // the typing indicator must NOT reach the app.
        CometChatJsDriver.start(uid = partnerUid, action = "typing", receiver = appUid, receiverType = "user")
        Thread.sleep(8000) // give any (incorrect) typing event time to arrive

        assertTrue(
            "Typing indicator must NOT appear when the partner has blocked the app user",
            device.findObjects(By.textContains("yping")).isEmpty()
        )
    }

    // ─── RT-GRP-004: Member banned — removed from the group ───────────────────────
    @Test
    fun test04_memberBannedRemovedFromGroup() {
        val guid = "e2e_rt_ban_v3"
        ensureGroup(guid, "E2E RT Ban")
        RestApiHelper.ensureGroupMember(onBehalfOf = appUid, guid = guid, uid = secondPartnerUid)
        openGroupChat(guid)
        val before = groupMemberCount()

        RestApiHelper.banGroupMember(onBehalfOf = appUid, guid = guid, uid = secondPartnerUid)

        assertTrue(
            "Member count should decrease after the member is banned",
            verifyMemberCountChange(guid, before, wantIncrease = false)
        )
    }

    // ─── RT-GRP-005: Member scope changed — action message appears ────────────────
    @Test
    fun test05_memberScopeChangedActionMessage() {
        val guid = "e2e_rt_scope_v3"
        ensureGroup(guid, "E2E RT Scope")
        RestApiHelper.ensureGroupMember(onBehalfOf = appUid, guid = guid, uid = secondPartnerUid)
        // Reset to participant first so the change to admin always produces an event.
        runCatching { RestApiHelper.changeMemberScope(appUid, guid, secondPartnerUid, "participant") }
        openGroupChat(guid)

        RestApiHelper.changeMemberScope(appUid, guid, secondPartnerUid, "admin")

        assertTrue(
            "A scope-change action message should appear in the group in real time",
            pollForAnyMessageInChat(listOf("dmin", "made", "scope", "Scope", "now an"), 30_000)
        )
    }

    // ─── RT-GRP-007: Kicked user's group conversation is removed from Chats ────────
    @Test
    fun test06_kickedUserConversationRemoved() {
        val guid = "e2e_rt_kick_v3"
        // Group owned by the partner so the partner can kick the app user.
        RestApiHelper.createGroupIfAbsent(owner = partnerUid, guid = guid, name = "E2E RT Kick")
        RestApiHelper.ensureGroupMember(onBehalfOf = partnerUid, guid = guid, uid = appUid)
        val seed = "kickseed_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, guid, seed, receiverType = "group")
        Thread.sleep(2500)

        E2ETestHelper.navigateToTab(device, "Chats")
        assertTrue("Group conversation should first appear in Chats", verifyConversationPreview(seed))

        // Partner kicks the app user; the group conversation should disappear from Chats.
        RestApiHelper.removeGroupMember(onBehalfOf = partnerUid, guid = guid, uid = appUid)
        assertTrue(
            "Kicked user's group conversation should be removed from Chats in real time",
            poll(30_000) {
                E2ETestHelper.navigateToTab(device, "Chats")
                device.findObjects(By.textContains(seed)).isEmpty()
            }
        )
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    /** Opens the partner's 1:1 chat by searching for [name] in the Users tab. */
    private fun openUserChatViaSearch(name: String) {
        E2ETestHelper.navigateToTab(device, "Users")
        Thread.sleep(E2ETestHelper.SETTLE_TIME)
        val sb = device.findObject(By.clazz("android.widget.EditText"))
        sb?.clear(); sb?.text = name
        Thread.sleep(4000)
        var bottom = 200
        device.findObject(By.clazz("android.widget.EditText"))?.let {
            try { bottom = it.visibleBounds.bottom + 20 } catch (_: Exception) {}
        }
        for (m in device.findObjects(By.textContains(name.take(8)))) {
            try { if (m.visibleBounds.top > bottom) { m.click(); break } } catch (_: Exception) { continue }
        }
        Thread.sleep(E2ETestHelper.SETTLE_TIME)
    }
}
