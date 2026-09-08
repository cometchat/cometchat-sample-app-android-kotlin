package com.cometchat.sampleapp.kotlin.e2e.helpers

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Extras for the ENG-38902 pin/save/thread regression suite, on top of [E2ETestHelper]:
 * SDK-side seeding/cleanup (pin, unpin, list pinned), a Toast reader, and bubble-scoped
 * indicator checks. Credentials and hosts come from [E2ETestConfig] exactly as for every other
 * suite — nothing here overrides them.
 */
object PinSaveRegressionHelper {
    const val TAG = "E2E"

    fun log(msg: String) = Log.i(TAG, msg)

    // ─── SDK ─────────────────────────────────────────────────────────────────────

    fun userName(uid: String): String = E2ETestHelper.getUserName(uid)

    /** Sends a 1-1 text via the SDK (fast seeding). Returns the server message. */
    fun sendViaSdk(receiverUid: String, text: String): BaseMessage {
        val latch = CountDownLatch(1)
        var sent: BaseMessage? = null
        var error: String? = null
        CometChat.sendMessage(TextMessage(receiverUid, text, CometChatConstants.RECEIVER_TYPE_USER),
            object : CometChat.CallbackListener<TextMessage>() {
                override fun onSuccess(p0: TextMessage?) { sent = p0; latch.countDown() }
                override fun onError(e: CometChatException?) { error = "${e?.code}: ${e?.message}"; latch.countDown() }
            })
        latch.await(20, TimeUnit.SECONDS)
        if (error != null || sent == null) throw RuntimeException("sendMessage failed: $error")
        return sent!!
    }

    /** Outcome of a raw SDK pin/unpin/save/unsave call. */
    data class SdkResult(val message: BaseMessage?, val error: CometChatException?) {
        val ok: Boolean get() = error == null && message != null
    }

