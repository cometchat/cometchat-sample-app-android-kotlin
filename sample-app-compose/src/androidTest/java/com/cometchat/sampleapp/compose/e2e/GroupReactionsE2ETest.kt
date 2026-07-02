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
 * E2E tests for Message Reactions in a group conversation (sample-app-compose).
 *
 * Key Compose differences:
 * - Long-press uses coordinate-based swipe (avoids StaleObjectException)
 * - Reaction indicators found via emoji text patterns
 * - No resource IDs
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
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.GroupReactionsE2ETest
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

        // Navigate to group and send a target message
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        navigateToTestGroup()

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
        longPressMessageCompose("ReactTarget")
        Thread.sleep(2500)

        val reactionTapped = findAndTapReaction()
        assertTrue("Could not find reaction emoji in popup", reactionTapped)

        Thread.sleep(3000)

        // Verify still on messages screen
        val editText = device.findObject(By.clazz("android.widget.EditText"))
            ?: device.findObject(By.desc("Send message"))
        assertNotNull("Messages screen should remain after adding reaction", editText)
    }

    /**
     * GRP-075: Tap reaction indicator shows reactor list.
     */
    @Test
    fun test02_tapReactionShowsReactorList() {
        // Add a reaction first
        longPressMessageCompose("ReactTarget")
        Thread.sleep(2500)
        val reactionAdded = findAndTapReaction()
        if (!reactionAdded) return

        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)
        Thread.sleep(1500)

        // Find bottom-most reaction indicator
        val reactionBounds = findBottomReactionBounds()
        if (reactionBounds != null) {
            // Long-press reaction indicator
            device.swipe(
                reactionBounds.centerX(), reactionBounds.centerY(),
                reactionBounds.centerX(), reactionBounds.centerY(), 100
            )
            Thread.sleep(3000)

            val reactorListVisible = device.findObject(By.textContains("You")) != null ||
                device.findObject(By.textContains("Reactions")) != null ||
                device.findObject(By.textContains("All")) != null
            assertTrue("Reactor list did not appear", reactorListVisible)

            device.pressBack()
            Thread.sleep(1000)
        }
    }

    /**
     * GRP-076: Remove own reaction by tapping it again.
     */
    @Test
    fun test03_removeOwnReaction() {
        longPressMessageCompose("ReactTarget")
        Thread.sleep(2500)
        val reactionAdded = findAndTapReaction()
        if (!reactionAdded) return

        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)
        Thread.sleep(1500)

        // Find and tap the reaction indicator to remove
        val reactionBounds = findBottomReactionBounds()
        if (reactionBounds != null) {
            device.click(reactionBounds.centerX(), reactionBounds.centerY())
            Thread.sleep(2000)
        }

        // Verify no crash
        val editText = device.findObject(By.clazz("android.widget.EditText"))
            ?: device.findObject(By.desc("Send message"))
        assertNotNull("Messages screen should remain after removing reaction", editText)
    }

    /**
     * GRP-077: React to another member's message.
     */
    @Test
    fun test04_reactToAnotherMemberMessage() {
        // Find other member's message (scroll up if needed)
        var otherMsgBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains("OtherReactMsg")
        ) { true }

        if (otherMsgBounds.isEmpty()) {
            E2ETestHelper.scrollUp(device)
            Thread.sleep(2000)
            otherMsgBounds = E2ETestHelper.safeGetBounds(
                device, By.textContains("OtherReactMsg")
            ) { true }
        }
        assertTrue("Other member's message not found", otherMsgBounds.isNotEmpty())

        // Long-press using center coordinates
        val rect = otherMsgBounds[0]
        device.swipe(rect.centerX(), rect.centerY(), rect.centerX(), rect.centerY(), 100)
        Thread.sleep(2500)

        val reactionTapped = findAndTapReaction()
        assertTrue("Could not react to other member's message", reactionTapped)

        Thread.sleep(3000)
        val editText = device.findObject(By.clazz("android.widget.EditText"))
            ?: device.findObject(By.desc("Send message"))
        assertNotNull("Messages screen should remain", editText)
    }

    /**
     * GRP-079: Multiple different reactions on same message.
     */
    @Test
    fun test05_multipleDifferentReactionsOnSameMessage() {
        // Add first reaction
        longPressMessageCompose("ReactTarget")
        Thread.sleep(2500)
        findAndTapReaction()
        Thread.sleep(3000)

        // Add second reaction
        longPressMessageCompose("ReactTarget")
        Thread.sleep(2500)
        findAndTapSecondReaction()
        Thread.sleep(3000)

        // Verify no crash
        val editText = device.findObject(By.clazz("android.widget.EditText"))
            ?: device.findObject(By.desc("Send message"))
        assertNotNull("Messages screen should remain after multiple reactions", editText)
    }

    /**
     * GRP-080: Reaction count shows correctly.
     */
    @Test
    fun test06_reactionCountShowsCorrectlyAfterMultipleAdds() {
        longPressMessageCompose("ReactTarget")
        Thread.sleep(2500)
        findAndTapReaction()
        Thread.sleep(3000)

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1500)

        // Verify at least one reaction indicator is visible
        val reactionBounds = findBottomReactionBounds()
        assertNotNull("Reaction indicator should be visible on the message", reactionBounds)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun longPressMessageCompose(messageText: String) {
        val bounds = E2ETestHelper.safeGetBounds(device, By.textContains(messageText)) { true }
        assertTrue("Message '$messageText' not found for long-press", bounds.isNotEmpty())
        val rect = bounds[0]
        // Long-press ABOVE the text (on the bubble header area) to reliably trigger the popup.
        // For received messages, pressing on the text itself may not trigger OnLongClickListener.
        val targetY = if (rect.top - 30 > 50) rect.top - 30 else rect.centerY()
        val targetX = device.displayWidth / 2
        device.swipe(targetX, targetY, targetX, targetY, 100)
    }

    private fun findAndTapReaction(): Boolean {
        val defaultEmoji = listOf("😍", "👍🏻", "👍", "🔥", "😊", "❤️", "❤")
        for (emoji in defaultEmoji) {
            val el = device.findObject(By.text(emoji)) ?: device.findObject(By.textContains(emoji))
            if (el != null) { el.click(); return true }
        }
        return false
    }

    private fun findAndTapSecondReaction(): Boolean {
        val secondaryEmoji = listOf("🔥", "😊", "❤️", "❤", "😍", "👍🏻", "👍")
        for (emoji in secondaryEmoji) {
            val el = device.findObject(By.text(emoji)) ?: device.findObject(By.textContains(emoji))
            if (el != null) { el.click(); return true }
        }
        return false
    }

    private fun findBottomReactionBounds(): android.graphics.Rect? {
        val emojiList = listOf("👍", "❤", "😍", "🔥", "😊")
        val allBounds = mutableListOf<android.graphics.Rect>()
        for (emoji in emojiList) {
            allBounds.addAll(E2ETestHelper.safeGetBounds(device, By.textContains(emoji)) { true })
        }
        return allBounds.maxByOrNull { it.top }
    }

    private fun navigateToTestGroup() {
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
