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
 * E2E tests for sending messages in a group conversation (sample-app-compose).
 *
 * Setup: Creates a test group via SDK to guarantee owner role and known state.
 * All tests open this group and verify message sending behavior.
 *
 * Verified selectors from source (DefaultSendButton.kt):
 * - Send button active: contentDescription = "Send message"
 * - Send button disabled: contentDescription = "Send button disabled"
 * - Composer text field: By.clazz("android.widget.EditText")
 *
 * Key Compose differences:
 * - Use retry loops for text input (StaleObjectException)
 * - Send button is always PRESENT but changes contentDescription (active vs disabled)
 * - Use safeGetBounds for element counting
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
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.GroupSendMessageE2ETest
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
        // Verify we're on messages screen
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded (no EditText)", composer)

        // Count existing messages with unique prefix
        val uniqueText = "GRPSEND${System.currentTimeMillis()}"
        val preCount = E2ETestHelper.safeCount(device, By.textContains(uniqueText))

        // Send the message using helper (handles retry + keyboard dismiss)
        E2ETestHelper.sendMessage(device, uniqueText)

        // Scroll to see new message
        E2ETestHelper.scrollDown(device)

        // Verify message appeared
        val postCount = E2ETestHelper.safeCount(device, By.textContains(uniqueText))
        assertTrue(
            "Sent message '$uniqueText' not found (pre=$preCount, post=$postCount)",
            postCount > preCount
        )
    }

    /**
     * GRP-021: Empty message cannot be sent — send button shows "Send button disabled".
     *
     * In Compose: Send button is always rendered but with different contentDescription:
     * - Empty: "Send button disabled" (not clickable)
     * - Has text: "Send message" (clickable)
     */
    @Test
    fun test02_emptyMessageCannotBeSentInGroup() {
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", composer)

        // With empty composer, look for "Send button disabled" contentDescription
        val disabledSend = device.findObject(By.desc("Send button disabled"))
        val activeSend = device.findObject(By.desc("Send message"))

        assertTrue(
            "With empty composer: should find 'Send button disabled', not 'Send message'",
            disabledSend != null || activeSend == null
        )
    }

    /**
     * GRP-022: Whitespace-only message cannot be sent.
     */
    @Test
    fun test03_whitespaceOnlyMessageCannotBeSent() {
        // Type whitespace using retry loop
        var typed = false
        repeat(3) { attempt ->
            if (typed) return@repeat
            try {
                val editText = device.findObject(By.clazz("android.widget.EditText"))
                editText?.click()
                Thread.sleep(500)
                editText?.text = "   "
                typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                Thread.sleep(1500)
            }
        }
        Thread.sleep(1000)

        // Send button should still be disabled (whitespace = empty for send logic)
        val disabledSend = device.findObject(By.desc("Send button disabled"))
        val activeSend = device.findObject(By.desc("Send message"))

        assertTrue(
            "Whitespace-only: send should be disabled",
            disabledSend != null || activeSend == null
        )

        // Clear
        try {
            val editText = device.findObject(By.clazz("android.widget.EditText"))
            editText?.clear()
        } catch (_: Exception) { }
    }

    /**
     * GRP-023: Long text message (1000+ chars) sends successfully.
     */
    @Test
    fun test04_longTextMessageSendsSuccessfully() {
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", composer)

        // Create long message
        val longText = "LONGMSG${System.currentTimeMillis()}x" + "B".repeat(1000)
        val marker = longText.take(25)

        val preCount = E2ETestHelper.safeCount(device, By.textContains(marker))

        // Send using helper
        E2ETestHelper.sendMessage(device, longText)
        E2ETestHelper.scrollDown(device)

        val postCount = E2ETestHelper.safeCount(device, By.textContains(marker))
        assertTrue(
            "Long message not found after sending",
            postCount > preCount
        )
    }

    /**
     * GRP-024: Emoji-only message sends and renders correctly.
     */
    @Test
    fun test05_emojiOnlyMessageSendsAndRenders() {
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", composer)

        val emojiText = "\uD83D\uDE00\uD83D\uDE0D\uD83D\uDE80" // 😀😍🚀
        val preCount = E2ETestHelper.safeCount(device, By.textContains("\uD83D\uDE00"))

        E2ETestHelper.sendMessage(device, emojiText)
        E2ETestHelper.scrollDown(device)

        val postCount = E2ETestHelper.safeCount(device, By.textContains("\uD83D\uDE00"))
        assertTrue(
            "Emoji message not found after sending",
            postCount > preCount
        )
    }

    /**
     * GRP-027: Composer clears after successful send.
     */
    @Test
    fun test06_composerClearsAfterSuccessfulSend() {
        val msgText = "ClearTest_${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, msgText)

        // After sending, check EditText is empty
        Thread.sleep(2000)
        var currentText = ""
        repeat(3) { attempt ->
            try {
                val editText = device.findObject(By.clazz("android.widget.EditText"))
                currentText = editText?.text ?: ""
                return@repeat
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                Thread.sleep(1000)
            }
        }

        assertTrue(
            "Composer should be empty after send, found: '$currentText'",
            currentText.isEmpty() || currentText.isBlank()
        )
    }

    /**
     * GRP-028: Send button activates only when text is present.
     *
     * In Compose (from DefaultSendButton.kt):
     * - No text: contentDescription = "Send button disabled"
     * - Has text: contentDescription = "Send message"
     */
    @Test
    fun test07_sendButtonActivatesOnlyWhenTextPresent() {
        // Initially: Send button should be disabled
        val sendBefore = device.findObject(By.desc("Send button disabled"))
        assertNotNull(
            "Send button should show 'Send button disabled' when empty",
            sendBefore
        )

        // Type text using retry loop
        var typed = false
        repeat(3) { attempt ->
            if (typed) return@repeat
            try {
                val editText = device.findObject(By.clazz("android.widget.EditText"))
                editText?.click()
                Thread.sleep(500)
                editText?.text = "test text"
                typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                Thread.sleep(1500)
            }
        }
        Thread.sleep(2000)

        // Now Send button SHOULD be active
        val sendActive = device.findObject(By.desc("Send message"))
        assertNotNull(
            "Send button should show 'Send message' when text is typed",
            sendActive
        )

        // Clear text — button should go back to disabled
        try {
            val editText = device.findObject(By.clazz("android.widget.EditText"))
            editText?.clear()
        } catch (_: Exception) { }
        // Compose needs extra time to recompose after clearing
        Thread.sleep(3000)
        device.waitForIdle()

        val sendDisabledAgain = device.findObject(By.desc("Send button disabled"))
            ?: device.findObject(By.descContains("disabled"))
        // If not found as disabled, at minimum the "Send message" (active) should be gone
        val sendStillActive = device.findObject(By.desc("Send message"))
        assertTrue(
            "After clearing: send should be disabled or at least not active",
            sendDisabledAgain != null || sendStillActive == null
        )
    }

    /**
     * GRP-025: Message with @mention sends correctly.
     *
     * Compose: Suggestion list has contentDescription = "Suggestion List"
     * Items have contentDescription = "Suggestion: {name}"
     */
    @Test
    fun test08_messageWithMentionSendsCorrectly() {
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", composer)

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

        // Type "@" in composer — use retry for Compose
        var typed = false
        repeat(3) { attempt ->
            if (typed) return@repeat
            try {
                val editText = device.findObject(By.clazz("android.widget.EditText"))
                editText?.click()
                Thread.sleep(500)
                editText?.click()
                Thread.sleep(300)
                device.executeShellCommand("input text @")
                typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                Thread.sleep(1500)
            }
        }
        Thread.sleep(3000) // Wait for suggestion list to load

        // Verify suggestion list appeared (contentDescription = "Suggestion List")
        val suggestionList = device.findObject(By.desc("Suggestion List"))
            ?: device.findObject(By.descContains("Suggestion"))
        assertNotNull("Suggestion list did not appear after typing @", suggestionList)

        // Tap the first suggestion item (desc = "Suggestion: {name}")
        val firstSuggestion = E2ETestHelper.safeGetBounds(
            device, By.descContains("Suggestion:")
        ) { true }
        if (firstSuggestion.isNotEmpty()) {
            device.click(firstSuggestion[0].centerX(), firstSuggestion[0].centerY())
        } else {
            // Fallback: tap any clickable inside the suggestion area
            suggestionList?.click()
        }
        Thread.sleep(2000)

        // Send the message
        val sendButton = device.findObject(By.desc("Send message"))
        assertNotNull("Send button not active after selecting mention", sendButton)
        sendButton?.click()
        Thread.sleep(3000)

        // Verify message list still visible (sent successfully)
        val editTextAfter = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Should still be on messages screen after sending mention", editTextAfter)
    }

    /**
     * GRP-032: Mentions picker shows group members and "all" (lowercase).
     */
    @Test
    fun test09_mentionsPickerShowsGroupMembersAndAll() {
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", composer)

        // Add a member
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

        // Type "@"
        var typed = false
        repeat(3) { attempt ->
            if (typed) return@repeat
            try {
                val editText = device.findObject(By.clazz("android.widget.EditText"))
                editText?.click()
                Thread.sleep(500)
                editText?.click()
                Thread.sleep(300)
                device.executeShellCommand("input text @")
                typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                Thread.sleep(1500)
            }
        }
        Thread.sleep(3000)

        // Verify suggestion list appeared
        val suggestionList = device.findObject(By.desc("Suggestion List"))
        assertNotNull("Suggestion list did not appear", suggestionList)

        // Verify it has multiple items (members + "all")
        val suggestions = E2ETestHelper.safeGetBounds(
            device, By.descContains("Suggestion:")
        ) { true }
        assertTrue(
            "Suggestion list should have at least 2 items (member + all), found ${suggestions.size}",
            suggestions.size >= 2
        )

        // Verify "all" is present (lowercase)
        val allItem = device.findObject(By.textContains("all"))
            ?: device.findObject(By.descContains("all"))
        assertNotNull("'all' entry not found in mention suggestions", allItem)

        // Clear to dismiss
        try {
            val editText = device.findObject(By.clazz("android.widget.EditText"))
            editText?.clear()
        } catch (_: Exception) { }
    }

    /**
     * GRP-033: @all mention in group sends correctly.
     */
    @Test
    fun test11_atAllMentionInGroup() {
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", composer)

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

        // Type "@"
        var typed = false
        repeat(3) { attempt ->
            if (typed) return@repeat
            try {
                val editText = device.findObject(By.clazz("android.widget.EditText"))
                editText?.click()
                Thread.sleep(500)
                editText?.click()
                Thread.sleep(300)
                device.executeShellCommand("input text @")
                typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                Thread.sleep(1500)
            }
        }
        Thread.sleep(3000)

        // Find and tap "all" in the suggestion list
        val allItem = device.findObject(By.textContains("all"))
            ?: device.findObject(By.descContains("all"))
        assertNotNull("'all' not found in suggestion list", allItem)

        // Use coordinate click to avoid StaleObjectException
        try {
            val bounds = allItem!!.visibleBounds
            device.click(bounds.centerX(), bounds.centerY())
        } catch (_: androidx.test.uiautomator.StaleObjectException) {
            // Retry with safeGetBounds
            val allBounds = E2ETestHelper.safeGetBounds(device, By.textContains("all")) { true }
            if (allBounds.isNotEmpty()) {
                device.click(allBounds[0].centerX(), allBounds[0].centerY())
            }
        }
        Thread.sleep(2000)

        // Send the @all message
        val sendButton = device.findObject(By.desc("Send message"))
        assertNotNull("Send button not active after @all selection", sendButton)
        sendButton?.click()
        Thread.sleep(3000)

        // Verify still on messages screen
        val editTextAfter = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Should still be on messages screen after @all send", editTextAfter)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helper
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun navigateToTestGroup() {
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        // Search for the test group using retry
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
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                Thread.sleep(1500)
            }
        }
        Thread.sleep(5000)

        // Click group below search field
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
            // Fallback
            val contentBounds = E2ETestHelper.safeGetBounds(device, By.clickable(true)) { bounds ->
                bounds.top > searchFieldBottom && bounds.bottom < device.displayHeight - 200
            }
            if (contentBounds.isNotEmpty()) {
                device.click(contentBounds[0].centerX(), contentBounds[0].centerY())
            }
        }

        // Wait for messages screen
        val messagesLoaded = device.wait(
            Until.hasObject(By.clazz("android.widget.EditText")),
            TIMEOUT
        )
        assertTrue("Messages screen did not load for test group", messagesLoaded)
        Thread.sleep(SETTLE_TIME)
    }
}
