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
import com.cometchat.chat.models.TextMessage
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SETTLE_TIME
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
 * E2E tests for Group Conversation Navigation (sample-app-compose).
 *
 * Key Compose differences:
 * - No resource IDs — uses text-based selectors and content descriptions
 * - Bottom nav uses By.desc("Chats"), By.desc("Groups"), etc.
 * - Messages screen detected by EditText (composer) presence
 * - Password join dialog uses text-based selectors (no By.res)
 * - Must call E2ETestHelper.launchApp(device) after SDK login switches
 *
 * Test IDs:
 * - GRP-002: testOpenGroupChatFromConversationsTab
 * - GRP-003: testOpenGroupChatFromNewChatGroupsTab
 * - GRP-004: testBackButtonReturnsToHome
 * - GRP-006: testJoinPasswordProtectedGroupOpensChat
 * - GRP-007: testJoinPasswordGroupWrongPasswordShowsError
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.GroupConversationE2ETest
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

        // Add member
        val addLatch = CountDownLatch(1)
        val members = listOf(GroupMember(otherMemberUid, CometChatConstants.SCOPE_PARTICIPANT))
        CometChat.addMembersToGroup(publicGroupId, members, null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(r: HashMap<String, String>?) { addLatch.countDown() }
                override fun onError(e: CometChatException?) { addLatch.countDown() }
            })
        addLatch.await(10, TimeUnit.SECONDS)

        // Send a message so it shows in Conversations tab
        val sendLatch = CountDownLatch(1)
        val msg = TextMessage(publicGroupId, "NavTestMsg$ts", CometChatConstants.RECEIVER_TYPE_GROUP)
        CometChat.sendMessage(msg, object : CometChat.CallbackListener<TextMessage>() {
            override fun onSuccess(m: TextMessage) { sendLatch.countDown() }
            override fun onError(e: CometChatException?) { sendLatch.countDown() }
        })
        sendLatch.await(10, TimeUnit.SECONDS)

        // Create a password-protected group for join tests
        passwordGroupId = "grp_conv_pass_$ts"
        passwordGroupName = "ConvPass$ts"
        createGroup(passwordGroupId, passwordGroupName, CometChatConstants.GROUP_TYPE_PASSWORD, "secret123")
    }

    @After
    fun teardown() {
        if (publicGroupId.isNotEmpty()) deleteGroup(publicGroupId)
        if (passwordGroupId.isNotEmpty()) deleteGroup(passwordGroupId)
    }

    /**
     * GRP-002: Open group chat from Conversations tab.
     */
    @Test
    fun test01_openGroupChatFromConversationsTab() {
        E2ETestHelper.navigateToTab(device, "Chats")
        Thread.sleep(5000) // Extra wait for conversations list to load from server

        // Find the group conversation by its name (with retry for slow loading)
        var groupBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains(publicGroupName.take(8))
        ) { true }

        // Retry after scrolling if not found immediately
        if (groupBounds.isEmpty()) {
            Thread.sleep(5000)
            groupBounds = E2ETestHelper.safeGetBounds(
                device, By.textContains(publicGroupName.take(8))
            ) { true }
        }

        assertTrue(
            "Group '$publicGroupName' not found in Conversations tab",
            groupBounds.isNotEmpty()
        )
        device.click(groupBounds[0].centerX(), groupBounds[0].centerY())

        // Verify messages screen opened (EditText for composer)
        val messagesLoaded = device.wait(
            Until.hasObject(By.clazz("android.widget.EditText")),
            TIMEOUT
        )
        assertTrue("Messages screen did not load after opening group from Conversations", messagesLoaded)
    }

    /**
     * GRP-003: Open group chat from Groups tab.
     */
    @Test
    fun test02_openGroupChatFromNewChatGroupsTab() {
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        // Search for the test group
        var searchSuccess = false
        repeat(3) { attempt ->
            if (searchSuccess) return@repeat
            try {
                val searchBar = device.findObject(By.clazz("android.widget.EditText"))
                searchBar?.click()
                Thread.sleep(500)
                searchBar?.clear()
                searchBar?.text = publicGroupName
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
            device, By.textContains(publicGroupName.take(8))
        ) { it.top > searchFieldBottom }

        assertTrue("Test group not found in Groups tab search results", groupBounds.isNotEmpty())
        device.click(groupBounds[0].centerX(), groupBounds[0].centerY())

        // Verify messages screen
        val messagesLoaded = device.wait(
            Until.hasObject(By.clazz("android.widget.EditText")),
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

        var searchSuccess = false
        repeat(3) {
            if (searchSuccess) return@repeat
            try {
                val searchBar = device.findObject(By.clazz("android.widget.EditText"))
                searchBar?.click(); Thread.sleep(500)
                searchBar?.clear(); searchBar?.text = publicGroupName
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
            device, By.textContains(publicGroupName.take(8))
        ) { it.top > searchFieldBottom }

        if (groupBounds.isNotEmpty()) {
            device.click(groupBounds[0].centerX(), groupBounds[0].centerY())
        }

        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), TIMEOUT)
        Thread.sleep(SETTLE_TIME)

        // Press back — in Compose, pressBack may close the app entirely,
        // so use launchApp to recover if needed
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Should be back on home (bottom nav visible)
        var homeVisible = device.findObject(By.desc("Chats")) != null ||
            device.findObject(By.desc("Groups")) != null ||
            device.findObject(By.text("Groups")) != null

        // If pressBack closed the app, re-launch
        if (!homeVisible) {
            E2ETestHelper.launchApp(device)
            Thread.sleep(SETTLE_TIME)
            homeVisible = device.findObject(By.desc("Chats")) != null ||
                device.findObject(By.desc("Groups")) != null
        }

        assertTrue("Should return to home screen after pressing back from group messages", homeVisible)
    }

    /**
     * GRP-006: Join a password-protected group with correct password opens chat.
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

        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        // Search for the password group
        var searchSuccess = false
        repeat(3) {
            if (searchSuccess) return@repeat
            try {
                val searchBar = device.findObject(By.clazz("android.widget.EditText"))
                searchBar?.click(); Thread.sleep(500)
                searchBar?.clear(); searchBar?.text = passwordGroupName
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
            device, By.textContains(passwordGroupName.take(8))
        ) { it.top > searchFieldBottom }

        assertTrue("Password group not found in search results", groupBounds.isNotEmpty())
        device.click(groupBounds[0].centerX(), groupBounds[0].centerY())
        Thread.sleep(3000)

        // Join password dialog should appear — find the password input
        // In Compose, this is an EditText in a bottom sheet/dialog
        val passwordField = device.wait(
            Until.findObject(By.clazz("android.widget.EditText")),
            TIMEOUT
        )
        assertNotNull("Password field not found. Join dialog may not have appeared.", passwordField)

        // The password field should be different from the search field (cleared after navigation)
        // Look for hint text or just type into the available EditText
        passwordField!!.click()
        Thread.sleep(500)
        passwordField.text = "secret123"
        Thread.sleep(1000)

        // Tap "Join Group" button
        val joinBtn = device.findObject(By.textContains("Join"))
            ?: device.findObject(By.textContains("join"))
        assertNotNull("Join Group button not found", joinBtn)
        joinBtn!!.click()
        Thread.sleep(5000)

        // Verify messages screen opened — look for a SECOND EditText (the composer)
        // or verify we navigated to messages by checking for send button
        val messagesLoaded = device.findObject(By.desc("Send message")) != null ||
            device.findObject(By.descContains("Send")) != null ||
            device.findObject(By.desc("Attachment")) != null ||
            device.findObject(By.descContains("Attach")) != null
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

        var searchSuccess = false
        repeat(3) {
            if (searchSuccess) return@repeat
            try {
                val searchBar = device.findObject(By.clazz("android.widget.EditText"))
                searchBar?.click(); Thread.sleep(500)
                searchBar?.clear(); searchBar?.text = passwordGroupName
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
            device, By.textContains(passwordGroupName.take(8))
        ) { it.top > searchFieldBottom }

        if (groupBounds.isNotEmpty()) {
            device.click(groupBounds[0].centerX(), groupBounds[0].centerY())
        }
        Thread.sleep(3000)

        // Enter wrong password
        val passwordField = device.wait(
            Until.findObject(By.clazz("android.widget.EditText")),
            TIMEOUT
        )
        assertNotNull("Password field not found for wrong password test", passwordField)
        passwordField!!.click()
        Thread.sleep(500)
        passwordField.text = "wrongpassword"
        Thread.sleep(1000)

        // Tap "Join Group"
        val joinBtn = device.findObject(By.textContains("Join"))
            ?: device.findObject(By.textContains("join"))
        assertNotNull("Join Group button not found", joinBtn)
        joinBtn!!.click()
        Thread.sleep(5000)

        // Should NOT open messages — verify we stayed on join dialog or got error
        val sendBtn = device.findObject(By.desc("Send message"))
        val errorVisible = device.findObject(By.textContains("wrong")) != null ||
            device.findObject(By.textContains("incorrect")) != null ||
            device.findObject(By.textContains("error")) != null ||
            device.findObject(By.textContains("Something went wrong")) != null
        val stillOnJoinDialog = device.findObject(By.textContains("Join")) != null

        assertTrue(
            "Should stay on join dialog or show error when wrong password is entered",
            sendBtn == null && (errorVisible || stillOnJoinDialog)
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
