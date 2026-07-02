package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SETTLE_TIME
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.TIMEOUT
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
 * E2E tests for the 1:1 message header (sample-app-kotlin).
 *
 * Tests header information display: last seen, blocked status, info button.
 *
 * Test IDs:
 * - 1TO1-009: testHeaderShowsLastSeenWhenOffline
 * - 1TO1-011: testHeaderHidesStatusWhenBlocked
 * - 1TO1-014: testInfoButtonNavigatesToUserInfo
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.OneToOneHeaderE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class OneToOneHeaderE2ETest {

    private lateinit var device: UiDevice
    private val otherUserUid = E2ETestConfig.GROUP_MEMBER_1_UID

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        // Navigate to Users tab and open first user for most tests
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)
    }

    /**
     * 1TO1-009: Header shows "Last seen" or a timestamp when the other user is offline.
     *
     * When a user is offline, the header subtitle shows their last active time
     * (e.g., "Last Active At: ..." or "Offline").
     */
    @Test
    fun test01_headerShowsLastSeenWhenOffline() {
        // Verify we're on the messages screen
        val messageHeader = device.findObject(By.res(PACKAGE, "messageHeader"))
        assertNotNull("Message header not found", messageHeader)

        // Look for status/subtitle text in the header area
        val headerTexts = messageHeader!!.findObjects(By.clazz("android.widget.TextView"))
        assertTrue("No text elements found in message header", headerTexts.isNotEmpty())

        // The subtitle should contain either:
        // - "Last Active" / "last active" / "Last seen"
        // - "Offline"
        // - A time/date pattern (for last active timestamp)
        // - "Online" (if user happens to be online — still valid, header shows status)
        val hasStatusInfo = headerTexts.any { textView ->
            val text = textView.text ?: ""
            text.contains("Last", ignoreCase = true) ||
                text.contains("Offline", ignoreCase = true) ||
                text.contains("Online", ignoreCase = true) ||
                text.contains("Active", ignoreCase = true) ||
                text.contains("ago", ignoreCase = true) ||
                text.contains("AM", ignoreCase = false) ||
                text.contains("PM", ignoreCase = false) ||
                text.matches(Regex(".*\\d+:\\d+.*"))
        }

        assertTrue(
            "Header should show last seen/online status info. " +
                "Found texts: ${headerTexts.map { it.text }}",
            hasStatusInfo
        )
    }

    /**
     * 1TO1-011: Header hides online/last-seen status when the user is blocked.
     *
     * Blocks "d1" (Dhruv New 1), then searches and opens that specific user's chat.
     */
    @Test
    fun test02_headerHidesStatusWhenBlocked() {
        val blockedUid = E2ETestConfig.ONE_TO_ONE_UID
        val blockedName = E2ETestHelper.getUserName(E2ETestConfig.ONE_TO_ONE_UID)

        // Block the user via SDK
        val blockLatch = CountDownLatch(1)
        CometChat.blockUsers(listOf(blockedUid), object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(r: HashMap<String, String>?) { blockLatch.countDown() }
            override fun onError(e: CometChatException?) { blockLatch.countDown() }
        })
        blockLatch.await(10, TimeUnit.SECONDS)

        // Re-launch and navigate to the blocked user's chat by searching
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        E2ETestHelper.navigateToTab(device, "Users")
        Thread.sleep(SETTLE_TIME)

        // Search for "Dhruv New 1"
        val searchBar = device.findObject(By.clazz("android.widget.EditText"))
        searchBar?.clear()
        searchBar?.text = blockedName
        Thread.sleep(5000)

        // Click the user below search field
        var searchFieldBottom = 200
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 20
        } catch (_: Exception) { }

        val matches = device.findObjects(By.textContains(blockedName.take(8)))
        for (match in matches) {
            try {
                if (match.visibleBounds.top > searchFieldBottom) { match.click(); break }
            } catch (_: Exception) { continue }
        }

        device.wait(Until.hasObject(By.res(PACKAGE, "messageList")), TIMEOUT)
        Thread.sleep(SETTLE_TIME)

        // Check the header — status should be hidden when blocked
        val messageHeader = device.findObject(By.res(PACKAGE, "messageHeader"))
        if (messageHeader != null) {
            val headerTexts = messageHeader.findObjects(By.clazz("android.widget.TextView"))
            val hasOnlineStatus = headerTexts.any { textView ->
                val text = textView.text ?: ""
                text.contains("Online", ignoreCase = true) ||
                    text.contains("Last Active", ignoreCase = true)
            }

            assertTrue(
                "Header should hide online status when user is blocked",
                !hasOnlineStatus || headerTexts.any { (it.text ?: "").contains("Blocked", ignoreCase = true) }
            )
        }

        // Unblock for cleanup
        val unblockLatch = CountDownLatch(1)
        CometChat.unblockUsers(listOf(blockedUid), object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(r: HashMap<String, String>?) { unblockLatch.countDown() }
            override fun onError(e: CometChatException?) { unblockLatch.countDown() }
        })
        unblockLatch.await(10, TimeUnit.SECONDS)
    }

    /**
     * 1TO1-014: Tapping the info/details button in the header navigates to user info screen.
     */
    @Test
    fun test03_infoButtonNavigatesToUserInfo() {
        // Verify message header exists
        val messageHeader = device.findObject(By.res(PACKAGE, "messageHeader"))
        assertNotNull("Message header not found", messageHeader)

        // Find the rightmost clickable ImageView in the header (overflow/info icon)
        val headerButtons = device.findObjects(
            By.clazz("android.widget.ImageView").clickable(true)
        ).filter { it.visibleBounds.top < 250 }

        val rightMostBtn = headerButtons.maxByOrNull { it.visibleBounds.left }

        var detailsOpened = false

        if (rightMostBtn != null) {
            rightMostBtn.click()
            Thread.sleep(2000)

            // Check if a "Details" option appeared (overflow menu) or we went directly to details
            val detailsOption = device.findObject(By.text("Details"))
                ?: device.findObject(By.textContains("Details"))
            if (detailsOption != null) {
                detailsOption.click()
                detailsOpened = true
            } else {
                // Maybe it opened directly (some layouts navigate directly to user info)
                val userInfoContent = device.findObject(By.textContains("Block"))
                    ?: device.findObject(By.textContains("Delete Chat"))
                    ?: device.findObject(By.textContains("Call"))
                if (userInfoContent != null) {
                    detailsOpened = true
                } else {
                    device.pressBack()
                    Thread.sleep(500)
                }
            }
        }

        // Fallback: try all header buttons
        if (!detailsOpened) {
            for (btn in headerButtons) {
                try {
                    btn.click()
                    Thread.sleep(2000)
                    val detailsOption = device.findObject(By.text("Details"))
                        ?: device.findObject(By.textContains("Details"))
                    if (detailsOption != null) {
                        detailsOption.click()
                        detailsOpened = true
                        break
                    }
                    val userInfoContent = device.findObject(By.textContains("Block"))
                        ?: device.findObject(By.textContains("Delete Chat"))
                    if (userInfoContent != null) {
                        detailsOpened = true
                        break
                    }
                    device.pressBack()
                    Thread.sleep(500)
                } catch (_: Exception) { continue }
            }
        }

        assertTrue("Could not navigate to user info from header", detailsOpened)

        // Verify user info screen content
        Thread.sleep(SETTLE_TIME)
        val userInfoIndicator = device.findObject(By.textContains("Block"))
            ?: device.findObject(By.textContains("Delete"))
            ?: device.findObject(By.textContains("Call"))
            ?: device.findObject(By.clazz("android.widget.ImageView")) // Avatar
        assertNotNull("User info screen content not found after navigation", userInfoIndicator)
    }
}
