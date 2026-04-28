package com.cometchat.uikit.kotlin.presentation.aiassistantchathistory.ui

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatAIAssistantChatHistoryViewModelFactory
import com.cometchat.uikit.core.state.ChatHistoryUIState
import com.cometchat.uikit.core.viewmodel.CometChatAIAssistantChatHistoryViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatAiAssistantChatHistoryBinding
import com.cometchat.uikit.kotlin.presentation.aiassistantchathistory.style.CometChatAIAssistantChatHistoryStyle
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.kotlin.shared.interfaces.Function2
import com.cometchat.uikit.kotlin.shared.interfaces.OnClick
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.shared.resources.utils.recycler_touch.ClickListener
import com.cometchat.uikit.kotlin.shared.resources.utils.recycler_touch.RecyclerTouchListener
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderAdapter
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderDecoration
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView

/**
 * CometChatAIAssistantChatHistory is a custom View that displays a scrollable,
 * date-grouped list of past AI assistant text messages with support for pagination,
 * message deletion (local and real-time), loading/empty/error states, a header with
 * close and "New Chat" actions, popup menu customization, and comprehensive theming.
 *
 * This component extends [MaterialCardView] and uses the shared
 * [CometChatAIAssistantChatHistoryViewModel] from chatuikit-core, bridging StateFlow
 * to LiveData via `asLiveData()` for XML-based observation.
 *
 * Ported from the Java implementation:
 * chatuikit/src/main/java/com/cometchat/chatuikit/aiassistantchathistory/CometChatAIAssistantChatHistory.java
 */
