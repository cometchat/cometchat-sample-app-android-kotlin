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
import com.cometchat.chat.models.TextMessage
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
 * E2E tests for deleting messages in a group conversation (sample-app-kotlin).
 *
 * Setup: Creates a test group via SDK with a second member and a moderator.
 * The second member sends a message so we can test admin/moderator delete capabilities.
 *
 * Verified from source:
 * - Long-press popup option: "Delete" (from R.string.cometchat_delete)
 * - Delete confirmation dialog: single "Delete" button
 * - Deleted placeholder: "This message was deleted" or "deleted"
 *
 * Test IDs:
 * - GRP-048: testDeleteOwnMessageForMe
 * - GRP-049: testDeleteOwnMessageForEveryone
 * - GRP-050: testAdminDeletesOtherMemberMessage
 * - GRP-051: testRegularMemberCannotDeleteOthersMessage
 * - GRP-052: testDeletedMessageShowsDeletedPlaceholder
 * - GRP-054: testModeratorCanDeleteOtherMemberMessage
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.GroupDeleteMessageE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupDeleteMessageE2ETest {

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
        testGroupId = "grp_del_$ts"
        testGroupName = "DelTest$ts"

        // Create group (logged-in user becomes owner/admin)
        val latch = CountDownLatch(1)
        val group = Group(testGroupId, testGroupName, CometChatConstants.GROUP_TYPE_PUBLIC, "")
        CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(g: Group) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(15, TimeUnit.SECONDS)

        // Add other member as participant
        val addLatch = CountDownLatch(1)
        val members = listOf(GroupMember(otherMemberUid, CometChatConstants.SCOPE_PARTICIPANT))
        CometChat.addMembersToGroup(testGroupId, members, null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(r: HashMap<String, String>?) { addLatch.countDown() }
                override fun onError(e: CometChatException?) { addLatch.countDown() }
            })
        addLatch.await(10, TimeUnit.SECONDS)

        // Add moderator
        val modLatch = CountDownLatch(1)
        val modMembers = listOf(GroupMember(moderatorUid, CometChatConstants.SCOPE_MODERATOR))
        CometChat.addMembersToGroup(testGroupId, modMembers, null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(r: HashMap<String, String>?) { modLatch.countDown() }
                override fun onError(e: CometChatException?) { modLatch.countDown() }
            })
        modLatch.await(10, TimeUnit.SECONDS)

        // Send a message as the other member (for delete tests)
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(otherMemberUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        val sendLatch = CountDownLatch(1)
        val otherMsg = TextMessage(testGroupId, "OtherMsg$ts", CometChatConstants.RECEIVER_TYPE_GROUP)
        CometChat.sendMessage(otherMsg, object : CometChat.CallbackListener<TextMessage>() {
            override fun onSuccess(msg: TextMessage) { sendLatch.countDown() }
            override fun onError(e: CometChatException?) { sendLatch.countDown() }
        })
        sendLatch.await(10, TimeUnit.SECONDS)

        // Login back as test user (owner/admin)
        val reloginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(E2ETestHelper.testUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { reloginLatch.countDown() }
                override fun onError(e: CometChatException?) { reloginLatch.countDown() }
            })
        reloginLatch.await(10, TimeUnit.SECONDS)

        // Navigate to the group
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
     * GRP-048/049: Delete own message — message disappears after tapping Delete.
     */
    @Test
    fun test01_deleteOwnMessage() {
        // Send a message to delete
        val msg = "DeleteMe${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, msg)
        Thread.sleep(2000)
        E2ETestHelper.scrollDown(device)

        // Long-press the message
        E2ETestHelper.longPressMessage(device, msg)
        Thread.sleep(2000)

        // Tap "Delete"
        val deleteOption = device.wait(Until.findObject(By.text("Delete")), SHORT_TIMEOUT)
        assertNotNull("Delete option not found in popup", deleteOption)
        deleteOption!!.click()
        Thread.sleep(2000)

        // Confirm the delete (a confirmation dialog may appear with a "Delete" button)
        val confirmBtn = device.findObject(By.text("Delete"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes"))
        confirmBtn?.click()
        Thread.sleep(3000)

        // Verify the message is gone from view or shows deleted placeholder
        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)
        val messageStillVisible = device.findObject(By.textContains(msg))
        val deletedPlaceholder = device.findObject(By.textContains("deleted"))
        assertTrue(
            "Message should be removed or show deleted placeholder after delete",
            messageStillVisible == null || deletedPlaceholder != null
        )
    }

    /**
     * GRP-050: Admin (group owner) can delete another member's message.
     */
    @Test
    fun test03_adminDeletesOtherMemberMessage() {
        // The other member's message was sent in @Before
        Thread.sleep(2000)
        E2ETestHelper.scrollDown(device)

        // Find the other member's message — scroll up if not visible
        var otherMsg = device.findObject(By.textContains("OtherMsg"))
        if (otherMsg == null) {
            val displayHeight = device.displayHeight
            val displayWidth = device.displayWidth
            device.swipe(displayWidth / 2, displayHeight / 4, displayWidth / 2, displayHeight * 3 / 4, 20)
            Thread.sleep(2000)
            otherMsg = device.findObject(By.textContains("OtherMsg"))
        }
        assertNotNull("Other member's message not found in group", otherMsg)

        // Long-press it (we are admin/owner)
        E2ETestHelper.longPressMessage(device, "OtherMsg")
        Thread.sleep(2000)

        // Verify "Delete" option IS present for admin
        val deleteOption = device.wait(Until.findObject(By.text("Delete")), SHORT_TIMEOUT)
        assertNotNull("Delete option should appear for admin on other's message", deleteOption)
        deleteOption!!.click()
        Thread.sleep(2000)

        // Confirm the delete
        val confirmBtn = device.findObject(By.text("Delete"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes"))
        confirmBtn?.click()
        Thread.sleep(3000)

        // Verify the message is gone or shows deleted placeholder
        val messageGone = device.findObject(By.textContains("OtherMsg"))
        val deletedPlaceholder = device.findObject(By.textContains("deleted"))
        assertTrue(
            "Admin should be able to delete other member's message",
            messageGone == null || deletedPlaceholder != null
        )
    }

    /**
     * GRP-051: Regular member cannot delete other member's message.
     *
     * Login as the regular participant (otherMemberUid) and verify "Delete" is not
     * available for the admin's messages.
     */
    @Test
    fun test04_regularMemberCannotDeleteOthersMessage() {
        // First, send a message as admin so there's something for the member to try to delete
        val adminMsg = "AdminMsg${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, adminMsg)
        Thread.sleep(2000)

        // Now login as the other member (participant)
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(otherMemberUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        // Re-launch and navigate to the group
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        navigateToTestGroup()

        // Find the admin's message
        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)
        val targetMsg = device.findObject(By.textContains(adminMsg))
        if (targetMsg == null) {
            val displayHeight = device.displayHeight
            val displayWidth = device.displayWidth
            device.swipe(displayWidth / 2, displayHeight / 4, displayWidth / 2, displayHeight * 3 / 4, 20)
            Thread.sleep(2000)
        }

        // Long-press the admin's message
        E2ETestHelper.longPressMessage(device, adminMsg)
        Thread.sleep(2000)

        // Verify "Delete" option is NOT present for a regular member on someone else's message
        val deleteOption = device.findObject(By.text("Delete"))
        assertNull(
            "Delete option should NOT appear for regular member on other's message",
            deleteOption
        )

        // Dismiss popup
        device.pressBack()
        Thread.sleep(500)

        // Login back as test user for teardown
        val reloginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(E2ETestHelper.testUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { reloginLatch.countDown() }
                override fun onError(e: CometChatException?) { reloginLatch.countDown() }
            })
        reloginLatch.await(10, TimeUnit.SECONDS)
    }

    /**
     * GRP-052: Deleted message shows "deleted" placeholder text.
     */
    @Test
    fun test05_deletedMessageShowsDeletedPlaceholder() {
        // Send a message and delete it
        val msg = "Placeholder${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, msg)
        Thread.sleep(2000)
        E2ETestHelper.scrollDown(device)

        E2ETestHelper.longPressMessage(device, msg)
        Thread.sleep(2000)

        val deleteOption = device.wait(Until.findObject(By.text("Delete")), SHORT_TIMEOUT)
        assertNotNull("Delete option not found", deleteOption)
        deleteOption!!.click()
        Thread.sleep(2000)

        // Confirm the delete
        val confirmBtn = device.findObject(By.text("Delete"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes"))
        confirmBtn?.click()
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        // Verify the deleted placeholder appears
        val deletedPlaceholder = device.findObject(By.textContains("deleted"))
            ?: device.findObject(By.textContains("This message was deleted"))
        assertNotNull(
            "Deleted message should show a 'deleted' placeholder text",
            deletedPlaceholder
        )
    }

    /**
     * GRP-054: Moderator can delete another member's message.
     *
     * Login as moderator and verify they can delete the other participant's message.
     */
    @Test
    fun test06_moderatorCanDeleteOtherMemberMessage() {
        // Login as moderator
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(moderatorUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        // Re-launch and navigate to the group
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        navigateToTestGroup()

        // Find the other member's message (sent in @Before)
        Thread.sleep(2000)
        E2ETestHelper.scrollDown(device)
        var otherMsg = device.findObject(By.textContains("OtherMsg"))
        if (otherMsg == null) {
            val displayHeight = device.displayHeight
            val displayWidth = device.displayWidth
            device.swipe(displayWidth / 2, displayHeight / 4, displayWidth / 2, displayHeight * 3 / 4, 20)
            Thread.sleep(2000)
            otherMsg = device.findObject(By.textContains("OtherMsg"))
        }
        assertNotNull("Other member's message not found for moderator", otherMsg)

        // Long-press as moderator
        E2ETestHelper.longPressMessage(device, "OtherMsg")
        Thread.sleep(2000)

        // Verify "Delete" option IS present for moderator
        val deleteOption = device.wait(Until.findObject(By.text("Delete")), SHORT_TIMEOUT)
        assertNotNull("Delete option should appear for moderator on other's message", deleteOption)
        deleteOption!!.click()
        Thread.sleep(2000)

        // Confirm delete
        val confirmBtn = device.findObject(By.text("Delete"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes"))
        confirmBtn?.click()
        Thread.sleep(3000)

        // Verify the message is gone or shows deleted placeholder
        val messageGone = device.findObject(By.textContains("OtherMsg"))
        val deletedPlaceholder = device.findObject(By.textContains("deleted"))
        assertTrue(
            "Moderator should be able to delete other member's message",
            messageGone == null || deletedPlaceholder != null
        )

        // Login back as test user for teardown
        val reloginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(E2ETestHelper.testUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { reloginLatch.countDown() }
                override fun onError(e: CometChatException?) { reloginLatch.countDown() }
            })
        reloginLatch.await(10, TimeUnit.SECONDS)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

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
