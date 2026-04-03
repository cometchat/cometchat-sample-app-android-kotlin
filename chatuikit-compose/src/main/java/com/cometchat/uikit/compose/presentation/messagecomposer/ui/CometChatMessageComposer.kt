package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
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
import com.cometchat.uikit.core.factory.CometChatMessageComposerViewModelFactory
import com.cometchat.uikit.core.viewmodel.CometChatMediaRecorderViewModel
import com.cometchat.uikit.core.viewmodel.CometChatInlineAudioRecorderViewModel
import com.cometchat.uikit.core.formatter.RichTextConfiguration
import com.cometchat.uikit.core.formatter.RichTextEditorController
import com.cometchat.uikit.core.formatter.RichTextEditorState
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.core.formatter.RichTextFormatterManager
import com.cometchat.uikit.core.formatter.RichTextSpan
import com.cometchat.uikit.core.formatter.ComposerSegment
import com.cometchat.uikit.core.formatter.SegmentComposerController
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import com.cometchat.uikit.core.viewmodel.ComposerMode
import com.cometchat.uikit.core.viewmodel.RecordingState
import com.cometchat.uikit.core.domain.model.CometChatMessageComposerAction
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
import com.cometchat.uikit.compose.presentation.shared.mentions.ComposerMentionVisualTransformation
import com.cometchat.uikit.compose.presentation.shared.mentions.CombinedVisualTransformation
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.domain.model.Sticker
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
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
    // Visibility controls
    hideAttachmentButton: Boolean = false,
    hideVoiceRecordingButton: Boolean = false,
    hideSendButton: Boolean = false,
    hideAuxiliaryButton: Boolean = false,
    hideStickersButton: Boolean = false,
    // Rich text formatting - toolbar visible below text input when enabled
    enableRichTextFormatting: Boolean = false,
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
    LaunchedEffect(user, group, parentMessageId) {
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

    // Collect state from ViewModel
    val editMessage by composerViewModel.editMessage.collectAsState()
    val replyMessage by composerViewModel.replyMessage.collectAsState()
    val composeText by composerViewModel.composeText.collectAsState()
    val isAIGenerating by composerViewModel.isAIGenerating.collectAsState()
    val idMap by composerViewModel.idMap.collectAsState()
    val currentUser by composerViewModel.user.collectAsState()
    val currentGroup by composerViewModel.group.collectAsState()
    val composerMode by composerViewModel.composerMode.collectAsState()

    // Local state - use TextFieldValue to track selection
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var showAttachmentPopup by remember { mutableStateOf(false) }
    var showAISheet by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var activeFormats by remember { mutableStateOf<Set<RichTextFormat>>(emptySet()) }
    var spanVersion by remember { mutableStateOf(0) }
    var showStickerKeyboard by remember { mutableStateOf(false) }
    var showCreatePollDialog by remember { mutableStateOf(false) }

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
    LaunchedEffect(user, group, effectiveTextFormatters) {
        effectiveTextFormatters.forEach { formatter ->
            formatter.setUser(user)
            formatter.setGroup(group)
        }
    }
    
    // Reactively observe suggestion list changes from the active formatter
    // This uses the exposed Compose State properties for proper recomposition
    val activeFormatter = mentionDetectionState.activeFormatter
    if (activeFormatter != null && mentionDetectionState.isActive) {
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

    // Span-based rich text controller (WYSIWYG — no markdown markers visible)
    val richTextController = remember { RichTextEditorController() }

    // Segment composer controller — manages normal + code block segments
    val segmentController = remember { SegmentComposerController() }
    // Trigger recomposition when segments change
    var segmentVersion by remember { mutableStateOf(0) }
    DisposableEffect(Unit) {
        segmentController.setListener(object : SegmentComposerController.Listener {
            override fun onSegmentsChanged() {
                segmentVersion++
            }
        })
        onDispose { segmentController.setListener(null) }
    }

    // Track activeFormats separately — updated immediately for toolbar responsiveness
    DisposableEffect(Unit) {
        richTextController.setListener(object : RichTextEditorController.Listener {
            override fun onStateChanged() {
                activeFormats = richTextController.state.activeFormats
                spanVersion++
            }
        })
        onDispose { richTextController.setListener(null) }
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
    
    // Rich text toolbar visibility - controlled by enableRichTextFormatting property
    // Toolbar is always visible inside the composer when enabled, regardless of text presence
    val showRichTextToolbar = enableRichTextFormatting && enabledFormats.isNotEmpty()

    // Media selection state for handling attachment options
    // Each callback uses a fixed message type based on the picker used, NOT the detected content type.
    // This matches Java reference behavior where the action type determines the message type.
    val mediaSelectionState = rememberMediaSelectionState(
        onImageSelected = { result ->
            // Image picker always sends as "image" type
            handleMediaSelectionWithType(result, "image", composerViewModel, onError)
        },
        onVideoSelected = { result ->
            // Video picker always sends as "video" type
            handleMediaSelectionWithType(result, "video", composerViewModel, onError)
        },
        onAudioSelected = { result ->
            // Audio picker always sends as "audio" type
            handleMediaSelectionWithType(result, "audio", composerViewModel, onError)
        },
        onFileSelected = { result ->
            // File picker always sends as "file" type regardless of actual MIME type
            // This matches Java reference behavior where DOCUMENT action always uses MESSAGE_TYPE_FILE
            handleMediaSelectionWithType(result, "file", composerViewModel, onError)
        },
        onCameraCapture = { result ->
            // Camera capture always sends as "image" type
            handleMediaSelectionWithType(result, "image", composerViewModel, onError)
        },
        onVideoCapture = { result ->
            // Video capture always sends as "video" type
            handleMediaSelectionWithType(result, "video", composerViewModel, onError)
        },
        onError = { exception ->
            onError?.invoke(CometChatException("MEDIA_SELECTION_ERROR", exception.message ?: "Media selection failed"))
        }
    )

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
    LaunchedEffect(editMessage, effectiveTextFormatters) {
        editMessage?.let { msg ->
            if (msg is TextMessage) {
                // Run formatter pipeline to resolve mention tokens (e.g., <@uid:userId> -> @userName)
                var formattedText: AnnotatedString = AnnotatedString(msg.text ?: "")
                for (formatter in effectiveTextFormatters) {
                    formattedText = formatter.prepareMessageString(
                        context,
                        msg,
                        formattedText,
                        UIKitConstants.MessageBubbleAlignment.RIGHT,
                        UIKitConstants.FormattingType.MESSAGE_COMPOSER
                    )
                }
                textFieldValue = TextFieldValue(formattedText.text)
            }
        }
    }

    // Handle error events
    LaunchedEffect(Unit) {
        composerViewModel.errorEvent.collect { error ->
            onError?.invoke(error)
        }
    }

    // Determine send button state — check both single-field text and segment content
    val isSendButtonActive = textFieldValue.text.isNotBlank() || segmentController.hasContent

    // Placeholder text
    val placeholder = placeholderText ?: context.getString(R.string.cometchat_composer_place_holder_text)
    
    // Span-based visual transformation: recreated whenever spans change (spanVersion)
    // so BasicTextField re-applies filter() and picks up new formatting.
    val spanBasedTransformation = remember(enabledFormats, spanVersion) {
        if (enabledFormats.isNotEmpty()) {
            SpanBasedVisualTransformation(richTextController)
        } else {
            VisualTransformation.None
        }
    }
    
    // Mention visual transformation: applies styling to tracked mentions
    // Recreated whenever mentions change to pick up new styling
    val mentionVisualTransformation = remember(mentionVersion, disableMentions, defaultMentionStyle) {
        if (!disableMentions) {
            ComposerMentionVisualTransformation(mentionInsertionState, defaultMentionStyle)
        } else {
            VisualTransformation.None
        }
    }
    
    // Combined visual transformation: applies both rich text and mention styling
    val combinedVisualTransformation = remember(spanBasedTransformation, mentionVisualTransformation) {
        CombinedVisualTransformation(listOf(spanBasedTransformation, mentionVisualTransformation))
    }

    // Effective attachment popup style with 12dp corner radius (matching Kotlin cometchat_corner_radius_3)
    // Use iconTintHighlight (primary color) for attachment icons to match Kotlin implementation
    val effectiveAttachmentPopupStyle = attachmentPopupStyle.copy(
        cornerRadius = 12.dp, 
        itemPaddingVertical = 8.dp,
        startIconTint = CometChatTheme.colorScheme.iconTintHighlight
    )



    Column(
        modifier = modifier
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
                        // Insert the mention
                        val formatter = mentionDetectionState.activeFormatter
                        if (formatter != null && mentionDetectionState.isActive) {
                            // Get the appropriate style for this suggestion
                            val mentionStyle = if (formatter is CometChatMentionsFormatter) {
                                formatter.getSpanStyleForSuggestionItem(suggestionItem)
                            } else {
                                defaultMentionStyle
                            }
                            
                            // Insert the mention using the insertion state
                            textFieldValue = mentionInsertionState.insertMention(
                                currentValue = textFieldValue,
                                mentionState = mentionDetectionState,
                                suggestionItem = suggestionItem,
                                formatter = formatter,
                                mentionStyle = mentionStyle
                            )
                            
                            // Increment mention version to trigger visual transformation update
                            mentionVersion++
                            
                            // Sync richTextController with the new text after mention insertion
                            if (enabledFormats.isNotEmpty()) {
                                richTextController.onTextChanged(
                                    textFieldValue.text,
                                    textFieldValue.selection.min,
                                    textFieldValue.selection.max
                                )
                            }
                            
                            // Update the formatter's selected list
                            formatter.setSelectedList(context, mentionInsertionState.getSelectedSuggestionItems())
                            
                            // Invoke callback
                            onMentionClick?.invoke(suggestionItem)
                            
                            // Notify text change
                            onTextChanged?.invoke(textFieldValue.text)
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
                        textFieldValue = TextFieldValue("")
                    }
                } else {
                    DefaultEditPreview(
                        message = message,
                        textFormatters = effectiveTextFormatters,
                        style = style,
                        onClose = {
                            composerViewModel.clearEditMessage()
                            textFieldValue = TextFieldValue("")
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
                        // Send the audio file with correct CometChat message type
                        composerViewModel.sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_AUDIO)
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
            } else {
            // Track actual line count of text field to determine button alignment
            // Center alignment for single line, bottom alignment for multi-line
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
                    composerViewModel.showCameraOption.collectAsState().value,
                    composerViewModel.showImageOption.collectAsState().value,
                    composerViewModel.showVideoOption.collectAsState().value,
                    composerViewModel.showAudioOption.collectAsState().value,
                    composerViewModel.showFileOption.collectAsState().value,
                    composerViewModel.showPollOption.collectAsState().value,
                    composerViewModel.showCollaborativeDocumentOption.collectAsState().value,
                    composerViewModel.showCollaborativeWhiteboardOption.collectAsState().value
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
                                if (!handled) mediaSelectionState.launchCamera()
                            }
                            CometChatMessageComposerAction.ID_IMAGE -> {
                                val handled = onImageClick?.invoke() ?: false
                                if (!handled) mediaSelectionState.launchImagePicker()
                            }
                            CometChatMessageComposerAction.ID_VIDEO -> {
                                val handled = onVideoClick?.invoke() ?: false
                                if (!handled) mediaSelectionState.launchVideoPicker()
                            }
                            CometChatMessageComposerAction.ID_AUDIO -> {
                                val handled = onAudioClick?.invoke() ?: false
                                if (!handled) mediaSelectionState.launchAudioPicker()
                            }
                            CometChatMessageComposerAction.ID_DOCUMENT -> {
                                val handled = onDocumentClick?.invoke() ?: false
                                if (!handled) mediaSelectionState.launchFilePicker()
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
                        hideAttachmentButton = hideAttachmentButton,
                        hideVoiceRecordingButton = hideVoiceRecordingButton,
                        isAttachmentPopupExpanded = showAttachmentPopup,
                        style = style,
                        onAttachmentClick = { showAttachmentPopup = !showAttachmentPopup },
                        onVoiceRecordClick = { composerViewModel.startRecordingMode() }
                    )
                }
                
                // Vertical separator after attachment button (12dp spacing from attachment - Figma: padding_3)
                if (!hideAttachmentButton) {
                    Spacer(modifier = Modifier.width(12.dp))
                    VerticalDivider(
                        modifier = Modifier
                            .height(20.dp)
                            .width(1.dp),
                        color = style.separatorColor
                    )
                }
                } // End of inner Row for center alignment
            }

            // Text input area — always shows normal editor, code block appears below when active
            // Read segmentVersion to trigger recomposition when segments change
            @Suppress("UNUSED_VARIABLE")
            val currentSegmentVersion = segmentVersion

            // Focus requester for normal editor
            val normalEditorFocusRequester = remember { FocusRequester() }
            var wasInCodeBlockMode by remember { mutableStateOf(false) }
            LaunchedEffect(segmentController.hasCodeBlocks) {
                if (wasInCodeBlockMode && !segmentController.hasCodeBlocks) {
                    // Code block was removed — merge any code text back into richTextController
                    // removeCodeSegment() merged everything into the first normal segment's controller
                    val firstNormal = segmentController.segments.filterIsInstance<ComposerSegment.Normal>().firstOrNull()
                    if (firstNormal != null) {
                        val mergedText = firstNormal.controller.state.text
                        if (mergedText != richTextController.state.text) {
                            richTextController.clear()
                            if (mergedText.isNotEmpty()) {
                                richTextController.onTextChanged(mergedText, mergedText.length, mergedText.length)
                            }
                            textFieldValue = TextFieldValue(
                                text = mergedText,
                                selection = TextRange(mergedText.length)
                            )
                        }
                    }
                    activeFormats = richTextController.state.activeFormats
                    normalEditorFocusRequester.requestFocus()
                }
                wasInCodeBlockMode = segmentController.hasCodeBlocks
            }

            // Always-present Column: normal editor + optional code block below
            val inputScrollState = rememberScrollState()
            // Flag to force-show normal editor after triple-enter exit from code block
            var forceShowNormalEditor by remember { mutableStateOf(false) }
            // Reset force flag when code blocks are removed
            LaunchedEffect(segmentController.hasCodeBlocks) {
                if (!segmentController.hasCodeBlocks) forceShowNormalEditor = false
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(inputScrollState)
                    .padding(horizontal = 4.dp)
            ) {
                // Normal text editor — shown when no code block, or when text is present with code block,
                // or when forced visible after triple-enter exit from code block
                val showNormalEditor = !segmentController.hasCodeBlocks || textFieldValue.text.isNotEmpty() || forceShowNormalEditor
                if (showNormalEditor) {
                // Request focus on normal editor when forced visible after triple-enter
                LaunchedEffect(forceShowNormalEditor) {
                    if (forceShowNormalEditor) {
                        normalEditorFocusRequester.requestFocus()
                    }
                }
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        val prevText = textFieldValue.text
                        val newText = newValue.text

                        // ── Samsung keyboard duplication guard ──
                        val guardText = lastNewlineText
                        if (guardText != null) {
                            lastNewlineText = null
                            if (newText.length > guardText.length && newText.startsWith(guardText)) {
                                val appended = newText.substring(guardText.length)
                                val lastNewlineIdx = guardText.lastIndexOf('\n')
                                if (lastNewlineIdx >= 0) {
                                    val prevLine = guardText.substring(
                                        guardText.lastIndexOf('\n', lastNewlineIdx - 1).coerceAtLeast(0).let {
                                            if (guardText[it] == '\n') it + 1 else it
                                        },
                                        lastNewlineIdx
                                    )
                                    if (appended.isNotEmpty() && prevLine.endsWith(appended)) {
                                        return@BasicTextField
                                    }
                                }
                            }
                        }

                        val newNewlineCount = newText.count { it == '\n' }
                        val prevNewlineCount = prevText.count { it == '\n' }

                        // ── Process through controller ──
                        if (enabledFormats.isNotEmpty()) {
                            richTextController.onTextChanged(
                                newValue.text,
                                newValue.selection.min,
                                newValue.selection.max
                            )
                            val controllerText = richTextController.state.text
                            val controllerSelStart = richTextController.state.selectionStart
                            val controllerSelEnd = richTextController.state.selectionEnd
                            if (controllerText != newValue.text) {
                                textFieldValue = TextFieldValue(
                                    text = controllerText,
                                    selection = TextRange(controllerSelStart, controllerSelEnd),
                                    composition = null
                                )
                            } else {
                                textFieldValue = newValue
                            }
                        } else {
                            textFieldValue = newValue
                        }

                        if (newNewlineCount > prevNewlineCount) {
                            lastNewlineText = textFieldValue.text
                        }

                        onTextChanged?.invoke(textFieldValue.text)
                        if (!disableTypingEvents) {
                            if (textFieldValue.text.isNotEmpty()) composerViewModel.startTyping()
                            else composerViewModel.endTyping()
                        }
                        
                        // ── Mention detection ──
                        if (!disableMentions && effectiveTextFormatters.isNotEmpty()) {
                            // Sync mention insertion state with current text
                            mentionInsertionState.syncWithText(textFieldValue.text)
                            
                            // Detect mention
                            val newMentionState = detectMention(
                                text = textFieldValue.text,
                                cursorPosition = textFieldValue.selection.start,
                                textFormatters = effectiveTextFormatters
                            )
                            
                            mentionDetectionState = newMentionState
                            
                            if (newMentionState.isActive) {
                                // Trigger search on the active formatter
                                newMentionState.activeFormatter?.let { formatter ->
                                    showSuggestionList = true
                                    isLoadingSuggestions = true
                                    // Trigger async search - results will be observed via LaunchedEffect
                                    formatter.search(context, newMentionState.query)
                                }
                            } else {
                                showSuggestionList = false
                                suggestionItems = emptyList()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 36.dp)
                        .focusRequester(normalEditorFocusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && showStickerKeyboard) {
                                showStickerKeyboard = false
                            }
                        },
                    textStyle = style.inputTextStyle.copy(color = style.inputTextColor),
                    cursorBrush = SolidColor(style.inputTextColor),
                    visualTransformation = combinedVisualTransformation,
                    onTextLayout = { textLayoutResult ->
                        textLayoutLineCount = textLayoutResult.lineCount
                    },
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (textFieldValue.text.isEmpty()) {
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
                } // End of showNormalEditor

                // Code block — shown below normal editor when active
                if (segmentController.hasCodeBlocks) {
                    val codeSegment = segmentController.segments.filterIsInstance<ComposerSegment.Code>().firstOrNull()
                    if (codeSegment != null) {
                        if (showNormalEditor) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        CodeSegmentTextField(
                            segment = codeSegment,
                            segmentController = segmentController,
                            style = style,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 36.dp),
                            onFocused = {
                                segmentController.setFocusedSegment(codeSegment.id)
                                activeFormats = setOf(RichTextFormat.CODE_BLOCK)
                            },
                            onTextChanged = { newText ->
                                onTextChanged?.invoke(segmentController.toPlainText())
                                if (!disableTypingEvents) {
                                    if (segmentController.hasContent || textFieldValue.text.isNotEmpty()) composerViewModel.startTyping()
                                    else composerViewModel.endTyping()
                                }
                            },
                            onTripleEnterExit = {
                                // Force normal editor visible and focus it
                                forceShowNormalEditor = true
                                activeFormats = richTextController.state.activeFormats
                            }
                        )
                    }
                }
            } // End of input Column

            // Auxiliary button (sticker, AI, voice recording - no rich text toggle)
            // Hide sticker and voice recording buttons when text is entered
            // Animated visibility for smooth show/hide transition
            val hasText = textFieldValue.text.isNotEmpty() || segmentController.hasContent
            AnimatedVisibility(
                visible = !hideAuxiliaryButton && !hasText,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                if (auxiliaryButtonView != null) {
                    auxiliaryButtonView(currentUser, currentGroup, idMap)
                } else {
                    DefaultAuxiliaryButton(
                        hideRichTextToggle = true, // Always hide - toolbar visibility is automatic
                        hideStickersButton = hideStickersButton,
                        hideAIButton = true, // AI button hidden by default
                        hideVoiceRecordingButton = hideVoiceRecordingButton,
                        style = style,
                        onStickerClick = {
                            if (!showStickerKeyboard) {
                                // Opening sticker panel — hide the software keyboard first
                                keyboardController?.hide()
                            }
                            showStickerKeyboard = !showStickerKeyboard
                        },
                        onAIClick = { showAISheet = true },
                        onVoiceRecordClick = { composerViewModel.startRecordingMode() }
                    )
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
                                richTextController = if (enabledFormats.isNotEmpty()) richTextController else null,
                                segmentController = if (segmentController.hasCodeBlocks) segmentController else null,
                                inputText = textFieldValue.text,
                                editMessage = editMessage,
                                viewModel = composerViewModel,
                                onSendButtonClick = onSendButtonClick,
                                onClear = {
                                    textFieldValue = TextFieldValue("")
                                    if (enabledFormats.isNotEmpty()) richTextController.clear()
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
                                textFormatters = effectiveTextFormatters
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
                        style = style,
                        onClick = {
                            if (isAIGenerating) {
                                composerViewModel.setAIGenerating(false)
                            } else {
                                handleSend(
                                    context = context,
                                    richTextController = if (enabledFormats.isNotEmpty()) richTextController else null,
                                    segmentController = if (segmentController.hasCodeBlocks) segmentController else null,
                                    inputText = textFieldValue.text,
                                    editMessage = editMessage,
                                    viewModel = composerViewModel,
                                    onSendButtonClick = onSendButtonClick,
                                    onClear = {
                                        textFieldValue = TextFieldValue("")
                                        if (enabledFormats.isNotEmpty()) richTextController.clear()
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
                                    textFormatters = effectiveTextFormatters
                                )
                            }
                        }
                    )
                }
            }
            } // End of input Row
            } // End of else block (normal mode)
            
            // Rich text toolbar (inside compose box, always visible when enableRichTextFormatting=true)
            // Animated visibility for smooth show/hide transition
            AnimatedVisibility(
                visible = showRichTextToolbar,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    // Separator between input and toolbar
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = style.separatorColor
                    )
                    
                    CometChatRichTextToolbar(
                        modifier = Modifier.fillMaxWidth(),
                        style = style,
                        activeFormats = activeFormats,
                        disabledFormats = if (segmentController.isTypingInCode) {
                            RichTextFormat.entries.toSet()
                        } else {
                            richTextController.state.toolbarDisabledFormats
                        },
                        enabledFormats = enabledFormats,
                        onFormatClick = { format ->
                            if (format == RichTextFormat.CODE_BLOCK) {
                                // Sync richTextController text into the segment's controller
                                // before toggling, so insertCodeBlock knows the current text
                                if (!segmentController.hasCodeBlocks) {
                                    val focused = segmentController.focusedSegment
                                    if (focused is ComposerSegment.Normal) {
                                        val currentText = richTextController.state.text
                                        val selStart = richTextController.state.selectionStart
                                        val selEnd = richTextController.state.selectionEnd
                                        focused.controller.clear()
                                        if (currentText.isNotEmpty()) {
                                            focused.controller.onTextChanged(currentText, selStart, selEnd)
                                        }
                                    }
                                }
                                segmentController.toggleCodeBlock()
                            } else if (!segmentController.isTypingInCode) {
                                // Always use richTextController for the normal text editor.
                                // The normal BasicTextField is always backed by richTextController
                                // regardless of whether code blocks exist.
                                richTextController.toggleFormat(format)
                                val controllerText = richTextController.state.text
                                // Always update textFieldValue to force recomposition.
                                // For inline formats (bold, italic, etc.) the text doesn't change
                                // but spans do — we need a new TextFieldValue instance so
                                // BasicTextField re-applies the VisualTransformation.
                                textFieldValue = TextFieldValue(
                                    text = controllerText,
                                    selection = TextRange(
                                        richTextController.state.selectionStart.coerceAtMost(controllerText.length),
                                        richTextController.state.selectionEnd.coerceAtMost(controllerText.length)
                                    )
                                )
                            }
                        },
                        onLinkClick = { showLinkDialog = true }
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

    // Link edit dialog
    if (showLinkDialog) {
        CometChatLinkEditDialog(
            style = style,
            onApply = { text, url ->
                val linkMarkdown = "[$text]($url)"
                val currentText = textFieldValue.text
                val newText = if (currentText.isNotEmpty()) {
                    "$currentText $linkMarkdown"
                } else {
                    linkMarkdown
                }
                textFieldValue = TextFieldValue(
                    text = newText,
                    selection = TextRange(newText.length)
                )
                showLinkDialog = false
            },
            onDismiss = { showLinkDialog = false }
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
 * When a [richTextController] is provided, uses it to serialize spans to markdown.
 * When code blocks are present, combines normal text markdown from [richTextController]
 * with code block content from [segmentController].
 * When mentions are present, processes the text to replace prompt text with underlying text
 * and calls handlePreMessageSend on all formatters.
 * Otherwise falls back to sending the raw input text.
 */
private fun handleSend(
    context: Context,
    richTextController: RichTextEditorController? = null,
    segmentController: SegmentComposerController? = null,
    inputText: String,
    editMessage: TextMessage?,
    viewModel: CometChatMessageComposerViewModel,
    onSendButtonClick: ((Context, BaseMessage) -> Unit)?,
    onClear: () -> Unit,
    mentionInsertionState: com.cometchat.uikit.compose.presentation.shared.mentions.ComposeMentionInsertionState? = null,
    textFormatters: List<CometChatTextFormatter> = emptyList()
) {
    android.util.Log.d("MessageComposer", "handleSend: inputText='$inputText', length=${inputText.length}")
    android.util.Log.d("MessageComposer", "handleSend: richTextController=${richTextController != null}, segmentController=${segmentController != null}")
    android.util.Log.d("MessageComposer", "handleSend: mentionInsertionState=${mentionInsertionState != null}, mentionCount=${mentionInsertionState?.getMentionsManager()?.getMentions()?.size ?: 0}")
    
    // First, process mentions on the input text to get the text with underlying mention format
    var textToProcess = inputText
    if (mentionInsertionState != null) {
        textToProcess = mentionInsertionState.getProcessedText(inputText)
        android.util.Log.d("MessageComposer", "handleSend: textToProcess after mention processing='$textToProcess', length=${textToProcess.length}")
    }
    
    // When code blocks are present, the normal text lives in richTextController
    // (not in the segment's own controller), so we build markdown manually.
    var markdownText = when {
        segmentController != null && segmentController.hasCodeBlocks -> {
            val parts = mutableListOf<String>()
            // Normal text - use the mention-processed text
            val normalMd = textToProcess.trim()
            if (normalMd.isNotEmpty()) parts.add(normalMd)
            // Code blocks from segments
            for (seg in segmentController.segments) {
                if (seg is ComposerSegment.Code) {
                    val code = seg.text.trim()
                    if (code.isNotEmpty()) {
                        parts.add("```${seg.language}\n$code\n```")
                    }
                }
            }
            parts.joinToString("\n")
        }
        richTextController != null -> {
            // Update the controller with the mention-processed text, then convert to markdown
            richTextController.onTextChanged(textToProcess, textToProcess.length, textToProcess.length)
            richTextController.toMarkdown()
        }
        else -> textToProcess
    }

    android.util.Log.d("MessageComposer", "handleSend: final markdownText='$markdownText'")

    if (markdownText.isBlank()) return

    if (onSendButtonClick != null) {
        viewModel.createTextMessage(markdownText)?.let { message ->
            // Call handlePreMessageSend on all formatters to attach mentioned users
            textFormatters.forEach { formatter ->
                formatter.handlePreMessageSend(context, message)
            }
            onSendButtonClick(context, message)
        }
    } else {
        if (editMessage != null) {
            viewModel.editMessage(markdownText)
        } else {
            // Create message first to call handlePreMessageSend
            val message = viewModel.createTextMessage(markdownText)
            if (message != null) {
                // Call handlePreMessageSend on all formatters to attach mentioned users
                textFormatters.forEach { formatter ->
                    formatter.handlePreMessageSend(context, message)
                }
                viewModel.sendTextMessageWithMentions(message)
            } else {
                viewModel.sendTextMessage(markdownText)
            }
        }
    }
    onClear()
}

/**
 * Renders a code block segment as a dark-background monospace BasicTextField.
 * No rich text formatting — plain text only.
 *
 * Handles:
 * - Triple-enter exit: calls [segmentController.handleCodeTextChanged] which detects \n\n\n
 * - Backspace-on-empty: calls [segmentController.handleBackspaceOnEmptyCodeBlock]
 * - FocusRequester with pending focus consumption
 */
@Composable
private fun CodeSegmentTextField(
    segment: ComposerSegment.Code,
    segmentController: SegmentComposerController,
    style: CometChatMessageComposerStyle,
    modifier: Modifier = Modifier,
    onFocused: () -> Unit,
    onTextChanged: (String) -> Unit,
    onTripleEnterExit: () -> Unit = {}
) {
    var tfv by remember(segment.id) {
        mutableStateOf(TextFieldValue(segment.text))
    }
    val focusRequester = remember { FocusRequester() }

    // Pending focus consumption
    val isPendingFocus = segmentController.pendingFocusSegmentId == segment.id
    LaunchedEffect(isPendingFocus) {
        if (isPendingFocus) {
            focusRequester.requestFocus()
            segmentController.consumePendingFocus()
        }
    }

    // Sync tfv when segment text changes externally (e.g., triple-enter trim)
    LaunchedEffect(segment.text) {
        if (tfv.text != segment.text) {
            tfv = TextFieldValue(segment.text, TextRange(segment.text.length))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .border(1.dp, Color(0xFF3C3C3C), RoundedCornerShape(8.dp))
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

                // Route through segment controller for triple-enter detection
                val exited = segmentController.handleCodeTextChanged(segment, newText)
                if (exited) {
                    // Triple-enter detected — text was trimmed, keep code block but
                    // focus the normal editor. The wasInCodeBlockMode LaunchedEffect
                    // won't fire since hasCodeBlocks is still true, so we manually
                    // reset toolbar and request focus via onTripleEnterExit callback.
                    onTripleEnterExit()
                    return@BasicTextField
                }

                onTextChanged(newText)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
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
                color = Color(0xFFD4D4D4),
                fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(Color(0xFFD4D4D4)),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (tfv.text.isEmpty()) {
                        Text(
                            text = "Enter code...",
                            color = Color(0xFF666666),
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
 * Span-based visual transformation that renders WYSIWYG rich text.
 * Reads formatting spans from the controller at render time (not captured at creation).
 * This avoids stale span data when recomposition timing causes the transformation
 * to be evaluated before the controller state is fully consistent.
 */
private class SpanBasedVisualTransformation(
    private val controller: RichTextEditorController,
    private val inputTextColor: Color = Color.Unspecified
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val currentSpans = controller.state.spans
        val rawText = text.text

        // Build display string replacing line prefixes with visual equivalents.
        // All replacements are same-length so OffsetMapping.Identity still works.
        //   "- "  →  "• "   (bullet dot)
        //   "> "  →  "┃ "   (vertical bar for blockquote)
        val displayText = buildString {
            val lines = rawText.split("\n")
            for ((i, line) in lines.withIndex()) {
                when {
                    line.startsWith("- ") -> {
                        append("• ")
                        append(line.substring(2))
                    }
                    line.startsWith("> ") -> {
                        append("┃ ")
                        append(line.substring(2))
                    }
                    else -> append(line)
                }
                if (i < lines.size - 1) append("\n")
            }
        }

        val styled = buildAnnotatedString {
            append(displayText)

            // Apply inline format spans — combine TextDecorations per span
            for (span in currentSpans) {
                val start = span.start.coerceAtMost(displayText.length)
                val end = span.end.coerceAtMost(displayText.length)
                if (start >= end) continue

                val decorations = mutableListOf<TextDecoration>()
                for (format in span.formats) {
                    when (format) {
                        RichTextFormat.UNDERLINE -> decorations.add(TextDecoration.Underline)
                        RichTextFormat.STRIKETHROUGH -> decorations.add(TextDecoration.LineThrough)
                        RichTextFormat.LINK -> decorations.add(TextDecoration.Underline)
                        else -> {
                            val s = formatToSpanStyle(format)
                            if (s != null) addStyle(s, start, end)
                        }
                    }
                }
                if (decorations.isNotEmpty()) {
                    addStyle(SpanStyle(textDecoration = TextDecoration.combine(decorations)), start, end)
                }
                if (RichTextFormat.LINK in span.formats) {
                    addStyle(SpanStyle(color = Color(0xFF1A73E8)), start, end)
                }
            }

            // Style line-based prefixes
            styleLinePrefixes(rawText)
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }

    private fun formatToSpanStyle(format: RichTextFormat): SpanStyle? = when (format) {
        RichTextFormat.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
        RichTextFormat.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
        RichTextFormat.INLINE_CODE -> SpanStyle(
            fontFamily = FontFamily.Monospace,
            background = Color.LightGray.copy(alpha = 0.3f)
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
                // Bullet list: "- " → displayed as "• " — style the bullet bold with text color
                line.startsWith("- ") -> {
                    addStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = inputTextColor
                    ), idx, idx + 2)
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
                // Blockquote: "> " → displayed as "┃ " — style bar bold with text color,
                // content in gray (no italic — keep user's own formatting intact)
                line.startsWith("> ") -> {
                    addStyle(SpanStyle(
                        color = inputTextColor,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal
                    ), idx, idx + 1)
                    if (line.length > 2) {
                        addStyle(SpanStyle(
                            color = Color(0xFF666666)
                        ), idx + 2, idx + line.length)
                    }
                }
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

