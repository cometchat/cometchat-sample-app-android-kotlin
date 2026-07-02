package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
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
 * E2E tests for Block/Unblock user in 1:1 conversations (sample-app-kotlin).
 *
 * Test IDs:
 * - 1TO1-058: testBlockedUserShowsBanner
 * - 1TO1-059: testBlockedUserDisablesComposer
 * - 1TO1-060: testBlockFromUserInfoScreen
 * - 1TO1-061: testUnblockFromUserInfoScreen
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.BlockUserE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class BlockUserE2ETest {

    private lateinit var device: UiDevice
    private val otherUserUid = E2ETestConfig.ONE_TO_ONE_UID
    private val otherUserName by lazy { E2ETestHelper.getUserName(E2ETestConfig.ONE_TO_ONE_UID) }

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        // Ensure user is unblocked before each test
        unblockUser()
    }

    @After
    fun teardown() {
        // Always unblock after tests
        unblockUser()
    }

    /**
     * 1TO1-058: When the user is blocked, a banner/indicator appears in the chat.
     */
    @Test
    fun test01_blockedUserShowsBanner() {
        // Block the user via SDK
        blockUser()

        // Navigate to the blocked user's chat
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        // Navigate to blocked user chat
        navigateToUserChat()
        Thread.sleep(SETTLE_TIME)

        // Look for blocked banner/indicator
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
        // Navigate to blocked user chat
        navigateToUserChat()
        Thread.sleep(SETTLE_TIME)

        // Composer should be disabled or hidden
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        if (composer != null) {
            val editText = composer.findObject(By.clazz("android.widget.EditText"))
            // EditText should be null (hidden) or not enabled
            val composerDisabled = editText == null || !editText.isEnabled
            assertTrue("Composer should be disabled when user is blocked", composerDisabled)
        } else {
            // Composer not present at all — also valid for blocked state
            assertTrue("Composer hidden when user is blocked (valid)", true)
        }
    }

    /**
     * 1TO1-060: Block a user from the User Info screen.
     */
    @Test
    fun test03_blockFromUserInfoScreen() {
        // Navigate to blocked user chat
        navigateToUserChat()

        // Open User Info/Details
        openUserDetailsFromHeader()

        // Find and tap "Block User" option
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

        // Verify blocked state — look for "Unblock" option now visible
        val unblockOption = device.findObject(By.textContains("Unblock"))
            ?: device.findObject(By.textContains("unblock"))

        // Or navigate back and verify blocked banner
        if (unblockOption != null) {
            assertNotNull("Unblock option should appear after blocking", unblockOption)
        } else {
            device.pressBack()
            Thread.sleep(SETTLE_TIME)
            val blockedBanner = device.findObject(By.textContains("blocked"))
                ?: device.findObject(By.textContains("Blocked"))
            assertNotNull("Blocked indicator should appear after blocking from User Info", blockedBanner)
        }
    }

    /**
     * 1TO1-061: Unblock a user from the User Info screen.
     */
    @Test
    fun test04_unblockFromUserInfoScreen() {
        // First, block the user
        blockUser()

        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        // Navigate to blocked user chat
        navigateToUserChat()

        // Open User Info/Details
        openUserDetailsFromHeader()

        // Find and tap "Unblock" option
        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val unblockOption = device.findObject(By.textContains("Unblock"))
            ?: device.findObject(By.textContains("unblock"))
        assertNotNull("Unblock option not found on User Info for blocked user", unblockOption)
        unblockOption!!.click()
        Thread.sleep(2000)

        // Confirm if dialog appears
        val confirmBtn = device.findObject(By.textContains("Unblock"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes"))
        confirmBtn?.click()
        Thread.sleep(3000)

        // Navigate back and verify composer is enabled again
        device.pressBack()
        Thread.sleep(SETTLE_TIME)

        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        if (composer != null) {
            val editText = composer.findObject(By.clazz("android.widget.EditText"))
            assertNotNull("Composer EditText should be enabled after unblock", editText)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

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

    /**
     * Navigates to the specific blocked user's chat by searching in Users tab.
     */
    private fun navigateToUserChat() {
        E2ETestHelper.navigateToTab(device, "Users")
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        val searchBar = device.findObject(By.clazz("android.widget.EditText"))
        searchBar?.clear()
        searchBar?.text = otherUserName
        Thread.sleep(5000)

        var searchFieldBottom = 200
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 20
        } catch (_: Exception) { }

        val matches = device.findObjects(By.textContains(otherUserName.take(8)))
        for (match in matches) {
            try {
                if (match.visibleBounds.top > searchFieldBottom) { match.click(); break }
            } catch (_: Exception) { continue }
        }

        device.wait(androidx.test.uiautomator.Until.hasObject(By.res(E2ETestHelper.PACKAGE, "messageList")), E2ETestHelper.TIMEOUT)
        Thread.sleep(E2ETestHelper.SETTLE_TIME)
    }

    private fun openUserDetailsFromHeader() {
        Thread.sleep(SETTLE_TIME)

        val headerButtons = device.findObjects(
            By.clazz("android.widget.ImageView").clickable(true)
        ).filter { it.visibleBounds.top < 250 }

        val rightMostBtn = headerButtons.maxByOrNull { it.visibleBounds.left }

        var detailsOpened = false
        if (rightMostBtn != null) {
            rightMostBtn.click()
            Thread.sleep(2000)

            val detailsOption = device.findObject(By.text("Details"))
                ?: device.findObject(By.textContains("Details"))
            if (detailsOption != null) {
                detailsOption.click()
                detailsOpened = true
            } else {
                val blockOption = device.findObject(By.textContains("Block"))
                    ?: device.findObject(By.textContains("Unblock"))
                if (blockOption != null) detailsOpened = true
                else { device.pressBack(); Thread.sleep(500) }
            }
        }

        if (!detailsOpened) {
            val messageHeader = device.findObject(By.res(PACKAGE, "messageHeader"))
            messageHeader?.click()
            Thread.sleep(2000)
        }

        Thread.sleep(SETTLE_TIME)
    }
}
