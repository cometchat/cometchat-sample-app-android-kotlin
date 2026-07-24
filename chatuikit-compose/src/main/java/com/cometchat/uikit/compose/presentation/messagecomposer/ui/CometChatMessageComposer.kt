package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import com.cometchat.uikit.compose.presentation.shared.mediaselection.createMediaSelectionResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatAttachmentTileStyle
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatAttachmentTrayStyle
import com.cometchat.uikit.compose.presentation.shared.erroralert.CometChatErrorAlert
import com.cometchat.uikit.compose.presentation.shared.erroralert.style.CometChatErrorAlertStyle
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.presentation.shared.popupmenu.PopupPosition
import com.cometchat.uikit.compose.shared.views.popupmenu.CometChatPopupMenuStyle
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.presentation.shared.mediarecorder.ui.CometChatMediaRecorder
import com.cometchat.uikit.compose.presentation.shared.mediarecorder.style.CometChatMediaRecorderStyle
import com.cometchat.uikit.compose.presentation.shared.inlineaudiorecorder.ui.CometChatInlineAudioRecorder
import com.cometchat.uikit.compose.presentation.shared.inlineaudiorecorder.style.CometChatInlineAudioRecorderStyle
import com.cometchat.uikit.compose.presentation.shared.mediaselection.MediaContentType
import com.cometchat.uikit.compose.presentation.shared.mediaselection.MediaSelectionResult
import com.cometchat.uikit.compose.presentation.shared.mediaselection.rememberMediaSelectionState
import com.cometchat.uikit.core.models.AttachmentSource
import com.cometchat.uikit.core.models.StagedAttachmentInput
import com.cometchat.uikit.core.models.defaultAttachmentCategory
import com.cometchat.uikit.core.factory.CometChatMessageComposerViewModelFactory
import com.cometchat.uikit.core.viewmodel.CometChatMediaRecorderViewModel
import com.cometchat.uikit.core.viewmodel.CometChatInlineAudioRecorderViewModel
import com.cometchat.uikit.core.formatter.RichTextConfiguration
import com.cometchat.uikit.core.formatter.RichTextEditorController
import com.cometchat.uikit.core.formatter.RichTextEditorState
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.core.formatter.RichTextFormatterManager
import com.cometchat.uikit.core.mentions.SelectedMention
import com.cometchat.uikit.core.formatter.RichTextSpan
import com.cometchat.uikit.core.formatter.ComposerSegment
import com.cometchat.uikit.core.formatter.SegmentComposerController
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.utils.AgentChatDetector
import com.cometchat.uikit.core.utils.extractMediaDurationMillis
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import com.cometchat.uikit.core.viewmodel.ComposerMode
import com.cometchat.uikit.core.viewmodel.RecordingState
import com.cometchat.uikit.core.domain.model.CometChatMessageComposerAction
import com.cometchat.uikit.core.domain.model.ComposerLayoutMode
import com.cometchat.uikit.compose.presentation.createpoll.ui.CometChatCreatePoll
import com.cometchat.uikit.compose.presentation.stickerkeyboard.ui.CometChatStickerKeyboard
import com.cometchat.uikit.compose.presentation.stickerkeyboard.style.CometChatStickerKeyboardStyle
import com.cometchat.uikit.compose.presentation.shared.suggestionlist.CometChatSuggestionList
import com.cometchat.uikit.compose.presentation.shared.suggestionlist.CometChatSuggestionListStyle
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.SuggestionItem
import com.cometchat.uikit.compose.presentation.shared.mentions.ComposeMentionState
import com.cometchat.uikit.compose.presentation.shared.mentions.detectMention
import com.cometchat.uikit.compose.presentation.shared.mentions.rememberMentionInsertionState
import com.cometchat.uikit.compose.presentation.shared.mentions.ComposeMentionInsertionState
import com.cometchat.uikit.compose.presentation.shared.mentions.ComposerMentionVisualTransformation
import com.cometchat.uikit.compose.presentation.shared.mentions.CombinedVisualTransformation
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.domain.model.Sticker
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import java.io.File

