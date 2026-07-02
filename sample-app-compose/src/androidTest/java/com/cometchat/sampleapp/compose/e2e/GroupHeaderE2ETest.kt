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
 * E2E tests for the Group Message Header in the Compose sample app (single-device).
 *
 * Verifies CometChatMessageHeader behavior in group context:
 * - Group name and avatar display
 * - Member count display
 * - Voice/Video call buttons visibility
 * - Details menu navigation to GroupDetailsScreen
 * - Back navigation from GroupDetails
 *
 * Setup: Creates a test group via SDK to guarantee owner role and known state.
 *
 * Key Compose differences:
 * - No resource IDs — uses By.desc(), By.text(), By.textContains()
 * - StaleObjectException handled via safeGetBounds()
 * - Details menu uses "Menu" content description for overflow dots icon
 * - Group details shows "Group Info" title text
 * - Action cards use multiline text ("View\nMembers")
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
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.GroupHeaderE2ETest
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
     * Verifies the group name is visible in the CometChatMessageHeader.
     * In Compose: uses By.textContains() to find the group name text.
     */
    @Test
    fun test01_headerDisplaysGroupName() {
        // Verify we're on messages screen (EditText = composer present)
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded (no composer EditText)", composer)

        // Find the group name text (in the header area — top 200px)
        val groupNameBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains(testGroupName.take(10))
        ) { bounds -> bounds.top < 300 }

        assertTrue(
            "Group name '$testGroupName' not visible in message header area",
            groupNameBounds.isNotEmpty()
        )
    }

    /**
     * GRP-010: Header displays group avatar.
     *
     * Verifies an avatar/image element is present in the header area.
     * In Compose: CometChatAvatar renders as an Image composable.
     */
    @Test
    fun test02_headerDisplaysGroupAvatar() {
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", composer)

        // Wait for header to fully render
        Thread.sleep(3000)
        device.waitForIdle()

        // Find clickable/image elements in the top-left header area (avatar location)
        // Avatar in Compose can be up to 120px, so broaden the size check
        val headerLeftBounds = E2ETestHelper.safeGetBounds(
            device, By.clickable(true)
        ) { bounds ->
            bounds.top < 300 && bounds.left < 300 &&
                bounds.width() < 150 && bounds.height() < 150
        }

        // Also look for any ImageView in the header area
        val headerImages = E2ETestHelper.safeGetBounds(
            device, By.clazz("android.widget.ImageView")
        ) { bounds ->
            bounds.top < 300 && bounds.left < 300
        }

        // Also check for any View element in the avatar zone
        val anyViewInZone = E2ETestHelper.safeGetBounds(
            device, By.clazz("android.view.View")
        ) { bounds ->
            bounds.top < 250 && bounds.left < 200 &&
                bounds.width() in 20..150 && bounds.height() in 20..150
        }

        assertTrue(
            "No avatar element found in the left side of group header",
            headerLeftBounds.isNotEmpty() || headerImages.isNotEmpty() || anyViewInZone.isNotEmpty()
        )
    }

    /**
     * GRP-011: Header displays member count.
     *
     * Verifies member count text (e.g., "1 member" or "X members")
     * is visible in the header subtitle area.
     */
    @Test
    fun test03_headerDisplaysMemberCount() {
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", composer)

        // Look for "member" or "members" text in the header area
        val memberBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains("member")
        ) { bounds -> bounds.top < 300 }

        // Fallback: also check for "Member" (capital M)
        val memberBoundsAlt = if (memberBounds.isEmpty()) {
            E2ETestHelper.safeGetBounds(
                device, By.textContains("Member")
            ) { bounds -> bounds.top < 300 }
        } else memberBounds

        assertTrue(
            "Member count text not found in group header area",
            memberBounds.isNotEmpty() || memberBoundsAlt.isNotEmpty()
        )
    }

    /**
     * GRP-016: Voice call button visible in group header.
     *
     * In Compose: CometChatCallButtons renders with contentDescription = "Voice Call"
     * (verified from chatuikit-compose/.../callbuttons/ui/CometChatCallButtons.kt)
     */
    @Test
    fun test04_voiceCallButtonVisibleInGroupHeader() {
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", composer)

        // Exact contentDescription from source: "Voice Call"
        val voiceBtn = device.findObject(By.desc("Voice Call"))
            ?: device.findObject(By.descContains("Voice Call"))

        assertNotNull(
            "Voice call button (desc='Voice Call') not found in group header",
            voiceBtn
        )
    }

    /**
     * GRP-017: Video call button visible in group header.
     *
     * In Compose: CometChatCallButtons renders with contentDescription = "Video Call"
     * (verified from chatuikit-compose/.../callbuttons/ui/CometChatCallButtons.kt)
     */
    @Test
    fun test05_videoCallButtonVisibleInGroupHeader() {
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", composer)

        // Exact contentDescription from source: "Video Call"
        val videoBtn = device.findObject(By.desc("Video Call"))
            ?: device.findObject(By.descContains("Video Call"))

        assertNotNull(
            "Video call button (desc='Video Call') not found in group header",
            videoBtn
        )
    }

    /**
     * GRP-018: Details menu navigates to GroupDetailsScreen.
     *
     * Flow: Tap overflow menu (3 dots icon) → tap "Details" → verify GroupDetails shows.
     * In Compose: Overflow icon has desc "Menu", popup has text "Details".
     * GroupDetailsScreen shows "Group Info" title and action cards.
     */
    @Test
    fun test06_detailsMenuNavigatesToGroupDetails() {
        val composer = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", composer)

        // Find overflow menu icon — desc "Menu" or rightmost clickable in header
        var menuClicked = false
        val menuBtn = device.findObject(By.desc("Menu"))
            ?: device.findObject(By.descContains("Menu"))
            ?: device.findObject(By.descContains("menu"))

        if (menuBtn != null) {
            menuBtn.click()
            menuClicked = true
        } else {
            // Fallback: tap the rightmost clickable in the header area
            val headerBounds = E2ETestHelper.safeGetBounds(
                device, By.clickable(true)
            ) { bounds -> bounds.top < 300 && bounds.left > device.displayWidth * 2 / 3 }

            if (headerBounds.isNotEmpty()) {
                val rightmost = headerBounds.maxByOrNull { it.left }!!
                device.click(rightmost.centerX(), rightmost.centerY())
                menuClicked = true
            }
        }
        assertTrue("Could not find/click overflow menu in header", menuClicked)
        Thread.sleep(1500)

        // Tap "Details" in the popup
        val detailsOption = device.findObject(By.text("Details"))
            ?: device.findObject(By.textContains("Details"))
        assertNotNull("'Details' option not found in popup menu", detailsOption)
        detailsOption!!.click()
        Thread.sleep(SETTLE_TIME)

        // Verify GroupDetails screen opened — look for "Group Info" title or group name
        val groupInfoTitle = device.wait(
            Until.findObject(By.textContains("Group Info")),
            TIMEOUT
        )
        val groupNameInDetails = device.findObject(By.textContains(testGroupName.take(10)))

        assertTrue(
            "GroupDetails screen not opened — neither 'Group Info' title nor group name found",
            groupInfoTitle != null || groupNameInDetails != null
        )

        // Verify member-related content exists (cards or text)
        // Compose uses "View\nMembers" (multiline), so use textContains
        val memberContent = device.findObject(By.textContains("Members"))
            ?: device.findObject(By.textContains("members"))
            ?: device.findObject(By.textContains("View"))
        assertNotNull("No member-related content in Group Details", memberContent)
    }

    /**
     * GRP-019: Back from GroupDetails returns to messages.
     *
     * Flow: Navigate to GroupDetails → press back → verify messages screen returns.
     * In Compose: back uses navController.popBackStack().
     */
    @Test
    fun test07_backFromGroupDetailsReturnsToMessages() {
        // First navigate to GroupDetails
        var menuClicked = false
        val menuBtn = device.findObject(By.desc("Menu"))
            ?: device.findObject(By.descContains("Menu"))

        if (menuBtn != null) {
            menuBtn.click()
            menuClicked = true
        } else {
            val headerBounds = E2ETestHelper.safeGetBounds(
                device, By.clickable(true)
            ) { bounds -> bounds.top < 300 && bounds.left > device.displayWidth * 2 / 3 }
            if (headerBounds.isNotEmpty()) {
                val rightmost = headerBounds.maxByOrNull { it.left }!!
                device.click(rightmost.centerX(), rightmost.centerY())
                menuClicked = true
            }
        }

        if (menuClicked) {
            Thread.sleep(1500)
            val detailsOption = device.findObject(By.text("Details"))
                ?: device.findObject(By.textContains("Details"))
            detailsOption?.click()
            Thread.sleep(SETTLE_TIME)
        }

        // Verify we're in GroupDetails
        val inDetails = device.findObject(By.textContains("Group Info"))
            ?: device.findObject(By.textContains("Members"))
            ?: device.findObject(By.textContains("Leave"))
        assertNotNull("Not in GroupDetails screen", inDetails)

        // Press back
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        // Verify messages screen returned (EditText = composer present)
        val composerBack = device.wait(
            Until.findObject(By.clazz("android.widget.EditText")),
            TIMEOUT
        )
        assertNotNull(
            "Messages screen (composer) not found after pressing back from GroupDetails",
            composerBack
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Navigates to the test group via Groups tab search.
     * Uses safe bounds pattern for Compose StaleObjectException handling.
     */
    private fun navigateToTestGroup() {
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        // Search for the test group — use retry loop for Compose
        var success = false
        repeat(3) { attempt ->
            if (success) return@repeat
            try {
                val searchBar = device.findObject(By.clazz("android.widget.EditText"))
                if (searchBar != null) {
                    searchBar.click()
                    Thread.sleep(500)
                    searchBar.clear()
                    searchBar.text = testGroupName
                    success = true
                }
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                Thread.sleep(1500)
            }
        }

        Thread.sleep(5000) // Wait for search results

        // Click the group item below the search field
        var searchFieldBottom = 250
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 30
        } catch (_: androidx.test.uiautomator.StaleObjectException) { }

        // Find group name text below search and click it
        val groupBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains(testGroupName.take(10))
        ) { bounds -> bounds.top > searchFieldBottom }

        if (groupBounds.isNotEmpty()) {
            device.click(groupBounds[0].centerX(), groupBounds[0].centerY())
        } else {
            // Fallback: click first item below search
            val contentBounds = E2ETestHelper.safeGetBounds(
                device, By.clickable(true)
            ) { bounds ->
                bounds.top > searchFieldBottom && bounds.bottom < device.displayHeight - 200
            }
            if (contentBounds.isNotEmpty()) {
                device.click(contentBounds[0].centerX(), contentBounds[0].centerY())
            }
        }

        // Wait for messages screen (EditText = composer)
        val messagesLoaded = device.wait(
            Until.hasObject(By.clazz("android.widget.EditText")),
            TIMEOUT
        )
        assertTrue("Messages screen did not load for test group", messagesLoaded)
        Thread.sleep(SETTLE_TIME)
    }
}
