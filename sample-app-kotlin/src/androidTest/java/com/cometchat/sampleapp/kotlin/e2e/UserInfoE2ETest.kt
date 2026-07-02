package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SETTLE_TIME
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for the User Info (Details) screen in 1:1 conversations (sample-app-kotlin).
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
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.UserInfoE2ETest
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

        // Navigate to Users tab, open first user, then open details
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)
        openUserDetailsFromHeader()
    }

    /**
     * 1TO1-063: User Info shows the user's display name.
     */
    @Test
    fun test01_userInfoShowsName() {
        val allTexts = device.findObjects(By.clazz("android.widget.TextView"))
        val headerTexts = allTexts.filter {
            it.visibleBounds.top < device.displayHeight / 3 &&
                it.text != null && it.text.isNotBlank()
        }
        assertTrue("User name not found on User Info screen", headerTexts.isNotEmpty())
    }

    /**
     * 1TO1-064: User Info shows the user's avatar.
     */
    @Test
    fun test02_userInfoShowsAvatar() {
        val avatarImages = device.findObjects(By.clazz("android.widget.ImageView"))
            .filter { it.visibleBounds.top < device.displayHeight / 3 }
        assertTrue("Avatar not found on User Info screen", avatarImages.isNotEmpty())
    }

    /**
     * 1TO1-066: User Info shows call buttons (voice/video).
     * The buttons are MaterialCardViews with "Voice" and "Video" text inside.
     */
    @Test
    fun test03_userInfoCallButtons() {
        // Call buttons are MaterialCardViews with TextViews showing "Voice" and "Video"
        val voiceCallCard = device.findObject(By.res(PACKAGE, "card_voice_call"))
            ?: device.findObject(By.text("Voice"))
            ?: device.findObject(By.textContains("Voice"))
        val videoCallCard = device.findObject(By.res(PACKAGE, "card_video_call"))
            ?: device.findObject(By.text("Video"))
            ?: device.findObject(By.textContains("Video"))

        assertTrue(
            "Voice and/or Video call cards should be present on User Info",
            voiceCallCard != null || videoCallCard != null
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
        // First send a message to create a conversation
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

        // Confirm
        val confirmBtn = device.findObject(By.text("Delete"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes"))
            ?: device.findObject(By.textContains("Clear"))
        confirmBtn?.click()
        Thread.sleep(5000)

        // Should be back on home screen
        val bottomNav = device.findObject(By.res(PACKAGE, "bottomNavigationView"))
        assertNotNull("Should return to home screen after Delete Chat", bottomNav)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

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
                val infoContent = device.findObject(By.textContains("Block"))
                    ?: device.findObject(By.textContains("Delete"))
                if (infoContent != null) detailsOpened = true
                else { device.pressBack(); Thread.sleep(500) }
            }
        }

        if (!detailsOpened) {
            for (btn in headerButtons) {
                try {
                    btn.click()
                    Thread.sleep(2000)
                    val opt = device.findObject(By.text("Details"))
                        ?: device.findObject(By.textContains("Details"))
                    if (opt != null) { opt.click(); detailsOpened = true; break }
                    val content = device.findObject(By.textContains("Block"))
                    if (content != null) { detailsOpened = true; break }
                    device.pressBack(); Thread.sleep(500)
                } catch (_: Exception) { continue }
            }
        }

        assertTrue("Could not open User Info from header", detailsOpened)
        Thread.sleep(SETTLE_TIME)
    }
}
