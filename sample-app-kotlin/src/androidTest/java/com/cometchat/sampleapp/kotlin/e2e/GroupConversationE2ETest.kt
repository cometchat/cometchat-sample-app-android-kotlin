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
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.TIMEOUT
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
 * E2E tests for Group Conversation Navigation (sample-app-kotlin).
 *
 * Verifies opening group chats from various entry points and
 * joining password-protected groups.
 *
 * Test IDs:
 * - GRP-002: testOpenGroupChatFromConversationsTab
 * - GRP-003: testOpenGroupChatFromNewChatGroupsTab
 * - GRP-004: testBackButtonReturnsToHome
 * - GRP-006: testJoinPasswordProtectedGroupOpensChat
 * - GRP-007: testJoinPasswordGroupWrongPasswordShowsError
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.GroupConversationE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupConversationE2ETest {

    private lateinit var device: UiDevice
    private var publicGroupId = ""
    private var publicGroupName = ""
    private var passwordGroupId = ""
    private var passwordGroupName = ""
    private val otherMemberUid = E2ETestConfig.GROUP_MEMBER_1_UID

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        val ts = System.currentTimeMillis()

        // Create a public group for navigation tests
        publicGroupId = "grp_conv_pub_$ts"
        publicGroupName = "ConvPub$ts"
        createGroup(publicGroupId, publicGroupName, CometChatConstants.GROUP_TYPE_PUBLIC, "")

        // Add member so the group has content
        val addLatch = CountDownLatch(1)
        val members = listOf(GroupMember(otherMemberUid, CometChatConstants.SCOPE_PARTICIPANT))
        CometChat.addMembersToGroup(publicGroupId, members, null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(r: HashMap<String, String>?) { addLatch.countDown() }
                override fun onError(e: CometChatException?) { addLatch.countDown() }
            })
        addLatch.await(10, TimeUnit.SECONDS)

        // Send a message so it shows up in Conversations tab
        val sendLatch = CountDownLatch(1)
        val sendOk = java.util.concurrent.atomic.AtomicBoolean(false)
        val msg = TextMessage(publicGroupId, "NavTestMsg$ts", CometChatConstants.RECEIVER_TYPE_GROUP)
        CometChat.sendMessage(msg, object : CometChat.CallbackListener<TextMessage>() {
            override fun onSuccess(m: TextMessage) { sendOk.set(true); sendLatch.countDown() }
            override fun onError(e: CometChatException?) { sendLatch.countDown() }
        })
        sendLatch.await(10, TimeUnit.SECONDS)
        assertTrue("Seed group message failed to send — conversation won't appear in Chats", sendOk.get())

        // Create a password-protected group for join tests
        passwordGroupId = "grp_conv_pass_$ts"
        passwordGroupName = "ConvPass$ts"
        createGroup(passwordGroupId, passwordGroupName, CometChatConstants.GROUP_TYPE_PASSWORD, "secret123")

        // The Conversations list was fetched at login, BEFORE these groups existed. Relaunch so it
        // re-fetches from the server and includes the freshly-seeded group conversation (matches the
        // SDK-seed-then-refresh pattern used elsewhere, e.g. MessagesE2ETest#test15).
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
    }

    @After
    fun teardown() {
        if (publicGroupId.isNotEmpty()) {
            deleteGroup(publicGroupId)
        }
        if (passwordGroupId.isNotEmpty()) {
            deleteGroup(passwordGroupId)
        }
    }

    /**
     * GRP-002: Open group chat from Conversations tab.
     * After sending a message to the group, it should appear in Conversations.
     * Tapping it should open the group messages screen.
     */
    @Test
    fun test01_openGroupChatFromConversationsTab() {
        // Navigate to Chats tab
        E2ETestHelper.navigateToTab(device, "Chats")
        Thread.sleep(5000) // Extra wait for conversations list to load from server

        // Find the group conversation by its name (extended timeout)
        val groupConvo = device.wait(
            Until.findObject(By.textContains(publicGroupName.take(8))),
            20_000L
        )
        assertNotNull(
            "Group '$publicGroupName' not found in Conversations tab. " +
                "Ensure a message was sent to make it appear.",
            groupConvo
        )
        groupConvo!!.click()

        // Verify messages screen opened (messageList component)
        val messagesLoaded = device.wait(
            Until.hasObject(By.res(PACKAGE, "messageList")),
            TIMEOUT
        )
        assertTrue("Messages screen did not load after opening group from Conversations", messagesLoaded)
    }

    /**
     * GRP-003: Open group chat from Groups tab.
     * Navigate to Groups, find the test group, tap it to open messages.
     */
    @Test
    fun test02_openGroupChatFromNewChatGroupsTab() {
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        // Search for our test group
        val searchBar = device.findObject(By.clazz("android.widget.EditText"))
        searchBar?.clear()
        searchBar?.text = publicGroupName
        Thread.sleep(5000)

        // Find and click the group (below search field)
        var searchFieldBottom = 200
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 20
        } catch (_: Exception) { }

        val matches = device.findObjects(By.textContains(publicGroupName.take(8)))
        var clicked = false
        for (match in matches) {
            try {
                if (match.visibleBounds.top > searchFieldBottom) {
                    match.click()
                    clicked = true
                    break
                }
            } catch (_: Exception) { continue }
        }
        assertTrue("Test group not found in Groups tab search results", clicked)

        // Verify messages screen
        val messagesLoaded = device.wait(
            Until.hasObject(By.res(PACKAGE, "messageList")),
            TIMEOUT
        )
        assertTrue("Messages screen did not load after opening group from Groups tab", messagesLoaded)
    }

    /**
     * GRP-004: Back button returns to Home from group messages.
     */
    @Test
    fun test03_backButtonReturnsToHome() {
        // Open the group first
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        val searchBar = device.findObject(By.clazz("android.widget.EditText"))
        searchBar?.clear()
        searchBar?.text = publicGroupName
        Thread.sleep(5000)

        var searchFieldBottom = 200
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 20
        } catch (_: Exception) { }

        val matches = device.findObjects(By.textContains(publicGroupName.take(8)))
        for (match in matches) {
            try {
                if (match.visibleBounds.top > searchFieldBottom) { match.click(); break }
            } catch (_: Exception) { continue }
        }

        device.wait(Until.hasObject(By.res(PACKAGE, "messageList")), TIMEOUT)
        Thread.sleep(SETTLE_TIME)

        // Press back
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Should be back on home (bottomNavigationView visible)
        val homeVisible = device.findObject(By.res(PACKAGE, "bottomNavigationView")) != null ||
            device.findObject(By.res(PACKAGE, "groups_list")) != null
        assertTrue("Should return to home screen after pressing back from group messages", homeVisible)
    }

    /**
     * GRP-006: Join a password-protected group with correct password opens chat.
     *
     * Flow:
     * 1. Login as a different user who is NOT already a member
     * 2. Navigate to Groups tab, find the password group
     * 3. Tap it — join password bottom sheet appears
     * 4. Enter the correct password, tap "Join Group"
     * 5. Verify messages screen opens
     */
    @Test
    fun test04_joinPasswordProtectedGroupOpensChat() {
        // Login as another user who is NOT a member of the password group
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(otherMemberUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)

        // Navigate to Groups tab and find the password group
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        val searchBar = device.findObject(By.clazz("android.widget.EditText"))
        searchBar?.clear()
        searchBar?.text = passwordGroupName
        Thread.sleep(5000)

        var searchFieldBottom = 200
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 20
        } catch (_: Exception) { }

        val matches = device.findObjects(By.textContains(passwordGroupName.take(8)))
        for (match in matches) {
            try {
                if (match.visibleBounds.top > searchFieldBottom) { match.click(); break }
            } catch (_: Exception) { continue }
        }
        Thread.sleep(3000)

        // Join password bottom sheet should appear with password field
        val passwordField = device.wait(
            Until.findObject(By.res(PACKAGE, "et_password")),
            TIMEOUT
        )
        assertNotNull("Password field (et_password) not found. Join dialog may not have appeared.", passwordField)
        passwordField!!.text = "secret123"
        Thread.sleep(1000)

        // Tap "Join Group" button
        val joinBtn = device.findObject(By.res(PACKAGE, "join_group_btn_text"))
            ?: device.findObject(By.textContains("Join Group"))
            ?: device.findObject(By.textContains("Join"))
        assertNotNull("Join Group button not found", joinBtn)
        joinBtn!!.click()
        Thread.sleep(5000)

        // Verify messages screen opened
        val messagesLoaded = device.wait(
            Until.hasObject(By.res(PACKAGE, "messageList")),
            TIMEOUT
        )
        assertTrue("Messages screen did not open after joining password group", messagesLoaded)

        // Login back as main user
        val reloginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(E2ETestHelper.testUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { reloginLatch.countDown() }
                override fun onError(e: CometChatException?) { reloginLatch.countDown() }
            })
        reloginLatch.await(10, TimeUnit.SECONDS)
    }

    /**
     * GRP-007: Joining a password group with wrong password shows error.
     *
     * Flow:
     * 1. Login as a different user not in the group
     * 2. Navigate to group, enter wrong password
     * 3. Tap join — error message should appear
     */
    @Test
    fun test05_joinPasswordGroupWrongPasswordShowsError() {
        // Login as another user
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(otherMemberUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)

        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        val searchBar = device.findObject(By.clazz("android.widget.EditText"))
        searchBar?.clear()
        searchBar?.text = passwordGroupName
        Thread.sleep(5000)

        var searchFieldBottom = 200
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 20
        } catch (_: Exception) { }

        val matches = device.findObjects(By.textContains(passwordGroupName.take(8)))
        for (match in matches) {
            try {
                if (match.visibleBounds.top > searchFieldBottom) { match.click(); break }
            } catch (_: Exception) { continue }
        }
        Thread.sleep(3000)

        // Enter wrong password
        val passwordField = device.wait(
            Until.findObject(By.res(PACKAGE, "et_password")),
            TIMEOUT
        )
        assertNotNull("Password field not found for wrong password test", passwordField)
        passwordField!!.text = "wrongpassword"
        Thread.sleep(1000)

        // Tap "Join Group"
        val joinBtn = device.findObject(By.res(PACKAGE, "join_group_btn_text"))
            ?: device.findObject(By.textContains("Join Group"))
            ?: device.findObject(By.textContains("Join"))
        assertNotNull("Join Group button not found", joinBtn)
        joinBtn!!.click()
        Thread.sleep(5000)

        // Should NOT open messages — still on the join dialog or showing error
        val messagesLoaded = device.findObject(By.res(PACKAGE, "messageList"))
        val errorVisible = device.findObject(By.res(PACKAGE, "tv_error")) != null ||
            device.findObject(By.textContains("wrong")) != null ||
            device.findObject(By.textContains("incorrect")) != null ||
            device.findObject(By.textContains("error")) != null ||
            device.findObject(By.textContains("Something went wrong")) != null
        val stillOnJoinDialog = device.findObject(By.res(PACKAGE, "et_password")) != null

        assertTrue(
            "Should stay on join dialog or show error when wrong password is entered",
            messagesLoaded == null && (errorVisible || stillOnJoinDialog)
        )

        // Login back
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

    private fun createGroup(groupId: String, groupName: String, type: String, password: String) {
        val latch = CountDownLatch(1)
        val group = Group(groupId, groupName, type, password)
        CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(g: Group) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(15, TimeUnit.SECONDS)
    }

    private fun deleteGroup(groupId: String) {
        val latch = CountDownLatch(1)
        CometChat.deleteGroup(groupId, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(10, TimeUnit.SECONDS)
    }
}