class CometChatAIAssistantChatHistory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometChatAIAssistantChatHistoryStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatAIAssistantChatHistory::class.java.simpleName
    }

    // ViewBinding
    private val binding: CometchatAiAssistantChatHistoryBinding

    // ViewModel (shared from chatuikit-core)
    private lateinit var viewModel: CometChatAIAssistantChatHistoryViewModel

    // Adapter
    private val adapter: AIAssistantChatHistoryAdapter

    // Layout manager
    private val layoutManager: LinearLayoutManager

    // Lifecycle
    private var lifecycleOwner: LifecycleOwner? = null
    private var isDetachedFromWindow: Boolean = false

    // Sticky header decoration
    private val stickyHeaderDecoration: StickyHeaderDecoration

    // Popup menu
    private val cometchatPopUpMenu: CometChatPopupMenu

    // Delete confirmation dialog
    private var deleteAlertDialog: CometChatConfirmDialog? = null

    // Scroll state
    private var isScrolling: Boolean = false
    private var isInProgress: Boolean = false
    private var hasMore: Boolean = true

    // Callbacks
    private var onCloseButtonClickListener: OnClick? = null
    private var onNewChatClickListener: OnClick? = null
    private var onItemClick: ((View, Int, BaseMessage?) -> Unit)? = null
    private var onItemLongClick: ((View, Int, BaseMessage?) -> Unit)? = null

    // Popup menu customization
    var options: Function2<Context, BaseMessage, List<CometChatPopupMenu.MenuItem>>? = null
    var addOptions: Function2<Context, BaseMessage, List<CometChatPopupMenu.MenuItem>>? = null

    // Visibility controls
    private var errorStateVisibility: Int = View.VISIBLE
    private var emptyStateVisibility: Int = View.VISIBLE

    // Style object
    private var style: CometChatAIAssistantChatHistoryStyle = CometChatAIAssistantChatHistoryStyle()

    init {
        // Inflate layout
        binding = CometchatAiAssistantChatHistoryBinding.inflate(
            LayoutInflater.from(context), this, true
        )

        // Reset the card view to default values
        Utils.initMaterialCard(this)

        // Initialize adapter
        adapter = AIAssistantChatHistoryAdapter(context)

        // Setup RecyclerView
        layoutManager = LinearLayoutManager(context)
        binding.rvChatHistory.layoutManager = layoutManager
        binding.rvChatHistory.adapter = adapter
        @Suppress("UNCHECKED_CAST")
        stickyHeaderDecoration = StickyHeaderDecoration(adapter as StickyHeaderAdapter<RecyclerView.ViewHolder>)
        binding.rvChatHistory.addItemDecoration(stickyHeaderDecoration, 0)

        // Setup scroll listener for pagination
        binding.rvChatHistory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                handleScrollStateChange(newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                handleScroll()
            }
        })

        // Initialize popup menu
        cometchatPopUpMenu = CometChatPopupMenu(context, 0)

        // Setup click listeners
        setupClickListeners()

        // Initialize ViewModel
        initViewModel()

        // Apply style attributes from XML
        applyStyleAttributes(attrs, defStyleAttr)
    }

    // ==================== Private Setup Methods ====================

    /**
     * Initializes the ViewModel via ViewModelProvider with the factory.
     */
    private fun initViewModel() {
        lifecycleOwner = Utils.getLifecycleOwner(context)
        if (lifecycleOwner == null) return

        val factory = CometChatAIAssistantChatHistoryViewModelFactory()
        viewModel = ViewModelProvider(
            lifecycleOwner as ViewModelStoreOwner,
            factory
        )[CometChatAIAssistantChatHistoryViewModel::class.java]

        attachObservers()
    }

    /**
     * Bridges StateFlow → LiveData via asLiveData() and observes all ViewModel streams.
     */
    private fun attachObservers() {
        val owner = lifecycleOwner ?: return
        if (!::viewModel.isInitialized) return

        viewModel.uiState.asLiveData().observe(owner) { state ->
            handleUIState(state)
        }

        viewModel.messages.asLiveData().observe(owner) { messages ->
            onMessagesReceived(messages)
        }

        viewModel.deleteState.asLiveData().observe(owner) { deleteState ->
            handleDeleteState(deleteState)
        }

        viewModel.removeMessagePosition.asLiveData().observe(owner) { position ->
            adapter.notifyItemRemoved(position)
        }

        viewModel.messagesRangeChanged.asLiveData().observe(owner) { range ->
            adapter.notifyItemRangeInserted(0, range)
        }

        viewModel.hasMore.asLiveData().observe(owner) { value ->
            hasMore = value
        }

        viewModel.isInProgress.asLiveData().observe(owner) { value ->
            isInProgress = value
        }
    }

    /**
     * Removes all LiveData observers.
     */
    private fun disposeObservers() {
        val owner = lifecycleOwner ?: return
        if (!::viewModel.isInitialized) return

        try {
            viewModel.uiState.asLiveData().removeObservers(owner)
            viewModel.messages.asLiveData().removeObservers(owner)
            viewModel.deleteState.asLiveData().removeObservers(owner)
            viewModel.removeMessagePosition.asLiveData().removeObservers(owner)
            viewModel.messagesRangeChanged.asLiveData().removeObservers(owner)
            viewModel.hasMore.asLiveData().removeObservers(owner)
            viewModel.isInProgress.asLiveData().removeObservers(owner)
        } catch (e: Exception) {
            // Silently handle observer disposal errors
        }
    }

    /**
     * Sets up click listeners for header close, new chat, and RecyclerView items.
     */
    private fun setupClickListeners() {
        binding.ivClose.setOnClickListener {
            onCloseButtonClickListener?.onClick()
        }

        binding.newChatLayout.setOnClickListener {
            onNewChatClickListener?.onClick()
        }

        binding.rvChatHistory.addOnItemTouchListener(
            RecyclerTouchListener(context, binding.rvChatHistory, object : ClickListener {
                override fun onClick(view: View, position: Int) {
                    onItemClick?.invoke(view, position, adapter.getItem(position))
                }

                override fun onLongClick(view: View, position: Int) {
                    if (onItemLongClick != null) {
                        onItemLongClick?.invoke(view, position, adapter.getItem(position))
                    } else {
                        val message = adapter.getItem(position)
                        if (message != null) {
                            preparePopupMenu(view, message)
                        }
                    }
                }
            })
        )
    }

    /**
     * Applies style attributes from XML using the TypedArray pattern.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var directAttributes = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatAIAssistantChatHistory, defStyleAttr, 0
        )
        @StyleRes val styleResId = directAttributes.getResourceId(
            R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryStyle, 0
        )
        directAttributes.recycle()

        directAttributes = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatAIAssistantChatHistory, defStyleAttr, styleResId
        )
        extractAttributesAndApplyDefaults(directAttributes)
    }

    /**
     * Extracts all 21 style attributes from the TypedArray and applies them to the UI.
     */
    private fun extractAttributesAndApplyDefaults(typedArray: TypedArray?) {
        if (typedArray == null) return
        try {
            setChatHistoryBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                )
            )
            setChatHistoryHeaderBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                )
            )
            setChatHistoryHeaderTextColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                )
            )
            setChatHistoryHeaderTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderTextAppearance,
                    0
                )
            )
            setChatHistoryHeaderCloseIcon(
                typedArray.getDrawable(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderCloseIcon
                )
            )
            setChatHistoryHeaderCloseIconTint(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderCloseIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                )
            )
            setNewChatBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                )
            )
            setNewChatTextColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                )
            )
            setNewChatTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatTextAppearance,
                    0
                )
            )
            setNewChatIcon(
                typedArray.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatIcon,
                    0
                )
            )
            setNewChatIconTint(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                )
            )
            setDateSeparatorBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDateSeparatorBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                )
            )
            setDateSeparatorTextColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDateSeparatorTextColor,
                    CometChatTheme.getTextColorTertiary(context)
                )
            )
            setDateSeparatorTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDateSeparatorTextAppearance,
                    0
                )
            )
            setItemBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryItemBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                )
            )
            setItemTextColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryItemTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                )
            )
            setItemTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryItemTextAppearance,
                    0
                )
            )
            setDeleteOptionIcon(
                typedArray.getDrawable(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionIcon
                )
            )
            setDeleteOptionIconTint(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionIconTint,
                    CometChatTheme.getErrorColor(context)
                )
            )
            setDeleteOptionTextColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                )
            )
            setDeleteOptionTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionTextAppearance,
                    0
                )
            )
        } finally {
            typedArray.recycle()
        }
    }

    // ==================== UI State Handling ====================

    /**
     * Handles UI state changes from the ViewModel.
     */
    private fun handleUIState(state: ChatHistoryUIState) {
        when (state) {
            is ChatHistoryUIState.Loading -> handleLoadingState()
            is ChatHistoryUIState.Empty -> handleEmptyState()
            is ChatHistoryUIState.Error -> handleErrorState()
            is ChatHistoryUIState.Content -> handleNonEmptyState()
        }
    }

    /**
     * Shows shimmer loading effect.
     */
    private fun handleLoadingState() {
        val shimmerAdapter = CometChatShimmerAdapter(30, R.layout.shimmer_chat_history_item)
        binding.shimmerRecyclerviewMessageList.adapter = shimmerAdapter
        binding.shimmerRecyclerviewMessageList.layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean = false
        }

        binding.rvChatHistory.visibility = View.GONE
        binding.shimmerParentLayout.visibility = View.VISIBLE
        binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(context))
        binding.shimmerEffectFrame.startShimmer()
    }

    /**
     * Shows empty state with "No conversations history" title.
     */
    private fun handleEmptyState() {
        hideShimmer()
        binding.rvChatHistory.visibility = View.GONE
        binding.errorStateView.visibility = View.GONE
        if (emptyStateVisibility == View.VISIBLE) {
            binding.tvEmptyTitle.text = resources.getString(R.string.cometchat_no_conversations_history_title)
            binding.tvEmptySubtitle.text = resources.getString(R.string.cometchat_no_conversations_history_subtitle)
            binding.emptyStateView.visibility = View.VISIBLE
        } else {
            binding.emptyStateView.visibility = View.GONE
        }
    }

    /**
     * Shows error state with error title and subtitle.
     */
    private fun handleErrorState() {
        hideShimmer()
        binding.rvChatHistory.visibility = View.GONE
        binding.emptyStateView.visibility = View.GONE
        if (errorStateVisibility == View.VISIBLE) {
            binding.tvErrorTitle.text = resources.getString(R.string.cometchat_error_conversations_title)
            binding.tvErrorSubtitle.text = resources.getString(R.string.cometchat_error_conversations_subtitle)
            binding.errorStateView.visibility = View.VISIBLE
        } else {
            binding.errorStateView.visibility = View.GONE
        }
    }

    /**
     * Shows populated list, hides all state views.
     */
    private fun handleNonEmptyState() {
        hideShimmer()
        binding.emptyStateView.visibility = View.GONE
        binding.errorStateView.visibility = View.GONE
        binding.rvChatHistory.visibility = View.VISIBLE
    }

    /**
     * Hides the shimmer loading effect.
     */
    private fun hideShimmer() {
        if (binding.shimmerEffectFrame.isShimmerRunning()) {
            binding.shimmerEffectFrame.stopShimmer()
        }
        binding.shimmerParentLayout.visibility = View.GONE
    }

    // ==================== Delete State Handling ====================

    /**
     * Handles delete state changes from the ViewModel.
     */
    private fun handleDeleteState(deleteState: UIKitConstants.DeleteState) {
        when (deleteState) {
            UIKitConstants.DeleteState.SUCCESS_DELETE -> {
                deleteAlertDialog?.dismiss()
            }
            UIKitConstants.DeleteState.FAILURE_DELETE -> {
                deleteAlertDialog?.dismiss()
                Toast.makeText(
                    context,
                    context.getString(R.string.cometchat_conversation_delete_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
            UIKitConstants.DeleteState.INITIATED_DELETE -> {
                deleteAlertDialog?.showPositiveButtonProgress(true)
            }
        }
    }

    // ==================== Messages Handling ====================

    /**
     * Callback when messages are received from ViewModel.
     */
    private fun onMessagesReceived(messages: List<BaseMessage>) {
        if (messages.isNotEmpty()) {
            adapter.setMessageList(messages)
        }
    }

    // ==================== Scroll Handling ====================

    /**
     * Handles changes in the RecyclerView scroll state.
     */
    private fun handleScrollStateChange(newState: Int) {
        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            isScrolling = true
        } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            isScrolling = false
        }
    }

    /**
     * Handles the scrolling behavior for pagination.
     * Triggers fetch when fewer than 2 items remain below the last visible position.
     */
    private fun handleScroll() {
        if (hasMore && !isInProgress) {
            if (isScrolling && adapter.itemCount > 0 &&
                ((adapter.itemCount - 1) - layoutManager.findLastVisibleItemPosition() < 2)
            ) {
                isInProgress = true
                isScrolling = false
                fetchPreviousMessages()
            }
        }
    }

    /**
     * Fetches previous messages via the ViewModel.
     */
    private fun fetchPreviousMessages() {
        if (::viewModel.isInitialized) {
            viewModel.fetchMessages()
        }
    }

    // ==================== Popup Menu ====================

    /**
     * Prepares and displays the popup menu with options for the selected chat history item.
     */
    private fun preparePopupMenu(view: View, baseMessage: BaseMessage) {
        val optionsArrayList = mutableListOf<CometChatPopupMenu.MenuItem>()

        if (options != null) {
            optionsArrayList.addAll(options!!.apply(context, baseMessage))
        } else {
            optionsArrayList.add(
                CometChatPopupMenu.MenuItem(
                    UIKitConstants.ConversationOption.DELETE,
                    context.getString(R.string.cometchat_delete),
                    style.deleteOptionIcon,
                    null,
                    style.deleteOptionIconTint,
                    0,
                    style.deleteOptionTextColor,
                    style.deleteOptionTextAppearance,
                    null
                )
            )

            if (addOptions != null) {
                optionsArrayList.addAll(addOptions!!.apply(context, baseMessage))
            }
        }

        cometchatPopUpMenu.setMenuItems(optionsArrayList)
        cometchatPopUpMenu.setOnMenuItemClickListener { id, _ ->
            for (item in optionsArrayList) {
                if (id.equals(item.id, ignoreCase = true)) {
                    if (item.onClick != null) {
                        item.onClick.invoke()
                    } else {
                        handleDefaultClickEvents(item, baseMessage)
                    }
                    break
                }
            }
            cometchatPopUpMenu.dismiss()
        }

        cometchatPopUpMenu.show(view)
    }

    /**
     * Handles default click events for menu items.
     * Shows delete confirmation dialog for the DELETE option.
     */
    private fun handleDefaultClickEvents(item: CometChatPopupMenu.MenuItem, baseMessage: BaseMessage) {
        if (item.id.equals(UIKitConstants.ConversationOption.DELETE, ignoreCase = true)) {
            deleteAlertDialog = CometChatConfirmDialog(context, R.style.CometChatConfirmDialogStyle)
            showDeleteConversationAlertDialog(baseMessage)
        }
    }

    /**
     * Shows the delete confirmation dialog.
     */
    private fun showDeleteConversationAlertDialog(baseMessage: BaseMessage) {
        deleteAlertDialog?.apply {
            setConfirmDialogIcon(
                ResourcesCompat.getDrawable(resources, R.drawable.cometchat_ic_delete, null)
            )
            setTitleText(context.getString(R.string.cometchat_conversation_delete_message_title))
            setSubtitleText(context.getString(R.string.cometchat_conversation_delete_message_subtitle))
            setPositiveButtonText(context.getString(R.string.cometchat_delete))
            setNegativeButtonText(context.getString(R.string.cometchat_cancel))
            setOnPositiveButtonClick {
                viewModel.deleteChatHistoryItem(baseMessage)
            }
            setOnNegativeButtonClick { dismiss() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }

    // ==================== Lifecycle ====================

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isDetachedFromWindow) {
            attachObservers()
            isDetachedFromWindow = false
        }
        if (::viewModel.isInitialized) {
            viewModel.addListeners()
        }
    }

    override fun onDetachedFromWindow() {
        isDetachedFromWindow = true
        if (::viewModel.isInitialized) {
            viewModel.removeListeners()
        }
        disposeObservers()
        super.onDetachedFromWindow()
    }

    // ==================== Public Setter Methods ====================

    /**
     * Sets the User context and triggers message fetching.
     *
     * @param user The User whose chat history to display
     */
    fun setUser(user: User) {
        if (::viewModel.isInitialized) {
            viewModel.setUser(user)
        }
    }

    /**
     * Sets the Group context for the chat history.
     * Builds a MessagesRequest filtered by the Group GUID.
     * Note: Unlike setUser, this does NOT auto-trigger message fetching.
     *
     * @param group The Group whose chat history to display
     */
    fun setGroup(group: Group) {
        if (::viewModel.isInitialized) {
            viewModel.setGroup(group)
        }
    }

    /**
     * Sets the click listener for the header close icon button.
     *
     * @param listener The click listener
     */
    fun setOnCloseClickListener(listener: OnClick) {
        this.onCloseButtonClickListener = listener
    }

    /**
     * Sets the click listener for the "New Chat" action row.
     *
     * @param listener The click listener
     */
    fun setOnNewChatClickListener(listener: OnClick) {
        this.onNewChatClickListener = listener
    }

    /**
     * Sets the item click listener for chat history items.
     *
     * @param listener The click listener invoked with (view, position, message)
     */
    fun setOnItemClickListener(listener: (View, Int, BaseMessage?) -> Unit) {
        this.onItemClick = listener
    }

    /**
     * Sets the item long click listener for chat history items.
     * When set, replaces the default popup menu behavior.
     *
     * @param listener The long click listener invoked with (view, position, message)
     */
    fun setOnItemLongClickListener(listener: (View, Int, BaseMessage?) -> Unit) {
        this.onItemLongClick = listener
    }

    /**
     * Sets the style for this view from the provided style resource.
     * Extracts all 21 style attributes from the resource and applies them to the UI.
     *
     * @param style The style resource ID to apply
     */
    fun setStyle(@StyleRes style: Int) {
        if (style != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                style, R.styleable.CometChatAIAssistantChatHistory
            )
            extractAttributesAndApplyDefaults(typedArray)
        }
    }

    /**
     * Sets the visibility of the error state view.
     *
     * @param visibility The visibility state (View.VISIBLE or View.GONE)
     */
    fun setErrorStateVisibility(visibility: Int) {
        this.errorStateVisibility = visibility
        binding.errorStateView.visibility = visibility
    }

    /**
     * Sets the visibility of the empty state view.
     *
     * @param visibility The visibility state (View.VISIBLE or View.GONE)
     */
    fun setEmptyStateVisibility(visibility: Int) {
        this.emptyStateVisibility = visibility
        binding.emptyStateView.visibility = visibility
    }

    // ==================== Public Getter Methods (20 style getters) ====================

    /**
     * Gets the text appearance resource ID for the delete option text.
     */
    @StyleRes
    fun getDeleteOptionTextAppearance(): Int = style.deleteOptionTextAppearance

    /**
     * Gets the text color for the delete option in the popup menu.
     */
    @ColorInt
    fun getDeleteOptionTextColor(): Int = style.deleteOptionTextColor

    /**
     * Gets the tint color for the delete option icon in the popup menu.
     */
    @ColorInt
    fun getDeleteOptionIconTint(): Int = style.deleteOptionIconTint

    /**
     * Gets the drawable icon used for the delete option in the popup menu.
     */
    fun getDeleteOptionIcon(): Drawable? = style.deleteOptionIcon

    /**
     * Gets the background color for the date separator.
     */
    @ColorInt
    fun getDateSeparatorBackgroundColor(): Int = style.dateSeparatorBackgroundColor

    /**
     * Gets the background color for the entire chat history view.
     */
    @ColorInt
    fun getChatHistoryBackgroundColor(): Int = style.chatHistoryBackgroundColor

    /**
     * Gets the background color for the chat history header.
     */
    @ColorInt
    fun getChatHistoryHeaderBackgroundColor(): Int = style.chatHistoryHeaderBackgroundColor

    /**
     * Gets the text color for the chat history header title.
     */
    @ColorInt
    fun getChatHistoryHeaderTextColor(): Int = style.chatHistoryHeaderTextColor

    /**
     * Gets the text appearance resource ID for the chat history header title.
     */
    @StyleRes
    fun getChatHistoryHeaderTextAppearance(): Int = style.chatHistoryHeaderTextAppearance

    /**
     * Gets the drawable icon used for the chat history header close button.
     */
    fun getChatHistoryHeaderCloseIcon(): Drawable? = style.chatHistoryHeaderCloseIcon

    /**
     * Gets the tint color for the chat history header close button icon.
     */
    @ColorInt
    fun getChatHistoryHeaderCloseIconTint(): Int = style.chatHistoryHeaderCloseIconTint

    /**
     * Gets the text color for the "New Chat" label.
     */
    @ColorInt
    fun getNewChatTextColor(): Int = style.newChatTextColor

    /**
     * Gets the text appearance resource ID for the "New Chat" label.
     */
    @StyleRes
    fun getNewChatTextAppearance(): Int = style.newChatTextAppearance

    /**
     * Gets the drawable resource ID for the "New Chat" icon.
     */
    @DrawableRes
    fun getNewChatIcon(): Int = style.newChatIcon

    /**
     * Gets the tint color for the "New Chat" icon.
     */
    @ColorInt
    fun getNewChatIconTint(): Int = style.newChatIconTint

    /**
     * Gets the text color for the date separator.
     */
    @ColorInt
    fun getDateSeparatorTextColor(): Int = style.dateSeparatorTextColor

    /**
     * Gets the text appearance resource ID for the date separator.
     */
    @StyleRes
    fun getDateSeparatorTextAppearance(): Int = style.dateSeparatorTextAppearance

    /**
     * Gets the background color for individual chat history items.
     */
    @ColorInt
    fun getItemBackgroundColor(): Int = style.itemBackgroundColor

    /**
     * Gets the text color for individual chat history items.
     */
    @ColorInt
    fun getItemTextColor(): Int = style.itemTextColor

    /**
     * Gets the text appearance resource ID for individual chat history items.
     */
    @StyleRes
    fun getItemTextAppearance(): Int = style.itemTextAppearance

    // ==================== Internal Style Setter Methods ====================

    /**
     * Sets the background color for the entire chat history view.
     */
    private fun setChatHistoryBackgroundColor(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(chatHistoryBackgroundColor = color)
            binding.aiAssistantChatHistoryParent.setBackgroundColor(color)
        }
    }

    /**
     * Sets the background color for the chat history header.
     */
    private fun setChatHistoryHeaderBackgroundColor(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(chatHistoryHeaderBackgroundColor = color)
            binding.chatHistoryHeader.setBackgroundColor(color)
        }
    }

    /**
     * Sets the text color for the chat history header title.
     */
    private fun setChatHistoryHeaderTextColor(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(chatHistoryHeaderTextColor = color)
            binding.tvChatHistory.setTextColor(color)
        }
    }

    /**
     * Sets the text appearance style for the chat history header title.
     */
    private fun setChatHistoryHeaderTextAppearance(@StyleRes textAppearance: Int) {
        if (textAppearance != 0) {
            style = style.copy(chatHistoryHeaderTextAppearance = textAppearance)
            binding.tvChatHistory.setTextAppearance(textAppearance)
        }
    }

    /**
     * Sets the drawable icon used for the chat history header close button.
     */
    private fun setChatHistoryHeaderCloseIcon(drawable: Drawable?) {
        if (drawable != null) {
            style = style.copy(chatHistoryHeaderCloseIcon = drawable)
            binding.ivClose.setImageDrawable(drawable)
        }
    }

    /**
     * Sets the tint color for the chat history header close button icon.
     */
    private fun setChatHistoryHeaderCloseIconTint(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(chatHistoryHeaderCloseIconTint = color)
            binding.ivClose.imageTintList = ColorStateList.valueOf(color)
        }
    }

    /**
     * Sets the background color for the "New Chat" row.
     */
    private fun setNewChatBackgroundColor(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(newChatBackgroundColor = color)
            binding.newChatLayout.setBackgroundColor(color)
        }
    }

    /**
     * Sets the text color for the "New Chat" label.
     */
    private fun setNewChatTextColor(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(newChatTextColor = color)
            binding.tvNewChat.setTextColor(color)
        }
    }

    /**
     * Sets the text appearance style for the "New Chat" label.
     */
    private fun setNewChatTextAppearance(@StyleRes textAppearance: Int) {
        if (textAppearance != 0) {
            style = style.copy(newChatTextAppearance = textAppearance)
            binding.tvNewChat.setTextAppearance(textAppearance)
        }
    }

    /**
     * Sets the icon for the "New Chat" row.
     */
    private fun setNewChatIcon(@DrawableRes icon: Int) {
        if (icon != 0) {
            style = style.copy(newChatIcon = icon)
            binding.ivNewChat.setImageResource(icon)
        }
    }

    /**
     * Sets the tint color for the "New Chat" icon.
     */
    private fun setNewChatIconTint(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(newChatIconTint = color)
            binding.ivNewChat.setColorFilter(color)
        }
    }

    /**
     * Sets the background color for the date separator headers.
     */
    private fun setDateSeparatorBackgroundColor(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(dateSeparatorBackgroundColor = color)
            adapter.setDateSeparatorBackgroundColor(color)
        }
    }

    /**
     * Sets the text color for the date separator headers.
     */
    private fun setDateSeparatorTextColor(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(dateSeparatorTextColor = color)
            adapter.setDateSeparatorTextColor(color)
        }
    }

    /**
     * Sets the text appearance style for the date separator headers.
     */
    private fun setDateSeparatorTextAppearance(@StyleRes textAppearance: Int) {
        if (textAppearance != 0) {
            style = style.copy(dateSeparatorTextAppearance = textAppearance)
            adapter.setDateSeparatorTextAppearance(textAppearance)
        }
    }

    /**
     * Sets the background color for individual chat history items.
     */
    private fun setItemBackgroundColor(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(itemBackgroundColor = color)
            adapter.setItemBackgroundColor(color)
        }
    }

    /**
     * Sets the text color for individual chat history items.
     */
    private fun setItemTextColor(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(itemTextColor = color)
            adapter.setItemTextColor(color)
        }
    }

    /**
     * Sets the text appearance style for individual chat history items.
     */
    private fun setItemTextAppearance(@StyleRes textAppearance: Int) {
        if (textAppearance != 0) {
            style = style.copy(itemTextAppearance = textAppearance)
            adapter.setItemTextAppearance(textAppearance)
        }
    }

    /**
     * Sets the drawable icon used for the delete option in the popup menu.
     */
    private fun setDeleteOptionIcon(drawable: Drawable?) {
        if (drawable != null) {
            style = style.copy(deleteOptionIcon = drawable)
        }
    }

    /**
     * Sets the tint color for the delete option icon in the popup menu.
     */
    private fun setDeleteOptionIconTint(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(deleteOptionIconTint = color)
        }
    }

    /**
     * Sets the text color for the delete option in the popup menu.
     */
    private fun setDeleteOptionTextColor(@ColorInt color: Int) {
        if (color != 0) {
            style = style.copy(deleteOptionTextColor = color)
        }
    }

    /**
     * Sets the text appearance style for the delete option in the popup menu.
     */
    private fun setDeleteOptionTextAppearance(@StyleRes textAppearance: Int) {
        if (textAppearance != 0) {
            style = style.copy(deleteOptionTextAppearance = textAppearance)
        }
    }
}