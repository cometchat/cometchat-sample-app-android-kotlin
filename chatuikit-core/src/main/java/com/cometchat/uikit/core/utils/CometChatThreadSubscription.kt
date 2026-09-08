package com.cometchat.uikit.core.utils

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatThreadEvent
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * The kit's thread-subscription controller: the write path, and the local mirrors of the server's
 * auto-subscribe behaviour.
 *
 * It holds **no subscription state**. State lives on the message ([isThreadSubscribed]); this object
 * only performs the server writes, publishes every change on [CometChatEvents.threadEvents] so open
 * surfaces stay in sync, and reconciles realtime replies that arrive with no flag.
 *
 * ### The five cases
 *
 * | Case | What happens | Who acts |
 * | --- | --- | --- |
 * | 1. Manual toggle (bell / message option) | Optimistic publish → server call → revert + error on failure | [toggle] — the only server write |
 * | 2. Author subscribed by default | The server subscribes a message's sender when they send it | [applyOwnMessageSent] / [applyIncomingMessageSubscription] stamp the realtime copies |
 * | 3. An incoming reply @-mentions me | The server subscribes me | [applyIncomingMessageSubscription] mirrors it locally |
 * | 4. I send a reply in a thread | The server subscribes me | [applyOwnMessageSent] from the send's success callback |
 * | 5. An edit leaves me @-mentioned in a reply | The server subscribes me | [applyEditedMessageSubscription], which also preserves state an edit would otherwise wipe |
 *
 * Cases 2–5 are **mirrors of server-side truths**, not writes: they publish and stamp for
 * immediate feedback but never call `subscribeToThread`. Where a mirror and a later fetched flag
 * disagree, the fetched flag wins.
 *
 * The server stamps `threadSubscribed` only on **fetched** messages (confirmed on the API side);
 * anything delivered in realtime — a send result, or an own message relayed to another device —
 * arrives flagless and must be stamped by the kit, which is what Case 2's mirrors do.
 */
object CometChatThreadSubscription {

    /** Swallows an impatient double-tap without blocking a deliberate retry after a failure. */
    private const val TOGGLE_DEBOUNCE_MS = 400L

    private val inFlight: MutableSet<Long> = Collections.synchronizedSet(mutableSetOf())

    /**
     * Last toggle time per thread, for the debounce above. Pruned on every write (see
     * [pruneExpiredToggles]) — this object outlives every screen, so without eviction it would
     * retain an entry for every thread the user ever toggled, for the life of the process.
     */
    private val lastToggleAt = ConcurrentHashMap<Long, Long>()

    /** The outcome of a [toggle], for the caller to turn into its own toast copy. */
    sealed class ToggleResult {
        /** The server write succeeded; [subscribed] is the settled state. */
        data class Success(val parentMessageId: Long, val subscribed: Boolean) : ToggleResult()

        /** The server write failed and the optimistic flip has already been reverted. */
        data class Failure(val parentMessageId: Long, val exception: CometChatException?) : ToggleResult()
    }

    /**
     * Whether the thread-subscription affordances should be offered at all. The feature is opt-in via
     * [com.cometchat.uikit.core.UIKitSettings.enableThreadSubscription].
     */
    fun isSupported(): Boolean = CometChatUIKit.isThreadSubscriptionEnabled()

    /**
     * Whether the follow control should be offered for the thread rooted at [parentMessage].
     *
     * **Both 1-1 and group conversations** (ENG-38903). Threads exist in a 1-1 too, so the
     * reply-notification control is offered wherever threading itself is — the same scope as
     * [REPLY_IN_THREAD][com.cometchat.uikit.core.constants.UIKitConstants.MessageOption.REPLY_IN_THREAD].
     * This keeps the header bell in step with the action-sheet option, which is gated the same way
     * in [com.cometchat.uikit.core.utils.MessageOptionsUtils]; gating one but not the other would
     * let a user subscribe from the sheet with no bell to unsubscribe from.
     *
     * Still hides on an un-sent root — a thread needs a server-assigned parent id — and whenever
     * the feature is unsupported.
     */
    fun isAvailableForThread(parentMessage: BaseMessage?): Boolean =
        isSupported() &&
            parentMessage != null &&
            parentMessage.id > 0L

    /** The id of the thread a message belongs to: its own id if a root, else its `parentMessageId`. */
    fun threadRootId(message: BaseMessage): Long =
        if (message.parentMessageId != 0L) message.parentMessageId else message.id