/**
 * CometChatMessageComposer is a comprehensive message composition component.
 * It provides text input, media attachments, voice recording, rich text formatting,
 * mentions, and AI integration.
 *
 * @param modifier Modifier for the composer
 * @param user User to send messages to (mutually exclusive with group)
 * @param group Group to send messages to (mutually exclusive with user)
 * @param parentMessageId Parent message ID for threaded messages
 * @param viewModel Optional ViewModel instance (creates default if not provided)
 * @param style Style configuration for the composer
 * @param attachmentPopupStyle Style configuration for the attachment popup menu
 * @param hideAttachmentButton Whether to hide the attachment button
 * @param hideVoiceRecordingButton Whether to hide the voice recording button
 * @param hideSendButton Whether to hide the send button
 * @param hideAuxiliaryButton Whether to hide auxiliary buttons (emoji, AI, stickers)
 * @param hideStickersButton Whether to hide the stickers button
 * @param enableRichTextFormatting Whether to enable rich text formatting toolbar below the text input
 * @param disableTypingEvents Whether to disable typing indicator events
 * @param disableSoundForMessages Whether to disable sound for sent messages
 * @param disableMentions Whether to disable @mentions functionality
 * @param maxLines Maximum lines for the text input
 * @param placeholderText Placeholder text for the input field
 * @param enabledFormats Set of enabled rich text formats
 * @param headerView Custom header view slot
 * @param footerView Custom footer view slot
 * @param sendButtonView Custom send button view
 * @param secondaryButtonView Custom secondary button view (attachment + mic)
 * @param auxiliaryButtonView Custom auxiliary button view
 * @param editPreviewView Custom edit preview view
 * @param replyPreviewView Custom reply preview view
 * @param onSendButtonClick Callback when send button is clicked
 * @param onError Callback when an error occurs
 * @param onTextChanged Callback when text changes
 * @param onCameraClick Callback when camera attachment option is clicked. Return `true` to skip default behavior, `false` or `null` to execute default.
 * @param onImageClick Callback when image attachment option is clicked. Return `true` to skip default behavior, `false` or `null` to execute default.
 * @param onVideoClick Callback when video attachment option is clicked. Return `true` to skip default behavior, `false` or `null` to execute default.
 * @param onAudioClick Callback when audio attachment option is clicked. Return `true` to skip default behavior, `false` or `null` to execute default.
 * @param onDocumentClick Callback when document attachment option is clicked. Return `true` to skip default behavior, `false` or `null` to execute default.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CometChatMessageComposer(
    modifier: Modifier = Modifier,
    // Data configuration
    user: User? = null,
    group: Group? = null,
    parentMessageId: Long = -1,
    // ViewModel
    viewModel: CometChatMessageComposerViewModel? = null,
    // Style
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    attachmentPopupStyle: CometChatPopupMenuStyle = CometChatPopupMenuStyle.default(),
    attachmentTrayStyle: CometChatAttachmentTrayStyle = CometChatAttachmentTrayStyle.default(),
    attachmentTileStyle: CometChatAttachmentTileStyle = CometChatAttachmentTileStyle.default(),
    attachmentErrorAlertStyle: CometChatErrorAlertStyle = CometChatErrorAlertStyle.default(),
    /**
     * When `true` (default), picking attachments stages them in a horizontal tray and uploads them
     * up front; the send button is gated until **all** staged attachments finish uploading, and a
     * single multi-attachment message is sent on tap. When `false`, every attachment option reverts
     * to the legacy single-pick, send-immediately behavior (no tray, no multi-upload).
     */
    enableMultipleAttachments: Boolean = true,
    // Visibility controls
    hideAttachmentButton: Boolean = false,
    hideVoiceRecordingButton: Boolean = false,
    hideSendButton: Boolean = false,
    hideAuxiliaryButton: Boolean = false,
    hideStickersButton: Boolean = false,
    // Rich text formatting - toolbar visible below text input when enabled
    enableRichTextFormatting: Boolean = false,
    // Composer layout mode - controls single-row vs two-row layout
    layoutMode: ComposerLayoutMode = ComposerLayoutMode.SINGLE_LINE,
    // Configuration
    disableTypingEvents: Boolean = false,
    disableSoundForMessages: Boolean = false,
    disableMentions: Boolean = false,
    maxLines: Int = 5,
    placeholderText: String? = null,
    // Rich text configuration
    enabledFormats: Set<RichTextFormat> = emptySet(),
    // Custom view slots
    headerView: (@Composable () -> Unit)? = null,
    footerView: (@Composable () -> Unit)? = null,
    sendButtonView: (@Composable (onClick: () -> Unit, isActive: Boolean, isAIGenerating: Boolean) -> Unit)? = null,
    secondaryButtonView: (@Composable (onAttachmentClick: () -> Unit, onVoiceRecordClick: () -> Unit) -> Unit)? = null,
    auxiliaryButtonView: (@Composable (User?, Group?, HashMap<String, String>) -> Unit)? = null,
    editPreviewView: (@Composable (BaseMessage, onClose: () -> Unit) -> Unit)? = null,
    replyPreviewView: (@Composable (BaseMessage, onClose: () -> Unit) -> Unit)? = null,
    // Callbacks
    onSendButtonClick: ((Context, BaseMessage) -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null,
    onTextChanged: ((String) -> Unit)? = null,
    // Attachment option callbacks - return true to override default behavior, false or null to execute default
    /**
     * Callback invoked when the camera attachment option is clicked.
     * @return `true` to skip default camera launch behavior (developer handles it),
     *         `false` or `null` to execute default camera launch behavior.
     */
    onCameraClick: (() -> Boolean)? = null,
    /**
     * Callback invoked when the image attachment option is clicked.
     * @return `true` to skip default image picker behavior (developer handles it),
     *         `false` or `null` to execute default image picker behavior.
     */
    onImageClick: (() -> Boolean)? = null,
    /**
     * Callback invoked when the video attachment option is clicked.
     * @return `true` to skip default video picker behavior (developer handles it),
     *         `false` or `null` to execute default video picker behavior.
     */
    onVideoClick: (() -> Boolean)? = null,
    /**
     * Callback invoked when the audio attachment option is clicked.
     * @return `true` to skip default audio picker behavior (developer handles it),
     *         `false` or `null` to execute default audio picker behavior.
     */
    onAudioClick: (() -> Boolean)? = null,
    /**
     * Callback invoked when the document attachment option is clicked.
     * @return `true` to skip default document picker behavior (developer handles it),
     *         `false` or `null` to execute default document picker behavior.
     */
    onDocumentClick: (() -> Boolean)? = null,
    // Sticker keyboard
    /**
     * Style configuration for the sticker keyboard.
     */
    stickerKeyboardStyle: CometChatStickerKeyboardStyle = CometChatStickerKeyboardStyle.default(),
    /**
     * Callback invoked when a sticker is selected from the keyboard.
     */
    onStickerSelected: ((Sticker) -> Unit)? = null,
    // Attachment option visibility controls
    /**
     * Whether to hide the Camera attachment option.
     * Default is false (visible).
     */
    hideCameraOption: Boolean = false,
    /**
     * Whether to hide the Image attachment option.
     * Default is false (visible).
     */
    hideImageOption: Boolean = false,
    /**
     * Whether to hide the Video attachment option.
     * Default is false (visible).
     */
    hideVideoOption: Boolean = false,
    /**
     * Whether to hide the Audio attachment option.
     * Default is false (visible).
     */
    hideAudioOption: Boolean = false,
    /**
     * Whether to hide the File/Document attachment option.
     * Default is false (visible).
     */
    hideFileOption: Boolean = false,
    /**
     * Whether to hide the Poll attachment option.
     * Default is true (hidden). Set to false to show this extension option.
     * In the reference implementation, this option is only added by PollsExtensionDecorator
     * when the extension is enabled.
     */
    hidePollOption: Boolean = true,
    /**
     * Whether to hide the Collaborative Document attachment option.
     * Default is true (hidden). Set to false to show this extension option.
     * In the reference implementation, this option is only added by CollaborativeDocumentExtensionDecorator
     * when the extension is enabled.
     */
    hideCollaborativeDocumentOption: Boolean = true,
    /**
     * Whether to hide the Collaborative Whiteboard attachment option.
     * Default is true (hidden). Set to false to show this extension option.
     * In the reference implementation, this option is only added by CollaborativeWhiteboardExtensionDecorator
     * when the extension is enabled.
     */
    hideCollaborativeWhiteboardOption: Boolean = true,
    // Custom attachment options
    /**
     * Custom attachment options to add after the default options.
     * These options appear at the end of the attachment popup menu.
     *
     * Example usage:
     * ```kotlin
     * CometChatMessageComposer(
     *     user = user,
     *     attachmentOptions = listOf(
     *         CometChatMessageComposerAction(
     *             id = "LOCATION",
     *             title = "Share Location",
     *             icon = R.drawable.ic_location
     *         )
     *     )
     * )
     * ```
     */
    attachmentOptions: List<CometChatMessageComposerAction> = emptyList(),
    /**
     * Callback invoked when a custom attachment option is clicked.
     * Receives the action that was clicked.
     *
     * Example usage:
     * ```kotlin
     * CometChatMessageComposer(
     *     user = user,
     *     attachmentOptions = listOf(
     *         CometChatMessageComposerAction(id = "LOCATION", title = "Share Location", icon = R.drawable.ic_location)
     *     ),
     *     onAttachmentOptionClick = { action ->
     *         when (action.id) {
     *             "LOCATION" -> { /* Handle location sharing */ }
     *         }
     *     }
     * )
     * ```
     */
    onAttachmentOptionClick: ((CometChatMessageComposerAction) -> Unit)? = null,
    // Extension option callbacks
    /**
     * Callback invoked when the Poll attachment option is clicked.
     * @return `true` to skip default poll creation behavior (developer handles it),
     *         `false` or `null` to execute default poll creation behavior.
     */
    onPollClick: (() -> Boolean)? = null,
    /**
     * Callback invoked when the Collaborative Document attachment option is clicked.
     * @return `true` to skip default collaborative document creation behavior (developer handles it),
     *         `false` or `null` to execute default collaborative document creation behavior.
     */
    onCollaborativeDocumentClick: (() -> Boolean)? = null,
    /**
     * Callback invoked when the Collaborative Whiteboard attachment option is clicked.
     * @return `true` to skip default collaborative whiteboard creation behavior (developer handles it),
     *         `false` or `null` to execute default collaborative whiteboard creation behavior.
     */
    onCollaborativeWhiteboardClick: (() -> Boolean)? = null,
    // Mentions configuration
    /**
     * Text formatters for mention detection and formatting.
     * If not provided, a default CometChatMentionsFormatter will be created when mentions are enabled.
     */
    textFormatters: List<CometChatTextFormatter>? = null,
    /**
     * Style configuration for the suggestion list.
     */
    suggestionListStyle: CometChatSuggestionListStyle = CometChatSuggestionListStyle.default(),
    /**
     * Callback invoked when a mention is clicked in the suggestion list.
     */
    onMentionClick: ((SuggestionItem) -> Unit)? = null
) {
    val context = LocalContext.current

    // Create default ViewModel if none provided
    val composerViewModel = viewModel ?: viewModel(
        factory = CometChatMessageComposerViewModelFactory()
    )

    // Set user or group on ViewModel
    LaunchedEffect(user?.uid, group?.guid, parentMessageId) {
        user?.let { composerViewModel.setUser(it) }
        group?.let { composerViewModel.setGroup(it) }
        if (parentMessageId > -1) {
            composerViewModel.setParentMessageId(parentMessageId)
        }
    }

    // Sync attachment option visibility with ViewModel
    LaunchedEffect(
        hideCameraOption,
        hideImageOption,
        hideVideoOption,
        hideAudioOption,
        hideFileOption,
        hidePollOption,
        hideCollaborativeDocumentOption,
        hideCollaborativeWhiteboardOption
    ) {
        composerViewModel.setCameraOptionVisibility(!hideCameraOption)
        composerViewModel.setImageOptionVisibility(!hideImageOption)
        composerViewModel.setVideoOptionVisibility(!hideVideoOption)
        composerViewModel.setAudioOptionVisibility(!hideAudioOption)
        composerViewModel.setFileOptionVisibility(!hideFileOption)
        composerViewModel.setPollOptionVisibility(!hidePollOption)
        composerViewModel.setCollaborativeDocumentOptionVisibility(!hideCollaborativeDocumentOption)
        composerViewModel.setCollaborativeWhiteboardOptionVisibility(!hideCollaborativeWhiteboardOption)
    }

    // Sync custom attachment options with ViewModel
    LaunchedEffect(attachmentOptions) {
        composerViewModel.setAttachmentOptions(attachmentOptions)
    }

    // Sync typing event preference with ViewModel and end any active typing
    // session when the composer leaves composition
    LaunchedEffect(disableTypingEvents) {
        composerViewModel.disableTypingEvents = disableTypingEvents
    }
    DisposableEffect(composerViewModel) {
        onDispose { composerViewModel.endTyping() }
    }

    // Collect state from ViewModel
    val editMessage by composerViewModel.editMessage.collectAsStateWithLifecycle()
    val replyMessage by composerViewModel.replyMessage.collectAsStateWithLifecycle()
    val composeText by composerViewModel.composeText.collectAsStateWithLifecycle()
    val isAIGenerating by composerViewModel.isAIGenerating.collectAsStateWithLifecycle()
    val idMap by composerViewModel.idMap.collectAsStateWithLifecycle()
    val currentUser by composerViewModel.user.collectAsStateWithLifecycle()
    val currentGroup by composerViewModel.group.collectAsStateWithLifecycle()
    val composerMode by composerViewModel.composerMode.collectAsStateWithLifecycle()

    // Multi-attachment staging state (only meaningful when enableMultipleAttachments = true)
    val attachmentTiles by composerViewModel.attachmentTiles.collectAsStateWithLifecycle()
    val attachmentsAllUploaded by composerViewModel.attachmentsAllUploaded.collectAsStateWithLifecycle()
    val hasStagedAttachments = enableMultipleAttachments && attachmentTiles.isNotEmpty()

    // Per-message attachment cap: the server's fileCount setting. The remaining slots drive the
    // picker selection limit (Gate A); stageAttachments trims anything that slips past it (Gate B).
    val maxAttachmentCount = composerViewModel.maxAttachmentCount
    val remainingAttachmentSlots =
        (maxAttachmentCount - attachmentTiles.size).coerceAtLeast(0)

    // Detect agentic (AI bot) user — mirrors chatuikit-kotlin behavior
    val isAgentChat = remember(currentUser) {
        currentUser?.let { AgentChatDetector.isAgentChat(it) } ?: false
    }

    // Override visibility flags for agentic users:
    // - Always hide attachment, voice recording, sticker buttons, and rich text toolbar
    val effectiveHideAttachmentButton = if (isAgentChat) true else hideAttachmentButton
    val effectiveHideVoiceRecordingButton = if (isAgentChat) true else hideVoiceRecordingButton
    val effectiveHideStickersButton = if (isAgentChat) true else hideStickersButton
    val effectiveEnableRichTextFormatting = if (isAgentChat) false else enableRichTextFormatting

    // Local state
    var showAttachmentPopup by remember { mutableStateOf(false) }
    // Tracks the last time the attachment popup was dismissed via outside click.
    // Used to prevent the button click (which fires in the same gesture) from
    // immediately re-opening the popup when focusable = false.
    var attachmentPopupDismissTime by remember { mutableStateOf(0L) }
    var showAISheet by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showLinkPopup by remember { mutableStateOf(false) }
    var showStickerKeyboard by remember { mutableStateOf(false) }
    var showCreatePollDialog by remember { mutableStateOf(false) }

    // Link editing state
    var linkEditInitialText by remember { mutableStateOf("") }
    var linkEditInitialUrl by remember { mutableStateOf("") }
    var isLinkEditMode by remember { mutableStateOf(false) }
    var linkEditSpanStart by remember { mutableStateOf(-1) }
    var linkEditSpanEnd by remember { mutableStateOf(-1) }

    // Mention state management
    val mentionInsertionState = rememberMentionInsertionState()
    var mentionDetectionState by remember { mutableStateOf(ComposeMentionState.INACTIVE) }
    var showSuggestionList by remember { mutableStateOf(false) }
    var suggestionItems by remember { mutableStateOf<List<SuggestionItem>>(emptyList()) }
    var isLoadingSuggestions by remember { mutableStateOf(false) }
    var mentionInfoMessage by remember { mutableStateOf("") }
    var showMentionInfo by remember { mutableStateOf(false) }
    // Version counter to trigger recomposition when mentions change
    var mentionVersion by remember { mutableStateOf(0) }
    
    // Create or use provided text formatters for mentions
    val effectiveTextFormatters = remember(textFormatters, disableMentions) {
        if (disableMentions) {
            emptyList()
        } else {
            textFormatters ?: listOf(CometChatMentionsFormatter(context))
        }
    }
    
    // Get the mentions formatter if available
    val mentionsFormatter = remember(effectiveTextFormatters) {
        effectiveTextFormatters.filterIsInstance<CometChatMentionsFormatter>().firstOrNull()
    }
    
    // Default mention style from theme
    val defaultMentionStyle = SpanStyle(
        color = CometChatTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
        background = CometChatTheme.colorScheme.primary.copy(alpha = 0.2f)
    )
    
    // Sync user/group with text formatters for mentions
    LaunchedEffect(user?.uid, group?.guid, effectiveTextFormatters) {
        effectiveTextFormatters.forEach { formatter ->
            formatter.setUser(user)
            formatter.setGroup(group)
        }
    }
    
    // Reactively observe suggestion list changes from the active formatter
    // This uses the exposed Compose State properties for proper recomposition
    val activeFormatter = mentionDetectionState.activeFormatter
    if (activeFormatter != null && mentionDetectionState.isActive) {
        // NOTE: search() is intentionally NOT triggered here. The single debounced
        // LaunchedEffect(mentionDetectionState) below is the only search path.
        // Calling search() here as well caused two overlapping GroupMembersRequest
        // fetches to append to the same list, duplicating the suggestion items.

        // Read the state values directly - this triggers recomposition when they change
        val formatterSuggestions = activeFormatter.suggestionItemListState.value
        val formatterLoading = activeFormatter.showLoadingIndicatorState.value
        val formatterInfoMessage = activeFormatter.tagInfoMessageState.value
        val formatterInfoVisible = activeFormatter.tagInfoVisibleState.value
        
        // Update local state from formatter state
        LaunchedEffect(formatterSuggestions, formatterLoading, formatterInfoMessage, formatterInfoVisible) {
            suggestionItems = formatterSuggestions
            isLoadingSuggestions = formatterLoading
            mentionInfoMessage = formatterInfoMessage
            showMentionInfo = formatterInfoVisible
        }
    }

    // Trigger formatter search when mention detection state changes.
    // Without this, typing @ detects the mention but never calls formatter.search()
    // so the suggestion list stays empty and the suggestion sheet never appears.
    LaunchedEffect(mentionDetectionState) {
        if (mentionDetectionState.isActive && mentionDetectionState.activeFormatter != null) {
            // Debounce search by a short delay to avoid excessive API calls while typing
            delay(300)
            mentionDetectionState.activeFormatter?.search(context, mentionDetectionState.query)
        } else {
            // Clear cached results when mention context is lost so stale results
            // don't appear the next time @ is typed
            mentionDetectionState.activeFormatter?.search(context, null)
        }
    }

    // Keyboard controller for programmatic hide/show
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current

    // Track the last known keyboard height so the sticker panel can match it
    val imeInsets = WindowInsets.ime
    val imeBottomDp = with(density) { imeInsets.getBottom(density).toDp() }
    var lastKeyboardHeight by remember { mutableStateOf(296.dp) }
    // Update last known keyboard height whenever the keyboard is visible
    LaunchedEffect(imeBottomDp) {
        if (imeBottomDp > 0.dp) {
            lastKeyboardHeight = imeBottomDp
        }
    }

    // Segment composer controller — manages normal + code block segments
    val segmentController = remember { SegmentComposerController() }
    // Trigger recomposition when segments change
    var segmentVersion by remember { mutableStateOf(0) }
    // Trigger toolbar recomposition when formats change (toggleFormat doesn't fire onSegmentsChanged)
    var formatVersion by remember { mutableStateOf(0) }
    DisposableEffect(Unit) {
        segmentController.setListener(object : SegmentComposerController.Listener {
            override fun onSegmentsChanged() {
                segmentVersion++
            }
        })
        onDispose { segmentController.setListener(null) }
    }

    // Map of segment ID → FocusRequester, rebuilt when segments change
    val focusRequesters = remember(segmentVersion) {
        segmentController.segments.associate { it.id to FocusRequester() }
    }

    // Sync composeText from ViewModel (set by ComposeMessage UIEvent) to the segment controller.
    // This enables suggested message chips and conversation starters to populate the composer.
    LaunchedEffect(composeText) {
        if (composeText.isNotEmpty()) {
            val firstSegment = segmentController.segments.firstOrNull()
            if (firstSegment is ComposerSegment.Normal) {
                firstSegment.controller.onTextChanged(composeText, composeText.length, composeText.length)
            }
            // Clear the ViewModel state to avoid re-applying on recomposition
            composerViewModel.clearComposeText()
        }
    }

    // Consume pending focus after recomposition
    LaunchedEffect(segmentController.pendingFocusSegmentId, segmentVersion) {
        val pendingId = segmentController.consumePendingFocus()
        if (pendingId != null) {
            focusRequesters[pendingId]?.requestFocus()
        }
    }

    // Samsung keyboard duplication guard.
    // Samsung IME commits composition, sends Enter, then immediately re-sends
    // the committed text as new content on the next line (~7ms later).
    // We track the text at the moment Enter was processed, and if the very next
    // onValueChange appends text after the \n that matches the end of the
    // previous line, we reject it.
    var lastNewlineText by remember { mutableStateOf<String?>(null) }

    // Check if in recording mode
    val isInRecordingMode = composerMode is ComposerMode.Recording
    
    // Rich text toolbar visibility - controlled by effectiveEnableRichTextFormatting.
    // The toolbar sits below the input row in both layout modes and is always visible
    // when formatting is enabled, regardless of text presence.
    val showRichTextToolbar = effectiveEnableRichTextFormatting && enabledFormats.isNotEmpty()

    // Media selection state for handling attachment options
    // Each callback uses a fixed category based on the picker used, NOT the detected content type.
    // This matches Java reference behavior where the action type determines the message type: a
    // photo chosen through the file picker stays a `file` (document tile, file message), while the
    // same photo from the gallery/camera is an `image`.
    // Stages a batch of picked files into the composer tray (multi-attachment flow). Items whose
    // cached file is missing are skipped with an error; the rest start uploading immediately.
    // `category` fixes the tile/send category for every item; null derives it from the MIME type
    // (used by the visual-media picker, which returns a mix of images and videos).
    val stageResultsAs: (List<MediaSelectionResult>, String?) -> Unit = { results, category ->
        val inputs = results.mapNotNull { result ->
            val file = result.file
            if (file == null || !file.exists()) {
                onError?.invoke(CometChatException("FILE_ERROR", "Selected file does not exist"))
                null
            } else {
                val mime = result.mimeType ?: "application/octet-stream"
                StagedAttachmentInput(
                    file = file,
                    name = result.fileName,
                    size = if (result.fileSize > 0) result.fileSize else file.length(),
                    mimeType = mime,
                    category = category ?: defaultAttachmentCategory(mime),
                    source = AttachmentSource.PICKER,
                    localUri = result.uri.toString(),
                    // Duration badge (video/audio) — read once at staging so it can be stored in
                    // the sent message metadata and shown on the receive-side bubble.
                    durationMillis = extractMediaDurationMillis(file.absolutePath, mime)
                )
            }
        }
        if (inputs.isNotEmpty()) composerViewModel.stageAttachments(inputs)
    }

    // Media dragged onto the composer (multi-window, tablets, ChromeOS/DeX) stages into the tray
    // like a picker selection, category from MIME. NOTE: keyboard image-paste needs the
    // TextFieldState-based BasicTextField content receiver — the composer's legacy
    // TextFieldValue fields can't receive it; the Views composer covers that path.
    val currentStageResults by rememberUpdatedState(stageResultsAs)
    val mediaDropTarget = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                return try {
                    val dragEvent = event.toAndroidDragEvent()
                    // Content uris need the platform grant before they can be read.
                    context.findActivity()?.requestDragAndDropPermissions(dragEvent)
                    val clip = dragEvent.clipData ?: return false
                    val uris = (0 until clip.itemCount).mapNotNull { clip.getItemAt(it).uri }
                    if (uris.isEmpty()) return false
                    val results = uris.map { createMediaSelectionResult(context, it, copyToCache = true) }
                    currentStageResults(results, null)
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

    // Paste interception: the legacy TextFieldValue-based BasicTextField can only paste TEXT —
    // an image/file on the clipboard never reaches the field (and compose hides the Paste action
    // entirely for media-only clips, since its clipboard check is text-based). The segment text
    // fields wrap LocalTextToolbar with these two hooks: offer Paste when the clipboard holds
    // media uris, and stage them into the tray like a picker selection instead of pasting.
    val hasClipboardMedia: () -> Boolean = hasMedia@{
        if (!enableMultipleAttachments) return@hasMedia false
        val description = try {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager)
                .primaryClipDescription
        } catch (e: Exception) {
            null
        } ?: return@hasMedia false
        (0 until description.mimeTypeCount).any { i ->
            val mime = description.getMimeType(i)
            mime.startsWith("image/") || mime.startsWith("video/") ||
                mime.startsWith("audio/") || mime.startsWith("application/")
        }
    }
    val stageClipboardMedia: () -> Boolean = stageMedia@{
        if (!enableMultipleAttachments) return@stageMedia false
        try {
            val clip = (context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager)
                .primaryClip ?: return@stageMedia false
            val uris = (0 until clip.itemCount).mapNotNull { clip.getItemAt(it).uri }
            if (uris.isEmpty()) return@stageMedia false
            val results = uris.map { createMediaSelectionResult(context, it, copyToCache = true) }
            stageResultsAs(results, null)
            true
        } catch (e: Exception) {
            Log.e("CometChatMessageComposer", "Failed to stage clipboard media: ${e.message}")
            false
        }
    }

    val mediaSelectionState = rememberMediaSelectionState(
        // Gate A: the OS visual-media picker only offers the remaining tray slots.
        maxSelection = remainingAttachmentSlots,
        // Single-select callbacks fire only on the legacy path (enableMultipleAttachments = false),
        // where each pick is sent immediately with a fixed message type.
        onImageSelected = { result ->
            handleMediaSelectionWithType(result, "image", composerViewModel, onError)
        },
        onVideoSelected = { result ->
            handleMediaSelectionWithType(result, "video", composerViewModel, onError)
        },
        onAudioSelected = { result ->
            handleMediaSelectionWithType(result, "audio", composerViewModel, onError)
        },
        onFileSelected = { result ->
            handleMediaSelectionWithType(result, "file", composerViewModel, onError)
        },
        // Camera/video capture are always single-shot; they stage in the multi-attachment flow and
        // send immediately on the legacy path.
        onCameraCapture = { result ->
            if (enableMultipleAttachments) stageResultsAs(listOf(result), CometChatConstants.MESSAGE_TYPE_IMAGE)
            else handleMediaSelectionWithType(result, "image", composerViewModel, onError)
        },
        onVideoCapture = { result ->
            if (enableMultipleAttachments) stageResultsAs(listOf(result), CometChatConstants.MESSAGE_TYPE_VIDEO)
            else handleMediaSelectionWithType(result, "video", composerViewModel, onError)
        },
        // Multi-select callbacks fire only on the multi-attachment path and always stage. The
        // picker fixes the category: file-picker picks stay `file` and audio-picker picks stay
        // `audio` regardless of MIME; the visual-media picker derives image/video per item.
        onImagesSelected = { stageResultsAs(it, null) },
        onVideosSelected = { stageResultsAs(it, null) },
        onAudiosSelected = { stageResultsAs(it, CometChatConstants.MESSAGE_TYPE_AUDIO) },
        onFilesSelected = { stageResultsAs(it, CometChatConstants.MESSAGE_TYPE_FILE) },
        onError = { exception ->
            onError?.invoke(CometChatException("MEDIA_SELECTION_ERROR", exception.message ?: "Media selection failed"))
        }
    )

    // The documents / audio picker UI can't be capped like the photo picker, so the selection
    // limit is surfaced as the picker opens instead: a toast with the remaining slot count renders
    // on top of the opening picker. Shown only when part of the cap is already used.
    val toastRemainingSlotsHint: () -> Unit = {
        if (enableMultipleAttachments &&
            remainingAttachmentSlots in 1 until maxAttachmentCount
        ) {
            Toast.makeText(
                context,
                context.getString(R.string.cometchat_attachment_remaining_slots, remainingAttachmentSlots),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Gate A (multi-attachment): refuse to open a picker when the tray is already at the cap.
    // Anything that still slips past the picker limit is trimmed by stageAttachments (Gate B).
    val launchIfSlotsRemain: (() -> Unit) -> Unit = { launch ->
        if (enableMultipleAttachments && remainingAttachmentSlots <= 0) {
            Toast.makeText(
                context,
                context.getString(R.string.cometchat_attachment_count_exceeded, maxAttachmentCount),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            launch()
        }
    }

    // Rich text formatter manager (kept for backward compat — bubble display side)
    val formatterManager = remember(enabledFormats) {
        val config = RichTextConfiguration(
            enableBold = RichTextFormat.BOLD in enabledFormats,
            enableItalic = RichTextFormat.ITALIC in enabledFormats,
            enableUnderline = RichTextFormat.UNDERLINE in enabledFormats,
            enableStrikethrough = RichTextFormat.STRIKETHROUGH in enabledFormats,
            enableInlineCode = RichTextFormat.INLINE_CODE in enabledFormats,
            enableCodeBlock = RichTextFormat.CODE_BLOCK in enabledFormats,
            enableLink = RichTextFormat.LINK in enabledFormats,
            enableBulletList = RichTextFormat.BULLET_LIST in enabledFormats,
            enableOrderedList = RichTextFormat.ORDERED_LIST in enabledFormats,
            enableBlockquote = RichTextFormat.BLOCKQUOTE in enabledFormats
        )
        RichTextFormatterManager(config)
    }

    // Sync compose text from ViewModel (for edit mode)
    // Parses the edit message markdown into segments: fenced code blocks become Code segments,
    // everything else becomes Normal segments.
    LaunchedEffect(editMessage, effectiveTextFormatters) {
        editMessage?.let { msg ->
            // Text messages edit their text; media messages edit their caption.
            val rawText = when (msg) {
                is TextMessage -> msg.text ?: ""
                is MediaMessage -> msg.caption ?: ""
                else -> null
            }
            if (rawText != null) {
                // Run formatter pipeline to resolve mention tokens (e.g., <@uid:userId> -> @userName)
                var formattedText: AnnotatedString = AnnotatedString(rawText)
                for (formatter in effectiveTextFormatters) {
                    formattedText = formatter.prepareMessageString(
                        context,
                        msg,
                        formattedText,
                        UIKitConstants.MessageBubbleAlignment.RIGHT,
                        UIKitConstants.FormattingType.MESSAGE_COMPOSER
                    )
                }
                val text = formattedText.text

                // Register resolved mentions with mentionInsertionState so the
                // ComposerMentionVisualTransformation can style them in the composer.
                if (!disableMentions) {
                    mentionInsertionState.clear()
                    val mentionedUsers = msg.mentionedUsers
                    val mentionsFormatter = effectiveTextFormatters
                        .filterIsInstance<CometChatMentionsFormatter>()
                        .firstOrNull()
                    if (mentionedUsers != null && mentionsFormatter != null) {
                        val trackChar = mentionsFormatter.getTrackingCharacter()
                        for (user in mentionedUsers) {
                            val promptText = "$trackChar${user.name}"
                            val idx = text.indexOf(promptText)
                            if (idx >= 0) {
                                mentionInsertionState.getMentionsManager().addMention(
                                    SelectedMention(
                                        id = user.uid,
                                        name = user.name,
                                        promptText = promptText,
                                        underlyingText = "<${trackChar}uid:${user.uid}>",
                                        spanStart = idx,
                                        spanEnd = idx + promptText.length
                                    )
                                )
                            }
                        }
                    }
                    mentionVersion++
                }

                // Parse markdown into alternating Normal / Code segments.
                // Fenced code blocks are delimited by lines starting with ``` (optionally followed by a language hint).
                segmentController.clear()

                // Regex to match fenced code blocks: ```<optional language>\n<content>\n```
                val codeBlockRegex = Regex("```(\\w*)\\n([\\s\\S]*?)\\n```")
                val matches = codeBlockRegex.findAll(text).toList()

                if (matches.isEmpty()) {
                    // No code blocks — load all text into the first (already-existing) Normal segment
                    val firstSegment = segmentController.segments.firstOrNull()
                    if (firstSegment is ComposerSegment.Normal && text.isNotEmpty()) {
                        firstSegment.controller.onTextChanged(text, text.length, text.length)
                    }
                } else {
                    // Build segments from the parsed markdown
                    var cursor = 0
                    var isFirstNormal = true
                    for (match in matches) {
                        // Text before this code block → Normal segment
                        val beforeText = text.substring(cursor, match.range.first).trim()
                        if (isFirstNormal) {
                            // Reuse the existing first Normal segment from clear()
                            val firstSeg = segmentController.segments.firstOrNull()
                            if (firstSeg is ComposerSegment.Normal && beforeText.isNotEmpty()) {
                                firstSeg.controller.onTextChanged(beforeText, beforeText.length, beforeText.length)
                            }
                            // Focus the first normal, then toggle to insert a code block
                            segmentController.setFocusedSegment(firstSeg!!.id)
                            isFirstNormal = false
                        } else if (beforeText.isNotEmpty()) {
                            // There should be a Normal segment after the last code block;
                            // find the last Normal segment and set its text
                            val lastNormal = segmentController.segments.lastOrNull { it is ComposerSegment.Normal } as? ComposerSegment.Normal
                            if (lastNormal != null) {
                                lastNormal.controller.onTextChanged(beforeText, beforeText.length, beforeText.length)
                            }
                        }

                        // Insert a code block via toggleCodeBlock (focuses the last Normal, then toggles)
                        val lastNormalForToggle = segmentController.segments.lastOrNull { it is ComposerSegment.Normal }
                        if (lastNormalForToggle != null) {
                            segmentController.setFocusedSegment(lastNormalForToggle.id)
                        }
                        segmentController.toggleCodeBlock()

                        // Set the code segment's text and language
                        val codeSegment = segmentController.segments.lastOrNull { it is ComposerSegment.Code } as? ComposerSegment.Code
                        if (codeSegment != null) {
                            val language = match.groupValues[1]
                            val codeContent = match.groupValues[2]
                            codeSegment.text = codeContent
                            codeSegment.language = language
                        }

                        cursor = match.range.last + 1
                    }

                    // Text after the last code block → set on the trailing Normal segment
                    if (cursor < text.length) {
                        val trailingText = text.substring(cursor).trim()
                        if (trailingText.isNotEmpty()) {
                            val lastNormal = segmentController.segments.lastOrNull { it is ComposerSegment.Normal } as? ComposerSegment.Normal
                            if (lastNormal != null) {
                                lastNormal.controller.onTextChanged(trailingText, trailingText.length, trailingText.length)
                            }
                        }
                    }

                    // Focus the first segment
                    val firstSeg = segmentController.segments.firstOrNull()
                    if (firstSeg != null) {
                        segmentController.focusSegment(firstSeg.id)
                    }
                }
            }
        }
    }

    // Handle error events
    LaunchedEffect(Unit) {
        composerViewModel.errorEvent.collect { error ->
            // Gate B backstop: picked files beyond the attachment cap were dropped — tell the user.
            if (error.code == CometChatMessageComposerViewModel.ERROR_MAX_ATTACHMENTS_EXCEEDED) {
                Toast.makeText(
                    context,
                    context.getString(
                        R.string.cometchat_attachment_count_exceeded,
                        composerViewModel.maxAttachmentCount
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }
            onError?.invoke(error)
        }
    }

    // Determine send button state — segment controller tracks all content across segments.
    // With staged attachments, the send button follows the all-or-nothing rule: it stays disabled
    // until every staged attachment has finished uploading (text becomes an optional caption).
    val canSendStagedAttachments = hasStagedAttachments && attachmentsAllUploaded
    val isSendButtonActive = if (hasStagedAttachments) {
        canSendStagedAttachments
    } else {
        segmentController.hasContent
    }

    // Placeholder text
    val placeholder = placeholderText ?: context.getString(R.string.cometchat_composer_place_holder_text)
    
    // Effective attachment popup style with 12dp corner radius (matching Kotlin cometchat_corner_radius_3)
    // Use iconTintHighlight (primary color) for attachment icons to match Kotlin implementation
    val effectiveAttachmentPopupStyle = attachmentPopupStyle.copy(
        cornerRadius = 12.dp, 
        itemPaddingVertical = 8.dp,
        startIconTint = CometChatTheme.colorScheme.iconTintHighlight
    )

    // Transient messages (e.g. why a staged attachment was rejected) surface through a themed
    // CometChatErrorAlert pinned just ABOVE the composer. Setting the text shows it; it clears itself
    // on timeout or ✕. The composer width is tracked so the floating bar can match it.
    var attachmentErrorMessage by remember { mutableStateOf<String?>(null) }
    var composerWidthPx by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { composerWidthPx = it.size.width }
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = style.backgroundColor,
                shape = RoundedCornerShape(style.cornerRadius)
            )
            .then(
                if (style.strokeWidth > 0.dp) {
                    Modifier.border(
                        width = style.strokeWidth,
                        color = style.strokeColor,
                        shape = RoundedCornerShape(style.cornerRadius)
                    )
                } else Modifier
            )
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    enableMultipleAttachments && event.mimeTypes().any { mime ->
                        mime.startsWith("image/") || mime.startsWith("video/") ||
                            mime.startsWith("audio/") || mime.startsWith("application/")
                    }
                },
                target = mediaDropTarget
            )
            .semantics { contentDescription = "Message Composer" }
    ) {
        // Header view slot
        headerView?.invoke()

        // Suggestion list for mentions (shown above compose box when mention is detected)
        AnimatedVisibility(
            visible = showSuggestionList && !disableMentions && suggestionItems.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                CometChatSuggestionList(
                    modifier = Modifier.fillMaxWidth(),
                    suggestions = suggestionItems,
                    isLoading = isLoadingSuggestions,
                    style = suggestionListStyle,
                    showAvatar = true,
                    onItemClick = { suggestionItem ->
                        // Insert the mention into the focused Normal segment
                        val formatter = mentionDetectionState.activeFormatter
                        val focusedNormal = segmentController.focusedSegment as? ComposerSegment.Normal
                        if (formatter != null && mentionDetectionState.isActive && focusedNormal != null) {
                            // Get the appropriate style for this suggestion
                            val mentionStyle = if (formatter is CometChatMentionsFormatter) {
                                formatter.getSpanStyleForSuggestionItem(suggestionItem)
                            } else {
                                defaultMentionStyle
                            }

                            // Build a TextFieldValue from the focused segment's controller
                            val ctrl = focusedNormal.controller
                            val currentTfv = TextFieldValue(
                                text = ctrl.state.text,
                                selection = TextRange(ctrl.state.selectionStart, ctrl.state.selectionEnd)
                            )

                            // Insert the mention using the insertion state
                            val updatedTfv = mentionInsertionState.insertMention(
                                currentValue = currentTfv,
                                mentionState = mentionDetectionState,
                                suggestionItem = suggestionItem,
                                formatter = formatter,
                                mentionStyle = mentionStyle
                            )

                            // Increment mention version to trigger visual transformation update
                            mentionVersion++
                            // Trigger NormalSegmentTextField recomposition to sync tfv from controller
                            formatVersion++

                            // Sync the focused segment's controller with the new text.
                            // Always update the controller so the segment text is current,
                            // regardless of whether rich text formatting is enabled.
                            ctrl.onTextChanged(
                                updatedTfv.text,
                                updatedTfv.selection.min,
                                updatedTfv.selection.max
                            )

                            // Bump segmentVersion to force NormalSegmentTextField to
                            // recompose and pick up the updated controller text.
                            segmentVersion++

                            // Update the formatter's selected list
                            formatter.setSelectedList(context, mentionInsertionState.getSelectedSuggestionItems())

                            // Invoke callback
                            onMentionClick?.invoke(suggestionItem)

                            // Notify text change
                            onTextChanged?.invoke(updatedTfv.text)
                        }

                        // Hide suggestion list
                        showSuggestionList = false
                        suggestionItems = emptyList()
                        mentionDetectionState = ComposeMentionState.INACTIVE
                    },
                    onScrollToBottom = {
                        // Trigger pagination
                        mentionDetectionState.activeFormatter?.onScrollToBottom()
                    }
                )
            }
        }
        
        // Mention limit info message
        AnimatedVisibility(
            visible = showMentionInfo && mentionInfoMessage.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(
                        color = style.infoBackgroundColor,
                        shape = RoundedCornerShape(style.infoCornerRadius)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mentionInfoMessage,
                    color = style.infoTextColor,
                    style = style.infoTextStyle
                )
            }
        }

        // Main compose box - Column containing edit/reply preview, input row and toolbar
        // Edit and reply previews are INSIDE the compose box for integrated appearance
        // Figma: 8dp horizontal margin, 0dp top padding, 8dp bottom padding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(style.composeBoxCornerRadius))
                .background(
                    color = style.composeBoxBackgroundColor,
                    shape = RoundedCornerShape(style.composeBoxCornerRadius)
                )
                .then(
                    if (style.composeBoxStrokeWidth > 0.dp) {
                        Modifier.border(
                            width = style.composeBoxStrokeWidth,
                            color = style.composeBoxStrokeColor,
                            shape = RoundedCornerShape(style.composeBoxCornerRadius)
                        )
                    } else Modifier
                )
        ) {
            // Edit preview panel (inside compose box for integrated appearance)
            editMessage?.let { message ->
                if (editPreviewView != null) {
                    editPreviewView(message) {
                        composerViewModel.clearEditMessage()
                        segmentController.clear()
                    }
                } else {
                    DefaultEditPreview(
                        message = message,
                        textFormatters = effectiveTextFormatters,
                        style = style,
                        onClose = {
                            composerViewModel.clearEditMessage()
                            segmentController.clear()
                        }
                    )
                }
            }

            // Reply preview panel (inside compose box for integrated appearance)
            replyMessage?.let { message ->
                if (replyPreviewView != null) {
                    replyPreviewView(message) {
                        composerViewModel.clearReplyMessage()
                    }
                } else {
                    DefaultReplyPreview(
                        message = message,
                        textFormatters = effectiveTextFormatters,
                        style = style,
                        onClose = {
                            composerViewModel.clearReplyMessage()
                        }
                    )
                }
            }

            // Multi-attachment staging tray (renders only while ≥1 attachment is staged).
            // Rendered BELOW the text input, per the design: text on top, tray under it, action
            // buttons at the bottom.
            val attachmentTrayContent: @Composable () -> Unit = {
                if (enableMultipleAttachments) {
                    CometChatAttachmentTray(
                        tiles = attachmentTiles,
                        style = attachmentTrayStyle,
                        tileStyle = attachmentTileStyle,
                        onCancelTile = { composerViewModel.removeAttachment(it) },
                        onRemoveTile = { composerViewModel.removeAttachment(it) },
                        onRetryTile = { composerViewModel.retryAttachment(it) },
                        onTileClick = { openStagedAttachmentPreview(context, it) },
                        onRejectedTile = { tile ->
                            // Surface the SDK-provided rejection reason verbatim (e.g. the size-limit
                            // message already carries the actual per-file limit) — the UIKit never
                            // recomputes or hardcodes the limit.
                            attachmentErrorMessage = tile.error?.message?.takeIf { it.isNotBlank() }
                                ?: context.getString(R.string.cometchat_attachment_upload_failed)
                        }
                    )
                }
            }

            // Input row with buttons - or inline recorder when in recording mode
            if (isInRecordingMode) {
                // Show new CometChatInlineAudioRecorder component
                // Create a dedicated ViewModel for the inline audio recorder
                val inlineAudioRecorderViewModel: CometChatInlineAudioRecorderViewModel = viewModel()
                
                CometChatInlineAudioRecorder(
                    modifier = Modifier.fillMaxWidth(),
                    viewModel = inlineAudioRecorderViewModel,
                    style = CometChatInlineAudioRecorderStyle.default(),
                    onSubmit = { file ->
                        // Recorded voice note → mark it so the receive side uses VoiceNoteBubble.
                        composerViewModel.sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_AUDIO, isVoiceNote = true)
                        composerViewModel.exitRecordingMode()
                    },
                    onCancel = {
                        composerViewModel.exitRecordingMode()
                    },
                    onError = { errorMessage ->
                        onError?.invoke(CometChatException("RECORDING_ERROR", errorMessage))
                        composerViewModel.exitRecordingMode()
                    }
                )
                attachmentTrayContent()
            } else {
            // ===== INPUT ROW =====
            // Both layout modes keep the action buttons inline with the text input:
            // attachment on the left, then sticker, voice recording and send on the right.
            // MULTI_LINE only differs by pinning the rich text toolbar below this row.
            var textLayoutLineCount by remember { mutableStateOf(1) }
            val buttonAlignment = if (textLayoutLineCount > 1) Alignment.Bottom else Alignment.CenterVertically
            
            // Figma: 12dp padding inside compose box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(12.dp),
                verticalAlignment = buttonAlignment
            ) {
            // Secondary button (attachment + microphone) with separator
            if (secondaryButtonView != null) {
                secondaryButtonView(
                    { showAttachmentPopup = !showAttachmentPopup },
                    { composerViewModel.startRecordingMode() }
                )
            } else {
                // Row to center-align attachment button with separator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                // Get attachment options from ViewModel (filtered by visibility flags)
                val attachmentOptions = remember(
                    composerViewModel.showCameraOption.collectAsStateWithLifecycle().value,
                    composerViewModel.showImageOption.collectAsStateWithLifecycle().value,
                    composerViewModel.showVideoOption.collectAsStateWithLifecycle().value,
                    composerViewModel.showAudioOption.collectAsStateWithLifecycle().value,
                    composerViewModel.showFileOption.collectAsStateWithLifecycle().value,
                    composerViewModel.showPollOption.collectAsStateWithLifecycle().value,
                    composerViewModel.showCollaborativeDocumentOption.collectAsStateWithLifecycle().value,
                    composerViewModel.showCollaborativeWhiteboardOption.collectAsStateWithLifecycle().value
                ) {
                    composerViewModel.getDefaultAttachmentOptions(
                        cameraTitle = context.getString(R.string.cometchat_camera),
                        cameraIcon = R.drawable.cometchat_ic_camera,
                        imageTitle = context.getString(R.string.cometchat_attach_image),
                        imageIcon = R.drawable.cometchat_ic_image_library,
                        videoTitle = context.getString(R.string.cometchat_attach_video),
                        videoIcon = R.drawable.cometchat_ic_video_library,
                        audioTitle = context.getString(R.string.cometchat_attach_audio),
                        audioIcon = R.drawable.cometchat_ic_audio,
                        fileTitle = context.getString(R.string.cometchat_attach_document),
                        fileIcon = R.drawable.cometchat_ic_file_upload,
                        pollTitle = context.getString(R.string.cometchat_poll),
                        pollIcon = R.drawable.cometchat_ic_polls,
                        collaborativeDocumentTitle = context.getString(R.string.cometchat_collaborative_doc),
                        collaborativeDocumentIcon = R.drawable.cometchat_ic_collaborative_document,
                        collaborativeWhiteboardTitle = context.getString(R.string.cometchat_collaborative_whiteboard),
                        collaborativeWhiteboardIcon = R.drawable.cometchat_ic_conversations_collaborative_whiteboard
                    )
                }
                
                // Convert CometChatMessageComposerAction to MenuItem
                val menuItems = attachmentOptions.map { action ->
                    MenuItem.withIcons(
                        id = action.id,
                        name = action.title,
                        startIcon = androidx.compose.ui.res.painterResource(action.icon)
                    )
                }
                
                // Attachment popup using CometChatPopupMenu - button is inside content for proper anchor height
                CometChatPopupMenu(
                    expanded = showAttachmentPopup,
                    onDismissRequest = { showAttachmentPopup = false },
                    menuItems = menuItems,
                    style = effectiveAttachmentPopupStyle,
                    position = PopupPosition.ABOVE,
                    onMenuItemClick = { id, _ ->
                        showAttachmentPopup = false
                        // Handle attachment option clicks using media selection utilities
                        // Check callback return value: true = skip default, false/null = execute default
                        when (id) {
                            CometChatMessageComposerAction.ID_CAMERA -> {
                                val handled = onCameraClick?.invoke() ?: false
                                if (!handled) launchIfSlotsRemain(mediaSelectionState.launchCamera)
                            }
                            CometChatMessageComposerAction.ID_IMAGE -> {
                                val handled = onImageClick?.invoke() ?: false
                                if (!handled) { if (enableMultipleAttachments) launchIfSlotsRemain(mediaSelectionState.launchImagePickerMultiple) else mediaSelectionState.launchImagePicker() }
                            }
                            CometChatMessageComposerAction.ID_VIDEO -> {
                                val handled = onVideoClick?.invoke() ?: false
                                if (!handled) { if (enableMultipleAttachments) launchIfSlotsRemain(mediaSelectionState.launchVideoPickerMultiple) else mediaSelectionState.launchVideoPicker() }
                            }
                            CometChatMessageComposerAction.ID_AUDIO -> {
                                val handled = onAudioClick?.invoke() ?: false
                                if (!handled) { if (enableMultipleAttachments) run { toastRemainingSlotsHint(); launchIfSlotsRemain(mediaSelectionState.launchAudioPickerMultiple) } else mediaSelectionState.launchAudioPicker() }
                            }
                            CometChatMessageComposerAction.ID_DOCUMENT -> {
                                val handled = onDocumentClick?.invoke() ?: false
                                if (!handled) { if (enableMultipleAttachments) run { toastRemainingSlotsHint(); launchIfSlotsRemain(mediaSelectionState.launchFilePickerMultiple) } else mediaSelectionState.launchFilePicker() }
                            }
                            CometChatMessageComposerAction.ID_POLL -> {
                                val handled = onPollClick?.invoke() ?: false
                                if (!handled) showCreatePollDialog = true
                            }
                            CometChatMessageComposerAction.ID_COLLABORATIVE_DOCUMENT -> {
                                val handled = onCollaborativeDocumentClick?.invoke() ?: false
                                if (!handled) {
                                    composerViewModel.createCollaborativeDocument(
                                        onSuccess = {
                                            // Document created successfully - no UI feedback needed
                                        },
                                        onError = { exception ->
                                            Toast.makeText(
                                                context,
                                                exception.message ?: context.getString(R.string.cometchat_something_went_wrong),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onError?.invoke(exception)
                                        }
                                    )
                                }
                            }
                            CometChatMessageComposerAction.ID_COLLABORATIVE_WHITEBOARD -> {
                                val handled = onCollaborativeWhiteboardClick?.invoke() ?: false
                                if (!handled) {
                                    composerViewModel.createCollaborativeWhiteboard(
                                        onSuccess = {
                                            // Whiteboard created successfully - no UI feedback needed
                                        },
                                        onError = { exception ->
                                            Toast.makeText(
                                                context,
                                                exception.message ?: context.getString(R.string.cometchat_something_went_wrong),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onError?.invoke(exception)
                                        }
                                    )
                                }
                            }
                            else -> {
                                // Handle custom attachment options
                                val customAction = attachmentOptions.find { it.id == id }
                                customAction?.let { action ->
                                    onAttachmentOptionClick?.invoke(action)
                                }
                            }
                        }
                    }
                ) {
                    DefaultSecondaryButton(
                        hideAttachmentButton = effectiveHideAttachmentButton,
                        hideVoiceRecordingButton = effectiveHideVoiceRecordingButton,
                        isAttachmentPopupExpanded = showAttachmentPopup,
                        style = style,
                        onAttachmentClick = { showAttachmentPopup = !showAttachmentPopup },
                        onVoiceRecordClick = { composerViewModel.startRecordingMode() }
                    )
                }
                } // End of inner Row for center alignment
            }

            // Text input area — always shows normal editor, code block appears below when active
            // Read segmentVersion to trigger recomposition when segments change
            @Suppress("UNUSED_VARIABLE")
            val currentSegmentVersion = segmentVersion

            // Segment Column — renders each segment as its own text field
            val inputScrollState = rememberScrollState()
            val coroutineScope = rememberCoroutineScope()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(inputScrollState)
                    .padding(horizontal = 4.dp)
            ) {
                val segments = segmentController.segments
                segments.forEachIndexed { index, segment ->
                    // Add spacing between Normal and Code segments when the Normal has text
                    if (index > 0) {
                        val prev = segments[index - 1]
                        val needsSpacing = (prev is ComposerSegment.Normal && segment is ComposerSegment.Code && prev.controller.state.text.isNotEmpty()) ||
                            (prev is ComposerSegment.Code && segment is ComposerSegment.Normal && segment.controller.state.text.isNotEmpty())
                        if (needsSpacing) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                    when (segment) {
                        is ComposerSegment.Normal -> {
                            NormalSegmentTextField(
                                segment = segment,
                                segmentController = segmentController,
                                focusRequester = focusRequesters[segment.id]!!,
                                style = style,
                                enabledFormats = enabledFormats,
                                placeholder = placeholder,
                                showPlaceholder = index == 0 && !segmentController.hasContent,
                                onTextChanged = { text -> onTextChanged?.invoke(text) },
                                onFocused = {
                                    // Close sticker keyboard if open
                                    if (showStickerKeyboard) showStickerKeyboard = false
                                    // Trigger toolbar recomposition to reflect this segment's formats
                                    formatVersion++
                                },
                                mentionInsertionState = mentionInsertionState,
                                mentionDetectionState = mentionDetectionState,
                                onMentionDetected = { state -> mentionDetectionState = state },
                                showSuggestionList = showSuggestionList,
                                onShowSuggestionList = { show -> showSuggestionList = show },
                                effectiveTextFormatters = effectiveTextFormatters,
                                disableMentions = disableMentions,
                                composerViewModel = composerViewModel,
                                formatVersion = formatVersion,
                                onLinkTapped = { linkText, linkUrl, spanStart, spanEnd ->
                                    linkEditInitialText = linkText
                                    linkEditInitialUrl = linkUrl
                                    linkEditSpanStart = spanStart
                                    linkEditSpanEnd = spanEnd
                                    showLinkPopup = true
                                },
                                onSelectionChanged = {
                                    // Trigger toolbar recomposition so activeFormats
                                    // reflects the formats at the new cursor position
                                    formatVersion++
                                },
                                onCodeBlockInserted = {
                                    segmentVersion++
                                    formatVersion++
                                },
                                onMediaPaste = stageClipboardMedia,
                                hasClipboardMedia = hasClipboardMedia
                            )
                        }
                        is ComposerSegment.Code -> {
                            CodeSegmentTextField(
                                segment = segment,
                                segmentController = segmentController,
                                focusRequester = focusRequesters[segment.id]!!,
                                style = style,
                                onFocused = {
                                    segmentController.setFocusedSegment(segment.id)
                                    if (showStickerKeyboard) showStickerKeyboard = false
                                    // Trigger toolbar recomposition to reflect code segment's formats
                                    formatVersion++
                                },
                                onTextChanged = { text ->
                                    onTextChanged?.invoke(text)
                                    // Trigger auto-scroll to keep cursor visible while typing in code block
                                    formatVersion++
                                    if (text.isNotEmpty()) {
                                        composerViewModel.startTyping()
                                    } else if (!segmentController.hasContent) {
                                        composerViewModel.endTyping()
                                    }
                                }
                            )
                        }
                    }
                }
            } // End of input Column

            // Auto-scroll to keep cursor visible when text changes, segments change,
            // or pending focus is consumed
            LaunchedEffect(segmentVersion, segmentController.pendingFocusSegmentId) {
                // Small delay to let recomposition settle before scrolling
                delay(50)
                coroutineScope.launch {
                    inputScrollState.animateScrollTo(inputScrollState.maxValue)
                }
            }

            // Auxiliary buttons — sticker and AI remain visible; only voice recording hides when typing
            val hasText = segmentController.hasContent

            if (!hideAuxiliaryButton) {
                if (auxiliaryButtonView != null) {
                    auxiliaryButtonView(currentUser, currentGroup, idMap)
                } else {
                    // Sticker and AI buttons — always visible regardless of text
                    DefaultAuxiliaryButton(
                        hideRichTextToggle = true, // Always hide - toolbar visibility is automatic
                        hideStickersButton = effectiveHideStickersButton,
                        hideAIButton = true, // AI button hidden by default
                        hideVoiceRecordingButton = true, // Voice recording handled separately below
                        isStickerKeyboardOpen = showStickerKeyboard,
                        style = style,
                        onStickerClick = {
                            if (!showStickerKeyboard) {
                                // Opening sticker panel — hide the software keyboard first
                                keyboardController?.hide()
                            }
                            showStickerKeyboard = !showStickerKeyboard
                        },
                        onAIClick = { showAISheet = true },
                        onVoiceRecordClick = { /* handled below */ }
                    )

                    // Voice recording button — slides out when typing or when attachments are staged
                    AnimatedVisibility(
                        visible = !effectiveHideVoiceRecordingButton && !hasText && !hasStagedAttachments,
                        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    ) {
                        DefaultAuxiliaryButton(
                            hideRichTextToggle = true,
                            hideStickersButton = true,
                            hideAIButton = true,
                            hideVoiceRecordingButton = false,
                            style = style,
                            onVoiceRecordClick = { composerViewModel.startRecordingMode() }
                        )
                    }
                }
            }

            // Send button (12dp left spacing - Figma: padding_3)
            if (!hideSendButton) {
                Spacer(modifier = Modifier.width(12.dp))
                if (sendButtonView != null) {
                    sendButtonView(
                        {
                            handleSend(
                                context = context,
                                segmentController = segmentController,
                                editMessage = editMessage,
                                viewModel = composerViewModel,
                                onSendButtonClick = onSendButtonClick,
                                onClear = {
                                    segmentController.clear()
                                    // Clear mention state
                                    mentionInsertionState.clear()
                                    mentionVersion = 0
                                    showSuggestionList = false
                                    suggestionItems = emptyList()
                                    mentionDetectionState = ComposeMentionState.INACTIVE
                                    // Clear formatter selected lists
                                    effectiveTextFormatters.forEach { it.setSelectedList(context, emptyList()) }
                                },
                                mentionInsertionState = if (!disableMentions) mentionInsertionState else null,
                                textFormatters = effectiveTextFormatters,
                                sendStagedAttachments = canSendStagedAttachments
                            )
                        },
                        isSendButtonActive,
                        isAIGenerating
                    )
                } else {
                    DefaultSendButton(
                        modifier = Modifier,
                        isActive = isSendButtonActive,
                        isAIGenerating = isAIGenerating,
                        isAgentChat = isAgentChat,
                        style = style,
                        onClick = {
                            handleSend(
                                context = context,
                                segmentController = segmentController,
                                editMessage = editMessage,
                                viewModel = composerViewModel,
                                onSendButtonClick = onSendButtonClick,
                                onClear = {
                                    segmentController.clear()
                                    // Clear mention state
                                    mentionInsertionState.clear()
                                    mentionVersion = 0
                                    showSuggestionList = false
                                    suggestionItems = emptyList()
                                    mentionDetectionState = ComposeMentionState.INACTIVE
                                    // Clear formatter selected lists
                                    effectiveTextFormatters.forEach { it.setSelectedList(context, emptyList()) }
                                },
                                mentionInsertionState = if (!disableMentions) mentionInsertionState else null,
                                textFormatters = effectiveTextFormatters,
                                sendStagedAttachments = canSendStagedAttachments
                            )
                        }
                    )
                }
            }
            } // End of input Row

            attachmentTrayContent()
            } // End of recording mode else block
            
            // Rich text toolbar (inside compose box, below the input row, when enableRichTextFormatting=true)
            AnimatedVisibility(
                visible = showRichTextToolbar,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    // Read formatVersion to trigger recomposition when formats change
                    @Suppress("UNUSED_VARIABLE")
                    val currentFormatVersion = formatVersion

                    // Compute effective active formats including line format detection.
                    // RichTextEditorState.activeFormats only tracks span-based formats + pending.
                    // Line formats (bullet list, ordered list, blockquote) are stored as text
                    // prefixes ("- ", "1. ", "> ") and must be detected from the text.
                    val effectiveActiveFormats = run {
                        val base = segmentController.activeFormats
                        val focused = segmentController.focusedSegment as? ComposerSegment.Normal
                        if (focused != null) {
                            val text = focused.controller.state.text
                            val cursorPos = focused.controller.state.selectionStart
                            if (text.isNotEmpty() && cursorPos <= text.length) {
                                val lineStart = text.lastIndexOf('\n', (cursorPos - 1).coerceAtLeast(0)) + 1
                                val lineEnd = text.indexOf('\n', cursorPos).let { if (it == -1) text.length else it }
                                if (lineStart <= lineEnd && lineEnd <= text.length) {
                                    val currentLine = text.substring(lineStart, lineEnd)
                                    val lineFormats = mutableSetOf<RichTextFormat>()
                                    if (currentLine.startsWith("- ") || currentLine.startsWith("• ")) {
                                        lineFormats.add(RichTextFormat.BULLET_LIST)
                                    } else if (currentLine.matches(Regex("^\\d+\\. .*"))) {
                                        lineFormats.add(RichTextFormat.ORDERED_LIST)
                                    } else if (currentLine.startsWith("> ")) {
                                        lineFormats.add(RichTextFormat.BLOCKQUOTE)
                                    }
                                    base + lineFormats
                                } else base
                            } else base
                        } else {
                            base
                        }
                    }

                    // Compute effective disabled formats — also disable inline formats
                    // when cursor is inside a LINK span (links are immune to formatting)
                    val effectiveDisabledFormats = run {
                        val base = segmentController.toolbarDisabledFormats
                        val focused = segmentController.focusedSegment as? ComposerSegment.Normal
                        if (focused != null) {
                            val cursorPos = focused.controller.state.selectionStart
                            val spanManager = focused.controller.state.spanManager
                            val linkSpan = spanManager.findLinkSpanAt(cursorPos)
                                ?: if (cursorPos > 0) spanManager.findLinkSpanAt(cursorPos - 1) else null
                            if (linkSpan != null) {
                                base + setOf(
                                    RichTextFormat.BOLD,
                                    RichTextFormat.ITALIC,
                                    RichTextFormat.UNDERLINE,
                                    RichTextFormat.STRIKETHROUGH,
                                    RichTextFormat.INLINE_CODE
                                )
                            } else {
                                base
                            }
                        } else {
                            base
                        }
                    }

                    CometChatRichTextToolbar(
                        modifier = Modifier.fillMaxWidth(),
                        style = style,
                        activeFormats = effectiveActiveFormats,
                        disabledFormats = effectiveDisabledFormats,
                        enabledFormats = enabledFormats,
                        onFormatClick = { format ->
                            android.util.Log.d("SegmentDebug", "onFormatClick: format=$format, isTypingInCode=${segmentController.isTypingInCode}, focusedSegmentId=${segmentController.focusedSegmentId}")
                            
                            // If no segment is focused, focus the first normal segment
                            if (segmentController.focusedSegment == null) {
                                val firstNormal = segmentController.segments.firstOrNull { it is ComposerSegment.Normal }
                                if (firstNormal != null) {
                                    segmentController.focusSegment(firstNormal.id)
                                }
                            } else {
                                // Ensure the focused segment gets keyboard focus
                                segmentController.focusedSegmentId?.let { id ->
                                    focusRequesters[id]?.requestFocus()
                                }
                            }
                            
                            val focusedSeg = segmentController.focusedSegment
                            when {
                                // Case 1: Tapping CODE_BLOCK while inside a Code segment → extract cursor paragraph
                                format == RichTextFormat.CODE_BLOCK && focusedSeg is ComposerSegment.Code -> {
                                    android.util.Log.d("SegmentDebug", "onFormatClick: extracting paragraph from code block (deselect), cursor=${focusedSeg.cursorPosition}")
                                    segmentController.extractParagraphFromCodeBlock(focusedSeg.cursorPosition, null)
                                }
                                // Case 2: Tapping a line format (blockquote/list) while inside a Code segment
                                // → extract cursor paragraph and apply that format
                                focusedSeg is ComposerSegment.Code && format in setOf(
                                    RichTextFormat.BULLET_LIST, RichTextFormat.ORDERED_LIST, RichTextFormat.BLOCKQUOTE
                                ) -> {
                                    android.util.Log.d("SegmentDebug", "onFormatClick: extracting paragraph from code block with format=$format, cursor=${focusedSeg.cursorPosition}")
                                    segmentController.extractParagraphFromCodeBlock(focusedSeg.cursorPosition, format)
                                }
                                // Case 3: Tapping CODE_BLOCK while in a Normal segment → convert cursor paragraph to code
                                format == RichTextFormat.CODE_BLOCK && focusedSeg is ComposerSegment.Normal -> {
                                    android.util.Log.d("SegmentDebug", "onFormatClick: convertCursorParagraphToCodeBlock")
                                    segmentController.convertCursorParagraphToCodeBlock()
                                }
                                // Case 4: Tapping any format while in a Normal segment → toggle format
                                focusedSeg is ComposerSegment.Normal -> {
                                    focusedSeg.controller.toggleFormat(format)
                                }
                            }
                            // Increment formatVersion to trigger toolbar recomposition
                            // so activeFormats/disabledFormats are re-read after the toggle
                            formatVersion++
                        },
                        onLinkClick = {
                            val focused = segmentController.focusedSegment as? ComposerSegment.Normal
                            focused?.controller?.let { ctrl ->
                                val spanManager = ctrl.state.spanManager
                                val selStart = ctrl.state.selectionStart
                                val selEnd = ctrl.state.selectionEnd
                                val text = ctrl.state.text

                                // Check if cursor is inside a LINK span
                                val checkPos = if (selStart > 0) selStart - 1 else selStart
                                val linkSpan = spanManager.findLinkSpanAt(checkPos)
                                    ?: spanManager.findLinkSpanAt(selStart)

                                if (linkSpan != null) {
                                    // Cursor is inside an existing link → show Link popup
                                    linkEditInitialText = text.substring(linkSpan.start, linkSpan.end)
                                    linkEditInitialUrl = spanManager.getLinkUrlAt(linkSpan.start) ?: ""
                                    linkEditSpanStart = linkSpan.start
                                    linkEditSpanEnd = linkSpan.end
                                    showLinkPopup = true
                                } else if (selStart != selEnd) {
                                    // Text is selected but not a link → Add Link dialog with pre-filled text
                                    isLinkEditMode = false
                                    linkEditInitialText = text.substring(selStart, selEnd)
                                    linkEditInitialUrl = ""
                                    linkEditSpanStart = -1
                                    linkEditSpanEnd = -1
                                    showLinkDialog = true
                                } else {
                                    // No selection, no link → Add Link dialog with empty fields
                                    isLinkEditMode = false
                                    linkEditInitialText = ""
                                    linkEditInitialUrl = ""
                                    linkEditSpanStart = -1
                                    linkEditSpanEnd = -1
                                    showLinkDialog = true
                                }
                            }
                        }
                    )
                }
            }
        } // End of compose box Column

        // Footer view slot
        footerView?.invoke()

        // Sticker keyboard panel (shown when sticker button is clicked)
        AnimatedVisibility(
            visible = showStickerKeyboard,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CometChatStickerKeyboard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lastKeyboardHeight.coerceAtLeast(296.dp)),
                style = stickerKeyboardStyle,
                onStickerClick = { sticker ->
                    // Send the sticker as a custom message
                    val pushNotificationMessage = context.getString(R.string.cometchat_shared_sticker)
                    composerViewModel.sendStickerMessage(
                        stickerUrl = sticker.url,
                        stickerName = sticker.name,
                        pushNotificationMessage = pushNotificationMessage
                    )
                    // Also invoke the callback for any additional handling
                    onStickerSelected?.invoke(sticker)
                },
                onError = { exception ->
                    onError?.invoke(exception)
                }
            )
        }
    }

        // Transient error alert for composer messages (e.g. attachment rejection reasons). Rendered in
        // an overlay Popup pinned just ABOVE the composer (its bottom edge sits at the composer's
        // top), mirroring the Views CometChatErrorAlert / iOS behaviour instead of overlapping the
        // composer content.
        // Only compose the Popup while there is a message. Presence drives show/hide — animating an
        // AnimatedVisibility inside an always-present, self-sizing Popup made the alert flash/double
        // on appear and stay stuck on ✕, because the Popup re-anchors per frame and never tears the
        // exit frame down cleanly.
        if (!attachmentErrorMessage.isNullOrBlank()) {
            val errorAlertDensity = LocalDensity.current
            val errorAlertSidePaddingPx = with(errorAlertDensity) { 8.dp.roundToPx() }
            val errorAlertGapPx = with(errorAlertDensity) { 8.dp.roundToPx() }
            Popup(
                popupPositionProvider = object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset = IntOffset(
                        x = anchorBounds.left + errorAlertSidePaddingPx,
                        y = (anchorBounds.top - popupContentSize.height - errorAlertGapPx).coerceAtLeast(0)
                    )
                },
                properties = PopupProperties(focusable = false)
            ) {
                val errorAlertWidthDp = with(errorAlertDensity) {
                    (composerWidthPx - errorAlertSidePaddingPx * 2).coerceAtLeast(0).toDp()
                }
                CometChatErrorAlert(
                    message = attachmentErrorMessage,
                    onDismiss = { attachmentErrorMessage = null },
                    modifier = Modifier.width(errorAlertWidthDp),
                    style = attachmentErrorAlertStyle
                )
            }
        }
    }

    // Link edit dialog
    if (showLinkDialog) {
        CometChatLinkEditDialog(
            style = style,
            initialText = linkEditInitialText,
            initialUrl = linkEditInitialUrl,
            isEditMode = isLinkEditMode,
            onApply = { text, url ->
                val focused = segmentController.focusedSegment as? ComposerSegment.Normal
                focused?.controller?.let { ctrl ->
                    if (isLinkEditMode && linkEditSpanStart >= 0 && linkEditSpanEnd >= 0) {
                        // Edit existing link
                        ctrl.editLink(linkEditSpanStart, linkEditSpanEnd, text, url)
                    } else {
                        // Add new link
                        ctrl.applyLink(text, url)
                    }
                }
                // Trigger recomposition so NormalSegmentTextField syncs tfv from controller
                formatVersion++
                showLinkDialog = false
            },
            onRemove = null,
            onDismiss = { showLinkDialog = false }
        )
    }

    // Link popup dialog (shown when tapping link tool while cursor is inside a link)
    if (showLinkPopup) {
        CometChatLinkPopupDialog(
            url = linkEditInitialUrl,
            style = style,
            onEdit = {
                // Dismiss popup, show Edit Link dialog pre-filled
                showLinkPopup = false
                isLinkEditMode = true
                showLinkDialog = true
            },
            onRemove = {
                // Remove the link, keep text plain
                if (linkEditSpanStart >= 0 && linkEditSpanEnd >= 0) {
                    val focused = segmentController.focusedSegment as? ComposerSegment.Normal
                    focused?.controller?.removeLink(linkEditSpanStart, linkEditSpanEnd)
                }
                // Trigger recomposition so toolbar and text field update
                formatVersion++
                showLinkPopup = false
            },
            onDismiss = { showLinkPopup = false }
        )
    }

    // Create Poll bottom sheet (using ModalBottomSheet like showcase for proper keyboard handling)
    var isPollSubmitting by remember { mutableStateOf(false) }
    var pollErrorMessage by remember { mutableStateOf<String?>(null) }
    
    if (showCreatePollDialog) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { 
                if (!isPollSubmitting) {
                    showCreatePollDialog = false
                    pollErrorMessage = null
                }
            },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            CometChatCreatePoll(
                modifier = Modifier.fillMaxSize(),
                isSubmitting = isPollSubmitting,
                errorMessage = pollErrorMessage,
                onSubmitClick = { question, options ->
                    isPollSubmitting = true
                    pollErrorMessage = null
                    composerViewModel.createPoll(
                        question = question,
                        options = options,
                        onSuccess = {
                            isPollSubmitting = false
                            showCreatePollDialog = false
                            pollErrorMessage = null
                        },
                        onError = { exception ->
                            isPollSubmitting = false
                            pollErrorMessage = exception.message ?: context.getString(R.string.cometchat_something_went_wrong)
                        }
                    )
                },
                onBackPress = {
                    if (!isPollSubmitting) {
                        showCreatePollDialog = false
                        pollErrorMessage = null
                    }
                }
            )
        }
    }

    // TODO: Add attachment sheet, AI sheet, and media recorder dialogs
    // These will be implemented in subsequent tasks
}

