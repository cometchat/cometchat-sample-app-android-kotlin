package com.cometchat.uikit.kotlin.presentation.conversations.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.RawRes
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.resources.soundmanager.CometChatSoundManager
import com.cometchat.uikit.core.resources.soundmanager.Sound
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import com.cometchat.uikit.core.factory.CometChatConversationsViewModelFactory
import com.cometchat.uikit.core.state.UIState
import com.cometchat.uikit.core.state.DeleteState
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatConversationsListViewBinding
import com.cometchat.uikit.kotlin.presentation.conversations.style.CometChatConversationsStyle
import com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationsViewHolderListener
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.kotlin.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.kotlin.shared.formatters.style.CometChatMentionStyle
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.HashMap


/**
 * CometChatConversations is a custom Android View that displays a list of conversations
 * with support for real-time updates, selection modes, custom views, and full styling.
 *
 * This component uses the shared CometChatConversationsViewModel from chatuikit-core,
 * ensuring consistent business logic with the Jetpack Compose version.
 *
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.ui.conversations.CometChatConversations
 *     android:id="@+id/conversationList"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:cometchatConversationsStyle="@style/CometChatConversationsStyle" />
 * ```
 *
 * Usage in Kotlin:
 * ```kotlin
 * val conversationList = CometChatConversations(context)
 * conversationList.setOnItemClick { conversation ->
 *     // Handle click
 * }
 * ```
 */
