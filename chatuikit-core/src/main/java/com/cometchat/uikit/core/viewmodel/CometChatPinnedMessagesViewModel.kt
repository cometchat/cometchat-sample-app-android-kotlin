package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.state.PinnedSavedListUIState
import com.cometchat.uikit.core.utils.getDefaultMessagesCategories
import com.cometchat.uikit.core.utils.getDefaultMessagesTypes
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import com.cometchat.uikit.core.utils.PinSaveUtils
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * ViewModel backing the per-conversation Pinned Messages panel.
 *
 * Pinned messages are conversation-wide (visible to everyone) and ordered newest-pin-first. The
 * count shown in the panel title is a fetch-all-and-count (there is no server count field; the cap
 * is 100). The panel is read-only — it never marks messages read or changes unread counts.
 *
 * @param enableListeners subscribe to the UIKit bus for live upkeep (false for tests/previews)
 */
open class CometChatPinnedMessagesViewModel(
    private val enableListeners: Boolean = true
) : ViewModel() {

    companion object {
        private const val DEFAULT_LIMIT = 30
        private const val MAX_LIMIT = 100
    }

    private val _messages = MutableStateFlow<List<BaseMessage>>(emptyList())

    /** The conversation's pinned messages, newest pin first. */
    val messages: StateFlow<List<BaseMessage>> = _messages.asStateFlow()

    private val _uiState = MutableStateFlow<PinnedSavedListUIState>(PinnedSavedListUIState.Loading)

    /** Screen state: [PinnedSavedListUIState.Loading] until the first load settles. */
    val uiState: StateFlow<PinnedSavedListUIState> = _uiState.asStateFlow()

    private val _count = MutableStateFlow(0)

    /** Exact pinned-message count (fetch-all-and-count; the backend caps at 100). */
    val count: StateFlow<Int> = _count.asStateFlow()

    private val _actionResult = MutableSharedFlow<PinnedActionResult>(extraBufferCapacity = 1)

    /** One-shot results of message-option actions, for the View to surface as toasts. */
    val actionResult: SharedFlow<PinnedActionResult> = _actionResult.asSharedFlow()

    private var uid: String? = null
    private var guid: String? = null
    private var request: MessagesRequest? = null

    /**
     * The in-flight paging job. Cancelled before a new load starts so a conversation switch can
     * never let the previous conversation's pages land under the new conversation's header, and so
     * two rapid reloads cannot page concurrently into the same list.
     */
    private var loadJob: Job? = null
    private var listenersTag: String? = null

    init {
        if (enableListeners) addListeners()
    }

    /**
     * Configures the panel for a conversation. Pass the peer uid (1-1) OR the group guid.
     * Triggers an initial load.
     */
    fun configure(uid: String?, guid: String?) {
        this.uid = uid
        this.guid = guid
        reload()
    }

    /** Rebuilds the request and loads all pages (bounded by the 100 cap). */
    fun reload() {
        // Same types + categories as the message list: without them the server's own (narrower)
        // defaults apply and custom-category messages — polls, stickers, whiteboard/document,
        // meetings, cards — are silently absent from the panel (ENG-38060 scope).
        val builder = MessagesRequest.MessagesRequestBuilder()
            .setPinned(true)
            .setLimit(DEFAULT_LIMIT)
            .setTypes(getDefaultMessagesTypes())
            .setCategories(getDefaultMessagesCategories())
        uid?.let { builder.setUID(it) }
        guid?.let { builder.setGUID(it) }
        request = builder.build()
        _messages.value = emptyList()
        _count.value = 0
        _uiState.value = PinnedSavedListUIState.Loading
        loadAll()
    }

    /**
     * Fetches every page and accumulates them, so the count is exact. Bounded by MAX_LIMIT pages of
     * data because the backend caps a conversation at 100 pinned messages.
     */
    private fun loadAll() {
        val req = request ?: return
        // Cancel first: the previous job captured the previous request, and once cancelled it can
        // no longer publish its pages (the write below the loop is unreachable after cancellation).
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val accumulated = mutableListOf<BaseMessage>()
            val seenIds = mutableSetOf<Long>()
            try {
                while (accumulated.size < MAX_LIMIT) {
                    val page = fetchNext(req)
                    if (page.isEmpty()) break
                    // Overlapping pages would otherwise show the same message twice and inflate
                    // the count.
                    page.forEach { if (seenIds.add(it.id)) accumulated.add(it) }
                }
                _messages.value = accumulated
                _count.value = accumulated.size
                _uiState.value = if (accumulated.isEmpty()) PinnedSavedListUIState.Empty
                else PinnedSavedListUIState.Content
            } catch (e: CometChatException) {
                _uiState.value = PinnedSavedListUIState.Error(e)
            }
        }
    }

    private suspend fun fetchNext(req: MessagesRequest): List<BaseMessage> =
        suspendCancellableCoroutine { cont ->
            req.fetchNext(object : CometChat.CallbackListener<List<BaseMessage>>() {
                override fun onSuccess(result: List<BaseMessage>) {
                    if (cont.isActive) cont.resume(result)
                }

                override fun onError(e: CometChatException) {
                    if (cont.isActive) cont.cancel(e)
                }
            })
        }

    /**
     * Unpins a message. Optimistically removes it from the panel, then reverts on error. On success
     * the event is broadcast on the UIKit bus so the main list bubble updates too.
     */
    fun unpin(message: BaseMessage) {
        val index = _messages.value.indexOfFirst { it.id == message.id }
        removeRow(message)

        CometChat.unpinMessage(message.id, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(updated: BaseMessage) {
                CometChatEvents.emitMessageEvent(CometChatMessageEvent.MessageUnpinned(updated))
            }

            override fun onError(e: CometChatException) {
                // Revert. The full-screen Error state is reserved for LOAD failures — a failed
                // action on a healthy list just restores the row and surfaces a toast.
                restoreRow(message, index)
                _actionResult.tryEmit(pinFailureResult(e))
            }
        })
    }

    /** Optimistically drops a row, keeping count and screen state in step. */
    private fun removeRow(message: BaseMessage) {
        _messages.update { list -> list.filter { it.id != message.id } }
        _count.value = _messages.value.size
        if (_messages.value.isEmpty()) _uiState.value = PinnedSavedListUIState.Empty
    }

    /**
     * Undoes [removeRow] by re-inserting the single removed row at [index].
     *
     * Deliberately not a whole-snapshot restore: a pin/unpin delivered by the SDK listener between
     * the optimistic removal and this failure callback must survive the revert.
     */
    private fun restoreRow(message: BaseMessage, index: Int) {
        if (index >= 0) {
            _messages.update { list ->
                if (list.any { it.id == message.id }) list
                else list.toMutableList().apply { add(index.coerceAtMost(size), message) }
            }
        }
        _count.value = _messages.value.size
        _uiState.value = if (_messages.value.isNotEmpty()) PinnedSavedListUIState.Content
        else PinnedSavedListUIState.Empty
    }

    /**
     * Maps a pin/unpin failure to its toast: an RBAC denial gets the shared "you don't have
     * permission" message (pin/unpin is offered to every member, the server enforces the policy),
     * everything else the generic failure.
     */
    private fun pinFailureResult(e: CometChatException): PinnedActionResult =
        when (PinSaveUtils.classifyFailure(e)) {
            PinSaveUtils.Failure.PermissionDenied -> PinnedActionResult.PERMISSION_DENIED
            is PinSaveUtils.Failure.LimitReached -> PinnedActionResult.PIN_LIMIT_REACHED
            is PinSaveUtils.Failure.Other -> PinnedActionResult.PIN_FAILED
        }

    /**
     * Pins a message. On success broadcasts on the UIKit bus so the main list bubble updates.
     * (On the pinned panel every row is already pinned, so this is rarely reachable — wired for
     * completeness / robustness.)
     */
    fun pin(message: BaseMessage) {
        CometChat.pinMessage(message.id, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(updated: BaseMessage) {
                CometChatEvents.emitMessageEvent(CometChatMessageEvent.MessagePinned(updated))
                _actionResult.tryEmit(PinnedActionResult.PINNED)
            }

            override fun onError(e: CometChatException) {
                _actionResult.tryEmit(pinFailureResult(e))
            }
        })
    }

    /**
     * Deletes a message. Optimistically removes it from the panel, reverts on error. Mirrors the
     * [unpin] pattern.
     */
    fun delete(message: BaseMessage) {
        val index = _messages.value.indexOfFirst { it.id == message.id }
        removeRow(message)

        CometChat.deleteMessage(message.id, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(deleted: BaseMessage) {
                // Broadcast so an open message list (and conversations preview) swaps the bubble
                // to its deleted tombstone immediately — same event the list's own delete emits.
                CometChatEvents.emitMessageEvent(CometChatMessageEvent.MessageDeleted(deleted))
                _actionResult.tryEmit(PinnedActionResult.DELETED)
            }

            override fun onError(e: CometChatException) {
                restoreRow(message, index)
                _actionResult.tryEmit(PinnedActionResult.DELETE_FAILED)
            }
        })
    }

    /**
     * Translates a text message into the device language via the message-translation extension —
     * the same call the message list makes. The translated text lands in the message metadata
     * ("translated_message", which the text bubble renders) on a CLONE that replaces the row:
     * BaseMessage equality is identity, so an in-place mutation would be suppressed by the
     * StateFlow and the bubble would never re-render.
     */
    fun translate(message: BaseMessage) {
        if (message !is com.cometchat.chat.models.TextMessage) return
        viewModelScope.launch {
            try {
                val body = org.json.JSONObject()
                body.put("msgId", message.id)
                body.put("text", message.text)
                body.put("languages", org.json.JSONArray().put(java.util.Locale.getDefault().language))

                val result = suspendCancellableCoroutine { cont ->
                    CometChat.callExtension(
                        "message-translation",
                        "POST",
                        "/v2/translate",
                        body,
                        object : CometChat.CallbackListener<org.json.JSONObject>() {
                            override fun onSuccess(response: org.json.JSONObject) {
                                if (cont.isActive) cont.resume(response)
                            }

                            override fun onError(e: CometChatException) {
                                if (cont.isActive) cont.cancel(e)
                            }
                        }
                    )
                }

                val translatedText = result
                    .optJSONObject("data")
                    ?.optJSONArray("translations")
                    ?.takeIf { it.length() > 0 }
                    ?.getJSONObject(0)
                    ?.optString("message_translated", "")
                    .orEmpty()

                if (translatedText.isNotEmpty()) {
                    val updated = message.clone().apply {
                        // Copy into a fresh JSONObject: clone() is shallow, so mutating the
                        // existing metadata in place would also mutate the original message's.
                        val source = this.metadata
                        val copy = org.json.JSONObject()
                        source?.keys()?.forEach { key -> copy.put(key, source.get(key)) }
                        copy.put("translated_message", translatedText)
                        this.metadata = copy
                    }
                    _messages.update { list ->
                        list.map { if (it.id == message.id) updated else it }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionResult.tryEmit(PinnedActionResult.TRANSLATE_FAILED)
            }
        }
    }

    /**
     * Subscribes directly to the SDK's message listener so the panel stays live on its own.
     *
     * The View also forwards [CometChatMessageEvent.MessagePinned]/[CometChatMessageEvent.MessageUnpinned]
     * from the UIKit bus, but that bus is only fed by [CometChatMessageListViewModel] — i.e. only
     * while a message list is mounted. This panel is regularly opened without one (and on a second
     * device the SDK event is the ONLY signal), so it registers its own listener. Both paths land on
     * the same idempotent [onMessagePinnedExternally]/[onMessageUnpinnedExternally] handlers, so a
     * doubled delivery is a no-op.
     */
    private fun addListeners() {
        val tag = "${UIKitConstants.ListenerTags.PINNED_MESSAGES}_${hashCode()}_${System.currentTimeMillis()}"
        listenersTag = tag
        CometChat.addMessageListener(tag, object : CometChat.MessageListener() {
            override fun onMessagePinned(message: BaseMessage) {
                onMessagePinnedExternally(message)
            }

            override fun onMessageUnpinned(message: BaseMessage) {
                onMessageUnpinnedExternally(message)
            }
        })
    }

    private fun removeListeners() {
        listenersTag?.let { CometChat.removeMessageListener(it) }
        listenersTag = null
    }

    override fun onCleared() {
        removeListeners()
        super.onCleared()
    }

    /**
     * Live upkeep. Called both by this ViewModel's own SDK listener (see [addListeners]) and by the
     * View's lifecycle-aware UIKit-bus subscription; both are safe to fire for the same message.
     */
    fun onMessagePinnedExternally(message: BaseMessage) {
        if (!belongsToThisConversation(message)) return
        if (_messages.value.none { it.id == message.id }) {
            _messages.update { list ->
                if (list.any { it.id == message.id }) list else listOf(message) + list
            }
            _count.value = _messages.value.size
            _uiState.value = PinnedSavedListUIState.Content
        }
    }

    /** Removes a row when a message is unpinned elsewhere (see [onMessagePinnedExternally]). */
    fun onMessageUnpinnedExternally(message: BaseMessage) {
        if (_messages.value.any { it.id == message.id }) {
            removeRow(message)
        }
    }

    /** Outcome of a message-option action, surfaced to the View for a toast. */
    enum class PinnedActionResult {
        PINNED, PIN_FAILED,

        /** The pinned-messages cap was hit; the View reads the cap via [PinSaveUtils.pinnedMessagesLimit]. */
        PIN_LIMIT_REACHED,

        /** Any RBAC/SBAC denial, whichever action was attempted. */
        PERMISSION_DENIED,

        DELETED, DELETE_FAILED,
        TRANSLATE_FAILED
    }

    private fun belongsToThisConversation(message: BaseMessage): Boolean {
        val g = guid
        val u = uid
        return when {
            g != null -> message.receiverType == "group" && message.receiverUid == g
            u != null && message.receiverType == "user" -> {
                // A 1-1 message belongs here only if it runs between the peer and the logged-in
                // user; matching on the peer alone would also accept a message the peer sent to
                // somebody else. Falls back to the looser check only if the session has no user
                // (tests/previews), where dropping every event would be worse.
                val me = CometChat.getLoggedInUser()?.uid
                if (me == null) {
                    message.receiverUid == u || message.sender?.uid == u
                } else {
                    (message.receiverUid == u && message.sender?.uid == me) ||
                        (message.sender?.uid == u && message.receiverUid == me)
                }
            }
            else -> false
        }
    }
}