/**
 * Handles the send action for the message composer.
 * Uses [segmentController.toMarkdown()][SegmentComposerController.toMarkdown] to serialize
 * all segments (Normal + Code) into a single markdown string.
 * When mentions are present, processes the text to replace prompt text with underlying text
 * and calls handlePreMessageSend on all formatters.
 */
private fun handleSend(
    context: Context,
    segmentController: SegmentComposerController,
    editMessage: BaseMessage?,
    viewModel: CometChatMessageComposerViewModel,
    onSendButtonClick: ((Context, BaseMessage) -> Unit)?,
    onClear: () -> Unit,
    mentionInsertionState: com.cometchat.uikit.compose.presentation.shared.mentions.ComposeMentionInsertionState? = null,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    sendStagedAttachments: Boolean = false
) {
    // Serialize all segments to markdown via the controller
    val markdownText = segmentController.toMarkdown()
    android.util.Log.d("MessageComposer", "handleSend: markdownText='$markdownText', length=${markdownText.length}")
    android.util.Log.d("MessageComposer", "handleSend: mentionInsertionState=${mentionInsertionState != null}, mentionCount=${mentionInsertionState?.getMentionsManager()?.getMentions()?.size ?: 0}")

    // Process mentions on the markdown text
    var textToSend = markdownText
    if (mentionInsertionState != null) {
        textToSend = mentionInsertionState.getProcessedText(markdownText)
        android.util.Log.d("MessageComposer", "handleSend: textToSend after mention processing='$textToSend', length=${textToSend.length}")
    }

    android.util.Log.d("MessageComposer", "handleSend: final textToSend='$textToSend'")

    // Multi-attachment send: the staged attachments go out as a single media message with the
    // current text as caption (which may be blank). Takes precedence over a plain text send.
    if (sendStagedAttachments) {
        viewModel.sendStagedAttachments(caption = textToSend.takeIf { it.isNotBlank() })
        // Compose clears the input programmatically, which never fires onValueChange —
        // end the typing session here, where legacy's clear-triggered watcher would have
        viewModel.endTyping()
        onClear()
        return
    }

    if (textToSend.isBlank()) return

    if (onSendButtonClick != null) {
        viewModel.createTextMessage(textToSend)?.let { message ->
            // Call handlePreMessageSend on all formatters to attach mentioned users
            textFormatters.forEach { formatter ->
                formatter.handlePreMessageSend(context, message)
            }
            onSendButtonClick(context, message)
        }
    } else {
        if (editMessage != null) {
            viewModel.editMessage(textToSend)
            viewModel.clearEditMessage()
        } else {
            // Create message first to call handlePreMessageSend
            val message = viewModel.createTextMessage(textToSend)
            if (message != null) {
                // Call handlePreMessageSend on all formatters to attach mentioned users
                textFormatters.forEach { formatter ->
                    formatter.handlePreMessageSend(context, message)
                }
                viewModel.sendTextMessageWithMentions(message)
            } else {
                viewModel.sendTextMessage(textToSend)
            }
        }
    }
    // Compose clears the input programmatically, which never fires onValueChange —
    // end the typing session here, where legacy's clear-triggered watcher would have
    viewModel.endTyping()
    onClear()
}

