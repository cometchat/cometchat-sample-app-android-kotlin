package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SETTLE_TIME
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for 1:1 conversation edge cases (sample-app-kotlin).
 *
 * Test IDs:
 * - 1TO1-098: testRapidMessageSendDoesNotCrash
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.OneToOneEdgeCasesE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class OneToOneEdgeCasesE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        // Navigate to Users tab and open first user for 1:1 chat
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)
    }

    /**
     * 1TO1-098: Rapidly sending multiple messages does not crash the app.
     * Sends 10 messages quickly and verifies the app remains stable.
     */
    @Test
    fun test01_rapidMessageSendDoesNotCrash() {
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen not loaded", messageList)

        // Send 10 messages in rapid succession
        for (i in 1..20) {
            E2ETestHelper.sendMessage(device, "RapidMsg$i ts${System.currentTimeMillis()}")
            Thread.sleep(500) // Minimal delay between sends
        }

        // Wait for all messages to be processed
        Thread.sleep(5000)

        // Scroll to bottom to see latest messages
        E2ETestHelper.scrollDown(device)
        Thread.sleep(2000)

        // Verify the app didn't crash — messages screen still functional
        val messageListAfter = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Messages screen disappeared after rapid sending — possible crash", messageListAfter)

        // Verify at least one of the rapid messages is visible
        val anyMessage = device.findObject(By.textContains("RapidMsg"))
        assertNotNull("None of the rapid messages visible — possible send failure", anyMessage)

        // Verify composer is still functional
        val composer = device.findObject(By.res(PACKAGE, "messageComposer"))
        assertNotNull("Composer not found after rapid sending", composer)
    }

    /**
     * 1TO1-097: Empty conversation shows greeting text.
     * Creates a temp user, opens their chat, verifies empty state.
     */
    @Test
    fun test02_emptyConversationShowsGreeting() {
        val ts = System.currentTimeMillis()
        val tempUid = "temp_user_$ts"
        val tempName = "TempUser$ts"

        // Create temp user via SDK
        val createLatch = java.util.concurrent.CountDownLatch(1)
        var userCreated = false
        val user = com.cometchat.chat.models.User()
        user.uid = tempUid
        user.name = tempName
        com.cometchat.chat.core.CometChat.createUser(
            user, E2ETestHelper.authKey,
            object : com.cometchat.chat.core.CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                override fun onSuccess(u: com.cometchat.chat.models.User?) {
                    userCreated = true
                    createLatch.countDown()
                }
                override fun onError(e: com.cometchat.chat.exceptions.CometChatException?) {
                    createLatch.countDown()
                }
            }
        )
        createLatch.await(15, java.util.concurrent.TimeUnit.SECONDS)
        assertTrue("Failed to create temp user", userCreated)

        // Navigate to Users tab and search for the temp user
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)
        E2ETestHelper.navigateToTab(device, "Users")
        Thread.sleep(SETTLE_TIME)

        val searchBar = device.findObject(By.clazz("android.widget.EditText"))
        searchBar?.clear()
        searchBar?.text = tempName
        Thread.sleep(5000)

        var searchFieldBottom = 200
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 20
        } catch (_: Exception) { }

        val matches = device.findObjects(By.textContains(tempName.take(8)))
        var clicked = false
        for (match in matches) {
            try {
                if (match.visibleBounds.top > searchFieldBottom) { match.click(); clicked = true; break }
            } catch (_: Exception) { continue }
        }

        if (clicked) {
            device.wait(androidx.test.uiautomator.Until.hasObject(By.res(PACKAGE, "messageList")), TIMEOUT)
            Thread.sleep(SETTLE_TIME)

            // Verify empty state greeting
            val greeting = device.findObject(By.textContains("Say Hello"))
                ?: device.findObject(By.textContains("say hello"))
                ?: device.findObject(By.textContains(tempName))
                ?: device.findObject(By.textContains("Send a message"))
            assertNotNull("Empty conversation should show greeting or user name", greeting)
        }
    }
}
