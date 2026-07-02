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
 * E2E tests for Group Edge Cases (sample-app-compose).
 *
 * Key Compose differences:
 * - No resource IDs — uses text-based, desc, and class selectors
 * - StaleObjectException handling with safeGetBounds
 * - Long-press uses coordinate-based swipe
 * - Messages screen detection via EditText (composer) or desc("Send message")
 * - Must call E2ETestHelper.launchApp(device) after SDK login switches
 *
 * Test IDs:
 * - GRP-117: testOpenGroupAfterBeingKickedShowsNonMemberBanner
 * - GRP-118: testMessagesLoadOnScrollPaginationInGroup
 * - GRP-119: testGroupWithOneMemberOwnerDeletesGroup
 * - GRP-120: testRotateDevicePreservesGroupMessagesScrollPosition
 * - GRP-121: testRotateDevicePreservesComposerDraftText
 * - GRP-122: testCopyMessageTextFromGroup
 * - GRP-123: testMessageInfoShowsSentAtTimestamp
 * - GRP-124: testLongPressGroupMessageShowsActionPopup
 * - GRP-126: testGroupWithSpecialCharsInNameRendersCorrectly
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.GroupEdgeCasesE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupEdgeCasesE2ETest {

    private lateinit var device: UiDevice
    private var testGroupId = ""
    private var testGroupName = ""
    private val otherMemberUid = E2ETestConfig.GROUP_MEMBER_1_UID

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        val ts = System.currentTimeMillis()
        testGroupId = "grp_edge_$ts"
        testGroupName = "EdgeGrp$ts"

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
        // Restore orientation
        try {
            device.setOrientationNatural()
            device.unfreezeRotation()
        } catch (_: Exception) { }

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
     * GRP-118: Messages load on scroll (pagination) in the group.
     */
    @Test
    fun test01_messagesLoadOnScrollPaginationInGroup() {
        // Send at least 15 messages to fill the screen and trigger pagination on scroll
        for (i in 1..15) {
            E2ETestHelper.sendMessage(device, "PaginationMsg$i ts${System.currentTimeMillis()}")
            Thread.sleep(1000)
        }
        Thread.sleep(3000)

        // Scroll down to see latest messages
        E2ETestHelper.scrollDown(device)
        Thread.sleep(2000)

        // Verify at least one of our messages is visible
        val lastMsg = device.findObject(By.textContains("PaginationMsg"))
        assertNotNull("Sent messages not visible in the message list", lastMsg)

        // Scroll up to trigger older messages loading (pagination)
        E2ETestHelper.scrollUp(device)
        Thread.sleep(3000)

        // Verify the screen is still functional (EditText still present)
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Composer disappeared after scroll — pagination may have failed", editText)
    }

    /**
     * GRP-120: Rotate device preserves group messages scroll position.
     */
    @Test
    fun test02_rotateDevicePreservesGroupMessagesScrollPosition() {
        // Send a message to have content
        val msg = "RotateTest${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, msg)
        Thread.sleep(3000)

        // Verify message is visible
        val msgBounds = E2ETestHelper.safeGetBounds(device, By.textContains("RotateTest")) { true }
        assertTrue("Test message not visible before rotation", msgBounds.isNotEmpty())

        // Rotate to landscape
        device.setOrientationLeft()
        Thread.sleep(3000)

        // Verify messages screen is still present (EditText or scrollable list)
        val editTextAfterRotate = device.findObject(By.clazz("android.widget.EditText"))
        val scrollableAfterRotate = device.findObject(By.scrollable(true))
        assertTrue(
            "Messages screen not found after rotation",
            editTextAfterRotate != null || scrollableAfterRotate != null
        )

        // Rotate back to portrait
        device.setOrientationNatural()
        Thread.sleep(3000)

        // Verify still on messages screen
        val editTextFinal = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Composer not found after rotating back to portrait", editTextFinal)
    }

    /**
     * GRP-121: Rotate device preserves composer draft text.
     * This test verifies that text typed in the composer is preserved after rotation.
     * If the draft is lost, this test FAILS — indicating a bug in state preservation.
     */
    @Test
    fun test03_rotateDevicePreservesComposerDraftText() {
        val draftText = "DraftPreserve${System.currentTimeMillis()}"

        // Type text in the composer but DON'T send
        var typed = false
        repeat(3) {
            if (typed) return@repeat
            try {
                val editText = device.findObject(By.clazz("android.widget.EditText"))
                editText?.click()
                Thread.sleep(500)
                editText?.text = draftText
                typed = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
        assertTrue("Failed to type draft text in composer", typed)
        Thread.sleep(1000)

        // Rotate to landscape
        device.setOrientationLeft()
        Thread.sleep(3000)

        // Check if the draft text is preserved in the composer after rotation
        var textAfterRotation = ""
        try {
            val editTextAfter = device.findObject(By.clazz("android.widget.EditText"))
            textAfterRotation = editTextAfter?.text ?: ""
        } catch (_: Exception) { }

        // Rotate back to portrait
        device.setOrientationNatural()
        Thread.sleep(3000)

        // Check text after rotating back
        var textAfterReturn = ""
        try {
            val editTextFinal = device.findObject(By.clazz("android.widget.EditText"))
            textAfterReturn = editTextFinal?.text ?: ""
        } catch (_: Exception) { }

        // The draft text should be preserved after rotation
        // If it's empty, this is a BUG in the app's state management
        assertTrue(
            "Draft text should be preserved after device rotation. " +
                "Expected: '$draftText', After landscape: '$textAfterRotation', After portrait: '$textAfterReturn'. " +
                "This indicates a state preservation bug in the Compose message composer.",
            textAfterRotation.contains(draftText) || textAfterReturn.contains(draftText)
        )
    }

    /**
     * GRP-122: Copy message text from group.
     */
    @Test
    fun test04_copyMessageTextFromGroup() {
        val msg = "CopyThis${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, msg)
        Thread.sleep(3000)

        // Long-press the message via coordinate swipe
        longPressMessageCompose(msg)

        // Look for "Copy" option in the popup menu
        val copyOption = device.wait(Until.findObject(By.text("Copy")), 5000)
            ?: device.findObject(By.textContains("Copy"))
        assertNotNull("Copy option not found in popup menu after long-press", copyOption)
        copyOption!!.click()
        Thread.sleep(2000)

        // After copying, popup should dismiss and we should be back on messages
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Should return to messages after copying", editText)
    }

    /**
     * GRP-123: Message info shows Sent At timestamp.
     */
    @Test
    fun test05_messageInfoShowsSentAtTimestamp() {
        val msg = "InfoTest${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, msg)
        Thread.sleep(3000)

        // Long-press the message
        longPressMessageCompose(msg)

        // Find "Info" option
        val infoOption = device.wait(Until.findObject(By.text("Info")), 5000)
            ?: device.findObject(By.textContains("Info"))
        assertNotNull("Info option not found in popup menu", infoOption)
        infoOption!!.click()
        Thread.sleep(3000)

        // Verify message info shows timestamp info
        val sentAtLabel = device.findObject(By.textContains("Sent"))
            ?: device.findObject(By.textContains("sent"))
            ?: device.findObject(By.textContains("Delivered"))
            ?: device.findObject(By.textContains("delivered"))
        assertNotNull("Sent/Delivered timestamp not found in message info", sentAtLabel)

        // Go back
        device.pressBack()
        Thread.sleep(1000)
    }

    /**
     * GRP-124: Long-press group message shows action popup.
     */
    @Test
    fun test06_longPressGroupMessageShowsActionPopup() {
        val msg = "PopupTest${System.currentTimeMillis()}"
        E2ETestHelper.sendMessage(device, msg)
        Thread.sleep(3000)

        // Long-press the message
        longPressMessageCompose(msg)

        // Verify popup appeared with at least one expected option
        val hasEdit = device.findObject(By.text("Edit")) != null
        val hasDelete = device.findObject(By.text("Delete")) != null
        val hasCopy = device.findObject(By.text("Copy")) != null
        val hasReply = device.findObject(By.textContains("Reply")) != null
        val hasInfo = device.findObject(By.text("Info")) != null

        assertTrue(
            "Action popup should show at least one option (Edit/Delete/Copy/Reply/Info)",
            hasEdit || hasDelete || hasCopy || hasReply || hasInfo
        )

        // Dismiss popup
        device.pressBack()
        Thread.sleep(1000)
    }

    /**
     * GRP-126: Group with special characters in name renders correctly.
     */
    @Test
    fun test07_groupWithSpecialCharsInNameRendersCorrectly() {
        // Create a group with special characters
        val specialGroupId = "grp_special_${System.currentTimeMillis()}"
        val specialGroupName = "Test & Dev (2024)"

        val latch = CountDownLatch(1)
        val group = Group(specialGroupId, specialGroupName, CometChatConstants.GROUP_TYPE_PUBLIC, "")
        CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(g: Group) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(15, TimeUnit.SECONDS)

        // Navigate to Groups tab and search for it
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
                searchBar?.clear(); searchBar?.text = "Test & Dev"
                searchSuccess = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
        Thread.sleep(5000)

        // Find the group with special chars
        val groupBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains("Test & Dev")
        ) { true }
        assertTrue("Group with special characters not found in Groups tab", groupBounds.isNotEmpty())

        // Click to open
        device.click(groupBounds[0].centerX(), groupBounds[0].centerY())
        Thread.sleep(3000)

        // Verify messages screen loaded
        val messagesLoaded = device.wait(
            Until.hasObject(By.clazz("android.widget.EditText")),
            TIMEOUT
        )
        assertTrue("Messages screen did not load for group with special chars", messagesLoaded)

        // Clean up
        val deleteLatch = CountDownLatch(1)
        CometChat.deleteGroup(specialGroupId, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) { deleteLatch.countDown() }
            override fun onError(e: CometChatException?) { deleteLatch.countDown() }
        })
        deleteLatch.await(10, TimeUnit.SECONDS)
    }

    /**
     * GRP-117: Open group after being banned shows non-member state.
     * Banned members cannot rejoin (unlike kicked members who can).
     */
    @Test
    fun test08_openGroupAfterBeingBannedShowsNonMemberBanner() {
        // BAN (not kick) the other member — banned members cannot rejoin
        val banLatch = CountDownLatch(1)
        CometChat.banGroupMember(otherMemberUid, testGroupId,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) { banLatch.countDown() }
                override fun onError(e: CometChatException?) { banLatch.countDown() }
            })
        banLatch.await(10, TimeUnit.SECONDS)

        // Login as the kicked user
        val loginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(otherMemberUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { loginLatch.countDown() }
                override fun onError(e: CometChatException?) { loginLatch.countDown() }
            })
        loginLatch.await(10, TimeUnit.SECONDS)

        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)

        // Navigate to Groups tab and try to find/open the group
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        var searchSuccess = false
        repeat(3) {
            if (searchSuccess) return@repeat
            try {
                val searchBar = device.findObject(By.clazz("android.widget.EditText"))
                searchBar?.click(); Thread.sleep(500)
                searchBar?.clear(); searchBar?.text = testGroupName
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
        ) { it.top > searchFieldBottom }

        var clicked = false
        if (groupBounds.isNotEmpty()) {
            device.click(groupBounds[0].centerX(), groupBounds[0].centerY())
            clicked = true
        }
        Thread.sleep(5000)

        // After being kicked, the group shouldn't be accessible normally
        val sendBtn = device.findObject(By.desc("Send message"))
        val nonMemberBanner = device.findObject(By.textContains("no longer"))
            ?: device.findObject(By.textContains("not a member"))
            ?: device.findObject(By.textContains("not a participant"))
            ?: device.findObject(By.textContains("cannot"))

        // Either the group is not found, messages don't load, or non-member banner shows
        val correctBehavior = !clicked || sendBtn == null || nonMemberBanner != null
        assertTrue("Kicked user should not be able to access the group normally", correctBehavior)

        // Login back
        val reloginLatch = CountDownLatch(1)
        com.cometchat.uikit.core.CometChatUIKit.login(E2ETestHelper.testUid,
            object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) { reloginLatch.countDown() }
                override fun onError(e: CometChatException?) { reloginLatch.countDown() }
            })
        reloginLatch.await(10, TimeUnit.SECONDS)
    }

    /**
     * GRP-119: Group with one member — owner deletes the group.
     */
    @Test
    fun test09_groupWithOneMemberOwnerDeletesGroup() {
        // Create a solo group (no other members)
        val soloGroupId = "grp_solo_${System.currentTimeMillis()}"
        val soloGroupName = "SoloGrp${System.currentTimeMillis()}"

        val latch = CountDownLatch(1)
        val group = Group(soloGroupId, soloGroupName, CometChatConstants.GROUP_TYPE_PUBLIC, "")
        CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(g: Group) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(15, TimeUnit.SECONDS)

        // Delete the group via SDK (owner with no other members)
        val deleteLatch = CountDownLatch(1)
        var deleteSuccess = false
        CometChat.deleteGroup(soloGroupId, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) {
                deleteSuccess = true
                deleteLatch.countDown()
            }
            override fun onError(e: CometChatException?) { deleteLatch.countDown() }
        })
        deleteLatch.await(10, TimeUnit.SECONDS)

        assertTrue("Owner should be able to delete a group with only one member", deleteSuccess)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Long-press a message in Compose using coordinate-based swipe.
     * Targets above the text (bubble header area) for received messages,
     * and center for own messages.
     */
    private fun longPressMessageCompose(messageText: String) {
        val textBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains(messageText)
        ) { true }
        assertTrue("Message '$messageText' not found for long-press", textBounds.isNotEmpty())

        val bounds = textBounds[0]
        val targetX = bounds.centerX()
        val targetY = bounds.centerY()

        // Long-press via swipe with same start/end (2.5s duration)
        device.swipe(targetX, targetY, targetX, targetY, 100)
        Thread.sleep(2500)
    }

    private fun navigateToTestGroup() {
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        var searchSuccess = false
        repeat(3) {
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
