package com.cometchat.sampleapp.compose.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.RestApiHelper
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time group-receive E2E tests (sample-app-compose).
 *
 * Revamp of multidevice/GroupReceiveMessageE2ETest — a group member ([partnerUid]) sends to
 * the group via REST while the app (logged in as [appUid]) observes in real time.
 *
 * Covers:
 * - GRP-034: Receive text message from a member in real-time
 * - GRP-037: Message arrives while on a different tab
 * - GRP-038: Group conversation preview updates on new message
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupReceiveMessageE2ETest : RealtimeTestBase() {

    // App-owned GUID: the group is created with appUid as owner so appUid can admin it.
    private val groupGuid = E2ETestConfig.GROUP_RECEIVE_GUID
    private val groupName = "E2E RT GroupRecv"

    @Before
    fun groupSetup() {
        // Runs after the base @Before (login). Ensure the group exists, the partner is a
        // member, and a seed message exists so the group is already a conversation in Chats.
        ensureGroup(groupGuid, groupName)
        RestApiHelper.ensureGroupMember(onBehalfOf = appUid, guid = groupGuid, uid = partnerUid)
        RestApiHelper.sendMessage(appUid, groupGuid, "seed_${System.currentTimeMillis()}", receiverType = "group")
        Thread.sleep(1500)
    }

    // ─── GRP-034: Receive text message from a member in real-time ─────────────────

    @Test
    fun test01_receiveTextMessageFromMemberRealTime() {
        openGroupChat(groupGuid)

        val tag = "GrpRecv_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, groupGuid, tag, receiverType = "group")

        assertTrue(
            "Group member's message did not appear in real time",
            pollForMessageInChat(tag, 45_000)
        )
    }

    // ─── GRP-037: Message arrives while on a different tab ────────────────────────

    @Test
    fun test02_messageArrivesWhileOnDifferentTab() {
        E2ETestHelper.navigateToTab(device, "Users")
        Thread.sleep(2000)

        val tag = "DiffTab_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, groupGuid, tag, receiverType = "group")
        Thread.sleep(3000)

        E2ETestHelper.navigateToTab(device, "Chats")
        assertTrue(
            "Group message received while on another tab did not appear in Chats",
            verifyConversationPreview(tag)
        )
    }

    // ─── GRP-038: Group conversation preview updates on new message ───────────────

    @Test
    fun test03_groupConversationPreviewUpdatesOnNewMessage() {
        E2ETestHelper.navigateToTab(device, "Chats")
        Thread.sleep(2000)

        val tag = "Preview_${System.currentTimeMillis()}"
        RestApiHelper.sendMessage(partnerUid, groupGuid, tag, receiverType = "group")

        assertTrue(
            "Group conversation preview did not update with the new message",
            verifyConversationPreview(tag)
        )
    }
}
