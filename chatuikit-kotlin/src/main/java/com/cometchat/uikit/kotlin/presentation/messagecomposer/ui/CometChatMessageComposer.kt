package com.cometchat.uikit.kotlin.presentation.messagecomposer.ui

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CardMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import android.net.Uri
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.models.AttachmentSource
import com.cometchat.uikit.core.models.StagedAttachmentInput
import com.cometchat.uikit.core.models.defaultAttachmentCategory
import com.cometchat.uikit.core.utils.AgentChatDetector
import com.cometchat.uikit.core.utils.extractMediaDurationMillis
import com.cometchat.uikit.core.factory.CometChatMessageComposerViewModelFactory
import com.cometchat.uikit.core.formatter.RichTextConfiguration
import com.cometchat.uikit.core.formatter.RichTextEditorController
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.core.formatter.RichTextFormatterManager
import com.cometchat.uikit.kotlin.shared.spans.BlockquoteFormatSpan
import com.cometchat.uikit.kotlin.shared.spans.BulletListFormatSpan
import com.cometchat.uikit.kotlin.shared.spans.CodeBlockFormatSpan
import com.cometchat.uikit.kotlin.shared.spans.FormatSpanWatcher
import com.cometchat.uikit.kotlin.shared.spans.InlineCodeFormatSpan
import com.cometchat.uikit.kotlin.shared.spans.LinkFormatSpan
import com.cometchat.uikit.kotlin.shared.spans.ListContinuationHandler
import com.cometchat.uikit.kotlin.shared.spans.MarkdownConverter
import com.cometchat.uikit.kotlin.shared.spans.MentionCodeBlockHandler
import com.cometchat.uikit.kotlin.shared.spans.NonEditableSpan
import com.cometchat.uikit.kotlin.shared.spans.NumberedListFormatSpan
import com.cometchat.uikit.kotlin.shared.spans.RichTextFormatSpan
import com.cometchat.uikit.kotlin.shared.spans.RichTextSpanManager
import com.cometchat.uikit.core.state.MessageComposerUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import com.cometchat.uikit.core.domain.model.CometChatMessageComposerAction
import com.cometchat.uikit.core.domain.model.ComposerLayoutMode
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatMessageComposerBinding
import com.cometchat.uikit.kotlin.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.kotlin.presentation.messagecomposer.utils.MessageComposerViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment.MultiAttachmentUtils
import com.cometchat.uikit.kotlin.presentation.polls.ui.CometChatCreatePoll
import com.cometchat.uikit.kotlin.presentation.shared.mediarecorder.CometChatMediaRecorder
import com.cometchat.uikit.kotlin.presentation.shared.snackbar.CometChatSnackBar
import com.cometchat.uikit.kotlin.presentation.shared.mediarecorder.MediaRecorderCallback
import com.cometchat.uikit.kotlin.presentation.shared.inlineaudiorecorder.CometChatInlineAudioRecorder
import com.cometchat.uikit.kotlin.presentation.shared.inlineaudiorecorder.InlineAudioRecorderManager
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.PopupPosition
import com.cometchat.uikit.kotlin.presentation.shared.permission.CometChatPermissionHandler
import com.cometchat.uikit.kotlin.presentation.shared.permission.PermissionType
import com.cometchat.uikit.kotlin.presentation.shared.permission.listener.ActivityResultListener
import com.cometchat.uikit.kotlin.presentation.shared.permission.listener.PermissionResultListener
import com.cometchat.uikit.kotlin.presentation.shared.suggestionlist.CometChatSuggestionList
import com.cometchat.uikit.kotlin.presentation.shared.suggestionlist.CometChatSuggestionListStyle
import com.cometchat.uikit.kotlin.presentation.stickerkeyboard.ui.CometChatStickerKeyboard
import com.cometchat.uikit.kotlin.presentation.stickerkeyboard.style.CometChatStickerKeyboardStyle
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.kotlin.shared.formatters.CometChatMentionsFormatter
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.SuggestionItem
import com.cometchat.uikit.kotlin.shared.mentions.MentionTextWatcher
import com.cometchat.uikit.kotlin.shared.mentions.MessageComposerMentionHelper
import com.cometchat.uikit.kotlin.shared.resources.utils.AnimationUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.MediaUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.shared.resources.utils.itemclicklistener.OnItemClickListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * CometChatMessageComposer is a custom Android View for composing and sending messages.
 * Provides text input, media attachments, voice recording, rich text formatting, and AI integration.
 * 
 * This component uses the shared CometChatMessageComposerViewModel from chatuikit-core,
 * ensuring consistent business logic with the Jetpack Compose version.
 * 
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.messagecomposer.ui.CometChatMessageComposer
 *     android:id="@+id/messageComposer"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:cometchatMessageComposerStyle="@style/CometChatMessageComposer" />
 * ```
 * 
 * Usage in Kotlin:
 * ```kotlin
 * val messageComposer = CometChatMessageComposer(context)
 * messageComposer.setUser(user)
 * messageComposer.setOnSendButtonClick { text -> /* Handle send */ }
 * ```
 */
class CometChatMessageComposer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatMessageComposerStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatMessageComposer::class.java.simpleName
        
        // Observer type constants for formatter observer tracking
        private const val OBSERVER_SUGGESTION_LIST = "suggestion_list"
        private const val OBSERVER_LOADING = "loading"
        private const val OBSERVER_TAG_INFO = "tag_info"
        private const val OBSERVER_TAG_VISIBILITY = "tag_visibility"

        /** Debounce window (ms) to ignore button clicks that arrive after an outside-touch dismiss */
        private const val DISMISS_DEBOUNCE_MS = 200L

        /**
         * Rich content the composer input accepts besides typed text: pasted/keyboard-inserted
         * images & GIFs, and any media dropped or pasted as a content uri (video, audio,
         * documents). Advertised to the IME via AppCompatEditText's receive-content integration.
         */
        private val RECEIVE_CONTENT_MIME_TYPES = arrayOf("image/*", "video/*", "audio/*", "application/*")
    }

    // View Binding
    private val binding: CometchatMessageComposerBinding = CometchatMessageComposerBinding.inflate(
        LayoutInflater.from(context), this
    )

    // ViewModel (shared from chatuikit-core)
    private var viewModel: CometChatMessageComposerViewModel? = null
    private var isExternalViewModel: Boolean = false

    // Coroutine scope for collecting flows
    private var viewScope: CoroutineScope? = null

    // Data
    private var user: User? = null
    private var group: Group? = null

    /**
     * When true, no typing indicator events are sent while the user types.
     */
    var disableTypingEvents: Boolean = false
        set(value) {
            field = value
            viewModel?.disableTypingEvents = value
        }

    // Rich text formatting - using RichTextEditorController from chatuikit-core (same as Jetpack)
    private val richTextController = RichTextEditorController()
    private var richTextFormatterManager: RichTextFormatterManager? = null
    private var richTextConfiguration: RichTextConfiguration = RichTextConfiguration()
    private var richTextToolbarVisibility: Int = View.GONE
    private var isApplyingRichTextStyling: Boolean = false

    // Span-based WYSIWYG formatting engine (V5-style, replaces marker-based approach)
    private var formatSpanWatcher: FormatSpanWatcher? = null
    
    // Track active and disabled formats for toolbar state
    private var activeFormats: Set<RichTextFormat> = emptySet()
    private var disabledFormats: Set<RichTextFormat> = emptySet()

    // Text selection menu and rich text formatting flags (Requirements 19.1–19.8)
    private var enableRichTextFormatting: Boolean = true
    private var showTextSelectionMenuItems: Boolean = true

    // Mention suppression inside code blocks (Requirement 14.3)
    private var suppressMentionDetection = false

    // Flag to track when text is being modified by a TextWatcher cycle.
    // Prevents the selection listener from clearing pending formats during typing.
    private var isTextChanging = false

    // Tracks the last cursor position set by a text change, so we can distinguish
    // user-initiated cursor moves from typing-induced cursor moves in onSelectionChanged.
    private var lastCursorAfterTextChange = -1

    // Callbacks
    private var onSendButtonClick: ((String) -> Unit)? = null
    private var onAttachmentClick: (() -> Unit)? = null
    /**
     * Callback invoked when the camera attachment option is clicked.
     * @return `true` to skip default camera launch behavior (developer handles it),
     *         `false` or `null` to execute default camera launch behavior.
     */
    private var onCameraClick: (() -> Boolean)? = null
    /**
     * Callback invoked when the image attachment option is clicked.
     * @return `true` to skip default image picker behavior (developer handles it),
     *         `false` or `null` to execute default image picker behavior.
     */
    private var onImageClick: (() -> Boolean)? = null
    /**
     * Callback invoked when the video attachment option is clicked.
     * @return `true` to skip default video picker behavior (developer handles it),
     *         `false` or `null` to execute default video picker behavior.
     */
    private var onVideoClick: (() -> Boolean)? = null
    /**
     * Callback invoked when the audio attachment option is clicked.
     * @return `true` to skip default audio picker behavior (developer handles it),
     *         `false` or `null` to execute default audio picker behavior.
     */
    private var onAudioClick: (() -> Boolean)? = null
    /**
     * Callback invoked when the document attachment option is clicked.
     * @return `true` to skip default document picker behavior (developer handles it),
     *         `false` or `null` to execute default document picker behavior.
     */
    private var onDocumentClick: (() -> Boolean)? = null
    /**
     * Callback invoked when the Poll attachment option is clicked.
     * @return `true` to skip default poll creation behavior (developer handles it),
     *         `false` or `null` to execute default poll creation behavior.
     */
    private var onPollClick: (() -> Boolean)? = null
    /**
     * Callback invoked when the Collaborative Document attachment option is clicked.
     * @return `true` to skip default collaborative document creation behavior (developer handles it),
     *         `false` or `null` to execute default collaborative document creation behavior.
     */
    private var onCollaborativeDocumentClick: (() -> Boolean)? = null
    /**
     * Callback invoked when the Collaborative Whiteboard attachment option is clicked.
     * @return `true` to skip default collaborative whiteboard creation behavior (developer handles it),
     *         `false` or `null` to execute default collaborative whiteboard creation behavior.
     */
    private var onCollaborativeWhiteboardClick: (() -> Boolean)? = null
    /**
     * Callback invoked when a custom attachment option is clicked.
     * Custom options are added via [setAttachmentOptions] or [addAttachmentOption].
     */
    private var onAttachmentOptionClick: ((CometChatMessageComposerAction) -> Unit)? = null
    private var onVoiceRecordingClick: (() -> Unit)? = null
    private var onAIClick: (() -> Unit)? = null
    private var onStickerClick: (() -> Unit)? = null
    private var onError: ((CometChatException) -> Unit)? = null
    private var onTextChanged: ((String) -> Unit)? = null

    // Custom view listeners
    private var headerViewListener: MessageComposerViewHolderListener? = null
    private var footerViewListener: MessageComposerViewHolderListener? = null
    private var secondaryButtonViewListener: MessageComposerViewHolderListener? = null
    private var sendButtonViewListener: MessageComposerViewHolderListener? = null
    private var auxiliaryButtonViewListener: MessageComposerViewHolderListener? = null

    // Agent chat detection
    private var isAgentChat: Boolean = false

    // Reply mode state
    private var replyingToMessageId: Long? = null

    // Visibility controls
    private var hideAttachmentButton: Boolean = false
    private var hideVoiceRecordingButton: Boolean = false

    /**
     * When `true` (default), picking attachments stages them in a horizontal tray and uploads them
     * up front; the send button is gated until **all** staged attachments finish uploading, and a
     * single multi-attachment message is sent on tap. When `false`, every attachment option reverts
     * to the legacy single-pick, send-immediately behavior (no tray, no multi-upload).
     */
    private var enableMultipleAttachments: Boolean = true

    private var attachmentTileAdapter: CometChatAttachmentTileAdapter? = null
    private var hideAIButton: Boolean = true
    private var hideStickerButton: Boolean = true
    private var hideEditPreview: Boolean = false
    private var hideMessagePreview: Boolean = false

    // Multiline mode — two-row layout with text input in Row 1 and buttons in Row 2
    private var composerLayoutMode: ComposerLayoutMode = ComposerLayoutMode.SINGLE_LINE
    private var isFormattingToolbarVisible: Boolean = false

    // Multiline toolbar button tracking for state updates
    private val multilineToolbarButtons = mutableListOf<Pair<android.widget.ImageView, MaterialCardView>>()
    private val multilineToolbarFormatMap = mutableMapOf<RichTextFormat, Pair<android.widget.ImageView, MaterialCardView>>()

    // Attachment popup
    private var attachmentPopup: CometChatPopupMenu? = null
    private var isAttachmentPopupOpen: Boolean = false
    private var lastAttachmentDismissTime: Long = 0L

    // Inline media recorder
    private var inlineMediaRecorder: CometChatMediaRecorder? = null
    private var inlineAudioRecorder: CometChatInlineAudioRecorder? = null
    private var isInRecordingMode: Boolean = false

    // Animation tracking to prevent vibration during fast typing
    private var voiceRecordingAnimation: android.view.animation.Animation? = null
    private var stickerAnimation: android.view.animation.Animation? = null
    private var toolbarAnimation: android.view.animation.Animation? = null
    private var isVoiceRecordingAnimating: Boolean = false
    private var isStickerAnimating: Boolean = false
    private var isToolbarAnimating: Boolean = false

    // Sticker keyboard
    private var stickerKeyboard: CometChatStickerKeyboard? = null
    private var isStickerKeyboardVisible: Boolean = false
    private var stickerKeyboardStyle: CometChatStickerKeyboardStyle? = null
    private var onStickerSelected: ((Sticker) -> Unit)? = null

    // Mention support
    private var textFormatters: MutableList<CometChatTextFormatter> = mutableListOf()
    private var cometchatMentionsFormatter: CometChatMentionsFormatter? = null
    private var mentionHelper: MessageComposerMentionHelper? = null
    private var suggestionList: CometChatSuggestionList? = null
    private var suggestionListStyle: CometChatSuggestionListStyle? = null
    @StyleRes private var mentionTextStyle: Int = 0
    private var currentMentionDetectionResult: MentionTextWatcher.MentionDetectionResult? = null
    
    // Map to store observers per formatter to properly track and remove them
    private val formatterObserversMap = mutableMapOf<Pair<CometChatTextFormatter, String>, Observer<*>>()
    
    // Search query debouncing timer (like Java's queryTimer)
    private var searchQueryTimer: java.util.Timer? = null
    private val searchQueryInterval: Long = 300L // milliseconds
    
    // Flag to track if we're waiting for fresh search results (ignore stale LiveData)
    private var isWaitingForFreshResults: Boolean = false

    // Placeholder text
    private var placeholderText: String = ""

    // Single style object - NO individual style properties
    private var style: CometChatMessageComposerStyle = CometChatMessageComposerStyle()

    init {
        Utils.initMaterialCard(this)
        applyStyleAttributes(attrs, defStyleAttr)
        setupClickListeners()
        setupTextWatcher()
        setupReceiveContent()
        setupAttachmentTray()
        initViewModel()
        initRichTextFormatter()
        setupTextSelectionMenu()
        initSuggestionList()
        // Initialize default mentions formatter (like Java implementation)
        processMentionsFormatter()
        android.util.Log.d(TAG, "init: initialization complete, textFormatters.size=${textFormatters.size}")
    }

    /**
     * Enables or disables the multi-attachment staging flow. When disabled, attachment picks revert
     * to the legacy single-pick, send-immediately behavior. Defaults to `true`.
     */
    fun setEnableMultipleAttachments(enable: Boolean) {
        enableMultipleAttachments = enable
        if (!enable) {
            viewModel?.clearAttachments()
            updateAttachmentTray(emptyList())
        }
    }

    /**
     * Gate A of the attachment count limit: when the tray already holds the maximum number of
     * attachments, shows the limit toast and reports `false` so the caller skips opening the
     * picker. Picks that exceed the remaining slots anyway (multi-select has no OS-level count
     * cap on intent pickers) are trimmed by `stageAttachments` (Gate B).
     */
    private fun canOpenAttachmentPicker(): Boolean {
        if (!enableMultipleAttachments) return true
        val vm = viewModel ?: return true
        val maxCount = vm.maxAttachmentCount
        if (vm.attachmentTiles.value.size + pendingStagingCount < maxCount) return true
        showAttachmentLimitToast(maxCount)
        return false
    }

    private fun showAttachmentLimitToast(maxCount: Int) {
        android.widget.Toast.makeText(
            context,
            context.getString(R.string.cometchat_attachment_count_exceeded, maxCount),
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Surfaces why a rejected staged attachment couldn't be uploaded (shown when its tile is
     * tapped), via a themed [CometChatSnackBar]. The reason is taken verbatim from the
     * SDK-provided error — e.g. the size-limit rejection already carries the actual per-file
     * limit — so the UIKit never recomputes or hardcodes the limit. Falls back to a generic
     * message only when the SDK gives no reason.
     */
    private fun showAttachmentErrorSnackbar(tile: com.cometchat.uikit.core.models.AttachmentUploadTile) {
        val message = tile.error?.message?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.cometchat_attachment_upload_failed)
        CometChatSnackBar.show(binding.root, message)
    }

    /**
     * Wires up the horizontal attachment tray: a [CometChatAttachmentTileAdapter] whose per-tile
     * intents forward to the shared ViewModel (cancel / remove / retry / preview).
     */
    private fun setupAttachmentTray() {
        val adapter = CometChatAttachmentTileAdapter(
            onCancel = { viewModel?.removeAttachment(it) },
            onRemove = { viewModel?.removeAttachment(it) },
            onRetry = { viewModel?.retryAttachment(it) },
            onClick = { openStagedAttachmentPreview(it) },
            onRejected = { showAttachmentErrorSnackbar(it) }
        )
        attachmentTileAdapter = adapter
        binding.attachmentTrayRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.attachmentTrayRecyclerView.adapter = adapter
    }

    /**
     * Opens a fullscreen preview for a successfully-uploaded tray tile: images in the in-app
     * [CometChatImageViewerActivity], videos in an external player (the staged local copy via
     * FileProvider, falling back to the uploaded URL). Audio plays inline on its tile's play
     * button and file tiles have no preview, so both are ignored here.
     */
    private fun openStagedAttachmentPreview(tile: com.cometchat.uikit.core.models.AttachmentUploadTile) {
        try {
            when (tile.category) {
                CometChatConstants.MESSAGE_TYPE_IMAGE -> {
                    val model = tile.localUri ?: tile.attachment?.fileUrl ?: return
                    context.startActivity(
                        com.cometchat.uikit.kotlin.presentation.shared.mediaviewer.CometChatImageViewerActivity.createIntent(
                            context, listOf(model), listOf(tile.mimeType), listOf(tile.name)
                        )
                    )
                }

                CometChatConstants.MESSAGE_TYPE_VIDEO -> {
                    val localFile = tile.localUri?.takeIf { it.startsWith("/") }?.let(::File)
                    if (localFile?.exists() == true) {
                        MediaUtils.openFile(context, localFile)
                    } else {
                        MediaUtils.openMediaInPlayer(
                            context,
                            tile.localUri ?: tile.attachment?.fileUrl,
                            tile.mimeType
                        )
                    }
                }

                else -> Unit
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to open staged attachment preview: ${e.message}")
        }
    }

    /** Reflects the staged-attachment list onto the tray and dependent button states. */
    private fun updateAttachmentTray(tiles: List<com.cometchat.uikit.core.models.AttachmentUploadTile>) {
        val show = enableMultipleAttachments && tiles.isNotEmpty()
        binding.attachmentTrayRecyclerView.visibility = if (show) View.VISIBLE else View.GONE
        attachmentTileAdapter?.submitList(tiles)
        updateSendButtonState(binding.etMessageInput.text?.isNotBlank() ?: false)
        updateButtonVisibility()
        updateMultilineRow2Visibility()
    }

    /** True while ≥1 attachment is staged (and multi-attachment mode is on). */
    private fun hasStagedAttachments(): Boolean =
        enableMultipleAttachments && (viewModel?.attachmentTiles?.value?.isNotEmpty() == true)

    /**
     * Initializes the default mentions formatter.
     * This creates a CometChatMentionsFormatter and adds it to the textFormatters list,
     * similar to how the Java implementation auto-initializes formatters.
     */
    private fun processMentionsFormatter() {
        android.util.Log.d(TAG, "processMentionsFormatter: creating default CometChatMentionsFormatter")
        cometchatMentionsFormatter = CometChatMentionsFormatter(context)
        
        // Apply mention text style
        val styleToApply = if (mentionTextStyle != 0) {
            mentionTextStyle
        } else {
            R.style.CometChatMessageComposerMentionsStyle
        }
        cometchatMentionsFormatter?.setMessageComposerMentionTextStyle(context, styleToApply)
        
        // Add to formatters list
        cometchatMentionsFormatter?.let { formatter ->
            textFormatters.add(formatter)
            android.util.Log.d(TAG, "processMentionsFormatter: added formatter, textFormatters.size=${textFormatters.size}")
        }
        
        // Initialize mention helper with the formatter
        initMentionHelper()
    }

    /**
     * Initializes the suggestion list for mentions.
     * Applies the suggestion list style from the composer style or uses theme defaults.
     */
    private fun initSuggestionList() {
        suggestionList = CometChatSuggestionList(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setMaxHeightLimit(resources.getDimensionPixelSize(R.dimen.cometchat_200dp))
            
            // Apply suggestion list style: use custom style if set, otherwise use theme default
            val styleToApply = suggestionListStyle ?: CometChatSuggestionListStyle.default(context)
            setStyle(styleToApply)
            
            setItemClickListener(object : OnItemClickListener<SuggestionItem> {
                override fun OnItemClick(item: SuggestionItem, position: Int) {
                    handleSuggestionItemClick(item)
                }
                
                override fun OnItemLongClick(item: SuggestionItem, position: Int) {
                    // No long click action needed
                }
            })
            
            setOnScrollToBottomListener {
                currentMentionDetectionResult?.formatter?.onScrollToBottom()
            }
        }
        binding.suggestionListLayout.addView(suggestionList)
    }

    /**
     * Initializes the mention helper with text formatters.
     * Should be called after text formatters are set.
     */
    private fun initMentionHelper() {
        android.util.Log.d(TAG, "initMentionHelper: textFormatters.size=${textFormatters.size}")
        if (textFormatters.isEmpty()) {
            android.util.Log.d(TAG, "initMentionHelper: no formatters, returning")
            return
        }
        
        // Clean up existing helper
        mentionHelper?.cleanup()
        
        mentionHelper = MessageComposerMentionHelper(
            editText = binding.etMessageInput,
            textFormatters = textFormatters
        )
        
        mentionHelper?.initialize { result ->
            currentMentionDetectionResult = result
            handleMentionDetection(result)
        }
        
        // Set up formatter observers
        setupFormatterObservers()
    }

    /**
     * Sets up LiveData observers for text formatters.
     * Each formatter gets its own set of observers stored in the maps.
     * 
     * Matches Java's setTagList behavior:
     * - Only show list if tempTextFormatter != null (isInMentionContext) AND items not empty
     * - Hide list when items empty or not in mention context
     */
    private fun setupFormatterObservers() {
        // Remove existing observers
        removeFormatterObservers()
        
        for (formatter in textFormatters) {
            // Observe suggestion item list - matches Java's setTagList method exactly
            val itemListObserver = Observer<List<SuggestionItem>> { items ->
                android.util.Log.d("MentionDebug", "[$TAG] Observer received ${items.size} items from formatter: ${items.map { it.name }}")
                
                // Match Java: only process if tempTextFormatter != null (we're in active mention context)
                // AND the current formatter matches this observer's formatter
                val isInMentionContext = currentMentionDetectionResult?.isActive == true
                val isCurrentFormatter = currentMentionDetectionResult?.formatter == formatter
                
                android.util.Log.d("MentionDebug", "[$TAG] Observer - isInMentionContext=$isInMentionContext, isCurrentFormatter=$isCurrentFormatter, items.isNotEmpty=${items.isNotEmpty()}")
                
                // Match Java's setTagList: show only if in context AND current formatter AND items not empty
                // Otherwise HIDE (this fixes the "amannn" showing 4 users issue)
                if (isInMentionContext && isCurrentFormatter && items.isNotEmpty()) {
                    android.util.Log.d("MentionDebug", "[$TAG] Observer - SHOWING suggestion list with ${items.size} items")
                    binding.suggestionListLayout.visibility = View.VISIBLE
                    suggestionList?.setList(items)
                } else {
                    android.util.Log.d("MentionDebug", "[$TAG] Observer - HIDING suggestion list (context=$isInMentionContext, formatter=$isCurrentFormatter, empty=${items.isEmpty()})")
                    // Hide when: not in mention context OR items empty
                    binding.suggestionListLayout.visibility = View.GONE
                }
            }
            formatter.getSuggestionItemList().observeForever(itemListObserver)
            formatterObserversMap[formatter to OBSERVER_SUGGESTION_LIST] = itemListObserver
            
            // Observe loading indicator - matches Java's setLoadingStateVisibility
            val loadingObserver = Observer<Boolean> { show ->
                android.util.Log.d("MentionDebug", "[$TAG] Loading observer - show=$show")
                // Only show shimmer if this formatter is the current active one
                val isCurrentFormatter = currentMentionDetectionResult?.formatter == formatter
                if (show && isCurrentFormatter) {
                    android.util.Log.d("MentionDebug", "[$TAG] Loading observer - SHOWING shimmer")
                    // Show shimmer and make visible
                    binding.suggestionListLayout.visibility = View.VISIBLE
                    suggestionList?.showShimmer(true)
                } else if (!show) {
                    android.util.Log.d("MentionDebug", "[$TAG] Loading observer - HIDING shimmer")
                    suggestionList?.showShimmer(false)
                }
            }
            formatter.getShowLoadingIndicator().observeForever(loadingObserver)
            formatterObserversMap[formatter to OBSERVER_LOADING] = loadingObserver
            
            // Observe tag info message
            val tagInfoObserver = Observer<String> { message ->
                binding.tvInfoText.text = message
            }
            formatter.getTagInfoMessage().observeForever(tagInfoObserver)
            formatterObserversMap[formatter to OBSERVER_TAG_INFO] = tagInfoObserver
            
            // Observe tag info visibility
            val tagVisibilityObserver = Observer<Boolean> { visible ->
                binding.infoPanelCard.visibility = if (visible) View.VISIBLE else View.GONE
            }
            formatter.getTagInfoVisibility().observeForever(tagVisibilityObserver)
            formatterObserversMap[formatter to OBSERVER_TAG_VISIBILITY] = tagVisibilityObserver
        }
    }

    /**
     * Removes LiveData observers from text formatters.
     */
    @Suppress("UNCHECKED_CAST")
    private fun removeFormatterObservers() {
        for (formatter in textFormatters) {
            // Remove suggestion list observer
            (formatterObserversMap[formatter to OBSERVER_SUGGESTION_LIST] as? Observer<List<SuggestionItem>>)?.let {
                formatter.getSuggestionItemList().removeObserver(it)
            }
            // Remove loading observer
            (formatterObserversMap[formatter to OBSERVER_LOADING] as? Observer<Boolean>)?.let {
                formatter.getShowLoadingIndicator().removeObserver(it)
            }
            // Remove tag info observer
            (formatterObserversMap[formatter to OBSERVER_TAG_INFO] as? Observer<String>)?.let {
                formatter.getTagInfoMessage().removeObserver(it)
            }
            // Remove tag visibility observer
            (formatterObserversMap[formatter to OBSERVER_TAG_VISIBILITY] as? Observer<Boolean>)?.let {
                formatter.getTagInfoVisibility().removeObserver(it)
            }
        }
        formatterObserversMap.clear()
    }

    /**
     * Handles mention detection results.
     * Matches Java's onSelectionChanged behavior:
     * - When active: trigger search with debouncing
     * - When inactive: hide suggestion list
     */
    private fun handleMentionDetection(result: MentionTextWatcher.MentionDetectionResult) {
        android.util.Log.d("MentionDebug", "[$TAG] handleMentionDetection() - isActive=${result.isActive}, query='${result.query}', formatter=${result.formatter?.javaClass?.simpleName}")
        
        // Update mention suppression flag based on current cursor position
        val editable = binding.etMessageInput.text
        val cursorPos = binding.etMessageInput.selectionStart
        suppressMentionDetection = if (editable != null && cursorPos >= 0) {
            isInsideCodeFormat(editable, cursorPos)
        } else {
            false
        }

        // Suppress mention detection when cursor is inside a code block or inline code span
        if (suppressMentionDetection && result.isActive) {
            android.util.Log.d("MentionDebug", "[$TAG] handleMentionDetection() - SUPPRESSED (cursor inside code format)")
            binding.suggestionListLayout.visibility = View.GONE
            suggestionList?.setList(emptyList())
            return
        }

        if (result.isActive && result.formatter != null) {
            android.util.Log.d("MentionDebug", "[$TAG] handleMentionDetection() - ACTIVE mention, triggering search")
            // Set flag to ignore stale LiveData callbacks until fresh search completes
            // This prevents old cached results from showing when @ is typed again
            isWaitingForFreshResults = true
            
            // Trigger search with debouncing (like Java's sendSearchQueryWithInterval)
            // The formatter will handle showing shimmer and updating the list
            sendSearchQueryWithInterval(result.formatter, result.query)
        } else {
            android.util.Log.d("MentionDebug", "[$TAG] handleMentionDetection() - INACTIVE mention, hiding list")
            // Cancel any pending search
            searchQueryTimer?.cancel()
            searchQueryTimer = null
            
            // Reset the flag
            isWaitingForFreshResults = false
            
            // Hide suggestion list when mention context is lost
            binding.suggestionListLayout.visibility = View.GONE
            suggestionList?.setList(emptyList())
            
            // Reset formatter state by calling search(null) - this clears the cached list
            // so old results don't show when @ is typed again
            android.util.Log.d("MentionDebug", "[$TAG] handleMentionDetection() - calling formatter.search(null) to clear cache")
            currentMentionDetectionResult?.formatter?.search(context, null)
        }
    }
    
    /**
     * Sends search query with interval to formatter (debouncing).
     * Matches Java's sendSearchQueryWithInterval method.
     */
    private fun sendSearchQueryWithInterval(formatter: CometChatTextFormatter, query: String) {
        android.util.Log.d("MentionDebug", "[$TAG] sendSearchQueryWithInterval() - query='$query', interval=${searchQueryInterval}ms")
        // Cancel any pending search
        searchQueryTimer?.cancel()
        searchQueryTimer = java.util.Timer()
        
        searchQueryTimer?.schedule(object : java.util.TimerTask() {
            override fun run() {
                // Run on main thread
                post {
                    android.util.Log.d("MentionDebug", "[$TAG] sendSearchQueryWithInterval() - timer fired, calling formatter.search('$query')")
                    // Clear the flag BEFORE calling search - this allows the observer
                    // to process the fresh results that will come from this search
                    isWaitingForFreshResults = false
                    formatter.search(context, query)
                }
            }
        }, searchQueryInterval)
    }

    /**
     * Handles suggestion item click.
     */
    private fun handleSuggestionItemClick(item: SuggestionItem) {
        val result = currentMentionDetectionResult ?: return
        
        // Set flag to prevent the rich text FormatSpanWatcher from processing the
        // text change triggered by mention insertion.  Without this guard the
        // afterTextChanged handler calls FormatSpanWatcher.handleTextChanged() which
        // can extend existing RichTextFormatSpan instances over the mention range,
        // overriding the NonEditableSpan styling (blue/highlighted text).
        isApplyingRichTextStyling = true
        try {
            mentionHelper?.onSuggestionSelected(item, result)
        } finally {
            isApplyingRichTextStyling = false
        }
        
        // Hide suggestion list after selection
        updateSuggestionListVisibility(false)
        
        // Sync mentions to update selected list
        mentionHelper?.syncMentions()
        
        // Update formatters with selected mentions
        processTextToSetUniqueSuggestions()
    }

    /**
     * Updates the visibility of the suggestion list.
     * Uses direct visibility changes without animation to avoid flickering
     * when rapid show/hide cycles occur during typing.
     */
    private fun updateSuggestionListVisibility(visible: Boolean) {
        // Show if we have items to display AND we're actively in a mention context
        val isInMentionContext = currentMentionDetectionResult?.isActive == true
        val shouldShow = visible && isInMentionContext
        
        android.util.Log.d(TAG, "updateSuggestionListVisibility: visible=$visible, isInMentionContext=$isInMentionContext, shouldShow=$shouldShow, currentVisibility=${binding.suggestionListLayout.visibility}")
        
        // Use direct visibility changes like Java implementation to avoid animation flickering
        if (shouldShow && binding.suggestionListLayout.visibility != View.VISIBLE) {
            android.util.Log.d(TAG, "updateSuggestionListVisibility: showing suggestion list")
            binding.suggestionListLayout.visibility = View.VISIBLE
        } else if (!shouldShow && binding.suggestionListLayout.visibility == View.VISIBLE) {
            android.util.Log.d(TAG, "updateSuggestionListVisibility: hiding suggestion list")
            binding.suggestionListLayout.visibility = View.GONE
            // Clear the list and reset formatter like Java implementation
            suggestionList?.setList(emptyList())
        }
    }

    /**
     * Processes text to extract unique suggestions and update formatters.
     * This tracks selected mentions via NonEditableSpan in the EditText.
     */
    private fun processTextToSetUniqueSuggestions() {
        mentionHelper?.syncMentions()
        
        val selectedItems = mentionHelper?.getSelectedSuggestionItems() ?: emptyList()
        
        // Group by formatter tracking character
        val selectedByFormatter = mutableMapOf<Char, MutableList<SuggestionItem>>()
        for (item in selectedItems) {
            // Determine which formatter this item belongs to based on prompt text
            for (formatter in textFormatters) {
                if (item.promptText.startsWith(formatter.getTrackingCharacter())) {
                    selectedByFormatter.getOrPut(formatter.getTrackingCharacter()) { mutableListOf() }
                        .add(item)
                    break
                }
            }
        }
        
        // Notify each formatter of its selected items
        for (formatter in textFormatters) {
            val items = selectedByFormatter[formatter.getTrackingCharacter()] ?: emptyList()
            formatter.setSelectedList(context, items)
        }
    }


    /**
     * Applies style attributes from XML using the style class factory method.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageComposer, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatMessageComposer_cometchatMessageComposerStyle, 0
        )
        typedArray.recycle()
        
        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageComposer, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatMessageComposerStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties from the style object to views.
     */
    private fun applyStyle() {
        // Container styling - always apply backgroundColor (transparent = 0 is valid)
        setCardBackgroundColor(style.backgroundColor)
        if (style.strokeColor != 0) setStrokeColor(style.strokeColor)
        if (style.strokeWidth != 0) strokeWidth = style.strokeWidth
        if (style.cornerRadius != 0) radius = style.cornerRadius.toFloat()
        
        // Compose box styling - always apply composeBoxBackgroundColor (transparent = 0 is valid)
        binding.composeBoxCard.setCardBackgroundColor(style.composeBoxBackgroundColor)
        if (style.composeBoxStrokeColor != 0) binding.composeBoxCard.setStrokeColor(style.composeBoxStrokeColor)
        if (style.composeBoxStrokeWidth != 0) binding.composeBoxCard.strokeWidth = style.composeBoxStrokeWidth
        if (style.composeBoxCornerRadius != 0) {
            binding.composeBoxCard.radius = style.composeBoxCornerRadius.toFloat()
            binding.composeBoxCard.cardElevation = 0f
        }
        
        // Separator styling
        if (style.separatorColor != 0) {
            binding.separatorView.setBackgroundColor(style.separatorColor)
            binding.toolbarSeparator1.setBackgroundColor(style.separatorColor)
            binding.toolbarSeparator2.setBackgroundColor(style.separatorColor)
            binding.toolbarInputSeparator.setBackgroundColor(style.separatorColor)
            binding.multilineRow2Separator?.setBackgroundColor(style.separatorColor)
        }
        
        // Attachment button styling
        style.attachmentIcon?.let { binding.ivAttachment.setImageDrawable(it) }
        if (style.attachmentIconTint != 0) binding.ivAttachment.setColorFilter(style.attachmentIconTint)
        
        // Voice recording button styling
        style.voiceRecordingIcon?.let { binding.ivVoiceRecording.setImageDrawable(it) }
        if (style.voiceRecordingIconTint != 0) binding.ivVoiceRecording.setColorFilter(style.voiceRecordingIconTint)
        
        // AI button styling
        style.aiIcon?.let { binding.ivAI.setImageDrawable(it) }
        if (style.aiIconTint != 0) binding.ivAI.setColorFilter(style.aiIconTint)
        
        // Sticker button styling
        style.stickerIcon?.let { binding.ivSticker.setImageDrawable(it) }
        if (style.stickerIconTint != 0) binding.ivSticker.setColorFilter(style.stickerIconTint)
        
        // Multiline Row 2 button styling — mirror single-line tints to multiline equivalents
        style.attachmentIcon?.let { binding.ivMultilineAttachment?.setImageDrawable(it) }
        if (style.attachmentIconTint != 0) binding.ivMultilineAttachment?.setColorFilter(style.attachmentIconTint)
        style.voiceRecordingIcon?.let { binding.ivMultilineVoiceRecording?.setImageDrawable(it) }
        if (style.voiceRecordingIconTint != 0) binding.ivMultilineVoiceRecording?.setColorFilter(style.voiceRecordingIconTint)
        style.stickerIcon?.let { binding.ivMultilineSticker?.setImageDrawable(it) }
        if (style.stickerIconTint != 0) binding.ivMultilineSticker?.setColorFilter(style.stickerIconTint)
        
        // Send button styling - applied via updateSendButtonState
        style.sendButtonInactiveIcon?.let { binding.ivSend.setImageDrawable(it) }
        
        // Edit preview styling
        if (style.editPreviewBackgroundColor != 0) binding.editPreviewCard.setCardBackgroundColor(style.editPreviewBackgroundColor)
        if (style.editPreviewStrokeWidth != 0) binding.editPreviewCard.strokeWidth = style.editPreviewStrokeWidth
        if (style.editPreviewStrokeColor != 0) binding.editPreviewCard.setStrokeColor(style.editPreviewStrokeColor)
        if (style.editPreviewCornerRadius != 0) {
            binding.editPreviewCard.radius = style.editPreviewCornerRadius.toFloat()
            binding.editPreviewCard.cardElevation = 0f
        }
        if (style.editPreviewTitleTextColor != 0) binding.tvEditPreviewTitle.setTextColor(style.editPreviewTitleTextColor)
        if (style.editPreviewTitleTextAppearance != 0) binding.tvEditPreviewTitle.setTextAppearance(style.editPreviewTitleTextAppearance)
        if (style.editPreviewMessageTextColor != 0) binding.tvEditPreviewMessage.setTextColor(style.editPreviewMessageTextColor)
        if (style.editPreviewMessageTextAppearance != 0) binding.tvEditPreviewMessage.setTextAppearance(style.editPreviewMessageTextAppearance)
        style.editPreviewCloseIcon?.let { binding.ivEditPreviewClose.setImageDrawable(it) }
        if (style.editPreviewCloseIconTint != 0) binding.ivEditPreviewClose.setColorFilter(style.editPreviewCloseIconTint)
        
        // Message preview styling
        if (style.messagePreviewBackgroundColor != 0) binding.messagePreviewCard.setCardBackgroundColor(style.messagePreviewBackgroundColor)
        if (style.messagePreviewStrokeWidth != 0) binding.messagePreviewCard.strokeWidth = style.messagePreviewStrokeWidth
        if (style.messagePreviewStrokeColor != 0) binding.messagePreviewCard.setStrokeColor(style.messagePreviewStrokeColor)
        if (style.messagePreviewCornerRadius != 0) {
            binding.messagePreviewCard.radius = style.messagePreviewCornerRadius.toFloat()
            binding.messagePreviewCard.cardElevation = 0f
        }
        if (style.messagePreviewSeparatorColor != 0) binding.messagePreviewSeparator.setBackgroundColor(style.messagePreviewSeparatorColor)
        if (style.messagePreviewTitleTextColor != 0) binding.tvMessagePreviewTitle.setTextColor(style.messagePreviewTitleTextColor)
        if (style.messagePreviewTitleTextAppearance != 0) binding.tvMessagePreviewTitle.setTextAppearance(style.messagePreviewTitleTextAppearance)
        if (style.messagePreviewSubtitleTextColor != 0) binding.tvMessagePreviewSubtitle.setTextColor(style.messagePreviewSubtitleTextColor)
        if (style.messagePreviewSubtitleTextAppearance != 0) binding.tvMessagePreviewSubtitle.setTextAppearance(style.messagePreviewSubtitleTextAppearance)
        style.messagePreviewCloseIcon?.let { binding.ivMessagePreviewClose.setImageDrawable(it) }
        if (style.messagePreviewCloseIconTint != 0) binding.ivMessagePreviewClose.setColorFilter(style.messagePreviewCloseIconTint)
        
        // Input text styling
        if (style.inputTextColor != 0) binding.etMessageInput.setTextColor(style.inputTextColor)
        if (style.inputTextAppearance != 0) binding.etMessageInput.setTextAppearance(style.inputTextAppearance)
        if (style.inputPlaceholderColor != 0) binding.etMessageInput.setHintTextColor(style.inputPlaceholderColor)
        
        // Rich text toolbar styling
        if (style.richTextToolbarBackgroundColor != 0) {
            binding.richTextToolbarLayout.setBackgroundColor(style.richTextToolbarBackgroundColor)
            binding.multilineToolbarGroup?.setBackgroundColor(style.richTextToolbarBackgroundColor)
        }
        if (style.richTextToolbarIconTint != 0) applyRichTextToolbarIconTints()
        
        // Update send button state with current text
        updateSendButtonState(binding.etMessageInput.text?.isNotBlank() ?: false)
        
        // Apply visibility
        updateButtonVisibility()
    }

    /**
     * Applies tint colors to rich text toolbar icons.
     */
    private fun applyRichTextToolbarIconTints() {
        val tint = style.richTextToolbarIconTint
        binding.ivFormatBold.setColorFilter(tint)
        binding.ivFormatItalic.setColorFilter(tint)
        binding.ivFormatUnderline.setColorFilter(tint)
        binding.ivFormatStrikethrough.setColorFilter(tint)
        binding.ivFormatCode.setColorFilter(tint)
        binding.ivFormatCodeBlock.setColorFilter(tint)
        binding.ivFormatLink.setColorFilter(tint)
        binding.ivFormatBulletList.setColorFilter(tint)
        binding.ivFormatOrderedList.setColorFilter(tint)
        binding.ivFormatBlockquote.setColorFilter(tint)
    }

    /**
     * Updates button visibility based on hide flags.
     * Rich text toolbar visibility is based on richTextToolbarVisibility setting.
     * Sticker and voice recording buttons are hidden when text is entered.
     * Animations are applied for smooth transitions with proper tracking to prevent vibration during fast typing.
     */
    private fun updateButtonVisibility() {
        // In multiline mode, single-line button visibility is managed by updateMultilineModeLayout()
        // and updateMultilineRow2Visibility(). Skip to prevent re-showing single-line elements.
        if (composerLayoutMode == ComposerLayoutMode.MULTI_LINE) return

        val hasText = binding.etMessageInput.text?.isNotEmpty() == true
        
        binding.ivAttachment.visibility = if (hideAttachmentButton) View.GONE else View.VISIBLE
        binding.secondaryButtonLayout.visibility = if (hideAttachmentButton) View.GONE else View.VISIBLE
        binding.separatorView.visibility = if (hideAttachmentButton) View.GONE else View.VISIBLE
        
        // Hide voice recording and sticker buttons when text is entered or attachments are staged
        val hasContent = hasText || hasStagedAttachments()
        val shouldShowVoiceRecording = !hideVoiceRecordingButton && !hasContent
        val shouldShowSticker = !hideStickerButton && !hasContent
        
        // Voice recording button animation - only animate if not already animating to the same state
        val voiceRecordingCurrentlyVisible = binding.ivVoiceRecording.visibility == View.VISIBLE
        if (shouldShowVoiceRecording && !voiceRecordingCurrentlyVisible && !isVoiceRecordingAnimating) {
            // Cancel any ongoing animation first
            voiceRecordingAnimation?.cancel()
            binding.ivVoiceRecording.clearAnimation()
            
            isVoiceRecordingAnimating = true
            binding.ivVoiceRecording.visibility = View.VISIBLE
            val slideIn = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.cometchat_slide_in_right)
            slideIn.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    isVoiceRecordingAnimating = false
                    voiceRecordingAnimation = null
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
            voiceRecordingAnimation = slideIn
            binding.ivVoiceRecording.startAnimation(slideIn)
        } else if (!shouldShowVoiceRecording && voiceRecordingCurrentlyVisible && !isVoiceRecordingAnimating) {
            // Cancel any ongoing animation first
            voiceRecordingAnimation?.cancel()
            binding.ivVoiceRecording.clearAnimation()
            
            isVoiceRecordingAnimating = true
            val slideOut = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.cometchat_slide_out_right)
            slideOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    binding.ivVoiceRecording.visibility = View.GONE
                    isVoiceRecordingAnimating = false
                    voiceRecordingAnimation = null
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
            voiceRecordingAnimation = slideOut
            binding.ivVoiceRecording.startAnimation(slideOut)
        } else if (!shouldShowVoiceRecording && !voiceRecordingCurrentlyVisible) {
            // Ensure it stays hidden without animation
            binding.ivVoiceRecording.visibility = View.GONE
        }
        
        // Sticker button animation - only animate if not already animating to the same state
        val stickerCurrentlyVisible = binding.ivSticker.visibility == View.VISIBLE
        if (shouldShowSticker && !stickerCurrentlyVisible && !isStickerAnimating) {
            // Cancel any ongoing animation first
            stickerAnimation?.cancel()
            binding.ivSticker.clearAnimation()
            
            isStickerAnimating = true
            binding.ivSticker.visibility = View.VISIBLE
            val slideIn = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.cometchat_slide_in_right)
            slideIn.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    isStickerAnimating = false
                    stickerAnimation = null
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
            stickerAnimation = slideIn
            binding.ivSticker.startAnimation(slideIn)
        } else if (!shouldShowSticker && stickerCurrentlyVisible && !isStickerAnimating) {
            // Cancel any ongoing animation first
            stickerAnimation?.cancel()
            binding.ivSticker.clearAnimation()
            
            isStickerAnimating = true
            val slideOut = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.cometchat_slide_out_right)
            slideOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    binding.ivSticker.visibility = View.GONE
                    isStickerAnimating = false
                    stickerAnimation = null
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
            stickerAnimation = slideOut
            binding.ivSticker.startAnimation(slideOut)
        } else if (!shouldShowSticker && !stickerCurrentlyVisible) {
            // Ensure it stays hidden without animation
            binding.ivSticker.visibility = View.GONE
        }
        
        binding.ivAI.visibility = if (hideAIButton) View.GONE else View.VISIBLE
        
        // Rich text toolbar visibility - controlled by richTextToolbarVisibility property
        // Toolbar is always visible inside the composer when enabled, regardless of text presence
        val showToolbar = richTextToolbarVisibility == View.VISIBLE && richTextConfiguration.hasAnyEnabled()
        
        // Toolbar animation - only animate if not already animating to the same state
        val toolbarCurrentlyVisible = binding.richTextToolbarScrollView.visibility == View.VISIBLE
        if (showToolbar && !toolbarCurrentlyVisible && !isToolbarAnimating) {
            // Cancel any ongoing animation first
            toolbarAnimation?.cancel()
            binding.richTextToolbarScrollView.clearAnimation()
            
            isToolbarAnimating = true
            binding.richTextToolbarScrollView.visibility = View.VISIBLE
            binding.toolbarInputSeparator.visibility = View.VISIBLE
            val expandAnim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.cometchat_expand_vertical)
            expandAnim.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    // Clear animation to ensure proper rendering after scale animation
                    binding.richTextToolbarScrollView.clearAnimation()
                    isToolbarAnimating = false
                    toolbarAnimation = null
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
            toolbarAnimation = expandAnim
            binding.richTextToolbarScrollView.startAnimation(expandAnim)
        } else if (!showToolbar && toolbarCurrentlyVisible && !isToolbarAnimating) {
            // Cancel any ongoing animation first
            toolbarAnimation?.cancel()
            binding.richTextToolbarScrollView.clearAnimation()
            
            isToolbarAnimating = true
            val collapseAnim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.cometchat_collapse_vertical)
            collapseAnim.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    binding.richTextToolbarScrollView.clearAnimation()
                    binding.richTextToolbarScrollView.visibility = View.GONE
                    binding.toolbarInputSeparator.visibility = View.GONE
                    isToolbarAnimating = false
                    toolbarAnimation = null
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
            toolbarAnimation = collapseAnim
            binding.richTextToolbarScrollView.startAnimation(collapseAnim)
        } else if (!showToolbar && !toolbarCurrentlyVisible) {
            // Ensure it stays hidden without animation
            binding.richTextToolbarScrollView.visibility = View.GONE
            binding.toolbarInputSeparator.visibility = View.GONE
        }
        
        // Apply separator colors for toolbar separators
        if (style.separatorColor != 0) {
            binding.toolbarSeparator1.setBackgroundColor(style.separatorColor)
            binding.toolbarSeparator2.setBackgroundColor(style.separatorColor)
            binding.toolbarInputSeparator.setBackgroundColor(style.separatorColor)
        }
    }

    /**
     * Updates button alignment based on input field line count.
     * - Single line: buttons are vertically centered
     * - Multi-line: buttons align to bottom
     */
    private fun updateButtonAlignment() {
        val lineCount = binding.etMessageInput.lineCount
        val isMultiLine = lineCount > 1
        
        // Get ConstraintLayout params for each button layout
        val secondaryParams = binding.secondaryButtonLayout.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        val auxiliaryParams = binding.auxiliaryButtonLayout.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        val sendParams = binding.sendButtonLayout.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        
        if (isMultiLine) {
            // Multi-line: align buttons to bottom only
            secondaryParams?.apply {
                topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            }
            auxiliaryParams?.apply {
                topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            }
            sendParams?.apply {
                topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            }
        } else {
            // Single line: center buttons vertically with EditText
            secondaryParams?.apply {
                topToTop = binding.etMessageInput.id
                bottomToBottom = binding.etMessageInput.id
            }
            auxiliaryParams?.apply {
                topToTop = binding.etMessageInput.id
                bottomToBottom = binding.etMessageInput.id
            }
            sendParams?.apply {
                topToTop = binding.auxiliaryButtonLayout.id
                bottomToBottom = binding.auxiliaryButtonLayout.id
            }
        }
        
        // Apply the updated params
        secondaryParams?.let { binding.secondaryButtonLayout.layoutParams = it }
        auxiliaryParams?.let { binding.auxiliaryButtonLayout.layoutParams = it }
        sendParams?.let { binding.sendButtonLayout.layoutParams = it }
    }


    /**
     * Sets up click listeners for interactive elements.
     */
    private fun setupClickListeners() {
        // Attachment button - show popup
        binding.ivAttachment.setOnClickListener {
            android.util.Log.d(TAG, "━━━ ivAttachment.onClick ━━━ isAttachmentPopupOpen=$isAttachmentPopupOpen, popupIsShowing=${attachmentPopup?.isShowing()}, popup=${attachmentPopup?.hashCode()}, thread=${Thread.currentThread().name}, time=${System.currentTimeMillis()}")
            toggleAttachmentPopup()
        }

        // Voice recording button
        binding.ivVoiceRecording.setOnClickListener {
            showInlineRecorder()
        }

        // AI button
        binding.ivAI.setOnClickListener {
            onAIClick?.invoke()
        }

        // Sticker button
        binding.ivSticker.setOnClickListener {
            toggleStickerKeyboard()
            onStickerClick?.invoke()
        }

        // Send button
        binding.ivSend.setOnClickListener {
            val text = binding.etMessageInput.text?.toString() ?: ""
            // Staged attachments send without any text (the text, when present, becomes the
            // caption) — the blank guard only applies to pure text sends.
            if (text.isNotBlank() || hasStagedAttachments()) {
                handleSendClick(text)
            }
        }

        // Edit preview close
        binding.ivEditPreviewClose.setOnClickListener {
            exitEditMode()
        }

        // Message preview close
        binding.ivMessagePreviewClose.setOnClickListener {
            exitReplyMode()
        }

        // Rich text toggle button removed - toolbar visibility is now automatic based on text presence

        // Multiline Row 2 button click listeners
        binding.ivMultilineAttachment?.setOnClickListener {
            toggleAttachmentPopup()
        }
        binding.ivMultilineVoiceRecording?.setOnClickListener {
            showInlineRecorder()
        }
        binding.ivMultilineSticker?.setOnClickListener {
            toggleStickerKeyboard()
            onStickerClick?.invoke()
        }
        binding.ivMultilineFormattingToggle?.setOnClickListener {
            isFormattingToolbarVisible = true
            updateMultilineRow2Visibility()
            updateFormattingToggleTint()
        }
        binding.multilineSendButtonCard?.setOnClickListener {
            val text = binding.etMessageInput.text?.toString() ?: ""
            // Staged attachments send without any text (the text, when present, becomes the
            // caption) — the blank guard only applies to pure text sends.
            if (text.isNotBlank() || hasStagedAttachments()) {
                handleSendClick(text)
            }
        }
        binding.ivMultilineToolbarClose?.setOnClickListener {
            isFormattingToolbarVisible = false
            updateMultilineRow2Visibility()
            updateFormattingToggleTint()
        }

        // Rich text format buttons
        setupRichTextFormatClickListeners()

        // Link click detection on EditText
        setupLinkClickDetection()
    }

    /**
     * Sets up click listeners for rich text format buttons.
     */
    private fun setupRichTextFormatClickListeners() {
        binding.ivFormatBold.setOnClickListener { toggleFormat(RichTextFormat.BOLD) }
        binding.ivFormatItalic.setOnClickListener { toggleFormat(RichTextFormat.ITALIC) }
        binding.ivFormatUnderline.setOnClickListener { toggleFormat(RichTextFormat.UNDERLINE) }
        binding.ivFormatStrikethrough.setOnClickListener { toggleFormat(RichTextFormat.STRIKETHROUGH) }
        binding.ivFormatCode.setOnClickListener { toggleFormat(RichTextFormat.INLINE_CODE) }
        binding.ivFormatCodeBlock.setOnClickListener { toggleFormat(RichTextFormat.CODE_BLOCK) }
        binding.ivFormatLink.setOnClickListener { showLinkDialog() }
        binding.ivFormatBulletList.setOnClickListener { toggleFormat(RichTextFormat.BULLET_LIST) }
        binding.ivFormatOrderedList.setOnClickListener { toggleFormat(RichTextFormat.ORDERED_LIST) }
        binding.ivFormatBlockquote.setOnClickListener { toggleFormat(RichTextFormat.BLOCKQUOTE) }
    }

    /**
     * Sets up the text selection context menu with formatting options.
     *
     * When the user long-presses and selects text, Bold, Italic, Strikethrough,
     * and InlineCode options appear in the system ActionMode menu. Tapping an
     * option applies the format to the selected range via [toggleFormat] and
     * finishes the action mode.
     *
     * If [enableRichTextFormatting] is false or [showTextSelectionMenuItems] is
     * false, the custom callback is removed (set to null) so the system default
     * menu is restored.
     *
     * Requirements: 19.1–19.8
     */
    private fun setupTextSelectionMenu() {
        if (!enableRichTextFormatting || !showTextSelectionMenuItems) {
            binding.etMessageInput.customSelectionActionModeCallback = null
            return
        }

        binding.etMessageInput.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                menu.add(0, R.id.action_bold, 0, R.string.cometchat_bold)
                menu.add(0, R.id.action_italic, 1, R.string.cometchat_italic)
                menu.add(0, R.id.action_strikethrough, 2, R.string.cometchat_strikethrough)
                menu.add(0, R.id.action_inline_code, 3, R.string.cometchat_inline_code)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                val format = when (item.itemId) {
                    R.id.action_bold -> RichTextFormat.BOLD
                    R.id.action_italic -> RichTextFormat.ITALIC
                    R.id.action_strikethrough -> RichTextFormat.STRIKETHROUGH
                    R.id.action_inline_code -> RichTextFormat.INLINE_CODE
                    else -> return false
                }
                toggleFormat(format)
                mode.finish()
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                // No cleanup needed
            }
        }
    }

    /**
     * Toggles a rich text format on toolbar button click.
     *
     * Uses the pure functions RichTextFormat.toggleFormat() and
     * RichTextFormat.computeDisabledFormats() from chatuikit-core to manage
     * toolbar state, then delegates to the span engine for visual formatting.
     *
     * @param format The format to toggle
     */
    private fun toggleFormat(format: RichTextFormat) {
        if (!richTextConfiguration.hasAnyEnabled()) return

        // Skip if format is currently disabled (incompatible with active formats)
        if (format in disabledFormats) return

        // Compute new active formats using pure function (handles auto-deselect rules)
        activeFormats = RichTextFormat.toggleFormat(activeFormats, format)

        // Compute new disabled formats based on updated active set
        disabledFormats = RichTextFormat.computeDisabledFormats(activeFormats)

        // Refresh all toolbar button visuals
        updateToolbarButtonStates()

        // Delegate to span engine for WYSIWYG formatting
        val editable = binding.etMessageInput.text ?: return
        val selStart = binding.etMessageInput.selectionStart
        val selEnd = binding.etMessageInput.selectionEnd

        val isCodeFormat = format == RichTextFormat.CODE_BLOCK || format == RichTextFormat.INLINE_CODE
        val isListFormat = format == RichTextFormat.BULLET_LIST || format == RichTextFormat.ORDERED_LIST
        val isBlockFormat = isListFormat || format == RichTextFormat.BLOCKQUOTE
        val isNowActive = format in activeFormats

        if (selStart != selEnd) {
            // Has selection → apply/remove span on the selected range
            RichTextSpanManager.toggleFormat(editable, format, selStart, selEnd, context)

            // When applying a code format on selection, remove the conflicting code format spans
            if (isCodeFormat && isNowActive) {
                val conflicting = if (format == RichTextFormat.CODE_BLOCK)
                    RichTextFormat.INLINE_CODE else RichTextFormat.CODE_BLOCK
                RichTextSpanManager.removeFormat(editable, selStart, selEnd, conflicting, context)
            }

            // Consume or restore mentions when code formatting is toggled
            if (isCodeFormat) {
                if (isNowActive) {
                    MentionCodeBlockHandler.consumeMentionsInRange(editable, selStart, selEnd)
                } else {
                    MentionCodeBlockHandler.restoreMentionsInRange(editable, selStart, selEnd)
                }
            }
        } else {
            // Collapsed cursor → toggle pending format for next typed character
            if (isNowActive) {
                // When enabling a list format, clear the conflicting list format from pending
                if (isListFormat) {
                    val conflicting = if (format == RichTextFormat.BULLET_LIST)
                        RichTextFormat.ORDERED_LIST else RichTextFormat.BULLET_LIST
                    formatSpanWatcher?.disableFormat(conflicting)
                    // Also remove any existing conflicting list spans on the current line
                    removeConflictingListSpansOnCurrentLine(editable, selStart, conflicting)
                }

                // When enabling a code format, clear the conflicting code format from pending
                // and remove existing conflicting code spans on the current line
                if (isCodeFormat) {
                    val conflicting = if (format == RichTextFormat.CODE_BLOCK)
                        RichTextFormat.INLINE_CODE else RichTextFormat.CODE_BLOCK
                    formatSpanWatcher?.disableFormat(conflicting)
                    // Remove existing conflicting code spans on the current line
                    val lineStart = findLineStart(editable, selStart)
                    val lineEnd = findLineEnd(editable, selStart)
                    if (lineStart < lineEnd) {
                        RichTextSpanManager.removeFormat(editable, lineStart, lineEnd, conflicting, context)
                        // Apply the new code format to existing text on the current line
                        RichTextSpanManager.applyFormat(editable, lineStart, lineEnd, format, context)
                    }

                    // When enabling CODE_BLOCK, also remove any list/blockquote spans on the current line
                    // Code block wins — these block formats are mutually exclusive with code block
                    if (format == RichTextFormat.CODE_BLOCK) {
                        formatSpanWatcher?.disableFormat(RichTextFormat.BULLET_LIST)
                        formatSpanWatcher?.disableFormat(RichTextFormat.ORDERED_LIST)
                        formatSpanWatcher?.disableFormat(RichTextFormat.BLOCKQUOTE)
                        if (lineStart < lineEnd) {
                            RichTextSpanManager.removeFormat(editable, lineStart, lineEnd, RichTextFormat.BULLET_LIST, context)
                            RichTextSpanManager.removeFormat(editable, lineStart, lineEnd, RichTextFormat.ORDERED_LIST, context)
                            RichTextSpanManager.removeFormat(editable, lineStart, lineEnd, RichTextFormat.BLOCKQUOTE, context)
                        }
                    }
                }

                formatSpanWatcher?.enableFormatWithSpanUpdate(editable, format, selStart)

                // For block formats (list/blockquote), immediately insert a visual
                // prefix on the current line so the user sees feedback right away
                if (isBlockFormat) {
                    insertBlockFormatOnCurrentLine(editable, selStart, format)
                }
            } else {
                formatSpanWatcher?.disableFormatWithSpanUpdate(editable, format, selStart)

                // When disabling a block format, remove the span from the current line
                if (isBlockFormat) {
                    removeBlockFormatFromCurrentLine(editable, selStart, format)
                }

                // When disabling code block, remove the span from the current line
                if (format == RichTextFormat.CODE_BLOCK) {
                    removeBlockFormatFromCurrentLine(editable, selStart, format)
                }
            }
        }
    }

    /**
     * Inserts a block-level format span on the current line when the user
     * toggles a list or blockquote format with a collapsed cursor.
     * This gives immediate visual feedback (bullet/number/quote prefix).
     */
    private fun insertBlockFormatOnCurrentLine(editable: Editable, cursorPos: Int, format: RichTextFormat) {
        val lineStart = findLineStart(editable, cursorPos)
        var lineEnd = findLineEnd(editable, cursorPos)
        
        // Don't apply if already covered
        val searchEnd = maxOf(lineEnd, lineStart + 1).coerceAtMost(editable.length)
        val existing = editable.getSpans(lineStart, searchEnd, RichTextFormatSpan::class.java)
        if (existing.any { it.getFormatType() == format }) return

        isApplyingRichTextStyling = true
        try {
            // V5 approach: if the line is empty, insert a space placeholder so the
            // LeadingMarginSpan can render the prefix (bullet/number) immediately.
            if (lineStart >= lineEnd) {
                editable.insert(lineStart, " ")
                lineEnd = lineStart + 1
            }

            val span: RichTextFormatSpan = when (format) {
                RichTextFormat.BULLET_LIST ->
                    if (context != null) BulletListFormatSpan(context) else BulletListFormatSpan()
                RichTextFormat.ORDERED_LIST -> {
                    val num = calculateListNumber(editable, lineStart)
                    if (context != null) NumberedListFormatSpan(num, context) else NumberedListFormatSpan(num)
                }
                RichTextFormat.BLOCKQUOTE ->
                    if (context != null) BlockquoteFormatSpan(context) else BlockquoteFormatSpan()
                else -> return
            }
            editable.setSpan(span, lineStart, maxOf(lineEnd, lineStart + 1), android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            
            // Move cursor to end of line
            binding.etMessageInput.setSelection(maxOf(lineEnd, lineStart + 1))
        } finally {
            isApplyingRichTextStyling = false
        }
    }

    /**
     * Removes a block-level format span from the current line when the user
     * toggles off a list or blockquote format.
     */
    private fun removeBlockFormatFromCurrentLine(editable: Editable, cursorPos: Int, format: RichTextFormat) {
        val lineStart = findLineStart(editable, cursorPos)
        val lineEnd = findLineEnd(editable, cursorPos)
        RichTextSpanManager.removeFormat(editable, lineStart, maxOf(lineEnd, lineStart + 1), format, context)
    }

    /**
     * Removes conflicting list spans from the current line.
     * E.g., when enabling BULLET_LIST, removes any ORDERED_LIST spans on the same line.
     */
    private fun removeConflictingListSpansOnCurrentLine(editable: Editable, cursorPos: Int, conflicting: RichTextFormat) {
        val lineStart = findLineStart(editable, cursorPos)
        val lineEnd = findLineEnd(editable, cursorPos)
        val spans = editable.getSpans(lineStart, maxOf(lineEnd, lineStart + 1), RichTextFormatSpan::class.java)
        for (span in spans) {
            if (span.getFormatType() == conflicting) {
                editable.removeSpan(span)
            }
        }
    }

    /** Finds the start of the line containing [position]. */
    private fun findLineStart(text: CharSequence, position: Int): Int {
        if (position <= 0) return 0
        var i = position - 1
        while (i > 0 && text[i] != '\n') i--
        return if (i == 0 && text[0] != '\n') 0 else i + 1
    }

    /** Finds the end of the line containing [position]. */
    private fun findLineEnd(text: CharSequence, position: Int): Int {
        val length = text.length
        if (position >= length) return length
        var i = position
        while (i < length && text[i] != '\n') i++
        return i
    }

    /** Calculates the list number for a numbered list item at the given line start.
     *  Scans backwards through all preceding lines to find the last NumberedListFormatSpan
     *  and continues from that number. This handles the case where bullet list items
     *  appear between numbered list sections. */
    private fun calculateListNumber(editable: Editable, lineStart: Int): Int {
        if (lineStart <= 0) return 1
        // Scan backwards through all lines to find the last numbered list span
        var scanPos = lineStart - 1
        while (scanPos >= 0) {
            var scanLineStart = scanPos
            while (scanLineStart > 0 && editable[scanLineStart - 1] != '\n') scanLineStart--
            val spans = editable.getSpans(scanLineStart, scanPos + 1, NumberedListFormatSpan::class.java)
            if (spans.isNotEmpty()) {
                return spans.maxOf { it.number } + 1
            }
            // Move to previous line
            scanPos = scanLineStart - 1
            if (scanPos < 0) break
        }
        return 1
    }

    /**
     * Handles Enter key for block formats (list, blockquote, code block).
     * Ported from V5's inline newline handling.
     *
     * On a blank/empty line: exits the block format (removes empty line spans,
     * deletes the empty line, disables the format in the watcher).
     * On a non-empty line: does nothing (FormatSpanWatcher handles continuation).
     */
    private fun handleNewlineForBlockFormats(editable: Editable, newlinePosition: Int) {
        if (newlinePosition <= 0) return

        // ── Step 1: Check for double-enter (exit block format) ──────────
        var isDoubleEnter = false

        if (editable[newlinePosition - 1] == '\n') {
            isDoubleEnter = true
        } else {
            var lineStart = newlinePosition - 1
            while (lineStart > 0 && editable[lineStart - 1] != '\n') lineStart--

            var lineIsEmpty = true
            for (i in lineStart until newlinePosition) {
                val c = editable[i]
                if (c != ' ' && c != '\u200B' && c != '\t') {
                    lineIsEmpty = false
                    break
                }
            }
            if (lineIsEmpty) {
                val hasBlockFormat = formatSpanWatcher?.isPendingFormat(RichTextFormat.BULLET_LIST) == true ||
                    formatSpanWatcher?.isPendingFormat(RichTextFormat.ORDERED_LIST) == true ||
                    formatSpanWatcher?.isPendingFormat(RichTextFormat.CODE_BLOCK) == true ||
                    formatSpanWatcher?.isPendingFormat(RichTextFormat.BLOCKQUOTE) == true
                isDoubleEnter = hasBlockFormat
            }
        }

        if (isDoubleEnter) {
            val emptyLineStart: Int = if (newlinePosition > 0 && editable[newlinePosition - 1] == '\n') {
                newlinePosition
            } else {
                var start = newlinePosition - 1
                while (start > 0 && editable[start] != '\n') start--
                if (start > 0 && editable[start] == '\n') start + 1
                else if (start == 0 && editable[0] != '\n') 0
                else start
            }
            val emptyLineEnd = newlinePosition
            val watcher = formatSpanWatcher

            if (watcher?.isPendingFormat(RichTextFormat.BULLET_LIST) == true) {
                exitBlockOnEmptyLine(editable, emptyLineStart, emptyLineEnd, BulletListFormatSpan::class.java)
                watcher.disableFormat(RichTextFormat.BULLET_LIST)
                return
            }
            if (watcher?.isPendingFormat(RichTextFormat.ORDERED_LIST) == true) {
                exitBlockOnEmptyLine(editable, emptyLineStart, emptyLineEnd, NumberedListFormatSpan::class.java)
                watcher.disableFormat(RichTextFormat.ORDERED_LIST)
                return
            }
            if (watcher?.isPendingFormat(RichTextFormat.CODE_BLOCK) == true) {
                truncateBlockSpan(editable, newlinePosition, CodeBlockFormatSpan::class.java)
                watcher.disableFormatWithSpanUpdate(editable, RichTextFormat.CODE_BLOCK, newlinePosition)
                return
            }
            if (watcher?.isPendingFormat(RichTextFormat.BLOCKQUOTE) == true) {
                truncateBlockSpan(editable, newlinePosition, BlockquoteFormatSpan::class.java)
                watcher.disableFormatWithSpanUpdate(editable, RichTextFormat.BLOCKQUOTE, newlinePosition)
                return
            }
            return
        }

        // ── Step 2: List continuation (V5 approach) ─────────────────────
        // Check if the line BEFORE the newline had a list format.
        // If so, insert a placeholder space with the list span on the new line
        // so the prefix (bullet/number) is visible immediately.
        val prevLineEnd = newlinePosition
        val prevLineStart = findLineStart(editable, if (prevLineEnd > 0) prevLineEnd - 1 else 0)

        // Bullet list continuation
        val bulletSpans = editable.getSpans(prevLineStart, prevLineEnd, BulletListFormatSpan::class.java)
        if (bulletSpans.isNotEmpty()) {
            // Trim original span so it doesn't extend past the newline
            for (span in bulletSpans) {
                val spanEnd = editable.getSpanEnd(span)
                if (spanEnd > newlinePosition) {
                    val spanStart = editable.getSpanStart(span)
                    editable.removeSpan(span)
                    if (spanStart < newlinePosition) {
                        editable.setSpan(span, spanStart, newlinePosition, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
            // Insert placeholder space with bullet span on new line
            val newLineStart = newlinePosition + 1
            editable.insert(newLineStart, " ")
            val newSpan = if (context != null) BulletListFormatSpan(context) else BulletListFormatSpan()
            editable.setSpan(newSpan, newLineStart, newLineStart + 1, android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            formatSpanWatcher?.enableFormat(RichTextFormat.BULLET_LIST)
            binding.etMessageInput.setSelection(newLineStart + 1)
            return
        }

        // Numbered list continuation
        val numberedSpans = editable.getSpans(prevLineStart, prevLineEnd, NumberedListFormatSpan::class.java)
        if (numberedSpans.isNotEmpty()) {
            // Trim original span
            for (span in numberedSpans) {
                val spanEnd = editable.getSpanEnd(span)
                if (spanEnd > newlinePosition) {
                    val spanStart = editable.getSpanStart(span)
                    editable.removeSpan(span)
                    if (spanStart < newlinePosition) {
                        editable.setSpan(span, spanStart, newlinePosition, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
            // Calculate next number
            val prevNumber = numberedSpans.maxOf { it.number }
            val nextNumber = prevNumber + 1
            // Insert placeholder space with numbered span on new line
            val newLineStart = newlinePosition + 1
            editable.insert(newLineStart, " ")
            val newSpan = if (context != null) NumberedListFormatSpan(nextNumber, context) else NumberedListFormatSpan(nextNumber)
            editable.setSpan(newSpan, newLineStart, newLineStart + 1, android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            formatSpanWatcher?.enableFormat(RichTextFormat.ORDERED_LIST)
            binding.etMessageInput.setSelection(newLineStart + 1)
            return
        }

        // Code block / blockquote continuation — just enable pending format
        val codeSpans = editable.getSpans(prevLineStart, prevLineEnd, CodeBlockFormatSpan::class.java)
        if (codeSpans.isNotEmpty()) {
            formatSpanWatcher?.enableFormat(RichTextFormat.CODE_BLOCK)
        }
        val quoteSpans = editable.getSpans(prevLineStart, prevLineEnd, BlockquoteFormatSpan::class.java)
        if (quoteSpans.isNotEmpty()) {
            formatSpanWatcher?.enableFormat(RichTextFormat.BLOCKQUOTE)
        }
    }

    private fun <T> exitBlockOnEmptyLine(editable: Editable, start: Int, end: Int, cls: Class<T>) {
        if (start < end) {
            for (s in editable.getSpans(start, end, cls)) editable.removeSpan(s)
            editable.delete(start, end)
        }
    }

    private fun <T : RichTextFormatSpan> truncateBlockSpan(editable: Editable, pos: Int, cls: Class<T>) {
        for (span in editable.getSpans(0, editable.length, cls)) {
            val ss = editable.getSpanStart(span)
            val se = editable.getSpanEnd(span)
            if (se > pos && ss < pos) {
                val flags = editable.getSpanFlags(span)
                editable.removeSpan(span)
                editable.setSpan(span, ss, pos, flags)
            }
        }
    }
    
    /**
     * Updates the active and disabled format sets based on the spans present
     * at the current cursor position. Called after text changes to keep the
     * toolbar state in sync with the WYSIWYG span state.
     */
    private fun updateActiveFormatsFromCursor() {
        val editable = binding.etMessageInput.text ?: return
        val selStart = binding.etMessageInput.selectionStart
        val selEnd = binding.etMessageInput.selectionEnd

        val formatsAtCursor = if (selStart == selEnd) {
            val spanFormats = RichTextSpanManager.getFormatsAt(editable, selStart)
            val pending = formatSpanWatcher?.getPendingFormats() ?: emptySet()
            val disabled = formatSpanWatcher?.getExplicitlyDisabledFormats() ?: emptySet()
            (spanFormats + pending) - disabled
        } else {
            RichTextSpanManager.getFormatsInRange(editable, selStart, selEnd)
        }

        activeFormats = formatsAtCursor
        disabledFormats = RichTextFormat.computeDisabledFormats(activeFormats)
        updateToolbarButtonStates()
    }

    /**
     * Checks whether the given cursor [position] is inside a CodeBlock or InlineCode span.
     *
     * @return `true` when the cursor sits inside code formatting, `false` otherwise.
     */
    private fun isInsideCodeFormat(editable: Editable, position: Int): Boolean {
        val formats = RichTextSpanManager.getFormatsAt(editable, position)
        return RichTextFormat.CODE_BLOCK in formats || RichTextFormat.INLINE_CODE in formats
    }

    /**
     * Updates the toolbar button states based on active and disabled formats.
     * Uses three-state visual feedback: active, normal, and disabled.
     */
    private fun updateToolbarButtonStates() {
        // Resolve colors for each state
        val activeIconTint = style.richTextToolbarActiveIconTint.takeIf { it != 0 }
            ?: CometChatTheme.getTextColorPrimary(context)
        val activeIconBgColor = style.richTextToolbarActiveIconBackgroundColor.takeIf { it != 0 }
            ?: CometChatTheme.getBackgroundColor4(context)
        val normalIconTint = style.richTextToolbarIconTint.takeIf { it != 0 }
            ?: CometChatTheme.getIconTintSecondary(context)
        val disabledIconTint = CometChatTheme.getIconTintTertiary(context)

        // Update each button with its card wrapper
        updateButtonState(binding.ivFormatBold, binding.cardBold, RichTextFormat.BOLD, disabledFormats, activeIconTint, activeIconBgColor, normalIconTint, disabledIconTint)
        updateButtonState(binding.ivFormatItalic, binding.cardItalic, RichTextFormat.ITALIC, disabledFormats, activeIconTint, activeIconBgColor, normalIconTint, disabledIconTint)
        updateButtonState(binding.ivFormatUnderline, binding.cardUnderline, RichTextFormat.UNDERLINE, disabledFormats, activeIconTint, activeIconBgColor, normalIconTint, disabledIconTint)
        updateButtonState(binding.ivFormatStrikethrough, binding.cardStrikethrough, RichTextFormat.STRIKETHROUGH, disabledFormats, activeIconTint, activeIconBgColor, normalIconTint, disabledIconTint)
        updateButtonState(binding.ivFormatCode, binding.cardInlineCode, RichTextFormat.INLINE_CODE, disabledFormats, activeIconTint, activeIconBgColor, normalIconTint, disabledIconTint)
        updateButtonState(binding.ivFormatCodeBlock, binding.cardCodeBlock, RichTextFormat.CODE_BLOCK, disabledFormats, activeIconTint, activeIconBgColor, normalIconTint, disabledIconTint)
        updateButtonState(binding.ivFormatLink, binding.cardLink, RichTextFormat.LINK, disabledFormats, activeIconTint, activeIconBgColor, normalIconTint, disabledIconTint)
        updateButtonState(binding.ivFormatBulletList, binding.cardBulletList, RichTextFormat.BULLET_LIST, disabledFormats, activeIconTint, activeIconBgColor, normalIconTint, disabledIconTint)
        updateButtonState(binding.ivFormatOrderedList, binding.cardOrderedList, RichTextFormat.ORDERED_LIST, disabledFormats, activeIconTint, activeIconBgColor, normalIconTint, disabledIconTint)
        updateButtonState(binding.ivFormatBlockquote, binding.cardBlockquote, RichTextFormat.BLOCKQUOTE, disabledFormats, activeIconTint, activeIconBgColor, normalIconTint, disabledIconTint)

        // Also update multiline toolbar buttons if populated
        for ((format, pair) in multilineToolbarFormatMap) {
            updateButtonState(pair.first, pair.second, format, disabledFormats, activeIconTint, activeIconBgColor, normalIconTint, disabledIconTint)
        }
    }

    /**
     * Updates the visual state of a single toolbar button with three-state feedback.
     *
     * - Active: BackgroundColor4 background on card wrapper, TextColorPrimary icon tint, full opacity
     * - Normal: transparent background, IconTintSecondary icon tint, full opacity
     * - Disabled: transparent background, IconTintTertiary icon tint, 0.4 alpha, rejects taps
     *
     * @param button The ImageButton to update
     * @param cardWrapper The MaterialCardView wrapping the button
     * @param format The RichTextFormat this button represents
     * @param disabledFormats Set of currently disabled formats
     * @param activeIconTint Tint color for active state icons
     * @param activeIconBgColor Background color for active state card
     * @param normalIconTint Tint color for normal state icons
     * @param disabledIconTint Tint color for disabled state icons
     */
    private fun updateButtonState(
        button: View,
        cardWrapper: MaterialCardView,
        format: RichTextFormat,
        disabledFormats: Set<RichTextFormat>,
        @ColorInt activeIconTint: Int,
        @ColorInt activeIconBgColor: Int,
        @ColorInt normalIconTint: Int,
        @ColorInt disabledIconTint: Int
    ) {
        val isActive = format in activeFormats
        val isDisabled = format in disabledFormats
        val imageButton = button as? android.widget.ImageView ?: return

        imageButton.isSelected = isActive
        imageButton.isEnabled = !isDisabled

        when {
            isDisabled -> {
                // Disabled state: 0.4 alpha, IconTintTertiary tint, transparent bg, reject taps
                imageButton.setColorFilter(disabledIconTint, android.graphics.PorterDuff.Mode.SRC_IN)
                cardWrapper.setCardBackgroundColor(0)
                imageButton.alpha = 0.4f
            }
            isActive -> {
                // Active state: BackgroundColor4 bg, TextColorPrimary tint, full opacity
                imageButton.setColorFilter(activeIconTint, android.graphics.PorterDuff.Mode.SRC_IN)
                cardWrapper.setCardBackgroundColor(activeIconBgColor)
                imageButton.alpha = 1.0f
            }
            else -> {
                // Normal state: transparent bg, IconTintSecondary tint, full opacity
                imageButton.setColorFilter(normalIconTint, android.graphics.PorterDuff.Mode.SRC_IN)
                cardWrapper.setCardBackgroundColor(0)
                imageButton.alpha = 1.0f
            }
        }
    }

    /**
     * Applies a rich text format to the selected text.
     * @deprecated Use toggleFormat instead which uses RichTextEditorController
     */
    @Deprecated("Use toggleFormat instead", ReplaceWith("toggleFormat(formatType)"))
    private fun applyFormat(formatType: RichTextFormat) {
        toggleFormat(formatType)
    }

    /**
     * Scans pasted text for markdown link patterns `[text](url)` and converts
     * them to [LinkFormatSpan] instances. The markdown markers are removed and
     * replaced with just the display text, with the link span applied.
     */
    private fun convertPastedMarkdownLinks(editable: android.text.Editable, start: Int, end: Int) {
        val linkPattern = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")
        val text = editable.subSequence(start, end).toString()
        var offset = 0 // tracks cumulative shift from deletions

        for (match in linkPattern.findAll(text)) {
            val matchStart = start + match.range.first - offset
            val matchEnd = start + match.range.last + 1 - offset
            val displayText = match.groupValues[1]
            val url = match.groupValues[2]

            if (matchStart < 0 || matchEnd > editable.length) continue

            isApplyingRichTextStyling = true
            try {
                // Replace [text](url) with just text
                editable.replace(matchStart, matchEnd, displayText)
                val spanEnd = matchStart + displayText.length
                // Apply link span
                RichTextSpanManager.applyLinkFormat(editable, matchStart, spanEnd, url, context)
            } finally {
                isApplyingRichTextStyling = false
            }

            // Track how many characters were removed
            offset += (match.value.length - displayText.length)
        }

        if (offset > 0) {
            // Sync compose text after modifying the editable
            viewModel?.setComposeText(editable.toString())
        }
    }

    // ── Markdown syntax auto-trigger handlers ───────────────────────────

    /**
     * Handles auto-triggering of code formats when a backtick is typed.
     *
     * - Triple backticks (```) at line start → removes markers, inserts ZWS placeholder,
     *   applies [CodeBlockFormatSpan], enables CODE_BLOCK pending format.
     * - Single backtick pair (`text`) → removes backticks, applies [InlineCodeFormatSpan].
     */
    private fun handleBacktickAutoTrigger(editable: Editable, backtickPosition: Int) {
        if (formatSpanWatcher == null) return

        val text = editable.toString()

        // ── Triple backticks → CODE_BLOCK ──────────────────────────────
        if (backtickPosition >= 2 &&
            text[backtickPosition - 2] == '`' &&
            text[backtickPosition - 1] == '`' &&
            text[backtickPosition] == '`'
        ) {
            val isValidStart = backtickPosition == 2 ||
                (backtickPosition >= 3 && (text[backtickPosition - 3] == '\n' || text[backtickPosition - 3] == ' '))

            if (isValidStart) {
                val markerStart = backtickPosition - 2
                editable.delete(markerStart, backtickPosition + 1)

                // Clean up the current line
                val lineStart = findLineStart(editable, markerStart)
                val lineEnd = findLineEnd(editable, markerStart)
                val spanCheckEnd = maxOf(lineEnd, lineStart + 1)

                // Remove list formats
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, RichTextFormat.BULLET_LIST)
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, RichTextFormat.ORDERED_LIST)

                // Disable inline and list pending formats
                formatSpanWatcher?.disableFormat(RichTextFormat.BULLET_LIST)
                formatSpanWatcher?.disableFormat(RichTextFormat.ORDERED_LIST)
                for (fmt in listOf(
                    RichTextFormat.BOLD, RichTextFormat.ITALIC, RichTextFormat.UNDERLINE,
                    RichTextFormat.STRIKETHROUGH, RichTextFormat.INLINE_CODE, RichTextFormat.LINK
                )) {
                    formatSpanWatcher?.disableFormatWithSpanUpdate(editable, fmt, markerStart)
                    RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, fmt)
                }

                // Insert ZWS placeholder and apply code block span
                val insertPos = markerStart
                val placeholder = "\u200B"
                editable.insert(insertPos, placeholder)

                val span = CodeBlockFormatSpan(context)
                editable.setSpan(span, insertPos, insertPos + placeholder.length, android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                editable.setSpan(
                    android.text.style.TypefaceSpan("monospace"),
                    insertPos, insertPos + placeholder.length,
                    android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )

                formatSpanWatcher?.enableFormat(RichTextFormat.CODE_BLOCK)
                binding.etMessageInput.setSelection(insertPos + placeholder.length)
                viewModel?.setComposeText(editable.toString())
                updateActiveFormatsFromCursor()
                return
            }
        }

        // ── Inline code: `text` ────────────────────────────────────────
        if (backtickPosition >= 2) {
            var openingBacktickPos = -1
            for (i in (backtickPosition - 1) downTo 0) {
                val c = text[i]
                if (c == '`') {
                    if (i < backtickPosition - 1) openingBacktickPos = i
                    break
                }
                if (c == '\n') break
            }

            if (openingBacktickPos >= 0) {
                val contentStart = openingBacktickPos + 1
                val contentEnd = backtickPosition
                val content = text.substring(contentStart, contentEnd)

                if (content.isNotBlank()) {
                    // Remove closing backtick first (higher index)
                    editable.delete(backtickPosition, backtickPosition + 1)
                    // Remove opening backtick
                    editable.delete(openingBacktickPos, openingBacktickPos + 1)

                    val spanStart = openingBacktickPos
                    val spanEnd = openingBacktickPos + content.length

                    val span = InlineCodeFormatSpan(context)
                    editable.setSpan(span, spanStart, spanEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    binding.etMessageInput.setSelection(spanEnd)
                    viewModel?.setComposeText(editable.toString())
                    updateActiveFormatsFromCursor()
                }
            }
        }
    }

    /**
     * Handles auto-triggering of list and blockquote formats when a space is typed
     * after a recognized line-start marker.
     *
     * - "- " → [BulletListFormatSpan]
     * - "N. " → [NumberedListFormatSpan] with parsed number
     * - "> " → [BlockquoteFormatSpan]
     */
    private fun handleListSyntaxAutoTrigger(editable: Editable, spacePosition: Int) {
        if (formatSpanWatcher == null) return

        val lineStart = findLineStart(editable, spacePosition)
        val prefix = editable.subSequence(lineStart, spacePosition).toString()

        // ── Bullet list: "-" ───────────────────────────────────────────
        if (prefix == "-") {
            val existing = editable.getSpans(lineStart, spacePosition + 1, BulletListFormatSpan::class.java)
            if (existing.isNotEmpty()) return

            editable.delete(lineStart, spacePosition + 1)

            var lineEnd = findLineEnd(editable, lineStart)
            if (lineEnd == lineStart) {
                editable.insert(lineStart, "\u200B")
                lineEnd = lineStart + 1
            }

            val span = BulletListFormatSpan(context)
            editable.setSpan(span, lineStart, maxOf(lineEnd, lineStart + 1), android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            formatSpanWatcher?.clearExplicitlyDisabled(RichTextFormat.BULLET_LIST)
            formatSpanWatcher?.enableFormat(RichTextFormat.BULLET_LIST)

            binding.etMessageInput.setSelection(maxOf(lineEnd, lineStart + 1))
            viewModel?.setComposeText(editable.toString())
            updateActiveFormatsFromCursor()
            return
        }

        // ── Numbered list: "N." ────────────────────────────────────────
        if (prefix.matches(Regex("\\d+\\."))) {
            val existing = editable.getSpans(lineStart, spacePosition + 1, NumberedListFormatSpan::class.java)
            if (existing.isNotEmpty()) return

            val number = try {
                prefix.substring(0, prefix.length - 1).toInt()
            } catch (_: NumberFormatException) {
                1
            }

            editable.delete(lineStart, spacePosition + 1)

            var lineEnd = findLineEnd(editable, lineStart)
            if (lineEnd == lineStart) {
                editable.insert(lineStart, "\u200B")
                lineEnd = lineStart + 1
            }

            val span = NumberedListFormatSpan(number, context)
            editable.setSpan(span, lineStart, maxOf(lineEnd, lineStart + 1), android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            formatSpanWatcher?.clearExplicitlyDisabled(RichTextFormat.ORDERED_LIST)
            formatSpanWatcher?.enableFormat(RichTextFormat.ORDERED_LIST)

            binding.etMessageInput.setSelection(maxOf(lineEnd, lineStart + 1))
            viewModel?.setComposeText(editable.toString())
            updateActiveFormatsFromCursor()
            return
        }

        // ── Blockquote: ">" ────────────────────────────────────────────
        if (prefix == ">") {
            val existing = editable.getSpans(lineStart, spacePosition + 1, BlockquoteFormatSpan::class.java)
            if (existing.isNotEmpty()) return

            editable.delete(lineStart, spacePosition + 1)

            var lineEnd = findLineEnd(editable, lineStart)
            if (lineEnd == lineStart) {
                editable.insert(lineStart, "\u200B")
                lineEnd = lineStart + 1
            }

            val span = BlockquoteFormatSpan(context)
            editable.setSpan(span, lineStart, maxOf(lineEnd, lineStart + 1), android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            formatSpanWatcher?.enableFormat(RichTextFormat.BLOCKQUOTE)

            binding.etMessageInput.setSelection(maxOf(lineEnd, lineStart + 1))
            viewModel?.setComposeText(editable.toString())
            updateActiveFormatsFromCursor()
        }
    }

    /**
     * Handles auto-triggering of bold and strikethrough when closing markers are typed.
     *
     * - **text** (closing ** typed) → removes markers, applies [BoldFormatSpan]
     * - ~~text~~ (closing ~~ typed) → removes markers, applies [StrikethroughFormatSpan]
     */
    private fun handleInlineFormatAutoTrigger(editable: Editable, position: Int) {
        if (formatSpanWatcher == null) return

        val text = editable.toString()
        val typedChar = text[position]

        // ── Bold: **text** ─────────────────────────────────────────────
        if (typedChar == '*' && position >= 4 && text[position - 1] == '*') {
            val contentEnd = position - 1
            var openingEnd = -1
            for (i in (contentEnd - 1) downTo 1) {
                if (text[i] == '*' && text[i - 1] == '*') {
                    openingEnd = i
                    break
                }
                if (text[i] == '\n') break
            }
            if (openingEnd >= 1) {
                val openingStart = openingEnd - 1
                val content = text.substring(openingEnd + 1, contentEnd)
                if (content.isNotBlank()) {
                    // Remove closing ** (2 chars)
                    editable.delete(position - 1, position + 1)
                    // Remove opening ** (2 chars)
                    editable.delete(openingStart, openingStart + 2)
                    val spanStart = openingStart
                    val spanEnd = openingStart + content.length
                    RichTextSpanManager.applyFormat(editable, spanStart, spanEnd, RichTextFormat.BOLD, context)
                    binding.etMessageInput.setSelection(spanEnd)
                    viewModel?.setComposeText(editable.toString())
                    updateActiveFormatsFromCursor()
                    return
                }
            }
        }

        // ── Strikethrough: ~~text~~ ────────────────────────────────────
        if (typedChar == '~' && position >= 4 && text[position - 1] == '~') {
            val contentEnd = position - 1
            var openingEnd = -1
            for (i in (contentEnd - 1) downTo 1) {
                if (text[i] == '~' && text[i - 1] == '~') {
                    openingEnd = i
                    break
                }
                if (text[i] == '\n') break
            }
            if (openingEnd >= 1) {
                val openingStart = openingEnd - 1
                val content = text.substring(openingEnd + 1, contentEnd)
                if (content.isNotBlank()) {
                    editable.delete(position - 1, position + 1)
                    editable.delete(openingStart, openingStart + 2)
                    val spanStart = openingStart
                    val spanEnd = openingStart + content.length
                    RichTextSpanManager.applyFormat(editable, spanStart, spanEnd, RichTextFormat.STRIKETHROUGH, context)
                    binding.etMessageInput.setSelection(spanEnd)
                    viewModel?.setComposeText(editable.toString())
                    updateActiveFormatsFromCursor()
                    return
                }
            }
        }
    }

    /**
     * Handles auto-triggering of italic when the closing _ is typed.
     *
     * Separated from [handleInlineFormatAutoTrigger] because _ is also used
     * within words (e.g. variable_name).
     *
     * - _text_ (closing _ typed) → removes markers, applies [ItalicFormatSpan]
     */
    private fun handleItalicAutoTrigger(editable: Editable, position: Int) {
        if (formatSpanWatcher == null) return
        if (position < 2) return

        val text = editable.toString()
        val contentEnd = position
        var openingPos = -1
        for (i in (contentEnd - 1) downTo 0) {
            if (text[i] == '_') {
                openingPos = i
                break
            }
            if (text[i] == '\n') break
        }
        if (openingPos >= 0 && openingPos < contentEnd) {
            val content = text.substring(openingPos + 1, contentEnd)
            if (content.isNotBlank()) {
                // Remove closing _
                editable.delete(contentEnd, contentEnd + 1)
                // Remove opening _
                editable.delete(openingPos, openingPos + 1)
                val spanStart = openingPos
                val spanEnd = openingPos + content.length
                RichTextSpanManager.applyFormat(editable, spanStart, spanEnd, RichTextFormat.ITALIC, context)
                binding.etMessageInput.setSelection(spanEnd)
                viewModel?.setComposeText(editable.toString())
                updateActiveFormatsFromCursor()
            }
        }
    }

    /**
     * Handles auto-triggering of underline when the closing > completes </u>.
     *
     * - <u>text</u> (closing > typed completing </u>) → removes tags,
     *   applies [UnderlineFormatSpan]
     */
    private fun handleUnderlineAutoTrigger(editable: Editable, position: Int) {
        if (formatSpanWatcher == null) return
        if (position < 3) return

        val text = editable.toString()

        // Check for closing </u> — 4 chars ending at position
        val closingStart = position - 3
        if (closingStart < 0) return
        val possibleClose = text.substring(closingStart, position + 1)
        if (possibleClose != "</u>") return

        // Search backwards for opening <u>
        val openingTag = "<u>"
        val searchEnd = closingStart
        val openingPos = text.lastIndexOf(openingTag, searchEnd - 1)
        if (openingPos < 0) return

        // Don't cross newlines
        val between = text.substring(openingPos, closingStart)
        if (between.contains('\n')) return

        val contentStart = openingPos + openingTag.length
        val contentEnd = closingStart
        val content = text.substring(contentStart, contentEnd)
        if (content.isBlank()) return

        // Remove closing </u> (4 chars)
        editable.delete(closingStart, position + 1)
        // Remove opening <u> (3 chars)
        editable.delete(openingPos, openingPos + openingTag.length)

        val spanStart = openingPos
        val spanEnd = openingPos + content.length
        RichTextSpanManager.applyFormat(editable, spanStart, spanEnd, RichTextFormat.UNDERLINE, context)
        binding.etMessageInput.setSelection(spanEnd)
        viewModel?.setComposeText(editable.toString())
        updateActiveFormatsFromCursor()
    }

    /**
     * Handles auto-triggering of link format when closing ) is typed for [text](url) syntax.
     *
     * - [text](url) (closing ) typed) → removes markdown, applies [LinkFormatSpan] with URL
     */
    private fun handleLinkAutoTrigger(editable: Editable, position: Int) {
        if (formatSpanWatcher == null) return
        if (position < 4) return // minimum: [x](y) = 6 chars

        val text = editable.toString()

        // Find the opening ( for the URL
        var urlOpenParen = -1
        for (i in (position - 1) downTo 0) {
            if (text[i] == '(') {
                urlOpenParen = i
                break
            }
            if (text[i] == '\n') return
        }
        if (urlOpenParen < 0) return

        // Check that ]( is right before the URL
        if (urlOpenParen < 1 || text[urlOpenParen - 1] != ']') return
        val closeBracket = urlOpenParen - 1

        // Find the opening [
        var openBracket = -1
        for (i in (closeBracket - 1) downTo 0) {
            if (text[i] == '[') {
                openBracket = i
                break
            }
            if (text[i] == '\n') return
        }
        if (openBracket < 0) return

        val linkText = text.substring(openBracket + 1, closeBracket)
        val url = text.substring(urlOpenParen + 1, position)
        if (linkText.isBlank() || url.isBlank()) return

        // Remove entire [text](url) and replace with just the link text
        editable.delete(openBracket, position + 1)
        editable.insert(openBracket, linkText)

        val spanStart = openBracket
        val spanEnd = openBracket + linkText.length
        RichTextSpanManager.applyLinkFormat(editable, spanStart, spanEnd, url, context)

        binding.etMessageInput.setSelection(spanEnd)
        viewModel?.setComposeText(editable.toString())
        updateActiveFormatsFromCursor()
    }

    /**
     * Shows a dialog prompting the user to enter a URL for link formatting.
     *
     * When the user confirms with a non-empty URL, a [LinkFormatSpan] is applied
     * to the currently selected text range via [RichTextSpanManager]. The toolbar
     * active/disabled state is updated accordingly.
     *
     * Handles the link toolbar button click. Determines the appropriate dialog
     * to show based on the current selection state:
     * - Selection with existing LinkFormatSpan → Edit Link dialog
     * - Selection without a link → Add Link dialog (pre-filled with selected text)
     * - No selection → Add Link dialog (empty text field)
     *
     * @see LinkFormatSpan
     * @see RichTextSpanManager
     */
    private fun showLinkDialog() {
        if (RichTextFormat.LINK in disabledFormats) return

        val editable = binding.etMessageInput.text ?: return
        val selStart = binding.etMessageInput.selectionStart
        val selEnd = binding.etMessageInput.selectionEnd

        if (selStart != selEnd) {
            // Has selection — check if it already has a LinkFormatSpan
            val existingUrl = RichTextSpanManager.getLinkUrl(editable, selStart, selEnd)
            if (existingUrl != null) {
                // Selection has a link → show Edit Link dialog
                val selectedText = editable.subSequence(selStart, selEnd).toString()
                showEditLinkDialog(selectedText, existingUrl, selStart, selEnd)
            } else {
                // Selection without a link → show Add Link dialog pre-filled with selected text
                val selectedText = editable.subSequence(selStart, selEnd).toString()
                showAddLinkDialog(selectedText, "", selStart, selEnd)
            }
        } else {
            // No selection → show Add Link dialog with empty fields
            showAddLinkDialog("", "", selStart, selEnd)
        }
    }

    /**
     * Shows the Add Link dialog using the styled MaterialCardView layout.
     * Allows the user to enter display text and a URL. On save, applies a
     * [LinkFormatSpan] to the text range.
     *
     * @param initialText Pre-filled text for the text input field.
     * @param initialUrl  Pre-filled URL for the link input field.
     * @param selStart    The selection start index in the editable.
     * @param selEnd      The selection end index in the editable.
     */
    private fun showAddLinkDialog(
        initialText: String,
        initialUrl: String,
        selStart: Int,
        selEnd: Int
    ) {
        showAddLinkDialog(
            initialText = initialText,
            initialUrl = initialUrl,
            selStart = selStart,
            selEnd = selEnd,
            isEditMode = false
        )
    }

    /**
     * Internal implementation of the Add/Edit Link dialog. Uses the
     * `cometchat_dialog_add_link.xml` layout with a transparent window
     * background so the MaterialCardView shape is visible.
     *
     * @param initialText Pre-filled text for the text input field.
     * @param initialUrl  Pre-filled URL for the link input field.
     * @param selStart    The selection start index in the editable.
     * @param selEnd      The selection end index in the editable.
     * @param isEditMode  If true, the dialog title shows "Edit Link" instead of "Add Link".
     */
    private fun showAddLinkDialog(
        initialText: String,
        initialUrl: String,
        selStart: Int,
        selEnd: Int,
        isEditMode: Boolean
    ) {
        val dialog = android.app.Dialog(context)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.cometchat_dialog_add_link)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val titleView = dialog.findViewById<android.widget.TextView>(R.id.cometchat_dialog_title)
        val closeButton = dialog.findViewById<android.widget.ImageButton>(R.id.cometchat_close_button)
        val textInput = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.cometchat_text_input)
        val linkInput = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.cometchat_link_input)
        val cancelButton = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.cometchat_cancel_button)
        val saveButton = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.cometchat_save_button)

        titleView.text = context.getString(
            if (isEditMode) R.string.cometchat_edit_link else R.string.cometchat_add_link
        )
        textInput.setText(initialText)
        linkInput.setText(initialUrl)

        closeButton.setOnClickListener { dialog.dismiss() }
        cancelButton.setOnClickListener { dialog.dismiss() }

        saveButton.setOnClickListener {
            val text = textInput.text?.toString()?.trim().orEmpty()
            val url = linkInput.text?.toString()?.trim().orEmpty()
            if (url.isNotEmpty()) {
                val editable = binding.etMessageInput.text ?: return@setOnClickListener
                if (text.isNotEmpty() && selStart != selEnd) {
                    // Replace selected text with the new text and apply link
                    editable.replace(selStart, selEnd, text)
                    val newEnd = selStart + text.length
                    RichTextSpanManager.applyLinkFormat(editable, selStart, newEnd, url, context)
                } else if (text.isNotEmpty()) {
                    // No selection — insert text at cursor and apply link
                    editable.insert(selStart, text)
                    val newEnd = selStart + text.length
                    RichTextSpanManager.applyLinkFormat(editable, selStart, newEnd, url, context)
                } else if (selStart != selEnd) {
                    // Text field empty but has selection — apply link to existing selection
                    RichTextSpanManager.applyLinkFormat(editable, selStart, selEnd, url, context)
                }
                updateActiveFormatsFromCursor()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Shows the Edit Link dialog when the user clicks on an existing link.
     * Displays the URL and provides Edit and Remove buttons.
     *
     * @param text     The display text of the link.
     * @param url      The URL of the existing link.
     * @param selStart The start index of the link span.
     * @param selEnd   The end index of the link span.
     */
    private fun showEditLinkDialog(
        text: String,
        url: String,
        selStart: Int,
        selEnd: Int
    ) {
        val dialog = android.app.Dialog(context)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.cometchat_dialog_edit_link)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val linkUrlView = dialog.findViewById<android.widget.TextView>(R.id.cometchat_link_url)
        val editButton = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.cometchat_edit_button)
        val removeButton = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.cometchat_remove_button)

        linkUrlView.text = url
        linkUrlView.setOnClickListener {
            try {
                var urlToOpen = url
                if (!urlToOpen.startsWith("http://") && !urlToOpen.startsWith("https://")) {
                    urlToOpen = "https://$urlToOpen"
                }
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(urlToOpen))
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (_: Exception) {
                // Silently fail if URL cannot be opened
            }
        }

        editButton.setOnClickListener {
            dialog.dismiss()
            showAddLinkDialog(
                initialText = text,
                initialUrl = url,
                selStart = selStart,
                selEnd = selEnd,
                isEditMode = true
            )
        }

        removeButton.setOnClickListener {
            val editable = binding.etMessageInput.text ?: return@setOnClickListener
            // Remove the LinkFormatSpan but keep the text
            RichTextSpanManager.removeFormat(editable, selStart, selEnd, RichTextFormat.LINK)
            updateActiveFormatsFromCursor()
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Sets up touch-based link click detection on the EditText.
     * When the user taps on text that has a [LinkFormatSpan], the Edit Link
     * dialog is shown instead of the default click behavior.
     */
    private fun setupLinkClickDetection() {
        binding.etMessageInput.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val editText = v as android.widget.EditText
                val editable = editText.text ?: return@setOnTouchListener false

                val x = event.x.toInt() - editText.totalPaddingLeft + editText.scrollX
                val y = event.y.toInt() - editText.totalPaddingTop + editText.scrollY

                val layout = editText.layout ?: return@setOnTouchListener false
                val line = layout.getLineForVertical(y)
                val offset = layout.getOffsetForHorizontal(line, x.toFloat())

                if (offset >= 0 && offset < editable.length) {
                    val linkSpans = editable.getSpans(offset, offset, LinkFormatSpan::class.java)
                    if (linkSpans.isNotEmpty()) {
                        val span = linkSpans[0]
                        val spanStart = editable.getSpanStart(span)
                        val spanEnd = editable.getSpanEnd(span)
                        val spanText = editable.subSequence(spanStart, spanEnd).toString()
                        showEditLinkDialog(spanText, span.url, spanStart, spanEnd)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    /**
     * Toggles the attachment popup visibility.
     * Shows the popup above the attachment button with animated icon rotation.
     * Uses [isAttachmentPopupOpen] flag to track state reliably, avoiding race
     * conditions between PopupWindow's outside-touch dismiss and the button click.
     */
    private fun toggleAttachmentPopup() {
        val now = android.os.SystemClock.uptimeMillis()
        val timeSinceDismiss = now - lastAttachmentDismissTime
        android.util.Log.d(TAG, "toggleAttachmentPopup() ENTER — isAttachmentPopupOpen=$isAttachmentPopupOpen, popupIsShowing=${attachmentPopup?.isShowing()}, popup=${attachmentPopup?.hashCode()}, timeSinceDismiss=${timeSinceDismiss}ms, time=${System.currentTimeMillis()}")
        
        // If popup is logically open, just dismiss it and return.
        if (isAttachmentPopupOpen) {
            android.util.Log.d(TAG, "toggleAttachmentPopup() — FLAG IS TRUE, dismissing and returning. popupIsShowing=${attachmentPopup?.isShowing()}")
            attachmentPopup?.dismiss()
            // Flag is reset in the dismiss listener
            android.util.Log.d(TAG, "toggleAttachmentPopup() — after dismiss call, isAttachmentPopupOpen=$isAttachmentPopupOpen")
            return
        }

        // Guard against the outside-touch race condition:
        // When PopupWindow (focusable=false, outsideTouchable=true) receives an outside
        // touch on the attachment button area, it dismisses itself first (resetting our flag),
        // then ~80-100ms later the button's onClick fires. By that time isAttachmentPopupOpen
        // is already false, so the flag guard above doesn't catch it. This debounce window
        // ensures we treat that stale click as a "close" rather than a "re-open".
        if (timeSinceDismiss < DISMISS_DEBOUNCE_MS) {
            android.util.Log.d(TAG, "toggleAttachmentPopup() — DEBOUNCE: ignoring click ${timeSinceDismiss}ms after dismiss (threshold=${DISMISS_DEBOUNCE_MS}ms)")
            return
        }

        // Mark as open BEFORE showing so any re-entrant click is guarded
        isAttachmentPopupOpen = true
        android.util.Log.d(TAG, "toggleAttachmentPopup() — OPENING popup, set flag=true")
        
        // Rotate the attachment icon to indicate popup is open (45 degrees to form an X)
        binding.ivAttachment.animate()
            .rotation(45f)
            .setDuration(200)
            .start()
        
        // Create popup menu with attachment options
        attachmentPopup = CometChatPopupMenu(context).apply {
            setBackgroundColor(CometChatTheme.getBackgroundColor1(context))
            setCornerRadius(context.resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_3))
            setStrokeColor(CometChatTheme.getStrokeColorLight(context))
            setStrokeWidth(context.resources.getDimensionPixelSize(R.dimen.cometchat_1dp))
            setStartIconTint(CometChatTheme.getIconTintHighlight(context))
            setTextColor(CometChatTheme.getTextColorPrimary(context))
            
            // Set dismiss listener to rotate icon back and reset open flag
            setOnDismissListener {
                android.util.Log.d(TAG, "onDismissListener FIRED — isAttachmentPopupOpen was $isAttachmentPopupOpen, setting to false, popupIsShowing=${attachmentPopup?.isShowing()}, time=${System.currentTimeMillis()}")
                Exception("Composer dismiss listener stacktrace").also { e ->
                    android.util.Log.d(TAG, "onDismissListener stacktrace:", e)
                }
                isAttachmentPopupOpen = false
                lastAttachmentDismissTime = android.os.SystemClock.uptimeMillis()
                binding.ivAttachment.animate()
                    .rotation(0f)
                    .setDuration(200)
                    .start()
                android.util.Log.d(TAG, "onDismissListener DONE — isAttachmentPopupOpen=$isAttachmentPopupOpen, lastDismissTime=$lastAttachmentDismissTime")
            }
            
            // Get attachment options from ViewModel (filtered by visibility flags)
            val attachmentOptions = viewModel?.getDefaultAttachmentOptions(
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
                collaborativeDocumentTitle = context.getString(R.string.cometchat_collaborative_doc),
                collaborativeDocumentIcon = R.drawable.cometchat_ic_collaborative_document,
                collaborativeWhiteboardTitle = context.getString(R.string.cometchat_collaborative_whiteboard),
                collaborativeWhiteboardIcon = R.drawable.cometchat_ic_conversations_collaborative_whiteboard,
                pollTitle = context.getString(R.string.cometchat_poll),
                pollIcon = R.drawable.cometchat_ic_polls,
            ) ?: emptyList()
            
            // Convert CometChatMessageComposerAction to MenuItem
            val menuItems = attachmentOptions.map { action ->
                CometChatPopupMenu.MenuItem(
                    id = action.id,
                    name = action.title,
                    startIcon = androidx.core.content.ContextCompat.getDrawable(context, action.icon),
                    onClick = {
                        when (action.id) {
                            CometChatMessageComposerAction.ID_CAMERA -> {
                                val handled = onCameraClick?.invoke() ?: false
                                if (!handled) launchCameraWithMediaHelper()
                            }
                            CometChatMessageComposerAction.ID_IMAGE -> {
                                val handled = onImageClick?.invoke() ?: false
                                if (!handled) launchImagePickerWithMediaHelper()
                            }
                            CometChatMessageComposerAction.ID_VIDEO -> {
                                val handled = onVideoClick?.invoke() ?: false
                                if (!handled) launchVideoPickerWithMediaHelper()
                            }
                            CometChatMessageComposerAction.ID_AUDIO -> {
                                val handled = onAudioClick?.invoke() ?: false
                                if (!handled) launchAudioPickerWithMediaHelper()
                            }
                            CometChatMessageComposerAction.ID_DOCUMENT -> {
                                val handled = onDocumentClick?.invoke() ?: false
                                if (!handled) launchFilePickerWithMediaHelper()
                            }
                            CometChatMessageComposerAction.ID_POLL -> {
                                val handled = onPollClick?.invoke() ?: false
                                if (!handled) showCreatePollBottomSheet()
                            }
                            CometChatMessageComposerAction.ID_COLLABORATIVE_DOCUMENT -> {
                                val handled = onCollaborativeDocumentClick?.invoke() ?: false
                                if (!handled) {
                                    viewModel?.createCollaborativeDocument(
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
                                    viewModel?.createCollaborativeWhiteboard(
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
                                onAttachmentOptionClick?.invoke(action)
                            }
                        }
                    }
                )
            }
            
            setMenuItems(menuItems)
        }
        
        // Show popup above the attachment button
        attachmentPopup?.show(binding.ivAttachment, PopupPosition.ABOVE)
        android.util.Log.d(TAG, "toggleAttachmentPopup() EXIT — popup shown, isAttachmentPopupOpen=$isAttachmentPopupOpen, popupIsShowing=${attachmentPopup?.isShowing()}, popup=${attachmentPopup?.hashCode()}, time=${System.currentTimeMillis()}")
    }

    // Bottom sheet dialog for create poll
    private var createPollBottomSheet: BottomSheetDialog? = null

    /**
     * Shows the CometChatCreatePoll component in a bottom sheet dialog.
     * This is the default behavior when the Poll attachment option is clicked.
     * Uses the ViewModel to create the poll via the CometChat Extensions API.
     */
    private fun showCreatePollBottomSheet() {
        val activity = context as? AppCompatActivity
        if (activity == null) {
            Toast.makeText(context, "Cannot show poll dialog - requires Activity context", Toast.LENGTH_SHORT).show()
            return
        }

        val createPoll = CometChatCreatePoll(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            // Set submit click listener - use ViewModel to create poll
            setOnSubmitClickListener { question: String, options: org.json.JSONArray ->
                // Show progress
                setProgressVisibility(View.VISIBLE)
                setErrorStateVisibility(View.GONE)
                
                viewModel?.createPoll(
                    question = question,
                    options = options,
                    onSuccess = {
                        // Poll created successfully - dismiss the dialog
                        setProgressVisibility(View.GONE)
                        createPollBottomSheet?.dismiss()
                    },
                    onError = { exception ->
                        // Show error
                        setProgressVisibility(View.GONE)
                        setErrorStateVisibility(View.VISIBLE)
                        setErrorMessage(exception.message ?: context.getString(R.string.cometchat_something_went_wrong))
                        onError?.invoke(exception)
                    }
                )
            }

            // Set back click listener
            setBackClickListener {
                createPollBottomSheet?.dismiss()
            }
        }

        // Set ViewTree owners for LiveData observation
        createPoll.setViewTreeLifecycleOwner(activity)
        createPoll.setViewTreeViewModelStoreOwner(activity)
        createPoll.setViewTreeSavedStateRegistryOwner(activity)

        createPollBottomSheet = BottomSheetDialog(context).apply {
            (createPoll.parent as? ViewGroup)?.removeView(createPoll)
            setContentView(createPoll)
            
            // Set adjustResize so keyboard pushes content up (send button stays visible)
            window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            
            setOnShowListener {
                val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                if (bottomSheet != null) {
                    bottomSheet.setBackgroundResource(android.R.color.transparent)
                    val behavior = BottomSheetBehavior.from(bottomSheet)
                    // Full screen height
                    val screenHeight = context.resources.displayMetrics.heightPixels
                    behavior.peekHeight = screenHeight
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.skipCollapsed = true
                    bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    bottomSheet.requestLayout()
                }
            }
            setCancelable(true)
        }
        createPollBottomSheet?.show()
    }

    // Track which action is being performed for result handling
    private var currentAttachmentAction: String = ""

    /**
     * Picked files whose cache copy is still running (see [stageAsync]) — their tray tiles don't
     * exist yet, so the count gates must reserve these slots or another picker can be opened past
     * the attachment cap during the copy. Main-thread only ([viewScope] is a Main-dispatcher scope).
     */
    private var pendingStagingCount = 0

    /**
     * Handles the activity result from camera or file pickers.
     * Extracts the file and sends it as a media message.
     */
    private fun handleActivityResult(result: androidx.activity.result.ActivityResult) {
        try {
            // Multi-attachment mode: picker results (which may carry several uris via clipData) are
            // staged into the tray and uploaded, rather than sent immediately. Camera is single-shot
            // but is still staged so it joins the same batch.
            if (enableMultipleAttachments) {
                if (currentAttachmentAction == UIKitConstants.ComposerAction.CAMERA) {
                    val cameraFile = handleCameraResult()
                    if (cameraFile != null && cameraFile.exists()) {
                        viewModel?.stageAttachments(
                            listOf(stagedInputFromFile(cameraFile, "image/jpeg"))
                        )
                    }
                } else {
                    // The action fixes the category: file-picker picks stay `file` and
                    // audio-picker picks stay `audio` regardless of MIME; the image/video pickers
                    // derive it from the MIME type (null → per-item default).
                    val category = when (currentAttachmentAction) {
                        UIKitConstants.ComposerAction.DOCUMENT -> CometChatConstants.MESSAGE_TYPE_FILE
                        UIKitConstants.ComposerAction.AUDIO -> CometChatConstants.MESSAGE_TYPE_AUDIO
                        else -> null
                    }
                    // Pickers whose UI can't be capped (documents / audio / pre-13 galleries) are
                    // limited on the result instead: over-selection shows the limit toast
                    // immediately — before any file copying — and only the remaining slots are
                    // staged. This is the documents-picker equivalent of the photo picker's
                    // in-picker cap.
                    val uris = collectResultUris(result)
                    val remaining = remainingAttachmentSlots()
                    if (uris.size > remaining) {
                        showAttachmentLimitToast(viewModel?.maxAttachmentCount ?: 0)
                    }
                    val toStage = uris.take(remaining)
                    // Copy the files + read their metadata off the main thread: each
                    // stagedInputFromUri() copies the picked file to cache and runs a
                    // MediaMetadataRetriever, so doing it inline for a multi-file selection would
                    // freeze the UI (potential ANR). The count/size toast above already ran on the
                    // main thread before any copying.
                    stageAsync(toStage) { uri -> stagedInputFromUri(uri, category) }
                }
                return
            }

            val file: File?
            val contentType: String
            
            when (currentAttachmentAction) {
                UIKitConstants.ComposerAction.CAMERA -> {
                    file = handleCameraResult()
                    contentType = "image"
                    if (file == null || !file.exists()) {
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.cometchat_file_not_exist),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
                UIKitConstants.ComposerAction.DOCUMENT -> {
                    file = handleFileResult(result)
                    contentType = "file"
                }
                else -> {
                    file = handleOtherMediaResult(result)
                    contentType = result.data?.data?.let { uri ->
                        MediaUtils.getContentType(context, uri)
                    } ?: "file"
                }
            }
            
            file?.let {
                viewModel?.sendMediaMessage(it, contentType)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error handling activity result: ${e.message}")
            onError?.invoke(CometChatException("MEDIA_SELECTION_ERROR", e.message ?: "Media selection failed"))
        } finally {
            currentAttachmentAction = ""
        }
    }

    /** Collects all selected uris from a picker result (multi-select via clipData, else single). */
    private fun collectResultUris(result: androidx.activity.result.ActivityResult): List<Uri> {
        val data = result.data ?: return emptyList()
        val clip = data.clipData
        return if (clip != null) {
            (0 until clip.itemCount).mapNotNull { clip.getItemAt(it).uri }
        } else {
            data.data?.let { listOf(it) } ?: emptyList()
        }
    }

    /**
     * Builds a [StagedAttachmentInput] from a picked content uri, copying it to a real file.
     * [category] fixes the tile/send category regardless of MIME (file / audio pickers); null
     * derives it from the MIME type (image / video pickers).
     */
    private fun stagedInputFromUri(uri: Uri, category: String? = null): StagedAttachmentInput? {
        val file = MediaUtils.getRealPath(context, uri, false) ?: return null
        if (!file.exists()) return null
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
        return StagedAttachmentInput(
            file = file,
            name = file.name,
            size = file.length(),
            mimeType = mime,
            category = category ?: defaultAttachmentCategory(mime),
            source = AttachmentSource.PICKER,
            // Use the copied file path for the preview: the picker's content-uri read grant may not
            // survive, whereas the cached file is always readable.
            localUri = file.absolutePath,
            // Duration label (video/audio) — read once at staging so the audio tile can show it
            // and it can be stored in the sent message metadata.
            durationMillis = extractMediaDurationMillis(file.absolutePath, mime)
        )
    }

    /** Builds a [StagedAttachmentInput] from an on-disk file (e.g. a camera capture). */
    private fun stagedInputFromFile(file: File, mimeType: String): StagedAttachmentInput =
        StagedAttachmentInput(
            file = file,
            name = file.name,
            size = file.length(),
            mimeType = mimeType,
            source = AttachmentSource.PICKER,
            localUri = file.absolutePath,
            durationMillis = extractMediaDurationMillis(file.absolutePath, mimeType)
        )
    
    /**
     * Handles the result when the action was to open the camera.
     */
    private fun handleCameraResult(): File? {
        return if (android.os.Build.VERSION.SDK_INT >= 29) {
            MediaUtils.uri?.let { MediaUtils.getRealPath(context, it, false) }
        } else {
            MediaUtils.pictureImagePath?.let { File(it) }
        }
    }
    
    /**
     * Handles the result when the action was to select a file.
     */
    private fun handleFileResult(result: androidx.activity.result.ActivityResult): File? {
        return result.data?.data?.let { uri ->
            MediaUtils.getRealPath(context, uri, false)
        }
    }
    
    /**
     * Handles the result for other types of media actions (image, video, audio).
     */
    private fun handleOtherMediaResult(result: androidx.activity.result.ActivityResult): File? {
        return result.data?.data?.let { uri ->
            MediaUtils.getRealPath(context, uri, false)
        }
    }

    /**
     * Launches the camera with permission handling.
     * Uses CometChatPermissionHandler for both permission requests and activity results.
     */
    private fun launchCameraWithMediaHelper() {
        if (!canOpenAttachmentPicker()) return
        currentAttachmentAction = UIKitConstants.ComposerAction.CAMERA
        
        CometChatPermissionHandler.withContext(context)
            .withPermissions(CometChatPermissionHandler.getPermissionsForType(PermissionType.CAMERA))
            .withListener(object : PermissionResultListener {
                override fun permissionResult(granted: List<String>, denied: List<String>) {
                    if (denied.isEmpty()) {
                        CometChatPermissionHandler.withContext(context)
                            .registerListener { result ->
                                if (result.resultCode == android.app.Activity.RESULT_OK) {
                                    handleActivityResult(result)
                                } else {
                                    currentAttachmentAction = ""
                                }
                            }
                            .withIntent(MediaUtils.openCamera(context))
                            .launch()
                    } else {
                        currentAttachmentAction = ""
                    }
                }
            })
            .check()
    }

    /**
     * Launches the image picker with permission handling.
     * Uses CometChatPermissionHandler for both permission requests and activity results.
     */
    private fun launchImagePickerWithMediaHelper() {
        if (!canOpenAttachmentPicker()) return
        currentAttachmentAction = UIKitConstants.ComposerAction.IMAGE
        
        // On Android 13+, no storage permission needed for picker
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            launchImagePickerIntent()
        } else {
            CometChatPermissionHandler.withContext(context)
                .withPermissions(CometChatPermissionHandler.getPermissionsForType(PermissionType.STORAGE))
                .withListener(object : PermissionResultListener {
                    override fun permissionResult(granted: List<String>, denied: List<String>) {
                        if (denied.isEmpty()) {
                            launchImagePickerIntent()
                        } else {
                            currentAttachmentAction = ""
                        }
                    }
                })
                .check()
        }
    }
    
    private fun launchImagePickerIntent() {
        CometChatPermissionHandler.withContext(context)
            .registerListener { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    handleActivityResult(result)
                } else {
                    currentAttachmentAction = ""
                }
            }
            .withIntent(
                MediaUtils.openImagePicker(context, enableMultipleAttachments, remainingAttachmentSlots())
            )
            .launch()
    }

    /**
     * Tray slots still available for new attachments — the selection cap handed to the pickers.
     * With 2 attachments already staged out of 10, only 8 more can be picked. Counts both the
     * staged tiles and the picks still being copied ([pendingStagingCount]).
     */
    private fun remainingAttachmentSlots(): Int {
        val vm = viewModel ?: return Int.MAX_VALUE
        return (vm.maxAttachmentCount - vm.attachmentTiles.value.size - pendingStagingCount)
            .coerceAtLeast(0)
    }

    /**
     * Copies [uris] to cache + reads their metadata on IO, then stages the results into the tray.
     * The slots are reserved via [pendingStagingCount] BEFORE the copy starts: tiles only appear
     * once `stageAttachments` runs, and for a multi-file pick the copy takes long enough that an
     * unreserved gap lets the user open another picker past the attachment cap.
     */
    private fun stageAsync(uris: List<Uri>, toInput: (Uri) -> StagedAttachmentInput?) {
        val scope = viewScope
        if (uris.isEmpty() || scope == null) return
        pendingStagingCount += uris.size
        scope.launch {
            try {
                val inputs = withContext(Dispatchers.IO) {
                    uris.mapNotNull { uri -> toInput(uri) }
                }
                if (inputs.isNotEmpty()) viewModel?.stageAttachments(inputs)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error staging attachments: ${e.message}")
                onError?.invoke(CometChatException("MEDIA_SELECTION_ERROR", e.message ?: "Media selection failed"))
            } finally {
                // Release the reservation in the same main-thread hop that staged the tiles (or on
                // failure/cancellation), so the count moves from pending to tiles without a gap.
                pendingStagingCount -= uris.size
            }
        }
    }

    /**
     * The documents / audio picker UI can't be capped like the photo picker, so the selection
     * limit is surfaced as the picker opens instead: a toast with the remaining slot count renders
     * on top of the opening picker. Shown only when part of the cap is already used — a fresh tray
     * needs no warning. Over-selection is still trimmed (with the limit toast) on return.
     */
    private fun toastRemainingSlotsHint() {
        if (!enableMultipleAttachments) return
        val vm = viewModel ?: return
        val remaining = remainingAttachmentSlots()
        if (remaining in 1 until vm.maxAttachmentCount) {
            android.widget.Toast.makeText(
                context,
                context.getString(R.string.cometchat_attachment_remaining_slots, remaining),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Launches the video picker with permission handling.
     * Uses CometChatPermissionHandler for both permission requests and activity results.
     */
    private fun launchVideoPickerWithMediaHelper() {
        if (!canOpenAttachmentPicker()) return
        currentAttachmentAction = UIKitConstants.ComposerAction.VIDEO
        
        // On Android 13+, no storage permission needed for picker
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            launchVideoPickerIntent()
        } else {
            CometChatPermissionHandler.withContext(context)
                .withPermissions(CometChatPermissionHandler.getPermissionsForType(PermissionType.STORAGE))
                .withListener(object : PermissionResultListener {
                    override fun permissionResult(granted: List<String>, denied: List<String>) {
                        if (denied.isEmpty()) {
                            launchVideoPickerIntent()
                        } else {
                            currentAttachmentAction = ""
                        }
                    }
                })
                .check()
        }
    }
    
    private fun launchVideoPickerIntent() {
        CometChatPermissionHandler.withContext(context)
            .registerListener { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    handleActivityResult(result)
                } else {
                    currentAttachmentAction = ""
                }
            }
            .withIntent(
                MediaUtils.openVideoPicker(context, enableMultipleAttachments, remainingAttachmentSlots())
            )
            .launch()
    }

    /**
     * Launches the audio picker with permission handling.
     * Uses CometChatPermissionHandler for both permission requests and activity results.
     */
    private fun launchAudioPickerWithMediaHelper() {
        if (!canOpenAttachmentPicker()) return
        toastRemainingSlotsHint()
        currentAttachmentAction = UIKitConstants.ComposerAction.AUDIO
        
        // On Android 13+, no storage permission needed for picker
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            launchAudioPickerIntent()
        } else {
            CometChatPermissionHandler.withContext(context)
                .withPermissions(CometChatPermissionHandler.getPermissionsForType(PermissionType.STORAGE))
                .withListener(object : PermissionResultListener {
                    override fun permissionResult(granted: List<String>, denied: List<String>) {
                        if (denied.isEmpty()) {
                            launchAudioPickerIntent()
                        } else {
                            currentAttachmentAction = ""
                        }
                    }
                })
                .check()
        }
    }
    
    private fun launchAudioPickerIntent() {
        CometChatPermissionHandler.withContext(context)
            .registerListener { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    handleActivityResult(result)
                } else {
                    currentAttachmentAction = ""
                }
            }
            .withIntent(MediaUtils.openAudioPicker(context, enableMultipleAttachments))
            .launch()
    }

    /**
     * Launches the file picker with permission handling.
     * Uses CometChatPermissionHandler for both permission requests and activity results.
     */
    private fun launchFilePickerWithMediaHelper() {
        if (!canOpenAttachmentPicker()) return
        toastRemainingSlotsHint()
        currentAttachmentAction = UIKitConstants.ComposerAction.DOCUMENT
        
        // On Android 13+, no storage permission needed for document picker
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            launchFilePickerIntent()
        } else {
            CometChatPermissionHandler.withContext(context)
                .withPermissions(CometChatPermissionHandler.getPermissionsForType(PermissionType.STORAGE))
                .withListener(object : PermissionResultListener {
                    override fun permissionResult(granted: List<String>, denied: List<String>) {
                        if (denied.isEmpty()) {
                            launchFilePickerIntent()
                        } else {
                            currentAttachmentAction = ""
                        }
                    }
                })
                .check()
        }
    }
    
    private fun launchFilePickerIntent() {
        CometChatPermissionHandler.withContext(context)
            .registerListener { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    handleActivityResult(result)
                } else {
                    currentAttachmentAction = ""
                }
            }
            .withIntent(MediaUtils.openFilePicker(enableMultipleAttachments))
            .launch()
    }

    /**
     * Shows the inline audio recorder, replacing the compose box content.
     * Uses the new CometChatInlineAudioRecorder with six-state management.
     */
    private fun showInlineRecorder() {
        if (isInRecordingMode) return
        
        isInRecordingMode = true
        
        // Hide the compose box card
        binding.composeBoxCard.visibility = View.GONE
        
        // Create and show the new CometChatInlineAudioRecorder
        if (inlineAudioRecorder == null) {
            inlineAudioRecorder = CometChatInlineAudioRecorder(context).apply {
                // Generate a unique ID for the view
                id = View.generateViewId()
                
                // Create and set ViewModel
                val recorderViewModel = com.cometchat.uikit.core.viewmodel.CometChatInlineAudioRecorderViewModel()
                setViewModel(recorderViewModel)
                
                // Create and set RecorderManager
                val recorderManager = InlineAudioRecorderManager(context)
                setRecorderManager(recorderManager)
                
                // Set up callbacks using individual listener setters
                setOnSubmitListener { file ->
                    // Send the audio file with correct CometChat message type
                    android.util.Log.d("CometChatMessageComposer", "onSubmitListener: received file = ${file.absolutePath}, exists = ${file.exists()}, size = ${file.length()}")
                    // Recorded voice note → mark it (DD / iOS metaData["audioType"] = "voice_note").
                    viewModel?.sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_AUDIO, isVoiceNote = true)
                    hideInlineRecorder()
                }
                
                setOnCancelListener {
                    hideInlineRecorder()
                }
                
                setOnErrorListener { errorMessage ->
                    onError?.invoke(CometChatException("RECORDING_ERROR", errorMessage))
                    hideInlineRecorder()
                }
            }
        }
        
        // Add inline recorder to the ConstraintLayout with proper constraints
        val parent = binding.parentLayout
        val recorder = inlineAudioRecorder ?: return
        
        // Remove if already added
        if (recorder.parent != null) {
            (recorder.parent as? android.view.ViewGroup)?.removeView(recorder)
        }
        
        // Create ConstraintLayout params that position the recorder where composeBoxCard was
        // Constrain to infoPanelCard (the view above composeBoxCard) since composeBoxCard is GONE
        val layoutParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            // Position below infoPanelCard (same as composeBoxCard's constraint)
            topToBottom = binding.infoPanelCard.id
            startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            // Add the same margin as composeBoxCard
            topMargin = resources.getDimensionPixelSize(R.dimen.cometchat_margin_2)
        }
        
        parent.addView(recorder, layoutParams)
        
        // Update footer constraint to point to the recorder instead of composeBoxCard
        val footerParams = binding.footerViewLayout.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        footerParams?.let {
            it.topToBottom = recorder.id
            binding.footerViewLayout.layoutParams = it
        }
        
        // Start recording automatically
        recorder.startRecording()
        
        // Invoke callback
        onVoiceRecordingClick?.invoke()
    }

    /**
     * Hides the inline audio recorder and restores the compose box.
     */
    private fun hideInlineRecorder() {
        isInRecordingMode = false
        
        // Remove inline audio recorder from layout
        inlineAudioRecorder?.let { recorder ->
            (recorder.parent as? android.view.ViewGroup)?.removeView(recorder)
        }
        // Set to null so a fresh instance is created next time
        // This ensures clean state after onDetachedFromWindow cleanup
        inlineAudioRecorder = null
        
        // Also clean up old media recorder if it exists
        inlineMediaRecorder?.let { recorder ->
            (recorder.parent as? android.view.ViewGroup)?.removeView(recorder)
        }
        inlineMediaRecorder = null
        
        // Restore footer constraint to point back to composeBoxCard
        val footerParams = binding.footerViewLayout.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        footerParams?.let {
            it.topToBottom = binding.composeBoxCard.id
            binding.footerViewLayout.layoutParams = it
        }
        
        // Show compose box content again
        binding.composeBoxCard.visibility = View.VISIBLE
    }

    /**
     * Handles send button click with explicit routing for edit, reply, and normal modes.
     *
     * - Edit mode: calls [editMessage][CometChatMessageComposerViewModel.editMessage] with the
     *   updated markdown, then exits edit mode (hides preview bar, clears state).
     * - Reply mode: creates a text message that the ViewModel attaches as a quoted/parent reply,
     *   then exits reply mode (hides preview bar, clears state).
     * - Normal mode: creates and sends a standard text message.
     *
     * Requirements: 12.3, 13.3
     */

    /**
     * Attaches consumed mention metadata to a message's metadata JSONObject.
     * If the message already has metadata, the consumed mentions array is added to it;
     * otherwise a new JSONObject is created.
     *
     * Requirements: 17.1
     */
    private fun attachConsumedMentionMetadata(message: BaseMessage, consumedMentions: org.json.JSONArray?) {
        if (consumedMentions == null || consumedMentions.length() == 0) return
        val metadata = message.metadata ?: org.json.JSONObject()
        metadata.put(MentionCodeBlockHandler.CONSUMED_MENTIONS_KEY, consumedMentions)
        message.metadata = metadata
    }

    private fun handleSendClick(text: String) {
        android.util.Log.d(TAG, "handleSendClick: input text='$text'")
        android.util.Log.d(TAG, "handleSendClick: mentionHelper=${mentionHelper != null}")
        
        // Get processed text with mentions replaced by underlying format
        val processedText = mentionHelper?.getProcessedText() ?: text
        
        android.util.Log.d(TAG, "handleSendClick: processedText='$processedText'")
        android.util.Log.d(TAG, "handleSendClick: text changed=${text != processedText}")
        
        // Use span-based MarkdownConverter to convert WYSIWYG spans to markdown
        val markdownText = if (richTextConfiguration.hasAnyEnabled()) {
            val editable = binding.etMessageInput.text
            if (editable != null && editable.isNotEmpty()) {
                // Replace mention display text (@Name) with underlying format (<@uid:xxx>)
                // before converting to markdown, so the sent message contains proper mention tokens.
                val mentionSpans = editable.getSpans(0, editable.length, NonEditableSpan::class.java)
                if (mentionSpans.isNotEmpty()) {
                    // Work on a copy to avoid modifying the EditText
                    val editableCopy = android.text.SpannableStringBuilder(editable)
                    val copySpans = editableCopy.getSpans(0, editableCopy.length, NonEditableSpan::class.java)
                    // Sort by position descending to replace from end to start (avoids offset shifts)
                    val sorted = copySpans.sortedByDescending { editableCopy.getSpanStart(it) }
                    for (span in sorted) {
                        val start = editableCopy.getSpanStart(span)
                        val end = editableCopy.getSpanEnd(span)
                        val underlying = span.getSuggestionItem()?.underlyingText
                        if (start >= 0 && end > start && underlying != null) {
                            editableCopy.replace(start, end, underlying)
                        }
                    }
                    MarkdownConverter.toMarkdown(editableCopy)
                } else {
                    MarkdownConverter.toMarkdown(editable)
                }
            } else {
                processedText
            }
        } else {
            processedText
        }
        
        android.util.Log.d(TAG, "handleSendClick: markdownText='$markdownText'")

        // Extract consumed mention metadata from code blocks before sending (Req 17.1)
        val consumedMentionMetadata: org.json.JSONArray? = if (richTextConfiguration.hasAnyEnabled()) {
            binding.etMessageInput.text?.let { MentionCodeBlockHandler.extractConsumedMentionMetadata(it) }
        } else null
        
        // ── Multi-attachment send (takes precedence) ───────────────────
        // The staged attachments go out as a single media message with the current text as the
        // caption (which may be blank). The send button is already gated on all-uploaded, but we
        // guard again here defensively.
        if (hasStagedAttachments()) {
            if (viewModel?.attachmentsAllUploaded?.value == true) {
                viewModel?.sendStagedAttachments(caption = markdownText.ifBlank { null })
                onSendButtonClick?.invoke(markdownText)
                resetComposerAfterSend()
            }
            return
        }

        val editMsg = viewModel?.editMessage?.value
        when {
            // ── Edit mode ──────────────────────────────────────────────
            editMsg != null -> {
                android.util.Log.d(TAG, "handleSendClick: EDIT mode, editingMessageId=${editMsg.id}")
                // Call handlePreMessageSend on formatters before editing
                for (formatter in textFormatters) {
                    formatter.handlePreMessageSend(context, editMsg)
                }
                // Attach consumed mention metadata to the edit message (Req 17.1)
                attachConsumedMentionMetadata(editMsg, consumedMentionMetadata)
                viewModel?.editMessage(markdownText)
                // Exit edit mode: hides preview bar, clears input & editing state
                exitEditMode()
            }

            // ── Reply mode ─────────────────────────────────────────────
            replyingToMessageId != null -> {
                android.util.Log.d(TAG, "handleSendClick: REPLY mode, replyingToMessageId=$replyingToMessageId")
                // Create message — ViewModel's createTextMessage already sets parentMessageId.
                // The ViewModel's sendTextMessageWithMentions attaches the replyMessage
                // as quotedMessage so the server treats it as a threaded reply.
                val message = viewModel?.createTextMessage(markdownText)
                if (message != null) {
                    for (formatter in textFormatters) {
                        formatter.handlePreMessageSend(context, message)
                    }
                    // Attach consumed mention metadata (Req 17.1)
                    attachConsumedMentionMetadata(message, consumedMentionMetadata)
                    viewModel?.sendTextMessageWithMentions(message)
                }
                onSendButtonClick?.invoke(markdownText)
                // Exit reply mode: hides preview bar, clears reply state
                exitReplyMode()
            }

            // ── Normal mode ────────────────────────────────────────────
            else -> {
                android.util.Log.d(TAG, "handleSendClick: NORMAL mode")
                val message = viewModel?.createTextMessage(markdownText)
                if (message != null) {
                    for (formatter in textFormatters) {
                        formatter.handlePreMessageSend(context, message)
                    }
                    // Attach consumed mention metadata (Req 17.1)
                    attachConsumedMentionMetadata(message, consumedMentionMetadata)
                    viewModel?.sendTextMessageWithMentions(message)
                }
                onSendButtonClick?.invoke(markdownText)
            }
        }
        
        resetComposerAfterSend()
    }

    /**
     * Shared post-send cleanup: clears the input, resets the WYSIWYG span engine, formats,
     * mentions, and formatter state. Used by both the text-send and staged-attachment send paths.
     */
    private fun resetComposerAfterSend() {
        // Clear input and reset span engine state
        binding.etMessageInput.setText("")
        if (richTextConfiguration.hasAnyEnabled()) {
            richTextController.clear()
            // Re-attach FormatSpanWatcher to the fresh Editable after clearing
            formatSpanWatcher?.clearPendingFormats()
            formatSpanWatcher?.attachTo(binding.etMessageInput.text)
        }
        
        // Reset active/disabled formats
        activeFormats = emptySet()
        disabledFormats = emptySet()
        updateToolbarButtonStates()

        // Clear mention helper state
        mentionHelper?.clear()
        
        // Reset formatters' selected lists
        for (formatter in textFormatters) {
            formatter.setSelectedList(context, emptyList())
        }
    }

    /**
     * Sets up text watcher for the input field.
     * Integrates the span-based WYSIWYG engine: FormatSpanWatcher handles span
     * extension/pending formats, and ListContinuationHandler manages Enter key
     * behavior for lists and blockquotes.
     */
    /**
     * Accepts rich content in the composer input — an image pasted from another app, a GIF/
     * sticker/image inserted from the keyboard, or media dragged onto the input — and stages it
     * in the attachment tray exactly like a picker selection (category derived from the MIME
     * type: image/video/audio/file). Text content is returned so the platform pastes it as
     * usual. AppCompatEditText advertises [RECEIVE_CONTENT_MIME_TYPES] to the IME and handles
     * the uri permission grants for all three sources.
     */
    private fun setupReceiveContent() {
        androidx.core.view.ViewCompat.setOnReceiveContentListener(
            binding.etMessageInput,
            RECEIVE_CONTENT_MIME_TYPES
        ) { _, payload ->
            val split = payload.partition { item -> item.uri != null }
            val uriContent = split.first
            // Media staging needs the tray — in legacy single-attachment mode the uris are
            // ignored (returning them would paste raw "content://…" text).
            if (uriContent != null && enableMultipleAttachments) {
                stageReceivedContent(uriContent)
            }
            split.second
        }
    }

    /** Stages pasted/dropped content uris into the tray, honoring the attachment-count cap. */
    private fun stageReceivedContent(content: androidx.core.view.ContentInfoCompat) {
        val clip = content.clip
        val uris = (0 until clip.itemCount).mapNotNull { clip.getItemAt(it).uri }
        if (uris.isEmpty()) return
        val remaining = remainingAttachmentSlots()
        if (uris.size > remaining) {
            showAttachmentLimitToast(viewModel?.maxAttachmentCount ?: 0)
        }
        // Copy + metadata off the main thread (see stagedInputFromUri) so pasting/dropping several
        // files doesn't freeze the UI; stageAsync reserves the slots while the copy runs.
        stageAsync(uris.take(remaining)) { uri -> stagedInputFromUri(uri, null) }
    }

    private fun setupTextWatcher() {
        // Listen for cursor position changes to update mention suppression and toolbar state
        binding.etMessageInput.setOnSelectionChangedListener { selStart, selEnd ->
            val editable = binding.etMessageInput.text ?: return@setOnSelectionChangedListener
            suppressMentionDetection = isInsideCodeFormat(editable, selStart)

            // Skip toolbar updates during programmatic text modifications
            // (e.g., list continuation inserting placeholder text)
            if (isApplyingRichTextStyling) return@setOnSelectionChangedListener

            if (richTextConfiguration.hasAnyEnabled()) {
                val isTypingCursorMove = isTextChanging || selStart == lastCursorAfterTextChange
                if (!isTypingCursorMove) {
                    formatSpanWatcher?.clearPendingFormats()
                }
                updateActiveFormatsFromCursor()
            }
        }

        binding.etMessageInput.addTextChangedListener(object : TextWatcher {
            private var previousText: String = ""
            private var changeStart: Int = 0
            private var changeBefore: Int = 0
            private var changeCount: Int = 0
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isTextChanging = true

                // Skip if we're applying rich text styling to avoid recursion
                if (isApplyingRichTextStyling) return

                // Store change params for afterTextChanged
                changeStart = start
                changeBefore = before
                changeCount = count
                
                val text = s?.toString() ?: ""
                viewModel?.setComposeText(text)
                onTextChanged?.invoke(text)
                updateSendButtonState(text.isNotBlank())
                
                // Update toolbar visibility based on text presence (automatic visibility)
                updateButtonVisibility()
                
                // Update button alignment based on input field line count
                updateButtonAlignment()

                // Handle typing indicators
                if (text.isNotEmpty()) {
                    viewModel?.startTyping()
                } else {
                    viewModel?.endTyping()
                }

            }

            override fun afterTextChanged(s: Editable?) {
                // Skip if we're already applying styling to avoid infinite loop
                if (isApplyingRichTextStyling) {
                    lastCursorAfterTextChange = binding.etMessageInput.selectionStart
                    isTextChanging = false
                    return
                }
                if (s == null) {
                    lastCursorAfterTextChange = binding.etMessageInput.selectionStart
                    isTextChanging = false
                    return
                }

                // Delegate to FormatSpanWatcher for span extension and pending format application
                if (richTextConfiguration.hasAnyEnabled()) {
                    formatSpanWatcher?.handleTextChanged(s, changeStart, changeBefore, changeCount)

                    // Detect Enter key: a single newline was inserted
                    if (changeCount == 1 && changeBefore == 0) {
                        val insertPos = changeStart
                        if (insertPos < s.length && s[insertPos] == '\n') {
                            isApplyingRichTextStyling = true
                            try {
                                handleNewlineForBlockFormats(s, insertPos)
                            } finally {
                                isApplyingRichTextStyling = false
                            }
                            // Sync compose text after list continuation modified the editable,
                            // preventing the composeText flow from calling setText() with stale text
                            viewModel?.setComposeText(s.toString())
                        }
                    }

                    // Detect paste: multiple characters inserted at once — parse markdown links
                    if (changeCount > 1 && changeBefore == 0) {
                        convertPastedMarkdownLinks(s, changeStart, changeStart + changeCount)
                    }

                    // Markdown syntax auto-trigger: single character typed
                    if (changeCount == 1 && changeBefore == 0 && changeStart < s.length) {
                        val insertedChar = s[changeStart]
                        isApplyingRichTextStyling = true
                        try {
                            when (insertedChar) {
                                '`' -> handleBacktickAutoTrigger(s, changeStart)
                                ' ' -> handleListSyntaxAutoTrigger(s, changeStart)
                                '*', '~' -> handleInlineFormatAutoTrigger(s, changeStart)
                                '_' -> handleItalicAutoTrigger(s, changeStart)
                                '>' -> handleUnderlineAutoTrigger(s, changeStart)
                                ')' -> handleLinkAutoTrigger(s, changeStart)
                            }
                        } finally {
                            isApplyingRichTextStyling = false
                        }
                        viewModel?.setComposeText(s.toString())
                    }

                    // Update active formats based on current cursor position spans
                    updateActiveFormatsFromCursor()
                }

                // Record cursor position so onSelectionChanged can distinguish
                // typing-induced moves from user-initiated moves
                lastCursorAfterTextChange = binding.etMessageInput.selectionStart
                isTextChanging = false
            }
        })
    }
    
    /**
     * Handles automatic list and blockquote continuation when Enter is pressed.
     * If the cursor is at the end of a bullet list line (• item), ordered list line (1. item),
     * or blockquote line (┃ text), automatically adds the next marker.
     * 
     * @return The modified text if auto-continuation was applied, null otherwise
     */
    private fun handleListAutoContinuation(oldText: String, newText: String, editable: Editable?): String? {
        if (editable == null) return null
        
        // Check if a newline was just inserted
        if (newText.length != oldText.length + 1) return null
        
        val cursorPos = binding.etMessageInput.selectionStart
        if (cursorPos <= 0 || cursorPos > newText.length) return null
        
        // Check if the character just inserted is a newline
        if (newText.getOrNull(cursorPos - 1) != '\n') return null
        
        // Find the line before the newline
        val textBeforeNewline = newText.substring(0, cursorPos - 1)
        val lastLineStart = textBeforeNewline.lastIndexOf('\n') + 1
        val previousLine = textBeforeNewline.substring(lastLineStart)
        
        // Check for blockquote: "> text" (markdown format)
        if (richTextConfiguration.enableBlockquote && previousLine.startsWith("> ")) {
            val markerLength = 2
            val content = if (previousLine.length > markerLength) previousLine.substring(markerLength) else ""
            
            if (content.isNotBlank()) {
                // Add new quote line
                isApplyingRichTextStyling = true
                try {
                    val newTextWithQuote = newText.substring(0, cursorPos) + "> " + newText.substring(cursorPos)
                    editable.replace(0, editable.length, newTextWithQuote)
                    binding.etMessageInput.setSelection(cursorPos + markerLength)
                } finally {
                    isApplyingRichTextStyling = false
                }
                return newText
            } else {
                // Empty quote line - remove the quote marker (exit quote mode)
                isApplyingRichTextStyling = true
                try {
                    val textWithoutEmptyQuote = textBeforeNewline.substring(0, lastLineStart.coerceAtLeast(0)) + 
                        newText.substring(cursorPos)
                    editable.replace(0, editable.length, textWithoutEmptyQuote)
                    binding.etMessageInput.setSelection(lastLineStart.coerceAtLeast(0))
                } finally {
                    isApplyingRichTextStyling = false
                }
                return newText
            }
        }
        
        // Check for bullet list: "• item" (display format) or "- item" (markdown format)
        if (richTextConfiguration.enableBulletList && (previousLine.matches(Regex("^• .+$")) || previousLine.matches(Regex("^- .+$")))) {
            val bulletMarker = if (previousLine.startsWith("• ")) "• " else "- "
            val content = previousLine.substring(2)
            if (content.isNotBlank()) {
                // Add new bullet point (use same marker as previous line)
                isApplyingRichTextStyling = true
                try {
                    val newTextWithBullet = newText.substring(0, cursorPos) + bulletMarker + newText.substring(cursorPos)
                    editable.replace(0, editable.length, newTextWithBullet)
                    binding.etMessageInput.setSelection(cursorPos + 2)
                } finally {
                    isApplyingRichTextStyling = false
                }
                return newText
            } else {
                // Empty bullet line - remove the bullet marker (exit list mode)
                isApplyingRichTextStyling = true
                try {
                    val textWithoutEmptyBullet = textBeforeNewline.substring(0, lastLineStart.coerceAtLeast(0)) + 
                        newText.substring(cursorPos)
                    editable.replace(0, editable.length, textWithoutEmptyBullet)
                    binding.etMessageInput.setSelection(lastLineStart.coerceAtLeast(0))
                } finally {
                    isApplyingRichTextStyling = false
                }
                return newText
            }
        }
        
        // Check for ordered list: "1. item", "2. item", etc.
        if (richTextConfiguration.enableOrderedList && previousLine.matches(Regex("^\\d+\\. .+$"))) {
            val match = Regex("^(\\d+)\\. (.*)$").find(previousLine)
            if (match != null) {
                val currentNumber = match.groupValues[1].toIntOrNull() ?: 0
                val content = match.groupValues[2]
                
                if (content.isNotBlank()) {
                    // Add next numbered item
                    val nextNumber = currentNumber + 1
                    isApplyingRichTextStyling = true
                    try {
                        val newTextWithNumber = newText.substring(0, cursorPos) + "$nextNumber. " + newText.substring(cursorPos)
                        editable.replace(0, editable.length, newTextWithNumber)
                        binding.etMessageInput.setSelection(cursorPos + "$nextNumber. ".length)
                    } finally {
                        isApplyingRichTextStyling = false
                    }
                    return newText
                } else {
                    // Empty numbered line - remove the number marker (exit list mode)
                    isApplyingRichTextStyling = true
                    try {
                        val textWithoutEmptyNumber = textBeforeNewline.substring(0, lastLineStart.coerceAtLeast(0)) + 
                            newText.substring(cursorPos)
                        editable.replace(0, editable.length, textWithoutEmptyNumber)
                        binding.etMessageInput.setSelection(lastLineStart.coerceAtLeast(0))
                    } finally {
                        isApplyingRichTextStyling = false
                    }
                    return newText
                }
            }
        }
        
        return null
    }
    
    /**
     * Applies inline rich text styling to the EditText based on RichTextEditorController spans.
     * This shows the formatted preview directly in the input field (WYSIWYG style like Jetpack).
     */
    private fun applyInlineRichTextStyling(editable: Editable?) {
        if (editable == null) return
        
        val text = editable.toString()
        if (text.isEmpty()) return
        
        // Set flag to prevent re-entry
        isApplyingRichTextStyling = true
        
        try {
            // Remove existing formatting spans to avoid duplicates
            val existingSpans = editable.getSpans(0, editable.length, Any::class.java)
            for (span in existingSpans) {
                if (span is android.text.style.StyleSpan ||
                    span is android.text.style.StrikethroughSpan ||
                    span is android.text.style.TypefaceSpan ||
                    span is android.text.style.ForegroundColorSpan ||
                    span is android.text.style.BackgroundColorSpan ||
                    span is android.text.style.UnderlineSpan ||
                    span is android.text.style.RelativeSizeSpan) {
                    editable.removeSpan(span)
                }
            }
            
            // Apply spans from RichTextEditorController (WYSIWYG style like Jetpack)
            if (richTextConfiguration.hasAnyEnabled()) {
                for (richSpan in richTextController.state.spans) {
                    val start = richSpan.start.coerceIn(0, text.length)
                    val end = richSpan.end.coerceIn(0, text.length)
                    if (start >= end) continue
                    
                    for (format in richSpan.formats) {
                        when (format) {
                            RichTextFormat.BOLD -> {
                                editable.setSpan(
                                    android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                    start, end,
                                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                            RichTextFormat.ITALIC -> {
                                editable.setSpan(
                                    android.text.style.StyleSpan(android.graphics.Typeface.ITALIC),
                                    start, end,
                                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                            RichTextFormat.UNDERLINE -> {
                                editable.setSpan(
                                    android.text.style.UnderlineSpan(),
                                    start, end,
                                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                            RichTextFormat.STRIKETHROUGH -> {
                                editable.setSpan(
                                    android.text.style.StrikethroughSpan(),
                                    start, end,
                                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                            RichTextFormat.INLINE_CODE, RichTextFormat.CODE_BLOCK -> {
                                editable.setSpan(
                                    android.text.style.TypefaceSpan("monospace"),
                                    start, end,
                                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                editable.setSpan(
                                    android.text.style.BackgroundColorSpan(
                                        CometChatTheme.getBackgroundColor3(context)
                                    ),
                                    start, end,
                                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                            RichTextFormat.LINK -> {
                                editable.setSpan(
                                    android.text.style.ForegroundColorSpan(
                                        CometChatTheme.getPrimaryColor(context)
                                    ),
                                    start, end,
                                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                editable.setSpan(
                                    android.text.style.UnderlineSpan(),
                                    start, end,
                                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                            else -> {
                                // BULLET_LIST, ORDERED_LIST, BLOCKQUOTE are line-based,
                                // handled below via styleLinePrefixes
                            }
                        }
                    }
                }
                
                // Apply line-based format styling (bullet list, ordered list, blockquote)
                // These formats use text prefixes that need visual styling
                styleLinePrefixes(editable, text)
            }
        } finally {
            // Reset flag
            isApplyingRichTextStyling = false
        }
    }
    
    /**
     * Styles line-based format prefixes (bullet list, ordered list, blockquote).
     * Applies visual styling to markdown prefixes without modifying the text.
     * Matches Jetpack's SpanBasedVisualTransformation behavior.
     */
    private fun styleLinePrefixes(editable: Editable, text: String) {
        val lines = text.split("\n")
        var idx = 0
        val textColor = style.inputTextColor.takeIf { it != 0 } ?: CometChatTheme.getTextColorPrimary(context)
        val quoteContentColor = android.graphics.Color.parseColor("#666666")
        
        for (line in lines) {
            when {
                // Bullet list: "- " — style the marker bold with primary color
                line.startsWith("- ") && idx + 2 <= editable.length -> {
                    editable.setSpan(
                        android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        idx, idx + 2,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    editable.setSpan(
                        android.text.style.ForegroundColorSpan(textColor),
                        idx, idx + 2,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                // Ordered list: "1. " prefix — style number+dot+space bold (allows empty content)
                line.matches(Regex("^\\d+\\. .*")) || line.matches(Regex("^\\d+\\. $")) || line.matches(Regex("^\\d+\\.$")) -> {
                    val dotIdx = line.indexOf('.')
                    if (dotIdx > 0) {
                        // Style up to dot+space or just dot if no space
                        val endIdx = if (line.length > dotIdx + 1 && line[dotIdx + 1] == ' ') dotIdx + 2 else dotIdx + 1
                        if (idx + endIdx <= editable.length) {
                            editable.setSpan(
                                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                idx, idx + endIdx,
                                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            editable.setSpan(
                                android.text.style.ForegroundColorSpan(textColor),
                                idx, idx + endIdx,
                                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
                // Blockquote: "> " — style the marker bold, content in gray
                line.startsWith("> ") && idx + 2 <= editable.length -> {
                    // Style the ">" marker bold with text color
                    editable.setSpan(
                        android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        idx, idx + 2,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    editable.setSpan(
                        android.text.style.ForegroundColorSpan(textColor),
                        idx, idx + 2,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    // Style the content in gray
                    if (line.length > 2 && idx + line.length <= editable.length) {
                        editable.setSpan(
                            android.text.style.ForegroundColorSpan(quoteContentColor),
                            idx + 2, idx + line.length,
                            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
            idx += line.length + 1 // +1 for newline
        }
    }
    
    /**
     * Applies styling to text matching a pattern.
     */
    private fun applyPatternStyling(
        editable: Editable,
        text: String,
        pattern: String,
        markerLength: Int,
        applySpan: (Int, Int) -> Unit
    ) {
        try {
            val regex = java.util.regex.Pattern.compile(pattern)
            val matcher = regex.matcher(text)
            
            while (matcher.find()) {
                val contentStart = matcher.start() + markerLength
                val contentEnd = matcher.end() - markerLength
                
                if (contentEnd > contentStart && contentStart >= 0 && contentEnd <= text.length) {
                    applySpan(contentStart, contentEnd)
                }
            }
        } catch (e: Exception) {
            // Ignore pattern matching errors
        }
    }
    
    /**
     * Applies link styling to [text](url) patterns.
     */
    private fun applyLinkStyling(editable: Editable, text: String) {
        try {
            val pattern = java.util.regex.Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)")
            val matcher = pattern.matcher(text)
            
            while (matcher.find()) {
                val textStart = matcher.start() + 1 // After [
                val textEnd = matcher.end(1) // End of text part
                
                if (textEnd > textStart && textStart >= 0 && textEnd <= text.length) {
                    editable.setSpan(
                        android.text.style.ForegroundColorSpan(
                            android.graphics.Color.parseColor("#2196F3")
                        ),
                        textStart, textEnd,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    editable.setSpan(
                        android.text.style.UnderlineSpan(),
                        textStart, textEnd,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore pattern matching errors
        }
    }
    
    /**
     * Dims the markdown markers to indicate they're formatting syntax.
     */
    private fun applyMarkerDimming(editable: Editable, text: String) {
        val markerColor = android.graphics.Color.parseColor("#9E9E9E")
        
        // Dim ** markers for bold
        dimMarkers(editable, text, "\\*\\*", markerColor)
        
        // Dim _ markers for italic (but not __)
        dimSingleMarkers(editable, text, "_", markerColor)
        
        // Dim ~~ markers for strikethrough
        dimMarkers(editable, text, "~~", markerColor)
        
        // Dim ` markers for inline code
        dimSingleMarkers(editable, text, "`", markerColor)
        
        // Dim ``` markers for code blocks
        dimMarkers(editable, text, "```", markerColor)
        
        // Dim link syntax markers
        dimLinkMarkers(editable, text, markerColor)
        
        // Dim > for blockquote
        dimBlockquoteMarkers(editable, text, markerColor)
        
        // Style bullet list markers (• or -)
        dimBulletListMarkers(editable, text, markerColor)
        
        // Dim 1. for ordered list
        dimOrderedListMarkers(editable, text, markerColor)
    }
    
    /**
     * Dims double-character markers like ** or ~~.
     */
    private fun dimMarkers(editable: Editable, text: String, marker: String, color: Int) {
        try {
            var index = 0
            val escapedMarker = java.util.regex.Pattern.quote(marker)
            val pattern = java.util.regex.Pattern.compile(escapedMarker)
            val matcher = pattern.matcher(text)
            
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                if (start >= 0 && end <= text.length) {
                    editable.setSpan(
                        android.text.style.ForegroundColorSpan(color),
                        start, end,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    /**
     * Dims single-character markers like _ or `.
     */
    private fun dimSingleMarkers(editable: Editable, text: String, marker: String, color: Int) {
        try {
            val escapedMarker = java.util.regex.Pattern.quote(marker)
            val pattern = java.util.regex.Pattern.compile(escapedMarker)
            val matcher = pattern.matcher(text)
            
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                if (start >= 0 && end <= text.length) {
                    editable.setSpan(
                        android.text.style.ForegroundColorSpan(color),
                        start, end,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    /**
     * Dims link syntax markers [, ], (, ).
     */
    private fun dimLinkMarkers(editable: Editable, text: String, color: Int) {
        try {
            val pattern = java.util.regex.Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)")
            val matcher = pattern.matcher(text)
            
            while (matcher.find()) {
                val fullStart = matcher.start()
                val fullEnd = matcher.end()
                val textEnd = matcher.end(1)
                
                // Dim [
                if (fullStart >= 0 && fullStart + 1 <= text.length) {
                    editable.setSpan(
                        android.text.style.ForegroundColorSpan(color),
                        fullStart, fullStart + 1,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                
                // Dim ](
                if (textEnd >= 0 && textEnd + 2 <= text.length) {
                    editable.setSpan(
                        android.text.style.ForegroundColorSpan(color),
                        textEnd, textEnd + 2,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                
                // Dim URL and )
                val urlStart = textEnd + 2
                if (urlStart >= 0 && fullEnd <= text.length) {
                    editable.setSpan(
                        android.text.style.ForegroundColorSpan(color),
                        urlStart, fullEnd,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    /**
     * Styles blockquote markers with gray color.
     * The "> " marker is shown visible (not transparent) with gray color.
     * Content is left unstyled to avoid text wrapping issues with italic spans.
     */
    private fun dimBlockquoteMarkers(editable: Editable, text: String, color: Int) {
        try {
            val lines = text.split("\n")
            var currentIndex = 0
            // Use gray with 60% alpha to match Jetpack Compose markerColor
            val markerColor = android.graphics.Color.argb(153, 128, 128, 128) // Gray with 0.6 alpha
            
            for (line in lines) {
                val lineStart = currentIndex
                
                val isBlockquoteLine = line.startsWith("> ")
                
                if (isBlockquoteLine) {
                    val markerLength = 2 // "> "
                    
                    if (lineStart >= 0 && lineStart + markerLength <= text.length) {
                        // Style the "> " marker with gray color (visible, not transparent)
                        editable.setSpan(
                            android.text.style.ForegroundColorSpan(markerColor),
                            lineStart, lineStart + markerLength,
                            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    // Content is left unstyled to avoid text wrapping issues
                }
                
                currentIndex += line.length + 1 // +1 for newline
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    /**
     * Styles bullet list markers (• or -) at the start of lines.
     * Makes the bullet bold for better visibility.
     */
    private fun dimBulletListMarkers(editable: Editable, text: String, color: Int) {
        try {
            val lines = text.split("\n")
            var currentIndex = 0
            
            for (line in lines) {
                // Check for bullet character (•) or dash (-)
                if (line.startsWith("• ") || line.startsWith("- ")) {
                    val bulletStart = currentIndex
                    val bulletEnd = currentIndex + 1 // Just the bullet/dash character
                    
                    if (bulletStart >= 0 && bulletEnd <= text.length) {
                        // Make the bullet bold for better visibility
                        editable.setSpan(
                            android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                            bulletStart, bulletEnd,
                            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                currentIndex += line.length + 1 // +1 for newline
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    /**
     * Dims ordered list markers (1., 2., etc.) at the start of lines.
     */
    private fun dimOrderedListMarkers(editable: Editable, text: String, color: Int) {
        try {
            val lines = text.split("\n")
            var currentIndex = 0
            val regex = java.util.regex.Pattern.compile("^\\d+\\. ")
            
            for (line in lines) {
                val matcher = regex.matcher(line)
                if (matcher.find()) {
                    val markerEnd = currentIndex + matcher.end()
                    if (currentIndex >= 0 && markerEnd <= text.length) {
                        // Dim the marker
                        editable.setSpan(
                            android.text.style.ForegroundColorSpan(color),
                            currentIndex, markerEnd,
                            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        // Make the number bold
                        val numberEnd = line.indexOf('.') + currentIndex
                        if (numberEnd > currentIndex) {
                            editable.setSpan(
                                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                currentIndex, numberEnd + 1,
                                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
                currentIndex += line.length + 1 // +1 for newline
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }

    /**
     * Updates the send button state based on whether there is text.
     * Matches the Java chatuikit reference behavior:
     * - Agent active: arrow_narrow_up icon, secondaryButtonBackgroundColor, clickable
     * - Agent inactive: arrow_narrow_up icon, backgroundColor4, NOT clickable
     * - AI generating (stop): stop icon, secondaryButtonBackgroundColor, NOT clickable
     * - Normal active: send_active icon, primaryColor, clickable
     * - Normal inactive: send_active icon, inactiveBackgroundColor, NOT clickable
     */
    private fun updateSendButtonState(hasText: Boolean) {
        val isAIGenerating = viewModel?.isAIGenerating?.value ?: false
        val secondaryBgColor = CometChatTheme.getSecondaryButtonBackgroundColor(context)

        // With staged attachments the send button follows the all-or-nothing rule: it stays disabled
        // until every staged attachment has finished uploading (text becomes an optional caption).
        val hasStaged = hasStagedAttachments()
        val active = if (hasStaged) (viewModel?.attachmentsAllUploaded?.value == true) else hasText

        when {
            isAIGenerating -> {
                style.sendButtonStopIcon?.let { binding.ivSend.setImageDrawable(it) }
                    ?: binding.ivSend.setImageResource(R.drawable.cometchat_ic_stop)
                applySendButtonBackground(style.sendButtonActiveBackgroundColor)
                binding.ivSend.isClickable = false
                binding.ivSend.isEnabled = false
            }
            isAgentChat && hasText -> {
                binding.ivSend.setImageResource(R.drawable.cometchat_ic_arrow_narrow_up)
                applySendButtonBackground(style.sendButtonActiveBackgroundColor)
                binding.ivSend.isClickable = true
                binding.ivSend.isEnabled = true
            }
            isAgentChat && !hasText -> {
                binding.ivSend.setImageResource(R.drawable.cometchat_ic_arrow_narrow_up)
                applySendButtonBackground(style.sendButtonInactiveBackgroundColor)
                binding.ivSend.isClickable = false
                binding.ivSend.isEnabled = false
            }
            active -> {
                style.sendButtonActiveIcon?.let { binding.ivSend.setImageDrawable(it) }
                    ?: binding.ivSend.setImageResource(R.drawable.cometchat_ic_send_active)
                applySendButtonBackground(style.sendButtonActiveBackgroundColor)
                binding.ivSend.isClickable = true
                binding.ivSend.isEnabled = true
            }
            else -> {
                style.sendButtonActiveIcon?.let { binding.ivSend.setImageDrawable(it) }
                    ?: binding.ivSend.setImageResource(R.drawable.cometchat_ic_send_active)
                applySendButtonBackground(style.sendButtonInactiveBackgroundColor)
                binding.ivSend.isClickable = false
                binding.ivSend.isEnabled = false
            }
        }
        
        // Apply icon tint — use theme-aware color for light/dark mode support
        binding.ivSend.setColorFilter(style.sendButtonIconTint)

        // Also update multiline send button to stay in sync
        updateMultilineSendButtonState(active, isAIGenerating)
    }

    /**
     * Updates the multiline send button appearance to match the single-line send button state.
     * Uses the same icon, background color, and clickability logic.
     */
    private fun updateMultilineSendButtonState(hasText: Boolean, isAIGenerating: Boolean) {
        val multilineSendIcon = binding.ivMultilineSend ?: return
        val multilineSendCard = binding.multilineSendButtonCard ?: return

        when {
            isAIGenerating -> {
                style.sendButtonStopIcon?.let { multilineSendIcon.setImageDrawable(it) }
                    ?: multilineSendIcon.setImageResource(R.drawable.cometchat_ic_stop)
                multilineSendCard.setCardBackgroundColor(style.sendButtonActiveBackgroundColor)
                multilineSendCard.isClickable = false
                multilineSendCard.isEnabled = false
            }
            isAgentChat && hasText -> {
                multilineSendIcon.setImageResource(R.drawable.cometchat_ic_arrow_narrow_up)
                multilineSendCard.setCardBackgroundColor(style.sendButtonActiveBackgroundColor)
                multilineSendCard.isClickable = true
                multilineSendCard.isEnabled = true
            }
            isAgentChat && !hasText -> {
                multilineSendIcon.setImageResource(R.drawable.cometchat_ic_arrow_narrow_up)
                multilineSendCard.setCardBackgroundColor(style.sendButtonInactiveBackgroundColor)
                multilineSendCard.isClickable = false
                multilineSendCard.isEnabled = false
            }
            hasText -> {
                style.sendButtonActiveIcon?.let { multilineSendIcon.setImageDrawable(it) }
                    ?: multilineSendIcon.setImageResource(R.drawable.cometchat_ic_send_active)
                multilineSendCard.setCardBackgroundColor(style.sendButtonActiveBackgroundColor)
                multilineSendCard.isClickable = true
                multilineSendCard.isEnabled = true
            }
            else -> {
                style.sendButtonActiveIcon?.let { multilineSendIcon.setImageDrawable(it) }
                    ?: multilineSendIcon.setImageResource(R.drawable.cometchat_ic_send_active)
                multilineSendCard.setCardBackgroundColor(style.sendButtonInactiveBackgroundColor)
                multilineSendCard.isClickable = false
                multilineSendCard.isEnabled = false
            }
        }

        // Apply icon tint
        multilineSendIcon.setColorFilter(style.sendButtonIconTint)
    }
    
    /**
     * Applies a circular background to the send button with the specified color.
     */
    private fun applySendButtonBackground(@ColorInt color: Int) {
        val drawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(color)
        }
        binding.ivSend.background = drawable
    }

    /**
     * Initializes the ViewModel.
     */
    private fun initViewModel() {
        if (!isExternalViewModel) {
            // Scope the ViewModel to the host Activity's ViewModelStore so it is retained across
            // configuration changes (e.g. theme switch) — otherwise a fresh, empty ViewModel is
            // created on every view recreation and staged attachments are lost (ENG-37015). This
            // mirrors the Compose composer, which acquires the same ViewModel via viewModel(...).
            val storeOwner = Utils.getActivity(context) as? ViewModelStoreOwner
            viewModel = if (storeOwner != null) {
                ViewModelProvider(
                    storeOwner,
                    CometChatMessageComposerViewModelFactory()
                )[CometChatMessageComposerViewModel::class.java]
            } else {
                CometChatMessageComposerViewModelFactory()
                    .create(CometChatMessageComposerViewModel::class.java)
            }
        }
        viewModel?.disableTypingEvents = disableTypingEvents
        startCollectingFlows()
    }

    /**
     * Initializes the rich text formatter manager and attaches the span-based
     * WYSIWYG formatting engine (FormatSpanWatcher) to the EditText's Editable.
     */
    private fun initRichTextFormatter() {
        richTextFormatterManager = RichTextFormatterManager(richTextConfiguration)

        // Initialize and attach FormatSpanWatcher for span-based WYSIWYG formatting
        if (richTextConfiguration.hasAnyEnabled()) {
            formatSpanWatcher = FormatSpanWatcher(context)
            val editable = binding.etMessageInput.text
            formatSpanWatcher?.attachTo(editable)
        }
    }


    /**
     * Starts collecting flows from the ViewModel.
     */
    private fun startCollectingFlows() {
        viewScope?.cancel()
        viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        viewScope?.launch {
            viewModel?.uiState?.collectLatest { state ->
                handleUIState(state)
            }
        }

        viewScope?.launch {
            viewModel?.editMessage?.collectLatest { message ->
                updateEditPreview(message)
            }
        }

        viewScope?.launch {
            viewModel?.replyMessage?.collectLatest { message ->
                updateMessagePreview(message)
            }
        }

        viewScope?.launch {
            viewModel?.isAIGenerating?.collectLatest { generating ->
                updateSendButtonState(binding.etMessageInput.text?.isNotBlank() ?: false)
            }
        }

        viewScope?.launch {
            viewModel?.errorEvent?.collect { error ->
                // Gate B backstop: picked files beyond the attachment cap were dropped — tell the user.
                if (error.code == CometChatMessageComposerViewModel.ERROR_MAX_ATTACHMENTS_EXCEEDED) {
                    showAttachmentLimitToast(viewModel?.maxAttachmentCount ?: 0)
                }
                onError?.invoke(error)
            }
        }

        viewScope?.launch {
            viewModel?.attachmentTiles?.collectLatest { tiles ->
                updateAttachmentTray(tiles)
            }
        }

        viewScope?.launch {
            viewModel?.composeText?.collectLatest { text ->
                // Only set text programmatically if it differs from what's already in the EditText.
                // This prevents a feedback loop where onTextChanged → setComposeText → collectLatest
                // → setText destroys all spans and resets cursor position.
                val currentText = binding.etMessageInput.text?.toString() ?: ""
                if (text.isNotEmpty() && text != currentText) {
                    binding.etMessageInput.setText(text)
                    binding.etMessageInput.setSelection(text.length)
                }
            }
        }
    }

    /**
     * Handles UI state changes.
     */
    private fun handleUIState(state: MessageComposerUIState) {
        when (state) {
            is MessageComposerUIState.Idle -> {
                // Reset to idle state
            }
            is MessageComposerUIState.Sending -> {
                // Show sending state if needed
            }
            is MessageComposerUIState.Editing -> {
                val editedMessage = state.message
                // Text messages edit their text; media messages edit their caption.
                val markdown = when (editedMessage) {
                    is TextMessage -> editedMessage.text ?: ""
                    is MediaMessage -> editedMessage.caption ?: ""
                    else -> null
                }
                markdown?.let {
                    if (richTextConfiguration.hasAnyEnabled() && markdown.isNotEmpty()) {
                        // Use MarkdownConverter to parse markdown into WYSIWYG spans
                        val spanEditable = MarkdownConverter.fromMarkdown(markdown, context)

                        // Run formatter pipeline to resolve mention tokens
                        var spannableBuilder = SpannableStringBuilder(spanEditable)
                        for (formatter in textFormatters) {
                            spannableBuilder = formatter.prepareMessageString(
                                context,
                                editedMessage,
                                spannableBuilder,
                                UIKitConstants.MessageBubbleAlignment.RIGHT,
                                UIKitConstants.FormattingType.MESSAGE_COMPOSER
                            ) ?: spannableBuilder
                        }
                        binding.etMessageInput.setText(spannableBuilder)
                        binding.etMessageInput.setSelection(spannableBuilder.length)

                        // Re-attach FormatSpanWatcher to the new Editable content
                        formatSpanWatcher?.clearPendingFormats()
                        formatSpanWatcher?.attachTo(binding.etMessageInput.text)
                    } else {
                        // Fallback: plain text with formatter pipeline
                        var spannableBuilder = SpannableStringBuilder(markdown)
                        for (formatter in textFormatters) {
                            spannableBuilder = formatter.prepareMessageString(
                                context,
                                editedMessage,
                                spannableBuilder,
                                UIKitConstants.MessageBubbleAlignment.RIGHT,
                                UIKitConstants.FormattingType.MESSAGE_COMPOSER
                            ) ?: spannableBuilder
                        }
                        binding.etMessageInput.setText(spannableBuilder)
                        binding.etMessageInput.setSelection(spannableBuilder.length)
                    }
                }
            }
            is MessageComposerUIState.Replying -> {
                // Reply state handled by replyMessage flow
            }
            is MessageComposerUIState.AIGenerating -> {
                updateSendButtonState(false)
            }
            is MessageComposerUIState.Success -> {
                // Message sent successfully
            }
            is MessageComposerUIState.Error -> {
                onError?.invoke(state.exception)
            }
        }
    }

    /**
     * Updates the edit preview panel.
     * Runs the formatter pipeline to resolve mention tokens (e.g., <@uid:userId>) 
     * to display names (e.g., @John) before displaying.
     */
    private fun updateEditPreview(message: BaseMessage?) {
        if (message != null && !hideEditPreview) {
            binding.editPreviewCard.visibility = View.VISIBLE

            val formattedText: CharSequence = when (message) {
                // Media messages show the summarized attachment preview:
                // "N Images · caption" / "N Images" / caption / file name —
                // same rules as the quoted message preview.
                is MediaMessage -> MultiAttachmentUtils.mediaPreviewSubtitle(context, message)

                else -> {
                    val rawText = (message as? TextMessage)?.text ?: ""
                    // Run formatter pipeline to resolve mention tokens
                    var spannableBuilder = SpannableStringBuilder(rawText)
                    for (formatter in textFormatters) {
                        spannableBuilder = formatter.prepareMessageString(
                            context,
                            message,
                            spannableBuilder,
                            UIKitConstants.MessageBubbleAlignment.RIGHT,
                            UIKitConstants.FormattingType.MESSAGE_COMPOSER
                        ) ?: spannableBuilder
                    }
                    // Parse markdown into formatted spans (bold, italic, strikethrough,
                    // underline, etc.) so the preview shows rendered text instead of raw
                    // markdown markers
                    com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationSubtitleRenderer.render(
                        context, spannableBuilder.toString()
                    )
                }
            }
            binding.tvEditPreviewMessage.text = formattedText
        } else {
            binding.editPreviewCard.visibility = View.GONE
        }
    }

    /**
     * Updates the message preview panel (for reply/quote).
     * Matches the Java implementation logic from Utils.setReplyMessagePreview.
     * Runs the formatter pipeline to resolve mention tokens for TextMessages.
     */
    private fun updateMessagePreview(message: BaseMessage?) {
        if (message != null && !hideMessagePreview) {
            binding.messagePreviewCard.visibility = View.VISIBLE
            
            // Get sender name - show "You" if it's the logged-in user
            val loggedInUser = com.cometchat.uikit.core.CometChatUIKit.getLoggedInUser()
            val senderName = if (message.sender?.uid == loggedInUser?.uid) {
                context.getString(R.string.cometchat_you)
            } else {
                message.sender?.name ?: ""
            }
            
            binding.tvMessagePreviewTitle.text = senderName
            
            // Set subtitle based on message type
            val subtitleText: CharSequence = when (message) {
                is TextMessage -> {
                    if (message.deletedAt > 0) {
                        context.getString(R.string.cometchat_this_message_deleted)
                    } else {
                        // Run formatter pipeline to resolve mention tokens
                        var spannableBuilder = SpannableStringBuilder(message.text ?: "")
                        for (formatter in textFormatters) {
                            spannableBuilder = formatter.prepareMessageString(
                                context,
                                message,
                                spannableBuilder,
                                UIKitConstants.MessageBubbleAlignment.RIGHT,
                                UIKitConstants.FormattingType.MESSAGE_COMPOSER
                            ) ?: spannableBuilder
                        }
                        spannableBuilder
                    }
                }
                is MediaMessage -> {
                    // Summarized attachment preview: "N Images · caption" / "N Images" /
                    // caption / file name — same rules as the quoted message preview.
                    MultiAttachmentUtils.mediaPreviewSubtitle(context, message).ifEmpty {
                        when (message.type) {
                            "image" -> context.getString(R.string.cometchat_message_image)
                            "video" -> context.getString(R.string.cometchat_message_video)
                            "audio" -> context.getString(R.string.cometchat_message_audio)
                            "file" -> context.getString(R.string.cometchat_message_document)
                            else -> message.type ?: ""
                        }
                    }
                }
                is CustomMessage -> {
                    when (message.type) {
                        "extension_poll" -> context.getString(R.string.cometchat_poll)
                        "extension_sticker" -> context.getString(R.string.cometchat_message_sticker)
                        "location" -> context.getString(R.string.cometchat_message_location)
                        "extension_document" -> context.getString(R.string.cometchat_message_document)
                        "extension_whiteboard" -> context.getString(R.string.cometchat_collaborative_whiteboard)
                        "meeting" -> context.getString(R.string.cometchat_meeting)
                        else -> message.conversationText ?: message.type ?: ""
                    }
                }
                is CardMessage -> message.text?.ifEmpty { null }
                    ?: context.getString(R.string.cometchat_message_card)
                else -> message.type ?: ""
            }
            binding.tvMessagePreviewSubtitle.text = subtitleText
        } else {
            binding.messagePreviewCard.visibility = View.GONE
        }
    }

    // ==================== Public API ====================

    /**
     * Sets the user as the message receiver.
     */
    fun setUser(user: User) {
        this.user = user
        this.group = null
        isAgentChat = AgentChatDetector.isAgentChat(user)
        if (isAgentChat) {
            hideAttachmentButton = true
            hideVoiceRecordingButton = true
            hideStickerButton = true
            richTextToolbarVisibility = View.GONE
            // Immediately hide buttons without animation to prevent flash on first load
            binding.ivVoiceRecording.visibility = View.GONE
            binding.ivSticker.visibility = View.GONE
            binding.ivAttachment.visibility = View.GONE
            binding.secondaryButtonLayout.visibility = View.GONE
            binding.separatorView.visibility = View.GONE
            updateButtonVisibility()
        }
        viewModel?.setUser(user)
        
        // Update formatters with user context
        for (formatter in textFormatters) {
            formatter.setUser(user)
            formatter.setGroup(null)
        }
        
        invokeViewCallbacks()
    }

    /**
     * Sets the group as the message receiver.
     */
    fun setGroup(group: Group) {
        this.group = group
        this.user = null
        viewModel?.setGroup(group)
        
        // Update formatters with group context
        for (formatter in textFormatters) {
            formatter.setGroup(group)
            formatter.setUser(null)
        }
        
        invokeViewCallbacks()
    }

    /**
     * Sets the parent message ID for threaded messages.
     */
    fun setParentMessageId(id: Long) {
        viewModel?.setParentMessageId(id)
    }

    /**
     * Sets the compose text programmatically.
     */
    fun setText(text: String) {
        binding.etMessageInput.setText(text)
        viewModel?.setComposeText(text)
    }

    /**
     * Gets the current compose text.
     */
    fun getText(): String {
        return binding.etMessageInput.text?.toString() ?: ""
    }

    /**
     * Sets the placeholder text.
     */
    fun setPlaceholderText(text: String) {
        placeholderText = text
        binding.etMessageInput.hint = text
    }

    /**
     * Sends a media message.
     */
    fun sendMediaMessage(file: File, contentType: String) {
        viewModel?.sendMediaMessage(file, contentType)
    }

    /**
     * Sets the message to be edited.
     */
    fun setEditMessage(message: TextMessage) {
        viewModel?.setEditMessage(message)
    }

    /**
     * Clears the edit message state.
     */
    fun clearEditMessage() {
        exitEditMode()
    }

    /**
     * Enters edit mode for the given message.
     * Shows the edit preview bar, parses the message markdown into spans via
     * [MarkdownConverter.fromMarkdown], populates the input field with the
     * resulting span-formatted Editable, and stores the editing state in the ViewModel.
     *
     * Requirements: 12.1, 12.2
     */
    fun enterEditMode(message: TextMessage) {
        // Store editing state in ViewModel (drives preview bar visibility via flow)
        viewModel?.setEditMessage(message)

        // Populate input field with span-formatted content
        val markdown = message.text ?: ""
        if (richTextConfiguration.hasAnyEnabled() && markdown.isNotEmpty()) {
            // Use MarkdownConverter to parse markdown into WYSIWYG spans
            val spanEditable = MarkdownConverter.fromMarkdown(markdown, context)

            // Run formatter pipeline to resolve mention tokens on the span editable
            var spannableBuilder = SpannableStringBuilder(spanEditable)
            for (formatter in textFormatters) {
                spannableBuilder = formatter.prepareMessageString(
                    context,
                    message,
                    spannableBuilder,
                    UIKitConstants.MessageBubbleAlignment.RIGHT,
                    UIKitConstants.FormattingType.MESSAGE_COMPOSER
                ) ?: spannableBuilder
            }

            // Restore consumed mentions from message metadata (Req 17.2)
            MentionCodeBlockHandler.restoreConsumedMentionsFromMetadata(
                spannableBuilder, message.metadata
            )

            binding.etMessageInput.setText(spannableBuilder)
            binding.etMessageInput.setSelection(spannableBuilder.length)

            // Re-attach FormatSpanWatcher to the new Editable content
            formatSpanWatcher?.clearPendingFormats()
            formatSpanWatcher?.attachTo(binding.etMessageInput.text)
        } else {
            // Fallback: plain text with formatter pipeline (no rich text enabled)
            var spannableBuilder = SpannableStringBuilder(markdown)
            for (formatter in textFormatters) {
                spannableBuilder = formatter.prepareMessageString(
                    context,
                    message,
                    spannableBuilder,
                    UIKitConstants.MessageBubbleAlignment.RIGHT,
                    UIKitConstants.FormattingType.MESSAGE_COMPOSER
                ) ?: spannableBuilder
            }
            binding.etMessageInput.setText(spannableBuilder)
            binding.etMessageInput.setSelection(spannableBuilder.length)
        }
    }

    /**
     * Exits edit mode: hides the edit preview bar, clears the input field,
     * and clears the editing state from the ViewModel.
     *
     * Requirements: 12.4
     */
    fun exitEditMode() {
        viewModel?.clearEditMessage()
        binding.etMessageInput.setText("")

        // Reset span engine state
        if (richTextConfiguration.hasAnyEnabled()) {
            formatSpanWatcher?.clearPendingFormats()
            formatSpanWatcher?.attachTo(binding.etMessageInput.text)
        }

        // Reset active/disabled formats
        activeFormats = emptySet()
        disabledFormats = emptySet()
        updateToolbarButtonStates()
    }

    /**
     * Sets the message to be replied to.
     */
    fun setReplyMessage(message: BaseMessage) {
        enterReplyMode(message)
    }

    /**
     * Clears the reply message state.
     */
    fun clearReplyMessage() {
        exitReplyMode()
    }

    /**
     * Enters reply mode for the given message.
     * Shows the reply preview bar above the input field with the sender name
     * and a type-dependent content summary, and stores the reply state.
     *
     * Content summary is determined by message type:
     * - [TextMessage]: the message text (with formatter pipeline applied for mentions)
     * - [MediaMessage]: attachment filename or localized type label (image, video, audio, file)
     * - [CustomMessage]: localized label based on custom type (poll, sticker, location, etc.)
     *
     * Requirements: 13.1, 13.2
     */
    fun enterReplyMode(message: BaseMessage) {
        // Store reply state in ViewModel (drives preview bar visibility via flow)
        viewModel?.setReplyMessage(message)
        replyingToMessageId = message.id.toLong()
    }

    /**
     * Exits reply mode: hides the reply preview bar and clears the reply state.
     *
     * Requirements: 13.4
     */
    fun exitReplyMode() {
        viewModel?.clearReplyMessage()
        replyingToMessageId = null
    }

    /**
     * Invokes custom view callbacks.
     */
    private fun invokeViewCallbacks() {
        headerViewListener?.let {
            Utils.handleView(binding.headerViewLayout, it.createView(context, user, group), true)
            binding.headerViewLayout.visibility = View.VISIBLE
        }
        footerViewListener?.let {
            Utils.handleView(binding.footerViewLayout, it.createView(context, user, group), true)
            binding.footerViewLayout.visibility = View.VISIBLE
        }
        secondaryButtonViewListener?.let {
            Utils.handleView(binding.secondaryButtonLayout, it.createView(context, user, group), true)
        }
        sendButtonViewListener?.let {
            Utils.handleView(binding.sendButtonLayout, it.createView(context, user, group), true)
        }
        auxiliaryButtonViewListener?.let {
            Utils.handleView(binding.auxiliaryButtonLayout, it.createView(context, user, group), true)
        }
    }


    // ==================== Visibility Setters ====================

    fun setHideAttachmentButton(hide: Boolean) {
        if (isAgentChat && !hide) return // Agent chat always hides attachment button
        hideAttachmentButton = hide
        updateButtonVisibility()
    }

    fun setHideVoiceRecordingButton(hide: Boolean) {
        if (isAgentChat && !hide) return // Agent chat always hides voice recording button
        hideVoiceRecordingButton = hide
        updateButtonVisibility()
    }

    fun setHideAIButton(hide: Boolean) {
        hideAIButton = hide
        updateButtonVisibility()
    }

    fun setHideStickerButton(hide: Boolean) {
        if (isAgentChat && !hide) return // Agent chat always hides sticker button
        hideStickerButton = hide
        updateButtonVisibility()
    }

    /**
     * Sets the visibility of the rich text formatting toolbar.
     * When set to VISIBLE, the toolbar appears immediately (if formats are enabled).
     * If no formats are configured, all formats will be enabled automatically.
     * 
     * @param visibility View.VISIBLE to show the toolbar, View.GONE to hide it
     */
    fun setRichTextToolbarVisibility(visibility: Int) {
        // Agent chat always hides the rich text toolbar
        if (isAgentChat && visibility == View.VISIBLE) return
        richTextToolbarVisibility = visibility
        // Auto-enable all formats if visibility is VISIBLE and no formats are configured
        if (visibility == View.VISIBLE && !richTextConfiguration.hasAnyEnabled()) {
            setRichTextConfiguration(RichTextConfiguration.allEnabled())
        }
        // When toolbar is visible, automatically enable text selection menu items (Req 19.8)
        if (visibility == View.VISIBLE) {
            showTextSelectionMenuItems = true
            setupTextSelectionMenu()
        }
        updateButtonVisibility()
    }

    /**
     * Gets the visibility of the rich text formatting toolbar.
     * 
     * @return View.VISIBLE or View.GONE
     */
    fun getRichTextToolbarVisibility(): Int = richTextToolbarVisibility

    /**
     * Enables or disables rich text formatting.
     * When disabled, the text selection menu formatting options are removed.
     *
     * @param enable true to enable rich text formatting, false to disable
     */
    fun setEnableRichTextFormatting(enable: Boolean) {
        enableRichTextFormatting = enable
        setupTextSelectionMenu()
    }

    /**
     * Returns whether rich text formatting is enabled.
     */
    fun isEnableRichTextFormatting(): Boolean = enableRichTextFormatting

    // ==================== Multiline Mode ====================

    /**
     * Sets the composer layout mode.
     * 
     * @param mode [ComposerLayoutMode.SINGLE_LINE] for default single-row layout,
     *             [ComposerLayoutMode.MULTI_LINE] for two-row layout with text input
     *             in Row 1 and action buttons in Row 2.
     */
    fun setLayoutMode(mode: ComposerLayoutMode) {
        composerLayoutMode = mode
        updateMultilineModeLayout()
    }

    /**
     * Returns the current composer layout mode.
     */
    fun getComposerMode(): ComposerLayoutMode = composerLayoutMode

    /**
     * Programmatically shows or hides the formatting toolbar in multiline mode.
     * Only has effect when multiline mode is enabled and rich text formatting is enabled.
     *
     * @param show true to show the formatting toolbar, false to show action buttons
     */
    fun setShowFormattingToolbar(show: Boolean) {
        if (composerLayoutMode != ComposerLayoutMode.MULTI_LINE || !enableRichTextFormatting) return
        isFormattingToolbarVisible = show
        updateMultilineRow2Visibility()
    }

    /**
     * Returns whether the formatting toolbar is currently visible in multiline mode.
     */
    fun isFormattingToolbarVisible(): Boolean = isFormattingToolbarVisible

    /**
     * Updates the layout based on multiline mode state.
     * In multiline mode: hides inline buttons from the input row, shows Row 2.
     * In single-line mode: restores the original layout.
     */
    private fun updateMultilineModeLayout() {
        if (composerLayoutMode == ComposerLayoutMode.MULTI_LINE) {
            // Hide buttons from the input row (they move to Row 2)
            binding.ivAttachment.visibility = View.GONE
            binding.secondaryButtonLayout.visibility = View.GONE
            binding.separatorView.visibility = View.GONE
            binding.ivVoiceRecording.visibility = View.GONE
            binding.ivSticker.visibility = View.GONE
            binding.sendButtonCard.visibility = View.GONE
            // Show separator and Row 2
            binding.multilineRow2Separator?.visibility = View.VISIBLE
            binding.multilineRow2Layout?.visibility = View.VISIBLE
            // Populate the multiline formatting toolbar buttons
            populateMultilineToolbar()
            updateMultilineRow2Visibility()
        } else {
            // Restore single-line layout
            binding.multilineRow2Separator?.visibility = View.GONE
            binding.multilineRow2Layout?.visibility = View.GONE
            updateButtonVisibility()
        }
    }

    /**
     * Populates the multiline formatting toolbar with 10 formatting buttons.
     * Button order matches the Compose reference and single-line toolbar:
     * Bold, Italic, Underline, Strikethrough | Link, Ordered List, Bullet List | Blockquote, Inline Code, Code Block
     *
     * Each button is a 32×32dp ImageButton inside a MaterialCardView wrapper,
     * matching the single-line toolbar's layout pattern.
     */
    private fun populateMultilineToolbar() {
        val toolbarLayout = binding.multilineToolbarButtonsLayout ?: return
        // Only populate once
        if (toolbarLayout.childCount > 0) return

        multilineToolbarButtons.clear()
        multilineToolbarFormatMap.clear()

        val density = context.resources.displayMetrics.density
        val buttonSizePx = (40 * density).toInt()
        val iconPaddingPx = (8 * density).toInt()
        val marginPx = (context.resources.getDimensionPixelSize(R.dimen.cometchat_margin_2))
        val separatorWidthPx = (1 * density).toInt()
        val separatorHeightPx = (24 * density).toInt()
        val separatorMarginPx = context.resources.getDimensionPixelSize(R.dimen.cometchat_margin_3)

        // Define the toolbar buttons: format, drawable resource, click action
        data class ToolbarButtonDef(
            val format: RichTextFormat?,
            val drawableRes: Int,
            val contentDesc: String,
            val onClick: () -> Unit
        )

        val buttonDefs = listOf(
            ToolbarButtonDef(RichTextFormat.BOLD, R.drawable.cometchat_ic_format_bold, "Bold") { toggleFormat(RichTextFormat.BOLD) },
            ToolbarButtonDef(RichTextFormat.ITALIC, R.drawable.cometchat_ic_format_italic, "Italic") { toggleFormat(RichTextFormat.ITALIC) },
            ToolbarButtonDef(RichTextFormat.UNDERLINE, R.drawable.cometchat_ic_format_underline, "Underline") { toggleFormat(RichTextFormat.UNDERLINE) },
            ToolbarButtonDef(RichTextFormat.STRIKETHROUGH, R.drawable.cometchat_ic_format_strikethrough, "Strikethrough") { toggleFormat(RichTextFormat.STRIKETHROUGH) },
            null, // Separator 1
            ToolbarButtonDef(RichTextFormat.LINK, R.drawable.cometchat_ic_format_link, "Link") { showLinkDialog() },
            ToolbarButtonDef(RichTextFormat.ORDERED_LIST, R.drawable.cometchat_ic_format_list_numbered, "Ordered List") { toggleFormat(RichTextFormat.ORDERED_LIST) },
            ToolbarButtonDef(RichTextFormat.BULLET_LIST, R.drawable.cometchat_ic_format_list_bullet, "Bullet List") { toggleFormat(RichTextFormat.BULLET_LIST) },
            null, // Separator 2
            ToolbarButtonDef(RichTextFormat.BLOCKQUOTE, R.drawable.cometchat_ic_format_quote, "Blockquote") { toggleFormat(RichTextFormat.BLOCKQUOTE) },
            ToolbarButtonDef(RichTextFormat.INLINE_CODE, R.drawable.cometchat_ic_format_code, "Inline Code") { toggleFormat(RichTextFormat.INLINE_CODE) },
            ToolbarButtonDef(RichTextFormat.CODE_BLOCK, R.drawable.cometchat_ic_format_code_block, "Code Block") { toggleFormat(RichTextFormat.CODE_BLOCK) }
        )

        val normalIconTint = style.richTextToolbarIconTint.takeIf { it != 0 }
            ?: CometChatTheme.getIconTintSecondary(context)
        val separatorColor = style.separatorColor.takeIf { it != 0 }
            ?: CometChatTheme.getBorderColorLight(context)

        for ((index, def) in buttonDefs.withIndex()) {
            if (def == null) {
                // Add separator view
                val separator = View(context).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        separatorWidthPx,
                        separatorHeightPx
                    ).apply {
                        marginStart = separatorMarginPx
                        marginEnd = separatorMarginPx
                    }
                    setBackgroundColor(separatorColor)
                }
                toolbarLayout.addView(separator)
                continue
            }

            // Create MaterialCardView wrapper
            val cardView = MaterialCardView(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0 && buttonDefs[index - 1] != null) {
                        marginStart = marginPx
                    }
                }
                setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
                radius = context.resources.getDimension(R.dimen.cometchat_radius_1)
                cardElevation = 0f
                strokeWidth = 0
            }

            // Create ImageButton
            val imageButton = android.widget.ImageButton(context).apply {
                layoutParams = ViewGroup.LayoutParams(buttonSizePx, buttonSizePx)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                contentDescription = def.contentDesc
                setPadding(iconPaddingPx, iconPaddingPx, iconPaddingPx, iconPaddingPx)
                setImageResource(def.drawableRes)
                setColorFilter(normalIconTint, android.graphics.PorterDuff.Mode.SRC_IN)
                setOnClickListener { def.onClick() }
            }

            cardView.addView(imageButton)
            toolbarLayout.addView(cardView)

            // Track for state updates
            if (def.format != null) {
                multilineToolbarButtons.add(Pair(imageButton, cardView))
                multilineToolbarFormatMap[def.format] = Pair(imageButton, cardView)
            }
        }
    }

    /**
     * Toggles Row 2 between action buttons and formatting toolbar in multiline mode.
     */
    private fun updateMultilineRow2Visibility() {
        if (composerLayoutMode != ComposerLayoutMode.MULTI_LINE) return
        val buttonsGroup = binding.multilineButtonsGroup
        val toolbarGroup = binding.multilineToolbarGroup
        if (buttonsGroup == null || toolbarGroup == null) return

        if (isFormattingToolbarVisible) {
            // Show toolbar, hide buttons
            buttonsGroup.visibility = View.GONE
            toolbarGroup.visibility = View.VISIBLE
        } else {
            // Show buttons, hide toolbar
            buttonsGroup.visibility = View.VISIBLE
            toolbarGroup.visibility = View.GONE
        }

        binding.ivMultilineVoiceRecording?.visibility =
            if (hideVoiceRecordingButton || hasStagedAttachments()) View.GONE else View.VISIBLE
    }

    /**
     * Updates the Aa formatting toggle icon tint based on toolbar visibility state.
     * When the formatting toolbar is visible (active), applies the active tint color
     * (defaults to primary color). When hidden (inactive), applies the normal toggle tint
     * (defaults to secondary icon tint).
     */
    private fun updateFormattingToggleTint() {
        val toggleView = binding.ivMultilineFormattingToggle ?: return
        val tint = if (isFormattingToolbarVisible) {
            style.richTextToggleIconActiveTint.takeIf { it != 0 }
                ?: CometChatTheme.getPrimaryColor(context)
        } else {
            style.richTextToolbarToggleIconTint.takeIf { it != 0 }
                ?: CometChatTheme.getIconTintSecondary(context)
        }
        toggleView.setColorFilter(tint, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    /**
     * Sets whether to show formatting options in the text selection menu.
     * When enabled and [enableRichTextFormatting] is true, long-pressing and
     * selecting text will show Bold, Italic, Strikethrough, and InlineCode
     * options in the system context menu.
     *
     * @param show true to show formatting options in selection menu, false to hide
     */
    fun setShowTextSelectionMenuItems(show: Boolean) {
        showTextSelectionMenuItems = show
        setupTextSelectionMenu()
    }

    /**
     * Returns whether formatting options are shown in the text selection menu.
     */
    fun isShowTextSelectionMenuItems(): Boolean = showTextSelectionMenuItems

    fun setHideEditPreview(hide: Boolean) {
        hideEditPreview = hide
        updateEditPreview(viewModel?.editMessage?.value)
    }

    fun setHideMessagePreview(hide: Boolean) {
        hideMessagePreview = hide
        updateMessagePreview(viewModel?.replyMessage?.value)
    }

    // ==================== Attachment Option Visibility Setters ====================

    /**
     * Sets the visibility of the Camera attachment option.
     * 
     * @param visibility View.VISIBLE or View.GONE
     */
    fun setCameraOptionVisibility(visibility: Int) {
        viewModel?.setCameraOptionVisibility(visibility == View.VISIBLE)
    }

    /**
     * Sets the visibility of the Image attachment option.
     * 
     * @param visibility View.VISIBLE or View.GONE
     */
    fun setImageOptionVisibility(visibility: Int) {
        viewModel?.setImageOptionVisibility(visibility == View.VISIBLE)
    }

    /**
     * Sets the visibility of the Video attachment option.
     * 
     * @param visibility View.VISIBLE or View.GONE
     */
    fun setVideoOptionVisibility(visibility: Int) {
        viewModel?.setVideoOptionVisibility(visibility == View.VISIBLE)
    }

    /**
     * Sets the visibility of the Audio attachment option.
     * 
     * @param visibility View.VISIBLE or View.GONE
     */
    fun setAudioOptionVisibility(visibility: Int) {
        viewModel?.setAudioOptionVisibility(visibility == View.VISIBLE)
    }

    /**
     * Sets the visibility of the File/Document attachment option.
     * 
     * @param visibility View.VISIBLE or View.GONE
     */
    fun setFileOptionVisibility(visibility: Int) {
        viewModel?.setFileOptionVisibility(visibility == View.VISIBLE)
    }

    /**
     * Sets the visibility of the Poll attachment option.
     * 
     * @param visibility View.VISIBLE or View.GONE
     */
    fun setPollOptionVisibility(visibility: Int) {
        viewModel?.setPollOptionVisibility(visibility == View.VISIBLE)
    }

    /**
     * Sets the visibility of the Collaborative Document attachment option.
     * 
     * @param visibility View.VISIBLE or View.GONE
     */
    fun setCollaborativeDocumentOptionVisibility(visibility: Int) {
        viewModel?.setCollaborativeDocumentOptionVisibility(visibility == View.VISIBLE)
    }

    /**
     * Sets the visibility of the Collaborative Whiteboard attachment option.
     * 
     * @param visibility View.VISIBLE or View.GONE
     */
    fun setCollaborativeWhiteboardOptionVisibility(visibility: Int) {
        viewModel?.setCollaborativeWhiteboardOptionVisibility(visibility == View.VISIBLE)
    }

    // ==================== Custom Attachment Options ====================

    /**
     * Sets/replaces the custom attachment options list.
     * These options appear after the default options in the attachment popup.
     *
     * Example usage:
     * ```kotlin
     * messageComposer.setAttachmentOptions(listOf(
     *     CometChatMessageComposerAction(
     *         id = "LOCATION",
     *         title = "Share Location",
     *         icon = R.drawable.ic_location
     *     ),
     *     CometChatMessageComposerAction(
     *         id = "CONTACT",
     *         title = "Share Contact",
     *         icon = R.drawable.ic_contact
     *     )
     * ))
     * ```
     *
     * @param options The list of custom attachment options to set
     */
    fun setAttachmentOptions(options: List<CometChatMessageComposerAction>) {
        viewModel?.setAttachmentOptions(options)
    }

    /**
     * Adds a single custom attachment option to the list.
     * The option appears after the default options in the attachment popup.
     *
     * Example usage:
     * ```kotlin
     * messageComposer.addAttachmentOption(
     *     CometChatMessageComposerAction(
     *         id = "LOCATION",
     *         title = "Share Location",
     *         icon = R.drawable.ic_location
     *     )
     * )
     * ```
     *
     * @param option The custom attachment option to add
     */
    fun addAttachmentOption(option: CometChatMessageComposerAction) {
        viewModel?.addAttachmentOption(option)
    }

    // ==================== Callback Setters ====================

    fun setOnSendButtonClick(callback: (String) -> Unit) {
        onSendButtonClick = callback
    }

    fun setOnAttachmentClick(callback: () -> Unit) {
        onAttachmentClick = callback
    }

    /**
     * Sets the callback for camera attachment option clicks.
     * 
     * @param callback A function that returns `true` to skip default camera launch behavior
     *                 (developer handles it), or `false` to execute default camera launch behavior.
     * 
     * Example usage:
     * ```kotlin
     * messageComposer.setOnCameraClick {
     *     // Custom camera handling
     *     launchCustomCamera()
     *     true // Skip default behavior
     * }
     * ```
     */
    fun setOnCameraClick(callback: () -> Boolean) {
        onCameraClick = callback
    }

    /**
     * Sets the callback for image attachment option clicks.
     * 
     * @param callback A function that returns `true` to skip default image picker behavior
     *                 (developer handles it), or `false` to execute default image picker behavior.
     * 
     * Example usage:
     * ```kotlin
     * messageComposer.setOnImageClick {
     *     // Custom image picker handling
     *     showCustomGallery()
     *     true // Skip default behavior
     * }
     * ```
     */
    fun setOnImageClick(callback: () -> Boolean) {
        onImageClick = callback
    }

    /**
     * Sets the callback for video attachment option clicks.
     * 
     * @param callback A function that returns `true` to skip default video picker behavior
     *                 (developer handles it), or `false` to execute default video picker behavior.
     * 
     * Example usage:
     * ```kotlin
     * messageComposer.setOnVideoClick {
     *     // Custom video picker handling
     *     showCustomVideoPicker()
     *     true // Skip default behavior
     * }
     * ```
     */
    fun setOnVideoClick(callback: () -> Boolean) {
        onVideoClick = callback
    }

    /**
     * Sets the callback for audio attachment option clicks.
     * 
     * @param callback A function that returns `true` to skip default audio picker behavior
     *                 (developer handles it), or `false` to execute default audio picker behavior.
     * 
     * Example usage:
     * ```kotlin
     * messageComposer.setOnAudioClick {
     *     // Add analytics before default behavior
     *     trackAnalytics("audio_attachment_clicked")
     *     false // Continue with default behavior
     * }
     * ```
     */
    fun setOnAudioClick(callback: () -> Boolean) {
        onAudioClick = callback
    }

    /**
     * Sets the callback for document attachment option clicks.
     * 
     * @param callback A function that returns `true` to skip default document picker behavior
     *                 (developer handles it), or `false` to execute default document picker behavior.
     * 
     * Example usage:
     * ```kotlin
     * messageComposer.setOnDocumentClick {
     *     // Custom document picker handling
     *     showCustomDocumentPicker()
     *     true // Skip default behavior
     * }
     * ```
     */
    fun setOnDocumentClick(callback: () -> Boolean) {
        onDocumentClick = callback
    }

    /**
     * Sets the callback for Poll attachment option clicks.
     * 
     * @param callback A function that returns `true` to skip default poll creation behavior
     *                 (developer handles it), or `false` to execute default poll creation behavior.
     * 
     * Example usage:
     * ```kotlin
     * messageComposer.setOnPollClick {
     *     // Custom poll creation handling
     *     showCustomPollCreator()
     *     true // Skip default behavior
     * }
     * ```
     */
    fun setOnPollClick(callback: () -> Boolean) {
        onPollClick = callback
    }

    /**
     * Sets the callback for Collaborative Document attachment option clicks.
     * 
     * @param callback A function that returns `true` to skip default collaborative document creation behavior
     *                 (developer handles it), or `false` to execute default collaborative document creation behavior.
     * 
     * Example usage:
     * ```kotlin
     * messageComposer.setOnCollaborativeDocumentClick {
     *     // Custom collaborative document handling
     *     showCustomDocumentEditor()
     *     true // Skip default behavior
     * }
     * ```
     */
    fun setOnCollaborativeDocumentClick(callback: () -> Boolean) {
        onCollaborativeDocumentClick = callback
    }

    /**
     * Sets the callback for Collaborative Whiteboard attachment option clicks.
     * 
     * @param callback A function that returns `true` to skip default collaborative whiteboard creation behavior
     *                 (developer handles it), or `false` to execute default collaborative whiteboard creation behavior.
     * 
     * Example usage:
     * ```kotlin
     * messageComposer.setOnCollaborativeWhiteboardClick {
     *     // Custom collaborative whiteboard handling
     *     showCustomWhiteboardEditor()
     *     true // Skip default behavior
     * }
     * ```
     */
    fun setOnCollaborativeWhiteboardClick(callback: () -> Boolean) {
        onCollaborativeWhiteboardClick = callback
    }

    /**
     * Sets the callback for custom attachment option clicks.
     * This callback is invoked when a custom attachment option (added via
     * [setAttachmentOptions] or [addAttachmentOption]) is clicked.
     * 
     * @param callback A function that receives the clicked [CometChatMessageComposerAction].
     * 
     * Example usage:
     * ```kotlin
     * // Add custom options
     * messageComposer.addAttachmentOption(
     *     CometChatMessageComposerAction(
     *         id = "LOCATION",
     *         title = "Share Location",
     *         icon = R.drawable.ic_location
     *     )
     * )
     * 
     * // Handle clicks
     * messageComposer.setOnAttachmentOptionClick { action ->
     *     when (action.id) {
     *         "LOCATION" -> shareLocation()
     *     }
     * }
     * ```
     */
    fun setOnAttachmentOptionClick(callback: (CometChatMessageComposerAction) -> Unit) {
        onAttachmentOptionClick = callback
    }

    fun setOnVoiceRecordingClick(callback: () -> Unit) {
        onVoiceRecordingClick = callback
    }

    fun setOnAIClick(callback: () -> Unit) {
        onAIClick = callback
    }

    fun setOnStickerClick(callback: () -> Unit) {
        onStickerClick = callback
    }

    fun setOnError(callback: (CometChatException) -> Unit) {
        onError = callback
    }

    fun setOnTextChanged(callback: (String) -> Unit) {
        onTextChanged = callback
    }

    // ==================== Custom View Listener Setters ====================

    fun setHeaderViewListener(listener: MessageComposerViewHolderListener) {
        headerViewListener = listener
        invokeViewCallbacks()
    }

    fun setFooterViewListener(listener: MessageComposerViewHolderListener) {
        footerViewListener = listener
        invokeViewCallbacks()
    }

    fun setSecondaryButtonViewListener(listener: MessageComposerViewHolderListener) {
        secondaryButtonViewListener = listener
        invokeViewCallbacks()
    }

    fun setSendButtonViewListener(listener: MessageComposerViewHolderListener) {
        sendButtonViewListener = listener
        invokeViewCallbacks()
    }

    fun setAuxiliaryButtonViewListener(listener: MessageComposerViewHolderListener) {
        auxiliaryButtonViewListener = listener
        invokeViewCallbacks()
    }

    // ==================== Rich Text Configuration ====================

    fun setRichTextConfiguration(configuration: RichTextConfiguration) {
        richTextConfiguration = configuration
        richTextFormatterManager = RichTextFormatterManager(configuration)
        
        // Initialize FormatSpanWatcher if formats are now enabled and watcher doesn't exist yet
        if (configuration.hasAnyEnabled() && formatSpanWatcher == null) {
            formatSpanWatcher = FormatSpanWatcher(context)
            formatSpanWatcher?.attachTo(binding.etMessageInput.text)
        }
        
        // Disable autocorrect/suggestions when rich text is enabled.
        // Autocorrect can modify text and break formatting spans,
        // autocomplete suggestions can replace formatted text,
        // and spell check underlines interfere with visual formatting.
        if (configuration.hasAnyEnabled()) {
            binding.etMessageInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            binding.etMessageInput.imeOptions = binding.etMessageInput.imeOptions or
                android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        }
        
        // Update toolbar visibility based on current text state
        updateButtonVisibility()
    }
    
    /**
     * Returns whether the rich text toolbar is currently visible.
     * Visibility is based on: richTextToolbarVisibility=VISIBLE AND hasFormats
     */
    fun isRichTextToolbarVisible(): Boolean {
        return richTextToolbarVisibility == View.VISIBLE && richTextConfiguration.hasAnyEnabled()
    }

    // ==================== Style Setter ====================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatMessageComposerStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatMessageComposer
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatMessageComposerStyle.fromTypedArray(context, typedArray))
        }
    }

    // ==================== Getters (read from style object) ====================

    fun getComposerBackgroundColor(): Int = style.backgroundColor
    fun getComposerStrokeColor(): Int = style.strokeColor
    fun getComposerStrokeWidth(): Int = style.strokeWidth
    fun getComposerCornerRadius(): Int = style.cornerRadius
    fun getComposeBoxBackgroundColor(): Int = style.composeBoxBackgroundColor
    fun getComposeBoxStrokeColor(): Int = style.composeBoxStrokeColor
    fun getComposeBoxStrokeWidth(): Int = style.composeBoxStrokeWidth
    fun getComposeBoxCornerRadius(): Int = style.composeBoxCornerRadius
    fun getSeparatorColor(): Int = style.separatorColor
    fun getAttachmentIcon(): Drawable? = style.attachmentIcon
    fun getAttachmentIconTint(): Int = style.attachmentIconTint
    fun getVoiceRecordingIcon(): Drawable? = style.voiceRecordingIcon
    fun getVoiceRecordingIconTint(): Int = style.voiceRecordingIconTint
    fun getAIIcon(): Drawable? = style.aiIcon
    fun getAIIconTint(): Int = style.aiIconTint
    fun getStickerIcon(): Drawable? = style.stickerIcon
    fun getStickerIconTint(): Int = style.stickerIconTint
    fun getSendButtonActiveIcon(): Drawable? = style.sendButtonActiveIcon
    fun getSendButtonInactiveIcon(): Drawable? = style.sendButtonInactiveIcon
    fun getSendButtonActiveBackgroundColor(): Int = style.sendButtonActiveBackgroundColor
    fun getSendButtonInactiveBackgroundColor(): Int = style.sendButtonInactiveBackgroundColor
    fun getSendButtonStopIcon(): Drawable? = style.sendButtonStopIcon
    fun getEditPreviewTitleTextColor(): Int = style.editPreviewTitleTextColor
    fun getEditPreviewTitleTextAppearance(): Int = style.editPreviewTitleTextAppearance
    fun getEditPreviewMessageTextColor(): Int = style.editPreviewMessageTextColor
    fun getEditPreviewMessageTextAppearance(): Int = style.editPreviewMessageTextAppearance
    fun getEditPreviewBackgroundColor(): Int = style.editPreviewBackgroundColor
    fun getEditPreviewCornerRadius(): Int = style.editPreviewCornerRadius
    fun getEditPreviewStrokeColor(): Int = style.editPreviewStrokeColor
    fun getEditPreviewStrokeWidth(): Int = style.editPreviewStrokeWidth
    fun getEditPreviewCloseIcon(): Drawable? = style.editPreviewCloseIcon
    fun getEditPreviewCloseIconTint(): Int = style.editPreviewCloseIconTint
    fun getMessagePreviewSeparatorColor(): Int = style.messagePreviewSeparatorColor
    fun getMessagePreviewTitleTextColor(): Int = style.messagePreviewTitleTextColor
    fun getMessagePreviewTitleTextAppearance(): Int = style.messagePreviewTitleTextAppearance
    fun getMessagePreviewSubtitleTextColor(): Int = style.messagePreviewSubtitleTextColor
    fun getMessagePreviewSubtitleTextAppearance(): Int = style.messagePreviewSubtitleTextAppearance
    fun getMessagePreviewBackgroundColor(): Int = style.messagePreviewBackgroundColor
    fun getMessagePreviewCornerRadius(): Int = style.messagePreviewCornerRadius
    fun getMessagePreviewStrokeColor(): Int = style.messagePreviewStrokeColor
    fun getMessagePreviewStrokeWidth(): Int = style.messagePreviewStrokeWidth
    fun getMessagePreviewCloseIcon(): Drawable? = style.messagePreviewCloseIcon
    fun getMessagePreviewCloseIconTint(): Int = style.messagePreviewCloseIconTint
    fun getInputTextColor(): Int = style.inputTextColor
    fun getInputTextAppearance(): Int = style.inputTextAppearance
    fun getInputPlaceholderColor(): Int = style.inputPlaceholderColor
    fun getInputPlaceholderAppearance(): Int = style.inputPlaceholderAppearance
    fun getRichTextToolbarBackgroundColor(): Int = style.richTextToolbarBackgroundColor
    fun getRichTextToolbarIconTint(): Int = style.richTextToolbarIconTint
    fun getRichTextToolbarActiveIconTint(): Int = style.richTextToolbarActiveIconTint
    fun getRichTextToolbarActiveIconBackgroundColor(): Int = style.richTextToolbarActiveIconBackgroundColor

    // ==================== Setters (update style object + apply) ====================

    fun setComposerBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        if (color != 0) setCardBackgroundColor(color)
    }

    fun setComposerStrokeColor(@ColorInt color: Int) {
        style = style.copy(strokeColor = color)
        if (color != 0) setStrokeColor(color)
    }

    fun setComposerStrokeWidth(@Dimension width: Int) {
        style = style.copy(strokeWidth = width)
        if (width != 0) strokeWidth = width
    }

    fun setComposerCornerRadius(@Dimension radius: Int) {
        style = style.copy(cornerRadius = radius)
        if (radius != 0) this.radius = radius.toFloat()
    }

    fun setComposeBoxBackgroundColor(@ColorInt color: Int) {
        style = style.copy(composeBoxBackgroundColor = color)
        if (color != 0) binding.composeBoxCard.setCardBackgroundColor(color)
    }

    fun setComposeBoxStrokeColor(@ColorInt color: Int) {
        style = style.copy(composeBoxStrokeColor = color)
        if (color != 0) binding.composeBoxCard.setStrokeColor(color)
    }

    fun setComposeBoxStrokeWidth(@Dimension width: Int) {
        style = style.copy(composeBoxStrokeWidth = width)
        if (width != 0) binding.composeBoxCard.strokeWidth = width
    }

    fun setComposeBoxCornerRadius(@Dimension radius: Int) {
        style = style.copy(composeBoxCornerRadius = radius)
        if (radius != 0) binding.composeBoxCard.radius = radius.toFloat()
    }

    fun setSeparatorColor(@ColorInt color: Int) {
        style = style.copy(separatorColor = color)
        if (color != 0) {
            binding.separatorView.setBackgroundColor(color)
            binding.toolbarSeparator1.setBackgroundColor(color)
            binding.toolbarSeparator2.setBackgroundColor(color)
            binding.toolbarInputSeparator.setBackgroundColor(color)
            binding.multilineRow2Separator?.setBackgroundColor(color)
        }
    }

    fun setAttachmentIcon(icon: Drawable?) {
        style = style.copy(attachmentIcon = icon)
        icon?.let { binding.ivAttachment.setImageDrawable(it) }
        icon?.let { binding.ivMultilineAttachment?.setImageDrawable(it) }
    }

    fun setAttachmentIconTint(@ColorInt color: Int) {
        style = style.copy(attachmentIconTint = color)
        if (color != 0) binding.ivAttachment.setColorFilter(color)
        if (color != 0) binding.ivMultilineAttachment?.setColorFilter(color)
    }

    fun setVoiceRecordingIcon(icon: Drawable?) {
        style = style.copy(voiceRecordingIcon = icon)
        icon?.let { binding.ivVoiceRecording.setImageDrawable(it) }
        icon?.let { binding.ivMultilineVoiceRecording?.setImageDrawable(it) }
    }

    fun setVoiceRecordingIconTint(@ColorInt color: Int) {
        style = style.copy(voiceRecordingIconTint = color)
        if (color != 0) binding.ivVoiceRecording.setColorFilter(color)
        if (color != 0) binding.ivMultilineVoiceRecording?.setColorFilter(color)
    }

    fun setAIIcon(icon: Drawable?) {
        style = style.copy(aiIcon = icon)
        icon?.let { binding.ivAI.setImageDrawable(it) }
    }

    fun setAIIconTint(@ColorInt color: Int) {
        style = style.copy(aiIconTint = color)
        if (color != 0) binding.ivAI.setColorFilter(color)
    }

    fun setStickerIcon(icon: Drawable?) {
        style = style.copy(stickerIcon = icon)
        icon?.let { binding.ivSticker.setImageDrawable(it) }
        icon?.let { binding.ivMultilineSticker?.setImageDrawable(it) }
    }

    fun setStickerIconTint(@ColorInt color: Int) {
        style = style.copy(stickerIconTint = color)
        if (color != 0) binding.ivSticker.setColorFilter(color)
        if (color != 0) binding.ivMultilineSticker?.setColorFilter(color)
    }

    fun setSendButtonActiveIcon(icon: Drawable?) {
        style = style.copy(sendButtonActiveIcon = icon)
        updateSendButtonState(binding.etMessageInput.text?.isNotBlank() ?: false)
    }

    fun setSendButtonInactiveIcon(icon: Drawable?) {
        style = style.copy(sendButtonInactiveIcon = icon)
        updateSendButtonState(binding.etMessageInput.text?.isNotBlank() ?: false)
    }

    fun setSendButtonActiveBackgroundColor(@ColorInt color: Int) {
        style = style.copy(sendButtonActiveBackgroundColor = color)
        updateSendButtonState(binding.etMessageInput.text?.isNotBlank() ?: false)
    }

    fun setSendButtonInactiveBackgroundColor(@ColorInt color: Int) {
        style = style.copy(sendButtonInactiveBackgroundColor = color)
        updateSendButtonState(binding.etMessageInput.text?.isNotBlank() ?: false)
    }

    fun setSendButtonStopIcon(icon: Drawable?) {
        style = style.copy(sendButtonStopIcon = icon)
        updateSendButtonState(binding.etMessageInput.text?.isNotBlank() ?: false)
    }

    fun setEditPreviewTitleTextColor(@ColorInt color: Int) {
        style = style.copy(editPreviewTitleTextColor = color)
        if (color != 0) binding.tvEditPreviewTitle.setTextColor(color)
    }

    fun setEditPreviewTitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(editPreviewTitleTextAppearance = appearance)
        if (appearance != 0) binding.tvEditPreviewTitle.setTextAppearance(appearance)
    }

    fun setEditPreviewMessageTextColor(@ColorInt color: Int) {
        style = style.copy(editPreviewMessageTextColor = color)
        if (color != 0) binding.tvEditPreviewMessage.setTextColor(color)
    }

    fun setEditPreviewMessageTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(editPreviewMessageTextAppearance = appearance)
        if (appearance != 0) binding.tvEditPreviewMessage.setTextAppearance(appearance)
    }

    fun setEditPreviewBackgroundColor(@ColorInt color: Int) {
        style = style.copy(editPreviewBackgroundColor = color)
        if (color != 0) binding.editPreviewCard.setCardBackgroundColor(color)
    }

    fun setEditPreviewCornerRadius(@Dimension radius: Int) {
        style = style.copy(editPreviewCornerRadius = radius)
        if (radius != 0) binding.editPreviewCard.radius = radius.toFloat()
    }

    fun setEditPreviewStrokeColor(@ColorInt color: Int) {
        style = style.copy(editPreviewStrokeColor = color)
        if (color != 0) binding.editPreviewCard.setStrokeColor(color)
    }

    fun setEditPreviewStrokeWidth(@Dimension width: Int) {
        style = style.copy(editPreviewStrokeWidth = width)
        if (width != 0) binding.editPreviewCard.strokeWidth = width
    }

    fun setEditPreviewCloseIcon(icon: Drawable?) {
        style = style.copy(editPreviewCloseIcon = icon)
        icon?.let { binding.ivEditPreviewClose.setImageDrawable(it) }
    }

    fun setEditPreviewCloseIconTint(@ColorInt color: Int) {
        style = style.copy(editPreviewCloseIconTint = color)
        if (color != 0) binding.ivEditPreviewClose.setColorFilter(color)
    }

    fun setMessagePreviewSeparatorColor(@ColorInt color: Int) {
        style = style.copy(messagePreviewSeparatorColor = color)
        if (color != 0) binding.messagePreviewSeparator.setBackgroundColor(color)
    }

    fun setMessagePreviewTitleTextColor(@ColorInt color: Int) {
        style = style.copy(messagePreviewTitleTextColor = color)
        if (color != 0) binding.tvMessagePreviewTitle.setTextColor(color)
    }

    fun setMessagePreviewTitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(messagePreviewTitleTextAppearance = appearance)
        if (appearance != 0) binding.tvMessagePreviewTitle.setTextAppearance(appearance)
    }

    fun setMessagePreviewSubtitleTextColor(@ColorInt color: Int) {
        style = style.copy(messagePreviewSubtitleTextColor = color)
        if (color != 0) binding.tvMessagePreviewSubtitle.setTextColor(color)
    }

    fun setMessagePreviewSubtitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(messagePreviewSubtitleTextAppearance = appearance)
        if (appearance != 0) binding.tvMessagePreviewSubtitle.setTextAppearance(appearance)
    }

    fun setMessagePreviewBackgroundColor(@ColorInt color: Int) {
        style = style.copy(messagePreviewBackgroundColor = color)
        if (color != 0) binding.messagePreviewCard.setCardBackgroundColor(color)
    }

    fun setMessagePreviewCornerRadius(@Dimension radius: Int) {
        style = style.copy(messagePreviewCornerRadius = radius)
        if (radius != 0) binding.messagePreviewCard.radius = radius.toFloat()
    }

    fun setMessagePreviewStrokeColor(@ColorInt color: Int) {
        style = style.copy(messagePreviewStrokeColor = color)
        if (color != 0) binding.messagePreviewCard.setStrokeColor(color)
    }

    fun setMessagePreviewStrokeWidth(@Dimension width: Int) {
        style = style.copy(messagePreviewStrokeWidth = width)
        if (width != 0) binding.messagePreviewCard.strokeWidth = width
    }

    fun setMessagePreviewCloseIcon(icon: Drawable?) {
        style = style.copy(messagePreviewCloseIcon = icon)
        icon?.let { binding.ivMessagePreviewClose.setImageDrawable(it) }
    }

    fun setMessagePreviewCloseIconTint(@ColorInt color: Int) {
        style = style.copy(messagePreviewCloseIconTint = color)
        if (color != 0) binding.ivMessagePreviewClose.setColorFilter(color)
    }

    fun setInputTextColor(@ColorInt color: Int) {
        style = style.copy(inputTextColor = color)
        if (color != 0) binding.etMessageInput.setTextColor(color)
    }

    fun setInputTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(inputTextAppearance = appearance)
        if (appearance != 0) binding.etMessageInput.setTextAppearance(appearance)
    }

    fun setInputPlaceholderColor(@ColorInt color: Int) {
        style = style.copy(inputPlaceholderColor = color)
        if (color != 0) binding.etMessageInput.setHintTextColor(color)
    }

    fun setInputPlaceholderAppearance(@StyleRes appearance: Int) {
        style = style.copy(inputPlaceholderAppearance = appearance)
        // Hint appearance is handled via text appearance
    }

    fun setRichTextToolbarBackgroundColor(@ColorInt color: Int) {
        style = style.copy(richTextToolbarBackgroundColor = color)
        if (color != 0) binding.richTextToolbarLayout.setBackgroundColor(color)
    }

    fun setRichTextToolbarIconTint(@ColorInt color: Int) {
        style = style.copy(richTextToolbarIconTint = color)
        if (color != 0) applyRichTextToolbarIconTints()
    }

    fun setRichTextToolbarActiveIconTint(@ColorInt color: Int) {
        style = style.copy(richTextToolbarActiveIconTint = color)
        // Applied when format buttons are toggled active
        updateToolbarButtonStates()
    }

    fun setRichTextToolbarActiveIconBackgroundColor(@ColorInt color: Int) {
        style = style.copy(richTextToolbarActiveIconBackgroundColor = color)
        updateToolbarButtonStates()
    }

    // ==================== Text Formatters ====================

    /**
     * Sets the text formatters for mention and other text formatting features.
     * This initializes the mention helper and sets up formatter observers.
     * Also applies the mention text style to CometChatMentionsFormatter instances.
     * 
     * Note: This method ADDS formatters to the existing list (which includes the default
     * CometChatMentionsFormatter), similar to the Java implementation behavior.
     * 
     * @param formatters List of text formatters to add
     */
    fun setTextFormatters(formatters: List<CometChatTextFormatter>?) {
        android.util.Log.d(TAG, "setTextFormatters: received ${formatters?.size ?: 0} formatters")
        
        // If null, just process existing formatters (like Java implementation)
        if (formatters == null) {
            android.util.Log.d(TAG, "setTextFormatters: formatters is null, processing existing ${textFormatters.size} formatters")
            processFormatters()
            return
        }
        
        // Add new formatters to existing list (like Java implementation)
        textFormatters.addAll(formatters)
        android.util.Log.d(TAG, "setTextFormatters: added formatters, total now ${textFormatters.size}")
        
        processFormatters()
    }
    
    /**
     * Processes all text formatters - sets up observers and applies user/group context.
     * This is called after formatters are added to ensure they're properly configured.
     */
    private fun processFormatters() {
        android.util.Log.d(TAG, "processFormatters: processing ${textFormatters.size} formatters")
        
        // Update formatters with current user/group context and apply mention style
        for (formatter in textFormatters) {
            android.util.Log.d(TAG, "processFormatters: processing formatter ${formatter.javaClass.simpleName}")
            user?.let { formatter.setUser(it) }
            group?.let { formatter.setGroup(it) }
            
            // Apply mention text style to CometChatMentionsFormatter instances
            if (formatter is CometChatMentionsFormatter) {
                val styleToApply = if (mentionTextStyle != 0) {
                    mentionTextStyle
                } else {
                    R.style.CometChatMessageComposerMentionsStyle
                }
                formatter.setMessageComposerMentionTextStyle(context, styleToApply)
            }
        }
        
        // Re-initialize mention helper with updated formatters
        android.util.Log.d(TAG, "processFormatters: calling initMentionHelper")
        initMentionHelper()
    }

    /**
     * Gets the current text formatters.
     */
    fun getTextFormatters(): List<CometChatTextFormatter> = textFormatters.toList()

    /**
     * Sets the mention text style for the message composer.
     * This style is applied to CometChatMentionsFormatter instances when setTextFormatters is called.
     * 
     * @param style The style resource ID (e.g., R.style.CometChatMessageComposerMentionsStyle)
     */
    fun setMentionTextStyle(@StyleRes style: Int) {
        mentionTextStyle = style
        
        // Apply to existing formatters if already set
        for (formatter in textFormatters) {
            if (formatter is CometChatMentionsFormatter) {
                formatter.setMessageComposerMentionTextStyle(context, style)
            }
        }
    }

    /**
     * Gets the mention text style resource ID.
     */
    @StyleRes
    fun getMentionTextStyle(): Int = mentionTextStyle

    /**
     * Sets the style for the suggestion list.
     * 
     * @param style The style configuration for the suggestion list
     */
    fun setSuggestionListStyle(style: CometChatSuggestionListStyle) {
        suggestionListStyle = style
        suggestionList?.setStyle(style)
    }

    /**
     * Gets the suggestion list style.
     */
    fun getSuggestionListStyle(): CometChatSuggestionListStyle? = suggestionListStyle

    /**
     * Sets the maximum height for the suggestion list.
     * 
     * @param maxHeight Maximum height in pixels
     */
    fun setSuggestionListMaxHeight(maxHeight: Int) {
        suggestionList?.setMaxHeightLimit(maxHeight)
    }

    /**
     * Shows or hides avatars in the suggestion list.
     * 
     * @param show True to show avatars, false to hide
     */
    fun showSuggestionListAvatar(show: Boolean) {
        suggestionList?.showAvatar(show)
    }

    // ==================== ViewModel Setter ====================

    fun setViewModel(viewModel: CometChatMessageComposerViewModel) {
        this.viewModel = viewModel
        isExternalViewModel = true
        viewModel.disableTypingEvents = disableTypingEvents
        startCollectingFlows()
    }

    // ==================== Lifecycle ====================

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel?.let { vm ->
            user?.let { vm.setUser(it) }
            group?.let { vm.setGroup(it) }
        }
    }

    override fun onDetachedFromWindow() {
        viewScope?.cancel()
        viewModel?.endTyping()
        hideStickerKeyboard()
        
        // Clean up search query timer
        searchQueryTimer?.cancel()
        searchQueryTimer = null
        
        // Clean up mention resources
        removeFormatterObservers()
        mentionHelper?.cleanup()
        mentionHelper = null
        
        super.onDetachedFromWindow()
    }

    // ==================== Sticker Keyboard ====================

    /**
     * Toggles the sticker keyboard visibility.
     * Shows the keyboard if hidden, hides it if visible.
     */
    fun toggleStickerKeyboard() {
        if (isStickerKeyboardVisible) {
            hideStickerKeyboard()
        } else {
            showStickerKeyboard()
        }
    }

    /**
     * Shows the sticker keyboard in the bottom panel with smooth expand animation.
     * Creates a fresh sticker keyboard instance each time (like old Java implementation).
     */
    fun showStickerKeyboard() {
        if (isStickerKeyboardVisible) return

        // Hide soft keyboard first
        Utils.hideKeyBoard(context, binding.etMessageInput)
        binding.etMessageInput.clearFocus()

        // Remove any existing view and create fresh instance (like old Java implementation)
        binding.bottomPanelLayout.removeAllViews()
        stickerKeyboard = CometChatStickerKeyboard(context).apply {
            // Set fixed height on the sticker keyboard itself (like old Java)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.cometchat_296dp)
            )
            stickerKeyboardStyle?.let { setStyle(it) }
            setStickerClickListener { sticker ->
                // Send the sticker as a custom message
                val pushNotificationMessage = context.getString(R.string.cometchat_shared_sticker)
                viewModel?.sendStickerMessage(
                    stickerUrl = sticker.url,
                    stickerName = sticker.name,
                    pushNotificationMessage = pushNotificationMessage
                )
                // Also invoke the callback for any additional handling
                onStickerSelected?.invoke(sticker)
            }
            setOnError { exception ->
                onError?.invoke(exception)
            }
        }
        binding.bottomPanelLayout.addView(stickerKeyboard)
        
        // Animate visibility with smooth expand animation
        AnimationUtils.animateVisibilityVisible(binding.bottomPanelLayout)
        isStickerKeyboardVisible = true
    }

    /**
     * Hides the sticker keyboard with smooth collapse animation.
     * Removes the sticker keyboard instance (like old Java implementation).
     */
    fun hideStickerKeyboard() {
        if (!isStickerKeyboardVisible) return

        // Animate visibility with smooth collapse animation
        AnimationUtils.animateVisibilityGone(binding.bottomPanelLayout)
        
        // Remove the sticker keyboard after animation completes (like old Java)
        binding.bottomPanelLayout.postDelayed({
            binding.bottomPanelLayout.removeAllViews()
            stickerKeyboard = null
        }, 300)
        
        isStickerKeyboardVisible = false
    }

    /**
     * Returns whether the sticker keyboard is currently visible.
     */
    fun isStickerKeyboardVisible(): Boolean = isStickerKeyboardVisible

    /**
     * Sets the style for the sticker keyboard.
     *
     * @param style The style configuration to apply
     */
    fun setStickerKeyboardStyle(style: CometChatStickerKeyboardStyle) {
        this.stickerKeyboardStyle = style
        stickerKeyboard?.setStyle(style)
    }

    /**
     * Sets the callback for when a sticker is selected.
     *
     * @param listener Lambda invoked when a sticker is clicked
     */
    fun setOnStickerSelected(listener: ((Sticker) -> Unit)?) {
        this.onStickerSelected = listener
        stickerKeyboard?.setStickerClickListener { sticker ->
            // Send the sticker as a custom message
            val pushNotificationMessage = context.getString(R.string.cometchat_shared_sticker)
            viewModel?.sendStickerMessage(
                stickerUrl = sticker.url,
                stickerName = sticker.name,
                pushNotificationMessage = pushNotificationMessage
            )
            // Also invoke the callback for any additional handling
            listener?.invoke(sticker)
        }
    }

    /**
     * Gets the bottom panel layout for custom content.
     */
    fun getBottomPanelLayout(): android.widget.FrameLayout = binding.bottomPanelLayout
}
