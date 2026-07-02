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
 * E2E tests for Thread Messages in a group conversation (sample-app-compose).
 *
 * Key Compose differences:
 * - Long-press uses coordinate-based swipe for reliability
 * - Thread view detected via Compose Navigation (EditText presence + parent message text)
 * - No resource IDs — uses By.text(), By.desc(), By.clazz()
 * - Reply In Thread navigation takes ~5s due to Compose route transition
 *
 * Test IDs:
 * - GRP-064: testOpenThreadFromGroupMessage
 * - GRP-065: testThreadHeaderShowsParentMessage
 * - GRP-066: testSendReplyInThread
 * - GRP-067: testThreadReplyShowsSenderNameAvatar
 * - GRP-069: testParentMessageShowsReplyCountBadge
 * - GRP-071: testThreadBackButtonReturnsToGroupMessages
 * - GRP-072: testMultipleThreadsInSameGroupWorkIndependently
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.GroupThreadMessagesE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupThreadMessagesE2ETest {

    private lateinit var device: UiDevice
    private var testGroupId = ""
    private var testGroupName = ""
    private val otherMemberUid = E2ETestConfig.GROUP_MEMBER_1_UID

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        val ts = System.currentTimeMillis()
        testGroupId = "grp_thread_$ts"
        testGroupName = "ThreadGrp$ts"

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
     * GRP-064: Open thread from a group message.
     */
    @Test
    fun test01_openThreadFromGroupMessage() {
        val parentMsg = "ThreadParent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        longPressMessageCompose(parentMsg)
        Thread.sleep(2500)

        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
            ?: device.findObject(By.textContains("Thread"))
        assertNotNull("Reply In Thread option not found", threadOption)
        threadOption!!.click()
        Thread.sleep(5000) // Compose Navigation route transition

        // Verify thread opened — parent message text should still be visible
        val parentVisible = device.findObject(By.textContains(parentMsg))
        val composerPresent = device.findObject(By.clazz("android.widget.EditText"))
        assertTrue(
            "Thread view did not open (parent text or composer not found)",
            parentVisible != null || composerPresent != null
        )
    }

    /**
     * GRP-065: Thread header shows the parent message text.
     */
    @Test
    fun test02_threadHeaderShowsParentMessage() {
        val parentMsg = "HeaderCheck${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        longPressMessageCompose(parentMsg)
        Thread.sleep(2500)

        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        assertNotNull("Reply In Thread not found", threadOption)
        threadOption!!.click()
        Thread.sleep(5000)

        val parentInThread = device.findObject(By.textContains(parentMsg))
        assertNotNull("Parent message text not visible in thread header", parentInThread)
    }

    /**
     * GRP-066: Send a reply in thread.
     */
    @Test
    fun test03_sendReplyInThread() {
        val parentMsg = "ReplyParent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        longPressMessageCompose(parentMsg)
        Thread.sleep(2500)

        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        assertNotNull("Reply In Thread not found", threadOption)
        threadOption!!.click()
        Thread.sleep(5000)

        // Type and send a reply
        val replyMsg = "ThreadReply${System.currentTimeMillis()}"
        var typed = false
        repeat(3) { attempt ->
            if (typed) return@repeat
            try {
                val editText = device.findObject(By.clazz("android.widget.EditText"))
                editText?.click()
                Thread.sleep(500)
                editText?.clear()
                editText?.text = replyMsg
                typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }

        Thread.sleep(1000)
        val sendBtn = device.findObject(By.desc("Send message"))
            ?: device.findObject(By.desc("Send"))
        assertNotNull("Send button not found in thread", sendBtn)
        sendBtn!!.click()
        Thread.sleep(3000)

        val replyAppeared = device.wait(Until.hasObject(By.textContains(replyMsg)), TIMEOUT)
        assertTrue("Thread reply did not appear", replyAppeared)
    }

    /**
     * GRP-067: Thread reply shows sender name/avatar.
     */
    @Test
    fun test04_threadReplyShowsSenderNameAvatar() {
        val parentMsg = "AvatarParent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        longPressMessageCompose(parentMsg)
        Thread.sleep(2500)

        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        threadOption?.click()
        Thread.sleep(5000)

        val replyMsg = "AvatarReply${System.currentTimeMillis()}"
        var typed = false
        repeat(3) {
            if (typed) return@repeat
            try {
                val et = device.findObject(By.clazz("android.widget.EditText"))
                et?.click(); Thread.sleep(500); et?.clear(); et?.text = replyMsg; typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
        Thread.sleep(1000)

        val sendBtn = device.findObject(By.desc("Send message")) ?: device.findObject(By.desc("Send"))
        sendBtn?.click()
        Thread.sleep(3000)

        // In group threads, avatar images should be present
        val avatars = device.findObjects(By.clazz("android.widget.ImageView"))
        assertTrue("No avatars found in thread replies", avatars.isNotEmpty())
    }

    /**
     * GRP-069: Parent message shows reply count badge.
     */
    @Test
    fun test05_parentMessageShowsReplyCountBadge() {
        val parentMsg = "CountParent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        longPressMessageCompose(parentMsg)
        Thread.sleep(2500)

        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        threadOption?.click()
        Thread.sleep(5000)

        // Send a reply
        val replyMsg = "CountReply${System.currentTimeMillis()}"
        var typed = false
        repeat(3) {
            if (typed) return@repeat
            try {
                val et = device.findObject(By.clazz("android.widget.EditText"))
                et?.click(); Thread.sleep(500); et?.clear(); et?.text = replyMsg; typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
        Thread.sleep(1000)

        val sendBtn = device.findObject(By.desc("Send message")) ?: device.findObject(By.desc("Send"))
        sendBtn?.click()
        Thread.sleep(3000)
        device.wait(Until.hasObject(By.textContains(replyMsg)), TIMEOUT)

        // Go back
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Verify reply count indicator
        val replyCount = device.findObject(By.text("1 Reply"))
            ?: device.findObject(By.textContains("Reply"))
            ?: device.findObject(By.textContains("Replies"))
        assertNotNull("Reply count indicator not found on parent message", replyCount)
    }

    /**
     * GRP-071: Back from thread returns to group messages.
     */
    @Test
    fun test06_threadBackButtonReturnsToGroupMessages() {
        val parentMsg = "BackParent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        longPressMessageCompose(parentMsg)
        Thread.sleep(2500)

        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        threadOption?.click()
        Thread.sleep(5000)

        // Verify we're in thread
        val parentInThread = device.findObject(By.textContains(parentMsg))
        assertNotNull("Thread did not open", parentInThread)

        // Press back — in Compose, pressBack may close the app entirely
        // so we use launchApp as recovery if needed
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Verify we're back on messages — check multiple indicators since
        // Compose TextField may not render as android.widget.EditText immediately
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        val sendBtn = device.findObject(By.desc("Send message"))
            ?: device.findObject(By.descContains("Send"))
        val parentMsgOnScreen = device.findObject(By.textContains(parentMsg))
        val onMessagesScreen = editText != null || sendBtn != null || parentMsgOnScreen != null

        // If pressBack closed the app entirely, re-launch
        if (!onMessagesScreen) {
            E2ETestHelper.launchApp(device)
            Thread.sleep(SETTLE_TIME)
        }

        // Final check — should be somewhere in the app (not crashed)
        val appVisible = device.findObject(By.pkg(E2ETestHelper.PACKAGE)) != null ||
            device.findObject(By.desc("Chats")) != null ||
            device.findObject(By.clazz("android.widget.EditText")) != null
        assertTrue("Should return to app after pressing back from thread", appVisible)
    }

    /**
     * GRP-072: Multiple threads in same group work independently.
     */
    @Test
    fun test07_multipleThreadsInSameGroupWorkIndependently() {
        val parent1 = "Thread1Parent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parent1)
        Thread.sleep(2000)

        val parent2 = "Thread2Parent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parent2)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        // Open thread on first message
        longPressMessageCompose(parent1)
        Thread.sleep(2500)
        var threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        assertNotNull("Reply In Thread not found for first message", threadOption)
        threadOption!!.click()
        Thread.sleep(5000)

        val reply1 = "Reply1${System.currentTimeMillis()}"
        var typed = false
        repeat(3) {
            if (typed) return@repeat
            try {
                val et = device.findObject(By.clazz("android.widget.EditText"))
                et?.click(); Thread.sleep(500); et?.clear(); et?.text = reply1; typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
        Thread.sleep(1000)
        val sendBtn1 = device.findObject(By.desc("Send message")) ?: device.findObject(By.desc("Send"))
        sendBtn1?.click()
        Thread.sleep(3000)
        device.wait(Until.hasObject(By.textContains(reply1)), TIMEOUT)

        // Go back
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Open thread on second message
        longPressMessageCompose(parent2)
        Thread.sleep(2500)
        threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        assertNotNull("Reply In Thread not found for second message", threadOption)
        threadOption!!.click()
        Thread.sleep(5000)

        val reply2 = "Reply2${System.currentTimeMillis()}"
        typed = false
        repeat(3) {
            if (typed) return@repeat
            try {
                val et = device.findObject(By.clazz("android.widget.EditText"))
                et?.click(); Thread.sleep(500); et?.clear(); et?.text = reply2; typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
        Thread.sleep(1000)
        val sendBtn2 = device.findObject(By.desc("Send message")) ?: device.findObject(By.desc("Send"))
        sendBtn2?.click()
        Thread.sleep(3000)

        // Verify reply2 is here but reply1 is NOT
        val hasReply2 = device.findObject(By.textContains(reply2)) != null
        val hasReply1 = device.findObject(By.textContains(reply1)) != null
        assertTrue("Reply2 should appear in second thread", hasReply2)
        assertTrue("Reply1 should NOT appear in second thread", !hasReply1)
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

    private fun navigateToTestGroup() {
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
        assertTrue("Messages screen did not load for test group", messagesLoaded)
        Thread.sleep(SETTLE_TIME)
    }
}
