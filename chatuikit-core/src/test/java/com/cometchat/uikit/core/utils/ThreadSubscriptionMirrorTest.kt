package com.cometchat.uikit.core.utils

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import io.kotest.core.spec.style.FunSpec
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for the realtime thread-subscription mirrors (ENG-37569).
 *
 * The server subscribes a sender to their message's thread on every send, but stamps the
 * `threadSubscribed` flag only on **fetched** messages (confirmed API behaviour on staging).
 * Everything delivered in realtime — a send's resolved result, or an own message relayed to
 * another device — arrives flagless, so the kit stamps it:
 * - [CometChatThreadSubscription.applyOwnMessageSent] on every send-success (root Case 2,
 *   reply Case 4);
 * - [CometChatThreadSubscription.applyIncomingMessageSubscription] on every socket delivery
 *   (own echoes, Case-3 mentions, and reply inherit-from-parent).
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*ThreadSubscriptionMirrorTest"
 */
class ThreadSubscriptionMirrorTest : FunSpec({

    val me = "me-uid"
    val other = "other-uid"

    fun user(uid: String): User = mock { whenever(it.uid).thenReturn(uid) }

    fun message(
        id: Long,
        parentId: Long,
        senderUid: String? = null,
        subscribed: Boolean = false,
        mentionsMe: Boolean = false
    ): BaseMessage {
        // Build the sender first — creating a mock inside another mock's stubbing lambda trips
        // Mockito's unfinished-stubbing detection.
        val sender = senderUid?.let { user(it) }
        return mock {
            whenever(it.id).thenReturn(id)
            whenever(it.parentMessageId).thenReturn(parentId)
            whenever(it.sender).thenReturn(sender)
            whenever(it.isThreadSubscribed).thenReturn(subscribed)
            whenever(it.hasMentionedMe()).thenReturn(mentionsMe)
        }
    }

    // ==================== applyOwnMessageSent (send-success path) ====================

    test("own sent root is stamped subscribed (Case 2)") {
        val root = message(id = 42L, parentId = 0L, senderUid = me)
        CometChatThreadSubscription.applyOwnMessageSent(root)
        verify(root).setThreadSubscribed(true)
    }

    test("own sent reply is stamped subscribed (Case 4)") {
        val reply = message(id = 43L, parentId = 500L, senderUid = me)
        CometChatThreadSubscription.applyOwnMessageSent(reply)
        verify(reply).setThreadSubscribed(true)
    }

    test("an unresolved root (no server id) is never stamped") {
        val unsent = message(id = 0L, parentId = 0L, senderUid = me)
        CometChatThreadSubscription.applyOwnMessageSent(unsent)
        verify(unsent, never()).setThreadSubscribed(true)
    }

    // ==================== applyIncomingMessageSubscription (socket path) ====================

    test("own root echoed to another device is stamped subscribed (Case 2 echo)") {
        val echo = message(id = 44L, parentId = 0L, senderUid = me)
        CometChatThreadSubscription.applyIncomingMessageSubscription(echo, null, me)
        verify(echo).setThreadSubscribed(true)
    }

    test("someone else's root stays un-stamped — the receiver is not subscribed") {
        val root = message(id = 45L, parentId = 0L, senderUid = other)
        CometChatThreadSubscription.applyIncomingMessageSubscription(root, null, me)
        verify(root, never()).setThreadSubscribed(true)
    }

    test("own reply echoed to another device stamps the reply and the held parent (Case 4 echo)") {
        val parent = message(id = 500L, parentId = 0L, senderUid = other)
        val echo = message(id = 46L, parentId = 500L, senderUid = me)
        CometChatThreadSubscription.applyIncomingMessageSubscription(echo, parent, me)
        verify(echo).setThreadSubscribed(true)
        verify(parent).setThreadSubscribed(true)
    }

    test("a mention from someone else stamps the reply and the held parent (Case 3)") {
        val parent = message(id = 500L, parentId = 0L, senderUid = me)
        val reply = message(id = 47L, parentId = 500L, senderUid = other, mentionsMe = true)
        CometChatThreadSubscription.applyIncomingMessageSubscription(reply, parent, me)
        verify(reply).setThreadSubscribed(true)
        verify(parent).setThreadSubscribed(true)
    }

    test("a non-mention reply from someone else inherits the parent's state") {
        val followedParent = message(id = 500L, parentId = 0L, senderUid = me, subscribed = true)
        val reply = message(id = 48L, parentId = 500L, senderUid = other)
        CometChatThreadSubscription.applyIncomingMessageSubscription(reply, followedParent, me)
        verify(reply).setThreadSubscribed(true)
        verify(followedParent, never()).setThreadSubscribed(true)

        val unfollowedParent = message(id = 501L, parentId = 0L, senderUid = me, subscribed = false)
        val reply2 = message(id = 49L, parentId = 501L, senderUid = other)
        CometChatThreadSubscription.applyIncomingMessageSubscription(reply2, unfollowedParent, me)
        verify(reply2).setThreadSubscribed(false)
    }

    // ==================== applyEditedMessageSubscription (edit path, Case 5) ====================

    test("an edit never un-follows: the held copy's state is stamped back onto the edited copy") {
        val held = message(id = 60L, parentId = 0L, senderUid = other, subscribed = true)
        val edited = message(id = 60L, parentId = 0L, senderUid = other)
        CometChatThreadSubscription.applyEditedMessageSubscription(edited, held, null, me)
        verify(edited).setThreadSubscribed(true)
    }

    test("an edited reply inherits the followed parent when no copy is held") {
        val parent = message(id = 500L, parentId = 0L, senderUid = other, subscribed = true)
        val edited = message(id = 61L, parentId = 500L, senderUid = other)
        CometChatThreadSubscription.applyEditedMessageSubscription(edited, null, parent, me)
        verify(edited).setThreadSubscribed(true)
    }

    test("an edit that adds a mention of me to a reply subscribes me (Case 5b)") {
        val parent = message(id = 500L, parentId = 0L, senderUid = other)
        val edited = message(id = 62L, parentId = 500L, senderUid = other, mentionsMe = true)
        CometChatThreadSubscription.applyEditedMessageSubscription(edited, null, parent, me)
        verify(edited).setThreadSubscribed(true)
        verify(parent).setThreadSubscribed(true)
    }

    test("an edit that preserves an existing mention re-subscribes me (Case 5c)") {
        // The user had unsubscribed since the mention was first sent: parent and held copy both say
        // false, and the edit must still flip them back — the server re-subscribes on every edit
        // that leaves the mention in place.
        val parent = message(id = 500L, parentId = 0L, senderUid = other, subscribed = false)
        val held = message(id = 63L, parentId = 500L, senderUid = other, subscribed = false)
        val edited = message(id = 63L, parentId = 500L, senderUid = other, mentionsMe = true)
        CometChatThreadSubscription.applyEditedMessageSubscription(edited, held, parent, me)
        verify(edited).setThreadSubscribed(true)
        verify(parent).setThreadSubscribed(true)
    }

    test("my own edit that mentions me subscribes me (Case 5a)") {
        val parent = message(id = 500L, parentId = 0L, senderUid = other)
        val edited = message(id = 64L, parentId = 500L, senderUid = me, mentionsMe = true)
        CometChatThreadSubscription.applyEditedMessageSubscription(edited, null, parent, me)
        verify(edited).setThreadSubscribed(true)
        verify(parent).setThreadSubscribed(true)
    }

    test("a mention added to a ROOT does not subscribe me — verified server behaviour") {
        val edited = message(id = 65L, parentId = 0L, senderUid = other, mentionsMe = true)
        CometChatThreadSubscription.applyEditedMessageSubscription(edited, null, null, me)
        verify(edited, never()).setThreadSubscribed(true)
    }

    test("an edit with no mention and nothing known leaves the flag alone") {
        val parent = message(id = 500L, parentId = 0L, senderUid = other, subscribed = false)
        val edited = message(id = 66L, parentId = 500L, senderUid = other)
        CometChatThreadSubscription.applyEditedMessageSubscription(edited, null, parent, me)
        verify(edited, never()).setThreadSubscribed(true)
    }
})