    /**
     * Case 1 — the manual toggle, and the only path that writes to the server.
     *
     * Publishes the desired state immediately so every open surface flips at once, then calls the
     * SDK. On failure it publishes the reverse before reporting, so a caller only has to render the
     * error. Both SDK calls are idempotent, so a duplicate can never error.
     *
     * Guarded by a per-thread in-flight lock and a short debounce; a call that is dropped by either
     * guard never invokes [onResult].
     *
     * @param message Any message in the thread — a root or one of its replies.
     * @param currentlySubscribed The state the caller is showing right now; the toggle targets its inverse.
     */
    fun toggle(
        message: BaseMessage,
        currentlySubscribed: Boolean,
        onResult: (ToggleResult) -> Unit = {}
    ) {
        val rootId = threadRootId(message)
        if (rootId <= 0L) return

        val now = System.currentTimeMillis()
        if (now - (lastToggleAt[rootId] ?: 0L) < TOGGLE_DEBOUNCE_MS) return
        if (!inFlight.add(rootId)) return
        pruneExpiredToggles(now)
        lastToggleAt[rootId] = now

        val desired = !currentlySubscribed
        // Optimistic: flip every surface now, undo below if the server refuses.
        publish(rootId, desired)

        val callback = object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                inFlight.remove(rootId)
                onResult(ToggleResult.Success(rootId, desired))
            }

