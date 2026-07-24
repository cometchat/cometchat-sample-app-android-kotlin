package com.cometchat.uikit.core.viewmodel

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.SettingsRepo
import com.cometchat.chat.core.UploadFileRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import com.cometchat.chat.upload.UploadFileItem
import com.cometchat.chat.upload.UploadFileListener
import com.cometchat.chat.upload.UploadResult
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.models.AttachmentSource
import com.cometchat.uikit.core.models.AttachmentUploadStatus
import com.cometchat.uikit.core.models.AttachmentUploadTile
import com.cometchat.uikit.core.models.StagedAttachmentInput
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
import com.cometchat.uikit.core.utils.AgentChatDetector
import com.cometchat.uikit.core.CometChatAIStreamService
import com.cometchat.uikit.core.domain.model.StreamingState
import org.json.JSONObject
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val _editMessage = MutableStateFlow<BaseMessage?>(null)
    val editMessage: StateFlow<BaseMessage?> = _editMessage.asStateFlow()

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

    // ==================== Attachment Staging State ====================

    /**
     * Ordered list of attachments staged in the composer tray, one [AttachmentUploadTile] per
     * SDK-assigned `fileId`. Empty when nothing is staged (the tray renders nothing). Insertion
     * order is preserved; consumed by both the Compose and Views composer trays.
     */
    private val _attachmentTiles = MutableStateFlow<List<AttachmentUploadTile>>(emptyList())
    val attachmentTiles: StateFlow<List<AttachmentUploadTile>> = _attachmentTiles.asStateFlow()

    /**
     * True while at least one attachment is staged. Drives the mic↔send swap (staged attachments
     * count as "content") and the tray's visibility.
     */
    val hasStagedAttachments: StateFlow<Boolean> = _attachmentTiles
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * True only when there is ≥1 staged attachment and **every** staged attachment has finished
     * uploading ([AttachmentUploadStatus.DONE]). This is the all-or-nothing send gate from the
     * design: the send button must stay disabled while any tile is still uploading, failed, or
     * rejected.
     */
    val attachmentsAllUploaded: StateFlow<Boolean> = _attachmentTiles
        .map { tiles -> tiles.isNotEmpty() && tiles.all { it.status == AttachmentUploadStatus.DONE } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Effective per-message attachment cap: the server's `fileCount` setting, guarded against a
     * misconfigured dashboard value (some apps set count == byte-size), falling back to
     * [DEFAULT_MAX_ATTACHMENT_COUNT]. The UIs use this for the picker selection limit and the
     * "tray full" gate; [stageAttachments] enforces it as the backstop.
     */
    val maxAttachmentCount: Int
        get() {
            val raw = runCatching { SettingsRepo.getSettings()?.fileCount ?: 0 }.getOrDefault(0)
            return if (raw in 1..1000) raw else DEFAULT_MAX_ATTACHMENT_COUNT
        }

    // ==================== Internal State ====================

    private var receiverId: String = ""
    private var receiverType: String = ""
    private var parentMessageId: Long = -1
    private var listenersTag: String? = null
    private var isAgentChat: Boolean = false

    /**
     * The SDK upload request for the current staging batch — created on the first staged file,
     * released (with [UploadFileRequest.clearAll]) on send or chat switch. One request = one batch:
     * its `batchId` is what the split send stamps into message metadata.
     */
    private var uploadFileRequest: UploadFileRequest? = null

    /** Sequence for app-minted fileIds, unique within the current upload batch. */
    private var stagedFileIdSeq = 0

    /** Original picked files by fileId — kept so a FAILED upload can be retried by re-uploading the same fileId. */
    private val stagedFilesById = mutableMapOf<String, File>()

    /** Shared listener for every upload batch; routes SDK callbacks to tiles by `fileId`. */
    private val mediaUploadListener: UploadFileListener by lazy { createUploadFileListener() }

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
        if (receiverId != user.uid || receiverType != UIKitConstants.ReceiverType.USER) {
            clearAttachments()
        }
        _user.value = user
        _group.value = null
        receiverId = user.uid
        receiverType = UIKitConstants.ReceiverType.USER
        isAgentChat = AgentChatDetector.isAgentChat(user)
        updateIdMap()
    }

    /**
     * Sets the group as the message receiver.
     * Clears any existing user receiver.
     * 
     * @param group The Group object to set as receiver
     */
    fun setGroup(group: Group) {
        if (receiverId != group.guid || receiverType != UIKitConstants.ReceiverType.GROUP) {
            clearAttachments()
        }
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
     * Changing the thread target resets any staged attachment batch — the SDK upload
     * request captures `parentMessageId` at creation (it is sent with every presign),
     * so a batch staged for one thread must not leak into another.
     *
     * @param id The parent message ID
     */
    fun setParentMessageId(id: Long) {
        if (parentMessageId != id) {
            clearAttachments()
        }
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



    // ==================== Compose Text ====================

    /**
     * Sets the compose text programmatically.
     * 
     * @param text The text to set in the compose field
     */
    fun setComposeText(text: String) {
        _composeText.value = text
    }

    /**
     * Clears the compose text after it has been consumed by the UI.
     */
    fun clearComposeText() {
        _composeText.value = ""
    }

    // ==================== Edit Message ====================

    /**
     * Sets the message to be edited.
     * Clears any existing reply message and updates UI state.
     *
     * @param message The message to edit (TextMessage text or MediaMessage caption)
     */
    fun setEditMessage(message: BaseMessage) {
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

    /** Job for monitoring stream completion as a fallback safety mechanism. */
    private var streamMonitorJob: Job? = null

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
            // Re-register stream callback to ensure it's on the current service instance.
            // The service instance is created by MessageListViewModel, which may initialize
            // after the ComposerViewModel. Re-registering here guarantees the callback
            // is set on the correct instance when AI generation starts.
            addStreamCallback()
            // Start monitoring stream states as a safety fallback.
            // If the onStreamCompleted callback doesn't fire for any reason
            // (e.g., timing issues, instance mismatch), this observer will
            // detect when all streams have completed and reset the state.
            startStreamMonitor()
        } else {
            _uiState.value = MessageComposerUIState.Idle
            streamMonitorJob?.cancel()
            streamMonitorJob = null
        }
    }

    /**
     * Monitors the stream service's streamingStates as a fallback mechanism.
     * When all active streaming runs complete or get interrupted,
     * resets the AI generating state independently of the onStreamCallback.
     *
     * This ensures the stop button always transitions back to send button,
     * even if the scroll-to-bottom or other UI actions disrupt the callback flow.
     */
    private fun startStreamMonitor() {
        streamMonitorJob?.cancel()
        streamMonitorJob = viewModelScope.launch {
            val service = CometChatAIStreamService.getInstance() ?: return@launch
            var hasSeenStreaming = false
            service.streamingStates.collect { states ->
                if (!_isAIGenerating.value) return@collect

                // Track if we've ever seen an active streaming state
                if (states.values.any { it is StreamingState.Streaming }) {
                    hasSeenStreaming = true
                }

                // If we've seen streaming activity and now all states are
                // completed/interrupted, or all states were cleaned up (empty),
                // reset the generating state.
                if (hasSeenStreaming) {
                    val allDone = states.isEmpty() || states.values.all { state ->
                        state is StreamingState.Completed || state is StreamingState.Interrupted
                    }
                    if (allDone) {
                        setAIGenerating(false)
                    }
                }
            }
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

        // Show the thinking/buffering indicator immediately when the user taps
        // send in an agent chat, matching the v5 Java reference where
        // updateComposerState(true) is called before the network request.
        if (isAgentChat) {
            setAIGenerating(true)
        }

        viewModelScope.launch {
            _uiState.value = MessageComposerUIState.Sending

            sendTextMessageUseCase(message)
                .onSuccess { sentMsg ->
                    _sentMessage.value = sentMsg
                    _replyMessage.value = null
                    _uiState.value = MessageComposerUIState.Success(sentMsg)

                    // Handle AI agent chat — update parentMessageId for threading
                    if (isAgentChat) {
                        if (parentMessageId == -1L) {
                            parentMessageId = sentMsg.id.toLong()
                            updateIdMap()
                        }
                    } else {
                        _uiState.value = MessageComposerUIState.Idle
                    }
                    
                    // Emit reply success event if was replying
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.ReplyToMessage(sentMsg, MessageStatus.SUCCESS)
                    )
                }
                .onFailure { e ->
                    // Reset AI generating state on failure so the composer
                    // returns to the normal send button state.
                    if (isAgentChat) {
                        setAIGenerating(false)
                    }
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

        // Show the thinking/buffering indicator immediately when the user taps
        // send in an agent chat, matching the v5 Java reference where
        // updateComposerState(true) is called before the network request.
        if (isAgentChat) {
            setAIGenerating(true)
        }

        viewModelScope.launch {
            _uiState.value = MessageComposerUIState.Sending

            sendTextMessageUseCase(message)
                .onSuccess { sentMsg ->
                    _sentMessage.value = sentMsg
                    _replyMessage.value = null
                    _uiState.value = MessageComposerUIState.Success(sentMsg)

                    // Handle AI agent chat — update parentMessageId for threading
                    if (isAgentChat) {
                        if (parentMessageId == -1L) {
                            parentMessageId = sentMsg.id.toLong()
                            updateIdMap()
                        }
                    } else {
                        _uiState.value = MessageComposerUIState.Idle
                    }
                    
                    // Emit reply success event if was replying
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.ReplyToMessage(sentMsg, MessageStatus.SUCCESS)
                    )
                }
                .onFailure { e ->
                    // Reset AI generating state on failure so the composer
                    // returns to the normal send button state.
                    if (isAgentChat) {
                        setAIGenerating(false)
                    }
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
    fun sendMediaMessage(file: File, contentType: String, isVoiceNote: Boolean = false) {
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

        // ENG-36737: mark a mic-recorded voice note so the receive side routes it to
        // VoiceNoteBubble. Uses the DD / iOS-compatible metaData["audioType"] = "voice_note".
        // Picker audio has no flag → AudiosBubble.
        if (isVoiceNote) {
            message.metadata = (message.metadata ?: JSONObject()).apply {
                put(UIKitConstants.JSONKeys.AUDIO_TYPE, UIKitConstants.JSONKeys.AUDIO_TYPE_VOICE_NOTE)
            }
        }

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

    // ==================== Attachment Staging ====================

    /**
     * Stages one or more picked files in the composer tray and starts uploading them.
     *
     * Each accepted input becomes an [AttachmentUploadTile] (status [AttachmentUploadStatus.UPLOADING]),
     * keyed by the app-minted `fileId`. Upload progress and terminal states are reflected back onto
     * the tiles by [mediaUploadListener].
     *
     * The per-message **count** guard is enforced here (UIKit-side, before the SDK): inputs beyond
     * the server-configured `fileCount` are dropped and an error is emitted. The per-file **size**
     * guard is enforced by the SDK during upload and surfaces as [AttachmentUploadStatus.REJECTED]
     * via `onFileError`.
     *
     * @param inputs The files to stage, in the order they should appear in the tray.
     */
    fun stageAttachments(inputs: List<StagedAttachmentInput>) {
        if (inputs.isEmpty()) return

        val maxCount = maxAttachmentCount
        val available = (maxCount - _attachmentTiles.value.size).coerceAtLeast(0)
        if (available <= 0) {
            emitMaxAttachmentsError(maxCount)
            return
        }
        val accepted = inputs.take(available)
        if (accepted.size < inputs.size) {
            emitMaxAttachmentsError(maxCount)
        }

        val items = try {
            val request = uploadFileRequest ?: CometChat.createUploadFileRequest(receiverId, receiverType)
                .setBatchId(UUID.randomUUID().toString()) // app provides the batch id; SDK generates only if omitted
                .apply { if (parentMessageId > -1L) setParentMessageId(parentMessageId) }
                .also {
                    uploadFileRequest = it
                    stagedFileIdSeq = 0
                }
            // App-minted fileIds (unique within the batch) — the id exists before any
            // byte moves, and the SDK echoes it back unchanged on every event.
            val items = accepted.map { UploadFileItem("${request.batchId}_${stagedFileIdSeq++}", it.file) }
            items.forEachIndexed { i, item -> stagedFilesById[item.fileId] = accepted[i].file }
            request.uploadAttachments(items, mediaUploadListener)
            items
        } catch (e: Exception) {
            val exception = if (e is CometChatException) e
                else CometChatException("UPLOAD_ERROR", e.message ?: "Failed to start upload")
            viewModelScope.launch { _errorEvent.emit(exception) }
            return
        }

        val newTiles = accepted.mapIndexed { index, input ->
            AttachmentUploadTile(
                fileId = items[index].fileId,
                name = input.name,
                size = input.size,
                mimeType = input.mimeType,
                category = input.category,
                total = input.size,
                status = AttachmentUploadStatus.UPLOADING,
                source = input.source,
                localUri = input.localUri ?: input.file.absolutePath,
                durationMillis = input.durationMillis
            )
        }
        _attachmentTiles.value = _attachmentTiles.value + newTiles
    }

    /**
     * Removes a staged attachment. If it is still uploading, the in-flight upload is cancelled
     * first. Used for both the tray's "cancel" (while uploading) and "remove" (otherwise) intents.
     */
    fun removeAttachment(tile: AttachmentUploadTile) {
        // The SDK handles every state: aborts an in-flight upload, or drops an
        // already-uploaded file from the batch.
        stagedFilesById.remove(tile.fileId)
        runCatching { uploadFileRequest?.removeAttachment(tile.fileId) }
        _attachmentTiles.value = _attachmentTiles.value.filterNot { it.fileId == tile.fileId }
    }

    /**
     * Retries a [AttachmentUploadStatus.FAILED] attachment's upload via the SDK. Rejected tiles are
     * not retryable and are ignored.
     */
    fun retryAttachment(tile: AttachmentUploadTile) {
        val request = uploadFileRequest ?: return
        updateTile(tile.fileId) {
            it.copy(status = AttachmentUploadStatus.UPLOADING, percent = 0, loaded = 0L, error = null)
        }
        // No dedicated retry in the SDK: re-uploading under the SAME fileId replaces
        // the failed entry with a fresh one (re-presigns automatically) and its
        // events land on this same tile.
        val file = stagedFilesById[tile.fileId]
        if (file == null) {
            updateTile(tile.fileId) {
                it.copy(status = AttachmentUploadStatus.FAILED,
                    error = CometChatException("RETRY_ERROR", "Original file no longer available"))
            }
            return
        }
        runCatching { request.uploadAttachment(tile.fileId, file, mediaUploadListener) }
            .onFailure { e ->
                val exception = if (e is CometChatException) e
                    else CometChatException("RETRY_ERROR", e.message ?: "Failed to retry upload")
                updateTile(tile.fileId) { it.copy(status = AttachmentUploadStatus.FAILED, error = exception) }
            }
    }

    /**
     * Clears the tray, cancelling any in-flight uploads. Called on send and when switching chats.
     */
    fun clearAttachments() {
        // clearAll() aborts any in-flight uploads and releases the batch from SDK memory.
        runCatching { uploadFileRequest?.clearAll() }
        uploadFileRequest = null
        stagedFilesById.clear()
        _attachmentTiles.value = emptyList()
    }

    /**
     * Sends all staged attachments as a **single** [MediaMessage] carrying the uploaded
     * [Attachment]s (via `setAttachments`), with the compose text as the message caption.
     *
     * No-op unless every staged tile has finished uploading ([attachmentsAllUploaded]) — the
     * caller should keep the send button disabled until then. The tray is cleared optimistically
     * once the message enters the send pipeline.
     *
     * @param caption Optional caption (the current compose text), trimmed; omitted when blank.
     */
    fun sendStagedAttachments(caption: String? = null) {
        val tiles = _attachmentTiles.value
        if (tiles.isEmpty() || tiles.any { it.status != AttachmentUploadStatus.DONE }) return
        if (tiles.none { it.attachment != null }) return

        // ENG-36737: split the staged attachments into one MediaMessage per type (image → video →
        // audio → file), all sharing a batchId so the message list can group them (avatar/name on
        // the first, time/receipt + caption on the last — matched by batchId list adjacency, iOS
        // contract). A single-type send yields one message with no batchId and behaves as before.
        val buckets = buildBatchBuckets(tiles)
        // The SDK request owns the batch id (iOS contract: captured BEFORE the request is
        // released); the UUID fallback only covers a tray restored without a live request.
        val batchId = uploadFileRequest?.batchId ?: UUID.randomUUID().toString()
        val batchSize = buckets.size
        val trimmedCaption = caption?.trim()?.takeIf { it.isNotEmpty() }
        val quotedMsg = _replyMessage.value

        val messages = buckets.mapIndexed { index, (type, bucketAttachments) ->
            MediaMessage(receiverId, type, receiverType).apply {
                this.attachments = bucketAttachments
                // Caption lives on the last message of the batch only.
                if (index == batchSize - 1) trimmedCaption?.let { this.caption = it }
                if (this@CometChatMessageComposerViewModel.parentMessageId > -1L) {
                    this.parentMessageId = this@CometChatMessageComposerViewModel.parentMessageId
                }
                // TODO(ENG-36737): reply/thread interplay across a split batch is deferred; for now
                // the quoted reply is attached to the first bubble only.
                if (index == 0) quotedMsg?.let { q ->
                    this.quotedMessage = q
                    this.quotedMessageId = q.id.toLong()
                }
                // Only batchId — grouping is by list adjacency, not index/count (iOS contract).
                if (batchSize > 1) {
                    this.metadata = (this.metadata ?: JSONObject()).apply {
                        put(UIKitConstants.JSONKeys.BATCH_ID, batchId)
                    }
                }
            }
        }

        // Bytes are already on storage — release the SDK batch (pure release, nothing in
        // flight since every tile is DONE), then clear the tray optimistically.
        runCatching { uploadFileRequest?.clearAll() }
        uploadFileRequest = null
        stagedFilesById.clear()
        _attachmentTiles.value = emptyList()

        viewModelScope.launch {
            _uiState.value = MessageComposerUIState.Sending
            var lastSent: BaseMessage? = null
            var failure: CometChatException? = null
            // Send sequentially so batchIndex order matches delivery/sentAt order.
            for (message in messages) {
                sendMediaMessageUseCase(message)
                    .onSuccess { sentMsg ->
                        lastSent = sentMsg
                        _sentMessage.value = sentMsg
                    }
                    .onFailure { e ->
                        failure = if (e is CometChatException) e
                            else CometChatException("SEND_ERROR", e.message ?: "Unknown error")
                    }
                if (failure != null) break
            }
            failure?.let { exception ->
                _errorEvent.emit(exception)
                _uiState.value = MessageComposerUIState.Error(exception)
                return@launch
            }
            _replyMessage.value = null
            lastSent?.let { sentMsg ->
                _uiState.value = MessageComposerUIState.Success(sentMsg)
                CometChatEvents.emitMessageEvent(
                    CometChatMessageEvent.ReplyToMessage(sentMsg, MessageStatus.SUCCESS)
                )
            }
            _uiState.value = MessageComposerUIState.Idle
        }
    }

    /** Applies [transform] to the tile with [fileId], leaving the rest untouched. */
    private fun updateTile(fileId: String, transform: (AttachmentUploadTile) -> AttachmentUploadTile) {
        _attachmentTiles.value = _attachmentTiles.value.map { if (it.fileId == fileId) transform(it) else it }
    }

    private fun emitMaxAttachmentsError(maxCount: Int) {
        viewModelScope.launch {
            _errorEvent.emit(
                CometChatException(ERROR_MAX_ATTACHMENTS_EXCEEDED, "You can attach up to $maxCount files at a time.")
            )
        }
    }

    companion object {
        /** Fallback attachment cap when the server settings are unavailable or misconfigured. */
        const val DEFAULT_MAX_ATTACHMENT_COUNT = 10

        /** Error code emitted on [errorEvent] when picked files are dropped by the count guard. */
        const val ERROR_MAX_ATTACHMENTS_EXCEEDED = "MAX_ATTACHMENTS_EXCEEDED"
    }

    /**
     * Splits the staged tiles into one bucket per type, in the fixed batch order
     * image → video → audio → file, dropping empty buckets. Each non-empty bucket becomes its own
     * MediaMessage in [sendStagedAttachments].
     *
     * Bucketing is by the tile's [AttachmentUploadTile.category] — the **picker** the file came
     * through — not the MIME type, so a photo chosen via the file picker is sent as a `file`
     * message, matching its document tile in the tray.
     */
    private fun buildBatchBuckets(tiles: List<AttachmentUploadTile>): List<Pair<String, List<Attachment>>> {
        val order = listOf(
            CometChatConstants.MESSAGE_TYPE_IMAGE,
            CometChatConstants.MESSAGE_TYPE_VIDEO,
            CometChatConstants.MESSAGE_TYPE_AUDIO,
            CometChatConstants.MESSAGE_TYPE_FILE,
        )
        val grouped = tiles.groupBy { it.category }
        return order.mapNotNull { type ->
            grouped[type]
                ?.mapNotNull { it.attachment }
                ?.takeIf { it.isNotEmpty() }
                ?.let { type to it }
        }
    }

    /** Builds the shared [UploadFileListener] that maps SDK upload callbacks onto staged tiles. */
    private fun createUploadFileListener(): UploadFileListener = object : UploadFileListener() {
        override fun onFileProgress(fileId: String, uploadedBytes: Long, totalBytes: Long, percentage: Int) {
            updateTile(fileId) {
                it.copy(
                    percent = percentage.coerceIn(0, 100),
                    loaded = uploadedBytes,
                    total = if (totalBytes > 0) totalBytes else it.total,
                    status = AttachmentUploadStatus.UPLOADING
                )
            }
        }

        override fun onFileUploaded(fileId: String, attachment: Attachment) {
            updateTile(fileId) {
                it.copy(
                    status = AttachmentUploadStatus.DONE,
                    percent = 100,
                    attachment = attachment,
                    error = null,
                    mimeType = attachment.fileMimeType ?: it.mimeType
                )
            }
        }

        override fun onFileFailure(fileId: String, exception: CometChatException) {
            updateTile(fileId) { it.copy(status = AttachmentUploadStatus.FAILED, error = exception) }
        }

        override fun onFileError(fileId: String, exception: CometChatException) {
            updateTile(fileId) { it.copy(status = AttachmentUploadStatus.REJECTED, error = exception) }
            viewModelScope.launch { _errorEvent.emit(exception) }
        }

        override fun onComplete(uploadResult: UploadResult) {
            uploadResult.rejected?.forEach { failure ->
                updateTile(failure.fileId) { it.copy(status = AttachmentUploadStatus.REJECTED, error = failure.error) }
            }
            uploadResult.failed?.forEach { failure ->
                updateTile(failure.fileId) {
                    if (it.status == AttachmentUploadStatus.DONE) it
                    else it.copy(status = AttachmentUploadStatus.FAILED, error = failure.error)
                }
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
     * For text messages the text is replaced; for media messages the caption is replaced.
     *
     * @param newText The new text content for the message
     */
    fun editMessage(newText: String) {
        val originalMessage = _editMessage.value ?: return
        if (newText.isBlank()) return

        // Build a fresh message carrying only the updated content, so a failed edit
        // can't leak the new text into the original instance shown in the list.
        val editedMessage: BaseMessage = when (originalMessage) {
            is MediaMessage -> MediaMessage(
                originalMessage.receiverUid,
                originalMessage.type,
                originalMessage.receiverType
            ).apply {
                // Re-send the attachments so the server keeps the media on the edited
                // message — the attachment array itself preserves it, no metadata needed.
                // We deliberately do NOT copy the original metadata: an edit should send a
                // clean payload (no server-injected @injected / receipt fields), matching
                // the other platforms. setAttachment appends into the attachments list, so
                // set exactly one of the two.
                val originalAttachments = originalMessage.attachments
                if (!originalAttachments.isNullOrEmpty()) {
                    attachments = originalAttachments
                } else {
                    originalMessage.attachment?.let { attachment = it }
                }
                caption = newText.trim()
            }
            is TextMessage -> TextMessage(
                originalMessage.receiverUid,
                newText.trim(),
                originalMessage.receiverType
            )
            else -> return
        }
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
     * When true, no typing indicator events are sent to the SDK.
     */
    var disableTypingEvents: Boolean = false

    /**
     * Tracks whether a typing session is currently active (startTyping sent
     * without a matching endTyping yet).
     */
    private var isTypingActive = false

    /**
     * Debounce job that ends the typing session after a period of inactivity.
     */
    private var typingDebounceJob: Job? = null

    /**
     * Starts a typing session and (re)schedules the debounced end.
     * Call on every keystroke: the first call sends the startTyping event,
     * subsequent calls only reset the inactivity timer, and [endTyping] runs
     * automatically after [UIKitConstants.UIKitUtilityConstants.TYPING_INDICATOR_DEBOUNCER]
     * milliseconds without another call.
     * Only sends if typing events are enabled and the user is not blocked.
     */
    open fun startTyping() {
        if (disableTypingEvents || isUserBlocked()) return
        if (!isTypingActive) {
            isTypingActive = true
            CometChat.startTyping(TypingIndicator(receiverId, receiverType))
        }
        typingDebounceJob?.cancel()
        typingDebounceJob = viewModelScope.launch {
            delay(UIKitConstants.UIKitUtilityConstants.TYPING_INDICATOR_DEBOUNCER.toLong())
            endTyping()
        }
    }

    /**
     * Ends the active typing session, if any, and cancels the pending
     * debounced end. Safe to call when no session is active.
     * Only sends if typing events are enabled and the user is not blocked.
     */
    open fun endTyping() {
        typingDebounceJob?.cancel()
        typingDebounceJob = null
        if (!isTypingActive) return
        isTypingActive = false
        if (!disableTypingEvents && !isUserBlocked()) {
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
            addStreamCallback()
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
                            (event.message is TextMessage || event.message is MediaMessage) &&
                            matchesIdMap(event.message)
                        ) {
                            setEditMessage(event.message)
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
                    is CometChatUIEvent.AgentChatThreadResolved -> {
                        // Sync parentMessageId when the MessageList resolves the
                        // agent chat thread (e.g., via fetchLastAgentConversation).
                        // Without this, the Composer would still have parentMessageId=-1
                        // and sent messages would lack thread context.
                        if (isAgentChat && event.receiverId == receiverId) {
                            parentMessageId = event.parentMessageId
                            updateIdMap()
                        }
                    }
                    else -> {}
                }
            }
        }
    }


    /**
     * Registers a callback on CometChatAIStreamService to reset AI generating state
     * when the stream completes or is interrupted.
     * Matches Java: CometChatAIStreamService.setOnStreamCallBack(...)
     *
     * Called from setUser() when agent chat is detected, and also re-registered
     * whenever AI generating state is set to true (to handle the case where the
     * service instance is created after the ViewModel).
     */
    private fun addStreamCallback() {
        CometChatAIStreamService.getInstance()?.setOnStreamCallback(
            object : CometChatAIStreamService.OnStreamCallback {
                override fun onStreamCompleted() {
                    setAIGenerating(false)
                }

                override fun onStreamInterrupted() {
                    setAIGenerating(false)
                }
            }
        )
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
        // Clear the stream callback to avoid leaks
        CometChatAIStreamService.getInstance()?.setOnStreamCallback(null)
    }

    override fun onCleared() {
        endTyping()
        super.onCleared()
        streamMonitorJob?.cancel()
        streamMonitorJob = null
        removeListeners()
    }
}
