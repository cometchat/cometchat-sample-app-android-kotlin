package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.usecase.CreatePollUseCase
import com.cometchat.uikit.core.state.CreatePollUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray

/**
 * ViewModel for managing CreatePoll component state.
 * Handles poll creation via the CometChat Extensions API.
 * Uses StateFlow for reactive state management.
 *
 * This ViewModel is shared between chatuikit-jetpack (Compose) and
 * chatuikit-kotlin (XML Views) implementations.
 *
 * @param createPollUseCase Use case for creating polls
 */
open class CometChatCreatePollViewModel(
    private val createPollUseCase: CreatePollUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "CreatePollVM"
    }

    // ==================== UI State ====================

    /** Current UI state (Idle, Submitting, Success, Error) */
    private val _uiState = MutableStateFlow<CreatePollUIState>(CreatePollUIState.Idle)
    val uiState: StateFlow<CreatePollUIState> = _uiState.asStateFlow()

    // ==================== Form Data ====================

    /** The poll question */
    private val _question = MutableStateFlow("")
    val question: StateFlow<String> = _question.asStateFlow()

    /** The poll options */
    private val _options = MutableStateFlow<List<String>>(listOf("", ""))
    val options: StateFlow<List<String>> = _options.asStateFlow()

    // ==================== Callbacks ====================

    /** Callback invoked when poll is created successfully */
    var onPollCreated: (() -> Unit)? = null

    /** Callback invoked when poll creation fails */
    var onError: ((CometChatException) -> Unit)? = null

    // ==================== Public API Methods ====================

    /**
     * Updates the poll question.
     *
     * @param question The new question text
     */
    fun setQuestion(question: String) {
        _question.value = question
    }

    /**
     * Updates a poll option at the specified index.
     *
     * @param index The index of the option to update
     * @param value The new option text
     */
    fun updateOption(index: Int, value: String) {
        val currentOptions = _options.value.toMutableList()
        if (index in currentOptions.indices) {
            currentOptions[index] = value
            _options.value = currentOptions
        }
    }

    /**
     * Adds a new empty option to the poll.
     */
    fun addOption() {
        val currentOptions = _options.value.toMutableList()
        currentOptions.add("")
        _options.value = currentOptions
    }

    /**
     * Removes an option at the specified index.
     * Minimum 2 options are required.
     *
     * @param index The index of the option to remove
     */
    fun removeOption(index: Int) {
        val currentOptions = _options.value.toMutableList()
        if (currentOptions.size > 2 && index in currentOptions.indices) {
            currentOptions.removeAt(index)
            _options.value = currentOptions
        }
    }

    /**
     * Checks if the form is valid for submission.
     * Question must not be empty and at least 2 non-empty options are required.
     *
     * @return True if the form is valid
     */
    fun isFormValid(): Boolean {
        val questionValid = _question.value.isNotBlank()
        val validOptions = _options.value.filter { it.isNotBlank() }
        return questionValid && validOptions.size >= 2
    }

    /**
     * Creates a poll with the current form data.
     *
     * @param receiverId The ID of the receiver (user UID or group GUID)
     * @param receiverType The type of receiver ("user" or "group")
     * @param quotedMessageId Optional ID of the message being replied to
     */
    fun createPoll(
        receiverId: String,
        receiverType: String,
        quotedMessageId: Long? = null
    ) {
        if (!isFormValid()) return
        if (_uiState.value is CreatePollUIState.Submitting) return

        _uiState.value = CreatePollUIState.Submitting

        viewModelScope.launch {
            val optionsArray = JSONArray().apply {
                _options.value.filter { it.isNotBlank() }.forEach { put(it) }
            }

            createPollUseCase(
                question = _question.value,
                options = optionsArray,
                receiverId = receiverId,
                receiverType = receiverType,
                quotedMessageId = quotedMessageId
            ).onSuccess {
                _uiState.value = CreatePollUIState.Success
                onPollCreated?.invoke()
            }.onFailure { exception ->
                val cometChatException = exception as? CometChatException
                    ?: CometChatException("ERR_POLL_CREATION", exception.message ?: "Unknown error")
                _uiState.value = CreatePollUIState.Error(cometChatException)
                onError?.invoke(cometChatException)
            }
        }
    }

    /**
     * Resets the form to initial state.
     */
    fun reset() {
        _question.value = ""
        _options.value = listOf("", "")
        _uiState.value = CreatePollUIState.Idle
    }

    /**
     * Dismisses the error state and returns to idle.
     */
    fun dismissError() {
        if (_uiState.value is CreatePollUIState.Error) {
            _uiState.value = CreatePollUIState.Idle
        }
    }
}
