package com.cometchat.sampleapp.kotlin.e2e.realtime

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.RestApiHelper
import org.junit.Before

/**
 * Shared base for single-emulator REST-driven real-time tests (sample-app-kotlin).
 *
 * The app stays logged in as [appUid] with a live WebSocket; "partner" actions ([partnerUid],
 * [secondPartnerUid]) are performed over the CometChat REST API via [RestApiHelper], so the
 * server pushes the resulting events to the app and the UI updates in real time.
 *
 * Args (all optional, sensible defaults):
 *   -e partnerUid <uid>   (default d1)
 *   -e partner2Uid <uid>  (default d2)
 */
abstract class RealtimeTestBase {

    protected lateinit var device: UiDevice

    protected val appUid: String get() = E2ETestConfig.LOGGED_IN_UID
    protected val partnerUid: String get() = E2ETestConfig.ONE_TO_ONE_UID
    protected val secondPartnerUid: String get() = E2ETestConfig.MULTI_MEMBER_UID

    @Before
    open fun baseSetup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device, uid = appUid)
    }

    // ─── Opening chats ─────────────────────────────────────────────────────────

    /** Seeds a 1:1 message so the partner conversation tops the Chats list, then opens it. */
    protected fun openPartnerChat() {
        RestApiHelper.sendMessage(partnerUid, appUid, "seed_${System.currentTimeMillis()}")
        Thread.sleep(2500)
        device.waitForIdle()
        E2ETestHelper.navigateToTab(device, "Chats")
        E2ETestHelper.openFirstConversation(device)
    }

    /** Ensures the test group exists with [appUid] as owner/member. Idempotent. */
    protected fun ensureGroup(guid: String, name: String) {
        RestApiHelper.createGroupIfAbsent(owner = appUid, guid = guid, name = name)
    }

    /** Opens the group chat: seeds a group message so it tops the Chats list, then opens it. */
    protected fun openGroupChat(guid: String) {
        RestApiHelper.sendMessage(appUid, guid, "seed_${System.currentTimeMillis()}", receiverType = "group")
        Thread.sleep(2500)
        device.waitForIdle()
        E2ETestHelper.navigateToTab(device, "Chats")
        E2ETestHelper.openFirstConversation(device)
    }

    /**
     * Verifies a conversation-list preview shows [snippet]. Falls back to toggling tabs to force
     * the (sometimes lagging) conversation list to re-render before re-checking.
     */
    protected fun verifyConversationPreview(snippet: String): Boolean {
        if (pollForConversationPreview(snippet, 30_000)) return true
        E2ETestHelper.navigateToTab(device, "Users"); Thread.sleep(1500)
        E2ETestHelper.navigateToTab(device, "Chats"); Thread.sleep(1500)
        return pollForConversationPreview(snippet, 30_000)
    }

    /**
     * Verifies the group [guid]'s header member count changed in the expected direction relative
     * to [before]. Polls the real-time header update first; if missed (the update can race the
     * header listener subscription), re-opens the chat to read the authoritative current count.
     */
    protected fun verifyMemberCountChange(guid: String, before: Int?, wantIncrease: Boolean): Boolean {
        fun cmp(now: Int?): Boolean = when {
            now == null -> false
            before == null -> wantIncrease && now >= 2
            wantIncrease -> now > before
            else -> now < before
        }
        if (poll(25_000) { cmp(groupMemberCount()) }) return true
        openGroupChat(guid)
        Thread.sleep(2000)
        return cmp(groupMemberCount())
    }

    // ─── Polling ───────────────────────────────────────────────────────────────

    /** Polls [cond] until true or [timeoutMs] elapses. Exceptions in [cond] count as false. */
    protected fun poll(timeoutMs: Long, intervalMs: Long = 1500, cond: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            try {
                if (cond()) return true
            } catch (_: Exception) { /* retry */ }
            Thread.sleep(intervalMs)
        }
        return false
    }

    /** Polls until any on-screen node contains [snippet]. */
    protected fun pollForText(snippet: String, timeoutMs: Long = 30_000): Boolean =
        poll(timeoutMs) { device.findObjects(By.textContains(snippet)).isNotEmpty() }

    /** Polls until NO on-screen node contains [snippet] (e.g. typing indicator cleared). */
    protected fun pollForTextGone(snippet: String, timeoutMs: Long = 30_000): Boolean =
        poll(timeoutMs) { device.findObjects(By.textContains(snippet)).isEmpty() }

    /**
     * Polls for [snippet] in the open chat. The Kotlin (RecyclerView) message list auto-scrolls
     * to new messages, so no manual scrolling is needed here (unlike Compose).
     */
    protected fun pollForMessageInChat(snippet: String, timeoutMs: Long): Boolean =
        pollForText(snippet, timeoutMs)

    /**
     * Polls until ANY of [snippets] appears in the open chat, scrolling toward the bottom each
     * iteration. Group ACTION messages (added/kicked) and thread-reply indicators land at the
     * newest position but are not always auto-scrolled into view, so we nudge to the bottom.
     */
    protected fun pollForAnyMessageInChat(snippets: List<String>, timeoutMs: Long): Boolean =
        poll(timeoutMs) {
            if (snippets.any { device.findObjects(By.textContains(it)).isNotEmpty() }) return@poll true
            E2ETestHelper.scrollDown(device)
            snippets.any { device.findObjects(By.textContains(it)).isNotEmpty() }
        }

    /** Polls for [snippet] in the conversation list preview (no scroll needed on Kotlin). */
    protected fun pollForConversationPreview(snippet: String, timeoutMs: Long): Boolean =
        pollForText(snippet, timeoutMs)

    /**
     * Ensures the latest message's reaction chip is visible. On Kotlin the RecyclerView keeps
     * the newest message (and its reaction) at the bottom, so this is a no-op.
     */
    protected fun revealLatestForReaction() { /* no-op on Kotlin */ }

    /**
     * Reads the member count shown in the group header (e.g. "3 members"), or null if not found.
     * The header updates this in real time on member add/kick (CometChatMessageHeaderViewModel).
     */
    protected fun groupMemberCount(): Int? {
        val re = Regex("(\\d+)\\s*[Mm]ember")
        for (o in device.findObjects(By.textContains("ember"))) {
            try {
                val t = o.text ?: continue
                re.find(t)?.let { return it.groupValues[1].toInt() }
            } catch (_: Exception) { /* stale — skip */ }
        }
        return null
    }
}
