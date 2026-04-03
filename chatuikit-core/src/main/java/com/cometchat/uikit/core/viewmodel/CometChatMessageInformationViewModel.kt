package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.uikit.core.data.datasource.MessageReceiptEventListener
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository
import com.cometchat.uikit.core.state.MessageInformationUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing message information state.
 * Handles fetching receipts, real-time updates, and state management.
 * Uses StateFlow for reactive state management.
 *
 * Per design doc: ViewModel State Management section.
 *
 * @param repository Repository for fetching receipts and creating receipts from messages
 * @param eventListener Event listener for real-time receipt updates
 * @param enableListeners Whether to enable real-time listeners (set to false for testing)
 */
open class CometChatMessageInformationViewModel(
    private val repository: MessageInformationRepository,
    private val eventListener: MessageReceiptEventListener,
    private val enableListeners: Boolean = true
) : ViewModel() {

    companion object {
        private const val TAG = "MessageInfoViewModel"
    }

    // ==================== UI State ====================

    /**
     * UI State - LOADING, LOADED, EMPTY, ERROR
     */
    private val _state = MutableStateFlow<MessageInformationUIState?>(null)
    val state: StateFlow<MessageInformationUIState?> = _state.asStateFlow()

    /**
     * Receipt list data
     */
    private val _listData = MutableStateFlow<List<MessageReceipt>>(emptyList())
    val listData: StateFlow<List<MessageReceipt>> = _listData.asStateFlow()

    /**
     * Update receipt notification (index of updated item)
     */
    private val _updateReceipt = MutableStateFlow<Int?>(null)
    val updateReceipt: StateFlow<Int?> = _updateReceipt.asStateFlow()

    /**
     * Add receipt notification (index where item was added)
     */
    private val _addReceipt = MutableStateFlow<Int?>(null)
    val addReceipt: StateFlow<Int?> = _addReceipt.asStateFlow()

    /**
     * Exception for error handling
     */
    private val _exception = MutableStateFlow<CometChatException?>(null)
    val exception: StateFlow<CometChatException?> = _exception.asStateFlow()

    /**
     * Clear list trigger
     */
    private val _clearList = MutableStateFlow<Unit?>(null)
    val clearList: StateFlow<Unit?> = _clearList.asStateFlow()

    // ==================== Internal State ====================

    /**
     * Current message being displayed
     */
    private var message: BaseMessage? = null

    /**
     * Conversation type - USER or GROUP
     */
    private var conversationType: String? = null

    /**
     * Internal mutable list of receipts
     */
    private val messageReceipts = mutableListOf<MessageReceipt>()

    /**
     * Job for real-time event listening
     */
    private var receiptEventsJob: Job? = null

    // ==================== Initialization ====================

    init {
        if (enableListeners) {
            addListener()
        }
    }

    // ==================== Public API ====================

    /**
     * Sets the message and initializes the component.
     * Per design doc: setMessage() logic for USER and GROUP conversations.
     *
     * @param baseMessage The message to display information for
     */
    fun setMessage(baseMessage: BaseMessage?) {
        if (baseMessage == null) return

        this.message = baseMessage

        // Determine conversation type
        conversationType = when (baseMessage.receiverType.lowercase()) {
            CometChatConstants.RECEIVER_TYPE_USER -> CometChatConstants.RECEIVER_TYPE_USER
            CometChatConstants.RECEIVER_TYPE_GROUP -> CometChatConstants.RECEIVER_TYPE_GROUP
            else -> null
        }

        when (conversationType) {
            CometChatConstants.RECEIVER_TYPE_USER -> {
                handleUserConversation(baseMessage)
            }
            CometChatConstants.RECEIVER_TYPE_GROUP -> {
                fetchMessageReceipt()
            }
        }
    }

    /**
     * Gets the current message.
     */
    fun getMessage(): BaseMessage? = message

    /**
     * Gets the conversation type.
     */
    fun getConversationType(): String? = conversationType

    /**
     * Fetches message receipts for GROUP conversations.
     * Per design doc: fetchMessageReceipt() logic.
     */
    fun fetchMessageReceipt() {
        val currentMessage = message
        if (currentMessage == null) {
            _state.value = MessageInformationUIState.Loading
            return
        }

        _state.value = MessageInformationUIState.Loading

        viewModelScope.launch {
            repository.fetchReceipts(currentMessage.id.toLong())
                .onSuccess { receipts ->
                    setList(receipts)
                    _state.value = if (receipts.isEmpty()) {
                        MessageInformationUIState.Empty
                    } else {
                        MessageInformationUIState.Loaded
                    }
                }
                .onFailure { throwable ->
                    val exception = throwable as? CometChatException
                        ?: CometChatException(
                            "UNKNOWN_ERROR",
                            throwable.message ?: "Unknown error occurred"
                        )
                    _exception.value = exception
                    _state.value = MessageInformationUIState.Error(exception)
                }
        }
    }

    /**
     * Adds the real-time event listener.
     * Per design doc: addListener() logic.
     */
    fun addListener() {
        receiptEventsJob = viewModelScope.launch {
            eventListener.receiptEvents().collect { messageReceipt ->
                val currentMessage = message
                if (currentMessage != null && messageReceipt.messageId == currentMessage.id) {
                    setOrUpdate(messageReceipt)
                }
            }
        }
    }

    /**
     * Removes the real-time event listener.
     * Per design doc: removeListener() logic.
     */
    fun removeListener() {
        receiptEventsJob?.cancel()
        receiptEventsJob = null
    }

    // ==================== Private Methods ====================

    /**
     * Handles USER conversation - creates receipt from message data.
     * Per design doc: USER conversation handling in setMessage().
     */
    private fun handleUserConversation(baseMessage: BaseMessage) {
        // Clear existing receipts
        messageReceipts.clear()

        // Create receipt from message data
        val receipt = repository.createReceiptFromMessage(baseMessage)

        // Only add if delivered (deliveredAt != 0)
        if (receipt.deliveredAt != 0L) {
            addToTop(receipt)
            _listData.value = messageReceipts.toList()
            _state.value = MessageInformationUIState.Loaded
        } else {
            _state.value = MessageInformationUIState.Empty
        }
    }

    /**
     * Sets the receipt list.
     */
    private fun setList(receipts: List<MessageReceipt>) {
        messageReceipts.clear()
        messageReceipts.addAll(receipts)
        _listData.value = messageReceipts.toList()
    }

    /**
     * Updates or adds a receipt based on whether it already exists.
     * Per design doc: setOrUpdate() logic.
     */
    private fun setOrUpdate(messageReceipt: MessageReceipt) {
        val existingReceipt = isPresent(messageReceipt)
        if (existingReceipt != null) {
            update(messageReceipt, existingReceipt)
        } else {
            addToTop(messageReceipt)
        }
    }

    /**
     * Checks if a receipt for the same sender already exists.
     * Per design doc: isPresent() logic - matches by sender UID (case-insensitive).
     */
    private fun isPresent(messageReceipt: MessageReceipt): MessageReceipt? {
        return messageReceipts.find {
            it.sender?.uid.equals(messageReceipt.sender?.uid, ignoreCase = true)
        }
    }

    /**
     * Updates an existing receipt with new timestamp data.
     * Per design doc: update() logic - preserves existing timestamps, only updates new ones.
     */
    private fun update(newReceipt: MessageReceipt, oldReceipt: MessageReceipt) {
        // Preserve existing timestamps, only update new ones
        oldReceipt.deliveredAt = if (newReceipt.deliveredAt == 0L) {
            oldReceipt.deliveredAt
        } else {
            newReceipt.deliveredAt
        }
        oldReceipt.readAt = if (newReceipt.readAt == 0L) {
            oldReceipt.readAt
        } else {
            newReceipt.readAt
        }

        val index = messageReceipts.indexOf(oldReceipt)
        if (index >= 0) {
            messageReceipts[index] = oldReceipt
            _listData.value = messageReceipts.toList()
            _state.value = MessageInformationUIState.Loaded
            _updateReceipt.value = index
        }
    }

    /**
     * Adds a receipt to the top of the list.
     * Per design doc: addToTop() logic - inserts at index 0.
     */
    private fun addToTop(messageReceipt: MessageReceipt?) {
        if (messageReceipt != null) {
            messageReceipts.add(0, messageReceipt)
            if (messageReceipts.size == 1) {
                _state.value = MessageInformationUIState.Loaded
            }
            _listData.value = messageReceipts.toList()
            _addReceipt.value = 0
        }
    }

    // ==================== Lifecycle ====================

    override fun onCleared() {
        super.onCleared()
        removeListener()
        messageReceipts.clear()
    }
}