    private fun pinSave(call: (CometChat.CallbackListener<BaseMessage>) -> Unit): SdkResult {
        val latch = CountDownLatch(1)
        var result = SdkResult(null, CometChatException("TIMEOUT", "no callback"))
        call(object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(p0: BaseMessage?) { result = SdkResult(p0, null); latch.countDown() }
            override fun onError(e: CometChatException?) { result = SdkResult(null, e); latch.countDown() }
        })
        latch.await(20, TimeUnit.SECONDS)
        return result
    }

    fun pinViaSdk(id: Long) = pinSave { CometChat.pinMessage(id, it) }
    fun unpinViaSdk(id: Long) = pinSave { CometChat.unpinMessage(id, it) }
    fun unsaveViaSdk(id: Long) = pinSave { CometChat.unsaveMessage(id, it) }

    /** Every pinned message in the 1-1 with [uid] (first page, up to 50). */
    fun pinnedViaSdk(uid: String): List<BaseMessage> {
        val latch = CountDownLatch(1)
        var list: List<BaseMessage> = emptyList()
        MessagesRequest.MessagesRequestBuilder().setUID(uid).setPinned(true).setLimit(50).build()
            .fetchNext(object : CometChat.CallbackListener<List<BaseMessage>>() {
                override fun onSuccess(p0: List<BaseMessage>?) { list = p0 ?: emptyList(); latch.countDown() }
                override fun onError(e: CometChatException?) { latch.countDown() }
            })
        latch.await(20, TimeUnit.SECONDS)
        return list
    }

    // ─── Navigation / gestures ───────────────────────────────────────────────────

    /** Users tab → filter by the partner's name → open the 1-1 chat. */
    fun openChatWith(device: UiDevice, uid: String = E2ETestConfig.ONE_TO_ONE_UID) {
        val name = userName(uid)
        E2ETestHelper.navigateToTab(device, "Users")
        // The first page can take a while on a slow backend; only fall back to the (server-side,
        // slower) search when the user isn't already listed.
        var row = device.wait(Until.findObject(By.text(name)), TIMEOUT)
        if (row == null) {
            val search = device.wait(Until.findObject(By.clazz("android.widget.EditText")), TIMEOUT)
            assertNotNull("Users search field not found", search)
            search!!.click(); Thread.sleep(300)
            search.text = name
            row = device.wait(Until.findObject(By.text(name)), 45_000L) ?: device.findObject(By.textContains(name))
        }
        assertNotNull("User '$name' not listed in Users", row)
        // Tapping the name label is enough — the row forwards the click.
        tap(device, row!!)
        assertNotNull("Message composer not shown", E2ETestHelper.waitForComposer(device))
        Thread.sleep(E2ETestHelper.SETTLE_TIME)
    }

    fun uniqueText(prefix: String) = "$prefix ${System.currentTimeMillis() % 1_000_000}"

    /** Sends via the composer and waits for the bubble to render. */
    fun sendAndAwait(device: UiDevice, text: String) {
        E2ETestHelper.sendMessage(device, text)
        assertTrue("Message '$text' did not appear", device.wait(Until.hasObject(By.textContains(text)), TIMEOUT))
        Thread.sleep(E2ETestHelper.SETTLE_TIME)
    }

    /** Long-presses the bubble showing [text]: the suite's shell long-touch first, then longClick. */
    fun longPress(device: UiDevice, text: String) {
        E2ETestHelper.longPressMessage(device, text)
        if (!isAnyOptionVisible(device)) {
            device.findObject(By.textContains(text))?.longClick()
            Thread.sleep(2000)
        }
    }

    fun isAnyOptionVisible(device: UiDevice): Boolean =
        listOf("Pin message", "Unpin message", "Save message", "Unsave message", "Copy", "Delete", "Reply In Thread", "Edit")
            .any { device.findObject(By.text(it)) != null }

    fun findOption(device: UiDevice, label: String): UiObject2? =
        device.findObject(By.text(label)) ?: device.findObject(By.textContains(label))

    fun tap(device: UiDevice, node: UiObject2) {
        val b = node.visibleBounds
        device.click(b.centerX(), b.centerY())
        Thread.sleep(E2ETestHelper.SETTLE_TIME)
    }

    /**
     * Taps an action-sheet option. The sheet is paginated: the first page shows a handful of
     * options plus "More"; Pin/Save/Translate/Info sit on the next page, so open "More" when the
     * option isn't on the page that's showing.
     */
    fun tapOption(device: UiDevice, label: String) {
        var node = findOption(device, label)
        if (node == null) {
            findOption(device, "More")?.let { more ->
                tap(device, more)
                node = findOption(device, label)
            }
        }
        assertNotNull("Option '$label' not in the action sheet (checked the 'More' page too)", node)
        tap(device, node!!)
    }

    /**
     * Whether the bubble showing [text] carries the footer glyph with contentDescription [desc]
     * ("Pinned" / "Saved"). Walks up from the text node so glyphs on OTHER bubbles don't count.
     */
    fun bubbleHasIndicator(device: UiDevice, text: String, desc: String): Boolean {
        repeat(3) {
            try {
                var node: UiObject2? = device.findObject(By.textContains(text)) ?: return false
                repeat(7) {
                    node = node?.parent ?: return false
                    if (node!!.findObject(By.desc(desc)) != null) return true
                }
                return false
            } catch (_: StaleObjectException) { Thread.sleep(500) }
        }
        return false
    }

    fun waitForBubbleIndicator(device: UiDevice, text: String, desc: String, timeout: Long = SHORT_TIMEOUT): Boolean {
        val deadline = System.currentTimeMillis() + timeout
        while (System.currentTimeMillis() < deadline) {
            if (bubbleHasIndicator(device, text, desc)) return true
            Thread.sleep(500)
        }
        return false
    }

    /**
     * Runs [action] and returns the text of the first Toast it raises (Toasts surface as
     * TYPE_NOTIFICATION_STATE_CHANGED accessibility events), or `null` if none within [timeoutMs].
     */
    fun captureToast(action: () -> Unit, timeoutMs: Long = 6_000L): String? {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        var text: String? = null
        return try {
            automation.executeAndWaitForEvent(
                { action() },
                { event ->
                    if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                        val t = event.text?.joinToString(" ") ?: ""
                        if (t.isNotBlank()) { text = t; true } else false
                    } else false
                },
                timeoutMs
            )
            text
        } catch (_: TimeoutException) {
            null
        }
    }

    fun waitFor(device: UiDevice, selector: BySelector, timeout: Long = TIMEOUT): UiObject2? =
        device.wait(Until.findObject(selector), timeout)
}