            override fun onError(e: CometChatException?) {
                inFlight.remove(rootId)
                publish(rootId, currentlySubscribed)
                onResult(ToggleResult.Failure(rootId, e))
            }
        }

        if (desired) CometChat.subscribeToThread(rootId, callback)
        else CometChat.unsubscribeFromThread(rootId, callback)
    }

    /**
     * Drops debounce entries that can no longer suppress anything (older than the window), so the
     * map stays proportional to threads toggled in the last [TOGGLE_DEBOUNCE_MS] rather than to
     * every thread toggled since process start.
     */
    private fun pruneExpiredToggles(now: Long) {
        if (lastToggleAt.isEmpty()) return
        lastToggleAt.entries.removeAll { now - it.value >= TOGGLE_DEBOUNCE_MS }
    }

    /**
     * Mirrors a subscription the **server has already made** onto the local surfaces — Cases 3 and 4.
     *
     * This publishes only; it never calls `subscribeToThread`, which would re-issue a redundant write
     * for a subscription that already exists. The next fetch of the parent confirms it.
     */
    fun mirrorSubscribed(parentMessageId: Long, subscribed: Boolean = true) {
        if (parentMessageId <= 0L) return
        publish(parentMessageId, subscribed)
    }

    /**
     * Cases 2 and 4 — the logged-in user sent a message, and the server subscribed them to its thread.
     *
     * Call this from every send's **success** path with the resolved result, which the server returns
     * flagless (only fetched copies carry `threadSubscribed`):
     * - A **reply** (Case 4) is stamped and the flip is mirrored onto the parent thread, so every
     *   open surface follows at once.
     * - A **root** (Case 2) is stamped only — its thread cannot be open anywhere yet, and later
     *   fetches of the message carry the flag from the server.
     */
    fun applyOwnMessageSent(sentMessage: BaseMessage) {
        val parentId = sentMessage.parentMessageId
        if (parentId > 0L) {
            sentMessage.setThreadSubscribed(true)
            mirrorSubscribed(parentId)
            return
        }
        if (sentMessage.id > 0L) sentMessage.setThreadSubscribed(true)
    }

    /**
     * Reconciles a **realtime** message, which always arrives with no `threadSubscribed` flag and
     * would otherwise render as un-followed even where the server has subscribed the user.
     *
     * - An own message relayed to another device/resource of the logged-in user was subscribed by
     *   the server at send time (Cases 2 and 4): stamp `true`, and for a reply also stamp the held
     *   parent and publish the flip.
     * - A reply from someone else that @-mentions the logged-in user subscribes them server-side
     *   (Case 3): stamp `true` onto the reply *and* the held parent, and publish the flip.
     * - Any other reply does not change the subscription: inherit the parent's current flag onto the
     *   reply, correcting the socket `false`. Someone else's root stays un-stamped — the receiver is
     *   not subscribed to it.
     *
     * Safe to call for every received message.
     *
     * @param parentMessage The thread's root, when the caller holds it. Without it only the mirrors
     *   can fire — there is nothing authoritative to inherit from.
     */
    fun applyIncomingMessageSubscription(
        message: BaseMessage,
        parentMessage: BaseMessage?,
        loggedInUserUid: String? = runCatching { CometChatUIKit.getLoggedInUser()?.uid }.getOrNull()
    ) {
        val senderUid = message.sender?.uid
        val fromMe = senderUid != null && senderUid == loggedInUserUid
        val parentId = message.parentMessageId

        if (parentId <= 0L) {
            // Case 2 echo: my own root, relayed to this device flagless.
            if (fromMe && message.id > 0L) message.setThreadSubscribed(true)
            return
        }

        if (fromMe) {
            // Case 4 echo: my own reply, relayed to this device — replying subscribed me.
            message.setThreadSubscribed(true)
            parentMessage?.setThreadSubscribed(true)
            mirrorSubscribed(parentId)
            return
        }

        if (senderUid != null && mentionsUser(message, loggedInUserUid)) {
            message.setThreadSubscribed(true)
            parentMessage?.setThreadSubscribed(true)
            mirrorSubscribed(parentId)
            return
        }

        // Not a mention: the subscription is unchanged, so the reply inherits the thread's state.
        if (parentMessage != null) message.setThreadSubscribed(parentMessage.isThreadSubscribed())
    }

    /**
     * Case 5 — a message was **edited**, and the edit both carries the thread's subscription state
     * and can create one.
     *
     * A realtime edit arrives with no `threadSubscribed` flag, exactly like a realtime send, but
     * unlike a send it replaces a copy the surface already holds. Applied blindly it therefore
     * *downgrades* a thread the user is subscribed to, so this reconciles in two steps:
     *
     * - **Preserve.** Whatever the surface already knew — from the held copy, or from the thread's
     *   root — is stamped back onto the edited copy. Editing a message never unsubscribes anyone.
     * - **Upgrade.** An edit that leaves the logged-in user @-mentioned in a **reply** subscribes
     *   them server-side, so mirror it: stamp the reply and the held parent, and publish the flip.
     *   This covers all three edit cases — the author adding a mention of the user, someone else
     *   adding one, and an edit that only preserves an existing mention while changing the text
     *   (which re-subscribes, even if the user had deliberately unsubscribed since).
     *
     * The upgrade is deliberately **replies-only**. Verified against staging: a mention on a *root*
     * message does not subscribe the mentioned user — only a mention inside a thread reply does,
     * which matches [applyIncomingMessageSubscription]'s Case 3. Stamping roots here would invent a
     * subscription the server never made.
     *
     * Safe to call for every edited message.
     *
     * @param editedMessage The edited copy about to replace what the surface holds.
     * @param heldMessage The copy the surface currently holds for that id, when it has one.
     * @param parentMessage The thread's root, when the caller holds it.
     */
    fun applyEditedMessageSubscription(
        editedMessage: BaseMessage,
        heldMessage: BaseMessage?,
        parentMessage: BaseMessage?,
        loggedInUserUid: String? = runCatching { CometChatUIKit.getLoggedInUser()?.uid }.getOrNull()
    ) {
        val parentId = editedMessage.parentMessageId

        // Preserve — an edit is flagless, so never let it undo what is already known.
        val alreadySubscribed = heldMessage?.isThreadSubscribed() == true ||
            (parentId > 0L && parentMessage?.isThreadSubscribed() == true)
        if (alreadySubscribed) editedMessage.setThreadSubscribed(true)

        // Upgrade — replies only; a mention on a root does not subscribe anyone.
        if (parentId <= 0L) return
        if (!mentionsUser(editedMessage, loggedInUserUid)) return

        editedMessage.setThreadSubscribed(true)
        parentMessage?.setThreadSubscribed(true)
        mirrorSubscribed(parentId)
    }

    /** Publishes on the kit bus. Surfaces re-render and stamp their held objects from here. */
    private fun publish(parentMessageId: Long, subscribed: Boolean) {
        CometChatEvents.emitThreadEvent(
            CometChatThreadEvent.SubscriptionChanged(parentMessageId, subscribed)
        )
    }

    private fun mentionsUser(message: BaseMessage, uid: String?): Boolean {
        if (message.hasMentionedMe()) return true
        if (uid.isNullOrEmpty()) return false
        return message.mentionedUsers?.any { it.uid == uid } == true
    }
}
