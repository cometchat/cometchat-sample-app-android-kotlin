package com.cometchat.uikit.kotlin.presentation.messagecomposer.ui

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
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
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatMessageComposerViewModelFactory
import com.cometchat.uikit.core.formatter.FormatCompatibility
import com.cometchat.uikit.core.formatter.RichTextConfiguration
import com.cometchat.uikit.core.formatter.RichTextEditorController
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.core.formatter.RichTextFormatterManager
import com.cometchat.uikit.core.state.MessageComposerUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import com.cometchat.uikit.core.domain.model.CometChatMessageComposerAction
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatMessageComposerBinding
import com.cometchat.uikit.kotlin.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.kotlin.presentation.messagecomposer.utils.MessageComposerViewHolderListener
import com.cometchat.uikit.kotlin.presentation.polls.ui.CometChatCreatePoll
import com.cometchat.uikit.kotlin.presentation.shared.mediarecorder.CometChatMediaRecorder
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

    // Rich text formatting - using RichTextEditorController from chatuikit-core (same as Jetpack)
    private val richTextController = RichTextEditorController()
    private var richTextFormatterManager: RichTextFormatterManager? = null
    private var richTextConfiguration: RichTextConfiguration = RichTextConfiguration()
    private var richTextToolbarVisibility: Int = View.GONE
    private var isApplyingRichTextStyling: Boolean = false
    
    // Track active formats for toolbar state
    private var activeFormats: Set<RichTextFormat> = emptySet()

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

    // Visibility controls
    private var hideAttachmentButton: Boolean = false
    private var hideVoiceRecordingButton: Boolean = false
    private var hideAIButton: Boolean = true
    private var hideStickerButton: Boolean = true
    private var hideEditPreview: Boolean = false
    private var hideMessagePreview: Boolean = false

    // Attachment popup
    private var attachmentPopup: CometChatPopupMenu? = null

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
        initViewModel()
        initRichTextFormatter()
        initSuggestionList()
        // Initialize default mentions formatter (like Java implementation)
        processMentionsFormatter()
        android.util.Log.d(TAG, "init: initialization complete, textFormatters.size=${textFormatters.size}")
    }

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
        
        mentionHelper?.onSuggestionSelected(item, result)
        
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
            binding.toolbarSeparator3.setBackgroundColor(style.separatorColor)
            binding.toolbarSeparator4.setBackgroundColor(style.separatorColor)
            binding.toolbarInputSeparator.setBackgroundColor(style.separatorColor)
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
        if (style.richTextToolbarBackgroundColor != 0) binding.richTextToolbarLayout.setBackgroundColor(style.richTextToolbarBackgroundColor)
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
        val hasText = binding.etMessageInput.text?.isNotEmpty() == true
        
        binding.ivAttachment.visibility = if (hideAttachmentButton) View.GONE else View.VISIBLE
        binding.secondaryButtonLayout.visibility = if (hideAttachmentButton) View.GONE else View.VISIBLE
        binding.separatorView.visibility = if (hideAttachmentButton) View.GONE else View.VISIBLE
        
        // Hide voice recording and sticker buttons when text is entered with animation
        val shouldShowVoiceRecording = !hideVoiceRecordingButton && !hasText
        val shouldShowSticker = !hideStickerButton && !hasText
        
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
            binding.toolbarSeparator3.setBackgroundColor(style.separatorColor)
            binding.toolbarSeparator4.setBackgroundColor(style.separatorColor)
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
            if (text.isNotBlank()) {
                handleSendClick(text)
            }
        }

        // Edit preview close
        binding.ivEditPreviewClose.setOnClickListener {
            viewModel?.clearEditMessage()
            binding.etMessageInput.setText("")
        }

        // Message preview close
        binding.ivMessagePreviewClose.setOnClickListener {
            viewModel?.clearReplyMessage()
        }

        // Rich text toggle button removed - toolbar visibility is now automatic based on text presence

        // Rich text format buttons
        setupRichTextFormatClickListeners()
    }

    /**
     * Sets up click listeners for rich text format buttons.
     */
    private fun setupRichTextFormatClickListeners() {
        binding.ivFormatBold.setOnClickListener { toggleFormat(RichTextFormat.BOLD) }
        binding.ivFormatItalic.setOnClickListener { toggleFormat(RichTextFormat.ITALIC) }
        binding.ivFormatStrikethrough.setOnClickListener { toggleFormat(RichTextFormat.STRIKETHROUGH) }
        binding.ivFormatCode.setOnClickListener { toggleFormat(RichTextFormat.INLINE_CODE) }
        binding.ivFormatCodeBlock.setOnClickListener { toggleFormat(RichTextFormat.CODE_BLOCK) }
        binding.ivFormatLink.setOnClickListener { showLinkDialog() }
        binding.ivFormatBulletList.setOnClickListener { toggleFormat(RichTextFormat.BULLET_LIST) }
        binding.ivFormatOrderedList.setOnClickListener { toggleFormat(RichTextFormat.ORDERED_LIST) }
        binding.ivFormatBlockquote.setOnClickListener { toggleFormat(RichTextFormat.BLOCKQUOTE) }
        
        // Set up listener for state changes from RichTextEditorController
        richTextController.setListener(object : RichTextEditorController.Listener {
            override fun onStateChanged() {
                activeFormats = richTextController.state.activeFormats
                updateToolbarButtonStates()
            }
        })
    }

    /**
     * Toggles a rich text format using RichTextEditorController (same as Jetpack).
     */
    private fun toggleFormat(format: RichTextFormat) {
        if (!richTextConfiguration.hasAnyEnabled()) return
        
        // Sync controller state with current EditText before toggling
        val currentText = binding.etMessageInput.text?.toString() ?: ""
        val selStart = binding.etMessageInput.selectionStart
        val selEnd = binding.etMessageInput.selectionEnd
        
        // Ensure controller has the latest text and selection
        if (richTextController.state.text != currentText ||
            richTextController.state.selectionStart != selStart ||
            richTextController.state.selectionEnd != selEnd) {
            richTextController.onTextChanged(currentText, selStart, selEnd)
        }
        
        richTextController.toggleFormat(format)
        
        // Update the EditText with the new text from controller
        val controllerText = richTextController.state.text
        
        if (controllerText != currentText) {
            isApplyingRichTextStyling = true
            try {
                binding.etMessageInput.setText(controllerText)
                binding.etMessageInput.setSelection(
                    richTextController.state.selectionStart.coerceIn(0, controllerText.length),
                    richTextController.state.selectionEnd.coerceIn(0, controllerText.length)
                )
            } finally {
                isApplyingRichTextStyling = false
            }
        }
        
        // Always apply styling after toggling (whether text changed or not)
        // Use post to ensure the EditText has been updated
        binding.etMessageInput.post {
            applyInlineRichTextStyling(binding.etMessageInput.text)
        }
        
        updateToolbarButtonStates()
    }
    
    /**
     * Updates the toolbar button states based on active and disabled formats.
     */
    private fun updateToolbarButtonStates() {
        val activeTint = style.richTextToolbarActiveIconTint.takeIf { it != 0 } 
            ?: CometChatTheme.getPrimaryColor(context)
        val inactiveTint = style.richTextToolbarIconTint.takeIf { it != 0 }
            ?: CometChatTheme.getIconTintSecondary(context)
        val disabledTint = (style.richTextToolbarIconTint.takeIf { it != 0 }
            ?: CometChatTheme.getIconTintSecondary(context)).let { 
                android.graphics.Color.argb(
                    (android.graphics.Color.alpha(it) * 0.3f).toInt(),
                    android.graphics.Color.red(it),
                    android.graphics.Color.green(it),
                    android.graphics.Color.blue(it)
                )
            }
        
        val disabledFormats = richTextController.state.toolbarDisabledFormats
        
        // Update each button's tint based on active/disabled state
        updateFormatButtonTint(binding.ivFormatBold, RichTextFormat.BOLD, activeTint, inactiveTint, disabledTint, disabledFormats)
        updateFormatButtonTint(binding.ivFormatItalic, RichTextFormat.ITALIC, activeTint, inactiveTint, disabledTint, disabledFormats)
        updateFormatButtonTint(binding.ivFormatStrikethrough, RichTextFormat.STRIKETHROUGH, activeTint, inactiveTint, disabledTint, disabledFormats)
        updateFormatButtonTint(binding.ivFormatCode, RichTextFormat.INLINE_CODE, activeTint, inactiveTint, disabledTint, disabledFormats)
        updateFormatButtonTint(binding.ivFormatCodeBlock, RichTextFormat.CODE_BLOCK, activeTint, inactiveTint, disabledTint, disabledFormats)
        updateFormatButtonTint(binding.ivFormatLink, RichTextFormat.LINK, activeTint, inactiveTint, disabledTint, disabledFormats)
        updateFormatButtonTint(binding.ivFormatBulletList, RichTextFormat.BULLET_LIST, activeTint, inactiveTint, disabledTint, disabledFormats)
        updateFormatButtonTint(binding.ivFormatOrderedList, RichTextFormat.ORDERED_LIST, activeTint, inactiveTint, disabledTint, disabledFormats)
        updateFormatButtonTint(binding.ivFormatBlockquote, RichTextFormat.BLOCKQUOTE, activeTint, inactiveTint, disabledTint, disabledFormats)
    }
    
    /**
     * Updates a single format button's tint based on its state.
     */
    private fun updateFormatButtonTint(
        button: View,
        format: RichTextFormat,
        activeTint: Int,
        inactiveTint: Int,
        disabledTint: Int,
        disabledFormats: Set<RichTextFormat>
    ) {
        val imageView = button as? android.widget.ImageView ?: return
        val tint = when {
            format in disabledFormats -> disabledTint
            format in activeFormats -> activeTint
            else -> inactiveTint
        }
        imageView.setColorFilter(tint)
        imageView.isEnabled = format !in disabledFormats
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
     * Shows the link edit dialog.
     */
    private fun showLinkDialog() {
        // TODO: Implement link dialog using CometChatDialog
    }

    /**
     * Toggles the attachment popup visibility.
     * Shows the popup above the attachment button with animated icon rotation.
     */
    private fun toggleAttachmentPopup() {
        attachmentPopup?.dismiss()
        
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
            
            // Set dismiss listener to rotate icon back
            setOnDismissListener {
                binding.ivAttachment.animate()
                    .rotation(0f)
                    .setDuration(200)
                    .start()
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
     * Handles the activity result from camera or file pickers.
     * Extracts the file and sends it as a media message.
     */
    private fun handleActivityResult(result: androidx.activity.result.ActivityResult) {
        try {
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
            .withIntent(MediaUtils.openImagePicker())
            .launch()
    }

    /**
     * Launches the video picker with permission handling.
     * Uses CometChatPermissionHandler for both permission requests and activity results.
     */
    private fun launchVideoPickerWithMediaHelper() {
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
            .withIntent(MediaUtils.openVideoPicker())
            .launch()
    }

    /**
     * Launches the audio picker with permission handling.
     * Uses CometChatPermissionHandler for both permission requests and activity results.
     */
    private fun launchAudioPickerWithMediaHelper() {
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
            .withIntent(MediaUtils.openAudioPicker(context))
            .launch()
    }

    /**
     * Launches the file picker with permission handling.
     * Uses CometChatPermissionHandler for both permission requests and activity results.
     */
    private fun launchFilePickerWithMediaHelper() {
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
            .withIntent(MediaUtils.openFilePicker())
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
                    viewModel?.sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_AUDIO)
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
     * Handles send button click.
     * Uses RichTextEditorController.toMarkdown() for proper markdown conversion (same as Jetpack).
     * Processes mentions before sending to replace spans with underlying text.
     */
    private fun handleSendClick(text: String) {
        android.util.Log.d(TAG, "handleSendClick: input text='$text'")
        android.util.Log.d(TAG, "handleSendClick: mentionHelper=${mentionHelper != null}")
        
        // Get processed text with mentions replaced by underlying format
        val processedText = mentionHelper?.getProcessedText() ?: text
        
        android.util.Log.d(TAG, "handleSendClick: processedText='$processedText'")
        android.util.Log.d(TAG, "handleSendClick: text changed=${text != processedText}")
        
        // Use RichTextEditorController to convert to markdown if rich text is enabled
        val markdownText = if (richTextConfiguration.hasAnyEnabled()) {
            // Apply markdown conversion to the processed text
            richTextController.onTextChanged(processedText, processedText.length, processedText.length)
            richTextController.toMarkdown()
        } else {
            // Fallback: Convert display formats back to markdown format
            processedText.replace(Regex("^• ", RegexOption.MULTILINE), "- ")
        }
        
        android.util.Log.d(TAG, "handleSendClick: markdownText='$markdownText'")
        
        val editMessage = viewModel?.editMessage?.value
        if (editMessage != null) {
            // Edit mode - call handlePreMessageSend on formatters
            for (formatter in textFormatters) {
                formatter.handlePreMessageSend(context, editMessage)
            }
            viewModel?.editMessage(markdownText)
        } else {
            // Send mode - use ViewModel's createTextMessage and set mentioned users
            val message = viewModel?.createTextMessage(markdownText)
            if (message != null) {
                // Call handlePreMessageSend on all formatters (sets mentionedUsers)
                for (formatter in textFormatters) {
                    formatter.handlePreMessageSend(context, message)
                }
                
                // Send the message with mentioned users already set
                viewModel?.sendTextMessageWithMentions(message)
            }
            onSendButtonClick?.invoke(markdownText)
        }
        
        // Clear input and rich text controller
        binding.etMessageInput.setText("")
        if (richTextConfiguration.hasAnyEnabled()) {
            richTextController.clear()
        }
        
        // Clear mention helper state
        mentionHelper?.clear()
        
        // Reset formatters' selected lists
        for (formatter in textFormatters) {
            formatter.setSelectedList(context, emptyList())
        }
    }

    /**
     * Sets up text watcher for the input field.
     * Syncs text changes with RichTextEditorController (same as Jetpack).
     */
    private fun setupTextWatcher() {
        binding.etMessageInput.addTextChangedListener(object : TextWatcher {
            private var previousText: String = ""
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Skip if we're applying rich text styling to avoid recursion
                if (isApplyingRichTextStyling) return
                
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
                
                // Sync with RichTextEditorController (same as Jetpack)
                if (richTextConfiguration.hasAnyEnabled()) {
                    val selStart = binding.etMessageInput.selectionStart
                    val selEnd = binding.etMessageInput.selectionEnd
                    richTextController.onTextChanged(text, selStart, selEnd)
                    
                    // Check if controller modified the text (e.g., list auto-continuation or exit)
                    val controllerText = richTextController.state.text
                    val controllerSelStart = richTextController.state.selectionStart
                    val controllerSelEnd = richTextController.state.selectionEnd
                    
                    if (controllerText != text) {
                        isApplyingRichTextStyling = true
                        try {
                            binding.etMessageInput.setText(controllerText)
                            binding.etMessageInput.setSelection(
                                controllerSelStart.coerceIn(0, controllerText.length),
                                controllerSelEnd.coerceIn(0, controllerText.length)
                            )
                        } finally {
                            isApplyingRichTextStyling = false
                        }
                    }
                    
                    // Update active formats and toolbar state
                    activeFormats = richTextController.state.activeFormats
                    updateToolbarButtonStates()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Skip if we're already applying styling to avoid infinite loop
                if (isApplyingRichTextStyling) return
                
                // Apply inline rich text styling for visual feedback
                applyInlineRichTextStyling(s)
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
     */
    private fun updateSendButtonState(hasText: Boolean) {
        val isAIGenerating = viewModel?.isAIGenerating?.value ?: false

        when {
            isAIGenerating -> {
                style.sendButtonStopIcon?.let { binding.ivSend.setImageDrawable(it) }
                    ?: binding.ivSend.setImageResource(R.drawable.cometchat_ic_stop)
                applySendButtonBackground(style.sendButtonActiveBackgroundColor)
            }
            hasText -> {
                style.sendButtonActiveIcon?.let { binding.ivSend.setImageDrawable(it) }
                    ?: binding.ivSend.setImageResource(R.drawable.cometchat_ic_send_active)
                applySendButtonBackground(style.sendButtonActiveBackgroundColor)
            }
            else -> {
                // Use active icon for inactive state too - only background color changes
                style.sendButtonActiveIcon?.let { binding.ivSend.setImageDrawable(it) }
                    ?: binding.ivSend.setImageResource(R.drawable.cometchat_ic_send_active)
                applySendButtonBackground(style.sendButtonInactiveBackgroundColor)
            }
        }
        
        // Apply icon tint (white for both states)
        binding.ivSend.setColorFilter(CometChatTheme.getColorWhite(context))
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
            viewModel = CometChatMessageComposerViewModelFactory()
                .create(CometChatMessageComposerViewModel::class.java)
        }
        startCollectingFlows()
    }

    /**
     * Initializes the rich text formatter manager.
     */
    private fun initRichTextFormatter() {
        richTextFormatterManager = RichTextFormatterManager(richTextConfiguration)
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
                onError?.invoke(error)
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
                val textMessage = state.message as? TextMessage
                textMessage?.let {
                    // Run formatter pipeline to resolve mention tokens (e.g., <@uid:userId> -> @userName)
                    var spannableBuilder = SpannableStringBuilder(it.text ?: "")
                    for (formatter in textFormatters) {
                        spannableBuilder = formatter.prepareMessageString(
                            context,
                            it,
                            spannableBuilder,
                            UIKitConstants.MessageBubbleAlignment.RIGHT,
                            UIKitConstants.FormattingType.MESSAGE_COMPOSER
                        ) ?: spannableBuilder
                    }
                    binding.etMessageInput.setText(spannableBuilder)
                    binding.etMessageInput.setSelection(spannableBuilder.length)
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
    private fun updateEditPreview(message: TextMessage?) {
        if (message != null && !hideEditPreview) {
            binding.editPreviewCard.visibility = View.VISIBLE
            
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
            binding.tvEditPreviewMessage.text = spannableBuilder
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
                    message.attachment?.fileName ?: when (message.type) {
                        "image" -> context.getString(R.string.cometchat_message_image)
                        "video" -> context.getString(R.string.cometchat_message_video)
                        "audio" -> context.getString(R.string.cometchat_message_audio)
                        "file" -> context.getString(R.string.cometchat_message_document)
                        else -> message.type ?: ""
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
        viewModel?.clearEditMessage()
        binding.etMessageInput.setText("")
    }

    /**
     * Sets the message to be replied to.
     */
    fun setReplyMessage(message: BaseMessage) {
        viewModel?.setReplyMessage(message)
    }

    /**
     * Clears the reply message state.
     */
    fun clearReplyMessage() {
        viewModel?.clearReplyMessage()
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
        hideAttachmentButton = hide
        updateButtonVisibility()
    }

    fun setHideVoiceRecordingButton(hide: Boolean) {
        hideVoiceRecordingButton = hide
        updateButtonVisibility()
    }

    fun setHideAIButton(hide: Boolean) {
        hideAIButton = hide
        updateButtonVisibility()
    }

    fun setHideStickerButton(hide: Boolean) {
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
        richTextToolbarVisibility = visibility
        // Auto-enable all formats if visibility is VISIBLE and no formats are configured
        if (visibility == View.VISIBLE && !richTextConfiguration.hasAnyEnabled()) {
            setRichTextConfiguration(RichTextConfiguration.allEnabled())
        }
        updateButtonVisibility()
    }

    /**
     * Gets the visibility of the rich text formatting toolbar.
     * 
     * @return View.VISIBLE or View.GONE
     */
    fun getRichTextToolbarVisibility(): Int = richTextToolbarVisibility

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
            binding.toolbarSeparator3.setBackgroundColor(color)
            binding.toolbarSeparator4.setBackgroundColor(color)
            binding.toolbarInputSeparator.setBackgroundColor(color)
        }
    }

    fun setAttachmentIcon(icon: Drawable?) {
        style = style.copy(attachmentIcon = icon)
        icon?.let { binding.ivAttachment.setImageDrawable(it) }
    }

    fun setAttachmentIconTint(@ColorInt color: Int) {
        style = style.copy(attachmentIconTint = color)
        if (color != 0) binding.ivAttachment.setColorFilter(color)
    }

    fun setVoiceRecordingIcon(icon: Drawable?) {
        style = style.copy(voiceRecordingIcon = icon)
        icon?.let { binding.ivVoiceRecording.setImageDrawable(it) }
    }

    fun setVoiceRecordingIconTint(@ColorInt color: Int) {
        style = style.copy(voiceRecordingIconTint = color)
        if (color != 0) binding.ivVoiceRecording.setColorFilter(color)
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
    }

    fun setStickerIconTint(@ColorInt color: Int) {
        style = style.copy(stickerIconTint = color)
        if (color != 0) binding.ivSticker.setColorFilter(color)
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
