package com.cometchat.uikit.kotlin.presentation.reactionlist.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.core.ReactionsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Reaction
import com.cometchat.chat.models.ReactionCount
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.factory.CometChatReactionListViewModelFactory
import com.cometchat.uikit.core.state.ReactionListUIState
import com.cometchat.uikit.core.viewmodel.CometChatReactionListViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatReactionListBinding
import com.cometchat.uikit.kotlin.presentation.reactionlist.adapter.ReactedUsersAdapter
import com.cometchat.uikit.kotlin.presentation.reactionlist.adapter.ReactionsHeaderAdapter
import com.cometchat.uikit.kotlin.presentation.reactionlist.style.CometChatReactionListStyle
import com.cometchat.uikit.kotlin.presentation.reactionlist.utils.ReactionListViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * CometChatReactionList is a custom Android View that displays a list of reactions
 * and the users who reacted to a message.
 *
 * This component uses the shared CometChatReactionListViewModel from chatuikit-core,
 * ensuring consistent business logic with the Jetpack Compose version.
 *
 * Features:
 * - Horizontal scrollable tabs showing reaction emojis with counts
 * - "All" tab showing total reaction count
 * - Vertical list of users who reacted
 * - "You" label and "Tap to remove" for logged-in user's reactions
 * - Shimmer loading state
 * - Error state handling
 * - Infinite scroll pagination
 *
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.reactionlist.ui.CometChatReactionList
 *     android:id="@+id/reactionList"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 * ```
 *
 * Usage in Kotlin:
 * ```kotlin
 * val reactionList = CometChatReactionList(context)
 * reactionList.setBaseMessage(message)
 * ```
 */
class CometChatReactionList @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatReactionListStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatReactionList::class.java.simpleName
    }

    // View Binding
    private val binding: CometchatReactionListBinding

    // ViewModel (shared from chatuikit-core)
    private var viewModel: CometChatReactionListViewModel? = null
    private var isExternalViewModel: Boolean = false

    // Adapters
    private val reactionsHeaderAdapter: ReactionsHeaderAdapter
    private val reactedUsersAdapter: ReactedUsersAdapter

    // Lifecycle owner for observing flows
    private var lifecycleOwner: LifecycleOwner? = null

    // Coroutine scope for collecting flows
    private var viewScope: CoroutineScope? = null

    // State
    private var baseMessage: BaseMessage? = null
    private var selectedReaction: String? = null
    private var isReactedUserListLoading = false
    private var hideSeparator = false
    private var isUsingMockData = false
    private var mockReactions: List<Reaction> = emptyList() // Store mock reactions for local filtering

    // Custom views
    private var customLoadingView: View? = null
    private var customErrorView: View? = null

    // Callbacks
    private var onItemClick: ((Reaction, BaseMessage?) -> Unit)? = null
    private var onEmpty: (() -> Unit)? = null
    private var onError: ((CometChatException) -> Unit)? = null

    // Request builder
    private var reactionsRequestBuilder: ReactionsRequest.ReactionsRequestBuilder? = null

    // Style
    private var style: CometChatReactionListStyle = CometChatReactionListStyle()

    // Error text
    private var errorText: String? = null

    init {
        // Inflate layout
        binding = CometchatReactionListBinding.inflate(
            LayoutInflater.from(context), this, true
        )

        // Reset the card view to default values
        Utils.initMaterialCard(this)

        // Initialize adapters
        reactionsHeaderAdapter = ReactionsHeaderAdapter()
        reactedUsersAdapter = ReactedUsersAdapter(context)

        // Setup RecyclerViews
        setupRecyclerViews()

        // Apply XML attributes
        applyStyleAttributes(attrs, defStyleAttr)

        // Initialize ViewModel
        initViewModel()
    }

    /**
     * Sets up the RecyclerViews for header tabs and user list.
     */
    private fun setupRecyclerViews() {
        // Header tabs (horizontal)
        binding.recyclerViewEmojis.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        binding.recyclerViewEmojis.adapter = reactionsHeaderAdapter

        // User list (vertical)
        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewUsers.adapter = reactedUsersAdapter

        // Tab selection listener
        reactionsHeaderAdapter.setOnTabSelected { index, reaction ->
            if (isUsingMockData) {
                // Local filtering for mock data
                filterMockReactionsByTab(index, reaction)
            } else {
                // Normal flow - delegate to ViewModel
                viewModel?.setSelectedReaction(reaction)
            }
        }

        // User item click listener
        reactedUsersAdapter.setAdapterEventListener { baseMessage, reactionList, position ->
            val loggedInUserId = getLoggedInUserSafe()?.uid
            val reaction = reactionList.getOrNull(position)
            val message = baseMessage ?: this.baseMessage
            if (reaction != null && reaction.uid == loggedInUserId && message != null) {
                viewModel?.removeReaction(message, reaction.reaction)
            }
        }

        reactedUsersAdapter.setOnItemClick { reaction, message ->
            onItemClick?.invoke(reaction, message)
        }

        // Infinite scroll for pagination
        binding.recyclerViewUsers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    if (!isReactedUserListLoading) {
                        viewModel?.fetchReactedUsers()
                    }
                }
            }
        })
    }

    /**
     * Applies style attributes from XML.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatReactionList, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatReactionList_cometchatReactionListStyle, 0
        )
        typedArray.recycle()

        typedArray = if (styleResId != 0) {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatReactionList, defStyleAttr, styleResId
            )
        } else {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatReactionList, defStyleAttr, 0
            )
        }

        style = CometChatReactionListStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies the style object to all views.
     */
    private fun applyStyle() {
        // Container styling
        setCardBackgroundColor(resources.getColor(R.color.cometchat_color_transparent, context.theme))
        binding.cardReactionList.setCardBackgroundColor(style.backgroundColor)

        // Always apply stroke settings
        binding.cardReactionList.strokeWidth = style.strokeWidth
        binding.cardReactionList.strokeColor = style.strokeColor

        // Apply corner radius
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, style.cornerRadius.toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, style.cornerRadius.toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
            .build()
        binding.cardReactionList.shapeAppearanceModel = shapeAppearanceModel

        // Tab styling
        reactionsHeaderAdapter.setTextColor(style.tabTextColor)
        reactionsHeaderAdapter.setTextActiveColor(style.tabTextActiveColor)
        reactionsHeaderAdapter.setTextAppearance(style.tabTextAppearance)
        reactionsHeaderAdapter.setTabActiveIndicatorColor(style.tabActiveIndicatorColor)

        // User list item styling
        reactedUsersAdapter.applyStyle(style.itemStyle)

        // Header Separator - always visible, only color/height can be customized
        binding.viewSeparator.setBackgroundColor(style.separatorColor)
        // Header separator is always visible - hideSeparator only affects item separators

        // Error text styling
        if (style.errorTextAppearance != 0) {
            binding.tvErrorState.setTextAppearance(style.errorTextAppearance)
        }
        if (style.errorTextColor != 0) {
            binding.tvErrorState.setTextColor(style.errorTextColor)
        }
    }

    /**
     * Initializes the ViewModel and sets up observers.
     */
    private fun initViewModel() {
        lifecycleOwner = Utils.getLifecycleOwner(context)
        if (lifecycleOwner == null) return

        if (!isExternalViewModel && viewModel == null) {
            val factory = CometChatReactionListViewModelFactory()
            viewModel = ViewModelProvider(
                lifecycleOwner as androidx.lifecycle.ViewModelStoreOwner,
                factory
            )[CometChatReactionListViewModel::class.java]
        }

        observeViewModel()
    }

    /**
     * Sets up observers for the ViewModel's StateFlows.
     */
    private fun observeViewModel() {
        val vm = viewModel ?: return
        
        // Skip ViewModel observations when using mock data
        if (isUsingMockData) return

        viewScope?.cancel()
        viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Observe UI state
        viewScope?.launch {
            vm.uiState.collectLatest { state ->
                handleStateChange(state)
            }
        }

        // Observe reaction headers
        viewScope?.launch {
            vm.reactionHeaders.collectLatest { headers ->
                reactionsHeaderAdapter.updateReactionCounts(headers)
                if (headers.isEmpty()) {
                    reactedUsersAdapter.setReactions(emptyList())
                    onEmpty?.invoke()
                }
            }
        }

        // Observe reacted users
        viewScope?.launch {
            vm.reactedUsers.collectLatest { users ->
                reactedUsersAdapter.setReactions(users)
                isReactedUserListLoading = false
            }
        }

        // Observe active tab index
        viewScope?.launch {
            vm.activeTabIndex.collectLatest { index ->
                reactionsHeaderAdapter.setActiveTab(index)
            }
        }

        // Observe selected reaction
        viewScope?.launch {
            vm.selectedReaction.collectLatest { reaction ->
                selectedReaction = reaction
            }
        }
    }

    /**
     * Handles UI state changes.
     */
    private fun handleStateChange(state: ReactionListUIState) {
        when (state) {
            is ReactionListUIState.Loading -> handleLoadingState()
            is ReactionListUIState.Content -> handleContentState()
            is ReactionListUIState.Empty -> handleEmptyState()
            is ReactionListUIState.Error -> handleErrorState(state.exception)
        }
    }

    /**
     * Handles loading state.
     */
    private fun handleLoadingState() {
        isReactedUserListLoading = true
        if (customLoadingView != null) {
            binding.layoutLoadingState.removeAllViews()
            removeViewFromParent(customLoadingView)
            binding.layoutLoadingState.addView(customLoadingView)
        } else {
            val shimmerAdapter = CometChatShimmerAdapter(10, R.layout.shimmer_cometchat_reaction_list_items)
            binding.shimmerRecyclerview.layoutManager = LinearLayoutManager(context)
            binding.shimmerRecyclerview.adapter = shimmerAdapter
            binding.shimmerEffectFrame.setShimmer(
                CometChatShimmerUtils.getCometChatShimmerConfig(context)
            )
            binding.shimmerEffectFrame.startShimmer()
        }
        binding.layoutLoadingState.visibility = View.VISIBLE
        binding.recyclerViewUsers.visibility = View.GONE
        binding.layoutErrorState.visibility = View.GONE
    }

    /**
     * Handles content state.
     */
    private fun handleContentState() {
        isReactedUserListLoading = false
        if (customLoadingView == null) {
            binding.shimmerEffectFrame.stopShimmer()
        }
        binding.layoutLoadingState.visibility = View.GONE
        binding.recyclerViewUsers.visibility = View.VISIBLE
        binding.layoutErrorState.visibility = View.GONE
    }

    /**
     * Handles empty state.
     */
    private fun handleEmptyState() {
        isReactedUserListLoading = false
        if (customLoadingView == null) {
            binding.shimmerEffectFrame.stopShimmer()
        }
        binding.layoutLoadingState.visibility = View.GONE
        binding.recyclerViewUsers.visibility = View.VISIBLE
        binding.layoutErrorState.visibility = View.GONE
        onEmpty?.invoke()
    }

    /**
     * Handles error state.
     */
    private fun handleErrorState(exception: CometChatException) {
        isReactedUserListLoading = false
        if (customLoadingView == null) {
            binding.shimmerEffectFrame.stopShimmer()
        }
        
        if (customErrorView != null) {
            binding.layoutErrorState.removeAllViews()
            removeViewFromParent(customErrorView)
            binding.layoutErrorState.addView(customErrorView)
        } else {
            val displayText = errorText ?: context.getString(R.string.cometchat_reaction_list_error)
            binding.tvErrorState.text = displayText
        }
        
        binding.layoutLoadingState.visibility = View.GONE
        binding.recyclerViewUsers.visibility = View.GONE
        binding.layoutErrorState.visibility = View.VISIBLE
        
        onError?.invoke(exception)
    }

    /**
     * Removes a view from its parent if it has one.
     */
    private fun removeViewFromParent(view: View?) {
        view?.let {
            (it.parent as? android.view.ViewGroup)?.removeView(it)
        }
    }

    // ==================== Public API Methods ====================

    /**
     * Sets the base message for the reaction list.
     * This initializes the reaction tabs and fetches reacted users.
     *
     * @param message The BaseMessage to display reactions for
     */
    fun setBaseMessage(message: BaseMessage) {
        // Reset mock data mode if it was previously set
        if (isUsingMockData) {
            isUsingMockData = false
            mockReactions = emptyList() // Clear mock reactions
            observeViewModel() // Re-attach ViewModel observers
        }
        
        this.baseMessage = message
        reactedUsersAdapter.setBaseMessage(message)
        viewModel?.setBaseMessage(message)
        viewModel?.fetchReactedUsers()
    }

    /**
     * Gets the base message.
     *
     * @return The current BaseMessage
     */
    fun getBaseMessage(): BaseMessage? = baseMessage

    /**
     * Sets the reactions request builder for custom fetch configuration.
     *
     * @param builder The custom request builder
     */
    fun setReactionsRequestBuilder(builder: ReactionsRequest.ReactionsRequestBuilder) {
        this.reactionsRequestBuilder = builder
        viewModel?.setReactionsRequestBuilder(builder)
    }

    /**
     * Gets the reactions request builder.
     *
     * @return The current ReactionsRequestBuilder
     */
    fun getReactionsRequestBuilder(): ReactionsRequest.ReactionsRequestBuilder? = reactionsRequestBuilder

    /**
     * Sets the style for the reaction list.
     *
     * @param style The CometChatReactionListStyle to apply
     */
    fun setStyle(style: CometChatReactionListStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     *
     * @param styleResId The style resource ID
     */
    fun setStyle(@StyleRes styleResId: Int) {
        if (styleResId != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleResId, R.styleable.CometChatReactionList
            )
            style = CometChatReactionListStyle.fromTypedArray(context, typedArray)
            applyStyle()
        }
    }

    /**
     * Gets the current style.
     *
     * @return The current CometChatReactionListStyle
     */
    fun getStyle(): CometChatReactionListStyle = style

    /**
     * Sets the custom loading view.
     *
     * @param view The custom view to display during loading
     */
    fun setLoadingView(view: View?) {
        this.customLoadingView = view
    }

    /**
     * Gets the custom loading view.
     *
     * @return The custom loading view
     */
    fun getLoadingView(): View? = customLoadingView

    /**
     * Sets the custom error view.
     *
     * @param view The custom view to display on error
     */
    fun setErrorView(view: View?) {
        this.customErrorView = view
    }

    /**
     * Gets the custom error view.
     *
     * @return The custom error view
     */
    fun getErrorView(): View? = customErrorView

    /**
     * Sets the error text.
     *
     * @param text The error message to display
     */
    fun setErrorText(text: String?) {
        this.errorText = text
        binding.tvErrorState.text = text
    }

    /**
     * Gets the error text.
     *
     * @return The current error text
     */
    fun getErrorText(): String? = errorText

    /**
     * Sets whether to hide the item separators in the user list.
     * Note: The header separator (between tabs and user list) is always visible.
     *
     * @param hide True to hide the item separators
     */
    fun hideSeparator(hide: Boolean) {
        this.hideSeparator = hide
        // Header separator is always visible - only item separators are affected
        reactedUsersAdapter.setHideSeparator(hide)
    }

    /**
     * Gets whether the separator is hidden.
     *
     * @return True if separator is hidden
     */
    fun hideSeparator(): Boolean = hideSeparator

    /**
     * Sets the item click callback.
     *
     * @param callback The callback to invoke when an item is clicked
     */
    fun setOnItemClick(callback: ((Reaction, BaseMessage?) -> Unit)?) {
        this.onItemClick = callback
    }

    /**
     * Sets the empty state callback.
     *
     * @param callback The callback to invoke when the list is empty
     */
    fun setOnEmpty(callback: (() -> Unit)?) {
        this.onEmpty = callback
    }

    /**
     * Sets the error callback.
     *
     * @param callback The callback to invoke on error
     */
    fun setOnError(callback: ((CometChatException) -> Unit)?) {
        this.onError = callback
    }

    /**
     * Sets the selected reaction.
     *
     * @param reaction The emoji to select, or null for "All"
     */
    fun setSelectedReaction(reaction: String?) {
        this.selectedReaction = reaction
        viewModel?.setSelectedReaction(reaction)
    }

    /**
     * Gets the selected reaction.
     *
     * @return The currently selected reaction
     */
    fun getSelectedReaction(): String? = selectedReaction

    /**
     * Sets the ViewModel externally.
     *
     * @param viewModel The ViewModel to use
     */
    fun setViewModel(viewModel: CometChatReactionListViewModel) {
        this.viewModel = viewModel
        this.isExternalViewModel = true
        observeViewModel()
    }

    /**
     * Gets the ViewModel.
     *
     * @return The current ViewModel
     */
    fun getViewModel(): CometChatReactionListViewModel? = viewModel

    /**
     * Sets the loading state flag.
     *
     * @param isLoading True if loading
     */
    fun isReactedUserListLoading(isLoading: Boolean) {
        this.isReactedUserListLoading = isLoading
    }

    // ==================== Custom View Setters ====================

    /**
     * Sets custom item view listener for replacing entire item.
     * When set, the entire default item layout is replaced with the custom view.
     *
     * @param listener The listener for custom item view, or null to use default
     */
    fun setItemView(listener: ReactionListViewHolderListener?) {
        reactedUsersAdapter.setItemView(listener)
    }

    /**
     * Gets the custom item view listener.
     *
     * @return The current item view listener
     */
    fun getItemView(): ReactionListViewHolderListener? = reactedUsersAdapter.getItemView()

    /**
     * Sets custom leading view listener (avatar area).
     * When set, the default avatar is replaced with the custom view.
     *
     * @param listener The listener for custom leading view, or null to use default
     */
    fun setLeadingView(listener: ReactionListViewHolderListener?) {
        reactedUsersAdapter.setLeadingView(listener)
    }

    /**
     * Gets the custom leading view listener.
     *
     * @return The current leading view listener
     */
    fun getLeadingView(): ReactionListViewHolderListener? = reactedUsersAdapter.getLeadingView()

    /**
     * Sets custom title view listener.
     * When set, the default title (user name or "You") is replaced with the custom view.
     *
     * @param listener The listener for custom title view, or null to use default
     */
    fun setTitleView(listener: ReactionListViewHolderListener?) {
        reactedUsersAdapter.setTitleView(listener)
    }

    /**
     * Gets the custom title view listener.
     *
     * @return The current title view listener
     */
    fun getTitleView(): ReactionListViewHolderListener? = reactedUsersAdapter.getTitleView()

    /**
     * Sets custom subtitle view listener.
     * When set, the default subtitle ("Tap to remove") is replaced with the custom view.
     *
     * @param listener The listener for custom subtitle view, or null to use default
     */
    fun setSubtitleView(listener: ReactionListViewHolderListener?) {
        reactedUsersAdapter.setSubtitleView(listener)
    }

    /**
     * Gets the custom subtitle view listener.
     *
     * @return The current subtitle view listener
     */
    fun getSubtitleView(): ReactionListViewHolderListener? = reactedUsersAdapter.getSubtitleView()

    /**
     * Sets custom trailing view listener (emoji area).
     * When set, the default emoji display is replaced with the custom view.
     *
     * @param listener The listener for custom trailing view, or null to use default
     */
    fun setTrailingView(listener: ReactionListViewHolderListener?) {
        reactedUsersAdapter.setTrailingView(listener)
    }

    /**
     * Gets the custom trailing view listener.
     *
     * @return The current trailing view listener
     */
    fun getTrailingView(): ReactionListViewHolderListener? = reactedUsersAdapter.getTrailingView()

    // ==================== Individual Style Setters ====================

    /**
     * Sets the background color.
     */
    fun setReactionListBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        binding.cardReactionList.setCardBackgroundColor(color)
    }

    /**
     * Gets the background color.
     */
    @ColorInt
    fun getReactionListBackgroundColor(): Int = style.backgroundColor

    /**
     * Sets the stroke color.
     */
    fun setReactionListStrokeColor(@ColorInt color: Int) {
        style = style.copy(strokeColor = color)
        binding.cardReactionList.strokeColor = color
    }

    /**
     * Gets the stroke color.
     */
    @ColorInt
    fun getReactionListStrokeColor(): Int = style.strokeColor

    /**
     * Sets the stroke width.
     */
    fun setReactionListStrokeWidth(@Dimension width: Int) {
        style = style.copy(strokeWidth = width)
        binding.cardReactionList.strokeWidth = width
    }

    /**
     * Gets the stroke width.
     */
    @Dimension
    fun getReactionListStrokeWidth(): Int = style.strokeWidth

    /**
     * Sets the corner radius.
     */
    fun setReactionListCornerRadius(@Dimension radius: Int) {
        style = style.copy(cornerRadius = radius)
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
            .build()
        binding.cardReactionList.shapeAppearanceModel = shapeAppearanceModel
    }

    /**
     * Gets the corner radius.
     */
    @Dimension
    fun getReactionListCornerRadius(): Int = style.cornerRadius

    /**
     * Sets the tab text color.
     */
    fun setReactionListTabTextColor(@ColorInt color: Int) {
        style = style.copy(tabTextColor = color)
        reactionsHeaderAdapter.setTextColor(color)
    }

    /**
     * Gets the tab text color.
     */
    @ColorInt
    fun getReactionListTabTextColor(): Int = style.tabTextColor

    /**
     * Sets the active tab text color.
     */
    fun setReactionListTabTextActiveColor(@ColorInt color: Int) {
        style = style.copy(tabTextActiveColor = color)
        reactionsHeaderAdapter.setTextActiveColor(color)
    }

    /**
     * Gets the active tab text color.
     */
    @ColorInt
    fun getReactionListTabTextActiveColor(): Int = style.tabTextActiveColor

    /**
     * Sets the tab text appearance.
     */
    fun setReactionListTabTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(tabTextAppearance = appearance)
        reactionsHeaderAdapter.setTextAppearance(appearance)
    }

    /**
     * Gets the tab text appearance.
     */
    @StyleRes
    fun getReactionListTabTextAppearance(): Int = style.tabTextAppearance

    /**
     * Sets the active tab indicator color.
     */
    fun setReactionListTabActiveIndicatorColor(@ColorInt color: Int) {
        style = style.copy(tabActiveIndicatorColor = color)
        reactionsHeaderAdapter.setTabActiveIndicatorColor(color)
    }

    /**
     * Gets the active tab indicator color.
     */
    @ColorInt
    fun getReactionListTabActiveIndicatorColor(): Int = style.tabActiveIndicatorColor

    /**
     * Sets the title text color.
     */
    fun setReactionListTitleTextColor(@ColorInt color: Int) {
        val newItemStyle = style.itemStyle.copy(titleTextColor = color)
        style = style.copy(itemStyle = newItemStyle)
        reactedUsersAdapter.setTitleTextColor(color)
    }

    /**
     * Gets the title text color.
     */
    @ColorInt
    fun getReactionListTitleTextColor(): Int = style.itemStyle.titleTextColor

    /**
     * Sets the title text appearance.
     */
    fun setReactionListTitleTextAppearance(@StyleRes appearance: Int) {
        val newItemStyle = style.itemStyle.copy(titleTextAppearance = appearance)
        style = style.copy(itemStyle = newItemStyle)
        reactedUsersAdapter.setTitleTextAppearance(appearance)
    }

    /**
     * Gets the title text appearance.
     */
    @StyleRes
    fun getReactionListTitleTextAppearance(): Int = style.itemStyle.titleTextAppearance

    /**
     * Sets the subtitle text color.
     */
    fun setReactionListSubTitleTextColor(@ColorInt color: Int) {
        val newItemStyle = style.itemStyle.copy(subtitleTextColor = color)
        style = style.copy(itemStyle = newItemStyle)
        reactedUsersAdapter.setSubtitleTextColor(color)
    }

    /**
     * Gets the subtitle text color.
     */
    @ColorInt
    fun getReactionListSubTitleTextColor(): Int = style.itemStyle.subtitleTextColor

    /**
     * Sets the subtitle text appearance.
     */
    fun setReactionListSubTitleTextAppearance(@StyleRes appearance: Int) {
        val newItemStyle = style.itemStyle.copy(subtitleTextAppearance = appearance)
        style = style.copy(itemStyle = newItemStyle)
        reactedUsersAdapter.setSubtitleTextAppearance(appearance)
    }

    /**
     * Gets the subtitle text appearance.
     */
    @StyleRes
    fun getReactionListSubTitleTextAppearance(): Int = style.itemStyle.subtitleTextAppearance

    /**
     * Sets the tail view text appearance.
     */
    fun setReactionListTailViewTextAppearance(@StyleRes appearance: Int) {
        val newItemStyle = style.itemStyle.copy(tailViewTextAppearance = appearance)
        style = style.copy(itemStyle = newItemStyle)
        reactedUsersAdapter.setTailViewTextAppearance(appearance)
    }

    /**
     * Gets the tail view text appearance.
     */
    @StyleRes
    fun getReactionListTailViewTextAppearance(): Int = style.itemStyle.tailViewTextAppearance

    /**
     * Sets the avatar style.
     */
    fun setReactionListAvatarStyle(@StyleRes styleResId: Int) {
        val newItemStyle = style.itemStyle.copy(avatarStyleResId = styleResId)
        style = style.copy(itemStyle = newItemStyle)
        reactedUsersAdapter.setAvatarStyle(styleResId)
    }

    /**
     * Gets the avatar style.
     */
    @StyleRes
    fun getReactionListAvatarStyle(): Int = style.itemStyle.avatarStyleResId

    /**
     * Sets the error text color.
     */
    fun setReactionListErrorTextColor(@ColorInt color: Int) {
        style = style.copy(errorTextColor = color)
        binding.tvErrorState.setTextColor(color)
    }

    /**
     * Gets the error text color.
     */
    @ColorInt
    fun getReactionListErrorTextColor(): Int = style.errorTextColor

    /**
     * Sets the error text appearance.
     */
    fun setReactionListErrorTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(errorTextAppearance = appearance)
        if (appearance != 0) {
            binding.tvErrorState.setTextAppearance(appearance)
        }
    }

    /**
     * Gets the error text appearance.
     */
    @StyleRes
    fun getReactionListErrorTextAppearance(): Int = style.errorTextAppearance

    /**
     * Sets the separator color.
     */
    fun setReactionListSeparatorColor(@ColorInt color: Int) {
        style = style.copy(separatorColor = color)
        binding.viewSeparator.setBackgroundColor(color)
    }

    /**
     * Gets the separator color.
     */
    @ColorInt
    fun getReactionListSeparatorColor(): Int = style.separatorColor

    // ==================== Mock Data for Showcase ====================

    /**
     * Sets mock data directly for showcase/demo purposes only.
     * This bypasses the ViewModel and SDK calls, displaying static data.
     * 
     * WARNING: This method is intended ONLY for demo/showcase applications.
     * Do NOT use this in production code. Use [setBaseMessage] instead for
     * normal operation with real SDK data.
     * 
     * After calling this method, the component will not respond to ViewModel
     * updates until [setBaseMessage] is called, which resets the mock mode.
     *
     * @param message The mock BaseMessage with reaction counts
     * @param reactions The list of mock Reaction objects with user data
     */
    fun setMockData(message: BaseMessage, reactions: List<Reaction>) {
        // Mark as using mock data and cancel ViewModel observations
        isUsingMockData = true
        viewScope?.cancel()
        viewScope = null
        
        this.baseMessage = message
        reactedUsersAdapter.setBaseMessage(message)
        
        // Create reaction headers with "All" tab first
        val originalCounts = message.reactions ?: emptyList()
        val totalCount = originalCounts.sumOf { it.count }
        
        // Create "All" tab as first item
        val allTab = ReactionCount().apply {
            reaction = context.getString(R.string.cometchat_all)
            count = totalCount
            setReactedByMe(false)
        }
        
        // Build header list with "All" first, then individual reactions
        val headerList = mutableListOf<ReactionCount>()
        headerList.add(allTab)
        headerList.addAll(originalCounts)
        
        reactionsHeaderAdapter.updateReactionCounts(headerList)
        
        // Store mock reactions for local filtering
        this.mockReactions = reactions
        
        // Set reacted users directly (show all initially)
        reactedUsersAdapter.setReactions(reactions)
        
        // Show content state
        handleContentState()
    }
    
    /**
     * Filters mock reactions by the selected tab.
     * This is used only when isUsingMockData is true.
     * 
     * @param index The tab index (0 = "All", 1+ = emoji tabs)
     * @param reaction The reaction string ("All" or emoji)
     */
    private fun filterMockReactionsByTab(index: Int, reaction: String) {
        val allText = context.getString(R.string.cometchat_all)
        val filteredReactions = if (index == 0 || reaction == allText) {
            // "All" tab - show all reactions
            mockReactions
        } else {
            // Emoji-specific tab - filter by emoji
            mockReactions.filter { it.reaction == reaction }
        }
        reactedUsersAdapter.setReactions(filteredReactions)
    }

    // ==================== Lifecycle Methods ====================

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        reactedUsersAdapter.setBaseMessage(baseMessage)
        
        // Skip ViewModel fetching when using mock data
        if (isUsingMockData) return
        
        // Re-initialize if base message is set (only if not using mock data)
        if (reactedUsersAdapter.itemCount == 0) {
            baseMessage?.let { message ->
                viewModel?.setBaseMessage(message)
                viewModel?.fetchReactedUsers()
            }
        }
    }

    override fun onDetachedFromWindow() {
        viewScope?.cancel()
        viewScope = null
        super.onDetachedFromWindow()
    }

    /**
     * Safely gets the logged-in user.
     */
    private fun getLoggedInUserSafe(): com.cometchat.chat.models.User? {
        return try {
            CometChatUIKit.getLoggedInUser()
        } catch (e: Exception) {
            null
        }
    }
}
