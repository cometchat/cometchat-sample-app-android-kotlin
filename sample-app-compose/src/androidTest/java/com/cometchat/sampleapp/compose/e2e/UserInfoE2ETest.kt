package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SETTLE_TIME
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for the User Info (Details) screen in 1:1 conversations (sample-app-compose).
 *
 * Test IDs:
 * - 1TO1-063: testUserInfoShowsName
 * - 1TO1-064: testUserInfoShowsAvatar
 * - 1TO1-066: testUserInfoCallButtons
 * - 1TO1-067: testUserInfoBlockOption
 * - 1TO1-068: testUserInfoDeleteChatOption
 * - 1TO1-069: testDeleteChatNavigatesHome
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.UserInfoE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UserInfoE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)
        openUserDetailsFromHeader()
    }

    /**
     * 1TO1-063: User Info shows the user's display name.
     */
    @Test
    fun test01_userInfoShowsName() {
        val nameBounds = E2ETestHelper.safeGetBounds(
            device, By.clazz("android.widget.TextView")
        ) { it.top < device.displayHeight / 3 }
        assertTrue("User name text not found on User Info screen", nameBounds.isNotEmpty())
    }

    /**
     * 1TO1-064: User Info shows the user's avatar.
     */
    @Test
    fun test02_userInfoShowsAvatar() {
        val avatarBounds = E2ETestHelper.safeGetBounds(
            device, By.clazz("android.widget.ImageView")
        ) { it.top < device.displayHeight / 3 }
        assertTrue("Avatar not found on User Info screen", avatarBounds.isNotEmpty())
    }

    /**
     * 1TO1-066: User Info shows call buttons.
     */
    @Test
    fun test03_userInfoCallButtons() {
        val callOption = device.findObject(By.textContains("Call"))
            ?: device.findObject(By.descContains("Call"))
            ?: device.findObject(By.descContains("Voice"))
            ?: device.findObject(By.descContains("Video"))

        val callIcons = E2ETestHelper.safeGetBounds(
            device, By.clickable(true)
        ) { it.top < device.displayHeight / 3 }

        assertTrue(
            "Call buttons or icons should be present on User Info",
            callOption != null || callIcons.size >= 2
        )
    }

    /**
     * 1TO1-067: User Info shows a Block option.
     */
    @Test
    fun test04_userInfoBlockOption() {
        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val blockOption = device.findObject(By.textContains("Block"))
            ?: device.findObject(By.textContains("block"))
        assertNotNull("Block option not found on User Info screen", blockOption)
    }

    /**
     * 1TO1-068: User Info shows a Delete Chat option.
     */
    @Test
    fun test05_userInfoDeleteChatOption() {
        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val deleteOption = device.findObject(By.textContains("Delete Chat"))
            ?: device.findObject(By.textContains("Delete"))
            ?: device.findObject(By.textContains("Clear Chat"))
        assertNotNull("Delete Chat option not found on User Info screen", deleteOption)
    }

    /**
     * 1TO1-069: Delete Chat navigates back to home screen.
     */
    @Test
    fun test06_deleteChatNavigatesHome() {
        // Go back to messages to send a message first
        device.pressBack()
        Thread.sleep(SETTLE_TIME)
        E2ETestHelper.sendMessage(device, "DeleteChatTest${System.currentTimeMillis()}")
        Thread.sleep(2000)

        // Re-open details
        openUserDetailsFromHeader()
        E2ETestHelper.scrollDown(device)
        Thread.sleep(1000)

        val deleteOption = device.findObject(By.textContains("Delete Chat"))
            ?: device.findObject(By.textContains("Delete"))
            ?: device.findObject(By.textContains("Clear Chat"))
        assertNotNull("Delete Chat not found", deleteOption)
        deleteOption!!.click()
        Thread.sleep(2000)

        val confirmBtn = device.findObject(By.text("Delete"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes"))
            ?: device.findObject(By.textContains("Clear"))
        confirmBtn?.click()
        Thread.sleep(5000)

        // Should be back on home screen
        val homeScreen = device.findObject(By.desc("Chats"))
            ?: device.findObject(By.text("Chats"))
            ?: device.findObject(By.desc("Users"))
        assertNotNull("Should return to home screen after Delete Chat", homeScreen)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

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
                val infoContent = device.findObject(By.textContains("Block"))
                    ?: device.findObject(By.textContains("Delete"))
                if (infoContent != null) detailsOpened = true
                else { device.pressBack(); Thread.sleep(500) }
            }
        }

        if (!detailsOpened) {
            val topCenter = device.displayWidth / 2
            device.click(topCenter, 120)
            Thread.sleep(3000)
            val opt = device.findObject(By.text("Details")) ?: device.findObject(By.textContains("Details"))
            if (opt != null) { opt.click(); detailsOpened = true }
            else {
                val content = device.findObject(By.textContains("Block"))
                if (content != null) detailsOpened = true
            }
        }

        assertTrue("Could not open User Info from header", detailsOpened)
        Thread.sleep(SETTLE_TIME)
    }
}