/**
 * Renders a code block segment as a dark-background monospace BasicTextField.
 * No rich text formatting — plain text only. Mention detection is intentionally
 * not wired here, so "@" typed in a Code segment does not trigger suggestions.
 *
 * Handles:
 * - Double-enter exit: calls [segmentController.handleCodeTextChanged] which detects \n\n
 * - Backspace-on-empty: calls [segmentController.handleBackspaceOnEmptyCodeBlock]
 * - External [focusRequester] from the parent's focus map (pending focus is consumed
 *   by the shared LaunchedEffect in the parent composable)
 */
@Composable
private fun CodeSegmentTextField(
    segment: ComposerSegment.Code,
    segmentController: SegmentComposerController,
    focusRequester: FocusRequester,
    style: CometChatMessageComposerStyle,
    modifier: Modifier = Modifier,
    onFocused: () -> Unit,
    onTextChanged: (String) -> Unit
) {
    // Note: "@" typed inside a Code segment intentionally does NOT trigger mention
    // detection. CodeSegmentTextField has no mention detection wiring, so mentions
    // are suppressed by design — no additional logic is needed.

    var tfv by remember(segment.id) {
        mutableStateOf(TextFieldValue(segment.text))
    }

    // Sync tfv when segment text changes externally (e.g., double-enter trim)
    LaunchedEffect(segment.text) {
        if (tfv.text != segment.text) {
            tfv = TextFieldValue(segment.text, TextRange(segment.text.length))
        }
    }

    val codeBackground = CometChatTheme.colorScheme.backgroundColor2
    val codeBorderColor = CometChatTheme.colorScheme.strokeColorDefault
    val codeTextColor = CometChatTheme.colorScheme.textColorPrimary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(codeBackground)
            .border(1.dp, codeBorderColor, RoundedCornerShape(8.dp))
    ) {
        BasicTextField(
            value = tfv,
            onValueChange = { newValue ->
                val prevText = tfv.text
                val newText = newValue.text

                // Detect backspace on empty → remove code block
                if (prevText.isEmpty() && newText.isEmpty()) {
                    segmentController.handleBackspaceOnEmptyCodeBlock(segment)
                    return@BasicTextField
                }

                // When all text is deleted, update segment text but keep the code block.
                // Only backspace on an already-empty code block removes it (handled above
                // and in onPreviewKeyEvent).
                if (prevText.isNotEmpty() && newText.isEmpty()) {
                    tfv = newValue
                    segment.text = ""
                    onTextChanged("")
                    return@BasicTextField
                }

                tfv = newValue

                // Track cursor position for paragraph extraction
                segment.cursorPosition = newValue.selection.start

                // Route through segment controller for double-enter detection.
                // When exited is true, the controller has trimmed trailing newlines
                // and set pendingFocusSegmentId — the shared LaunchedEffect in the
                // parent composable will consume the pending focus and transfer
                // keyboard focus to the next Normal segment.
                val exited = segmentController.handleCodeTextChanged(segment, newText)
                if (exited) {
                    return@BasicTextField
                }

                onTextChanged(newText)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onFocused() }
                .onPreviewKeyEvent { keyEvent ->
                    // Intercept backspace on empty code block — onValueChange won't fire
                    // because there's nothing to delete, so we catch it here.
                    if (keyEvent.key == Key.Backspace &&
                        keyEvent.type == KeyEventType.KeyDown &&
                        tfv.text.isEmpty()
                    ) {
                        segmentController.handleBackspaceOnEmptyCodeBlock(segment)
                        true
                    } else {
                        false
                    }
                },
            textStyle = style.inputTextStyle.copy(
                color = codeTextColor,
                fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(codeTextColor),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (tfv.text.isEmpty()) {
                        Text(
                            text = "Enter code...",
                            color = CometChatTheme.colorScheme.textColorTertiary,
                            style = style.inputTextStyle.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

/**
 * Renders a Normal segment as a BasicTextField with rich text support.
 *
 * Each Normal segment owns its own [RichTextEditorController] (`segment.controller`).
 * Visual transformations (rich text spans + mention styling) are created per-segment
 * and combined via [CombinedVisualTransformation].
 *
 * Handles:
 * - Rich text WYSIWYG rendering via [SpanBasedVisualTransformation]
 * - Mention styling via [ComposerMentionVisualTransformation]
 * - Backspace-at-position-0 to navigate to preceding Code segment
 * - Mention detection on text change
 * - Typing indicator integration
 */
@Composable
private fun NormalSegmentTextField(
    segment: ComposerSegment.Normal,
    segmentController: SegmentComposerController,
    focusRequester: FocusRequester,
    style: CometChatMessageComposerStyle,
    enabledFormats: Set<RichTextFormat>,
    placeholder: String,
    showPlaceholder: Boolean,
    onTextChanged: (String) -> Unit,
    onFocused: () -> Unit,
    mentionInsertionState: ComposeMentionInsertionState,
    mentionDetectionState: ComposeMentionState,
    onMentionDetected: (ComposeMentionState) -> Unit,
    showSuggestionList: Boolean,
    onShowSuggestionList: (Boolean) -> Unit,
    effectiveTextFormatters: List<CometChatTextFormatter>,
    disableMentions: Boolean,
    composerViewModel: CometChatMessageComposerViewModel,
    formatVersion: Int = 0,
    onCodeBlockInserted: (() -> Unit)? = null,
    onLinkTapped: ((linkText: String, linkUrl: String, spanStart: Int, spanEnd: Int) -> Unit)? = null,
    onSelectionChanged: (() -> Unit)? = null,
    onMediaPaste: (() -> Boolean)? = null,
    hasClipboardMedia: (() -> Boolean)? = null,
    modifier: Modifier = Modifier
) {
    // Per-segment text state backed by the segment's own controller
    var tfv by remember(segment.id) {
        mutableStateOf(
            TextFieldValue(
                text = segment.controller.state.text,
                selection = TextRange(segment.controller.state.text.length)
            )
        )
    }

    // Sync tfv when segment text changes externally (e.g., code block removal merges text,
    // or toolbar toggleFormat modifies text for line formats like bullet list/blockquote).
    // Reading formatVersion ensures this composable recomposes when toolbar actions change
    // the controller's text.
    @Suppress("UNUSED_VARIABLE")
    val currentFormatVersion = formatVersion
    val segmentText = segment.controller.state.text
    val segmentSelStart = segment.controller.state.selectionStart
    val segmentSelEnd = segment.controller.state.selectionEnd
    LaunchedEffect(segmentText, segmentSelStart, segmentSelEnd) {
        if (tfv.text != segmentText || tfv.selection.start != segmentSelStart || tfv.selection.end != segmentSelEnd) {
            tfv = TextFieldValue(segmentText, TextRange(segmentSelStart, segmentSelEnd))
        }
    }

    // Per-segment visual transformations
    val inlineCodeTextColor = CometChatTheme.colorScheme.textColorHighlight
    val inlineCodeBgColor = CometChatTheme.colorScheme.backgroundColor3
    val spanTransformation = remember(segment.id, inlineCodeTextColor, inlineCodeBgColor) {
        SpanBasedVisualTransformation(
            controller = segment.controller,
            inputTextColor = style.inputTextColor,
            inlineCodeTextColor = inlineCodeTextColor,
            inlineCodeBackgroundColor = inlineCodeBgColor,
            linkColor = Color(0xFF3D88F5)
        )
    }

    val mentionPrimaryColor = CometChatTheme.colorScheme.primary
    val mentionTransformation = remember(segment.id, mentionPrimaryColor) {
        ComposerMentionVisualTransformation(
            mentionInsertionState = mentionInsertionState,
            defaultMentionStyle = SpanStyle(
                color = mentionPrimaryColor,
                fontWeight = FontWeight.Medium,
                background = mentionPrimaryColor.copy(alpha = 0.2f)
            )
        )
    }

    val combinedTransformation = remember(segment.id) {
        CombinedVisualTransformation(
            listOf(spanTransformation, mentionTransformation)
        )
    }

    val inputTextColor = style.inputTextColor

    // Track text layout for drawing blockquote bars and inline code
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    val blockquoteBarColor = CometChatTheme.colorScheme.strokeColorDefault
    val inlineCodeBorderColor = CometChatTheme.colorScheme.strokeColorDark
    val inlineCodeCornerRadiusDp = 5.dp

    // Custom text selection toolbar: appends Bold/Italic/Strikethrough/Code to the
    // default floating menu while preserving system positioning behaviour.
    val view = LocalView.current
    val richTextSelectionToolbar = remember(view) {
        RichTextSelectionToolbar(view) { format ->
            segment.controller.toggleFormat(format)
            onSelectionChanged?.invoke()
        }
    }

    // Media paste support: wrap whichever toolbar is active so the Paste action can stage
    // clipboard media (image/video/audio/file uris) into the attachment tray — the legacy
    // BasicTextField itself can only paste text.
    val baseToolbar = if (enabledFormats.isNotEmpty()) richTextSelectionToolbar else LocalTextToolbar.current
    val currentOnMediaPaste by rememberUpdatedState(onMediaPaste)
    val currentHasClipboardMedia by rememberUpdatedState(hasClipboardMedia)
    val mediaAwareToolbar = remember(baseToolbar) {
        MediaPasteTextToolbar(
            delegate = baseToolbar,
            onMediaPaste = { currentOnMediaPaste?.invoke() ?: false },
            hasClipboardMedia = { currentHasClipboardMedia?.invoke() ?: false }
        )
    }

    CompositionLocalProvider(
        LocalTextToolbar provides mediaAwareToolbar
    ) {
    BasicTextField(
            value = tfv,
            onValueChange = { newValue: TextFieldValue ->
            val prevText = tfv.text
            val newText = newValue.text

            // --- Backspace-at-mention: delete entire mention in one press ---
            if (!disableMentions && prevText.length - newText.length == 1 && newValue.selection.collapsed) {
                val cursorPos = newValue.selection.start
                val mentions = mentionInsertionState.getMentionsManager().getMentions()
                val mentionToDelete = mentions.find { m ->
                    // Cursor was right after the mention end, and one char was deleted from it
                    cursorPos >= m.spanStart && cursorPos < m.spanEnd
                }
                if (mentionToDelete != null) {
                    val before = prevText.substring(0, mentionToDelete.spanStart)
                    val after = if (mentionToDelete.spanEnd < prevText.length) prevText.substring(mentionToDelete.spanEnd) else ""
                    val cleaned = before + after
                    mentionInsertionState.getMentionsManager().removeMention(mentionToDelete.id)
                    mentionInsertionState.syncWithText(cleaned)
                    val newCursor = mentionToDelete.spanStart.coerceIn(0, cleaned.length)
                    tfv = TextFieldValue(cleaned, TextRange(newCursor))
                    segment.controller.onTextChanged(cleaned, newCursor, newCursor)
                    onTextChanged(cleaned)
                    // Dismiss suggestion list and reset mention detection
                    onMentionDetected(ComposeMentionState.INACTIVE)
                    onShowSuggestionList(false)
                    return@BasicTextField
                }
            }

            tfv = newValue

            // Detect tap on link text: cursor moved without text change, and cursor
            // is now inside a LINK span → show the link popup
            if (prevText == newText && enabledFormats.isNotEmpty() && onLinkTapped != null) {
                val cursorPos = newValue.selection.start
                if (newValue.selection.start == newValue.selection.end && cursorPos > 0) {
                    val spanManager = segment.controller.state.spanManager
                    val linkSpan = spanManager.findLinkSpanAt(cursorPos - 1)
                        ?: spanManager.findLinkSpanAt(cursorPos)
                    if (linkSpan != null) {
                        val text = segment.controller.state.text
                        val safeStart = linkSpan.start.coerceIn(0, text.length)
                        val safeEnd = linkSpan.end.coerceIn(safeStart, text.length)
                        if (safeStart < safeEnd) {
                            val lt = text.substring(safeStart, safeEnd)
                            val lu = spanManager.getLinkUrlAt(linkSpan.start) ?: ""
                            onLinkTapped(lt, lu, safeStart, safeEnd)
                        }
                    }
                }
            }

            // Route text change through the segment's own RichTextEditorController.
            // This MUST happen regardless of `enabledFormats` so the segment controller
            // is the source of truth for composed text — the send button's active state
            // is derived from `segmentController.hasContent`, which reads from the
            // controller. Without this, typing with rich-text disabled leaves the
            // controller empty and the send button stays disabled.
            segment.controller.onTextChanged(
                newText,
                newValue.selection.min,
                newValue.selection.max
            )

            if (enabledFormats.isNotEmpty()) {
                // The controller may have modified the text internally (e.g.,
                // auto-continuation of list/blockquote prefixes, or removal of
                // an empty prefix line on double-enter). Sync tfv back if so.
                val controllerText = segment.controller.state.text
                val controllerSelStart = segment.controller.state.selectionStart
                val controllerSelEnd = segment.controller.state.selectionEnd
                if (controllerText != newText || controllerSelStart != newValue.selection.min || controllerSelEnd != newValue.selection.max) {
                    tfv = TextFieldValue(
                        controllerText,
                        TextRange(controllerSelStart, controllerSelEnd)
                    )
                }

                // Detect pasted fenced code blocks (```...```) FIRST — before shortcut detection
                // so that pasting ```code``` converts to a code segment instead of
                // the shortcut consuming the opening ``` and leaving text + empty code block
                if (segmentController.detectAndConvertPastedCodeBlocks()) {
                    onCodeBlockInserted?.invoke()
                    return@BasicTextField
                }

                // Detect ``` shortcut (user typed ``` on a line) and insert code block if found
                if (segmentController.detectAndInsertCodeBlockShortcut()) {
                    onCodeBlockInserted?.invoke()
                    return@BasicTextField
                }
            }

            // --- Task 3.3: Mention detection ---
            if (!disableMentions && effectiveTextFormatters.isNotEmpty()) {
                val detected = detectMention(
                    text = newText,
                    cursorPosition = newValue.selection.start,
                    textFormatters = effectiveTextFormatters
                )
                onMentionDetected(detected)
                onShowSuggestionList(detected.isActive)

                // Sync mention insertion state with the segment's text
                mentionInsertionState.syncWithText(newText)
            }

            // --- Task 3.4: Typing indicator integration ---
            if (newText.isNotEmpty()) {
                composerViewModel.startTyping()
            } else if (!segmentController.hasContent) {
                composerViewModel.endTyping()
            }

            onTextChanged(newText)

            // Notify that selection/content changed so toolbar can update active formats
            onSelectionChanged?.invoke()
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    segmentController.setFocusedSegment(segment.id)
                    onFocused()
                }
            }
            // --- Task 3.2: Backspace-at-position-0 handling ---
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.key == Key.Backspace && keyEvent.type == KeyEventType.KeyDown) {
                    val cursorPos = tfv.selection.start
                    if (cursorPos == 0) {
                        val idx = segmentController.segments.indexOf(segment)
                        // Check if there's a Code segment before this Normal segment
                        val prevCode = if (idx > 0) {
                            segmentController.segments.getOrNull(idx - 1) as? ComposerSegment.Code
                        } else null

                        if (prevCode != null) {
                            if (tfv.text.isEmpty()) {
                                // Empty Normal with preceding Code → remove this Normal, focus Code
                                segmentController.handleBackspaceOnEmptyNormalSegment(segment)
                                true
                            } else {
                                // Non-empty Normal with preceding Code → move focus to end of Code
                                segmentController.focusSegment(prevCode.id)
                                true // consume the key event to prevent deleting text
                            }
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                } else {
                    false
                }
            },
        textStyle = style.inputTextStyle.copy(color = inputTextColor),
        cursorBrush = SolidColor(inputTextColor),
        visualTransformation = if (enabledFormats.isNotEmpty()) combinedTransformation else VisualTransformation.None,
        onTextLayout = { result -> textLayoutResult = result },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        val layout = textLayoutResult

                        // --- Draw inline code backgrounds BEFORE content ---
                        if (layout != null && spanTransformation.inlineCodeRanges.isNotEmpty()) {
                            val hPad = 4f
                            val vPad = 2f
                            val cornerPx = inlineCodeCornerRadiusDp.toPx()
                            for (range in spanTransformation.inlineCodeRanges) {
                                val pathBounds = layout.getPathForRange(range.first, range.last + 1)
                                val bounds = pathBounds.getBounds()
                                if (bounds.width > 0f && bounds.height > 0f) {
                                    drawRoundRect(
                                        color = inlineCodeBgColor,
                                        topLeft = Offset(bounds.left - hPad, bounds.top - vPad),
                                        size = Size(bounds.width + hPad * 2, bounds.height + vPad * 2),
                                        cornerRadius = CornerRadius(cornerPx, cornerPx)
                                    )
                                }
                            }
                        }

                        // --- Draw content (text, placeholder, cursor) ---
                        drawContent()

                        // --- Draw inline code borders AFTER content ---
                        if (layout != null && spanTransformation.inlineCodeRanges.isNotEmpty()) {
                            val hPad = 4f
                            val vPad = 2f
                            val cornerPx = inlineCodeCornerRadiusDp.toPx()
                            val borderPx = 1.dp.toPx()
                            for (range in spanTransformation.inlineCodeRanges) {
                                val pathBounds = layout.getPathForRange(range.first, range.last + 1)
                                val bounds = pathBounds.getBounds()
                                if (bounds.width > 0f && bounds.height > 0f) {
                                    drawRoundRect(
                                        color = inlineCodeBorderColor,
                                        topLeft = Offset(bounds.left - hPad, bounds.top - vPad),
                                        size = Size(bounds.width + hPad * 2, bounds.height + vPad * 2),
                                        cornerRadius = CornerRadius(cornerPx, cornerPx),
                                        style = Stroke(width = borderPx)
                                    )
                                }
                            }
                        }

                        // --- Draw blockquote bars AFTER content ---
                        if (layout != null) {
                            val rawText = segment.controller.state.text
                            if (rawText.isNotEmpty()) {
                                val rawLines = rawText.split("\n")
                                val barWidthPx = 3.dp.toPx()
                                val barCornerRadius = barWidthPx / 2f
                                val layoutTextLen = layout.layoutInput.text.length
                                if (layoutTextLen > 0) {
                                    // Track display-text offsets (accounting for "> " → "   " expansion)
                                    var displayCharOffset = 0
                                    var blockquoteFirstDisplayOffset = -1
                                    var blockquoteLastDisplayOffset = -1

                                    for ((lineIdx, rawLine) in rawLines.withIndex()) {
                                        val isBlockquote = rawLine.startsWith("> ")
                                        // Display line length: blockquote adds +1 char (2→3), bullet adds +1 (2→3)
                                        val displayLineLen = when {
                                            rawLine.startsWith("> ") -> rawLine.length + 1
                                            rawLine.startsWith("- ") -> rawLine.length + 1
                                            else -> rawLine.length
                                        }
                                        val displayLineEnd = displayCharOffset + displayLineLen

                                        if (isBlockquote) {
                                            if (blockquoteFirstDisplayOffset == -1) {
                                                blockquoteFirstDisplayOffset = displayCharOffset
                                            }
                                            blockquoteLastDisplayOffset = displayLineEnd
                                        }

                                        if ((!isBlockquote || lineIdx == rawLines.size - 1) && blockquoteFirstDisplayOffset >= 0) {
                                            // Use display offsets for layout queries
                                            val safeFirst = blockquoteFirstDisplayOffset.coerceIn(0, layoutTextLen - 1)
                                            // For the last offset, use the end of the last blockquote line
                                            // but clamp to valid range. For empty lines (just "> "), use safeFirst.
                                            val safeLast = (blockquoteLastDisplayOffset - 1).coerceIn(safeFirst, layoutTextLen - 1)
                                            val firstVisualLine = layout.getLineForOffset(safeFirst)
                                            val lastVisualLine = layout.getLineForOffset(safeLast)
                                            val topY = layout.getLineTop(firstVisualLine)
                                            val bottomY = layout.getLineBottom(lastVisualLine)
                                            val paddingPx = 4.dp.toPx()
                                            val adjustedTop = if (blockquoteFirstDisplayOffset > 0) topY + paddingPx else topY
                                            val adjustedBottom = if (lineIdx < rawLines.size - 1 || !isBlockquote) bottomY - paddingPx else bottomY
                                            if (adjustedBottom > adjustedTop) {
                                                drawRoundRect(
                                                    color = blockquoteBarColor,
                                                    topLeft = Offset(0f, adjustedTop),
                                                    size = Size(barWidthPx, adjustedBottom - adjustedTop),
                                                    cornerRadius = CornerRadius(barCornerRadius, barCornerRadius)
                                                )
                                            }
                                            blockquoteFirstDisplayOffset = -1
                                            blockquoteLastDisplayOffset = -1
                                        }

                                        displayCharOffset = displayLineEnd + 1 // +1 for \n
                                    }
                                }
                            }
                        }
                    }
            ) {
                if (showPlaceholder && tfv.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = style.inputPlaceholderColor,
                        style = style.inputPlaceholderStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                innerTextField()
            }
        }
    )
    } // End CompositionLocalProvider
}

/**
 * Span-based visual transformation that renders WYSIWYG rich text.
 * Reads formatting spans from the controller at render time (not captured at creation).
 * This avoids stale span data when recomposition timing causes the transformation
 * to be evaluated before the controller state is fully consistent.
 */
private class SpanBasedVisualTransformation(
    private val controller: RichTextEditorController,
    private val inputTextColor: Color = Color.Unspecified,
    private val inlineCodeTextColor: Color = Color.Unspecified,
    private val inlineCodeBackgroundColor: Color = Color.LightGray.copy(alpha = 0.3f),
    private val linkColor: Color = Color.Unspecified
) : VisualTransformation {

    /** Inline code ranges in the *transformed* (display) text, updated on each filter() call. */
    var inlineCodeRanges: List<IntRange> = emptyList()
        private set

    override fun filter(text: AnnotatedString): TransformedText {
        val currentSpans = controller.state.spans
        val rawText = text.text

        // Build display string replacing line prefixes with visual equivalents.
        // Bullet "- " (2 chars) → "•  " (3 chars) to align with "N. " (3 chars).
        // Blockquote "> " (2 chars) → "┃  " (3 chars) for consistent alignment.
        // Since lengths differ, we use a custom OffsetMapping.
        val lines = rawText.split("\n")
        // Track cumulative offset difference per line for the offset mapping
        // offsetDiffs[i] = total extra chars inserted before position i in the raw text
        val offsetAtRawPos = IntArray(rawText.length + 1) // offsetAtRawPos[rawPos] = extra chars before rawPos

        val displayText = buildString {
            var rawIdx = 0
            var extraChars = 0
            for ((i, line) in lines.withIndex()) {
                when {
                    line.startsWith("- ") -> {
                        append("•  ") // 3 chars (aligns with "N. ")
                        append(line.substring(2))
                        // The "- " (2 raw chars) became "•  " (3 display chars) → +1 extra
                        // Mark offset for each raw position in this line
                        for (j in 0 until 2) {
                            offsetAtRawPos[rawIdx + j] = extraChars
                        }
                        extraChars += 1 // +1 for the extra space
                        for (j in 2..line.length) {
                            offsetAtRawPos[rawIdx + j] = extraChars
                        }
                    }
                    line.startsWith("> ") -> {
                        append("   ") // 3 spaces — bar drawn via drawBehind, not text
                        append(line.substring(2))
                        for (j in 0 until 2) {
                            offsetAtRawPos[rawIdx + j] = extraChars
                        }
                        extraChars += 1
                        for (j in 2..line.length) {
                            offsetAtRawPos[rawIdx + j] = extraChars
                        }
                    }
                    else -> {
                        append(line)
                        for (j in 0..line.length) {
                            offsetAtRawPos[rawIdx + j] = extraChars
                        }
                    }
                }
                rawIdx += line.length
                if (i < lines.size - 1) {
                    append("\n")
                    offsetAtRawPos[rawIdx] = extraChars
                    rawIdx += 1 // for the \n
                }
            }
            // Final position
            if (rawIdx <= rawText.length) {
                offsetAtRawPos[rawIdx] = extraChars
            }
        }

        val totalExtraChars = displayText.length - rawText.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, rawText.length)
                return safeOffset + offsetAtRawPos[safeOffset]
            }
            override fun transformedToOriginal(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, displayText.length)
                // Binary search for the raw position
                var lo = 0
                var hi = rawText.length
                while (lo < hi) {
                    val mid = (lo + hi + 1) / 2
                    if (mid + offsetAtRawPos[mid] <= safeOffset) lo = mid else hi = mid - 1
                }
                return lo
            }
        }

        val styled = buildAnnotatedString {
            append(displayText)

            // Collect inline code ranges in display coordinates for custom drawing
            val collectedInlineCodeRanges = mutableListOf<IntRange>()

            // Apply inline format spans — use offset mapping to convert raw positions to display positions
            for (span in currentSpans) {
                val rawStart = span.start.coerceAtMost(rawText.length)
                val rawEnd = span.end.coerceAtMost(rawText.length)
                if (rawStart >= rawEnd) continue
                val start = offsetMapping.originalToTransformed(rawStart)
                val end = offsetMapping.originalToTransformed(rawEnd)
                if (start >= end || start >= displayText.length) continue

                val decorations = mutableListOf<TextDecoration>()
                for (format in span.formats) {
                    when (format) {
                        RichTextFormat.UNDERLINE -> decorations.add(TextDecoration.Underline)
                        RichTextFormat.STRIKETHROUGH -> decorations.add(TextDecoration.LineThrough)
                        RichTextFormat.LINK -> decorations.add(TextDecoration.Underline)
                        else -> {
                            val s = formatToSpanStyle(format)
                            if (s != null) addStyle(s, start, end.coerceAtMost(displayText.length))
                        }
                    }
                    if (format == RichTextFormat.INLINE_CODE) {
                        collectedInlineCodeRanges.add(start until end.coerceAtMost(displayText.length))
                    }
                }
                if (decorations.isNotEmpty()) {
                    addStyle(SpanStyle(textDecoration = TextDecoration.combine(decorations)), start, end.coerceAtMost(displayText.length))
                }
                if (RichTextFormat.LINK in span.formats) {
                    addStyle(SpanStyle(color = linkColor), start, end.coerceAtMost(displayText.length))
                }
            }

            inlineCodeRanges = collectedInlineCodeRanges

            // Style line-based prefixes (using display positions)
            styleLinePrefixes(displayText)

            // Add paragraph indentation for blockquote wrapped lines.
            // Check raw lines (not display lines) to identify blockquotes accurately.
            var rawCharIdx = 0
            val rawLines2 = rawText.split("\n")
            var blockStart2 = -1
            var blockEnd2 = -1
            var displayOffset = 0
            for ((i, rawLine) in rawLines2.withIndex()) {
                val isBlockquote = rawLine.startsWith("> ")
                // Calculate display position for this line
                val displayLineLen = if (isBlockquote) rawLine.length + 1 else rawLine.length // +1 for "> " → "   " (2→3 chars)
                if (isBlockquote) {
                    if (blockStart2 == -1) blockStart2 = displayOffset
                    blockEnd2 = (displayOffset + displayLineLen).coerceAtMost(displayText.length)
                }
                if ((!isBlockquote || i == rawLines2.size - 1) && blockStart2 != -1) {
                    addStyle(
                        ParagraphStyle(
                            textIndent = TextIndent(
                                firstLine = 0.sp,
                                restLine = 10.sp
                            )
                        ),
                        blockStart2,
                        blockEnd2
                    )
                    blockStart2 = -1
                    blockEnd2 = -1
                }
                displayOffset += displayLineLen + 1 // +1 for \n
            }
        }
        return TransformedText(styled, offsetMapping)
    }

    private fun formatToSpanStyle(format: RichTextFormat): SpanStyle? = when (format) {
        RichTextFormat.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
        RichTextFormat.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
        RichTextFormat.INLINE_CODE -> SpanStyle(
            fontFamily = FontFamily.Monospace,
            color = inlineCodeTextColor
        )
        RichTextFormat.CODE_BLOCK -> SpanStyle(
            fontFamily = FontFamily.Monospace,
            background = Color.LightGray.copy(alpha = 0.3f)
        )
        // UNDERLINE, STRIKETHROUGH, LINK handled via combined TextDecoration
        else -> null
    }

    private fun AnnotatedString.Builder.styleLinePrefixes(text: String) {
        val lines = text.split("\n")
        var idx = 0
        for (line in lines) {
            when {
                // Bullet list: displayed as "•  " (3 chars) — style the bullet bold with text color
                line.startsWith("•  ") -> {
                    addStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = inputTextColor
                    ), idx, idx + 3)
                }
                // Ordered list: "1. " prefix — style number+dot bold with text color
                line.matches(Regex("^\\d+\\. .*")) -> {
                    val dotIdx = line.indexOf('.')
                    if (dotIdx > 0) {
                        addStyle(SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = inputTextColor
                        ), idx, idx + dotIdx + 2)
                    }
                }
                // Blockquote lines use default text color — no special styling needed.
                // The bar is drawn via drawBehind in NormalSegmentTextField.
            }
            idx += line.length + 1 // +1 for newline
        }
    }
}

