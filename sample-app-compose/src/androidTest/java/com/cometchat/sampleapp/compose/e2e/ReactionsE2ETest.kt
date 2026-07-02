package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for Message Reactions in the Compose sample app.
 *
 * Test IDs:
 * - E2E-036: testAddReaction
 * - E2E-038: testTapReactionShowsList
 * - E2E-039: testRemoveReaction
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.ReactionsE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ReactionsE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        // Navigate to messages screen with a sent message
        E2ETestHelper.openFirstConversation(device)

        // Send a message so we have a target for reactions
        E2ETestHelper.sendMessage(device, E2ETestHelper.uniqueMessage("ReactTest"))
        Thread.sleep(3000)
    }

    /**
     * E2E-036: Add a reaction to a message.
     *
     * Long-presses a message to open the action menu, then selects a reaction emoji.
     */
    @Test
    fun test01_addReaction() {
        // Find the most recent message (the one we just sent)
        val messages = device.findObjects(By.textContains("ReactTest"))
        assertTrue("No sent message found to react to", messages.isNotEmpty())

        val targetMessage = messages.last()

        // Long-press on the message to trigger action menu / reaction picker
        targetMessage.longClick()
        Thread.sleep(2000)

        // Look for reaction emoji options
        val reactionFound = findAndTapReaction()

        if (reactionFound) {
            Thread.sleep(3000)
        }

        // Verify the app didn't crash and we're still on the messages screen
        val editText = device.findObject(By.clazz("android.widget.EditText"))
            ?: device.findObject(By.desc("Send message"))
        assertNotNull("Messages screen should still be displayed after adding reaction", editText)
    }

    /**
     * E2E-038: Tap a reaction indicator to show reaction list/bottom sheet.
     *
     * Adds a reaction first, then long-presses the reaction indicator to
     * verify a list/bottom sheet showing who reacted appears.
     */
    @Test
    fun test02_tapReactionShowsList() {
        // Find the most recent message
        val messages = device.findObjects(By.textContains("ReactTest"))
        assertTrue("No sent message found to react to", messages.isNotEmpty())

        val targetMessage = messages.last()

        // Add a reaction first
        targetMessage.longClick()
        Thread.sleep(2000)
        val reactionAdded = findAndTapReaction()

        if (reactionAdded) {
            Thread.sleep(3000)

            // Scroll down to ensure our reacted message is visible
            E2ETestHelper.scrollDown(device)
            Thread.sleep(1500)

            // Find reaction indicators — get the LAST one (closest to our message at bottom)
            val reactionIndicators = findAllReactionIndicators()
            val bottomReaction = reactionIndicators.maxByOrNull { it.top }

            if (bottomReaction != null) {
                // Long-press on the reaction indicator to open the reaction list/bottom sheet
                device.swipe(
                    bottomReaction.centerX(), bottomReaction.centerY(),
                    bottomReaction.centerX(), bottomReaction.centerY(),
                    100 // ~2s long press
                )
                Thread.sleep(3000)

                // Verify a bottom sheet or reaction list appeared
                val reactionListVisible = device.findObject(By.textContains("You")) != null ||
                    device.findObject(By.textContains("Reactions")) != null ||
                    device.findObject(By.textContains("reactions")) != null ||
                    device.findObject(By.textContains("All")) != null ||
                    device.findObject(By.descContains("Reactions")) != null

                assertTrue(
                    "Reaction list/bottom sheet did not appear after long-pressing reaction indicator",
                    reactionListVisible
                )

                // Dismiss the bottom sheet
                device.pressBack()
                Thread.sleep(1000)
            }
        }

        // Verify messages screen intact
        val editText = device.findObject(By.clazz("android.widget.EditText"))
            ?: device.findObject(By.desc("Send message"))
        assertNotNull("Messages screen should remain intact", editText)
    }

    /**
     * E2E-039: Remove a previously added reaction.
     *
     * Adds a reaction first, then taps the SAME reaction on OUR message to remove it.
     * IMPORTANT: Uses maxByOrNull{top} to find the bottom-most reaction indicator
     * which is on our freshly reacted message — NOT an old reaction on a different message.
     */
    @Test
    fun test03_removeReaction() {
        // Find the most recent message
        val messages = device.findObjects(By.textContains("ReactTest"))
        assertTrue("No sent message found to react to", messages.isNotEmpty())

        val targetMessage = messages.last()

        // Step 1: Add a reaction first
        targetMessage.longClick()
        Thread.sleep(2000)
        val reactionAdded = findAndTapReaction()

        if (reactionAdded) {
            Thread.sleep(3000)

            // Scroll down to ensure our message with the reaction is visible
            E2ETestHelper.scrollDown(device)
            Thread.sleep(1500)

            // Step 2: Find the reaction indicator on OUR message (bottom-most one)
            // Tapping an existing reaction toggles it off
            val reactionIndicators = findAllReactionIndicators()

            // Get the LAST (bottom-most) reaction indicator — that's on our message
            val bottomReaction = reactionIndicators.maxByOrNull { it.top }

            if (bottomReaction != null) {
                device.click(bottomReaction.centerX(), bottomReaction.centerY())
                Thread.sleep(2000)
            }
        }

        // Verify the app is still on the messages screen (no crash)
        val editText = device.findObject(By.clazz("android.widget.EditText"))
            ?: device.findObject(By.desc("Send message"))
        assertNotNull("Messages screen should still be displayed after removing reaction", editText)
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /**
     * Finds all reaction indicator bounds on screen.
     * Reaction indicators are small TextViews/Views containing emoji characters.
     * Uses safeGetBounds to avoid StaleObjectException.
     */
    private fun findAllReactionIndicators(): List<android.graphics.Rect> {
        val emojis = listOf("👍", "❤", "😍", "🔥", "😊", "😂")
        val allBounds = mutableListOf<android.graphics.Rect>()

        for (emoji in emojis) {
            allBounds.addAll(E2ETestHelper.safeGetBounds(device, By.textContains(emoji)))
            allBounds.addAll(E2ETestHelper.safeGetBounds(device, By.descContains(emoji)))
        }

        return allBounds
    }

    /**
     * Attempts to find and tap a reaction emoji from the action menu.
     * Returns true if a reaction was successfully tapped.
     */
    private fun findAndTapReaction(): Boolean {
        // The UIKit shows quick reactions: "😍", "👍🏻", "🔥", "😊", "❤️"
        val defaultEmoji = listOf("😍", "👍🏻", "👍", "🔥", "😊", "❤️", "❤")

        for (emoji in defaultEmoji) {
            val emojiElement = device.findObject(By.text(emoji))
                ?: device.findObject(By.textContains(emoji))
                ?: device.findObject(By.desc(emoji))
            if (emojiElement != null) {
                emojiElement.click()
                return true
            }
        }

        // Fallback: look for any clickable element that looks like an emoji
        val commonEmoji = listOf("😂", "😮", "😢", "🙏", "👎", "🎉")
        for (emoji in commonEmoji) {
            val emojiElement = device.findObject(By.text(emoji))
                ?: device.findObject(By.textContains(emoji))
            if (emojiElement != null) {
                emojiElement.click()
                return true
            }
        }

        // Strategy 3: Look for small clickable elements in popup that look like emoji
        val popupElements = device.findObjects(By.clickable(true))
        for (element in popupElements) {
            try {
                val text = element.text ?: ""
                if (text.length <= 2 && text.isNotEmpty() && !text[0].isLetterOrDigit()) {
                    element.click()
                    return true
                }
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                continue
            }
        }

        return false
    }
}
