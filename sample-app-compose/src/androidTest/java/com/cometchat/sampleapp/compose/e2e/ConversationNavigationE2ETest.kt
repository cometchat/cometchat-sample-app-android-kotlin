package com.cometchat.sampleapp.compose.e2e

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SETTLE_TIME
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for message-list ⇄ conversation-list navigation resilience in the Compose
 * sample app.
 *
 * Test IDs:
 * - ENG-37363 test01: after a home-button background/foreground cycle on the message
 *   list, navigating back via the header arrow must land on the conversation list
 *   without crashing. Regression: the foreground reconnect refresh used to reset the
 *   conversations pagination request, so the next load-more re-fetched page 1 and
 *   appended duplicate conversationIds — crashing the list on duplicate LazyColumn keys.
 *   The test scrolls the list to the bottom after coming back to force that pagination
 *   fetch, and logs each step to logcat under tag "ENG37363-E2E".
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.ConversationNavigationE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ConversationNavigationE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
        // We start on the Chats tab (default after login)
    }

    @Test
    fun test01_backToConversationListAfterBackgroundForeground() {
        // Step 1: open the first conversation in the Chats tab.
        step("1. Opening first conversation")
        val firstConversation = findFirstConversationItem()
        assertNotNull("No conversation item found in the list", firstConversation)
        firstConversation!!.click()

        // Step 2: message list is showing (header back arrow present).
        step("2. Waiting for message list")
        val onMessageList = device.wait(Until.hasObject(By.desc("Back")), TIMEOUT)
        assertTrue("Message list did not open (no header back arrow)", onMessageList)
        Thread.sleep(SETTLE_TIME)

        // Step 3: real home-button press — the app backgrounds and the WebSocket drops.
        step("3. Pressing device HOME button")
        device.pressHome()
        device.waitForIdle()
        Thread.sleep(SETTLE_TIME)

        // Step 4: bring the app back to the foreground like tapping the app icon.
        // NEW_TASK only — no CLEAR_TASK, the existing message list must be RESUMED,
        // which reconnects the WebSocket and triggers the conversations refresh.
        step("4. Relaunching app from launcher (bring to foreground)")
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = requireNotNull(context.packageManager.getLaunchIntentForPackage(PACKAGE))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), TIMEOUT)

        // Step 5: confirm we are back on the message list.
        step("5. Verifying message list restored after foreground")
        assertTrue(
            "Message list was not restored after foregrounding",
            device.wait(Until.hasObject(By.desc("Back")), TIMEOUT)
        )
        Thread.sleep(SETTLE_TIME)

        // Step 6: navigate back via the message header arrow (the QA repro path).
        step("6. Tapping message header back arrow")
        device.findObject(By.desc("Back"))?.click()

        // Step 7: conversation list is showing again.
        step("7. Waiting for conversation list")
        assertTrue(
            "Conversation list did not reappear after back navigation",
            device.wait(Until.hasObject(By.scrollable(true)), TIMEOUT)
        )

        // Step 8: scroll to the bottom to force the pagination fetch — the spot where
        // the reconnect-reset request used to append duplicate conversations.
        step("8. Scrolling conversation list to bottom to trigger pagination")
        repeat(3) {
            E2ETestHelper.scrollDown(device)
            Thread.sleep(500)
        }

        // Step 9: the crash landed within a few seconds in the QA repro — hold, then
        // verify the app is still in the foreground with its UI intact.
        step("9. Waiting for delayed crash")
        Thread.sleep(SETTLE_TIME)
        assertTrue(
            "App is no longer in the foreground — it crashed after back navigation",
            device.hasObject(By.pkg(PACKAGE).depth(0))
        )
        assertTrue(
            "Conversation list UI is gone after back navigation",
            device.hasObject(By.scrollable(true))
        )
        step("Done — no crash")
    }

    /** Finds the first conversation row using the same strategy chain as ConversationsE2ETest. */
    private fun findFirstConversationItem(): androidx.test.uiautomator.UiObject2? {
        Thread.sleep(SETTLE_TIME)

        val scrollable = device.wait(Until.findObject(By.scrollable(true)), TIMEOUT)
        if (scrollable != null) {
            val clickableChildren = scrollable.findObjects(By.clickable(true))
            if (clickableChildren.isNotEmpty()) return clickableChildren[0]
        }

        val allClickables = device.findObjects(By.clickable(true))
        for (clickable in allClickables) {
            val bounds = clickable.visibleBounds
            if (bounds.top > 150 && bounds.bottom < device.displayHeight - 150) {
                return clickable
            }
        }
        return null
    }

    /** Logs a step marker to logcat so failures can be pinned to an exact step. */
    private fun step(message: String) {
        Log.d("ENG37363-E2E", message)
    }
}