/**
 * Handles media selection result by sending the media message through the ViewModel.
 * Uses the specified message type instead of detecting from the file's MIME type.
 * This matches the Java reference behavior where the picker/action type determines the message type.
 *
 * @param result The media selection result containing file info
 * @param messageType The message type to use ("image", "video", "audio", or "file")
 * @param viewModel The message composer ViewModel to send the media message
 * @param onError Callback for error handling
 */
private fun handleMediaSelectionWithType(
    result: MediaSelectionResult,
    messageType: String,
    viewModel: CometChatMessageComposerViewModel,
    onError: ((CometChatException) -> Unit)?
) {
    val file = result.file
    if (file == null || !file.exists()) {
        onError?.invoke(CometChatException("FILE_ERROR", "Selected file does not exist"))
        return
    }
    
    // Send the media message through the ViewModel with the specified type
    viewModel.sendMediaMessage(file, messageType)
}


/**
 * Opens a fullscreen preview for a successfully-uploaded tray tile: images in the in-app
 * [CometChatImageViewerActivity], videos in an external player via ACTION_VIEW (the staged local
 * copy through FileProvider, a picker content uri with our read grant forwarded, or the uploaded
 * URL as fallback). Audio plays inline on its tile's play button and file tiles have no preview,
 * so both are ignored here.
 */
