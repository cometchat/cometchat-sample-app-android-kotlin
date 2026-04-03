package com.cometchat.uikit.kotlin.presentation.users.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StyleRes
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatUsersViewModelFactory
import com.cometchat.uikit.core.state.UsersUIState
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatUsersListViewBinding
import com.cometchat.uikit.kotlin.presentation.users.style.CometChatUsersListItemStyle
import com.cometchat.uikit.kotlin.presentation.users.style.CometChatUsersStyle
import com.cometchat.uikit.kotlin.presentation.users.utils.UsersUtils
import com.cometchat.uikit.kotlin.presentation.users.utils.UsersViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.kotlin.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderDecoration
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * CometChatUsers is a custom Android View that displays a list of users
 * with support for real-time updates, selection modes, custom views, and full styling.
 *
 * This component uses the shared CometChatUsersViewModel from chatuikit-core,
 * ensuring consistent business logic with the Jetpack Compose version.
 */
class CometChatUsers @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatUsersStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatUsers::class.java.simpleName
    }

    // View Binding
    private val binding: CometchatUsersListViewBinding

    // ViewModel (shared from chatuikit-core)
    private var viewModel: CometChatUsersViewModel? = null
    private var isExternalViewModel: Boolean = false

    // Adapter (var to allow custom adapter injection)
    private var usersAdapter: UsersAdapter

    // Sticky header decoration
    private var stickyHeaderDecoration: StickyHeaderDecoration? = null

    // Overflow menu
    private var overflowMenu: View? = null

    // Lifecycle owner for observing flows
    private var lifecycleOwner: LifecycleOwner? = null

    // Coroutine scope for collecting flows
    private var viewScope: CoroutineScope? = null

    // Callbacks
    private var onItemClick: ((User) -> Unit)? = null
    private var onItemLongClick: ((User) -> Unit)? = null
    private var onError: ((CometChatException) -> Unit)? = null
    private var onLoad: ((List<User>) -> Unit)? = null
    private var onEmpty: (() -> Unit)? = null
    private var onBackPress: (() -> Unit)? = null
    private var onSelection: ((List<User>) -> Unit)? = null
    private var onSelectionChange: ((List<User>) -> Unit)? = null

    // Configuration
    private var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE

    // Custom views using ViewHolderListener pattern
    private var itemViewListener: UsersViewHolderListener? = null
    private var leadingViewListener: UsersViewHolderListener? = null
    private var titleViewListener: UsersViewHolderListener? = null
    private var subtitleViewListener: UsersViewHolderListener? = null
    private var trailingViewListener: UsersViewHolderListener? = null

    // Custom state views
    private var customEmptyView: View? = null
    private var customErrorView: View? = null
    private var customLoadingView: View? = null

    // Menu options
    private var options: ((Context, User) -> List<CometChatPopupMenu.MenuItem>)? = null
    private var addOptions: ((Context, User) -> List<CometChatPopupMenu.MenuItem>)? = null

    // Popup menu
    private val popupMenu: CometChatPopupMenu

    // Visibility controls
    private var toolbarVisibility = View.VISIBLE
    private var backIconVisibility = View.GONE
    private var searchBoxVisibility = View.VISIBLE
    private var userStatusVisibility = View.VISIBLE
    private var stickyHeaderVisibility = View.VISIBLE
    private var errorStateVisibility = View.VISIBLE
    private var loadingStateVisibility = View.VISIBLE
    private var emptyStateVisibility = View.VISIBLE

    // Style
    private var style: CometChatUsersStyle = CometChatUsersStyle()
    @StyleRes private var currentStyleResId: Int = 0

    init {
        // Inflate layout
        binding = CometchatUsersListViewBinding.inflate(
            LayoutInflater.from(context), this, true
        )

        // Reset the card view to default values
        Utils.initMaterialCard(this)

        // Initialize components
        popupMenu = CometChatPopupMenu(context, 0)

        // Initialize adapter
        usersAdapter = UsersAdapter()

        // Setup RecyclerView
        setupRecyclerView()

        // Apply XML attributes
        applyStyleAttributes(attrs, defStyleAttr)

        // Setup click listeners
        setupClickListeners()

        // Initialize ViewModel
        initViewModel()
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerviewUsersList.layoutManager = layoutManager
        binding.recyclerviewUsersList.adapter = usersAdapter
        setSeparatorVisibility(View.GONE)

        // Disable change animations for smoother updates
        val animator = binding.recyclerviewUsersList.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        // Setup sticky headers
        setupStickyHeaders()

        // Add scroll listener for pagination
        binding.recyclerviewUsersList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel?.fetchUsers()
                }
            }
        })
    }

    /**
     * Sets up sticky headers for the users list.
     */
    @Suppress("UNCHECKED_CAST")
    private fun setupStickyHeaders() {
        if (stickyHeaderVisibility == View.VISIBLE && stickyHeaderDecoration == null) {
            stickyHeaderDecoration = StickyHeaderDecoration(
                usersAdapter as com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderAdapter<RecyclerView.ViewHolder>
            )
            binding.recyclerviewUsersList.addItemDecoration(stickyHeaderDecoration!!, 0)
        }
    }

    /**
     * Removes sticky headers from the users list.
     */
    private fun removeStickyHeaders() {
        stickyHeaderDecoration?.let {
            binding.recyclerviewUsersList.removeItemDecoration(it)
            stickyHeaderDecoration = null
        }
    }

    /**
     * Sets up the shimmer loading effect for the users list.
     * Uses the same shimmer layout as ConversationList for parity.
     */
    private fun setupShimmer() {
        val shimmerAdapter = CometChatShimmerAdapter(30, R.layout.shimmer_list_base)
        binding.shimmerRecyclerview.adapter = shimmerAdapter
        binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(context))
        binding.shimmerEffectFrame.startShimmer()
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatUsers, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatUsers_cometchatUsersStyle, 0
        )
        typedArray.recycle()

        currentStyleResId = styleResId

        typedArray = if (styleResId != 0) {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatUsers, defStyleAttr, styleResId
            )
        } else {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatUsers, defStyleAttr, 0
            )
        }

        style = CometChatUsersStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

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
        binding.parentLayout.setBackgroundColor(style.backgroundColor)

        // Toolbar styling
        applyToolbarStyle()

        // Search box styling
        applySearchBoxStyle()

        // Empty state styling
        applyEmptyStateStyle()

        // Error state styling
        applyErrorStateStyle()

        // Apply item style to adapter
        usersAdapter.setItemStyle(style.itemStyle)

        // Apply sticky header styling to adapter
        applyStickyHeaderStyle()
    }

    /**
     * Applies sticky header styling to the adapter.
     */
    private fun applyStickyHeaderStyle() {
        val textColor = if (style.stickyHeaderTextColor != 0) {
            style.stickyHeaderTextColor
        } else {
            CometChatTheme.getTextColorHighlight(context)
        }
        
        val bgColor = if (style.stickyHeaderBackgroundColor != 0) {
            style.stickyHeaderBackgroundColor
        } else {
            CometChatTheme.getBackgroundColor1(context)
        }
        
        usersAdapter.setStickyTitleColor(textColor)
        if (style.stickyHeaderTextAppearance != 0) {
            usersAdapter.setStickyTitleAppearance(style.stickyHeaderTextAppearance)
        }
        usersAdapter.setStickyTitleBackgroundColor(bgColor)
        
        // Clear header cache so new styles are applied
        stickyHeaderDecoration?.clearHeaderCache()
    }

    private fun applyToolbarStyle() {
        val toolbarStyle = CometChatToolbarStyle.default(context).copy(
            backgroundColor = style.backgroundColor,
            titleTextColor = style.titleTextColor,
            titleTextAppearance = style.titleTextAppearance,
            navigationIconTint = style.backIconTint
        )
        binding.toolbar.setStyle(toolbarStyle)
        // Apply back icon drawable if provided
        style.backIcon?.let { binding.toolbar.setBackIcon(it) }
        binding.toolbar.setTitle(context.getString(R.string.cometchat_users))
        binding.toolbar.visibility = toolbarVisibility
        binding.toolbar.setBackIconVisibility(backIconVisibility)
        binding.toolbar.setOnBackPress { onBackPress?.invoke() }
    }

    private fun applySearchBoxStyle() {
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
        binding.searchBoxLayout.visibility = searchBoxVisibility
    }

    private fun applyEmptyStateStyle() {
        if (style.emptyStateTitleTextColor != 0) {
            binding.tvEmptyUsersTitle.setTextColor(style.emptyStateTitleTextColor)
        }
        if (style.emptyStateSubtitleTextColor != 0) {
            binding.tvEmptyUsersSubtitle.setTextColor(style.emptyStateSubtitleTextColor)
        }
        if (style.emptyStateTitleTextAppearance != 0) {
            binding.tvEmptyUsersTitle.setTextAppearance(style.emptyStateTitleTextAppearance)
        }
        if (style.emptyStateSubtitleTextAppearance != 0) {
            binding.tvEmptyUsersSubtitle.setTextAppearance(style.emptyStateSubtitleTextAppearance)
        }
    }

    private fun applyErrorStateStyle() {
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
        if (style.retryButtonBackgroundColor != 0) {
            binding.btnRetry.setBackgroundColor(style.retryButtonBackgroundColor)
        }
        if (style.retryButtonTextColor != 0) {
            binding.btnRetry.setTextColor(style.retryButtonTextColor)
        }
    }

    private fun setupClickListeners() {
        // Search box text change listener
        binding.searchBox.addOnSearchListener { _, query ->
            viewModel?.searchUsers(query.ifEmpty { null })
        }

        // Retry button
        binding.btnRetry.setOnClickListener {
            viewModel?.fetchUsers()
        }

        // Toolbar discard selection callback
        binding.toolbar.setOnDiscardSelection {
            viewModel?.clearSelection()
        }

        // Toolbar submit selection callback
        binding.toolbar.setOnSubmitSelection {
            onSelection?.invoke(viewModel?.getSelectedUsers() ?: emptyList())
        }

        // Adapter click listeners
        usersAdapter.setOnItemClick { _, position, user ->
            handleItemClick(user)
        }

        usersAdapter.setOnLongClick { view, position, user ->
            handleItemLongClick(view, user)
        }
    }

    private fun handleItemClick(user: User) {
        if (selectionMode != UIKitConstants.SelectionMode.NONE) {
            viewModel?.selectUser(user, selectionMode)
        } else {
            onItemClick?.invoke(user)
        }
    }

    private fun handleItemLongClick(view: View, user: User) {
        onItemLongClick?.invoke(user)

        // Show popup menu if options are configured
        val menuItems = buildMenuItems(user)
        if (menuItems.isNotEmpty()) {
            popupMenu.setMenuItems(menuItems)
            popupMenu.showAsDropDown(view)
        }
    }

    private fun buildMenuItems(user: User): List<CometChatPopupMenu.MenuItem> {
        // If custom options are provided, use them exclusively
        options?.let { return it(context, user) }

        val menuItems = mutableListOf<CometChatPopupMenu.MenuItem>()

        // Add additional options if provided
        addOptions?.let { menuItems.addAll(it(context, user)) }

        return menuItems
    }

    private fun initViewModel() {
        if (viewModel != null) return

        // Create a new ViewModel instance each time
        // This ensures fresh state when fragment is recreated
        viewModel = CometChatUsersViewModelFactory().create(CometChatUsersViewModel::class.java)
    }

    // ==================== Public API Methods ====================

    /**
     * Sets the ViewModel externally.
     */
    fun setViewModel(viewModel: CometChatUsersViewModel) {
        this.viewModel = viewModel
        this.isExternalViewModel = true
        startObservingViewModel()
    }

    /**
     * Sets the lifecycle owner for observing flows.
     */
    fun setLifecycleOwner(owner: LifecycleOwner) {
        this.lifecycleOwner = owner
        startObservingViewModel()
    }

    /**
     * Starts observing ViewModel flows.
     */
    private fun startObservingViewModel() {
        viewScope?.cancel()
        viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        viewModel?.let { vm ->
            viewScope?.launch {
                vm.uiState.collectLatest { state ->
                    handleUIState(state)
                }
            }

            viewScope?.launch {
                vm.users.collectLatest { users ->
                    usersAdapter.setList(users)
                }
            }

            viewScope?.launch {
                vm.selectedUsers.collectLatest { selected ->
                    usersAdapter.selectUsers(selected)
                    // Update toolbar selection count (matches Java implementation)
                    updateSelectionToolbar(selected.size)
                    // Notify selection change callback
                    onSelectionChange?.invoke(selected.toList())
                }
            }
        }
    }

    private fun handleUIState(state: UsersUIState) {
        when (state) {
            is UsersUIState.Loading -> showLoadingState()
            is UsersUIState.Content -> showContentState()
            is UsersUIState.Empty -> showEmptyState()
            is UsersUIState.Error -> showErrorState(state.exception)
        }
    }

    private fun showLoadingState() {
        if (loadingStateVisibility == View.VISIBLE) {
            if (customLoadingView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customLoadingView)
                binding.customLayout.visibility = View.VISIBLE
            } else {
                // Show shimmer
                binding.shimmerParentLayout.visibility = View.VISIBLE
                setupShimmer()
            }
        }
        binding.recyclerviewUsersList.visibility = View.GONE
        binding.emptyStateView.visibility = View.GONE
        binding.errorStateView.visibility = View.GONE
    }

    private fun showContentState() {
        binding.shimmerParentLayout.visibility = View.GONE
        binding.shimmerEffectFrame.stopShimmer()
        binding.customLayout.visibility = View.GONE
        binding.emptyStateView.visibility = View.GONE
        binding.errorStateView.visibility = View.GONE
        binding.recyclerviewUsersList.visibility = View.VISIBLE

        // Clear sticky header cache to reflect new data
        stickyHeaderDecoration?.clearHeaderCache()

        onLoad?.invoke(usersAdapter.getList())
    }

    private fun showEmptyState() {
        binding.shimmerParentLayout.visibility = View.GONE
        binding.shimmerEffectFrame.stopShimmer()
        binding.recyclerviewUsersList.visibility = View.GONE
        binding.errorStateView.visibility = View.GONE

        if (emptyStateVisibility == View.VISIBLE) {
            if (customEmptyView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customEmptyView)
                binding.customLayout.visibility = View.VISIBLE
            } else {
                binding.emptyStateView.visibility = View.VISIBLE
            }
        }

        onEmpty?.invoke()
    }

    private fun showErrorState(exception: CometChatException) {
        binding.shimmerParentLayout.visibility = View.GONE
        binding.shimmerEffectFrame.stopShimmer()
        binding.recyclerviewUsersList.visibility = View.GONE
        binding.emptyStateView.visibility = View.GONE

        if (errorStateVisibility == View.VISIBLE) {
            if (customErrorView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customErrorView)
                binding.customLayout.visibility = View.VISIBLE
            } else {
                binding.errorStateView.visibility = View.VISIBLE
            }
        }

        onError?.invoke(exception)
    }

    /**
     * Updates the toolbar to show selection mode with count.
     * Matches Java implementation behavior.
     */
    private fun updateSelectionToolbar(count: Int) {
        if (selectionMode != UIKitConstants.SelectionMode.NONE && count > 0) {
            binding.toolbar.setSelectionMode(true)
            binding.toolbar.setSelectionCount(count)
        } else {
            binding.toolbar.setSelectionMode(false)
        }
    }

    /**
     * Sets the item click callback.
     */
    fun setOnItemClick(callback: (User) -> Unit) {
        onItemClick = callback
    }

    /**
     * Sets the item long click callback.
     */
    fun setOnItemLongClick(callback: (User) -> Unit) {
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
    fun setOnLoad(callback: (List<User>) -> Unit) {
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
     * Sets the selection callback.
     */
    fun setOnSelection(callback: (List<User>) -> Unit) {
        onSelection = callback
    }

    /**
     * Sets the selection change callback.
     * This callback is invoked whenever the selection changes (user selected/deselected).
     * Use this to update UI elements like title with selection count.
     *
     * @param callback Callback invoked with the current list of selected users
     */
    fun setOnSelectionChange(callback: (List<User>) -> Unit) {
        onSelectionChange = callback
    }

    /**
     * Sets the selection mode.
     * Clears existing selections when mode changes (matches ConversationList behavior).
     */
    fun setSelectionMode(mode: UIKitConstants.SelectionMode) {
        // Clear existing selections (matches ConversationList/Java implementation)
        usersAdapter.selectUsers(emptySet())
        
        // Store the mode
        selectionMode = mode
        
        // Configure adapter based on mode
        usersAdapter.setSelectionEnabled(mode != UIKitConstants.SelectionMode.NONE)

        // Clear sticky header cache to redraw with correct margin alignment
        stickyHeaderDecoration?.clearHeaderCache()
    }

    /**
     * Selects or deselects a user based on the current selection mode.
     * In SINGLE mode, clears previous selection and selects the new user.
     * In MULTIPLE mode, toggles the user's selection state.
     *
     * @param user The user to select or deselect.
     * @param mode The selection mode to apply.
     */
    fun selectUser(user: User, mode: UIKitConstants.SelectionMode? = null) {
        val effectiveMode = mode ?: selectionMode
        viewModel?.selectUser(user, effectiveMode)
    }

    /**
     * Returns the list of currently selected users.
     *
     * @return List of selected users.
     */
    fun getSelectedUsers(): List<User> {
        return viewModel?.getSelectedUsers() ?: emptyList()
    }

    /**
     * Sets custom item view listener.
     */
    fun setItemView(listener: UsersViewHolderListener?) {
        itemViewListener = listener
        usersAdapter.setItemView(listener)
    }

    /**
     * Sets custom leading view listener.
     */
    fun setLeadingView(listener: UsersViewHolderListener?) {
        leadingViewListener = listener
        usersAdapter.setLeadingView(listener)
    }

    /**
     * Sets custom title view listener.
     */
    fun setTitleView(listener: UsersViewHolderListener?) {
        titleViewListener = listener
        usersAdapter.setTitleView(listener)
    }

    /**
     * Sets custom subtitle view listener.
     */
    fun setSubtitleView(listener: UsersViewHolderListener?) {
        subtitleViewListener = listener
        usersAdapter.setSubtitleView(listener)
    }

    /**
     * Sets custom trailing view listener.
     */
    fun setTrailingView(listener: UsersViewHolderListener?) {
        trailingViewListener = listener
        usersAdapter.setTrailingView(listener)
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
     * Sets menu options.
     */
    fun setOptions(options: ((Context, User) -> List<CometChatPopupMenu.MenuItem>)?) {
        this.options = options
    }

    /**
     * Sets additional menu options.
     */
    fun setAddOptions(addOptions: ((Context, User) -> List<CometChatPopupMenu.MenuItem>)?) {
        this.addOptions = addOptions
    }

    /**
     * Sets toolbar visibility.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setToolbarVisibility(visibility: Int) {
        toolbarVisibility = visibility
        binding.toolbar.visibility = visibility
    }

    /**
     * Gets the current toolbar visibility.
     *
     * @return The toolbar visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getToolbarVisibility(): Int = toolbarVisibility

    /**
     * Sets back icon visibility.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setBackIconVisibility(visibility: Int) {
        backIconVisibility = visibility
        binding.toolbar.setBackIconVisibility(visibility)
    }

    /**
     * Gets the current back icon visibility.
     *
     * @return The back icon visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getBackIconVisibility(): Int = backIconVisibility

    /**
     * Sets search box visibility.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setSearchBoxVisibility(visibility: Int) {
        searchBoxVisibility = visibility
        binding.searchBoxLayout.visibility = visibility
    }

    /**
     * Gets the current search box visibility.
     *
     * @return The search box visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getSearchBoxVisibility(): Int = searchBoxVisibility

    /**
     * Sets user status visibility.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setUserStatusVisibility(visibility: Int) {
        userStatusVisibility = visibility
        usersAdapter.setHideUserStatus(visibility == View.GONE)
    }

    /**
     * Gets the current user status visibility.
     *
     * @return The user status visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getUserStatusVisibility(): Int = userStatusVisibility

    /**
     * Sets separator visibility.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setSeparatorVisibility(visibility: Int) {
        usersAdapter.setHideSeparator(visibility != View.VISIBLE)
    }

    /**
     * Sets sticky header visibility.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setStickyHeaderVisibility(visibility: Int) {
        stickyHeaderVisibility = visibility
        if (visibility == View.VISIBLE) {
            setupStickyHeaders()
        } else {
            removeStickyHeaders()
        }
    }

    /**
     * Gets the current sticky header visibility.
     *
     * @return The sticky header visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getStickyHeaderVisibility(): Int = stickyHeaderVisibility

    /**
     * Sets the visibility of the empty state.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
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
     * Sets the visibility of the error state.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
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
     * Sets the visibility of the loading state.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
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
     * Hides or shows the loading state.
     * When hidden, the loading shimmer will not be displayed during initial fetch.
     *
     * @param hide true to hide the loading state, false to show it
     */
    fun setHideLoadingState(hide: Boolean) {
        loadingStateVisibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Hides or shows the empty state.
     * When hidden, the empty state view will not be displayed when there are no users.
     *
     * @param hide true to hide the empty state, false to show it
     */
    fun setHideEmptyState(hide: Boolean) {
        emptyStateVisibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Hides or shows the error state.
     * When hidden, the error state view will not be displayed when an error occurs.
     *
     * @param hide true to hide the error state, false to show it
     */
    fun setHideErrorState(hide: Boolean) {
        errorStateVisibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Sets the background color for the search input.
     */
    fun setSearchInputBackgroundColor(@androidx.annotation.ColorInt color: Int) {
        style = style.copy(searchBackgroundColor = color)
        binding.searchBox.setCardBackgroundColor(color)
    }

    /**
     * Sets the text color for the search input.
     */
    fun setSearchInputTextColor(@androidx.annotation.ColorInt color: Int) {
        style = style.copy(searchTextColor = color)
        binding.searchBox.setSearchInputTextColor(color)
    }

    /**
     * Sets the corner radius for the search input.
     */
    fun setSearchInputCornerRadius(@androidx.annotation.Dimension radius: Int) {
        style = style.copy(searchCornerRadius = radius)
        binding.searchBox.radius = radius.toFloat()
    }

    /**
     * Sets the stroke width for the search input.
     */
    fun setSearchInputStrokeWidth(@androidx.annotation.Dimension width: Int) {
        style = style.copy(searchStrokeWidth = width)
        binding.searchBox.strokeWidth = width
    }

    /**
     * Sets the stroke color for the search input.
     */
    fun setSearchInputStrokeColor(@androidx.annotation.ColorInt color: Int) {
        style = style.copy(searchStrokeColor = color)
        binding.searchBox.strokeColor = color
    }

    /**
     * Sets the title text color.
     */
    fun setTitleTextColor(@androidx.annotation.ColorInt color: Int) {
        style = style.copy(titleTextColor = color)
        applyToolbarStyle()
    }

    /**
     * Sets the style.
     */
    fun setStyle(style: CometChatUsersStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Gets the current style.
     */
    fun getStyle(): CometChatUsersStyle = style

    /**
     * Sets the users request builder for customizing fetch parameters.
     *
     * @param usersRequestBuilder The users request builder to set.
     */
    fun setUsersRequestBuilder(usersRequestBuilder: UsersRequest.UsersRequestBuilder) {
        viewModel?.setUsersRequestBuilder(usersRequestBuilder)
    }

    /**
     * Sets the search request builder for customizing search parameters.
     *
     * @param usersRequestBuilder The search request builder to set.
     */
    fun setSearchRequestBuilder(usersRequestBuilder: UsersRequest.UsersRequestBuilder?) {
        viewModel?.setSearchRequestBuilder(usersRequestBuilder)
    }

    /**
     * Programmatically sets a search keyword and triggers search.
     *
     * @param keyword The search keyword to set.
     */
    fun setSearchKeyword(keyword: String) {
        binding.searchBox.setSearchInputText(keyword)
        viewModel?.searchUsers(keyword.ifEmpty { null })
    }

    /**
     * Sets the overflow menu view in the toolbar.
     *
     * @param view The view to be set as the overflow menu.
     */
    fun setOverflowMenu(view: View?) {
        this.overflowMenu = view
        binding.toolbar.clearActionViews()
        if (view != null) {
            binding.toolbar.addActionView(view)
        }
    }

    /**
     * Gets the currently set overflow menu view.
     *
     * @return The overflow menu view, or null if not set.
     */
    fun getOverflowMenu(): View? = overflowMenu

    /**
     * Sets a custom adapter for the users list.
     *
     * @param usersAdapter The UsersAdapter instance to be set.
     */
    fun setAdapter(usersAdapter: UsersAdapter) {
        this.usersAdapter = usersAdapter
        binding.recyclerviewUsersList.adapter = usersAdapter
    }

    /**
     * Gets the current users adapter.
     *
     * @return The UsersAdapter instance.
     */
    fun getUsersAdapter(): UsersAdapter = usersAdapter

    /**
     * Sets the toolbar title text.
     *
     * @param title The title text to display in the toolbar.
     */
    fun setTitle(title: String) {
        binding.toolbar.setTitle(title)
    }

    /**
     * Sets the search box placeholder text.
     *
     * @param placeholder The placeholder text to display in the search box.
     */
    fun setSearchPlaceholderText(placeholder: String) {
        binding.searchBox.setSearchPlaceholderText(placeholder)
    }

    /**
     * Refreshes the users list by clearing and re-fetching from the server.
     * This is a public convenience method that delegates to the ViewModel.
     */
    fun refreshUsers() {
        viewModel?.refreshList()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startObservingViewModel()
        viewModel?.fetchUsers()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewModel?.clearSelection()
        viewScope?.cancel()
        viewScope = null
    }
}
