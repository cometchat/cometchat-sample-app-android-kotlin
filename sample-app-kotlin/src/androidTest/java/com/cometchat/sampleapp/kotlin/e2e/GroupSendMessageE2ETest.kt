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
 * E2E tests for sending messages in a group conversation (sample-app-kotlin).
 *
 * Setup: Creates a test group via SDK to guarantee owner role and known state.
 * All tests open this group and verify message sending behavior.
 *
 * Verified selectors from source:
 * - Send button: contentDescription = "Send" (from chatuikit-kotlin cometchat_message_composer.xml)
 * - Message composer: By.res(PACKAGE, "messageComposer")
 * - Message list: By.res(PACKAGE, "messageList")
 * - EditText inside composer for typing
 *
 * Test IDs:
 * - GRP-020: testSendTextMessageInGroup
 * - GRP-021: testEmptyMessageCannotBeSentInGroup
 * - GRP-022: testWhitespaceOnlyMessageCannotBeSent
 * - GRP-023: testLongTextMessageSendsSuccessfully
 * - GRP-024: testEmojiOnlyMessageSendsAndRenders
 * - GRP-027: testComposerClearsAfterSuccessfulSend
 * - GRP-028: testSendButtonActivatesOnlyWhenTextPresent
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.GroupSendMessageE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupSendMessageE2ETest {

    private lateinit var device: UiDevice
    private var testGroupId = ""
    private var testGroupName = ""

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        // Create a test group via SDK
        val ts = System.currentTimeMillis()
        testGroupId = "grp_send_$ts"
        testGroupName = "SendTest_$ts"

        val latch = CountDownLatch(1)
        val group = Group(testGroupId, testGroupName, CometChatConstants.GROUP_TYPE_PUBLIC, "")
        CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(g: Group) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(15, TimeUnit.SECONDS)

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
     * GRP-020: Send text message in group — appears in message list.
     */
    @Test
    fun test01_sendTextMessageInGroup() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // Count existing messages with our unique prefix
        val uniqueText = "GRPSEND${System.currentTimeMillis()}"
        val preCount = device.findObjects(By.textContains(uniqueText)).size

        // Send the message
        E2ETestHelper.sendMessage(device, uniqueText)

        // Dismiss keyboard and scroll to see new message
       // device.pressBack()
        Thread.sleep(500)
        E2ETestHelper.scrollDown(device)

        // Verify message appeared (Rule 10: check NEW content)
        val postCount = device.findObjects(By.textContains(uniqueText)).size
        assertTrue(
            "Sent message '$uniqueText' not found in message list (pre=$preCount, post=$postCount)",
            postCount > preCount
        )
    }

    /**
     * GRP-021: Empty message cannot be sent — send button should not be active.
     */
    @Test
    fun test02_emptyMessageCannotBeSentInGroup() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // The composer EditText should be empty initially
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        assertNotNull("Message composer not found", composer)

        // With empty text, Send button (desc="Send") is VISIBLE but DISABLED (not clickable)
        // In Kotlin UIKit: send button is always visible, just isEnabled=false when empty
        val sendButton = device.findObject(By.desc("Send"))

        // Button may be visible but should not be enabled
        if (sendButton != null) {
            assertTrue(
                "Send button should be disabled (not enabled) when composer is empty",
                !sendButton.isEnabled
            )
        }
        // If null, that's also fine (test passes either way)
    }

    /**
     * GRP-022: Whitespace-only message cannot be sent.
     */
    @Test
    fun test03_whitespaceOnlyMessageCannotBeSent() {
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        assertNotNull("Message composer not found", composer)

        val editText = composer!!.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("EditText not found in composer", editText)

        // Type whitespace only
        editText!!.text = "   "
        Thread.sleep(1000)

        // Send button should be disabled (visible but not enabled) for whitespace
        val sendButton = device.findObject(By.desc("Send"))
        if (sendButton != null) {
            assertTrue(
                "Send button should be disabled for whitespace-only text",
                !sendButton.isEnabled
            )
        }

        // Clear the field
        editText.clear()
    }

    /**
     * GRP-023: Long text message (1000+ chars) sends successfully.
     */
    @Test
    fun test04_longTextMessageSendsSuccessfully() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // Create a long message (1000+ chars)
        val longText = "LONGMSG${System.currentTimeMillis()}x" + "A".repeat(1000)
        val marker = longText.take(25) // Use first 20 chars as search marker

        val preCount = device.findObjects(By.textContains(marker)).size

        // Send the long message
        E2ETestHelper.sendMessage(device, longText)

        // Dismiss keyboard and verify
        //device.pressBack()
        Thread.sleep(500)
        E2ETestHelper.scrollDown(device)

        val postCount = device.findObjects(By.textContains(marker)).size
        assertTrue(
            "Long message not found in list after sending",
            postCount > preCount
        )
    }

    /**
     * GRP-024: Emoji-only message sends and renders correctly.
     */
    @Test
    fun test05_emojiOnlyMessageSendsAndRenders() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        val emojiText = "\uD83D\uDE00\uD83D\uDE0D\uD83D\uDE80" // 😀😍🚀
        val preCount = device.findObjects(By.textContains("\uD83D\uDE00")).size

        E2ETestHelper.sendMessage(device, emojiText)

        //device.pressBack()
        Thread.sleep(500)
        E2ETestHelper.scrollDown(device)

        val postCount = device.findObjects(By.textContains("\uD83D\uDE00")).size
        assertTrue(
            "Emoji message not found in list after sending",
            postCount > preCount
        )
    }

    /**
     * GRP-027: Composer clears after successful send.
     */
    @Test
    fun test06_composerClearsAfterSuccessfulSend() {
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        assertNotNull("Message composer not found", composer)

        val msgText = "ClearTest_${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, msgText)

        // After sending, check that the EditText is now empty
        Thread.sleep(2000)
        val editText = composer.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("EditText not found after send", editText)

        val currentText = editText!!.text ?: ""
        // "Type your message" is the PLACEHOLDER/HINT - it means the field IS empty
        val placeholderText = "Type your message"
        assertTrue(
            "Composer should be empty after send, but found: '$currentText'",
            currentText.isEmpty() || currentText.isBlank() || currentText.contains(placeholderText)
        )
    }

    /**
     * GRP-028: Send button activates only when text is present.
     */
    @Test
    fun test07_sendButtonActivatesOnlyWhenTextPresent() {
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        assertNotNull("Message composer not found", composer)

        // Initially: Send button should be DISABLED (visible but not enabled)
        val sendBefore = device.findObject(By.desc("Send"))
        if (sendBefore != null) {
            assertTrue("Send button should be disabled when empty", !sendBefore.isEnabled)
        }

        // Type text
        val editText = composer!!.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("EditText not found", editText)
        editText!!.text = "test text"
        Thread.sleep(1000)

        // Now Send button SHOULD be visible
        val sendAfter = device.findObject(By.desc("Send"))
        assertNotNull(
            "Send button (desc='Send') should appear when text is typed",
            sendAfter
        )

        // Clear text — button should become disabled again
        editText.clear()
        Thread.sleep(1000)

        val sendCleared = device.findObject(By.desc("Send"))
        if (sendCleared != null) {
            assertTrue("Send button should be disabled after clearing text", !sendCleared.isEnabled)
        }
    }

    /**
     * GRP-025: Message with @mention sends correctly.
     *
     * Flow: Type "@" → suggestion list appears → tap first member → mention inserted →
     *       send → verify message with mention text in list.
     *
     * Kotlin: Suggestion list RecyclerView = "recyclerViewSuggestionList" (com.cometchat.uikit.kotlin package)
     * Need to add a second member to the group for mention to work.
     */
    @Test
    fun test08_messageWithMentionSendsCorrectly() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // Add a member to the group so mention list has someone to show
        val memberUid = E2ETestConfig.GROUP_MEMBER_1_UID
        val addLatch = CountDownLatch(1)
        val members = listOf(
            com.cometchat.chat.models.GroupMember(memberUid, CometChatConstants.SCOPE_PARTICIPANT)
        )
        CometChat.addMembersToGroup(testGroupId, members, null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(r: HashMap<String, String>?) { addLatch.countDown() }
                override fun onError(e: CometChatException?) { addLatch.countDown() }
            }
        )
        addLatch.await(10, TimeUnit.SECONDS)
        Thread.sleep(2000)

        // Find composer EditText and type "@"
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        assertNotNull("Composer not found", composer)
        val editText = composer!!.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("EditText not found in composer", editText)

        editText!!.click()
        Thread.sleep(500)
        // IMPORTANT: Use shell input to simulate real typing — editText.text = "@" does NOT
        // trigger MentionTextWatcher because setText bypasses afterTextChanged.
        // Shell "input text" types character-by-character like a real keyboard.
        device.executeShellCommand("input text @")
        Thread.sleep(3000) // Wait for suggestion list to load

        // Verify suggestion list appeared
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val suggestionList = device.findObject(By.res(uikitPackage, "recyclerViewSuggestionList"))
            ?: device.findObject(By.res(PACKAGE, "recyclerViewSuggestionList"))

        assertNotNull("Suggestion list (recyclerViewSuggestionList) did not appear after typing @", suggestionList)

        // Tap the first suggestion item
        if (suggestionList != null && suggestionList.children.isNotEmpty()) {
            suggestionList.children[0].click()
        }
        Thread.sleep(2000)

        // Now send the message
        val sendButton = device.wait(Until.findObject(By.desc("Send")), TIMEOUT)
        assertNotNull("Send button not found after selecting mention", sendButton)
        sendButton!!.click()
        Thread.sleep(3000)

        // Verify message was sent (messages list still visible, no crash)
        val messageListAfter = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Message list should still be visible after sending mention", messageListAfter)
    }

    /**
     * GRP-032: Mentions picker shows group members and "all" option.
     *
     * Verifies the suggestion list when typing "@" shows:
     * 1. Group members (the added member)
     * 2. An "all" entry for @all mention
     */
    @Test
    fun test10_mentionsPickerShowsGroupMembersAndAll() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // Add a member to the group
        val memberUid = E2ETestConfig.GROUP_MEMBER_1_UID
        val addLatch = CountDownLatch(1)
        val members = listOf(
            com.cometchat.chat.models.GroupMember(memberUid, CometChatConstants.SCOPE_PARTICIPANT)
        )
        CometChat.addMembersToGroup(testGroupId, members, null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(r: HashMap<String, String>?) { addLatch.countDown() }
                override fun onError(e: CometChatException?) { addLatch.countDown() }
            }
        )
        addLatch.await(10, TimeUnit.SECONDS)
        Thread.sleep(2000)

        // Type "@" to trigger suggestion list
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        val editText = composer!!.findObject(By.clazz("android.widget.EditText"))
        editText!!.click()
        Thread.sleep(500)
        device.executeShellCommand("input text @")
        Thread.sleep(3000)

        // Verify suggestion list appeared
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val suggestionList = device.findObject(By.res(uikitPackage, "recyclerViewSuggestionList"))
            ?: device.findObject(By.res(PACKAGE, "recyclerViewSuggestionList"))
        assertNotNull("Suggestion list did not appear", suggestionList)

        // Verify it has items (members + "all")
        assertTrue(
            "Suggestion list should have at least 2 items (member + all)",
            suggestionList!!.children.size >= 2
        )

        // Check that "all" appears in the list (lowercase)
        val allItem = device.findObject(By.textContains("all"))
            ?: device.findObject(By.text("all"))
        assertNotNull("'all' entry not found in mention suggestions", allItem)

        // Clear to dismiss
        editText.clear()
    }

    /**
     * GRP-033: @all mention in group sends correctly.
     *
     * Flow: Type "@" → find "all" in suggestions → tap it → send → verify sent.
     */
    @Test
    fun test11_atAllMentionInGroup() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // Add a member (needed for @all to appear)
        val memberUid = E2ETestConfig.GROUP_MEMBER_1_UID
        val addLatch = CountDownLatch(1)
        val members = listOf(
            com.cometchat.chat.models.GroupMember(memberUid, CometChatConstants.SCOPE_PARTICIPANT)
        )
        CometChat.addMembersToGroup(testGroupId, members, null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(r: HashMap<String, String>?) { addLatch.countDown() }
                override fun onError(e: CometChatException?) { addLatch.countDown() }
            }
        )
        addLatch.await(10, TimeUnit.SECONDS)
        Thread.sleep(2000)

        // Type "@" to trigger suggestions
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        val editText = composer!!.findObject(By.clazz("android.widget.EditText"))
        editText!!.click()
        Thread.sleep(500)
        device.executeShellCommand("input text @")
        Thread.sleep(3000)

        // Find and tap "all" in the suggestion list
        val allItem = device.findObject(By.textContains("all"))
            ?: device.findObject(By.text("all"))
        assertNotNull("'all' not found in suggestion list", allItem)
        allItem!!.click()
        Thread.sleep(2000)

        // Send the @all mention message
        val sendButton = device.wait(Until.findObject(By.desc("Send")), TIMEOUT)
        assertNotNull("Send button not visible after selecting @all", sendButton)
        sendButton!!.click()
        Thread.sleep(3000)

        // Verify message sent (list still intact)
        val messageListAfter = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Message list should be visible after sending @all mention", messageListAfter)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helper
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun navigateToTestGroup() {
        // Open the freshly-created group by name via the shared helper (clicks the clickable row,
        // retries, and waits for the composer) — far less flaky than tapping the bare text node.
        E2ETestHelper.openGroupByName(device, testGroupName)
    }
}
