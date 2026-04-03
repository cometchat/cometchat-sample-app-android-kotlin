package com.cometchat.uikit.core.events

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.ReactionEvent
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TransientMessage
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Lifecycle-aware event subscription extensions for CometChat events.
 * Provides extensions for LifecycleOwner (Activity, Fragment) and ViewModel.
 *
 * These extensions automatically manage subscription lifecycle:
 * - LifecycleOwner extensions: Subscribe on ON_START, unsubscribe on ON_STOP (configurable)
 * - ViewModel extensions: Subscribe for ViewModel scope, cancelled when ViewModel is cleared
 *
 * All callbacks are executed on the main thread.
 *
 * Validates: Requirements 9.1-9.8
 */

// ==================== Base Lifecycle-Aware Subscription ====================

/**
 * Internal helper class for managing lifecycle-aware subscriptions.
 * Handles subscription start/stop based on lifecycle events.
 */
internal class LifecycleAwareSubscription<T>(
    private val lifecycleOwner: LifecycleOwner,
    private val startEvent: Lifecycle.Event,
    private val stopEvent: Lifecycle.Event,
    private val flowProvider: () -> Flow<T>,
    private val onEvent: (T) -> Unit
) : DefaultLifecycleObserver {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main.immediate + job)
    private var collectionJob: Job? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        // If already past start event, start collection immediately
        if (shouldStartImmediately()) {
            startCollection()
        }
    }

    private fun shouldStartImmediately(): Boolean {
        val currentState = lifecycleOwner.lifecycle.currentState
        return when (startEvent) {
            Lifecycle.Event.ON_CREATE -> currentState.isAtLeast(Lifecycle.State.CREATED)
            Lifecycle.Event.ON_START -> currentState.isAtLeast(Lifecycle.State.STARTED)
            Lifecycle.Event.ON_RESUME -> currentState.isAtLeast(Lifecycle.State.RESUMED)
            else -> false
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        if (startEvent == Lifecycle.Event.ON_CREATE) {
            startCollection()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (startEvent == Lifecycle.Event.ON_START) {
            startCollection()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        if (startEvent == Lifecycle.Event.ON_RESUME) {
            startCollection()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        if (stopEvent == Lifecycle.Event.ON_PAUSE) {
            stopCollection()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        if (stopEvent == Lifecycle.Event.ON_STOP) {
            stopCollection()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (stopEvent == Lifecycle.Event.ON_DESTROY) {
            stopCollection()
        }
        job.cancel()
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    private fun startCollection() {
        if (collectionJob?.isActive == true) return
        collectionJob = scope.launch {
            flowProvider().collect { event ->
                onEvent(event)
            }
        }
    }

    private fun stopCollection() {
        collectionJob?.cancel()
        collectionJob = null
    }

    fun getJob(): Job = job
}

// ==================== Category-Level LifecycleOwner Extensions ====================

/**
 * Subscribes to all message events with lifecycle awareness.
 * Automatically subscribes on startEvent and unsubscribes on stopEvent.
 *
 * @param startEvent The lifecycle event to start subscription (default: ON_START)
 * @param stopEvent The lifecycle event to stop subscription (default: ON_STOP)
 * @param onEvent Callback invoked when any message event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMessageEvents(
    startEvent: Lifecycle.Event = Lifecycle.Event.ON_START,
    stopEvent: Lifecycle.Event = Lifecycle.Event.ON_STOP,
    onEvent: (CometChatMessageEvent) -> Unit
): Job {
    return LifecycleAwareSubscription(
        lifecycleOwner = this,
        startEvent = startEvent,
        stopEvent = stopEvent,
        flowProvider = { CometChatEvents.messageEvents },
        onEvent = onEvent
    ).getJob()
}

/**
 * Subscribes to all user events with lifecycle awareness.
 *
 * @param startEvent The lifecycle event to start subscription (default: ON_START)
 * @param stopEvent The lifecycle event to stop subscription (default: ON_STOP)
 * @param onEvent Callback invoked when any user event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onUserEvents(
    startEvent: Lifecycle.Event = Lifecycle.Event.ON_START,
    stopEvent: Lifecycle.Event = Lifecycle.Event.ON_STOP,
    onEvent: (CometChatUserEvent) -> Unit
): Job {
    return LifecycleAwareSubscription(
        lifecycleOwner = this,
        startEvent = startEvent,
        stopEvent = stopEvent,
        flowProvider = { CometChatEvents.userEvents },
        onEvent = onEvent
    ).getJob()
}

/**
 * Subscribes to all group events with lifecycle awareness.
 *
 * @param startEvent The lifecycle event to start subscription (default: ON_START)
 * @param stopEvent The lifecycle event to stop subscription (default: ON_STOP)
 * @param onEvent Callback invoked when any group event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onGroupEvents(
    startEvent: Lifecycle.Event = Lifecycle.Event.ON_START,
    stopEvent: Lifecycle.Event = Lifecycle.Event.ON_STOP,
    onEvent: (CometChatGroupEvent) -> Unit
): Job {
    return LifecycleAwareSubscription(
        lifecycleOwner = this,
        startEvent = startEvent,
        stopEvent = stopEvent,
        flowProvider = { CometChatEvents.groupEvents },
        onEvent = onEvent
    ).getJob()
}

/**
 * Subscribes to all call events with lifecycle awareness.
 *
 * @param startEvent The lifecycle event to start subscription (default: ON_START)
 * @param stopEvent The lifecycle event to stop subscription (default: ON_STOP)
 * @param onEvent Callback invoked when any call event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onCallEvents(
    startEvent: Lifecycle.Event = Lifecycle.Event.ON_START,
    stopEvent: Lifecycle.Event = Lifecycle.Event.ON_STOP,
    onEvent: (CometChatCallEvent) -> Unit
): Job {
    return LifecycleAwareSubscription(
        lifecycleOwner = this,
        startEvent = startEvent,
        stopEvent = stopEvent,
        flowProvider = { CometChatEvents.callEvents },
        onEvent = onEvent
    ).getJob()
}

/**
 * Subscribes to all conversation events with lifecycle awareness.
 *
 * @param startEvent The lifecycle event to start subscription (default: ON_START)
 * @param stopEvent The lifecycle event to stop subscription (default: ON_STOP)
 * @param onEvent Callback invoked when any conversation event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onConversationEvents(
    startEvent: Lifecycle.Event = Lifecycle.Event.ON_START,
    stopEvent: Lifecycle.Event = Lifecycle.Event.ON_STOP,
    onEvent: (CometChatConversationEvent) -> Unit
): Job {
    return LifecycleAwareSubscription(
        lifecycleOwner = this,
        startEvent = startEvent,
        stopEvent = stopEvent,
        flowProvider = { CometChatEvents.conversationEvents },
        onEvent = onEvent
    ).getJob()
}

/**
 * Subscribes to all UI events with lifecycle awareness.
 *
 * @param startEvent The lifecycle event to start subscription (default: ON_START)
 * @param stopEvent The lifecycle event to stop subscription (default: ON_STOP)
 * @param onEvent Callback invoked when any UI event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onUIEvents(
    startEvent: Lifecycle.Event = Lifecycle.Event.ON_START,
    stopEvent: Lifecycle.Event = Lifecycle.Event.ON_STOP,
    onEvent: (CometChatUIEvent) -> Unit
): Job {
    return LifecycleAwareSubscription(
        lifecycleOwner = this,
        startEvent = startEvent,
        stopEvent = stopEvent,
        flowProvider = { CometChatEvents.uiEvents },
        onEvent = onEvent
    ).getJob()
}


// ==================== Individual Message Event Extensions ====================

/**
 * Subscribes to message sent events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a message is sent
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMessageSent(
    onEvent: (BaseMessage, MessageStatus) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.MessageSent) {
        onEvent(event.message, event.status)
    }
}

/**
 * Subscribes to message edited events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a message is edited
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMessageEdited(
    onEvent: (BaseMessage, MessageStatus) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.MessageEdited) {
        onEvent(event.message, event.status)
    }
}

/**
 * Subscribes to message deleted events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a message is deleted
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMessageDeleted(
    onEvent: (BaseMessage) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.MessageDeleted) {
        onEvent(event.message)
    }
}

/**
 * Subscribes to message read events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a message is read
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMessageRead(
    onEvent: (BaseMessage) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.MessageRead) {
        onEvent(event.message)
    }
}

/**
 * Subscribes to all message received events (text, media, custom) with lifecycle awareness.
 *
 * @param onEvent Callback invoked when any message is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMessageReceived(
    onEvent: (BaseMessage) -> Unit
): Job = onMessageEvents { event ->
    val message: BaseMessage? = when (event) {
        is CometChatMessageEvent.TextMessageReceived -> event.message
        is CometChatMessageEvent.MediaMessageReceived -> event.message
        is CometChatMessageEvent.CustomMessageReceived -> event.message
        else -> null
    }
    message?.let { onEvent(it) }
}

/**
 * Subscribes to text message received events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a text message is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onTextMessageReceived(
    onEvent: (TextMessage) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.TextMessageReceived) {
        onEvent(event.message)
    }
}

/**
 * Subscribes to media message received events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a media message is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMediaMessageReceived(
    onEvent: (MediaMessage) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.MediaMessageReceived) {
        onEvent(event.message)
    }
}

/**
 * Subscribes to custom message received events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a custom message is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onCustomMessageReceived(
    onEvent: (CustomMessage) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.CustomMessageReceived) {
        onEvent(event.message)
    }
}

/**
 * Subscribes to typing started events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when typing starts
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onTypingStarted(
    onEvent: (TypingIndicator) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.TypingStarted) {
        onEvent(event.indicator)
    }
}

/**
 * Subscribes to typing ended events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when typing ends
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onTypingEnded(
    onEvent: (TypingIndicator) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.TypingEnded) {
        onEvent(event.indicator)
    }
}

/**
 * Subscribes to messages delivered events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when messages are delivered
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMessagesDelivered(
    onEvent: (MessageReceipt) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.MessagesDelivered) {
        onEvent(event.receipt)
    }
}

/**
 * Subscribes to messages read receipt events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when messages are read
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMessagesRead(
    onEvent: (MessageReceipt) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.MessagesRead) {
        onEvent(event.receipt)
    }
}

/**
 * Subscribes to reaction added events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a reaction is added
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onReactionAdded(
    onEvent: (ReactionEvent) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.ReactionAdded) {
        onEvent(event.event)
    }
}

/**
 * Subscribes to reaction removed events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a reaction is removed
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onReactionRemoved(
    onEvent: (ReactionEvent) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.ReactionRemoved) {
        onEvent(event.event)
    }
}

/**
 * Subscribes to transient message received events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a transient message is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onTransientMessageReceived(
    onEvent: (TransientMessage) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.TransientMessageReceived) {
        onEvent(event.message)
    }
}

/**
 * Subscribes to live reaction events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a live reaction is received
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onLiveReaction(
    onEvent: (Int) -> Unit
): Job = onMessageEvents { event ->
    if (event is CometChatMessageEvent.LiveReaction) {
        onEvent(event.icon)
    }
}

// ==================== Individual User Event Extensions ====================

/**
 * Subscribes to user blocked events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a user is blocked
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onUserBlocked(
    onEvent: (User) -> Unit
): Job = onUserEvents { event ->
    if (event is CometChatUserEvent.UserBlocked) {
        onEvent(event.user)
    }
}

/**
 * Subscribes to user unblocked events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a user is unblocked
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onUserUnblocked(
    onEvent: (User) -> Unit
): Job = onUserEvents { event ->
    if (event is CometChatUserEvent.UserUnblocked) {
        onEvent(event.user)
    }
}

// ==================== Individual Group Event Extensions ====================

/**
 * Subscribes to group created events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a group is created
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onGroupCreated(
    onEvent: (Group) -> Unit
): Job = onGroupEvents { event ->
    if (event is CometChatGroupEvent.GroupCreated) {
        onEvent(event.group)
    }
}

/**
 * Subscribes to group deleted events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a group is deleted
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onGroupDeleted(
    onEvent: (Group) -> Unit
): Job = onGroupEvents { event ->
    if (event is CometChatGroupEvent.GroupDeleted) {
        onEvent(event.group)
    }
}

/**
 * Subscribes to group left events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a user leaves a group
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onGroupLeft(
    onEvent: (Action, User, Group) -> Unit
): Job = onGroupEvents { event ->
    if (event is CometChatGroupEvent.GroupLeft) {
        onEvent(event.action, event.user, event.group)
    }
}

/**
 * Subscribes to member joined events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a user joins a group
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMemberJoined(
    onEvent: (User, Group) -> Unit
): Job = onGroupEvents { event ->
    if (event is CometChatGroupEvent.MemberJoined) {
        onEvent(event.user, event.group)
    }
}

/**
 * Subscribes to members added events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when users are added to a group
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMembersAdded(
    onEvent: (List<Action>, List<User>, Group, User) -> Unit
): Job = onGroupEvents { event ->
    if (event is CometChatGroupEvent.MembersAdded) {
        onEvent(event.actions, event.users, event.group, event.addedBy)
    }
}

/**
 * Subscribes to member kicked events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a user is kicked from a group
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMemberKicked(
    onEvent: (Action, User, User, Group) -> Unit
): Job = onGroupEvents { event ->
    if (event is CometChatGroupEvent.MemberKicked) {
        onEvent(event.action, event.user, event.kickedBy, event.group)
    }
}

/**
 * Subscribes to member banned events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a user is banned from a group
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMemberBanned(
    onEvent: (Action, User, User, Group) -> Unit
): Job = onGroupEvents { event ->
    if (event is CometChatGroupEvent.MemberBanned) {
        onEvent(event.action, event.user, event.bannedBy, event.group)
    }
}

/**
 * Subscribes to member unbanned events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a user is unbanned from a group
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMemberUnbanned(
    onEvent: (Action, User, User, Group) -> Unit
): Job = onGroupEvents { event ->
    if (event is CometChatGroupEvent.MemberUnbanned) {
        onEvent(event.action, event.user, event.unbannedBy, event.group)
    }
}

/**
 * Subscribes to member scope changed events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a group member's scope changes
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onMemberScopeChanged(
    onEvent: (Action, User, String, String, Group) -> Unit
): Job = onGroupEvents { event ->
    if (event is CometChatGroupEvent.MemberScopeChanged) {
        onEvent(event.action, event.user, event.newScope, event.oldScope, event.group)
    }
}

/**
 * Subscribes to ownership changed events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when group ownership changes
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onOwnershipChanged(
    onEvent: (Group, GroupMember) -> Unit
): Job = onGroupEvents { event ->
    if (event is CometChatGroupEvent.OwnershipChanged) {
        onEvent(event.group, event.newOwner)
    }
}


// ==================== Individual Call Event Extensions ====================

/**
 * Subscribes to outgoing call events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when an outgoing call is initiated
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onOutgoingCall(
    onEvent: (Call) -> Unit
): Job = onCallEvents { event ->
    if (event is CometChatCallEvent.OutgoingCall) {
        onEvent(event.call)
    }
}

/**
 * Subscribes to call accepted events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a call is accepted
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onCallAccepted(
    onEvent: (Call) -> Unit
): Job = onCallEvents { event ->
    if (event is CometChatCallEvent.CallAccepted) {
        onEvent(event.call)
    }
}

/**
 * Subscribes to call rejected events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a call is rejected
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onCallRejected(
    onEvent: (Call) -> Unit
): Job = onCallEvents { event ->
    if (event is CometChatCallEvent.CallRejected) {
        onEvent(event.call)
    }
}

/**
 * Subscribes to call ended events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a call ends
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onCallEnded(
    onEvent: (Call) -> Unit
): Job = onCallEvents { event ->
    if (event is CometChatCallEvent.CallEnded) {
        onEvent(event.call)
    }
}

// ==================== Individual Conversation Event Extensions ====================

/**
 * Subscribes to conversation deleted events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a conversation is deleted
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onConversationDeleted(
    onEvent: (Conversation) -> Unit
): Job = onConversationEvents { event ->
    if (event is CometChatConversationEvent.ConversationDeleted) {
        onEvent(event.conversation)
    }
}

// ==================== Individual UI Event Extensions ====================

/**
 * Subscribes to active chat changed events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when the active chat changes
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onActiveChatChanged(
    onEvent: (Map<String, String>, BaseMessage?, User?, Group?, Int) -> Unit
): Job = onUIEvents { event ->
    if (event is CometChatUIEvent.ActiveChatChanged) {
        onEvent(event.id, event.message, event.user, event.group, event.unreadCount)
    }
}

/**
 * Subscribes to compose message events with lifecycle awareness.
 *
 * @param onEvent Callback invoked for compose message actions
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onComposeMessage(
    onEvent: (String, String) -> Unit
): Job = onUIEvents { event ->
    if (event is CometChatUIEvent.ComposeMessage) {
        onEvent(event.id, event.text)
    }
}

/**
 * Subscribes to open chat events with lifecycle awareness.
 *
 * @param onEvent Callback invoked when a chat should be opened
 * @return Job that can be cancelled to stop subscription early
 */
fun LifecycleOwner.onOpenChat(
    onEvent: (User?, Group?) -> Unit
): Job = onUIEvents { event ->
    if (event is CometChatUIEvent.OpenChat) {
        onEvent(event.user, event.group)
    }
}

// ==================== ViewModel Scope Extensions ====================

/**
 * Subscribes to all message events within ViewModel scope.
 * Subscription survives configuration changes and is cancelled when ViewModel is cleared.
 *
 * @param onEvent Callback invoked when any message event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun ViewModel.onMessageEvents(
    onEvent: (CometChatMessageEvent) -> Unit
): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
    CometChatEvents.messageEvents.collect { event ->
        onEvent(event)
    }
}

/**
 * Subscribes to all user events within ViewModel scope.
 * Subscription survives configuration changes and is cancelled when ViewModel is cleared.
 *
 * @param onEvent Callback invoked when any user event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun ViewModel.onUserEvents(
    onEvent: (CometChatUserEvent) -> Unit
): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
    CometChatEvents.userEvents.collect { event ->
        onEvent(event)
    }
}

/**
 * Subscribes to all group events within ViewModel scope.
 * Subscription survives configuration changes and is cancelled when ViewModel is cleared.
 *
 * @param onEvent Callback invoked when any group event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun ViewModel.onGroupEvents(
    onEvent: (CometChatGroupEvent) -> Unit
): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
    CometChatEvents.groupEvents.collect { event ->
        onEvent(event)
    }
}

/**
 * Subscribes to all call events within ViewModel scope.
 * Subscription survives configuration changes and is cancelled when ViewModel is cleared.
 *
 * @param onEvent Callback invoked when any call event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun ViewModel.onCallEvents(
    onEvent: (CometChatCallEvent) -> Unit
): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
    CometChatEvents.callEvents.collect { event ->
        onEvent(event)
    }
}

/**
 * Subscribes to all conversation events within ViewModel scope.
 * Subscription survives configuration changes and is cancelled when ViewModel is cleared.
 *
 * @param onEvent Callback invoked when any conversation event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun ViewModel.onConversationEvents(
    onEvent: (CometChatConversationEvent) -> Unit
): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
    CometChatEvents.conversationEvents.collect { event ->
        onEvent(event)
    }
}

/**
 * Subscribes to all UI events within ViewModel scope.
 * Subscription survives configuration changes and is cancelled when ViewModel is cleared.
 *
 * @param onEvent Callback invoked when any UI event is received
 * @return Job that can be cancelled to stop subscription early
 */
fun ViewModel.onUIEvents(
    onEvent: (CometChatUIEvent) -> Unit
): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
    CometChatEvents.uiEvents.collect { event ->
        onEvent(event)
    }
}

// ==================== StateFlow Conversion Extensions ====================

/**
 * Converts message events to a StateFlow for UI state observation.
 * The StateFlow is scoped to the ViewModel and survives configuration changes.
 *
 * @param initial The initial value for the StateFlow (default: null)
 * @return StateFlow of message events
 */
fun ViewModel.messageEventsAsState(
    initial: CometChatMessageEvent? = null
): StateFlow<CometChatMessageEvent?> = CometChatEvents.messageEvents
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)

/**
 * Converts user events to a StateFlow for UI state observation.
 * The StateFlow is scoped to the ViewModel and survives configuration changes.
 *
 * @param initial The initial value for the StateFlow (default: null)
 * @return StateFlow of user events
 */
fun ViewModel.userEventsAsState(
    initial: CometChatUserEvent? = null
): StateFlow<CometChatUserEvent?> = CometChatEvents.userEvents
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)

/**
 * Converts group events to a StateFlow for UI state observation.
 * The StateFlow is scoped to the ViewModel and survives configuration changes.
 *
 * @param initial The initial value for the StateFlow (default: null)
 * @return StateFlow of group events
 */
fun ViewModel.groupEventsAsState(
    initial: CometChatGroupEvent? = null
): StateFlow<CometChatGroupEvent?> = CometChatEvents.groupEvents
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)

/**
 * Converts call events to a StateFlow for UI state observation.
 * The StateFlow is scoped to the ViewModel and survives configuration changes.
 *
 * @param initial The initial value for the StateFlow (default: null)
 * @return StateFlow of call events
 */
fun ViewModel.callEventsAsState(
    initial: CometChatCallEvent? = null
): StateFlow<CometChatCallEvent?> = CometChatEvents.callEvents
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)

/**
 * Converts conversation events to a StateFlow for UI state observation.
 * The StateFlow is scoped to the ViewModel and survives configuration changes.
 *
 * @param initial The initial value for the StateFlow (default: null)
 * @return StateFlow of conversation events
 */
fun ViewModel.conversationEventsAsState(
    initial: CometChatConversationEvent? = null
): StateFlow<CometChatConversationEvent?> = CometChatEvents.conversationEvents
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)

/**
 * Converts UI events to a StateFlow for UI state observation.
 * The StateFlow is scoped to the ViewModel and survives configuration changes.
 *
 * @param initial The initial value for the StateFlow (default: null)
 * @return StateFlow of UI events
 */
fun ViewModel.uiEventsAsState(
    initial: CometChatUIEvent? = null
): StateFlow<CometChatUIEvent?> = CometChatEvents.uiEvents
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)
