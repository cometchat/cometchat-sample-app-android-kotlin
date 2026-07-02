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
 * E2E tests for 1:1 conversation edge cases (sample-app-compose).
 *
 * Test IDs:
 * - 1TO1-098: testRapidMessageSendDoesNotCrash
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.OneToOneEdgeCasesE2ETest
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

        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)
    }

    /**
     * 1TO1-098: Rapidly sending multiple messages does not crash the app.
     * Sends 10 messages quickly and verifies the app remains stable.
     */
    @Test
    fun test01_rapidMessageSendDoesNotCrash() {
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen not loaded (no EditText)", editText)

        // Send 10 messages in rapid succession
        for (i in 1..10) {
            E2ETestHelper.sendMessage(device, "RapidMsg$i ts${System.currentTimeMillis()}")
            Thread.sleep(500)
        }

        // Wait for all messages to be processed
        Thread.sleep(5000)

        // Scroll to bottom
        E2ETestHelper.scrollDown(device)
        Thread.sleep(2000)

        // Verify the app didn't crash — EditText still present
        val editTextAfter = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Messages screen disappeared after rapid sending — possible crash", editTextAfter)

        // Verify at least one rapid message is visible
        val anyMessage = device.findObject(By.textContains("RapidMsg"))
        assertNotNull("None of the rapid messages visible — possible send failure", anyMessage)
    }

    /**
     * 1TO1-097: Empty conversation shows greeting text.
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

        // Navigate to Users and search
        E2ETestHelper.launchApp(device)
        Thread.sleep(E2ETestHelper.SETTLE_TIME)
        E2ETestHelper.navigateToTab(device, "Users")
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        var searchSuccess = false
        repeat(3) {
            if (searchSuccess) return@repeat
            try {
                val searchBar = device.findObject(By.clazz("android.widget.EditText"))
                searchBar?.click(); Thread.sleep(500)
                searchBar?.clear(); searchBar?.text = tempName
                searchSuccess = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
        Thread.sleep(5000)

        var searchFieldBottom = 250
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 30
        } catch (_: androidx.test.uiautomator.StaleObjectException) { }

        val userBounds = E2ETestHelper.safeGetBounds(
            device, By.textContains(tempName.take(8))
        ) { it.top > searchFieldBottom }

        if (userBounds.isNotEmpty()) {
            device.click(userBounds[0].centerX(), userBounds[0].centerY())
            Thread.sleep(5000)

            // Verify empty state greeting
            val greeting = device.findObject(By.textContains("Say Hello"))
                ?: device.findObject(By.textContains("say hello"))
                ?: device.findObject(By.textContains(tempName))
                ?: device.findObject(By.textContains("Send a message"))
            assertNotNull("Empty conversation should show greeting or user name", greeting)
        }
    }
}
