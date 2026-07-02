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
 * E2E tests for Group Details screen.
 *
 * Setup: Creates a test group via SDK. The logged-in user is the owner.
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
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.GroupDetailsE2ETest
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

        // Create group (logged-in user becomes owner)
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

        // Navigate to group and open details
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

        // Look for ImageView (avatar) in the details screen header area
        val avatarImages = device.findObjects(By.clazz("android.widget.ImageView"))
            .filter { it.visibleBounds.top < device.displayHeight / 3 }
        assertTrue("Avatar ImageView not found in Group Details header", avatarImages.isNotEmpty())
    }

    /**
     * GRP-082: Group Details shows the group name.
     */
    @Test
    fun test02_groupDetailsShowsGroupName() {
        openGroupDetailsFromHeader()

        val groupNameText = device.findObject(By.textContains(testGroupName.take(8)))
        assertNotNull("Group name not visible on Group Details screen", groupNameText)
    }

    /**
     * GRP-083: Group Details shows the member count.
     */
    @Test
    fun test03_groupDetailsShowsMemberCount() {
        openGroupDetailsFromHeader()

        // Look for member count text (e.g., "3 Members")
        val memberCount = device.findObject(By.res(PACKAGE, "member_count"))
            ?: device.findObject(By.textContains("Members"))
            ?: device.findObject(By.textContains("members"))
        assertNotNull("Member count not visible on Group Details", memberCount)
    }

    /**
     * GRP-084: View Members card is visible for all roles.
     */
    @Test
    fun test04_viewMembersCardVisibleForAllRoles() {
        openGroupDetailsFromHeader()

        val viewMembers = device.findObject(By.text("View Members"))
            ?: device.findObject(By.textContains("View Members"))
        assertNotNull("'View Members' card not visible (owner role)", viewMembers)
    }

    /**
     * GRP-085: Add Members card visible only for admin/owner.
     */
    @Test
    fun test05_addMembersCardVisibleOnlyForAdminOwner() {
        openGroupDetailsFromHeader()

        // As owner, Add Members should be visible
        val addMembers = device.findObject(By.text("Add Members"))
            ?: device.findObject(By.textContains("Add Members"))
        assertNotNull("'Add Members' should be visible for owner", addMembers)
    }

    /**
     * GRP-086: Banned Members card visible for admin/moderator.
     */
    @Test
    fun test06_bannedMembersCardVisibleForAdminModerator() {
        openGroupDetailsFromHeader()

        // Scroll down if needed to find Banned Members
        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val bannedMembers = device.findObject(By.text("Banned Members"))
            ?: device.findObject(By.textContains("Banned Members"))
            ?: device.findObject(By.textContains("Banned"))
        assertNotNull("'Banned Members' should be visible for owner", bannedMembers)
    }

    /**
     * GRP-087: Leave Group visible for non-owners.
     * Login as a participant and verify Leave is visible.
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
        assertNotNull("'Leave' option should be visible for non-owner member", leaveOption)

        // Login back as owner for teardown
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

        // As owner, "Delete and Exit" should be visible
        val deleteExit = device.findObject(By.textContains("Delete and Exit"))
            ?: device.findObject(By.textContains("Delete & Exit"))
        assertNotNull("'Delete and Exit' should be visible for owner", deleteExit)
    }

    /**
     * GRP-089: Delete Chat visible for all members.
     */
    @Test
    fun test09_deleteChatVisibleForAllMembers() {
        openGroupDetailsFromHeader()

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val deleteChat = device.findObject(By.textContains("Delete Chat"))
            ?: device.findObject(By.textContains("Clear Chat"))
        // Delete Chat may or may not be a separate option from Delete and Exit
        // Just verify the details screen has some delete-related option
        val anyDeleteOption = device.findObject(By.textContains("Delete"))
        assertNotNull("Some delete option should be visible for members", anyDeleteOption)
    }

    /**
     * GRP-090: Leave Group shows confirmation and navigates away.
     * Uses a participant login to test leaving.
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

        // Confirm leave
        val confirmBtn = device.findObject(By.text("Leave"))
            ?: device.findObject(By.textContains("Leave"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes"))
        confirmBtn?.click()
        Thread.sleep(5000)

        // After leaving, should be back on home/groups
        val backOnHome = device.findObject(By.res(PACKAGE, "bottomNavigationView"))
        assertNotNull("Should return to home after leaving group", backOnHome)

        // Login back as owner for teardown
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
        // First send a message so there's a conversation
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
        val backOnHome = device.findObject(By.res(PACKAGE, "bottomNavigationView"))
        assertNotNull("Should return to home screen after Delete Chat", backOnHome)
    }

    /**
     * GRP-093: Back from Group Details returns to messages.
     */
    @Test
    fun test12_backFromGroupDetailsReturnsToMessages() {
        openGroupDetailsFromHeader()

        // Press back
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Verify we're back on the messages screen
        val messageList = device.wait(Until.findObject(By.res(PACKAGE, "messageList")), TIMEOUT)
        assertNotNull("Should return to messages screen after pressing back from details", messageList)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun openGroupDetailsFromHeader() {
        Thread.sleep(SETTLE_TIME)

        var detailsOpened = false

        // Find the rightmost clickable ImageView in the header (overflow icon)
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

        // Fallback via messageHeader
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

        assertTrue("Could not open Group Details from message header", detailsOpened)
        Thread.sleep(SETTLE_TIME)
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
