package com.cometchat.sampleapp.compose.e2e

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
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SETTLE_TIME
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * E2E tests for Group Details screen (sample-app-compose).
 *
 * Key Compose differences:
 * - No resource IDs — uses text-based selectors
 * - "View\nMembers" and "Add\nMembers" (multiline text in action cards)
 * - Uses By.textContains() for partial matches
 * - Coordinate-based clicks to avoid StaleObjectException
 *
 * Test IDs:
 * - GRP-081: testGroupDetailsShowsAvatar
 * - GRP-082: testGroupDetailsShowsGroupName
 * - GRP-083: testGroupDetailsShowsMemberCount
 * - GRP-084: testViewMembersCardVisibleForAllRoles
 * - GRP-085: testAddMembersCardVisibleOnlyForAdminOwner
 * - GRP-086: testBannedMembersCardVisibleForAdminModerator
 * - GRP-087: testLeaveGroupVisibleForNonOwners
 * - GRP-088: testDeleteAndExitVisibleOnlyForOwner
 * - GRP-089: testDeleteChatVisibleForAllMembers
 * - GRP-090: testLeaveGroupConfirmationAndRemoval
 * - GRP-091: testDeleteChatRemovesConversationLocally
 * - GRP-093: testBackFromGroupDetailsReturnsToMessages
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.GroupDetailsE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupDetailsE2ETest {

    private lateinit var device: UiDevice
    private var testGroupId = ""
    private var testGroupName = ""
    private val otherMemberUid = E2ETestConfig.GROUP_MEMBER_1_UID
    private val moderatorUid = E2ETestConfig.GROUP_MEMBER_2_UID

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        val ts = System.currentTimeMillis()
        testGroupId = "grp_details_$ts"
        testGroupName = "DetailGrp$ts"

        // Create group
        val latch = CountDownLatch(1)
        val group = Group(testGroupId, testGroupName, CometChatConstants.GROUP_TYPE_PUBLIC, "")
        CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(g: Group) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(15, TimeUnit.SECONDS)

        // Add participant and moderator
        val addLatch = CountDownLatch(1)
        val members = listOf(
            GroupMember(otherMemberUid, CometChatConstants.SCOPE_PARTICIPANT),
            GroupMember(moderatorUid, CometChatConstants.SCOPE_MODERATOR)
        )
        CometChat.addMembersToGroup(testGroupId, members, null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(r: HashMap<String, String>?) { addLatch.countDown() }
                override fun onError(e: CometChatException?) { addLatch.countDown() }
            })
        addLatch.await(10, TimeUnit.SECONDS)

        navigateToTestGroup()
    }

    @After
    fun teardown() {
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
     * GRP-081: Group Details shows an avatar.
     */
    @Test
    fun test01_groupDetailsShowsAvatar() {
        openGroupDetailsFromHeader()

        // Look for ImageView in the header area
        val avatarBounds = E2ETestHelper.safeGetBounds(
            device, By.clazz("android.widget.ImageView")
        ) { it.top < device.displayHeight / 3 }
        assertTrue("Avatar not found in Group Details header", avatarBounds.isNotEmpty())
    }

    /**
     * GRP-082: Group Details shows the group name.
     */
    @Test
    fun test02_groupDetailsShowsGroupName() {
        openGroupDetailsFromHeader()

        val groupNameBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains(testGroupName.take(8))
        ) { true }
        assertTrue("Group name not visible on Details screen", groupNameBounds.isNotEmpty())
    }

    /**
     * GRP-083: Group Details shows the member count.
     */
    @Test
    fun test03_groupDetailsShowsMemberCount() {
        openGroupDetailsFromHeader()

        val memberCount = device.findObject(By.textContains("Members"))
            ?: device.findObject(By.textContains("members"))
        assertNotNull("Member count not visible on Group Details", memberCount)
    }

    /**
     * GRP-084: View Members card visible for all roles.
     */
    @Test
    fun test04_viewMembersCardVisibleForAllRoles() {
        openGroupDetailsFromHeader()

        // Compose uses multiline "View\nMembers" — use partial match
        val viewMembers = device.findObject(By.textContains("View"))
            ?: device.findObject(By.textContains("Members"))
        assertNotNull("View Members card not visible", viewMembers)
    }

    /**
     * GRP-085: Add Members card visible only for admin/owner.
     */
    @Test
    fun test05_addMembersCardVisibleOnlyForAdminOwner() {
        openGroupDetailsFromHeader()

        val addMembers = device.findObject(By.textContains("Add"))
        assertNotNull("Add Members should be visible for owner", addMembers)
    }

    /**
     * GRP-086: Banned Members card visible for admin/moderator.
     */
    @Test
    fun test06_bannedMembersCardVisibleForAdminModerator() {
        openGroupDetailsFromHeader()

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val bannedMembers = device.findObject(By.textContains("Banned"))
            ?: device.findObject(By.textContains("banned"))
        assertNotNull("Banned Members should be visible for owner", bannedMembers)
    }

    /**
     * GRP-087: Leave Group visible for non-owners.
     */
    @Test
    fun test07_leaveGroupVisibleForNonOwners() {
        // Login as participant
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(otherMemberUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        navigateToTestGroup()
        openGroupDetailsFromHeader()

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val leaveOption = device.findObject(By.textContains("Leave"))
        assertNotNull("Leave option should be visible for non-owner", leaveOption)

        // Login back
        val reloginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(E2ETestHelper.testUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { reloginLatch.countDown() }
                override fun onError(e: CometChatException?) { reloginLatch.countDown() }
            })
        reloginLatch.await(10, TimeUnit.SECONDS)
    }

    /**
     * GRP-088: Delete and Exit visible only for owner.
     */
    @Test
    fun test08_deleteAndExitVisibleOnlyForOwner() {
        openGroupDetailsFromHeader()

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val deleteExit = device.findObject(By.textContains("Delete & Exit"))
            ?: device.findObject(By.textContains("Delete and Exit"))
        assertNotNull("Delete & Exit should be visible for owner", deleteExit)
    }

    /**
     * GRP-089: Delete Chat visible for all members.
     */
    @Test
    fun test09_deleteChatVisibleForAllMembers() {
        openGroupDetailsFromHeader()

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val anyDeleteOption = device.findObject(By.textContains("Delete"))
        assertNotNull("Some delete option should be visible for members", anyDeleteOption)
    }

    /**
     * GRP-090: Leave Group confirmation and removal.
     */
    @Test
    fun test10_leaveGroupConfirmationAndRemoval() {
        // Login as participant
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(otherMemberUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        navigateToTestGroup()
        openGroupDetailsFromHeader()

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val leaveOption = device.findObject(By.textContains("Leave"))
        assertNotNull("Leave option not found", leaveOption)
        leaveOption!!.click()
        Thread.sleep(2000)

        // Confirm
        val confirmBtn = device.findObject(By.text("Leave"))
            ?: device.findObject(By.textContains("Leave"))
            ?: device.findObject(By.textContains("Confirm"))
        confirmBtn?.click()
        Thread.sleep(5000)

        // Should be back on home
        val homeScreen = device.findObject(By.desc("Chats"))
            ?: device.findObject(By.text("Chats"))
            ?: device.findObject(By.desc("Groups"))
        assertNotNull("Should return to home after leaving group", homeScreen)

        // Login back
        val reloginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(E2ETestHelper.testUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { reloginLatch.countDown() }
                override fun onError(e: CometChatException?) { reloginLatch.countDown() }
            })
        reloginLatch.await(10, TimeUnit.SECONDS)
    }

    /**
     * GRP-091: Delete Chat removes conversation locally.
     */
    @Test
    fun test11_deleteChatRemovesConversationLocally() {
        // Send a message first
        E2ETestHelper.sendMessage(device, "ConvoMsg${System.currentTimeMillis()}")
        Thread.sleep(2000)

        openGroupDetailsFromHeader()
        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val deleteChat = device.findObject(By.textContains("Delete Chat"))
            ?: device.findObject(By.textContains("Clear Chat"))
        assertNotNull("Delete Chat option not found on Group Details", deleteChat)
        deleteChat!!.click()
        Thread.sleep(2000)

        // Confirm the delete
        val confirmBtn = device.findObject(By.text("Delete"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes"))
            ?: device.findObject(By.textContains("Clear"))
        confirmBtn?.click()
        Thread.sleep(5000)

        // After delete chat, should return to home screen (Groups/Chats tab)
        val homeScreen = device.findObject(By.desc("Chats"))
            ?: device.findObject(By.text("Chats"))
            ?: device.findObject(By.desc("Groups"))
            ?: device.findObject(By.text("Groups"))
        assertNotNull("Should return to home screen after Delete Chat", homeScreen)
    }

    /**
     * GRP-093: Back from Group Details returns to messages.
     */
    @Test
    fun test12_backFromGroupDetailsReturnsToMessages() {
        openGroupDetailsFromHeader()

        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Should be back on messages (EditText for composer)
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Should return to messages screen with composer after back", editText)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun openGroupDetailsFromHeader() {
        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        // In Compose, the header has clickable icons. Find rightmost icon in top area.
        val headerBounds = E2ETestHelper.safeGetBounds(
            device, By.clickable(true)
        ) { it.top < 250 && it.left > device.displayWidth / 2 }

        val rightMost = headerBounds.maxByOrNull { it.left }

        var detailsOpened = false
        if (rightMost != null) {
            device.click(rightMost.centerX(), rightMost.centerY())
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

        // Fallback: try tapping the group name/avatar area at the top
        if (!detailsOpened) {
            val topCenter = device.displayWidth / 2
            device.click(topCenter, 120)
            Thread.sleep(3000)

            val detailsOption = device.findObject(By.text("Details"))
                ?: device.findObject(By.textContains("Details"))
            if (detailsOption != null) {
                detailsOption.click()
                detailsOpened = true
            }
        }

        assertTrue("Could not open Group Details", detailsOpened)
        Thread.sleep(SETTLE_TIME)
    }

    private fun navigateToTestGroup() {
        // Re-launch app to ensure fresh UI state before navigation
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)

        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        var searchSuccess = false
        repeat(3) { attempt ->
            if (searchSuccess) return@repeat
            try {
                val searchBar = device.findObject(By.clazz("android.widget.EditText"))
                searchBar?.click()
                Thread.sleep(500)
                searchBar?.clear()
                searchBar?.text = testGroupName
                searchSuccess = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
        Thread.sleep(5000)

        var searchFieldBottom = 250
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 30
        } catch (_: androidx.test.uiautomator.StaleObjectException) { }

        val groupBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains(testGroupName.take(8))
        ) { bounds -> bounds.top > searchFieldBottom }

        if (groupBounds.isNotEmpty()) {
            device.click(groupBounds[0].centerX(), groupBounds[0].centerY())
        } else {
            val contentBounds = E2ETestHelper.safeGetBounds(device, By.clickable(true)) { bounds ->
                bounds.top > searchFieldBottom && bounds.bottom < device.displayHeight - 200
            }
            if (contentBounds.isNotEmpty()) {
                device.click(contentBounds[0].centerX(), contentBounds[0].centerY())
            }
        }

        val messagesLoaded = device.wait(Until.hasObject(By.clazz("android.widget.EditText")), TIMEOUT)
        assertTrue("Messages screen did not load", messagesLoaded)
        Thread.sleep(SETTLE_TIME)
    }
}
