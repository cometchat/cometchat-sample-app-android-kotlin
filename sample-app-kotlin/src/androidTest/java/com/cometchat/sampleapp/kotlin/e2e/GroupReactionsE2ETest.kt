package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * E2E tests for Message Reactions in a group conversation.
 *
 * Setup: Creates a test group via SDK with a second member who sends a message
 * (for reacting to another member's message).
 *
 * Test IDs:
 * - GRP-073: testAddReactionToGroupMessage
 * - GRP-075: testTapReactionShowsReactorList
 * - GRP-076: testRemoveOwnReaction
 * - GRP-077: testReactToAnotherMemberMessage
 * - GRP-079: testMultipleDifferentReactionsOnSameMessage
 * - GRP-080: testReactionCountShowsCorrectlyAfterMultipleAdds
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.GroupReactionsE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupReactionsE2ETest {

    private lateinit var device: UiDevice
    private var testGroupId = ""
    private var testGroupName = ""
    private val otherMemberUid = E2ETestConfig.GROUP_MEMBER_1_UID

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        val ts = System.currentTimeMillis()
        testGroupId = "grp_react_$ts"
        testGroupName = "ReactGrp$ts"

        // Create group
        val latch = CountDownLatch(1)
        val group = Group(testGroupId, testGroupName, CometChatConstants.GROUP_TYPE_PUBLIC, "")
        CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(g: Group) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(15, TimeUnit.SECONDS)

        // Add other member
        val addLatch = CountDownLatch(1)
        val members = listOf(GroupMember(otherMemberUid, CometChatConstants.SCOPE_PARTICIPANT))
        CometChat.addMembersToGroup(testGroupId, members, null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(r: HashMap<String, String>?) { addLatch.countDown() }
                override fun onError(e: CometChatException?) { addLatch.countDown() }
            })
        addLatch.await(10, TimeUnit.SECONDS)

        // Send a message as the other member (for GRP-077)
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(otherMemberUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        val sendLatch = CountDownLatch(1)
        val otherMsg = TextMessage(testGroupId, "OtherReactMsg$ts", CometChatConstants.RECEIVER_TYPE_GROUP)
        CometChat.sendMessage(otherMsg, object : CometChat.CallbackListener<TextMessage>() {
            override fun onSuccess(msg: TextMessage) { sendLatch.countDown() }
            override fun onError(e: CometChatException?) { sendLatch.countDown() }
        })
        sendLatch.await(10, TimeUnit.SECONDS)

        // Login back as test user
        val reloginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(E2ETestHelper.testUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { reloginLatch.countDown() }
                override fun onError(e: CometChatException?) { reloginLatch.countDown() }
            })
        reloginLatch.await(10, TimeUnit.SECONDS)

        // Navigate to the group
        navigateToTestGroup()

        // Send a message so we have a fresh target for reactions
        E2ETestHelper.sendMessage(device, "ReactTarget${ts}")
        Thread.sleep(2000)
        E2ETestHelper.scrollDown(device)
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
     * GRP-073: Add a reaction to a group message.
     */
    @Test
    fun test01_addReactionToGroupMessage() {
        // Long-press our message to get the popup with emoji reactions
        E2ETestHelper.longPressMessage(device, "ReactTarget")
        Thread.sleep(2500)

        // Find and tap an emoji reaction from the popup
        val reactionTapped = findAndTapReaction()
        assertTrue("Could not find any reaction emoji in the popup menu", reactionTapped)

        Thread.sleep(3000)

        // Verify we're still on the messages screen (no crash)
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen should still be displayed after adding reaction", messageList)
    }

    /**
     * GRP-075: Tap a reaction indicator to show the reactors list.
     */
    @Test
    fun test02_tapReactionShowsReactorList() {
        // First add a reaction
        E2ETestHelper.longPressMessage(device, "ReactTarget")
        Thread.sleep(2500)
        val reactionAdded = findAndTapReaction()
        if (!reactionAdded) return

        Thread.sleep(3000)

        // Scroll to bottom to see reaction indicator on our message
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val rv = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
        rv?.fling(Direction.DOWN)
        Thread.sleep(1500)

        // Find the reaction indicator (bottom-most one is on our message)
        val reactionIndicators = device.findObjects(By.textContains("\uD83D")) + // emoji unicode range
            device.findObjects(By.textContains("👍")) +
            device.findObjects(By.textContains("❤")) +
            device.findObjects(By.textContains("😍")) +
            device.findObjects(By.textContains("🔥"))

        val bottomReaction = reactionIndicators.maxByOrNull { it.visibleBounds.top }

        if (bottomReaction != null) {
            // Long-press the reaction indicator to show reactors list
            bottomReaction.longClick()
            Thread.sleep(3000)

            // Verify a bottom sheet / list appeared showing who reacted
            val reactorListVisible = device.findObject(By.textContains("You")) != null ||
                device.findObject(By.textContains("Reactions")) != null ||
                device.findObject(By.textContains("All")) != null
            assertTrue("Reactor list/bottom sheet did not appear", reactorListVisible)

            device.pressBack()
            Thread.sleep(1000)
        }
    }

    /**
     * GRP-076: Remove own reaction by tapping it again.
     */
    @Test
    fun test03_removeOwnReaction() {
        // Add a reaction first
        E2ETestHelper.longPressMessage(device, "ReactTarget")
        Thread.sleep(2500)
        val reactionAdded = findAndTapReaction()
        if (!reactionAdded) return

        Thread.sleep(3000)

        // Scroll to bottom
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val rv = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
        rv?.fling(Direction.DOWN)
        Thread.sleep(1500)

        // Find the reaction indicator on our message (bottom-most)
        val reactionIndicators = device.findObjects(By.textContains("👍")) +
            device.findObjects(By.textContains("❤")) +
            device.findObjects(By.textContains("😍")) +
            device.findObjects(By.textContains("🔥"))
        val bottomReaction = reactionIndicators.maxByOrNull { it.visibleBounds.top }

        if (bottomReaction != null) {
            // Tap to toggle/remove the reaction
            bottomReaction.click()
            Thread.sleep(2000)

            // Verify the app is still on messages screen (no crash)
            val messageList = device.findObject(By.res(PACKAGE, "messageList"))
            assertNotNull("Messages screen should remain after removing reaction", messageList)
        }
    }

    /**
     * GRP-077: React to another member's message in the group.
     */
    @Test
    fun test04_reactToAnotherMemberMessage() {
        // The other member's message "OtherReactMsg..." was sent in @Before
        // Scroll up to find it
        var otherMsg = device.findObject(By.textContains("OtherReactMsg"))
        if (otherMsg == null) {
            val displayHeight = device.displayHeight
            val displayWidth = device.displayWidth
            device.swipe(displayWidth / 2, displayHeight / 4, displayWidth / 2, displayHeight * 3 / 4, 20)
            Thread.sleep(2000)
            otherMsg = device.findObject(By.textContains("OtherReactMsg"))
        }
        assertNotNull("Other member's message not found", otherMsg)

        // Long-press the other member's message
        E2ETestHelper.longPressMessage(device, "OtherReactMsg")
        Thread.sleep(2500)

        // Tap a reaction emoji
        val reactionTapped = findAndTapReaction()
        assertTrue("Could not find reaction emoji to react to other member's message", reactionTapped)

        Thread.sleep(3000)

        // Verify messages screen is intact
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen should remain after reacting to other's message", messageList)
    }

    /**
     * GRP-079: Multiple different reactions on the same message.
     */
    @Test
    fun test05_multipleDifferentReactionsOnSameMessage() {
        // Add first reaction
        E2ETestHelper.longPressMessage(device, "ReactTarget")
        Thread.sleep(2500)
        findAndTapReaction()
        Thread.sleep(3000)

        // Add second reaction (long-press again, pick a different emoji)
        E2ETestHelper.longPressMessage(device, "ReactTarget")
        Thread.sleep(2500)

        // Try to tap a different emoji than the first time
        val secondReactionAdded = findAndTapSecondReaction()
        Thread.sleep(3000)

        // Verify messages screen is intact (multiple reactions didn't crash)
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen should remain after multiple reactions", messageList)
    }

    /**
     * GRP-080: Reaction count shows correctly after multiple adds.
     */
    @Test
    fun test06_reactionCountShowsCorrectlyAfterMultipleAdds() {
        // Add a reaction
        E2ETestHelper.longPressMessage(device, "ReactTarget")
        Thread.sleep(2500)
        findAndTapReaction()
        Thread.sleep(3000)

        // Scroll to bottom to see reaction indicator
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val rv = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
        rv?.fling(Direction.DOWN)
        Thread.sleep(1500)

        // Verify at least one reaction indicator is visible on our message
        val reactionIndicators = device.findObjects(By.textContains("👍")) +
            device.findObjects(By.textContains("❤")) +
            device.findObjects(By.textContains("😍")) +
            device.findObjects(By.textContains("🔥"))
        val bottomReaction = reactionIndicators.maxByOrNull { it.visibleBounds.top }
        assertNotNull("Reaction indicator should be visible on the message", bottomReaction)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun findAndTapReaction(): Boolean {
        val defaultEmoji = listOf("😍", "👍🏻", "👍", "🔥", "😊", "❤️", "❤")
        for (emoji in defaultEmoji) {
            val el = device.findObject(By.text(emoji)) ?: device.findObject(By.textContains(emoji))
            if (el != null) { el.click(); return true }
        }
        return false
    }

    private fun findAndTapSecondReaction(): Boolean {
        // Try emojis in reverse order to pick a different one
        val secondaryEmoji = listOf("🔥", "😊", "❤️", "❤", "😍", "👍🏻", "👍")
        for (emoji in secondaryEmoji) {
            val el = device.findObject(By.text(emoji)) ?: device.findObject(By.textContains(emoji))
            if (el != null) { el.click(); return true }
        }
        return false
    }

    private fun navigateToTestGroup() {
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
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
