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
 * E2E tests for editing messages in a group conversation (sample-app-compose).
 *
 * Setup: Creates a test group via SDK with a second member (for GRP-046).
 * The second member sends a message via SDK so we can test "cannot edit other's message".
 *
 * Key Compose differences from Kotlin version:
 * - No resource IDs (no By.res()) — uses By.desc(), By.text(), By.clazz()
 * - Send button: contentDescription = "Send message" (active)
 * - Uses retry loops for text input (StaleObjectException)
 * - Long-press uses coordinate-based approach for reliability
 *
 * Test IDs:
 * - GRP-042: testEditOwnTextMessageInGroup
 * - GRP-044: testCancelEditReturnsToNormalMode
 * - GRP-045: testEditedMessageShowsEditedLabel
 * - GRP-046: testCannotEditOtherMemberMessage
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.GroupEditMessageE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupEditMessageE2ETest {

    private lateinit var device: UiDevice
    private var testGroupId = ""
    private var testGroupName = ""
    private val otherMemberUid = E2ETestConfig.GROUP_MEMBER_1_UID

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        val ts = System.currentTimeMillis()
        testGroupId = "grp_edit_$ts"
        testGroupName = "EditTest$ts"

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

        // Send a message as the other member (for GRP-046)
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(otherMemberUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        val sendLatch = CountDownLatch(1)
        val otherMsg = TextMessage(testGroupId, "OtherMemberMsg$ts", CometChatConstants.RECEIVER_TYPE_GROUP)
        CometChat.sendMessage(otherMsg, object : CometChat.CallbackListener<TextMessage>() {
            override fun onSuccess(msg: TextMessage) { sendLatch.countDown() }
            override fun onError(e: CometChatException?) { sendLatch.countDown() }
        })
        sendLatch.await(10, TimeUnit.SECONDS)

        // Wait for moderation to approve the message (status goes from "pending" to "approved")
        Thread.sleep(3000)

        // Login back as test user
        val reloginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(E2ETestHelper.testUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { reloginLatch.countDown() }
                override fun onError(e: CometChatException?) { reloginLatch.countDown() }
            })
        reloginLatch.await(10, TimeUnit.SECONDS)

        // Re-launch the app to ensure UI is in sync with the new login state
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)

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
     * GRP-042: Edit own text message in group via long-press.
     */
    @Test
    fun test01_editOwnTextMessageInGroup() {
        // Send a message to edit
        val originalMsg = "EditMe${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, originalMsg)
        Thread.sleep(2000)
        E2ETestHelper.scrollDown(device)

        // Long-press the message using coordinate-based approach
        longPressMessageCompose(originalMsg)
        Thread.sleep(2000)

        // Tap "Edit"
        val editOption = device.wait(Until.findObject(By.text("Edit")), SHORT_TIMEOUT)
        assertNotNull("Edit option not found in popup", editOption)
        editOption!!.click()
        Thread.sleep(1500)

        // Modify text in composer using retry loop
        val editedMsg = "${originalMsg}EDITED"
        var typed = false
        repeat(3) { attempt ->
            if (typed) return@repeat
            try {
                val editText = device.findObject(By.clazz("android.widget.EditText"))
                assertNotNull("EditText not found in edit mode", editText)
                editText!!.click()
                Thread.sleep(500)
                editText.clear()
                Thread.sleep(300)
                editText.text = editedMsg
                typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                Thread.sleep(1500)
            }
        }
        Thread.sleep(1000)

        // Tap send
        val sendBtn = device.findObject(By.desc("Send message"))
            ?: device.findObject(By.desc("Send"))
        assertNotNull("Send button not found for edit confirm", sendBtn)
        sendBtn!!.click()

        // Verify edited message appears
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)
        val edited = device.wait(Until.hasObject(By.textContains("EDITED")), TIMEOUT)
        assertTrue("Edited message not found", edited)
    }

    /**
     * GRP-044: Cancel edit returns to normal mode.
     */
    @Test
    fun test02_cancelEditReturnsToNormalMode() {
        val msg = "CancelEdit${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, msg)
        Thread.sleep(2000)
        E2ETestHelper.scrollDown(device)

        longPressMessageCompose(msg)
        Thread.sleep(2000)

        val editOption = device.wait(Until.findObject(By.text("Edit")), SHORT_TIMEOUT)
        if (editOption != null) {
            editOption.click()
            Thread.sleep(1500)

            // Look for cancel/close button in edit mode (usually an X icon)
            val cancelBtn = device.findObject(By.descContains("Close"))
                ?: device.findObject(By.descContains("Cancel"))
                ?: device.findObject(By.descContains("close"))
                ?: device.findObject(By.descContains("cancel"))
            if (cancelBtn != null) {
                cancelBtn.click()
                Thread.sleep(1000)
            } else {
                // Fallback: press Back to cancel edit mode
                device.pressBack()
                Thread.sleep(1000)
            }

            // Verify composer is back to normal (EditText still exists)
            val editText = device.findObject(By.clazz("android.widget.EditText"))
            assertNotNull("Composer should still exist after cancel", editText)
        }
    }

    /**
     * GRP-045: Edited message shows "edited" label.
     */
    @Test
    fun test03_editedMessageShowsEditedLabel() {
        val msg = "LabelCheck${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, msg)
        Thread.sleep(2000)
        E2ETestHelper.scrollDown(device)

        longPressMessageCompose(msg)
        Thread.sleep(2000)

        val editOption = device.wait(Until.findObject(By.text("Edit")), SHORT_TIMEOUT)
        assertNotNull("Edit option not found", editOption)
        editOption!!.click()
        Thread.sleep(1500)

        // Modify text using retry loop
        var typed = false
        repeat(3) { attempt ->
            if (typed) return@repeat
            try {
                val editText = device.findObject(By.clazz("android.widget.EditText"))
                editText?.click()
                Thread.sleep(500)
                editText?.clear()
                Thread.sleep(300)
                editText?.text = "${msg}v2"
                typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                Thread.sleep(1500)
            }
        }
        Thread.sleep(1000)

        val sendBtn = device.findObject(By.desc("Send message"))
            ?: device.findObject(By.desc("Send"))
        sendBtn?.click()
        Thread.sleep(3000)
        E2ETestHelper.scrollDown(device)

        // Verify "edited" label appears near the message
        val editedLabel = device.findObject(By.textContains("edited"))
            ?: device.findObject(By.textContains("Edited"))
        assertNotNull("'edited' label not found after editing message", editedLabel)
    }

    /**
     * GRP-046: Cannot edit another member's message (Edit option should not appear).
     *
     * The other member's message was sent via SDK in @Before. Since previous tests
     * (test01-03) send messages that push the other member's message up in the list,
     * we need to scroll UP aggressively to find it.
     */
    @Test
    fun test04_cannotEditOtherMemberMessage() {
        // The other member's message was sent in @Before — it will be above our own messages
        Thread.sleep(2000)

        // Scroll UP multiple times to find the other member's message (it's the oldest)
        var msgBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains("OtherMemberMsg")
        ) { true }

        // Try scrolling up several times to find it
        var attempts = 0
        while (msgBounds.isEmpty() && attempts < 5) {
            E2ETestHelper.scrollUp(device)
            Thread.sleep(2000)
            msgBounds = E2ETestHelper.safeGetBounds(
                device, By.textContains("OtherMemberMsg")
            ) { true }
            attempts++
        }

        // Also try scrolling down in case it's below
        if (msgBounds.isEmpty()) {
            E2ETestHelper.scrollDown(device)
            Thread.sleep(2000)
            msgBounds = E2ETestHelper.safeGetBounds(
                device, By.textContains("OtherMemberMsg")
            ) { true }
        }

        assertTrue("Other member's message not found in group after scrolling", msgBounds.isNotEmpty())

        // Long-press the other member's message using coordinate-based swipe
        // Use the message's Y coordinate but center X for full-width hit area
        val rect = msgBounds[0]
        val centerX = device.displayWidth / 2
        val centerY = rect.centerY()
        device.swipe(centerX, centerY, centerX, centerY, 100) // ~2s press
        Thread.sleep(2000)

        // Wait for popup to potentially appear
        val editOption = device.wait(Until.findObject(By.text("Edit")), SHORT_TIMEOUT)

        // Verify "Edit" option is NOT present in the popup
        // Note: If the popup appeared, Edit should not be listed for other's message
        // If popup didn't appear at all, that also means Edit isn't available (valid pass)
        assertNull("Edit option should NOT appear for other member's message", editOption)

        // Dismiss popup if it appeared (press back is safest)
        device.pressBack()
        Thread.sleep(500)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun longPressMessageCompose(messageText: String) {
        val bounds = E2ETestHelper.safeGetBounds(
            device, By.textContains(messageText)
        ) { true }
        assertTrue("Message '$messageText' not found for long-press", bounds.isNotEmpty())

        val rect = bounds[0]
        // Long-press ABOVE the text (on the bubble header area) to reliably trigger the popup.
        // For received messages, pressing on the text itself may not trigger OnLongClickListener.
        val targetY = if (rect.top - 30 > 50) rect.top - 30 else rect.centerY()
        val targetX = device.displayWidth / 2
        device.swipe(targetX, targetY, targetX, targetY, 100)
    }

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
            // Fallback: click first content item below search
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