private fun openStagedAttachmentPreview(
    context: android.content.Context,
    tile: com.cometchat.uikit.core.models.AttachmentUploadTile
) {
    try {
        when (tile.category) {
            CometChatConstants.MESSAGE_TYPE_IMAGE -> {
                // Coil (the viewer's loader) needs a scheme — prefix bare file paths.
                val model = tile.localUri?.let { if (it.startsWith("/")) "file://$it" else it }
                    ?: tile.attachment?.fileUrl
                    ?: return
                context.startActivity(
                    com.cometchat.uikit.compose.presentation.imageviewer.ui.CometChatImageViewerActivity.createIntent(
                        context = context,
                        imageUrl = model,
                        fileName = tile.name,
                        mimeType = tile.mimeType
                    )
                )
            }

            CometChatConstants.MESSAGE_TYPE_VIDEO -> {
                val local = tile.localUri
                val uri = when {
                    local == null -> tile.attachment?.fileUrl?.let(android.net.Uri::parse)
                    local.startsWith("/") -> File(local).takeIf { it.exists() }?.let {
                        androidx.core.content.FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", it
                        )
                    } ?: tile.attachment?.fileUrl?.let(android.net.Uri::parse)
                    else -> android.net.Uri.parse(local)
                } ?: return
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, tile.mimeType.ifEmpty { "video/*" })
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }

            else -> Unit
        }
    } catch (e: Exception) {
        Log.e("CometChatMessageComposer", "Failed to open staged attachment preview: ${e.message}")
    }
}

