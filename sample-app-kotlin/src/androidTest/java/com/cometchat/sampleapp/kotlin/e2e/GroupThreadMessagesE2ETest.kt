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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * E2E tests for Thread Messages in a group conversation.
 *
 * Setup: Creates a test group via SDK with a second member.
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
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.GroupThreadMessagesE2ETest
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
     * GRP-064: Open thread from a group message via long-press -> Reply In Thread.
     */
    @Test
    fun test01_openThreadFromGroupMessage() {
        // Send a parent message
        val parentMsg = "ThreadParent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        // Long-press to open popup
        E2ETestHelper.longPressMessage(device, parentMsg)
        Thread.sleep(2500)

        // Tap "Reply In Thread"
        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
            ?: device.findObject(By.textContains("Thread"))
        assertNotNull("Reply In Thread option not found in popup", threadOption)
        threadOption!!.click()
        Thread.sleep(3000)

        // Verify thread view opened (threadHeader present)
        val threadHeader = device.wait(Until.findObject(By.res(PACKAGE, "threadHeader")), TIMEOUT)
        assertNotNull("Thread view did not open (threadHeader not found)", threadHeader)
    }

    /**
     * GRP-065: Thread header shows the parent message text.
     */
    @Test
    fun test02_threadHeaderShowsParentMessage() {
        val parentMsg = "ParentVisible${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        E2ETestHelper.longPressMessage(device, parentMsg)
        Thread.sleep(2500)

        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        assertNotNull("Reply In Thread option not found", threadOption)
        threadOption!!.click()
        Thread.sleep(3000)

        val threadHeader = device.wait(Until.findObject(By.res(PACKAGE, "threadHeader")), TIMEOUT)
        assertNotNull("Thread view did not open", threadHeader)

        // Verify parent message text is visible in the thread header
        val parentInHeader = device.findObject(By.textContains(parentMsg))
        assertNotNull("Parent message text not visible in thread header", parentInHeader)
    }

    /**
     * GRP-066: Send a reply in a thread and verify it appears.
     */
    @Test
    fun test03_sendReplyInThread() {
        val parentMsg = "ReplyParent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        E2ETestHelper.longPressMessage(device, parentMsg)
        Thread.sleep(2500)

        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        assertNotNull("Reply In Thread option not found", threadOption)
        threadOption!!.click()
        Thread.sleep(3000)

        val threadHeader = device.wait(Until.findObject(By.res(PACKAGE, "threadHeader")), TIMEOUT)
        assertNotNull("Thread view did not open", threadHeader)

        // Type and send a reply in the thread
        val replyMsg = "ThreadReply${System.currentTimeMillis()}"
        val threadComposer = device.findObject(By.res(PACKAGE, "messageComposer"))
        val editText = threadComposer?.findObject(By.clazz("android.widget.EditText"))
            ?: device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("EditText not found in thread composer", editText)
        editText!!.clear()
        editText.text = replyMsg
        Thread.sleep(1000)

        val sendBtn = device.findObject(By.desc("Send"))
            ?: device.findObject(By.descContains("Send"))
        assertNotNull("Send button not found in thread", sendBtn)
        sendBtn!!.click()
        Thread.sleep(3000)

        // Verify the reply appears
        val replyAppeared = device.wait(Until.hasObject(By.textContains(replyMsg)), TIMEOUT)
        assertTrue("Thread reply did not appear in thread message list", replyAppeared)
    }

    /**
     * GRP-067: Thread reply shows sender name/avatar (group context).
     */
    @Test
    fun test04_threadReplyShowsSenderNameAvatar() {
        val parentMsg = "AvatarParent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        E2ETestHelper.longPressMessage(device, parentMsg)
        Thread.sleep(2500)

        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        assertNotNull("Reply In Thread option not found", threadOption)
        threadOption!!.click()
        Thread.sleep(3000)

        device.wait(Until.findObject(By.res(PACKAGE, "threadHeader")), TIMEOUT)

        // Send a reply
        val replyMsg = "AvatarReply${System.currentTimeMillis()}"
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        editText?.clear()
        editText?.text = replyMsg
        Thread.sleep(1000)

        val sendBtn = device.findObject(By.desc("Send"))
        sendBtn?.click()
        Thread.sleep(3000)

        // Verify the reply has avatar (ImageView) in the thread message list
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Message list not found in thread view", messageList)

        // In group thread replies, each message should show sender info
        // Verify at least one ImageView (avatar) exists in the message area
        val avatars = messageList!!.findObjects(By.clazz("android.widget.ImageView"))
        assertTrue("No avatars found in thread replies (sender info not shown)", avatars.isNotEmpty())
    }

    /**
     * GRP-069: Parent message shows reply count badge after thread reply.
     */
    @Test
    fun test05_parentMessageShowsReplyCountBadge() {
        val parentMsg = "CountParent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        E2ETestHelper.longPressMessage(device, parentMsg)
        Thread.sleep(2500)

        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        assertNotNull("Reply In Thread option not found", threadOption)
        threadOption!!.click()
        Thread.sleep(3000)

        device.wait(Until.findObject(By.res(PACKAGE, "threadHeader")), TIMEOUT)

        // Send a reply
        val replyMsg = "CountReply${System.currentTimeMillis()}"
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        editText?.clear()
        editText?.text = replyMsg
        Thread.sleep(1000)

        val sendBtn = device.findObject(By.desc("Send"))
        sendBtn?.click()
        Thread.sleep(3000)

        // Verify reply appeared
        device.wait(Until.hasObject(By.textContains(replyMsg)), TIMEOUT)

        // Go back to group messages
        val backBtn = device.findObject(By.res(PACKAGE, "toolbar"))
            ?.findObject(By.clickable(true))
            ?: device.findObject(By.descContains("Back"))
            ?: device.findObject(By.descContains("Navigate up"))
        if (backBtn != null) backBtn.click() else device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Verify we're back on messages screen
        val messageList = device.wait(Until.findObject(By.res(PACKAGE, "messageList")), TIMEOUT)
        assertNotNull("Did not return to group messages", messageList)

        // Verify reply count indicator
        val replyCount = device.findObject(By.text("1 Reply"))
            ?: device.findObject(By.textContains("Reply"))
            ?: device.findObject(By.textContains("Replies"))
        assertNotNull("Reply count indicator not found on parent message", replyCount)
    }

    /**
     * GRP-071: Back button from thread returns to group messages.
     */
    @Test
    fun test06_threadBackButtonReturnsToGroupMessages() {
        val parentMsg = "BackParent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parentMsg)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        E2ETestHelper.longPressMessage(device, parentMsg)
        Thread.sleep(2500)

        val threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        assertNotNull("Reply In Thread option not found", threadOption)
        threadOption!!.click()
        Thread.sleep(3000)

        val threadHeader = device.wait(Until.findObject(By.res(PACKAGE, "threadHeader")), TIMEOUT)
        assertNotNull("Thread view did not open", threadHeader)

        // Press back
        val backBtn = device.findObject(By.res(PACKAGE, "toolbar"))
            ?.findObject(By.clickable(true))
            ?: device.findObject(By.descContains("Back"))
            ?: device.findObject(By.descContains("Navigate up"))
        if (backBtn != null) backBtn.click() else device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Verify we're back on group messages screen
        val messageList = device.wait(Until.findObject(By.res(PACKAGE, "messageList")), TIMEOUT)
        assertNotNull("Did not return to group messages after pressing back from thread", messageList)

        // Verify the thread header is gone
        val threadHeaderGone = device.findObject(By.res(PACKAGE, "threadHeader"))
        assertTrue("Should not be in thread view anymore", threadHeaderGone == null)
    }

    /**
     * GRP-072: Multiple threads in same group work independently.
     */
    @Test
    fun test07_multipleThreadsInSameGroupWorkIndependently() {
        // Send two parent messages
        val parent1 = "Thread1Parent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parent1)
        Thread.sleep(2000)

        val parent2 = "Thread2Parent${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, parent2)
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        // Open thread on first message and send a reply
        E2ETestHelper.longPressMessage(device, parent1)
        Thread.sleep(2500)
        var threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        assertNotNull("Reply In Thread not found for first message", threadOption)
        threadOption!!.click()
        Thread.sleep(3000)
        device.wait(Until.findObject(By.res(PACKAGE, "threadHeader")), TIMEOUT)

        val reply1 = "Reply1${System.currentTimeMillis()}"
        var editText = device.findObject(By.clazz("android.widget.EditText"))
        editText?.clear()
        editText?.text = reply1
        Thread.sleep(1000)
        device.findObject(By.desc("Send"))?.click()
        Thread.sleep(3000)
        device.wait(Until.hasObject(By.textContains(reply1)), TIMEOUT)

        // Go back
        val backBtn = device.findObject(By.descContains("Back"))
            ?: device.findObject(By.descContains("Navigate up"))
        if (backBtn != null) backBtn.click() else device.pressBack()
        Thread.sleep(SETTLE_TIME)
        device.wait(Until.findObject(By.res(PACKAGE, "messageList")), TIMEOUT)

        // Open thread on second message and send a different reply
        E2ETestHelper.longPressMessage(device, parent2)
        Thread.sleep(2500)
        threadOption = device.findObject(By.text("Reply In Thread"))
            ?: device.findObject(By.textContains("Reply In Thread"))
        assertNotNull("Reply In Thread not found for second message", threadOption)
        threadOption!!.click()
        Thread.sleep(3000)
        device.wait(Until.findObject(By.res(PACKAGE, "threadHeader")), TIMEOUT)

        val reply2 = "Reply2${System.currentTimeMillis()}"
        editText = device.findObject(By.clazz("android.widget.EditText"))
        editText?.clear()
        editText?.text = reply2
        Thread.sleep(1000)
        device.findObject(By.desc("Send"))?.click()
        Thread.sleep(3000)

        // Verify reply2 is here but reply1 is NOT (they are independent threads)
        val hasReply2 = device.findObject(By.textContains(reply2)) != null
        val hasReply1 = device.findObject(By.textContains(reply1)) != null
        assertTrue("Reply2 should appear in second thread", hasReply2)
        assertTrue("Reply1 should NOT appear in second thread (threads are independent)", !hasReply1)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun navigateToTestGroup() {
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        // Robust open: RecyclerView-scoped row click + retry + waits for the composer. The old
        // text-node click was flaky (sometimes hit the search field), which left later tests with
        // "Message composer not found".
        E2ETestHelper.openGroupByName(device, testGroupName)
    }
}
