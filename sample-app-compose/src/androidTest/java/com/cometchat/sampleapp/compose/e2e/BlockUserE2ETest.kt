package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
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
 * E2E tests for Block/Unblock user in 1:1 conversations (sample-app-compose).
 *
 * IMPORTANT: Uses search-based navigation to find the specific user (cometchat-uid-1)
 * instead of openFirstUser(), because a blocked user may not appear as the first item.
 *
 * Test IDs:
 * - 1TO1-058: testBlockedUserShowsBanner
 * - 1TO1-059: testBlockedUserDisablesComposer
 * - 1TO1-060: testBlockFromUserInfoScreen
 * - 1TO1-061: testUnblockFromUserInfoScreen
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.BlockUserE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class BlockUserE2ETest {

    private lateinit var device: UiDevice
    private val otherUserUid = E2ETestConfig.GROUP_MEMBER_1_UID
    // The display name of cometchat-uid-1 — used for search
    private val otherUserName by lazy { E2ETestHelper.getUserName(E2ETestConfig.GROUP_MEMBER_1_UID) }

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
        unblockUser()
    }

    @After
    fun teardown() {
        unblockUser()
    }

    /**
     * 1TO1-058: When the user is blocked, a banner/indicator appears in the chat.
     */
    @Test
    fun test01_blockedUserShowsBanner() {
        blockUser()

        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        navigateToUserChat()
        Thread.sleep(SETTLE_TIME)

        val blockedIndicator = device.findObject(By.textContains("blocked"))
            ?: device.findObject(By.textContains("Blocked"))
            ?: device.findObject(By.textContains("unblock"))
            ?: device.findObject(By.textContains("Unblock"))
            ?: device.findObject(By.textContains("You have blocked"))

        assertNotNull(
            "Blocked banner or indicator should be visible when user is blocked",
            blockedIndicator
        )
    }

    /**
     * 1TO1-059: When the user is blocked, the composer is disabled.
     */
    @Test
    fun test02_blockedUserDisablesComposer() {
        blockUser()

        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        navigateToUserChat()
        Thread.sleep(SETTLE_TIME)

        // Composer EditText should be null (hidden) or not enabled
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        val composerDisabled = editText == null || !editText.isEnabled

        // Or blocked banner is shown (which implies composer is not usable)
        val blockedBanner = device.findObject(By.textContains("blocked"))
            ?: device.findObject(By.textContains("Blocked"))

        assertTrue(
            "Composer should be disabled or blocked banner shown when user is blocked",
            composerDisabled || blockedBanner != null
        )
    }

    /**
     * 1TO1-060: Block a user from the User Info screen.
     */
    @Test
    fun test03_blockFromUserInfoScreen() {
        navigateToUserChat()

        openUserDetailsFromHeader()

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val blockOption = device.findObject(By.textContains("Block"))
            ?: device.findObject(By.textContains("block"))
        assertNotNull("Block option not found on User Info screen", blockOption)
        blockOption!!.click()
        Thread.sleep(2000)

        // Confirm block if dialog appears
        val confirmBtn = device.findObject(By.text("Block"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes"))
        confirmBtn?.click()
        Thread.sleep(3000)

        // Verify blocked state
        val unblockOption = device.findObject(By.textContains("Unblock"))
            ?: device.findObject(By.textContains("unblock"))

        if (unblockOption != null) {
            assertNotNull("Unblock option should appear after blocking", unblockOption)
        } else {
            device.pressBack()
            Thread.sleep(SETTLE_TIME)
            val blockedBanner = device.findObject(By.textContains("blocked"))
                ?: device.findObject(By.textContains("Blocked"))
            assertNotNull("Blocked indicator should appear after blocking", blockedBanner)
        }
    }

    /**
     * 1TO1-061: Unblock a user from the User Info screen.
     */
    @Test
    fun test04_unblockFromUserInfoScreen() {
        blockUser()

        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        navigateToUserChat()

        openUserDetailsFromHeader()

        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val unblockOption = device.findObject(By.textContains("Unblock"))
            ?: device.findObject(By.textContains("unblock"))
        assertNotNull("Unblock option not found for blocked user", unblockOption)
        unblockOption!!.click()
        Thread.sleep(2000)

        val confirmBtn = device.findObject(By.textContains("Unblock"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes"))
        confirmBtn?.click()
        Thread.sleep(3000)

        // Navigate back and verify composer is enabled
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Composer EditText should be enabled after unblock", editText)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Navigates to the specific user's chat by searching in the Users tab.
     * This ensures we always open the correct user (cometchat-uid-1) regardless
     * of list ordering or blocked status.
     */
    private fun navigateToUserChat() {
        E2ETestHelper.navigateToTab(device, "Users")
        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        // Search for the user by name
        val searchTerm = otherUserName.split(" ")[0] // Use first name for search
        var searchSuccess = false
        repeat(3) {
            if (searchSuccess) return@repeat
            try {
                val searchBar = device.findObject(By.clazz("android.widget.EditText"))
                searchBar?.click()
                Thread.sleep(500)
                searchBar?.clear()
                searchBar?.text = searchTerm
                searchSuccess = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
        Thread.sleep(5000) // Wait for search results

        // Find and click the user below the search field
        var searchFieldBottom = 250
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 30
        } catch (_: androidx.test.uiautomator.StaleObjectException) { }

        val userBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains(searchTerm)
        ) { it.top > searchFieldBottom }

        if (userBounds.isNotEmpty()) {
            device.click(userBounds[0].centerX(), userBounds[0].centerY())
        } else {
            // Fallback: click first content item below search
            val contentBounds = E2ETestHelper.safeGetBounds(device, By.clickable(true)) { bounds ->
                bounds.top > searchFieldBottom && bounds.bottom < device.displayHeight - 200
            }
            if (contentBounds.isNotEmpty()) {
                device.click(contentBounds[0].centerX(), contentBounds[0].centerY())
            }
        }

        // Wait for messages screen to load
        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), TIMEOUT)
        Thread.sleep(SETTLE_TIME)
    }

    private fun blockUser() {
        val latch = CountDownLatch(1)
        CometChat.blockUsers(listOf(otherUserUid), object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(r: HashMap<String, String>?) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(10, TimeUnit.SECONDS)
    }

    private fun unblockUser() {
        val latch = CountDownLatch(1)
        CometChat.unblockUsers(listOf(otherUserUid), object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(r: HashMap<String, String>?) { latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(10, TimeUnit.SECONDS)
    }

    private fun openUserDetailsFromHeader() {
        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        val headerBounds = E2ETestHelper.safeGetBounds(
            device, By.clickable(true)
        ) { it.top < 250 && it.left > device.displayWidth / 2 }

        val rightMost = headerBounds.maxByOrNull { it.left }

        var detailsOpened = false
        if (rightMost != null) {
            device.click(rightMost.centerX(), rightMost.centerY())
            Thread.sleep(2000)

            val detailsOption = device.findObject(By.text("Details"))
                ?: device.findObject(By.textContains("Details"))
            if (detailsOption != null) {
                detailsOption.click()
                detailsOpened = true
            } else {
                val blockContent = device.findObject(By.textContains("Block"))
                    ?: device.findObject(By.textContains("Unblock"))
                if (blockContent != null) detailsOpened = true
                else { device.pressBack(); Thread.sleep(500) }
            }
        }

        if (!detailsOpened) {
            val topCenter = device.displayWidth / 2
            device.click(topCenter, 120)
            Thread.sleep(3000)
        }

        Thread.sleep(SETTLE_TIME)
    }
}