/** Unwraps the [Activity] behind a composable's context (needed for drag-and-drop uri grants). */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * [TextToolbar] wrapper that makes the Paste action media-aware. The legacy TextFieldValue-based
 * BasicTextField can only paste text — and compose hides Paste entirely for media-only clips
 * (its clipboard check is text-based). This wrapper (a) intercepts Paste to stage clipboard
 * media uris into the attachment tray via [onMediaPaste], falling through to the delegate's
 * text paste when the clipboard has no media, and (b) offers a Paste action of its own when
 * compose suppressed it but [hasClipboardMedia] says there is something stageable.
 */
private class MediaPasteTextToolbar(
    private val delegate: TextToolbar,
    private val onMediaPaste: () -> Boolean,
    private val hasClipboardMedia: () -> Boolean
) : TextToolbar {

    override val status: TextToolbarStatus
        get() = delegate.status

    override fun hide() = delegate.hide()

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        val wrappedPaste: (() -> Unit)? = when {
            onPasteRequested != null -> {
                { if (!onMediaPaste()) onPasteRequested() }
            }
            hasClipboardMedia() -> {
                { onMediaPaste() }
            }
            else -> null
        }
        delegate.showMenu(rect, onCopyRequested, wrappedPaste, onCutRequested, onSelectAllRequested)
    }
}
