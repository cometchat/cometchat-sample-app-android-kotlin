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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * ViewModel backing the user-level Saved Messages screen.
 *
 * Saved messages are private to the current user and span all conversations, ordered
 * newest-save-first. Each row carries its source conversation context (receiver / receiverType) so
 * the screen can render a "@person / #group" label and deep-link without extra calls. The screen is
 * read-only (no markAsRead / receipts / unread changes).
 *
 * @param enableListeners subscribe to the UIKit bus for live upkeep (false for tests/previews)
 */
open class CometChatSavedMessagesViewModel(
    private val enableListeners: Boolean = true
) : ViewModel() {

    companion object {
        private const val DEFAULT_LIMIT = 30
        private const val MAX_LIMIT = 100
    }

    private val _messages = MutableStateFlow<List<BaseMessage>>(emptyList())

    /** The current user's saved messages across all conversations, newest save first. */
    val messages: StateFlow<List<BaseMessage>> = _messages.asStateFlow()

    private val _uiState = MutableStateFlow<PinnedSavedListUIState>(PinnedSavedListUIState.Loading)

    /** Screen state: [PinnedSavedListUIState.Loading] until the first load settles. */
    val uiState: StateFlow<PinnedSavedListUIState> = _uiState.asStateFlow()

    private val _count = MutableStateFlow(0)

    /** Exact saved-message count (fetch-all-and-count; the backend caps at 100). */
    val count: StateFlow<Int> = _count.asStateFlow()

    private val _unsaveSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** One-shot signal emitted when an unsave call succeeds, so the View can show a toast. */
    val unsaveSuccess: SharedFlow<Unit> = _unsaveSuccess.asSharedFlow()

    private var request: MessagesRequest? = null

    /**
     * The in-flight paging job. Cancelled before a new load starts so a stale run can never publish
     * its pages over a fresher list, and so two rapid reloads cannot page concurrently.
     */
    private var loadJob: Job? = null
    private var listenersTag: String? = null

    init {
        if (enableListeners) addListeners()
    }

    /**
     * Subscribes directly to the SDK's message listener so the screen stays live on its own.
     *
     * The View also forwards [CometChatMessageEvent.MessageSaved]/[CometChatMessageEvent.MessageUnsaved]
     * from the UIKit bus, but that bus is only fed by [CometChatMessageListViewModel] — i.e. only
     * while a message list is mounted. Saved Messages is user-level and normally opened straight from
     * the conversations menu with no message list anywhere (and on a second device the SDK event is
     * the ONLY signal), so it registers its own listener. Both paths land on the same idempotent
     * [onMessageSavedExternally]/[onMessageUnsavedExternally] handlers, so a doubled delivery is a
     * no-op.
     */
    private fun addListeners() {
        val tag = "${UIKitConstants.ListenerTags.SAVED_MESSAGES}_${hashCode()}_${System.currentTimeMillis()}"
        listenersTag = tag
        CometChat.addMessageListener(tag, object : CometChat.MessageListener() {
            override fun onMessageSaved(message: BaseMessage) {
                onMessageSavedExternally(message)
            }

            override fun onMessageUnsaved(message: BaseMessage) {
                onMessageUnsavedExternally(message)
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

    /** Rebuilds the request and loads all pages (bounded by the 100 cap). */
    fun reload() {
        // Same types + categories as the message list — without them the server's own (narrower)
        // defaults silently exclude custom-category messages (polls, stickers, whiteboard, etc.).
        request = MessagesRequest.MessagesRequestBuilder()
            .setSaved(true)
            .setLimit(DEFAULT_LIMIT)
            .setTypes(getDefaultMessagesTypes())
            .setCategories(getDefaultMessagesCategories())
            .build()
        _messages.value = emptyList()
        _count.value = 0
        _uiState.value = PinnedSavedListUIState.Loading
        loadAll()
    }

    private fun loadAll() {
        val req = request ?: run { reload(); return }
        // Cancel first: once cancelled the previous job can no longer publish its pages (the write
        // below the loop is unreachable after cancellation), so a fresh reload always wins.
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

    /** Unsaves a message: optimistic remove, revert on error, broadcast on success. */
    fun unsave(message: BaseMessage) {
        val index = _messages.value.indexOfFirst { it.id == message.id }
        removeRow(message)

        CometChat.unsaveMessage(message.id, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(updated: BaseMessage) {
                _unsaveSuccess.tryEmit(Unit)
                CometChatEvents.emitMessageEvent(CometChatMessageEvent.MessageUnsaved(updated))
            }

            override fun onError(e: CometChatException) {
                // Revert. The full-screen Error state is reserved for LOAD failures — a failed
                // unsave on a healthy list just restores the row.
                restoreRow(message, index)
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
     * Deliberately not a whole-snapshot restore: a save/unsave delivered by the SDK listener
     * between the optimistic removal and this failure callback must survive the revert.
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
     * Live upkeep. Called both by this ViewModel's own SDK listener (see [addListeners]) and by the
     * View's lifecycle-aware UIKit-bus subscription; both are safe to fire for the same message.
     */
    fun onMessageSavedExternally(message: BaseMessage) {
        if (_messages.value.none { it.id == message.id }) {
            _messages.update { list ->
                if (list.any { it.id == message.id }) list else listOf(message) + list
            }
            _count.value = _messages.value.size
            _uiState.value = PinnedSavedListUIState.Content
        }
    }

    /** Removes a row when a message is unsaved elsewhere (see [onMessageSavedExternally]). */
    fun onMessageUnsavedExternally(message: BaseMessage) {
        if (_messages.value.any { it.id == message.id }) {
            removeRow(message)
        }
    }
}
