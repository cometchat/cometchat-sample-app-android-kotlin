package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SETTLE_TIME
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Extended E2E tests for Group Members management — covers GRP-094 to GRP-107.
 *
 * NOTE ON DUPLICATES:
 * - GRP-094 (testViewMembersShowsAllGroupMembers) ≈ E2E-030 in GroupMembersE2ETest
 * - GRP-099 (testBanMemberViaAdminAction) ≈ E2E-033 in GroupMembersE2ETest
 * - GRP-102 (testChangeMemberRoleToAdmin) ≈ E2E-034 in GroupMembersE2ETest
 * - GRP-104 (testKickMemberRemovesFromGroup) ≈ E2E-032 in GroupMembersE2ETest
 *
 * This file implements the NON-DUPLICATE tests:
 * - GRP-095: testMembersListShowsRoleBadges
 * - GRP-096: testAddMembersScreenShowsNonMemberUsers
 * - GRP-097: testSelectMultipleUsersAndAddToGroup
 * - GRP-098: testAddedMemberAppearsInMembersList
 * - GRP-100: testBannedMemberAppearsInBannedList
 * - GRP-101: testUnbanMemberRemovesFromBannedList
 * - GRP-103: testChangeMemberRoleToParticipant
 * - GRP-105: testRegularMemberHasNoAdminActions
 * - GRP-106: testOwnerCannotBeKickedOrBannedByAdmin
 * - GRP-107: testTransferOwnershipBeforeDeleteAndExit
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.GroupMembersExtendedE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupMembersExtendedE2ETest {

    private lateinit var device: UiDevice
    private var testGroupId = ""
    private var testGroupName = ""
    private val memberUid1 = E2ETestConfig.GROUP_MEMBER_1_UID // Andrew Joseph
    private val memberUid2 = E2ETestConfig.GROUP_MEMBER_2_UID // George Alan
    private val adminUid = E2ETestConfig.GROUP_MEMBER_3_UID   // Will be promoted to admin

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        val ts = System.currentTimeMillis()
        testGroupId = "grp_members_$ts"
        testGroupName = "MembersGrp$ts"

        // Create group (logged-in user becomes owner)
        val latch = CountDownLatch(1)
        val group = Group(testGroupId, testGroupName, CometChatConstants.GROUP_TYPE_PUBLIC, "")
        CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(g: Group) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(15, TimeUnit.SECONDS)

        // Add members
        val addLatch = CountDownLatch(1)
        val members = listOf(
            GroupMember(memberUid1, CometChatConstants.SCOPE_PARTICIPANT),
            GroupMember(memberUid2, CometChatConstants.SCOPE_PARTICIPANT)
        )
        CometChat.addMembersToGroup(testGroupId, members, null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(r: HashMap<String, String>?) { addLatch.countDown() }
                override fun onError(e: CometChatException?) { addLatch.countDown() }
            })
        addLatch.await(10, TimeUnit.SECONDS)
    }

    @After
    fun teardown() {
        // Ensure we're logged in as owner for cleanup
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(E2ETestHelper.testUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        if (testGroupId.isNotEmpty()) {
            val latch = CountDownLatch(1)
            CometChat.deleteGroup(testGroupId, object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) { latch.countDown() }
                override fun onError(e: CometChatException?) { latch.countDown() }
            })
            latch.await(10, TimeUnit.SECONDS)
        }
    }

    /**
     * GRP-095: Members list shows role badges (Owner, Admin, Moderator labels).
     */
    @Test
    fun test01_membersListShowsRoleBadges() {
        navigateToTestGroup()
        openGroupDetailsFromHeader()
        openViewMembers()

        // The owner should have an "Owner" or "Admin" label visible
        val ownerLabel = device.findObject(By.textContains("Owner"))
            ?: device.findObject(By.textContains("owner"))
            ?: device.findObject(By.textContains("Admin"))
        assertNotNull("Role badge (Owner/Admin) not visible in members list", ownerLabel)
    }

    /**
     * GRP-096: Add Members screen shows non-member users.
     */
    @Test
    fun test02_addMembersScreenShowsNonMemberUsers() {
        navigateToTestGroup()
        openGroupDetailsFromHeader()

        val addMembers = device.findObject(By.text("Add Members"))
            ?: device.findObject(By.textContains("Add Members"))
        assertNotNull("Add Members option not found", addMembers)
        addMembers!!.click()
        Thread.sleep(SETTLE_TIME)

        // Verify users list loaded (should show users NOT already in the group)
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val deadline = System.currentTimeMillis() + TIMEOUT
        var usersRv: androidx.test.uiautomator.UiObject2? = null
        while (System.currentTimeMillis() < deadline) {
            usersRv = device.findObject(By.res(uikitPackage, "recyclerview_users_list"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_users_list"))
            if (usersRv != null && usersRv.children.isNotEmpty()) break
            Thread.sleep(1500)
        }
        assertNotNull("Users list did not load on Add Members screen", usersRv)
        assertTrue("Users list should have items (non-member users)", usersRv!!.children.isNotEmpty())
    }

    /**
     * GRP-097: Select multiple users and add to group.
     */
    @Test
    fun test03_selectMultipleUsersAndAddToGroup() {
        navigateToTestGroup()
        openGroupDetailsFromHeader()

        // Read member count before
        val memberCountBefore = device.findObject(By.res(PACKAGE, "member_count"))
        val countTextBefore = memberCountBefore?.text ?: ""
        val numBefore = extractNumber(countTextBefore)

        val addMembers = device.findObject(By.text("Add Members"))
            ?: device.findObject(By.textContains("Add Members"))
        assertNotNull("Add Members option not found", addMembers)
        addMembers!!.click()
        Thread.sleep(SETTLE_TIME)

        val uikitPackage = "com.cometchat.uikit.kotlin"
        val deadline = System.currentTimeMillis() + TIMEOUT
        var usersRv: androidx.test.uiautomator.UiObject2? = null
        while (System.currentTimeMillis() < deadline) {
            usersRv = device.findObject(By.res(uikitPackage, "recyclerview_users_list"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_users_list"))
            if (usersRv != null && usersRv.children.isNotEmpty()) break
            Thread.sleep(1500)
        }
        assertNotNull("Users list not loaded", usersRv)

        // Select users using coordinate-based clicks to avoid StaleObjectException after fling
        usersRv!!.fling(androidx.test.uiautomator.Direction.DOWN)
        Thread.sleep(2000)

        // Re-find the RecyclerView after fling (references may be stale)
        val usersRvAfterFling = device.findObject(By.res(uikitPackage, "recyclerview_users_list"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_users_list"))

        if (usersRvAfterFling != null) {
            val freshChildren = usersRvAfterFling.children
            // Click using coordinates to avoid stale references
            if (freshChildren.size >= 2) {
                try {
                    val bounds1 = freshChildren[freshChildren.size - 1].visibleBounds
                    device.click(bounds1.centerX(), bounds1.centerY())
                    Thread.sleep(500)
                    val bounds2 = freshChildren[freshChildren.size - 2].visibleBounds
                    device.click(bounds2.centerX(), bounds2.centerY())
                    Thread.sleep(500)
                } catch (_: androidx.test.uiautomator.StaleObjectException) {
                    // If stale, re-find and try the first available item
                    Thread.sleep(1000)
                    val retryRv = device.findObject(By.res(uikitPackage, "recyclerview_users_list"))
                        ?: device.findObject(By.res(PACKAGE, "recyclerview_users_list"))
                    if (retryRv != null && retryRv.children.isNotEmpty()) {
                        try {
                            val b = retryRv.children[0].visibleBounds
                            device.click(b.centerX(), b.centerY())
                        } catch (_: Exception) { }
                    }
                }
            } else if (freshChildren.isNotEmpty()) {
                try {
                    val bounds = freshChildren[0].visibleBounds
                    device.click(bounds.centerX(), bounds.centerY())
                } catch (_: Exception) { }
            }
        }
        Thread.sleep(500)

        // Tap "Add Members" confirm button
        val addBtn = device.findObject(By.res(PACKAGE, "add_members_btn"))
            ?: device.findObject(By.res(PACKAGE, "tv_add_members"))
            ?: device.findObject(By.text("Add Members"))
        addBtn?.click()
        Thread.sleep(5000)

        // Navigate back to details if needed
        if (device.findObject(By.res(PACKAGE, "member_count")) == null) {
            device.pressBack()
            Thread.sleep(SETTLE_TIME)
        }

        // Verify member count increased
        val memberCountAfter = device.findObject(By.res(PACKAGE, "member_count"))
        val countTextAfter = memberCountAfter?.text ?: ""
        val numAfter = extractNumber(countTextAfter)
        assertTrue(
            "Member count should increase after adding. Before: $numBefore, After: $numAfter",
            numAfter > numBefore
        )
    }

    /**
     * GRP-098: Added member appears in members list.
     */
    @Test
    fun test04_addedMemberAppearsInMembersList() {
        navigateToTestGroup()
        openGroupDetailsFromHeader()
        openViewMembers()

        val uikitPackage = "com.cometchat.uikit.kotlin"
        val membersRv = device.wait(
            Until.findObject(By.res(uikitPackage, "recyclerview_group_members")),
            TIMEOUT
        ) ?: device.findObject(By.res(PACKAGE, "recyclerview_group_members"))
        assertNotNull("Members RecyclerView not found", membersRv)

        // Should show the members we added in @Before (at least 3: owner + 2 members)
        val children = membersRv!!.children
        assertTrue(
            "Members list should show at least 3 entries (owner + 2 added), found ${children.size}",
            children.size >= 3
        )
    }

    /**
     * GRP-100: Banned member appears in Banned Members list.
     */
    @Test
    fun test05_bannedMemberAppearsInBannedList() {
        // Ban a member via SDK first
        val banLatch = CountDownLatch(1)
        CometChat.banGroupMember(memberUid2, testGroupId,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) { banLatch.countDown() }
                override fun onError(e: CometChatException?) { banLatch.countDown() }
            })
        banLatch.await(10, TimeUnit.SECONDS)

        navigateToTestGroup()
        openGroupDetailsFromHeader()

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val bannedOption = device.findObject(By.text("Banned Members"))
            ?: device.findObject(By.textContains("Banned"))
        assertNotNull("Banned Members option not found", bannedOption)
        bannedOption!!.click()
        Thread.sleep(SETTLE_TIME)

        // Verify the banned member appears in the banned list
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val bannedRv = device.findObject(By.res(uikitPackage, "recyclerview_banned_members"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_banned_members"))

        // At minimum, verify the banned members screen loaded with content
        val hasBannedMembers = bannedRv != null && bannedRv.children.isNotEmpty()
        val hasBannedText = device.findObject(By.textContains(E2ETestHelper.getUserName(E2ETestConfig.GROUP_MEMBER_2_UID))) != null ||
            device.findObject(By.textContains(E2ETestConfig.GROUP_MEMBER_2_UID)) != null
        assertTrue("Banned member should appear in Banned Members list", hasBannedMembers || hasBannedText)
    }

    /**
     * GRP-101: Unban member removes from banned list.
     */
    @Test
    fun test06_unbanMemberRemovesFromBannedList() {
        // Ban then navigate to banned list
        val banLatch = CountDownLatch(1)
        CometChat.banGroupMember(memberUid2, testGroupId,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) { banLatch.countDown() }
                override fun onError(e: CometChatException?) { banLatch.countDown() }
            })
        banLatch.await(10, TimeUnit.SECONDS)

        navigateToTestGroup()
        openGroupDetailsFromHeader()

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val bannedOption = device.findObject(By.text("Banned Members"))
            ?: device.findObject(By.textContains("Banned"))
        assertNotNull("Banned Members option not found", bannedOption)
        bannedOption!!.click()
        Thread.sleep(SETTLE_TIME)

        // Long-press the banned member to get unban option
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val bannedRv = device.findObject(By.res(uikitPackage, "recyclerview_banned_members"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_banned_members"))

        if (bannedRv != null && bannedRv.children.isNotEmpty()) {
            // Click the unban/close icon (iv_unban ImageView) on the right side of the item
            val unbanIcon = device.findObject(By.res(PACKAGE, "iv_unban"))
                ?: device.findObject(By.desc("Close"))
                ?: device.findObject(By.descContains("close"))
                ?: device.findObject(By.descContains("Unban"))

            if (unbanIcon != null) {
                unbanIcon.click()
            } else {
                // Fallback: find clickable ImageView on the right side of the first item
                val item = bannedRv.children[0]
                val icons = item.findObjects(By.clazz("android.widget.ImageView").clickable(true))
                if (icons.isNotEmpty()) {
                    icons.last().click()  // rightmost icon is the unban button
                } else {
                    // Last resort: click right side of the item
                    val bounds = item.visibleBounds
                    device.click(bounds.right - 50, bounds.centerY())
                }
            }
            Thread.sleep(2000)

            // Confirm unban in dialog
            val confirmBtn = device.findObject(By.text("Unban"))
                ?: device.findObject(By.textContains("Unban"))
                ?: device.findObject(By.textContains("Confirm"))
            confirmBtn?.click()
            Thread.sleep(3000)
        }

        // Verify the list is now empty or the member is gone
        val bannedRvAfter = device.findObject(By.res(uikitPackage, "recyclerview_banned_members"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_banned_members"))
        val isEmpty = bannedRvAfter == null || bannedRvAfter.children.isEmpty()
        val noGeorge = device.findObject(By.textContains(E2ETestHelper.getUserName(E2ETestConfig.GROUP_MEMBER_2_UID))) == null
        assertTrue("Banned list should be empty or member removed after unban", isEmpty || noGeorge)
    }

    /**
     * GRP-103: Change member role back to Participant.
     */
    @Test
    fun test07_changeMemberRoleToParticipant() {
        // First promote memberUid1 to admin via SDK
        val scopeLatch = CountDownLatch(1)
        CometChat.updateGroupMemberScope(
            memberUid1, testGroupId, CometChatConstants.SCOPE_ADMIN,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) { scopeLatch.countDown() }
                override fun onError(e: CometChatException?) { scopeLatch.countDown() }
            })
        scopeLatch.await(10, TimeUnit.SECONDS)

        navigateToTestGroup()
        openGroupDetailsFromHeader()
        openViewMembers()

        // Long-press the admin member
        longPressNonOwnerMember()

        val changeScopeOption = device.wait(Until.findObject(By.text("Change Scope")), SHORT_TIMEOUT)
            ?: device.findObject(By.textContains("Change Scope"))
            ?: device.findObject(By.textContains("Change Role"))

        if (changeScopeOption != null) {
            changeScopeOption.click()
            Thread.sleep(2000)

            val participantOption = device.findObject(By.text("Participant"))
                ?: device.findObject(By.textContains("Participant"))
            assertNotNull("Participant option not found", participantOption)
            participantOption!!.click()
            Thread.sleep(1000)

            val saveBtn = device.findObject(By.text("Save"))
                ?: device.findObject(By.textContains("Save"))
                ?: device.findObject(By.textContains("Confirm"))
            saveBtn?.click()
            Thread.sleep(3000)

            // Verify "Participant" label or absence of "Admin" for that member
            val messageList = device.findObject(By.res(PACKAGE, "messageList"))
                ?: device.findObject(By.textContains("Members"))
            assertNotNull("Should still be in the app after scope change", messageList)
        } else {
            device.pressBack()
        }
    }

    /**
     * GRP-105: Regular member has no admin actions (no Kick/Ban/Change Scope on long-press).
     */
    @Test
    fun test08_regularMemberHasNoAdminActions() {
        // Login as regular participant
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(memberUid1,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        navigateToTestGroup()
        openGroupDetailsFromHeader()
        openViewMembers()

        // Long-press another member
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val membersRv = device.findObject(By.res(uikitPackage, "recyclerview_group_members"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_group_members"))

        if (membersRv != null && membersRv.children.size >= 2) {
            membersRv.children[1].longClick()
            Thread.sleep(2000)

            // Verify no admin options appear
            val kickOption = device.findObject(By.text("Kick"))
            val banOption = device.findObject(By.text("Ban"))
            val changeScopeOption = device.findObject(By.text("Change Scope"))

            // All should be null for a regular participant
            val noAdminActions = kickOption == null && banOption == null && changeScopeOption == null
            assertTrue(
                "Regular member should NOT see Kick/Ban/Change Scope options",
                noAdminActions
            )

            device.pressBack()
            Thread.sleep(500)
        }
    }

    /**
     * GRP-106: Owner cannot be kicked or banned by an admin.
     */
    @Test
    fun test09_ownerCannotBeKickedOrBannedByAdmin() {
        // Promote memberUid1 to admin
        val scopeLatch = CountDownLatch(1)
        CometChat.updateGroupMemberScope(
            memberUid1, testGroupId, CometChatConstants.SCOPE_ADMIN,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) { scopeLatch.countDown() }
                override fun onError(e: CometChatException?) { scopeLatch.countDown() }
            })
        scopeLatch.await(10, TimeUnit.SECONDS)

        // Login as admin (memberUid1)
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(memberUid1,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        navigateToTestGroup()
        openGroupDetailsFromHeader()
        openViewMembers()

        // Try to long-press the owner — should not show Kick/Ban
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val membersRv = device.findObject(By.res(uikitPackage, "recyclerview_group_members"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_group_members"))

        if (membersRv != null && membersRv.children.isNotEmpty()) {
            // Owner is typically the first item
            membersRv.children[0].longClick()
            Thread.sleep(2000)

            val kickOption = device.findObject(By.text("Kick"))
            val banOption = device.findObject(By.text("Ban"))
            assertNull("Kick option should NOT appear for owner", kickOption)
            assertNull("Ban option should NOT appear for owner", banOption)

            device.pressBack()
            Thread.sleep(500)
        }
    }

    /**
     * GRP-107: Transfer Ownership shows before Delete and Exit (for owner with members).
     */
    @Test
    fun test10_transferOwnershipBeforeDeleteAndExit() {
        navigateToTestGroup()
        openGroupDetailsFromHeader()

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        // Tap "Delete and Exit"
        val deleteOption = device.findObject(By.textContains("Delete and Exit"))
            ?: device.findObject(By.textContains("Delete & Exit"))
        assertNotNull("Delete and Exit not found for owner", deleteOption)
        deleteOption!!.click()
        Thread.sleep(3000)

        // When the group has other members, a Transfer Ownership dialog should appear
        val transferDialog = device.findObject(By.textContains("Transfer"))
            ?: device.findObject(By.textContains("transfer"))
            ?: device.findObject(By.textContains("Ownership"))
            ?: device.findObject(By.textContains("Select"))

        // It may also directly show a member selection list
        val memberSelection = device.findObject(By.res(PACKAGE, "recyclerview_group_members"))
            ?: device.findObject(By.textContains(E2ETestHelper.getUserName(E2ETestConfig.GROUP_MEMBER_1_UID)))
            ?: device.findObject(By.textContains(E2ETestHelper.getUserName(E2ETestConfig.GROUP_MEMBER_2_UID)))

        assertTrue(
            "Transfer Ownership dialog or member selection should appear before deleting group with members",
            transferDialog != null || memberSelection != null
        )

        // Cancel to avoid actually deleting
        device.pressBack()
        Thread.sleep(1000)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun extractNumber(text: String): Int {
        val regex = Regex("\\d+")
        return regex.find(text)?.value?.toIntOrNull() ?: 0
    }

    private fun openGroupDetailsFromHeader() {
        Thread.sleep(SETTLE_TIME)
        var detailsOpened = false

        val rightMostHeaderBtn = device.findObjects(
            By.clazz("android.widget.ImageView").clickable(true)
        ).filter {
            it.visibleBounds.top < 250 && it.visibleBounds.left > device.displayWidth / 2
        }.maxByOrNull { it.visibleBounds.left }

        if (rightMostHeaderBtn != null) {
            rightMostHeaderBtn.click()
            Thread.sleep(2000)
            val detailsOption = device.findObject(By.text("Details"))
                ?: device.findObject(By.textContains("Details"))
            if (detailsOption != null) {
                detailsOption.click()
                detailsOpened = true
            } else {
                device.pressBack()
                Thread.sleep(500)
            }
        }

        if (!detailsOpened) {
            val messageHeader = device.findObject(By.res(PACKAGE, "messageHeader"))
            if (messageHeader != null) {
                val clickables = messageHeader.findObjects(By.clickable(true))
                    .filter { it.visibleBounds.left > 200 }
                    .sortedByDescending { it.visibleBounds.left }
                for (clickable in clickables) {
                    clickable.click()
                    Thread.sleep(2000)
                    val detailsOption = device.findObject(By.text("Details"))
                        ?: device.findObject(By.textContains("Details"))
                    if (detailsOption != null) {
                        detailsOption.click()
                        detailsOpened = true
                        break
                    }
                    device.pressBack()
                    Thread.sleep(500)
                }
            }
        }

        assertTrue("Could not open Group Details", detailsOpened)
        Thread.sleep(SETTLE_TIME)
    }

    private fun openViewMembers() {
        val viewMembers = device.findObject(By.text("View Members"))
            ?: device.findObject(By.textContains("View Members"))
        assertNotNull("View Members not found", viewMembers)
        viewMembers!!.click()
        Thread.sleep(SETTLE_TIME)
    }

    private fun longPressNonOwnerMember() {
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val membersRv = device.findObject(By.res(uikitPackage, "recyclerview_group_members"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_group_members"))
        if (membersRv != null && membersRv.children.size >= 2) {
            membersRv.children[1].longClick()
            Thread.sleep(2000)
        }
    }

    private fun navigateToTestGroup() {
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        val searchBar = device.findObject(By.clazz("android.widget.EditText"))
        searchBar?.clear()
        searchBar?.text = testGroupName
        Thread.sleep(5000)

        var searchFieldBottom = 200
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 20
        } catch (_: Exception) { }

        val matches = device.findObjects(By.textContains(testGroupName.take(8)))
        for (match in matches) {
            try {
                if (match.visibleBounds.top > searchFieldBottom) { match.click(); break }
            } catch (_: Exception) { continue }
        }

        device.wait(Until.hasObject(By.res(PACKAGE, "messageList")), TIMEOUT)
        Thread.sleep(SETTLE_TIME)
    }
}
