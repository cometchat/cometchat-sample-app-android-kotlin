package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SETTLE_TIME
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
 * E2E tests for the 1:1 message header (sample-app-compose).
 *
 * Tests header information display: last seen, blocked status, info button.
 *
 * Key Compose differences:
 * - No resource IDs — uses By.desc(), By.text(), By.clazz()
 * - Header area detected by position (top < 250)
 * - StaleObjectException handling with safeGetBounds
 * - Must call launchApp(device) after SDK login switches
 *
 * Test IDs:
 * - 1TO1-009: testHeaderShowsLastSeenWhenOffline
 * - 1TO1-011: testHeaderHidesStatusWhenBlocked
 * - 1TO1-014: testInfoButtonNavigatesToUserInfo
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.OneToOneHeaderE2ETest
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

        // Navigate to Users tab and open first user
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)
    }

    /**
     * 1TO1-009: Header shows "Last seen" or a timestamp when the other user is offline.
     *
     * When a user is offline, the header subtitle shows their last active time.
     */
    @Test
    fun test01_headerShowsLastSeenWhenOffline() {
        // Verify we're on the messages screen
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded (no EditText)", editText)

        Thread.sleep(SETTLE_TIME)

        // Find text elements in the header area (top ~250px)
        val allTexts = device.findObjects(By.clazz("android.widget.TextView"))
        val headerTexts = allTexts.filter { textView ->
            try {
                textView.visibleBounds.top < 250 &&
                    textView.text != null &&
                    textView.text.isNotBlank()
            } catch (_: androidx.test.uiautomator.StaleObjectException) { false }
        }

        assertTrue("No text elements found in message header", headerTexts.isNotEmpty())

        // The subtitle should contain status info
        val hasStatusInfo = headerTexts.any { textView ->
            val text = try { textView.text ?: "" } catch (_: Exception) { "" }
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
            "Header should show last seen/online status info",
            hasStatusInfo
        )
    }

    /**
     * 1TO1-011: Header hides online/last-seen status when the user is blocked.
     * Blocks "d1" (Dhruv New 1), searches and opens that specific user's chat.
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

        // Re-launch and search for the blocked user
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        E2ETestHelper.navigateToTab(device, "Users")
        Thread.sleep(SETTLE_TIME)

        // Search for "Dhruv New 1"
        var searchSuccess = false
        repeat(3) {
            if (searchSuccess) return@repeat
            try {
                val sb = device.findObject(By.clazz("android.widget.EditText"))
                sb?.click(); Thread.sleep(500); sb?.clear(); sb?.text = blockedName
                searchSuccess = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
        Thread.sleep(5000)

        var sfBottom = 250
        try { val sf = device.findObject(By.clazz("android.widget.EditText")); if (sf != null) sfBottom = sf.visibleBounds.bottom + 30 } catch (_: Exception) {}
        val ub = E2ETestHelper.safeGetBounds(device, By.textContains(blockedName.take(8))) { it.top > sfBottom }
        if (ub.isNotEmpty()) device.click(ub[0].centerX(), ub[0].centerY())
        Thread.sleep(5000)

        // Check the header texts — should NOT show "Online" or "Last Active"
        val allTexts = device.findObjects(By.clazz("android.widget.TextView"))
        val headerTexts = allTexts.filter { textView ->
            try {
                textView.visibleBounds.top < 250 && textView.text != null && textView.text.isNotBlank()
            } catch (_: androidx.test.uiautomator.StaleObjectException) { false }
        }

        val hasOnlineStatus = headerTexts.any { textView ->
            val text = try { textView.text ?: "" } catch (_: Exception) { "" }
            text.contains("Online", ignoreCase = true) ||
                text.contains("Last Active", ignoreCase = true)
        }

        assertTrue(
            "Header should hide online status when user is blocked",
            !hasOnlineStatus || headerTexts.any {
                val t = try { it.text ?: "" } catch (_: Exception) { "" }
                t.contains("Blocked", ignoreCase = true)
            }
        )

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
        // Verify we're on the messages screen
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded", editText)

        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        // Find clickable elements in the header area (top-right for info/overflow button)
        val headerBounds = E2ETestHelper.safeGetBounds(
            device, By.clickable(true)
        ) { it.top < 250 && it.left > device.displayWidth / 2 }

        val rightMost = headerBounds.maxByOrNull { it.left }

        var detailsOpened = false

        if (rightMost != null) {
            device.click(rightMost.centerX(), rightMost.centerY())
            Thread.sleep(2000)

            // Check if "Details" option appeared (overflow menu)
            val detailsOption = device.findObject(By.text("Details"))
                ?: device.findObject(By.textContains("Details"))
            if (detailsOption != null) {
                detailsOption.click()
                detailsOpened = true
            } else {
                // Check if we went directly to user info screen
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

        // Fallback: try tapping the header center (group name/avatar area)
        if (!detailsOpened) {
            val topCenter = device.displayWidth / 2
            device.click(topCenter, 120)
            Thread.sleep(3000)

            val detailsOption = device.findObject(By.text("Details"))
                ?: device.findObject(By.textContains("Details"))
            if (detailsOption != null) {
                detailsOption.click()
                detailsOpened = true
            } else {
                val userInfoContent = device.findObject(By.textContains("Block"))
                    ?: device.findObject(By.textContains("Delete Chat"))
                if (userInfoContent != null) {
                    detailsOpened = true
                }
            }
        }

        assertTrue("Could not navigate to user info from header", detailsOpened)

        // Verify user info screen content
        Thread.sleep(SETTLE_TIME)
        val userInfoIndicator = device.findObject(By.textContains("Block"))
            ?: device.findObject(By.textContains("Delete"))
            ?: device.findObject(By.textContains("Call"))
        assertNotNull("User info screen content not found after navigation", userInfoIndicator)
    }
}