class CometChatConversations @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatConversationsStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatConversations::class.java.simpleName
    }

    // View Binding
    private val binding: CometchatConversationsListViewBinding

    // ViewModel (shared from chatuikit-core)
    // Can be externally provided via setViewModel() or internally created
    private var viewModel: CometChatConversationsViewModel? = null
    
    // Flag to track if ViewModel was externally provided
    private var isExternalViewModel: Boolean = false

    // Adapter
    private val conversationsAdapter: ConversationsAdapter

    // Lifecycle owner for observing flows
    private var lifecycleOwner: LifecycleOwner? = null

    // Coroutine scope for collecting flows
    private var viewScope: CoroutineScope? = null

    // Selection state
    private val selectedConversations = HashMap<Conversation, Boolean>()
    private var isFurtherSelectionEnabled = true

    // Callbacks
    private var onItemClick: ((Conversation) -> Unit)? = null
    private var onItemLongClick: ((Conversation) -> Unit)? = null
    private var onError: ((CometChatException) -> Unit)? = null
    private var onLoad: ((List<Conversation>) -> Unit)? = null
    private var onEmpty: (() -> Unit)? = null
    private var onBackPress: (() -> Unit)? = null
    private var onSearchClick: (() -> Unit)? = null
    private var onSelection: ((List<Conversation>) -> Unit)? = null

    // Configuration
    private var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE
    private var disableSoundForMessages: Boolean = false
    @RawRes private var customSoundForMessage: Int = 0

    // Custom views using ViewHolderListener pattern (matching chatuikit)
    private var itemViewListener: ConversationsViewHolderListener? = null
    private var leadingViewListener: ConversationsViewHolderListener? = null
    private var titleViewListener: ConversationsViewHolderListener? = null
    private var subtitleViewListener: ConversationsViewHolderListener? = null
    private var trailingViewListener: ConversationsViewHolderListener? = null

    // Custom state views
    private var customEmptyView: View? = null
    private var customErrorView: View? = null
    private var customLoadingView: View? = null

    // Menu options
    private var options: ((Context, Conversation) -> List<CometChatPopupMenu.MenuItem>)? = null
    private var addOptions: ((Context, Conversation) -> List<CometChatPopupMenu.MenuItem>)? = null

    // Text formatters
    private val textFormatters: MutableList<CometChatTextFormatter> = mutableListOf()
    private var cometchatMentionsFormatter: CometChatMentionsFormatter? = null

    // Popup menu
    private val popupMenu: CometChatPopupMenu

    // Sound manager
    private val soundManager: CometChatSoundManager

    // Delete dialog
    private var deleteAlertDialog: CometChatConfirmDialog? = null


    // Visibility controls
    private var toolbarVisibility = VISIBLE
    private var backIconVisibility = GONE
    private var searchBoxVisibility = VISIBLE
    private var deleteConversationOptionVisibility = VISIBLE
    private var userStatusVisibility = VISIBLE
    private var groupTypeVisibility = VISIBLE
    private var receiptsVisibility = VISIBLE
    private var separatorVisibility = VISIBLE
    private var errorStateVisibility = VISIBLE
    private var loadingStateVisibility = VISIBLE
    private var emptyStateVisibility = VISIBLE

    // Single style object - replaces individual style properties
    private var style: CometChatConversationsStyle = CometChatConversationsStyle()
    
    // Stored style resource ID for refreshStyle
    @StyleRes private var currentStyleResId: Int = 0

    init {
        // Inflate layout
        binding = CometchatConversationsListViewBinding.inflate(
            LayoutInflater.from(context), this, true
        )

        // Reset the card view to default values
        Utils.initMaterialCard(this)

        // Initialize components
        soundManager = CometChatSoundManager(context)
        popupMenu = CometChatPopupMenu(context, 0)

        // Initialize adapter
        conversationsAdapter = ConversationsAdapter()

        // Setup RecyclerView
        setupRecyclerView()

        // Setup default text formatters
        getDefaultMentionsFormatter()

        // Apply XML attributes
        applyStyleAttributes(attrs, defStyleAttr)

        // Setup click listeners
        setupClickListeners()

        // Initialize ViewModel
        initViewModel()
    }


    /**
     * Sets up the RecyclerView with adapter and scroll listener.
     */
    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerviewConversationsList.layoutManager = layoutManager
        binding.recyclerviewConversationsList.adapter = conversationsAdapter
        setSeparatorVisibility(GONE)
        // Disable change animations for smoother updates
        val animator = binding.recyclerviewConversationsList.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        // Add scroll listener for pagination
        binding.recyclerviewConversationsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel?.fetchConversations()
                }
            }
        })
    }

    /**
     * Gets the default mentions formatter.
     */
    private fun getDefaultMentionsFormatter() {
        // Create mentions formatter directly instead of using data source
        cometchatMentionsFormatter = CometChatMentionsFormatter(context)
        cometchatMentionsFormatter?.let { textFormatters.add(it) }
        processFormatters()
    }

    /**
     * Processes and applies text formatters to the adapter.
     */
    private fun processFormatters() {
        conversationsAdapter.setTextFormatters(textFormatters)
    }

    /**
     * Applies style attributes from XML.
     * Uses CometChatConversationsStyle.fromTypedArray() for centralized attribute extraction.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        // First, obtain attributes to check if an explicit style is set
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatConversations, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatConversations_cometchatConversationsStyle, 0
        )
        typedArray.recycle()
        
        // Store the style resource ID for refreshStyle
        currentStyleResId = styleResId
        
        // Obtain final TypedArray with style resolution
        typedArray = if (styleResId != 0) {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatConversations, defStyleAttr, styleResId
            )
        } else {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatConversations, defStyleAttr, 0
            )
        }
        
        // Use fromTypedArray for centralized attribute extraction (handles recycling internally)
        style = CometChatConversationsStyle.fromTypedArray(context, typedArray)
        
        // Apply styles to views
        applyStyle()
    }

    /**
     * Applies the style object to all views.
     */
    private fun applyStyle() {
        // Container
        setCardBackgroundColor(style.backgroundColor)
        if (style.strokeWidth > 0) {
            setStrokeWidth(style.strokeWidth)
            setStrokeColor(style.strokeColor)
        }
        if (style.cornerRadius > 0) {
            radius = style.cornerRadius.toFloat()
        }
        // Parent layout
        binding.parentLayout.setBackgroundColor(style.backgroundColor)

        // Toolbar styling using CometChatToolbar
        binding.toolbar.setBackgroundColor(style.backgroundColor)
        
        // Apply text appearance after setStyle to ensure it takes effect
        if (style.titleTextAppearance != 0) {
            binding.toolbar.setTitleTextAppearance(style.titleTextAppearance)
        }
        // Apply text color after text appearance (text appearance may override color)
        if (style.titleTextColor != 0) {
            binding.toolbar.setTitleTextColor(style.titleTextColor)
        }
        
        // Apply back icon properties
        style.backIcon?.let { binding.toolbar.setBackIcon(it) }
        if (style.backIconTint != 0) {
            binding.toolbar.setBackIconTint(style.backIconTint)
        }
        
        // IMPORTANT: Set back icon visibility LAST to ensure it's not overridden
        binding.toolbar.setBackIconVisibility(backIconVisibility)
        
        // Apply selection icons
        style.discardSelectionIcon?.let { binding.toolbar.setDiscardIcon(it) }
        style.submitSelectionIcon?.let { binding.toolbar.setSubmitIcon(it) }
        if (style.discardSelectionIconTint != 0) {
            binding.toolbar.setDiscardIconTint(style.discardSelectionIconTint)
        }
        if (style.submitSelectionIconTint != 0) {
            binding.toolbar.setSubmitIconTint(style.submitSelectionIconTint)
        }
        
        // Selection count styling - matches Java: uses item title styling
        if (style.itemStyle.titleTextAppearance != 0) {
            binding.toolbar.setSelectionCountTextAppearance(style.itemStyle.titleTextAppearance)
        }
        if (style.itemStyle.titleTextColor != 0) {
            binding.toolbar.setSelectionCountTextColor(style.itemStyle.titleTextColor)
        }

        // Search box styling
        if (style.searchBackgroundColor != 0) {
            binding.searchBox.setCardBackgroundColor(style.searchBackgroundColor)
        }
        if (style.searchTextColor != 0) {
            binding.searchBox.setSearchInputTextColor(style.searchTextColor)
        }
        if (style.searchTextAppearance != 0) {
            binding.searchBox.setSearchInputTextAppearance(style.searchTextAppearance)
        }
        if (style.searchPlaceholderColor != 0) {
            binding.searchBox.setSearchInputPlaceHolderTextColor(style.searchPlaceholderColor)
        }
        if (style.searchPlaceholderTextAppearance != 0) {
            binding.searchBox.setSearchInputPlaceHolderTextAppearance(style.searchPlaceholderTextAppearance)
        }
        style.searchStartIcon?.let { binding.searchBox.setSearchInputStartIcon(it) }
        if (style.searchStartIconTint != 0) {
            binding.searchBox.setSearchInputStartIconTint(style.searchStartIconTint)
        }
        style.searchEndIcon?.let { binding.searchBox.setSearchInputEndIcon(it) }
        if (style.searchEndIconTint != 0) {
            binding.searchBox.setSearchInputEndIconTint(style.searchEndIconTint)
        }
        if (style.searchCornerRadius > 0) {
            binding.searchBox.radius = style.searchCornerRadius.toFloat()
        }
        if (style.searchStrokeWidth > 0) {
            binding.searchBox.strokeWidth = style.searchStrokeWidth
            binding.searchBox.strokeColor = style.searchStrokeColor
        }

        // Empty state
        if (style.emptyStateTitleTextColor != 0) {
            binding.tvEmptyConversationsTitle.setTextColor(style.emptyStateTitleTextColor)
        }
        if (style.emptyStateSubtitleTextColor != 0) {
            binding.tvEmptyConversationsSubtitle.setTextColor(style.emptyStateSubtitleTextColor)
        }
        if (style.emptyStateTitleTextAppearance != 0) {
            binding.tvEmptyConversationsTitle.setTextAppearance(style.emptyStateTitleTextAppearance)
        }
        if (style.emptyStateSubtitleTextAppearance != 0) {
            binding.tvEmptyConversationsSubtitle.setTextAppearance(style.emptyStateSubtitleTextAppearance)
        }

        // Error state
        if (style.errorStateTitleTextColor != 0) {
            binding.tvErrorTitle.setTextColor(style.errorStateTitleTextColor)
        }
        if (style.errorStateSubtitleTextColor != 0) {
            binding.tvErrorSubtitle.setTextColor(style.errorStateSubtitleTextColor)
        }
        if (style.errorStateTitleTextAppearance != 0) {
            binding.tvErrorTitle.setTextAppearance(style.errorStateTitleTextAppearance)
        }
        if (style.errorStateSubtitleTextAppearance != 0) {
            binding.tvErrorSubtitle.setTextAppearance(style.errorStateSubtitleTextAppearance)
        }

        // Update adapter item style
        updateAdapterItemStyle()
        
        // Apply mentions style to formatter
        applyMentionsStyle()
    }
    
    /**
     * Applies the mentions style to the mentions formatter.
     * This ensures the background color and text color are properly applied.
     */
    private fun applyMentionsStyle() {
        if (style.mentionsStyleResId != 0) {
            cometchatMentionsFormatter?.setConversationsMentionTextStyle(context, style.mentionsStyleResId)
        }
    }

    /**
     * Updates the adapter's item style.
     */
    private fun updateAdapterItemStyle() {
        conversationsAdapter.setItemStyle(style.itemStyle)
    }

    /**
     * Sets up click listeners for UI elements.
     */
    private fun setupClickListeners() {
        // Adapter item click
        conversationsAdapter.setOnItemClick { view, position, conversation ->
            if (onItemClick != null) {
                onItemClick?.invoke(conversation)
            } else {
                if (selectionMode != UIKitConstants.SelectionMode.NONE) {
                    selectConversation(conversation, selectionMode)
                }
            }
        }

        // Adapter item long click
        conversationsAdapter.setOnLongClick { view, position, conversation ->
            if (onItemLongClick != null) {
                onItemLongClick?.invoke(conversation)
            } else {
                preparePopupMenu(view, conversation)
            }
        }

        // Toolbar callbacks using CometChatToolbar
        binding.toolbar.setOnBackPress {
            onBackPress?.invoke()
        }
        
        binding.toolbar.setOnDiscardSelection {
            clearSelection()
        }
        
        binding.toolbar.setOnSubmitSelection {
            onSelection?.invoke(getSelectedConversations())
        }

        // Retry button
        binding.btnRetry.setOnClickListener {
            viewModel?.fetchConversations()
        }

        // Search box
        binding.searchBox.setOnSearchClick {
            onSearchClick?.invoke()
        }
        binding.searchBox.binding.etSearch.isFocusable = false
    }

    /**
     * Initializes the ViewModel and sets up observers.
     * Only creates an internal ViewModel if one was not externally provided.
     */
    private fun initViewModel() {
        lifecycleOwner = Utils.getLifecycleOwner(context)
        if (lifecycleOwner == null) return

        // Only create internal ViewModel if not externally provided
        if (!isExternalViewModel && viewModel == null) {
            // Create ViewModel using factory from chatuikit-core
            val factory = CometChatConversationsViewModelFactory()
            viewModel = ViewModelProvider(
                lifecycleOwner as ViewModelStoreOwner,
                factory
            )[CometChatConversationsViewModel::class.java]
        }

        // Initialize sound manager if ViewModel exists
        viewModel?.initSoundManager(context)

        // Set up observers
        observeViewModel()
    }
    
    /**
     * Sets up observers for the ViewModel's StateFlows and SharedFlows.
     * This method can be called to re-observe when the ViewModel changes.
     */
    private fun observeViewModel() {
        val vm = viewModel ?: return
        
        // Cancel any existing scope and create a new one
        viewScope?.cancel()

        // Create coroutine scope for collecting flows
        viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Observe conversations
        viewScope?.launch {
            vm.conversations.collectLatest { conversations ->
                conversationsAdapter.setList(conversations)
                onLoad?.invoke(conversations)
            }
        }

        // Observe UI state
        viewScope?.launch {
            vm.uiState.collectLatest { state ->
                handleStateChange(state)
            }
        }

        // Observe typing indicators
        viewScope?.launch {
            vm.typingIndicators.collectLatest { typingMap ->
                // Pass the typing indicators map directly to the adapter
                // The adapter will match indicators to conversations using the correct logic
                conversationsAdapter.setTypingIndicatorsFromViewModel(typingMap)
            }
        }

        // Observe selected conversations
        viewScope?.launch {
            vm.selectedConversations.collectLatest { selected ->
                val selectionMap = HashMap<Conversation, Boolean>()
                selected.forEach { selectionMap[it] = true }
                conversationsAdapter.selectConversation(selectionMap)
            }
        }

        // Observe delete state
        viewScope?.launch {
            vm.deleteState.collectLatest { state ->
                handleDeleteState(state)
            }
        }

        // Observe play sound event
        viewScope?.launch {
            vm.playSoundEvent.collectLatest { shouldPlay ->
                if (shouldPlay) playSound()
            }
        }

        // Observe scroll to top event
        viewScope?.launch {
            vm.scrollToTopEvent.collectLatest {
                scrollToTop()
            }
        }
    }


    /**
     * Handles UI state changes.
     */
    private fun handleStateChange(state: UIState) {
        hideAllStates()

        when (state) {
            is UIState.Loading -> handleLoadingState()
            is UIState.Content -> handleContentState()
            is UIState.Empty -> handleEmptyState()
            is UIState.Error -> handleErrorState(state.exception)
        }
    }

    /**
     * Hides all state views.
     */
    private fun hideAllStates() {
        binding.recyclerviewConversationsList.visibility = GONE
        binding.emptyStateView.visibility = GONE
        binding.errorStateView.visibility = GONE
        binding.shimmerParentLayout.visibility = GONE
        binding.customLayout.visibility = GONE
    }

    /**
     * Handles loading state.
     */
    private fun handleLoadingState() {
        if (loadingStateVisibility == VISIBLE) {
            if (customLoadingView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customLoadingView)
                binding.customLayout.visibility = VISIBLE
            } else {
                // Show shimmer
                binding.shimmerParentLayout.visibility = VISIBLE
                setupShimmer()
            }
        }
        // Announce loading state for accessibility
        announceForAccessibility(context.getString(R.string.cometchat_loading_conversations))
    }

    /**
     * Sets up shimmer loading effect.
     */
    private fun setupShimmer() {
        val shimmerAdapter = CometChatShimmerAdapter(30, R.layout.shimmer_list_base)
        binding.shimmerRecyclerview.adapter = shimmerAdapter
        binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(context))
        binding.shimmerEffectFrame.startShimmer()
    }

    /**
     * Handles content state.
     */
    private fun handleContentState() {
        binding.shimmerEffectFrame.stopShimmer()
        binding.recyclerviewConversationsList.visibility = VISIBLE
    }

    /**
     * Handles empty state.
     */
    private fun handleEmptyState() {
        binding.shimmerEffectFrame.stopShimmer()
        if (emptyStateVisibility == VISIBLE) {
            if (customEmptyView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customEmptyView)
                binding.customLayout.visibility = VISIBLE
            } else {
                binding.emptyStateView.visibility = VISIBLE
            }
        }
        // Announce empty state for accessibility
        announceForAccessibility(context.getString(R.string.cometchat_no_conversations))
        onEmpty?.invoke()
    }

    /**
     * Handles error state.
     */
    private fun handleErrorState(exception: CometChatException? = null) {
        binding.shimmerEffectFrame.stopShimmer()
        if (errorStateVisibility == VISIBLE) {
            if (customErrorView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customErrorView)
                binding.customLayout.visibility = VISIBLE
            } else {
                binding.errorStateView.visibility = VISIBLE
            }
        }
        // Announce error state for accessibility
        announceForAccessibility(context.getString(R.string.cometchat_error_loading_conversations))
        exception?.let { onError?.invoke(it) }
    }

    /**
     * Handles delete state changes.
     */
    private fun handleDeleteState(state: DeleteState) {
        when (state) {
            is DeleteState.Success -> {
                deleteAlertDialog?.dismiss()
            }
            is DeleteState.Failure -> {
                deleteAlertDialog?.dismiss()
                Toast.makeText(
                    context,
                    context.getString(R.string.cometchat_conversation_delete_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
            is DeleteState.InProgress -> {
                deleteAlertDialog?.showPositiveButtonProgress(true)
            }
            is DeleteState.Idle -> {
                // Do nothing
            }
        }
    }

    /**
     * Prepares and shows the popup menu for a conversation.
     */
    private fun preparePopupMenu(view: View, conversation: Conversation) {
        val menuItems = mutableListOf<CometChatPopupMenu.MenuItem>()

        if (options != null) {
            menuItems.addAll(options!!.invoke(context, conversation))
        } else {
            // Add default delete option
            if (deleteConversationOptionVisibility == VISIBLE) {
                menuItems.add(
                    CometChatPopupMenu.MenuItem(
                        UIKitConstants.ConversationOption.DELETE,
                        context.getString(R.string.cometchat_delete),
                        style.deleteOptionIcon ?: ResourcesCompat.getDrawable(
                            resources, R.drawable.cometchat_ic_delete, null
                        ),
                        null,
                        style.deleteOptionIconTint,
                        0,
                        style.deleteOptionTextColor,
                        style.deleteOptionTextAppearance,
                        null
                    )
                )
            }

            // Add additional options
            addOptions?.let { menuItems.addAll(it.invoke(context, conversation)) }
        }

        popupMenu.setMenuItems(menuItems)
        popupMenu.setOnMenuItemClickListener { id, name ->
            for (item in menuItems) {
                if (id.equals(item.id, ignoreCase = true)) {
                    if (item.onClick != null) {
                        item.onClick.invoke()
                    } else {
                        handleDefaultMenuClick(item, conversation)
                    }
                    break
                }
            }
            popupMenu.dismiss()
        }

        popupMenu.show(view)
    }

    /**
     * Handles default menu item clicks.
     */
    private fun handleDefaultMenuClick(item: CometChatPopupMenu.MenuItem, conversation: Conversation) {
        if (item.id.equals(UIKitConstants.ConversationOption.DELETE, ignoreCase = true)) {
            showDeleteConfirmationDialog(conversation)
        }
    }

    /**
     * Shows delete confirmation dialog.
     */
    private fun showDeleteConfirmationDialog(conversation: Conversation) {
        deleteAlertDialog = CometChatConfirmDialog(context, R.style.CometChatConfirmDialogStyle)
        deleteAlertDialog?.apply {
            setConfirmDialogIcon(
                ResourcesCompat.getDrawable(resources, R.drawable.cometchat_ic_delete, null)
            )
            setTitleText(context.getString(R.string.cometchat_conversation_delete_message_title))
            setSubtitleText(context.getString(R.string.cometchat_conversation_delete_message_subtitle))
            setPositiveButtonText(context.getString(R.string.cometchat_delete))
            setNegativeButtonText(context.getString(R.string.cometchat_cancel))
            setOnPositiveButtonClick {
                viewModel?.deleteConversation(conversation)
            }
            setOnNegativeButtonClick { dismiss() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }

    /**
     * Plays notification sound.
     */
    private fun playSound() {
        if (!disableSoundForMessages) {
            soundManager.play(Sound.INCOMING_MESSAGE_FROM_OTHER, customSoundForMessage)
        }
    }

    /**
     * Scrolls to the top of the list only if the user is near the top (first 3 items visible).
     * This prevents disruptive scrolling when the user is browsing older conversations.
     */
    private fun scrollToTop() {
        val layoutManager = binding.recyclerviewConversationsList.layoutManager as? LinearLayoutManager
        val firstVisiblePosition = layoutManager?.findFirstVisibleItemPosition() ?: 0
        
        // Only scroll to top if the first 3 items are visible
        if (firstVisiblePosition < 3) {
            binding.recyclerviewConversationsList.scrollToPosition(0)
        }
    }


    // ==================== Public API Methods ====================

    /**
     * Sets the item click callback.
     */
    fun setOnItemClick(callback: (Conversation) -> Unit) {
        onItemClick = callback
    }

    /**
     * Sets the item long click callback.
     */
    fun setOnItemLongClick(callback: (Conversation) -> Unit) {
        onItemLongClick = callback
    }

    /**
     * Sets the error callback.
     */
    fun setOnError(callback: (CometChatException) -> Unit) {
        onError = callback
    }

    /**
     * Sets the load callback.
     */
    fun setOnLoad(callback: (List<Conversation>) -> Unit) {
        onLoad = callback
    }

    /**
     * Sets the empty callback.
     */
    fun setOnEmpty(callback: () -> Unit) {
        onEmpty = callback
    }

    /**
     * Sets the back press callback.
     */
    fun setOnBackPress(callback: () -> Unit) {
        onBackPress = callback
    }

    /**
     * Sets the search click callback.
     */
    fun setOnSearchClick(callback: () -> Unit) {
        onSearchClick = callback
    }

    /**
     * Sets the selection callback.
     */
    fun setOnSelection(callback: (List<Conversation>) -> Unit) {
        onSelection = callback
    }

    /**
     * Sets an external ViewModel instance to be used by this component.
     *
     * This allows developers to:
     * - Share a ViewModel instance across multiple components
     * - Pre-configure the ViewModel before passing it to the component
     * - Have more control over the ViewModel lifecycle
     *
     * If called before the component is attached to a window, the external
     * ViewModel will be used from the start. If called after initialization,
     * the component will switch to using the provided ViewModel and re-observe
     * all StateFlows.
     *
     * @param viewModel The external ViewModel instance, or null to reset to
     *                  internal ViewModel creation
     *
     * Usage:
     * ```kotlin
     * // Create and configure ViewModel externally
     * val factory = CometChatConversationsViewModelFactory(
     *     conversationsRequestBuilder = ConversationsRequest.ConversationsRequestBuilder()
     *         .setLimit(30)
     *         .build()
     * )
     * val viewModel = ViewModelProvider(viewModelStoreOwner, factory)
     *     .get(CometChatConversationsViewModel::class.java)
     *
     * // Inject into component
     * conversationList.setViewModel(viewModel)
     * ```
     */
    fun setViewModel(viewModel: CometChatConversationsViewModel?) {
        val previousViewModel = this.viewModel
        this.viewModel = viewModel
        this.isExternalViewModel = viewModel != null
        
        // If we already have a lifecycle owner (component is initialized), handle the change
        if (lifecycleOwner != null) {
            if (viewModel != null) {
                // Initialize sound manager for the new ViewModel
                viewModel.initSoundManager(context)
                // Re-observe with new ViewModel
                observeViewModel()
            } else {
                // Reset to internal ViewModel creation
                isExternalViewModel = false
                initViewModel()
            }
        }
    }

    /**
     * Returns the current ViewModel instance being used by this component.
     *
     * @return The ViewModel instance, or null if not yet initialized
     */
    fun getViewModel(): CometChatConversationsViewModel? = viewModel

    /**
     * Sets the selection mode.
     * 
     * This method configures the selection UI based on the mode:
     * - SINGLE/MULTIPLE: Clears existing selections, enables selection on adapter,
     *   shows discard icon and selection count in toolbar
     * - NONE: Disables selection on adapter, hides all selection UI elements
     * 
     * Matches the Java chatuikit implementation behavior.
     */
    fun setSelectionMode(mode: UIKitConstants.SelectionMode) {
        // Clear existing selections (matches Java implementation)
        selectedConversations.clear()
        conversationsAdapter.selectConversation(selectedConversations)
        
        // Store the mode
        selectionMode = mode
        
        // Configure UI based on mode
        when (mode) {
            UIKitConstants.SelectionMode.SINGLE,
            UIKitConstants.SelectionMode.MULTIPLE -> {
                isFurtherSelectionEnabled = true
                conversationsAdapter.setSelectionEnabled(true)
                binding.toolbar.setSelectionMode(true)
                binding.toolbar.setSelectionCount(0)
            }
            UIKitConstants.SelectionMode.NONE -> {
                isFurtherSelectionEnabled = false
                conversationsAdapter.setSelectionEnabled(false)
                binding.toolbar.setSelectionMode(false)
            }
        }
    }

    /**
     * Selects a conversation based on the selection mode.
     */
    fun selectConversation(conversation: Conversation, mode: UIKitConstants.SelectionMode?) {
        if (mode == null) return

        selectionMode = mode
        when (mode) {
            UIKitConstants.SelectionMode.SINGLE -> {
                selectedConversations.clear()
                selectedConversations[conversation] = true
                binding.toolbar.setSelectionMode(true)
                binding.toolbar.setSelectionCount(1)
                conversationsAdapter.selectConversation(selectedConversations)
            }
            UIKitConstants.SelectionMode.MULTIPLE -> {
                if (selectedConversations.containsKey(conversation)) {
                    selectedConversations.remove(conversation)
                } else if (isFurtherSelectionEnabled) {
                    selectedConversations[conversation] = true
                }

                if (selectedConversations.isEmpty()) {
                    binding.toolbar.setSelectionMode(false)
                } else {
                    binding.toolbar.setSelectionMode(true)
                    binding.toolbar.setSelectionCount(selectedConversations.size)
                }
                conversationsAdapter.selectConversation(selectedConversations)
            }
            UIKitConstants.SelectionMode.NONE -> {
                // Do nothing
            }
        }
    }

    /**
     * Gets the list of selected conversations.
     */
    fun getSelectedConversations(): List<Conversation> {
        return selectedConversations.keys.toList()
    }

    /**
     * Clears the current selection.
     */
    fun clearSelection() {
        selectedConversations.clear()
        binding.toolbar.setSelectionMode(false)
        binding.toolbar.setSelectionCount(0)
        conversationsAdapter.selectConversation(selectedConversations)
    }

    /**
     * Sets custom item view listener.
     */
    fun setItemView(listener: ConversationsViewHolderListener?) {
        itemViewListener = listener
        conversationsAdapter.setItemView(listener)
    }

    /**
     * Sets custom leading view listener.
     */
    fun setLeadingView(listener: ConversationsViewHolderListener?) {
        leadingViewListener = listener
        conversationsAdapter.setLeadingView(listener)
    }

    /**
     * Sets custom title view listener.
     */
    fun setTitleView(listener: ConversationsViewHolderListener?) {
        titleViewListener = listener
        conversationsAdapter.setTitleView(listener)
    }

    /**
     * Sets custom subtitle view listener.
     */
    fun setSubtitleView(listener: ConversationsViewHolderListener?) {
        subtitleViewListener = listener
        conversationsAdapter.setSubtitleView(listener)
    }

    /**
     * Sets custom trailing view listener.
     */
    fun setTrailingView(listener: ConversationsViewHolderListener?) {
        trailingViewListener = listener
        conversationsAdapter.setTrailingView(listener)
    }

    /**
     * Sets custom empty view.
     */
    fun setEmptyView(view: View?) {
        customEmptyView = view
    }

    /**
     * Sets custom error view.
     */
    fun setErrorView(view: View?) {
        customErrorView = view
    }

    /**
     * Sets custom loading view.
     */
    fun setLoadingView(view: View?) {
        customLoadingView = view
    }

    /**
     * Sets menu options callback.
     */
    fun setOptions(callback: (Context, Conversation) -> List<CometChatPopupMenu.MenuItem>) {
        options = callback
    }

    /**
     * Sets additional menu options callback.
     */
    fun setAddOptions(callback: (Context, Conversation) -> List<CometChatPopupMenu.MenuItem>) {
        addOptions = callback
    }

    /**
     * Sets text formatters.
     */
    fun setTextFormatters(formatters: List<CometChatTextFormatter>) {
        textFormatters.clear()
        textFormatters.addAll(formatters)
        processFormatters()
    }

    /**
     * Sets the mentions style for conversation list previews.
     * This applies the style to the internal mentions formatter.
     *
     * @param style The CometChatMentionStyle to apply
     */
    fun setMentionsStyle(style: CometChatMentionStyle) {
        cometchatMentionsFormatter?.setConversationsMentionStyle(style)
    }

    /**
     * Sets the mentions style from a style resource.
     * This applies the style to the internal mentions formatter.
     *
     * @param styleRes The style resource ID
     */
    fun setMentionsStyle(@StyleRes styleRes: Int) {
        style = style.copy(mentionsStyleResId = styleRes)
        applyMentionsStyle()
    }

    /**
     * Disables sound for messages.
     */
    fun setDisableSoundForMessages(disable: Boolean) {
        disableSoundForMessages = disable
    }

    /**
     * Sets custom sound for messages.
     */
    fun setCustomSoundForMessages(@RawRes soundRes: Int) {
        customSoundForMessage = soundRes
    }

    /**
     * Sets toolbar visibility.
     */
    fun setToolbarVisibility(visibility: Int) {
        toolbarVisibility = visibility
        binding.toolbar.visibility = visibility
    }

    /**
     * Sets back icon visibility.
     */
    fun setBackIconVisibility(visibility: Int) {
        backIconVisibility = visibility
        binding.toolbar.setBackIconVisibility(visibility)
    }

    /**
     * Sets search box visibility.
     */
    fun setSearchBoxVisibility(visibility: Int) {
        searchBoxVisibility = visibility
        binding.searchBoxLayout.visibility = visibility
    }

    /**
     * Sets delete conversation option visibility.
     */
    fun setDeleteConversationOptionVisibility(visibility: Int) {
        deleteConversationOptionVisibility = visibility
    }

    /**
     * Sets user status visibility.
     */
    fun setUserStatusVisibility(visibility: Int) {
        userStatusVisibility = visibility
        conversationsAdapter.setHideUserStatus(visibility != VISIBLE)
    }

    /**
     * Sets group type visibility.
     */
    fun setGroupTypeVisibility(visibility: Int) {
        groupTypeVisibility = visibility
        conversationsAdapter.setHideGroupType(visibility != VISIBLE)
    }

    /**
     * Sets receipts visibility.
     */
    fun setReceiptsVisibility(visibility: Int) {
        receiptsVisibility = visibility
        conversationsAdapter.setHideReceipts(visibility != VISIBLE)
    }

    /**
     * Sets separator visibility.
     * When visibility is View.GONE, the separator between list items will be hidden.
     */
    fun setSeparatorVisibility(visibility: Int) {
        separatorVisibility = visibility
        conversationsAdapter.setHideSeparator(visibility != VISIBLE)
    }

    /**
     * Sets toolbar separator visibility.
     * When show is false, the separator line at the bottom of the toolbar will be hidden.
     */
    fun setToolbarSeparatorVisibility(show: Boolean) {
        binding.toolbar.setShowSeparator(show)
    }

    /**
     * Sets toolbar separator color.
     */
    fun setToolbarSeparatorColor(@ColorInt color: Int) {
        binding.toolbar.setSeparatorColor(color)
    }

    /**
     * Sets error state visibility.
     */
    fun setErrorStateVisibility(visibility: Int) {
        errorStateVisibility = visibility
    }

    /**
     * Gets the current error state visibility.
     *
     * @return The error state visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getErrorStateVisibility(): Int = errorStateVisibility

    /**
     * Sets loading state visibility.
     */
    fun setLoadingStateVisibility(visibility: Int) {
        loadingStateVisibility = visibility
    }

    /**
     * Gets the current loading state visibility.
     *
     * @return The loading state visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getLoadingStateVisibility(): Int = loadingStateVisibility

    /**
     * Sets empty state visibility.
     */
    fun setEmptyStateVisibility(visibility: Int) {
        emptyStateVisibility = visibility
    }

    /**
     * Gets the current empty state visibility.
     *
     * @return The empty state visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getEmptyStateVisibility(): Int = emptyStateVisibility

    /**
     * Hides or shows the loading state.
     * When hidden, the loading shimmer will not be displayed during initial fetch.
     * 
     * @param hide true to hide the loading state, false to show it
     */
    fun setHideLoadingState(hide: Boolean) {
        loadingStateVisibility = if (hide) GONE else VISIBLE
    }

    /**
     * Hides or shows the empty state.
     * When hidden, the empty state view will not be displayed when there are no conversations.
     * 
     * @param hide true to hide the empty state, false to show it
     */
    fun setHideEmptyState(hide: Boolean) {
        emptyStateVisibility = if (hide) GONE else VISIBLE
    }

    /**
     * Hides or shows the error state.
     * When hidden, the error state view will not be displayed when an error occurs.
     * 
     * @param hide true to hide the error state, false to show it
     */
    fun setHideErrorState(hide: Boolean) {
        errorStateVisibility = if (hide) GONE else VISIBLE
    }

    /**
     * Sets the title text.
     */
    fun setTitle(title: String) {
        binding.toolbar.setTitle(title)
    }

    /**
     * Sets overflow menu view.
     */
    fun setOverflowMenu(view: View?) {
        binding.toolbar.clearActionViews()
        if (view != null) {
            binding.toolbar.addActionView(view)
        }
    }

    /**
     * Sets the style programmatically.
     * 
     * @param style The CometChatConversationsStyle to apply
     */
    fun setStyle(style: CometChatConversationsStyle) {
        this.style = style
        applyStyle()
    }
    
    /**
     * Sets the style from a style resource.
     * 
     * @param styleRes The style resource ID
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            currentStyleResId = styleRes
            val typedArray = context.theme.obtainStyledAttributes(styleRes, R.styleable.CometChatConversations)
            // fromTypedArray handles recycling internally
            style = CometChatConversationsStyle.fromTypedArray(context, typedArray)
            applyStyle()
        }
    }
    
    /**
     * Gets the current style.
     * 
     * @return The current CometChatConversationsStyle
     */
    fun getStyle(): CometChatConversationsStyle = style

    /**
     * Sets the toolbar style programmatically.
     */
    fun setToolbarStyle(style: CometChatToolbarStyle) {
        binding.toolbar.setStyle(style)
    }

    // ==================== Toolbar Styling Methods ====================

    /**
     * Gets the title text color.
     */
    fun getTitleTextColor(): Int = style.titleTextColor

    /**
     * Sets the title text color.
     */
    fun setTitleTextColor(@ColorInt color: Int) {
        style = style.copy(titleTextColor = color)
        binding.toolbar.setTitleTextColor(color)
    }

    /**
     * Gets the title text appearance.
     */
    fun getTitleTextAppearance(): Int = style.titleTextAppearance

    /**
     * Sets the title text appearance.
     */
    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(titleTextAppearance = appearance)
        if (appearance != 0) {
            binding.toolbar.setTitleTextAppearance(appearance)
        }
    }

    /**
     * Gets the back icon.
     */
    fun getBackIcon(): Drawable? = style.backIcon

    /**
     * Sets the back icon.
     */
    fun setBackIcon(icon: Drawable?) {
        style = style.copy(backIcon = icon)
        icon?.let { binding.toolbar.setBackIcon(it) }
    }

    /**
     * Gets the back icon tint.
     */
    fun getBackIconTint(): Int = style.backIconTint

    /**
     * Sets the back icon tint.
     */
    fun setBackIconTint(@ColorInt tint: Int) {
        style = style.copy(backIconTint = tint)
        binding.toolbar.setBackIconTint(tint)
    }

    /**
     * Gets the discard selection icon.
     */
    fun getDiscardSelectionIcon(): Drawable? = style.discardSelectionIcon

    /**
     * Sets the discard selection icon.
     */
    fun setDiscardSelectionIcon(icon: Drawable?) {
        style = style.copy(discardSelectionIcon = icon)
        icon?.let { binding.toolbar.setDiscardIcon(it) }
    }

    /**
     * Gets the discard selection icon tint.
     */
    fun getDiscardSelectionIconTint(): Int = style.discardSelectionIconTint

    /**
     * Sets the discard selection icon tint.
     */
    fun setDiscardSelectionIconTint(@ColorInt tint: Int) {
        style = style.copy(discardSelectionIconTint = tint)
        binding.toolbar.setDiscardIconTint(tint)
    }

    /**
     * Gets the submit selection icon.
     */
    fun getSubmitSelectionIcon(): Drawable? = style.submitSelectionIcon

    /**
     * Sets the submit selection icon.
     */
    fun setSubmitSelectionIcon(icon: Drawable?) {
        style = style.copy(submitSelectionIcon = icon)
        icon?.let { binding.toolbar.setSubmitIcon(it) }
    }

    /**
     * Gets the submit selection icon tint.
     */
    fun getSubmitSelectionIconTint(): Int = style.submitSelectionIconTint

    /**
     * Sets the submit selection icon tint.
     */
    fun setSubmitSelectionIconTint(@ColorInt tint: Int) {
        style = style.copy(submitSelectionIconTint = tint)
        binding.toolbar.setSubmitIconTint(tint)
    }

    // ==================== Search Box Styling Methods ====================

    /**
     * Gets the background color for the search input.
     */
    fun getSearchInputBackgroundColor(): Int = style.searchBackgroundColor

    /**
     * Sets the background color for the search input.
     */
    fun setSearchInputBackgroundColor(@ColorInt color: Int) {
        style = style.copy(searchBackgroundColor = color)
        binding.searchBox.setCardBackgroundColor(color)
    }

    /**
     * Gets the text color for the search input.
     */
    fun getSearchInputTextColor(): Int = style.searchTextColor

    /**
     * Sets the text color for the search input.
     */
    fun setSearchInputTextColor(@ColorInt color: Int) {
        style = style.copy(searchTextColor = color)
        binding.searchBox.setSearchInputTextColor(color)
    }

    /**
     * Gets the text appearance for the search input.
     */
    fun getSearchInputTextAppearance(): Int = style.searchTextAppearance

    /**
     * Sets the text appearance for the search input.
     */
    fun setSearchInputTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(searchTextAppearance = appearance)
        binding.searchBox.setSearchInputTextAppearance(appearance)
    }

    /**
     * Gets the placeholder text color for the search input.
     */
    fun getSearchInputPlaceHolderTextColor(): Int = style.searchPlaceholderColor

    /**
     * Sets the placeholder text color for the search input.
     */
    fun setSearchInputPlaceHolderTextColor(@ColorInt color: Int) {
        style = style.copy(searchPlaceholderColor = color)
        binding.searchBox.setSearchInputPlaceHolderTextColor(color)
    }

    /**
     * Gets the placeholder text appearance for the search input.
     */
    fun getSearchInputPlaceHolderTextAppearance(): Int = style.searchPlaceholderTextAppearance

    /**
     * Sets the placeholder text appearance for the search input.
     */
    fun setSearchInputPlaceHolderTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(searchPlaceholderTextAppearance = appearance)
        binding.searchBox.setSearchInputPlaceHolderTextAppearance(appearance)
    }

    /**
     * Gets the start icon for the search input.
     */
    fun getSearchInputStartIcon(): Drawable? = style.searchStartIcon

    /**
     * Sets the start icon for the search input.
     */
    fun setSearchInputStartIcon(icon: Drawable?) {
        style = style.copy(searchStartIcon = icon)
        icon?.let { binding.searchBox.setSearchInputStartIcon(it) }
    }

    /**
     * Gets the start icon tint for the search input.
     */
    fun getSearchInputStartIconTint(): Int = style.searchStartIconTint

    /**
     * Sets the start icon tint for the search input.
     */
    fun setSearchInputStartIconTint(@ColorInt tint: Int) {
        style = style.copy(searchStartIconTint = tint)
        binding.searchBox.setSearchInputStartIconTint(tint)
    }

    /**
     * Gets the end icon for the search input.
     */
    fun getSearchInputEndIcon(): Drawable? = style.searchEndIcon

    /**
     * Sets the end icon for the search input.
     */
    fun setSearchInputEndIcon(icon: Drawable?) {
        style = style.copy(searchEndIcon = icon)
        icon?.let { binding.searchBox.setSearchInputEndIcon(it) }
    }

    /**
     * Gets the end icon tint for the search input.
     */
    fun getSearchInputEndIconTint(): Int = style.searchEndIconTint

    /**
     * Sets the end icon tint for the search input.
     */
    fun setSearchInputEndIconTint(@ColorInt tint: Int) {
        style = style.copy(searchEndIconTint = tint)
        binding.searchBox.setSearchInputEndIconTint(tint)
    }

    /**
     * Gets the corner radius for the search input.
     */
    fun getSearchInputCornerRadius(): Int = style.searchCornerRadius

    /**
     * Sets the corner radius for the search input.
     */
    fun setSearchInputCornerRadius(@Dimension radius: Int) {
        style = style.copy(searchCornerRadius = radius)
        binding.searchBox.radius = radius.toFloat()
    }

    /**
     * Gets the stroke width for the search input.
     */
    fun getSearchInputStrokeWidth(): Int = style.searchStrokeWidth

    /**
     * Sets the stroke width for the search input.
     */
    fun setSearchInputStrokeWidth(@Dimension width: Int) {
        style = style.copy(searchStrokeWidth = width)
        binding.searchBox.strokeWidth = width
    }

    /**
     * Gets the stroke color for the search input.
     */
    fun getSearchInputStrokeColor(): Int = style.searchStrokeColor

    /**
     * Sets the stroke color for the search input.
     */
    fun setSearchInputStrokeColor(@ColorInt color: Int) {
        style = style.copy(searchStrokeColor = color)
        binding.searchBox.strokeColor = color
    }

    /**
     * Sets the end icon visibility for the search input.
     */
    fun setSearchInputEndIconVisibility(visibility: Int) {
        binding.searchBox.setSearchInputEndIconVisibility(visibility)
    }

    /**
     * Sets the text for the search input.
     */
    fun setSearchInputText(text: String) {
        binding.searchBox.setSearchInputText(text)
    }

    /**
     * Sets the placeholder text for the search input.
     */
    fun setSearchPlaceholderText(placeholder: String?) {
        binding.searchBox.setSearchPlaceholderText(placeholder)
    }

    /**
     * Refreshes the component with current theme colors.
     * 
     * Call this method after a theme change to update all colors from the new theme.
     * This re-applies the style using the stored style resource ID, updating all
     * theme-dependent colors and properties.
     *
     * **Requirements:**
     * - 7.1: Theme adaptation for non-customized colors
     */
    fun refreshStyle() {
        // Re-apply style attributes using the stored style resource ID
        val typedArray = if (currentStyleResId != 0) {
            context.theme.obtainStyledAttributes(
                null, R.styleable.CometChatConversations, R.attr.cometchatConversationsStyle, currentStyleResId
            )
        } else {
            context.theme.obtainStyledAttributes(
                null, R.styleable.CometChatConversations, R.attr.cometchatConversationsStyle, 0
            )
        }
        // fromTypedArray handles recycling internally
        style = CometChatConversationsStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }
    
    /**
     * Sets the style resource ID and applies it.
     * 
     * @param styleResId The style resource ID to apply
     */
    fun setStyleResource(@StyleRes styleResId: Int) {
        currentStyleResId = styleResId
        refreshStyle()
    }

    /**
     * Sets the style for the popup menu (option list) that appears on long press.
     * 
     * @param styleRes The style resource ID to apply to the popup menu
     */
    fun setOptionListStyle(@StyleRes styleRes: Int) {
        if (styleRes == 0) return
        style = style.copy(optionListStyleResId = styleRes)
        popupMenu.setStyle(styleRes)
    }

    // ==================== Checkbox Styling Methods ====================

    /**
     * Sets the stroke width for the checkbox.
     * 
     * @param width The stroke width in pixels
     */
    fun setCheckBoxStrokeWidth(@Dimension width: Int) {
        style = style.copy(checkBoxStrokeWidth = width)
        conversationsAdapter.setCheckBoxStrokeWidth(width)
    }

    /**
     * Sets the corner radius for the checkbox.
     * 
     * @param radius The corner radius in pixels
     */
    fun setCheckBoxCornerRadius(@Dimension radius: Int) {
        style = style.copy(checkBoxCornerRadius = radius)
        conversationsAdapter.setCheckBoxCornerRadius(radius)
    }

    /**
     * Sets the stroke color for the checkbox.
     * 
     * @param color The stroke color
     */
    fun setCheckBoxStrokeColor(@ColorInt color: Int) {
        style = style.copy(checkBoxStrokeColor = color)
        conversationsAdapter.setCheckBoxStrokeColor(color)
    }

    /**
     * Sets the background color for the checkbox.
     * 
     * @param color The background color
     */
    fun setCheckBoxBackgroundColor(@ColorInt color: Int) {
        style = style.copy(checkBoxBackgroundColor = color)
        conversationsAdapter.setCheckBoxBackgroundColor(color)
    }

    /**
     * Sets the checked background color for the checkbox.
     * 
     * @param color The checked background color
     */
    fun setCheckBoxCheckedBackgroundColor(@ColorInt color: Int) {
        style = style.copy(checkBoxCheckedBackgroundColor = color)
        conversationsAdapter.setCheckBoxCheckedBackgroundColor(color)
    }

    /**
     * Sets the select icon for the checkbox.
     * 
     * @param icon The select icon drawable
     */
    fun setCheckBoxSelectIcon(icon: Drawable?) {
        style = style.copy(checkBoxSelectIcon = icon)
        conversationsAdapter.setCheckBoxSelectIcon(icon)
    }

    /**
     * Sets the select icon tint for the checkbox.
     * 
     * @param tint The select icon tint color
     */
    fun setCheckBoxSelectIconTint(@ColorInt tint: Int) {
        style = style.copy(checkBoxSelectIconTint = tint)
        conversationsAdapter.setCheckBoxSelectIconTint(tint)
    }

    // ==================== Date Format Method ====================

    /**
     * Sets a custom date format for conversation timestamps.
     * 
     * @param dateFormat The SimpleDateFormat to use for formatting dates
     */
    fun setDateFormat(dateFormat: SimpleDateFormat?) {
        conversationsAdapter.setDateFormat(dateFormat)
    }

    // ==================== Date Time Formatter Methods ====================

    /**
     * Sets a custom date/time formatter callback for conversation timestamps.
     * This provides more flexibility than setDateFormat by allowing different
     * formatting for different time contexts (today, yesterday, etc.).
     * 
     * @param formatter The DateTimeFormatterCallback to use, or null to use default formatting
     */
    fun setDateTimeFormatter(formatter: DateTimeFormatterCallback?) {
        conversationsAdapter.setDateTimeFormatter(formatter)
    }

    /**
     * Gets the current date/time formatter callback.
     * 
     * @return The current DateTimeFormatterCallback, or null if using default formatting
     */
    fun getDateTimeFormatter(): DateTimeFormatterCallback? {
        return conversationsAdapter.getDateTimeFormatter()
    }

    // ==================== Receipts Visibility Methods ====================

    /**
     * Hides or shows the read receipts in the conversations view.
     * This is a convenience method that wraps setReceiptsVisibility.
     * 
     * @param hide true to hide the read receipts, false to show them
     */
    fun hideReceipts(hide: Boolean) {
        setReceiptsVisibility(if (hide) GONE else VISIBLE)
    }

    // ==================== Refresh Methods ====================

    /**
     * Refreshes the conversation list by clearing existing data and fetching fresh.
     * This resets pagination and fetches conversations from the beginning.
     */
    fun refreshConversations() {
        viewModel?.refreshList()
    }

    // ==================== Request Builder Method ====================

    /**
     * Sets the conversations request builder for customizing conversation fetching.
     * 
     * @param builder The ConversationsRequest.ConversationsRequestBuilder to use
     */
    fun setConversationsRequestBuilder(builder: ConversationsRequest.ConversationsRequestBuilder) {
        viewModel?.setConversationsRequestBuilder(builder)
    }

    // ==================== Mention All Label Method ====================

    /**
     * Sets a custom label for the "mention all" feature for a specific ID.
     *
     * @param id The unique identifier (such as a group or user ID) for which the mention all label should be set.
     * @param mentionAllLabel The custom label to display when mentioning all members.
     *
     * If either parameter is null or empty, or if the mentions formatter is not initialized, this method does nothing.
     */
    fun setMentionAllLabelId(id: String, mentionAllLabel: String) {
        if (id.isNotEmpty() && mentionAllLabel.isNotEmpty()) {
            cometchatMentionsFormatter?.setMentionAllLabel(id, mentionAllLabel)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Cancel coroutine scope
        viewScope?.cancel()
        viewScope = null
    }
}
