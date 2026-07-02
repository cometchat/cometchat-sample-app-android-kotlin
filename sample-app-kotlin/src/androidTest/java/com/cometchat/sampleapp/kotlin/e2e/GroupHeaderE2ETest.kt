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
 * E2E tests for the Group Message Header (single-device).
 *
 * Verifies the CometChatMessageHeader behavior when inside a group chat:
 * - Group name and avatar display
 * - Member count display
 * - Voice/Video call buttons visibility
 * - Details menu navigation to GroupDetailsScreen
 * - Back navigation from GroupDetails
 *
 * Setup: Creates a test group via SDK to guarantee owner role and known state.
 *
 * Test IDs:
 * - GRP-009: testHeaderDisplaysGroupName
 * - GRP-010: testHeaderDisplaysGroupAvatar
 * - GRP-011: testHeaderDisplaysMemberCount
 * - GRP-016: testVoiceCallButtonVisibleInGroupHeader
 * - GRP-017: testVideoCallButtonVisibleInGroupHeader
 * - GRP-018: testDetailsMenuNavigatesToGroupDetails
 * - GRP-019: testBackFromGroupDetailsReturnsToMessages
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.GroupHeaderE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupHeaderE2ETest {

    private lateinit var device: UiDevice

    private var testGroupId = ""
    private var testGroupName = ""

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        // Create a test group via SDK — guarantees owner role and known group name
        val ts = System.currentTimeMillis()
        testGroupId = "grp_header_$ts"
        testGroupName = "HeaderTest_$ts"

        val latch = CountDownLatch(1)
        val group = Group(testGroupId, testGroupName, CometChatConstants.GROUP_TYPE_PUBLIC, "")
        CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(g: Group) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(15, TimeUnit.SECONDS)

        // Navigate to the group via Groups tab search
        navigateToTestGroup()
    }

    @After
    fun teardown() {
        // Clean up: delete the test group
        if (testGroupId.isNotEmpty()) {
            val latch = CountDownLatch(1)
            CometChat.deleteGroup(testGroupId, object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) { latch.countDown() }
                override fun onError(e: CometChatException?) { latch.countDown() }
            })
            latch.await(10, TimeUnit.SECONDS)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Single-Device Tests
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GRP-009: Header displays group name.
     *
     * Verifies the group name is visible in the CometChatMessageHeader
     * when inside a group conversation.
     */
    @Test
    fun test01_headerDisplaysGroupName() {
        // Verify messages screen is loaded
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // Find the group name text in the header area (top 200px)
        val groupNameText = device.findObject(By.textContains(testGroupName.take(10)))
        assertNotNull(
            "Group name '$testGroupName' not visible in message header",
            groupNameText
        )

        // Verify it's in the header area (top portion of screen)
        assertTrue(
            "Group name text not in header area (top of screen)",
            groupNameText!!.visibleBounds.top < 300
        )
    }

    /**
     * GRP-010: Header displays group avatar.
     *
     * Verifies an avatar/image element is present in the header area.
     * The CometChatMessageHeader shows group initials or image.
     */
    @Test
    fun test02_headerDisplaysGroupAvatar() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // Find ImageView elements in the header area (avatar is an ImageView)
        val headerImages = device.findObjects(
            By.clazz("android.widget.ImageView")
        ).filter { it.visibleBounds.top < 300 && it.visibleBounds.left < 200 }

        assertTrue(
            "No avatar image found in the group header area",
            headerImages.isNotEmpty()
        )
    }

    /**
     * GRP-011: Header displays member count.
     *
     * Verifies the member count text (e.g., "1 member" or "X members")
     * is visible in the header subtitle area.
     */
    @Test
    fun test03_headerDisplaysMemberCount() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // Look for member count text in header area
        // The header with status bar can extend to ~300px on modern devices
        val memberText = device.findObject(By.textContains("member"))
            ?: device.findObject(By.textContains("Member"))

        assertNotNull(
            "Member count text ('member' or 'members') not found in group header",
            memberText
        )

        // Verify it's in the header area (allow up to 300px for status bar + toolbar)
        assertTrue(
            "Member count not in header area (top=${memberText!!.visibleBounds.top}, expected < 300)",
            memberText.visibleBounds.top < 300
        )
    }

    /**
     * GRP-016: Voice call button visible in group header.
     *
     * Verifies the voice/audio call button is present in the header.
     */
    @Test
    fun test04_voiceCallButtonVisibleInGroupHeader() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // Voice call button is an ImageView in the header area (right side)
        // It has content description related to "call" or "voice" or "audio"
        val voiceCallBtn = device.findObject(By.descContains("voice"))
            ?: device.findObject(By.descContains("audio"))
            ?: device.findObject(By.descContains("Voice"))
            ?: device.findObject(By.descContains("Audio"))

        // Fallback: Look for clickable ImageViews in the right portion of the header
        if (voiceCallBtn == null) {
            val headerRightButtons = device.findObjects(
                By.clazz("android.widget.ImageView").clickable(true)
            ).filter {
                it.visibleBounds.top < 300 &&
                    it.visibleBounds.left > device.displayWidth / 2
            }
            assertTrue(
                "No call buttons found in the right side of group header",
                headerRightButtons.size >= 2 // voice + video
            )
        } else {
            assertTrue(
                "Voice call button not in header area",
                voiceCallBtn.visibleBounds.top < 300
            )
        }
    }

    /**
     * GRP-017: Video call button visible in group header.
     *
     * Verifies the video call button is present in the header.
     */
    @Test
    fun test05_videoCallButtonVisibleInGroupHeader() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        val videoCallBtn = device.findObject(By.descContains("video"))
            ?: device.findObject(By.descContains("Video"))

        // Fallback: count clickable ImageViews on right side of header
        if (videoCallBtn == null) {
            val headerRightButtons = device.findObjects(
                By.clazz("android.widget.ImageView").clickable(true)
            ).filter {
                it.visibleBounds.top < 300 &&
                    it.visibleBounds.left > device.displayWidth / 2
            }
            assertTrue(
                "Less than 2 call buttons in header (need voice + video)",
                headerRightButtons.size >= 2
            )
        } else {
            assertTrue(
                "Video call button not in header area",
                videoCallBtn.visibleBounds.top < 300
            )
        }
    }

    /**
     * GRP-018: Details menu navigates to GroupDetailsActivity.
     *
     * Flow: Tap header overflow menu → tap "Details" → GroupDetailsActivity opens
     * showing group name, avatar, and member management options.
     */
    @Test
    fun test06_detailsMenuNavigatesToGroupDetails() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // Find and click the overflow/menu icon in the header (rightmost ImageView)
        val headerButtons = device.findObjects(
            By.clazz("android.widget.ImageView").clickable(true)
        ).filter { it.visibleBounds.top < 300 }

        val overflowBtn = headerButtons.maxByOrNull { it.visibleBounds.left }
        assertNotNull("Overflow menu button not found in header", overflowBtn)
        overflowBtn!!.click()
        Thread.sleep(1500)

        // Tap "Details" in the popup menu
        val detailsOption = device.findObject(By.text("Details"))
            ?: device.findObject(By.textContains("Details"))
            ?: device.findObject(By.textContains("Info"))
        assertNotNull("'Details' option not found in header popup menu", detailsOption)
        detailsOption!!.click()
        Thread.sleep(SETTLE_TIME)

        // Verify GroupDetailsActivity opened — look for group name and "members" text
        val groupNameInDetails = device.wait(
            Until.findObject(By.textContains(testGroupName.take(10))),
            TIMEOUT
        )
        assertNotNull(
            "Group name not visible in GroupDetails screen",
            groupNameInDetails
        )

        // Also verify "View Members" card or "members" text is present
        val memberSection = device.findObject(By.textContains("Member"))
            ?: device.findObject(By.textContains("member"))
        assertNotNull("Member section not found in Group Details", memberSection)
    }

    /**
     * GRP-019: Back from GroupDetails returns to messages.
     *
     * Flow: From GroupDetails → press back → verify MessagesActivity (messageList visible).
     */
    @Test
    fun test07_backFromGroupDetailsReturnsToMessages() {
        // First navigate to GroupDetails (reuse test06 flow)
        val headerButtons = device.findObjects(
            By.clazz("android.widget.ImageView").clickable(true)
        ).filter { it.visibleBounds.top < 300 }

        val overflowBtn = headerButtons.maxByOrNull { it.visibleBounds.left }
        assertNotNull("Overflow menu button not found in header", overflowBtn)
        overflowBtn!!.click()
        Thread.sleep(1500)

        val detailsOption = device.findObject(By.text("Details"))
            ?: device.findObject(By.textContains("Details"))
            ?: device.findObject(By.textContains("Info"))
        if (detailsOption != null) {
            detailsOption.click()
            Thread.sleep(SETTLE_TIME)
        } else {
            // If no popup appeared, overflow might have navigated directly
            Thread.sleep(SETTLE_TIME)
        }

        // Verify we're in GroupDetails
        val inDetails = device.findObject(By.textContains("Member"))
            ?: device.findObject(By.textContains("member"))
            ?: device.findObject(By.textContains("Leave"))
        assertNotNull("Not in GroupDetails — cannot test back navigation", inDetails)

        // Press back
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Verify we returned to MessagesActivity
        val messageList = device.wait(
            Until.findObject(By.res(PACKAGE, "messageList")),
            TIMEOUT
        )
        assertNotNull(
            "messageList not found after pressing back from GroupDetails",
            messageList
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Navigates to the test group via Groups tab search.
     */
    private fun navigateToTestGroup() {
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        // Search for the test group
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val searchBar = device.findObject(By.clazz("android.widget.EditText"))
        if (searchBar != null) {
            searchBar.clear()
            searchBar.text = testGroupName
            Thread.sleep(5000)

            // Click the group item below the search field
            var searchFieldBottom = 200
            try {
                val sf = device.findObject(By.clazz("android.widget.EditText"))
                if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 20
            } catch (_: Exception) { }

            val matches = device.findObjects(By.textContains(testGroupName.take(10)))
            for (match in matches) {
                if (match.visibleBounds.top > searchFieldBottom) {
                    match.click()
                    break
                }
            }
        } else {
            // Fallback: use openFirstGroup
            E2ETestHelper.openFirstGroup(device)
        }

        // Wait for messages screen
        val messagesLoaded = device.wait(
            Until.hasObject(By.res(PACKAGE, "messageList")),
            TIMEOUT
        )
        assertTrue("Messages screen did not load for test group", messagesLoaded)
        Thread.sleep(SETTLE_TIME)
    }
}
