package com.cometchat.uikit.core.viewmodel

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.CometChatMessageComposerAction
import com.cometchat.uikit.core.domain.usecase.EditMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendCustomMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendTextMessageUseCase
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.events.CometChatUIEvent
import com.cometchat.uikit.core.events.CustomUIPosition
import com.cometchat.uikit.core.events.MessageStatus
import com.cometchat.uikit.core.state.ComposerPanelEvent
import com.cometchat.uikit.core.state.MessageComposerUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Recording state enum for the audio recorder.
 * Represents the different states of the audio recording process.
 */
enum class RecordingState {
    /** Initial state, ready to start recording */
    START,
    /** Actively recording audio */
    RECORDING,
    /** Recording is paused */
    PAUSED,
    /** Recording is complete, ready to send or preview */
    STOPPED
}

/**
 * Sealed class representing the different modes of the message composer.
 * Used to manage the UI state transitions between normal mode, attachment popup, and recording.
 */
sealed class ComposerMode {
    /** Normal text input mode */
    object Normal : ComposerMode()
    
    /** Attachment popup is open */
    object AttachmentPopupOpen : ComposerMode()
    
    /** Audio recording mode with the current recording state */
    data class Recording(val state: RecordingState) : ComposerMode()
}

/**
 * ViewModel for the CometChatMessageComposer component.
 * Manages UI state for composing and sending messages.
 * 
 * This ViewModel is shared by both chatuikit-jetpack (Compose) and chatuikit-kotlin (Views)
 * implementations, ensuring consistent behavior across both UI frameworks.
 * 
 * Features:
 * - Text, media, and custom message sending
 * - Message editing
 * - Reply/quote message handling
 * - Typing indicator management
 * - AI assistant integration
 * - Panel event handling for extensibility
 * - Event listener management for edit/reply events
 * 
 * @param sendTextMessageUseCase Use case for sending text messages
 * @param sendMediaMessageUseCase Use case for sending media messages
 * @param sendCustomMessageUseCase Use case for sending custom messages
 * @param editMessageUseCase Use case for editing messages
 * @param enableListeners Whether to enable CometChat listeners (set to false for testing)
 */
