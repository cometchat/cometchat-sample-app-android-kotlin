package com.cometchat.sampleapp.kotlin.e2e.realtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import com.cometchat.sampleapp.kotlin.e2e.helpers.CometChatJsDriver
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.RestApiHelper
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Single-emulator real-time EDGE / robustness E2E tests (sample-app-kotlin).
 *
 * These reframe the "precise concurrency" sheet rows (un-doable sub-millisecond timing on one
 * emulator) as STRESS / NO-CRASH tests: hammer the action repeatedly while real-time events fire,
 * and assert the app stays alive (no crash/ANR, a known UI anchor present).
 *
 * Covers: RT-EDGE-002 (edit while long-press), RT-EDGE-003 (delete conversation while a message
 * arrives), RT-EDGE-005 (rapid typing start/stop), RT-EDGE-006 (switch chats rapidly while
 * messages arrive).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RealTimeEdgeRobustnessE2ETest : RealtimeTestBase() {

    private val partnerName: String by lazy { E2ETestHelper.getUserName(partnerUid) }

    @After
    fun stopDriver() {
        CometChatJsDriver.stop()
    }

    // ─── RT-EDGE-002: Edit while peer long-presses ────────────────────────────────
    @Test
    fun test01_editWhilePeerLongPresses() {
        // Cold-start (first test): openPartnerChat's navigateToTab can race the first home render,
        // so retry the open a few times before giving up.
        var opened = false
        for (attempt in 0 until 3) {
            try { openPartnerChat(); opened = true; break }
            catch (e: Throwable) { Thread.sleep(4000) }
        }
        assertTrue("Could not open the partner chat after retries", opened)

        var menuAppeared = 0
        repeat(3) { i ->
            // NOTE: tag has NO underscores — CometChat markdown treats _paired_ underscores as
            // italic and strips them, so "X_123_0" renders as "X1230" and wouldn't match.
            val tag = "EditLP${System.currentTimeMillis()}x$i"
            val id = RestApiHelper.sendMessage(partnerUid, appUid, tag)
            assertTrue("Message $i should be visible", pollForMessageInChat(tag, 45_000))

            E2ETestHelper.longPressMessage(device, tag) // proven long-press gesture (see OneToOneActionsComposerE2ETest)
            Thread.sleep(1200)
            if (actionMenuShown()) menuAppeared++
            // The peer edits the message while its action menu is open (the concurrency).
            RestApiHelper.editMessage(partnerUid, id, "${tag}edited")
            Thread.sleep(1200)
            device.pressBack(); Thread.sleep(1000) // dismiss the action menu (back in chat)
        }
        assertTrue("The long-press action menu should appear at least once", menuAppeared >= 1)

        // No-crash check: after the stress we're still in the chat — verify the app is alive
        // (appAlive() passes on the composer EditText; no navigation needed).
        assertTrue("App should stay alive after the edit/long-press stress", poll(10_000) { appAlive() })
    }

    // ─── RT-EDGE-003: Delete conversation while a message arrives ──────────────────
    @Test
    fun test02_deleteConversationWhileMessageArrives() {
        openPartnerChat()
        device.pressBack(); Thread.sleep(1500)
        E2ETestHelper.navigateToTab(device, "Chats")
        Thread.sleep(1500)

        // Fire a message AND delete the conversation around the same moment.
        RestApiHelper.sendMessage(partnerUid, appUid, "DelRace_${System.currentTimeMillis()}")
        device.findObject(By.textContains(partnerName.take(8)))?.longClick()
        Thread.sleep(1200)
        (device.findObject(By.text("Delete")) ?: device.findObject(By.textContains("Delete")))?.click()
        Thread.sleep(1000)
        (device.findObject(By.text("Delete"))
            ?: device.findObject(By.textContains("Confirm"))
            ?: device.findObject(By.textContains("Yes")))?.click()
        Thread.sleep(2500)

        // Another message arrives after the delete (the conversation may reappear — also valid).
        RestApiHelper.sendMessage(partnerUid, appUid, "DelRace2_${System.currentTimeMillis()}")
        Thread.sleep(3000)

        assertTrue("App should stay stable through delete-while-message-arrives", appAlive())
    }

    // ─── RT-EDGE-005: Rapid typing start/stop ─────────────────────────────────────
    @Test
    fun test03_rapidTypingStartStop() {
        openPartnerChat()
        val ok = CometChatJsDriver.start(uid = partnerUid, action = "presence", receiver = appUid, receiverType = "user")
        assertTrue("Driver should log in for rapid typing", ok)

        CometChatJsDriver.rapidTyping(receiver = appUid, receiverType = "user", count = 15)
        Thread.sleep(6000) // let the rapid toggles play out

        assertTrue("App should stay alive under rapid typing toggles", appAlive())
        assertTrue("Typing indicator should be cleared after rapid toggling", pollForTextGone("yping", 20_000))
    }

    // ─── RT-EDGE-006: Switch chats rapidly while messages arrive ──────────────────
    @Test
    fun test04_switchChatsRapidlyWhileMessagesArrive() {
        openPartnerChat()
        device.pressBack(); Thread.sleep(1000)

        repeat(6) { i ->
            RestApiHelper.sendMessage(partnerUid, appUid, "Switch_${System.currentTimeMillis()}_$i")
            E2ETestHelper.openFirstConversation(device)
            Thread.sleep(800)
            device.pressBack()
            Thread.sleep(800)
            assertTrue("App should stay alive while switching chats (iter $i)", appAlive())
        }
        assertTrue("App should stay alive after rapid chat switching", appAlive())
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    /** True if a message action menu (Copy/Reply/Message Info) is currently shown. */
    private fun actionMenuShown(): Boolean =
        device.findObject(By.textContains("Copy")) != null ||
            device.findObject(By.textContains("Reply")) != null ||
            device.findObject(By.textContains("Message Info")) != null ||
            device.findObject(By.textContains("Delete")) != null

    /** Liveness: no crash/ANR dialog AND a known app UI anchor is present. */
    private fun appAlive(): Boolean {
        if (device.findObject(By.textContains("isn't responding")) != null) return false
        if (device.findObject(By.textContains("keeps stopping")) != null) return false
        if (device.findObject(By.textContains("has stopped")) != null) return false
        return device.findObject(By.res(E2ETestHelper.PACKAGE, "bottomNavigationView")) != null ||
            device.findObject(By.res(E2ETestHelper.PACKAGE, "messageList")) != null ||
            device.findObject(By.clazz("android.widget.EditText")) != null
    }
}