open class CometChatMessageComposerViewModel(
    private val sendTextMessageUseCase: SendTextMessageUseCase,
    private val sendMediaMessageUseCase: SendMediaMessageUseCase,
    private val sendCustomMessageUseCase: SendCustomMessageUseCase,
    private val editMessageUseCase: EditMessageUseCase,
    private val enableListeners: Boolean = true
) : ViewModel() {

    // ==================== UI State ====================

    /**
     * Current UI state of the message composer.
     */
    private val _uiState = MutableStateFlow<MessageComposerUIState>(MessageComposerUIState.Idle)
    val uiState: StateFlow<MessageComposerUIState> = _uiState.asStateFlow()

    // ==================== User/Group State ====================

    /**
     * User state (receiver for one-on-one conversations).
     */
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    /**
     * Group state (receiver for group conversations).
     */
    private val _group = MutableStateFlow<Group?>(null)
    val group: StateFlow<Group?> = _group.asStateFlow()

    // ==================== Compose State ====================

    /**
     * Current compose text in the input field.
     */
    private val _composeText = MutableStateFlow("")
    val composeText: StateFlow<String> = _composeText.asStateFlow()

    /**
     * Message being edited (null if not in edit mode).
     */
    private val _editMessage = MutableStateFlow<TextMessage?>(null)
    val editMessage: StateFlow<TextMessage?> = _editMessage.asStateFlow()

    /**
     * Message being replied to (null if not in reply mode).
     */
    private val _replyMessage = MutableStateFlow<BaseMessage?>(null)
    val replyMessage: StateFlow<BaseMessage?> = _replyMessage.asStateFlow()

    /**
     * Last successfully sent message (for callbacks).
     */
    private val _sentMessage = MutableStateFlow<BaseMessage?>(null)
    val sentMessage: StateFlow<BaseMessage?> = _sentMessage.asStateFlow()

    // ==================== AI State ====================

    /**
     * Whether AI is currently generating a response.
     */
    private val _isAIGenerating = MutableStateFlow(false)
    val isAIGenerating: StateFlow<Boolean> = _isAIGenerating.asStateFlow()

    // ==================== Composer Mode State ====================

    /**
     * Current mode of the message composer (Normal, AttachmentPopupOpen, or Recording).
     */
    private val _composerMode = MutableStateFlow<ComposerMode>(ComposerMode.Normal)
    val composerMode: StateFlow<ComposerMode> = _composerMode.asStateFlow()

    /**
     * Whether the attachment popup is currently expanded.
     */
    private val _isAttachmentPopupExpanded = MutableStateFlow(false)
    val isAttachmentPopupExpanded: StateFlow<Boolean> = _isAttachmentPopupExpanded.asStateFlow()

    // ==================== Recording State ====================

    /**
     * Current recording time formatted as MM:SS.
     */
    private val _recordingTime = MutableStateFlow("00:00")
    val recordingTime: StateFlow<String> = _recordingTime.asStateFlow()

    /**
     * Path to the recorded audio file.
     */
    private val _recordedFilePath = MutableStateFlow<String?>(null)
    val recordedFilePath: StateFlow<String?> = _recordedFilePath.asStateFlow()

    /**
     * Current audio amplitude for waveform visualization (0.0 to 1.0).
     */
    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    // ==================== ID Map ====================

    /**
     * ID map for receiver identification.
     * Contains RECEIVER_ID, RECEIVER_TYPE, and optionally PARENT_MESSAGE_ID.
     */
    private val _idMap = MutableStateFlow<HashMap<String, String>>(HashMap())
    val idMap: StateFlow<HashMap<String, String>> = _idMap.asStateFlow()

    // ==================== Events ====================

    /**
     * Panel events for showing/hiding custom panels.
     */
    private val _panelEvents = MutableSharedFlow<ComposerPanelEvent>()
    val panelEvents: SharedFlow<ComposerPanelEvent> = _panelEvents.asSharedFlow()

    /**
     * Error events for UI error handling.
     */
    private val _errorEvent = MutableSharedFlow<CometChatException>()
    val errorEvent: SharedFlow<CometChatException> = _errorEvent.asSharedFlow()

    // ==================== Attachment Option Visibility State ====================

    /**
     * Visibility flag for Camera attachment option.
     * Default is true (visible).
     */
    private val _showCameraOption = MutableStateFlow(true)
    val showCameraOption: StateFlow<Boolean> = _showCameraOption.asStateFlow()

    /**
     * Visibility flag for Image attachment option.
     * Default is true (visible).
     */
    private val _showImageOption = MutableStateFlow(true)
    val showImageOption: StateFlow<Boolean> = _showImageOption.asStateFlow()

    /**
     * Visibility flag for Video attachment option.
     * Default is true (visible).
     */
    private val _showVideoOption = MutableStateFlow(true)
    val showVideoOption: StateFlow<Boolean> = _showVideoOption.asStateFlow()

    /**
     * Visibility flag for Audio attachment option.
     * Default is true (visible).
     */
    private val _showAudioOption = MutableStateFlow(true)
    val showAudioOption: StateFlow<Boolean> = _showAudioOption.asStateFlow()

    /**
     * Visibility flag for File/Document attachment option.
     * Default is true (visible).
     */
    private val _showFileOption = MutableStateFlow(true)
    val showFileOption: StateFlow<Boolean> = _showFileOption.asStateFlow()

    /**
     * Visibility flag for Poll attachment option.
     * Default is false (hidden). Set to true to enable this extension option.
     * In the reference implementation, this option is only added by PollsExtensionDecorator
     * when the extension is enabled.
     */
    private val _showPollOption = MutableStateFlow(false)
    val showPollOption: StateFlow<Boolean> = _showPollOption.asStateFlow()

    /**
     * Visibility flag for Collaborative Document attachment option.
     * Default is false (hidden). Set to true to enable this extension option.
     * In the reference implementation, this option is only added by CollaborativeDocumentExtensionDecorator
     * when the extension is enabled.
     */
    private val _showCollaborativeDocumentOption = MutableStateFlow(false)
    val showCollaborativeDocumentOption: StateFlow<Boolean> = _showCollaborativeDocumentOption.asStateFlow()

    /**
     * Visibility flag for Collaborative Whiteboard attachment option.
     * Default is false (hidden). Set to true to enable this extension option.
     * In the reference implementation, this option is only added by CollaborativeWhiteboardExtensionDecorator
     * when the extension is enabled.
     */
    private val _showCollaborativeWhiteboardOption = MutableStateFlow(false)
    val showCollaborativeWhiteboardOption: StateFlow<Boolean> = _showCollaborativeWhiteboardOption.asStateFlow()

    /**
     * Custom attachment options added by the developer.
     * These are appended after the default options.
     */
    private val _customAttachmentOptions = MutableStateFlow<List<CometChatMessageComposerAction>>(emptyList())

    // ==================== Internal State ====================

    private var receiverId: String = ""
    private var receiverType: String = ""
    private var parentMessageId: Long = -1
    private var listenersTag: String? = null
    private var isAgentChat: Boolean = false

    // Event listener jobs
    private var messageEventsJob: Job? = null
    private var uiEventsJob: Job? = null

    init {
        if (enableListeners) {
            addListeners()
        }
    }


    // ==================== Receiver Configuration ====================

    /**
     * Sets the user as the message receiver.
     * Clears any existing group receiver.
     * 
     * @param user The User object to set as receiver
     */
    fun setUser(user: User) {
        _user.value = user
        _group.value = null
        receiverId = user.uid
        receiverType = UIKitConstants.ReceiverType.USER
        isAgentChat = isAgentChatUser(user)
        updateIdMap()
    }

    /**
     * Sets the group as the message receiver.
     * Clears any existing user receiver.
     * 
     * @param group The Group object to set as receiver
     */
    fun setGroup(group: Group) {
        _group.value = group
        _user.value = null
        receiverId = group.guid
        receiverType = UIKitConstants.ReceiverType.GROUP
        isAgentChat = false
        updateIdMap()
    }

    /**
     * Sets the parent message ID for threaded messages.
     * 
     * @param id The parent message ID
     */
    fun setParentMessageId(id: Long) {
        parentMessageId = id
        updateIdMap()
    }

    /**
     * Updates the ID map with current receiver information.
     * Called internally when receiver or parent message changes.
     */
    private fun updateIdMap() {
        val map = HashMap<String, String>()
        if (parentMessageId > 0) {
            map[UIKitConstants.MapId.PARENT_MESSAGE_ID] = parentMessageId.toString()
        }
        map[UIKitConstants.MapId.RECEIVER_ID] = receiverId
        map[UIKitConstants.MapId.RECEIVER_TYPE] = receiverType
        _idMap.value = map
    }

    /**
     * Checks if the user is an AI agent chat user.
     * 
     * @param user The user to check
     * @return True if the user is an AI agent
     */
    private fun isAgentChatUser(user: User): Boolean {
        return user.uid.startsWith(UIKitConstants.AIConstants.AGENTIC_USER)
    }

    // ==================== Compose Text ====================

    /**
     * Sets the compose text programmatically.
     * 
     * @param text The text to set in the compose field
     */
    fun setComposeText(text: String) {
        _composeText.value = text
    }

    // ==================== Edit Message ====================

    /**
     * Sets the message to be edited.
     * Clears any existing reply message and updates UI state.
     * 
     * @param message The TextMessage to edit
     */
    fun setEditMessage(message: TextMessage) {
        _editMessage.value = message
        _replyMessage.value = null
        _uiState.value = MessageComposerUIState.Editing(message)
    }

    /**
     * Clears the edit message state.
     * Returns UI to idle state.
     */
    fun clearEditMessage() {
        _editMessage.value = null
        _uiState.value = MessageComposerUIState.Idle
    }

    // ==================== Reply Message ====================

    /**
     * Sets the message to be replied to.
     * Clears any existing edit message and updates UI state.
     * 
     * @param message The BaseMessage to reply to
     */
    fun setReplyMessage(message: BaseMessage) {
        _replyMessage.value = message
        _editMessage.value = null
        _uiState.value = MessageComposerUIState.Replying(message)
    }

    /**
     * Clears the reply message state.
     * Returns UI to idle state.
     */
    fun clearReplyMessage() {
        _replyMessage.value = null
        _uiState.value = MessageComposerUIState.Idle
    }

    // ==================== AI State ====================

    /**
     * Sets the AI generating state.
     * Updates UI state accordingly.
     * 
     * @param generating True if AI is generating, false otherwise
     */
    fun setAIGenerating(generating: Boolean) {
        _isAIGenerating.value = generating
        if (generating) {
            _uiState.value = MessageComposerUIState.AIGenerating
        } else {
            _uiState.value = MessageComposerUIState.Idle
        }
    }

    // ==================== Attachment Option Visibility Setters ====================

    /**
     * Sets the visibility of the Camera attachment option.
     * 
     * @param visible True to show, false to hide
     */
    fun setCameraOptionVisibility(visible: Boolean) {
        _showCameraOption.value = visible
    }

    /**
     * Sets the visibility of the Image attachment option.
     * 
     * @param visible True to show, false to hide
     */
    fun setImageOptionVisibility(visible: Boolean) {
        _showImageOption.value = visible
    }

    /**
     * Sets the visibility of the Video attachment option.
     * 
     * @param visible True to show, false to hide
     */
    fun setVideoOptionVisibility(visible: Boolean) {
        _showVideoOption.value = visible
    }

    /**
     * Sets the visibility of the Audio attachment option.
     * 
     * @param visible True to show, false to hide
     */
    fun setAudioOptionVisibility(visible: Boolean) {
        _showAudioOption.value = visible
    }

    /**
     * Sets the visibility of the File/Document attachment option.
     * 
     * @param visible True to show, false to hide
     */
    fun setFileOptionVisibility(visible: Boolean) {
        _showFileOption.value = visible
    }

    /**
     * Sets the visibility of the Poll attachment option.
     * 
     * @param visible True to show, false to hide
     */
    fun setPollOptionVisibility(visible: Boolean) {
        _showPollOption.value = visible
    }

    /**
     * Sets the visibility of the Collaborative Document attachment option.
     * 
     * @param visible True to show, false to hide
     */
    fun setCollaborativeDocumentOptionVisibility(visible: Boolean) {
        _showCollaborativeDocumentOption.value = visible
    }

    /**
     * Sets the visibility of the Collaborative Whiteboard attachment option.
     * 
     * @param visible True to show, false to hide
     */
    fun setCollaborativeWhiteboardOptionVisibility(visible: Boolean) {
        _showCollaborativeWhiteboardOption.value = visible
    }

    // ==================== Attachment Options Utility Functions ====================

    /**
     * Returns all 8 default attachment options filtered by visibility flags.
     * Also filters out extension options (Poll, Document, Whiteboard) in threaded contexts.
     * Custom options are appended at the end.
     * 
     * @param context Android context for accessing resources
     * @param cameraTitle Localized title for Camera option
     * @param cameraIcon Drawable resource for Camera icon
     * @param imageTitle Localized title for Image option
     * @param imageIcon Drawable resource for Image icon
     * @param videoTitle Localized title for Video option
     * @param videoIcon Drawable resource for Video icon
     * @param audioTitle Localized title for Audio option
     * @param audioIcon Drawable resource for Audio icon
     * @param fileTitle Localized title for File option
     * @param fileIcon Drawable resource for File icon
     * @param pollTitle Localized title for Poll option
     * @param pollIcon Drawable resource for Poll icon
     * @param collaborativeDocumentTitle Localized title for Collaborative Document option
     * @param collaborativeDocumentIcon Drawable resource for Collaborative Document icon
     * @param collaborativeWhiteboardTitle Localized title for Collaborative Whiteboard option
     * @param collaborativeWhiteboardIcon Drawable resource for Collaborative Whiteboard icon
     * @return List of visible attachment options
     */
    fun getDefaultAttachmentOptions(
        cameraTitle: String,
        cameraIcon: Int,
        imageTitle: String,
        imageIcon: Int,
        videoTitle: String,
        videoIcon: Int,
        audioTitle: String,
        audioIcon: Int,
        fileTitle: String,
        fileIcon: Int,
        pollTitle: String,
        pollIcon: Int,
        collaborativeDocumentTitle: String,
        collaborativeDocumentIcon: Int,
        collaborativeWhiteboardTitle: String,
        collaborativeWhiteboardIcon: Int
    ): List<CometChatMessageComposerAction> {
        val isThreaded = parentMessageId > 0
        val options = mutableListOf<CometChatMessageComposerAction>()

        // Add default options based on visibility flags
        if (_showCameraOption.value) {
            options.add(
                CometChatMessageComposerAction(
                    id = CometChatMessageComposerAction.ID_CAMERA,
                    title = cameraTitle,
                    icon = cameraIcon
                )
            )
        }
        if (_showImageOption.value) {
            options.add(
                CometChatMessageComposerAction(
                    id = CometChatMessageComposerAction.ID_IMAGE,
                    title = imageTitle,
                    icon = imageIcon
                )
            )
        }
        if (_showVideoOption.value) {
            options.add(
                CometChatMessageComposerAction(
                    id = CometChatMessageComposerAction.ID_VIDEO,
                    title = videoTitle,
                    icon = videoIcon
                )
            )
        }
        if (_showAudioOption.value) {
            options.add(
                CometChatMessageComposerAction(
                    id = CometChatMessageComposerAction.ID_AUDIO,
                    title = audioTitle,
                    icon = audioIcon
                )
            )
        }
        if (_showFileOption.value) {
            options.add(
                CometChatMessageComposerAction(
                    id = CometChatMessageComposerAction.ID_DOCUMENT,
                    title = fileTitle,
                    icon = fileIcon
                )
            )
        }

        // Extension options - only add if not in threaded context
        if (!isThreaded) {
            if (_showPollOption.value) {
                options.add(
                    CometChatMessageComposerAction(
                        id = CometChatMessageComposerAction.ID_POLL,
                        title = pollTitle,
                        icon = pollIcon
                    )
                )
            }
            if (_showCollaborativeDocumentOption.value) {
                options.add(
                    CometChatMessageComposerAction(
                        id = CometChatMessageComposerAction.ID_COLLABORATIVE_DOCUMENT,
                        title = collaborativeDocumentTitle,
                        icon = collaborativeDocumentIcon
                    )
                )
            }
            if (_showCollaborativeWhiteboardOption.value) {
                options.add(
                    CometChatMessageComposerAction(
                        id = CometChatMessageComposerAction.ID_COLLABORATIVE_WHITEBOARD,
                        title = collaborativeWhiteboardTitle,
                        icon = collaborativeWhiteboardIcon
                    )
                )
            }
        }

        // Add custom options at the end
        options.addAll(_customAttachmentOptions.value)

        return options
    }

    /**
     * Replaces the entire custom attachment options list.
     * 
     * @param options The new list of custom attachment options
     */
    fun setAttachmentOptions(options: List<CometChatMessageComposerAction>) {
        _customAttachmentOptions.value = options
    }

    /**
     * Adds a custom attachment option to the list.
     * 
     * @param option The attachment option to add
     */
    fun addAttachmentOption(option: CometChatMessageComposerAction) {
        _customAttachmentOptions.value = _customAttachmentOptions.value + option
    }

    // ==================== Composer Mode Management ====================

    /**
     * Toggles the attachment popup between open and closed states.
     * If currently in Normal mode, opens the popup.
     * If currently in AttachmentPopupOpen mode, closes the popup.
     */
    fun toggleAttachmentPopup() {
        when (_composerMode.value) {
            is ComposerMode.Normal -> {
                _composerMode.value = ComposerMode.AttachmentPopupOpen
                _isAttachmentPopupExpanded.value = true
            }
            is ComposerMode.AttachmentPopupOpen -> {
                _composerMode.value = ComposerMode.Normal
                _isAttachmentPopupExpanded.value = false
            }
            is ComposerMode.Recording -> {
                // Cannot toggle popup while recording
            }
        }
    }

    /**
     * Sets the attachment popup expanded state directly.
     * 
     * @param expanded True to open the popup, false to close it
     */
    fun setAttachmentPopupExpanded(expanded: Boolean) {
        _isAttachmentPopupExpanded.value = expanded
        _composerMode.value = if (expanded) {
            ComposerMode.AttachmentPopupOpen
        } else {
            ComposerMode.Normal
        }
    }

    /**
     * Starts the recording mode.
     * Transitions the composer to Recording mode with START state.
     */
    fun startRecordingMode() {
        // Close attachment popup if open
        if (_composerMode.value is ComposerMode.AttachmentPopupOpen) {
            _isAttachmentPopupExpanded.value = false
        }
        _composerMode.value = ComposerMode.Recording(RecordingState.START)
        _recordingTime.value = "00:00"
        _recordedFilePath.value = null
        _audioAmplitude.value = 0f
    }

    /**
     * Sets the recording state within Recording mode.
     * Only valid when already in Recording mode.
     * 
     * @param state The new recording state
     */
    fun setRecordingState(state: RecordingState) {
        val currentMode = _composerMode.value
        if (currentMode is ComposerMode.Recording) {
            // Validate state transitions
            val isValidTransition = when (currentMode.state) {
                RecordingState.START -> state == RecordingState.RECORDING
                RecordingState.RECORDING -> state == RecordingState.PAUSED || state == RecordingState.STOPPED
                RecordingState.PAUSED -> state == RecordingState.RECORDING || state == RecordingState.STOPPED
                RecordingState.STOPPED -> state == RecordingState.START
            }
            
            if (isValidTransition) {
                _composerMode.value = ComposerMode.Recording(state)
            }
        }
    }

    /**
     * Updates the recording time display.
     * 
     * @param time The formatted time string (MM:SS)
     */
    fun updateRecordingTime(time: String) {
        _recordingTime.value = time
    }

    /**
     * Updates the recorded file path.
     * 
     * @param path The path to the recorded audio file
     */
    fun setRecordedFilePath(path: String?) {
        _recordedFilePath.value = path
    }

    /**
     * Updates the audio amplitude for waveform visualization.
     * 
     * @param amplitude The amplitude value (0.0 to 1.0)
     */
    fun updateAudioAmplitude(amplitude: Float) {
        _audioAmplitude.value = amplitude.coerceIn(0f, 1f)
    }

    /**
     * Exits recording mode and returns to Normal mode.
     * Should be called after sending or deleting a recording.
     */
    fun exitRecordingMode() {
        _composerMode.value = ComposerMode.Normal
        _recordingTime.value = "00:00"
        _recordedFilePath.value = null
        _audioAmplitude.value = 0f
    }

    /**
     * Gets the current recording state if in Recording mode.
     * 
     * @return The current RecordingState or null if not in Recording mode
     */
    fun getCurrentRecordingState(): RecordingState? {
        return (_composerMode.value as? ComposerMode.Recording)?.state
    }

    /**
     * Checks if the composer is currently in recording mode.
     * 
     * @return True if in Recording mode, false otherwise
     */
    fun isInRecordingMode(): Boolean {
        return _composerMode.value is ComposerMode.Recording
    }


    // ==================== Message Creation ====================

    /**
     * Creates a TextMessage object from the given text.
     * 
     * @param text The message text
     * @return TextMessage object or null if text is blank
     */
    fun createTextMessage(text: String): TextMessage? {
        if (text.isBlank()) return null
        val message = TextMessage(receiverId, text.trim(), receiverType)
        if (parentMessageId > -1L) {
            message.parentMessageId = parentMessageId
        }
        return message
    }

    /**
     * Creates a MediaMessage object from the given file and content type.
     * 
     * @param file The media file to send
     * @param contentType The MIME type of the file
     * @return MediaMessage object or null if file doesn't exist or is empty
     */
    fun createMediaMessage(file: File, contentType: String): MediaMessage? {
        android.util.Log.d("MessageComposerVM", "createMediaMessage: file=${file.absolutePath}, exists=${file.exists()}, size=${file.length()}, contentType=$contentType")
        if (!file.exists() || file.length() == 0L) {
            android.util.Log.e("MessageComposerVM", "createMediaMessage: file doesn't exist or is empty, returning null")
            return null
        }
        android.util.Log.d("MessageComposerVM", "createMediaMessage: receiverId=$receiverId, receiverType=$receiverType")
        val message = MediaMessage(receiverId, file, contentType, receiverType)
        android.util.Log.d("MessageComposerVM", "createMediaMessage: MediaMessage created, attachment=${message.attachment}")
        if (parentMessageId > -1L) {
            message.parentMessageId = parentMessageId
        }
        return message
    }

    // ==================== Message Sending ====================

    /**
     * Sends a text message.
     * Attaches quoted message if present using SDK's quotedMessageId and quotedMessage properties.
     * 
     * @param text The message text to send
     */
    fun sendTextMessage(text: String) {
        val message = createTextMessage(text) ?: return

        // Attach quoted message if present (matches Java CometChatMessageComposer behavior)
        _replyMessage.value?.let { quotedMsg ->
            message.quotedMessage = quotedMsg
            message.quotedMessageId = quotedMsg.id.toLong()
        }

        viewModelScope.launch {
            _uiState.value = MessageComposerUIState.Sending

            sendTextMessageUseCase(message)
                .onSuccess { sentMsg ->
                    _sentMessage.value = sentMsg
                    _replyMessage.value = null
                    _uiState.value = MessageComposerUIState.Success(sentMsg)

                    // Handle AI agent chat
                    if (isAgentChat) {
                        if (parentMessageId == -1L) {
                            parentMessageId = sentMsg.id.toLong()
                            updateIdMap()
                        }
                        _isAIGenerating.value = true
                        _uiState.value = MessageComposerUIState.AIGenerating
                    } else {
                        _uiState.value = MessageComposerUIState.Idle
                    }
                    
                    // Emit reply success event if was replying
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.ReplyToMessage(sentMsg, MessageStatus.SUCCESS)
                    )
                }
                .onFailure { e ->
                    val exception = if (e is CometChatException) e 
                        else CometChatException("SEND_ERROR", e.message ?: "Unknown error")
                    _errorEvent.emit(exception)
                    _uiState.value = MessageComposerUIState.Error(exception)
                }
        }
    }

    /**
     * Sends a pre-created text message with mentioned users already set.
     * This is used when the message has been prepared with mentions by text formatters.
     * Attaches quoted message if present using SDK's quotedMessageId and quotedMessage properties.
     * 
     * @param message The TextMessage to send (with mentionedUsers already set)
     */
    fun sendTextMessageWithMentions(message: TextMessage) {
        // Attach quoted message if present (matches Java CometChatMessageComposer behavior)
        _replyMessage.value?.let { quotedMsg ->
            message.quotedMessage = quotedMsg
            message.quotedMessageId = quotedMsg.id.toLong()
        }

        viewModelScope.launch {
            _uiState.value = MessageComposerUIState.Sending

            sendTextMessageUseCase(message)
                .onSuccess { sentMsg ->
                    _sentMessage.value = sentMsg
                    _replyMessage.value = null
                    _uiState.value = MessageComposerUIState.Success(sentMsg)

                    // Handle AI agent chat
                    if (isAgentChat) {
                        if (parentMessageId == -1L) {
                            parentMessageId = sentMsg.id.toLong()
                            updateIdMap()
                        }
                        _isAIGenerating.value = true
                        _uiState.value = MessageComposerUIState.AIGenerating
                    } else {
                        _uiState.value = MessageComposerUIState.Idle
                    }
                    
                    // Emit reply success event if was replying
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.ReplyToMessage(sentMsg, MessageStatus.SUCCESS)
                    )
                }
                .onFailure { e ->
                    val exception = if (e is CometChatException) e 
                        else CometChatException("SEND_ERROR", e.message ?: "Unknown error")
                    _errorEvent.emit(exception)
                    _uiState.value = MessageComposerUIState.Error(exception)
                }
        }
    }

    /**
     * Sends a media message.
     * Attaches quoted message if present using SDK's quotedMessageId and quotedMessage properties.
     * 
     * @param file The media file to send
     * @param contentType The MIME type of the file
     */
    fun sendMediaMessage(file: File, contentType: String) {
        android.util.Log.d("MessageComposerVM", "sendMediaMessage: file=${file.absolutePath}, exists=${file.exists()}, size=${file.length()}, contentType=$contentType")
        val message = createMediaMessage(file, contentType)
        if (message == null) {
            android.util.Log.e("MessageComposerVM", "sendMediaMessage: createMediaMessage returned null, aborting")
            // Emit error event when file is invalid
            viewModelScope.launch {
                val exception = CometChatException("FILE_ERROR", "File does not exist or is empty: ${file.absolutePath}")
                _errorEvent.emit(exception)
                _uiState.value = MessageComposerUIState.Error(exception)
            }
            return
        }
        android.util.Log.d("MessageComposerVM", "sendMediaMessage: MediaMessage created successfully")

        // Attach quoted message if present (matches Java CometChatMessageComposer behavior)
        _replyMessage.value?.let { quotedMsg ->
            message.quotedMessage = quotedMsg
            message.quotedMessageId = quotedMsg.id.toLong()
        }

        viewModelScope.launch {
            _uiState.value = MessageComposerUIState.Sending

            sendMediaMessageUseCase(message)
                .onSuccess { sentMsg ->
                    _sentMessage.value = sentMsg
                    _replyMessage.value = null
                    _uiState.value = MessageComposerUIState.Success(sentMsg)
                    _uiState.value = MessageComposerUIState.Idle
                    
                    // Emit reply success event if was replying
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.ReplyToMessage(sentMsg, MessageStatus.SUCCESS)
                    )
                }
                .onFailure { e ->
                    val exception = if (e is CometChatException) e 
                        else CometChatException("SEND_ERROR", e.message ?: "Unknown error")
                    _errorEvent.emit(exception)
                    _uiState.value = MessageComposerUIState.Error(exception)
                }
        }
    }

    /**
     * Sends a custom message.
     * 
     * @param message The CustomMessage to send
     */
    fun sendCustomMessage(message: CustomMessage) {
        viewModelScope.launch {
            _uiState.value = MessageComposerUIState.Sending

            sendCustomMessageUseCase(message)
                .onSuccess { sentMsg ->
                    _sentMessage.value = sentMsg
                    _uiState.value = MessageComposerUIState.Success(sentMsg)
                    _uiState.value = MessageComposerUIState.Idle
                }
                .onFailure { e ->
                    val exception = if (e is CometChatException) e 
                        else CometChatException("SEND_ERROR", e.message ?: "Unknown error")
                    _errorEvent.emit(exception)
                    _uiState.value = MessageComposerUIState.Error(exception)
                }
        }
    }

    /**
     * Sends a sticker message as a custom message.
     * Creates a CustomMessage with type "extension_sticker" containing the sticker URL and name.
     * Handles parent message ID for threaded messages and quoted message if present.
     * 
     * Matches the Java StickerExtensionDecorator behavior:
     * - Sets quotedMessageId and quotedMessage on the CustomMessage when replying
     * - Validates that the quoted message belongs to the same conversation
     * - Only quotes if the quoted message is in the same thread context
     * 
     * @param stickerUrl The URL of the sticker image
     * @param stickerName The name/identifier of the sticker
     * @param pushNotificationMessage The push notification message to display (e.g., "Shared a Sticker")
     */
    fun sendStickerMessage(stickerUrl: String, stickerName: String, pushNotificationMessage: String) {
        if (receiverId.isEmpty()) return

        viewModelScope.launch {
            try {
                // Create sticker data JSON
                val stickerData = org.json.JSONObject().apply {
                    put("sticker_url", stickerUrl)
                    put("sticker_name", stickerName)
                }

                // Create metadata JSON
                val metadata = org.json.JSONObject().apply {
                    put("incrementUnreadCount", true)
                    put("pushNotification", pushNotificationMessage)
                }

                // Create the custom message
                val customMessage = CustomMessage(
                    receiverId,
                    receiverType,
                    UIKitConstants.MessageType.EXTENSION_STICKER,
                    stickerData
                )
                customMessage.shouldUpdateConversation(true)
                customMessage.metadata = metadata

                // Set parent message ID for threaded messages
                if (parentMessageId > -1L) {
                    customMessage.parentMessageId = parentMessageId
                }

                // Handle quoted message (reply functionality)
                // Matches Java StickerExtensionDecorator behavior
                _replyMessage.value?.let { quotedMsg ->
                    val quotedMessageId = getQuotedMessageId(quotedMsg)
                    if (quotedMessageId > -1) {
                        // Only set quoted message if it's in the same thread context
                        if (quotedMsg.parentMessageId == customMessage.parentMessageId) {
                            customMessage.quotedMessageId = quotedMessageId
                            customMessage.quotedMessage = quotedMsg
                        }
                    }
                }

                _uiState.value = MessageComposerUIState.Sending

                sendCustomMessageUseCase(customMessage)
                    .onSuccess { sentMsg ->
                        _sentMessage.value = sentMsg
                        _replyMessage.value = null
                        _uiState.value = MessageComposerUIState.Success(sentMsg)
                        _uiState.value = MessageComposerUIState.Idle

                        // Emit reply success event if was replying
                        CometChatEvents.emitMessageEvent(
                            CometChatMessageEvent.ReplyToMessage(sentMsg, MessageStatus.SUCCESS)
                        )
                    }
                    .onFailure { e ->
                        val exception = if (e is CometChatException) e
                            else CometChatException("SEND_ERROR", e.message ?: "Unknown error")
                        _errorEvent.emit(exception)
                        _uiState.value = MessageComposerUIState.Error(exception)
                    }
            } catch (e: Exception) {
                val exception = CometChatException("STICKER_ERROR", e.message ?: "Failed to send sticker")
                _errorEvent.emit(exception)
                _uiState.value = MessageComposerUIState.Error(exception)
            }
        }
    }

    /**
     * Gets the quoted message ID if the message belongs to the current conversation.
     * Validates that the quoted message is for the same user or group.
     * 
     * Matches the Java Utils.getQuotedMessageId() behavior.
     * 
     * @param quotedMessage The message being quoted/replied to
     * @return The message ID if valid, -1 otherwise
     */
    private fun getQuotedMessageId(quotedMessage: BaseMessage): Long {
        val receiver = quotedMessage.receiver ?: return -1
        
        return when {
            // User conversation - check if the quoted message is in the same user conversation
            _user.value != null && receiver is User -> {
                val conversationId = quotedMessage.conversationId ?: return -1
                val ids = conversationId.split("_")
                val isCorrectConversation = ids.any { it == _user.value?.uid }
                if (isCorrectConversation) quotedMessage.id.toLong() else -1
            }
            // Group conversation - check if the quoted message is in the same group
            _group.value != null && receiver is Group -> {
                if (receiver.guid == _group.value?.guid) quotedMessage.id.toLong() else -1
            }
            else -> -1
        }
    }

    // ==================== Message Editing ====================

    /**
     * Edits an existing message with new text.
     * 
     * @param newText The new text content for the message
     */
    fun editMessage(newText: String) {
        val originalMessage = _editMessage.value ?: return
        if (newText.isBlank()) return

        // Create a new TextMessage with the updated text
        val editedMessage = TextMessage(
            originalMessage.receiverUid,
            newText.trim(),
            originalMessage.receiverType
        )
        editedMessage.id = originalMessage.id

        viewModelScope.launch {
            _uiState.value = MessageComposerUIState.Sending

            editMessageUseCase(editedMessage)
                .onSuccess { editedMsg ->
                    _editMessage.value = null
                    _uiState.value = MessageComposerUIState.Success(editedMsg)
                    _uiState.value = MessageComposerUIState.Idle

                    // Emit edit success event
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.MessageEdited(editedMsg, MessageStatus.SUCCESS)
                    )
                }
                .onFailure { e ->
                    val exception = if (e is CometChatException) e 
                        else CometChatException("EDIT_ERROR", e.message ?: "Unknown error")
                    _errorEvent.emit(exception)
                    _uiState.value = MessageComposerUIState.Error(exception)

                    // Emit edit error event
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.MessageEdited(originalMessage, MessageStatus.ERROR)
                    )
                }
        }
    }

    // ==================== Typing Indicators ====================

    /**
     * Starts typing indicator.
     * Only sends if user is not blocked.
     */
    fun startTyping() {
        if (!isUserBlocked()) {
            CometChat.startTyping(TypingIndicator(receiverId, receiverType))
        }
    }

    /**
     * Ends typing indicator.
     * Only sends if user is not blocked.
     */
    fun endTyping() {
        if (!isUserBlocked()) {
            CometChat.endTyping(TypingIndicator(receiverId, receiverType))
        }
    }

    /**
     * Checks if the current user is blocked.
     * 
     * @return True if user is blocked in either direction
     */
    private fun isUserBlocked(): Boolean {
        return _user.value?.let { it.isBlockedByMe || it.isHasBlockedMe } ?: false
    }

    // ==================== Event Listeners ====================

    /**
     * Adds all event listeners.
     * Called during initialization if enableListeners is true.
     */
    private fun addListeners() {
        listenersTag = "MessageComposer_${System.currentTimeMillis()}"

        listenersTag?.let { tag ->
            addMessageEventListeners()
            addUIEventListeners()
        }
    }

    /**
     * Adds message event listeners for edit/reply events.
     */
    private fun addMessageEventListeners() {
        messageEventsJob = viewModelScope.launch {
            CometChatEvents.messageEvents.collect { event ->
                when (event) {
                    is CometChatMessageEvent.MessageEdited -> {
                        if (event.status == MessageStatus.IN_PROGRESS &&
                            event.message is TextMessage &&
                            matchesIdMap(event.message)
                        ) {
                            setEditMessage(event.message as TextMessage)
                        }
                    }
                    is CometChatMessageEvent.ReplyToMessage -> {
                        if (event.status == MessageStatus.IN_PROGRESS &&
                            matchesIdMap(event.message)
                        ) {
                            setReplyMessage(event.message)
                        } else if (event.status == MessageStatus.SUCCESS &&
                            matchesIdMap(event.message)
                        ) {
                            clearReplyMessage()
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Adds UI event listeners for panel show/hide and compose text events.
     */
    private fun addUIEventListeners() {
        uiEventsJob = viewModelScope.launch {
            CometChatEvents.uiEvents.collect { event ->
                when (event) {
                    is CometChatUIEvent.ShowPanel -> {
                        if (matchesIdMap(event.id)) {
                            when (event.position) {
                                CustomUIPosition.COMPOSER_TOP -> {
                                    @Suppress("UNCHECKED_CAST")
                                    val viewProvider = event.content as? ((Context) -> View)
                                    viewProvider?.let {
                                        _panelEvents.emit(ComposerPanelEvent.ShowTopPanel(it))
                                    }
                                }
                                CustomUIPosition.COMPOSER_BOTTOM -> {
                                    @Suppress("UNCHECKED_CAST")
                                    val viewProvider = event.content as? ((Context) -> View)
                                    viewProvider?.let {
                                        _panelEvents.emit(ComposerPanelEvent.ShowBottomPanel(it))
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                    is CometChatUIEvent.HidePanel -> {
                        if (matchesIdMap(event.id)) {
                            when (event.position) {
                                CustomUIPosition.COMPOSER_TOP -> {
                                    _panelEvents.emit(ComposerPanelEvent.CloseTopPanel)
                                }
                                CustomUIPosition.COMPOSER_BOTTOM -> {
                                    _panelEvents.emit(ComposerPanelEvent.CloseBottomPanel)
                                }
                                else -> {}
                            }
                        }
                    }
                    is CometChatUIEvent.ComposeMessage -> {
                        if (event.id == receiverId) {
                            _composeText.value = event.text
                        }
                    }
                    else -> {}
                }
            }
        }
    }


    // ==================== ID Map Matching ====================

    /**
     * Checks if the given ID map matches the current receiver.
     * 
     * @param id The ID map to check
     * @return True if the ID map matches
     */
    private fun matchesIdMap(id: Map<String, String>): Boolean {
        return _idMap.value == id
    }

    /**
     * Checks if the message matches the current receiver.
     * 
     * For one-to-one chats, this method correctly handles both sent and received messages:
     * - For sent messages: receiverUid is the conversation partner's UID
     * - For received messages: receiverUid is the logged-in user's UID, so we use sender's UID
     * 
     * This matches the Java reference implementation in Utils.getIdMap().
     * 
     * @param message The message to check
     * @return True if the message matches the current receiver
     */
    private fun matchesIdMap(message: BaseMessage): Boolean {
        val messageIdMap = HashMap<String, String>()
        
        // For user chats: if the logged-in user is the receiver, use sender's UID
        // This handles received messages correctly (matching Java Utils.getIdMap behavior)
        val loggedInUserUid = CometChat.getLoggedInUser()?.uid
        val receiverId = if (message.receiverUid.equals(loggedInUserUid, ignoreCase = true)) {
            // I'm the receiver, so use the sender's UID as the conversation partner
            message.sender?.uid ?: message.receiverUid
        } else {
            // I'm the sender, so use the receiverUid as the conversation partner
            message.receiverUid
        }
        
        messageIdMap[UIKitConstants.MapId.RECEIVER_ID] = receiverId
        messageIdMap[UIKitConstants.MapId.RECEIVER_TYPE] = message.receiverType
        if (message.parentMessageId > 0) {
            messageIdMap[UIKitConstants.MapId.PARENT_MESSAGE_ID] = message.parentMessageId.toString()
        }
        return _idMap.value == messageIdMap
    }

    // ==================== Extension Features (Poll, Collaborative) ====================

    /**
     * Creates a poll via the CometChat Extensions API.
     * Uses the PollDataSource to call the extensions API.
     * Handles quoted message if present.
     *
     * @param question The poll question
     * @param options The poll options as a JSONArray
     * @param onSuccess Callback invoked on successful poll creation
     * @param onError Callback invoked on error
     */
    fun createPoll(
        question: String,
        options: org.json.JSONArray,
        onSuccess: (() -> Unit)? = null,
        onError: ((CometChatException) -> Unit)? = null
    ) {
        if (receiverId.isEmpty()) {
            onError?.invoke(CometChatException("ERR_NO_RECEIVER", "No receiver set"))
            return
        }

        viewModelScope.launch {
            try {
                // Get quoted message ID if replying
                val quotedMessageId = _replyMessage.value?.let { getQuotedMessageId(it) }

                val jsonObject = org.json.JSONObject().apply {
                    put("question", question)
                    put("options", options)
                    put("receiver", receiverId)
                    put("receiverType", receiverType)
                    if (quotedMessageId != null && quotedMessageId > -1) {
                        put("quotedMessageId", quotedMessageId)
                    }
                }

                CometChat.callExtension(
                    "polls",
                    "POST",
                    "/v2/create",
                    jsonObject,
                    object : CometChat.CallbackListener<org.json.JSONObject>() {
                        override fun onSuccess(response: org.json.JSONObject?) {
                            // Clear reply message on success
                            _replyMessage.value?.let {
                                CometChatEvents.emitMessageEvent(
                                    CometChatMessageEvent.ReplyToMessage(it, MessageStatus.SUCCESS)
                                )
                            }
                            _replyMessage.value = null
                            onSuccess?.invoke()
                        }

                        override fun onError(exception: CometChatException) {
                            onError?.invoke(exception)
                        }
                    }
                )
            } catch (e: Exception) {
                val exception = CometChatException(
                    "ERR_POLL_CREATION",
                    e.message ?: "Failed to create poll"
                )
                onError?.invoke(exception)
            }
        }
    }

    /**
     * Creates a collaborative whiteboard via the CometChat Extensions API.
     * Handles quoted message if present.
     *
     * @param onSuccess Callback invoked on successful whiteboard creation
     * @param onError Callback invoked on error
     */
    fun createCollaborativeWhiteboard(
        onSuccess: (() -> Unit)? = null,
        onError: ((CometChatException) -> Unit)? = null
    ) {
        if (receiverId.isEmpty()) {
            onError?.invoke(CometChatException("ERR_NO_RECEIVER", "No receiver set"))
            return
        }

        viewModelScope.launch {
            try {
                // Get quoted message ID if replying
                val quotedMessageId = _replyMessage.value?.let { getQuotedMessageId(it) }

                val jsonObject = org.json.JSONObject().apply {
                    put("receiver", receiverId)
                    put("receiverType", receiverType)
                    if (quotedMessageId != null && quotedMessageId > -1) {
                        put("quotedMessageId", quotedMessageId)
                    }
                }

                CometChat.callExtension(
                    "whiteboard",
                    "POST",
                    "/v1/create",
                    jsonObject,
                    object : CometChat.CallbackListener<org.json.JSONObject>() {
                        override fun onSuccess(response: org.json.JSONObject?) {
                            // Clear reply message on success
                            _replyMessage.value?.let {
                                CometChatEvents.emitMessageEvent(
                                    CometChatMessageEvent.ReplyToMessage(it, MessageStatus.SUCCESS)
                                )
                            }
                            _replyMessage.value = null
                            onSuccess?.invoke()
                        }

                        override fun onError(exception: CometChatException) {
                            onError?.invoke(exception)
                        }
                    }
                )
            } catch (e: Exception) {
                val exception = CometChatException(
                    "ERR_WHITEBOARD_CREATION",
                    e.message ?: "Failed to create whiteboard"
                )
                onError?.invoke(exception)
            }
        }
    }

    /**
     * Creates a collaborative document via the CometChat Extensions API.
     * Handles quoted message if present.
     *
     * @param onSuccess Callback invoked on successful document creation
     * @param onError Callback invoked on error
     */
    fun createCollaborativeDocument(
        onSuccess: (() -> Unit)? = null,
        onError: ((CometChatException) -> Unit)? = null
    ) {
        if (receiverId.isEmpty()) {
            onError?.invoke(CometChatException("ERR_NO_RECEIVER", "No receiver set"))
            return
        }

        viewModelScope.launch {
            try {
                // Get quoted message ID if replying
                val quotedMessageId = _replyMessage.value?.let { getQuotedMessageId(it) }

                val jsonObject = org.json.JSONObject().apply {
                    put("receiver", receiverId)
                    put("receiverType", receiverType)
                    if (quotedMessageId != null && quotedMessageId > -1) {
                        put("quotedMessageId", quotedMessageId)
                    }
                }

                CometChat.callExtension(
                    "document",
                    "POST",
                    "/v1/create",
                    jsonObject,
                    object : CometChat.CallbackListener<org.json.JSONObject>() {
                        override fun onSuccess(response: org.json.JSONObject?) {
                            // Clear reply message on success
                            _replyMessage.value?.let {
                                CometChatEvents.emitMessageEvent(
                                    CometChatMessageEvent.ReplyToMessage(it, MessageStatus.SUCCESS)
                                )
                            }
                            _replyMessage.value = null
                            onSuccess?.invoke()
                        }

                        override fun onError(exception: CometChatException) {
                            onError?.invoke(exception)
                        }
                    }
                )
            } catch (e: Exception) {
                val exception = CometChatException(
                    "ERR_DOCUMENT_CREATION",
                    e.message ?: "Failed to create document"
                )
                onError?.invoke(exception)
            }
        }
    }

    // ==================== Cleanup ====================

    /**
     * Removes all event listeners.
     * Called when the ViewModel is cleared.
     */
    fun removeListeners() {
        messageEventsJob?.cancel()
        uiEventsJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}
