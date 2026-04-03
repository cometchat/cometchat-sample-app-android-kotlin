package com.cometchat.uikit.kotlin.presentation.groups.ui

import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StyleRes
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatGroupsViewModelFactory
import com.cometchat.uikit.core.state.GroupsUIState
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatGroupsListViewBinding
import com.cometchat.uikit.kotlin.presentation.groups.adapter.GroupsAdapter
import com.cometchat.uikit.kotlin.presentation.groups.style.CometChatGroupsItemStyle
import com.cometchat.uikit.kotlin.presentation.groups.style.CometChatGroupsStyle
import com.cometchat.uikit.kotlin.presentation.groups.utils.GroupsViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * CometChatGroups is a custom Android View that displays a list of groups
 * with support for real-time updates, selection modes, custom views, and full styling.
 *
 * This component uses the shared CometChatGroupsViewModel from chatuikit-core,
 * ensuring consistent business logic with the Jetpack Compose version.
 *
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.groups.ui.CometChatGroups
 *     android:id="@+id/groupsList"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:cometchatGroupsStyle="@style/CometChatGroupsStyle" />
 * ```
 *
 * Usage in Kotlin:
 * ```kotlin
 * val groupsList = CometChatGroups(context)
 * groupsList.setOnItemClick { group ->
 *     // Handle click
 * }
 * ```
 */
class CometChatGroups @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatGroupsStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatGroups::class.java.simpleName
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    // View Binding
    private val binding: CometchatGroupsListViewBinding

    // ViewModel (shared from chatuikit-core)
    private var viewModel: CometChatGroupsViewModel? = null
    private var isExternalViewModel: Boolean = false

    // Adapter
    private val groupsAdapter: GroupsAdapter

    // Lifecycle owner for observing flows
    private var lifecycleOwner: LifecycleOwner? = null

    // Coroutine scope for collecting flows
    private var viewScope: CoroutineScope? = null
    private var searchJob: Job? = null

    // Selection state
    private val selectedGroups = HashMap<Group, Boolean>()

    // Callbacks
    private var onItemClick: ((Group) -> Unit)? = null
    private var onItemLongClick: ((Group) -> Unit)? = null
    private var onError: ((CometChatException) -> Unit)? = null
    private var onLoad: ((List<Group>) -> Unit)? = null
    private var onEmpty: (() -> Unit)? = null
    private var onBackPress: (() -> Unit)? = null
    private var onSelection: ((List<Group>) -> Unit)? = null

    // Configuration
    private var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE
    private var groupsRequestBuilder: GroupsRequest.GroupsRequestBuilder? = null
    private var searchRequestBuilder: GroupsRequest.GroupsRequestBuilder? = null

    // Custom views using ViewHolderListener pattern
    private var itemViewListener: GroupsViewHolderListener? = null
    private var leadingViewListener: GroupsViewHolderListener? = null
    private var titleViewListener: GroupsViewHolderListener? = null
    private var subtitleViewListener: GroupsViewHolderListener? = null
    private var trailingViewListener: GroupsViewHolderListener? = null

    // Custom state views
    private var customEmptyView: View? = null
    private var customErrorView: View? = null
    private var customLoadingView: View? = null

    // Overflow menu
    private var overflowMenu: View? = null

    // Search placeholder
    private var searchPlaceholderText: String? = null

    // Title visibility
    private var titleVisibility: Int = View.VISIBLE

    // Menu options
    private var options: ((Context, Group) -> List<CometChatPopupMenu.MenuItem>)? = null
    private var addOptions: ((Context, Group) -> List<CometChatPopupMenu.MenuItem>)? = null

    // Popup menu
    private val popupMenu: CometChatPopupMenu

    // Visibility controls
    private var toolbarVisibility = View.VISIBLE
    private var backIconVisibility = View.GONE
    private var searchBoxVisibility = View.VISIBLE
    private var groupTypeVisibility = View.VISIBLE
    private var separatorVisibility = View.VISIBLE
    private var errorStateVisibility = View.VISIBLE
    private var loadingStateVisibility = View.VISIBLE
    private var emptyStateVisibility = View.VISIBLE

    // Style
    private var style: CometChatGroupsStyle = CometChatGroupsStyle()
    @StyleRes private var currentStyleResId: Int = 0

    init {
        // Inflate layout
        binding = CometchatGroupsListViewBinding.inflate(
            LayoutInflater.from(context), this, true
        )

        // Reset the card view to default values
        Utils.initMaterialCard(this)

        // Initialize components
        popupMenu = CometChatPopupMenu(context, 0)

        // Initialize adapter
        groupsAdapter = GroupsAdapter()

        // Setup RecyclerView
        setupRecyclerView()

        // Apply XML attributes
        applyStyleAttributes(attrs, defStyleAttr)

        // Setup click listeners
        setupClickListeners()

        // Setup search
        setupSearch()

        // Initialize ViewModel
        initViewModel()
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerviewGroupsList.layoutManager = layoutManager
        binding.recyclerviewGroupsList.adapter = groupsAdapter
        setSeparatorVisibility(View.GONE)

        // Disable change animations for smoother updates
        val animator = binding.recyclerviewGroupsList.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        // Add scroll listener for pagination
        binding.recyclerviewGroupsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel?.fetchGroups()
                }
            }
        })
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatGroups, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatGroups_cometchatGroupsStyle, 0
        )
        typedArray.recycle()

        currentStyleResId = styleResId

        typedArray = if (styleResId != 0) {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatGroups, defStyleAttr, styleResId
            )
        } else {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatGroups, defStyleAttr, 0
            )
        }

        style = CometChatGroupsStyle.fromTypedArray(context, typedArray)
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
        binding.toolbar.setBackgroundColor(style.backgroundColor)
        if (style.titleTextAppearance != 0) {
            binding.toolbar.setTitleTextAppearance(style.titleTextAppearance)
        }
        if (style.titleTextColor != 0) {
            binding.toolbar.setTitleTextColor(style.titleTextColor)
        }
        style.backIcon?.let { binding.toolbar.setBackIcon(it) }
        if (style.backIconTint != 0) {
            binding.toolbar.setBackIconTint(style.backIconTint)
        }
        binding.toolbar.setBackIconVisibility(backIconVisibility)

        // Selection icons
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
            binding.tvEmptyGroupsTitle.setTextColor(style.emptyStateTitleTextColor)
        }
        if (style.emptyStateSubtitleTextColor != 0) {
            binding.tvEmptyGroupsSubtitle.setTextColor(style.emptyStateSubtitleTextColor)
        }
        if (style.emptyStateTitleTextAppearance != 0) {
            binding.tvEmptyGroupsTitle.setTextAppearance(style.emptyStateTitleTextAppearance)
        }
        if (style.emptyStateSubtitleTextAppearance != 0) {
            binding.tvEmptyGroupsSubtitle.setTextAppearance(style.emptyStateSubtitleTextAppearance)
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
    }

    private fun updateAdapterItemStyle() {
        groupsAdapter.setItemStyle(style.itemStyle)
    }

    private fun setupClickListeners() {
        // Adapter item click
        groupsAdapter.setOnItemClick { view, position, group ->
            if (onItemClick != null) {
                onItemClick?.invoke(group)
            } else {
                if (selectionMode != UIKitConstants.SelectionMode.NONE) {
                    viewModel?.selectGroup(group, selectionMode)
                }
            }
        }

        // Adapter item long click
        groupsAdapter.setOnItemLongClick { view, position, group ->
            if (onItemLongClick != null) {
                onItemLongClick?.invoke(group)
            } else {
                preparePopupMenu(view, group)
            }
        }

        // Toolbar callbacks
        binding.toolbar.setOnBackPress {
            onBackPress?.invoke()
        }

        binding.toolbar.setOnDiscardSelection {
            viewModel?.clearSelection()
        }

        binding.toolbar.setOnSubmitSelection {
            onSelection?.invoke(viewModel?.getSelectedGroups() ?: emptyList())
        }

        // Retry button
        binding.btnRetry.setOnClickListener {
            viewModel?.refreshList()
        }
    }

    private fun setupSearch() {
        binding.searchBox.binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = viewScope?.launch {
                    delay(SEARCH_DEBOUNCE_MS)
                    val query = s?.toString()?.trim()
                    if (query.isNullOrEmpty()) {
                        viewModel?.refreshList()
                    } else {
                        viewModel?.searchGroups(query)
                    }
                }
            }
        })
    }

    private fun initViewModel() {
        lifecycleOwner = Utils.getLifecycleOwner(context)
        if (lifecycleOwner == null) return

        if (!isExternalViewModel && viewModel == null) {
            // Create a new ViewModel instance each time
            // This ensures fresh state when fragment is recreated
            val factory = CometChatGroupsViewModelFactory()
            viewModel = factory.create(CometChatGroupsViewModel::class.java)
        }

        // Apply request builders if set
        groupsRequestBuilder?.let { viewModel?.setGroupsRequestBuilder(it) }
        searchRequestBuilder?.let { viewModel?.setSearchRequestBuilder(it) }

        observeViewModel()
    }

    private fun observeViewModel() {
        val vm = viewModel ?: return

        viewScope?.cancel()
        viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Observe groups
        viewScope?.launch {
            vm.groups.collectLatest { groups ->
                groupsAdapter.setGroupList(groups)
                onLoad?.invoke(groups)
            }
        }

        // Observe UI state
        viewScope?.launch {
            vm.uiState.collectLatest { state ->
                handleStateChange(state)
            }
        }

        // Observe selected groups
        viewScope?.launch {
            vm.selectedGroups.collectLatest { selected ->
                groupsAdapter.setSelectedGroups(selected)
                updateSelectionToolbar(selected.size)
            }
        }

        // Observe scroll to top event
        viewScope?.launch {
            vm.scrollToTopEvent.collectLatest {
                scrollToTop()
            }
        }
    }

    private fun handleStateChange(state: GroupsUIState) {
        hideAllStates()

        when (state) {
            is GroupsUIState.Loading -> handleLoadingState()
            is GroupsUIState.Content -> handleContentState()
            is GroupsUIState.Empty -> handleEmptyState()
            is GroupsUIState.Error -> handleErrorState(state.exception)
        }
    }

    private fun hideAllStates() {
        binding.recyclerviewGroupsList.visibility = View.GONE
        binding.emptyStateView.visibility = View.GONE
        binding.errorStateView.visibility = View.GONE
        binding.shimmerParentLayout.visibility = View.GONE
        binding.customLayout.visibility = View.GONE
    }

    private fun handleLoadingState() {
        if (loadingStateVisibility == View.VISIBLE) {
            if (customLoadingView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customLoadingView)
                binding.customLayout.visibility = View.VISIBLE
            } else {
                binding.shimmerParentLayout.visibility = View.VISIBLE
                setupShimmer()
            }
        }
        announceForAccessibility(context.getString(R.string.cometchat_loading_groups))
    }

    private fun setupShimmer() {
        val shimmerAdapter = CometChatShimmerAdapter(30, R.layout.shimmer_list_base)
        binding.shimmerRecyclerview.adapter = shimmerAdapter
        binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(context))
        binding.shimmerEffectFrame.startShimmer()
    }

    private fun handleContentState() {
        binding.shimmerEffectFrame.stopShimmer()
        binding.recyclerviewGroupsList.visibility = View.VISIBLE
    }

    private fun handleEmptyState() {
        binding.shimmerEffectFrame.stopShimmer()
        if (emptyStateVisibility == View.VISIBLE) {
            if (customEmptyView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customEmptyView)
                binding.customLayout.visibility = View.VISIBLE
            } else {
                binding.emptyStateView.visibility = View.VISIBLE
            }
        }
        announceForAccessibility(context.getString(R.string.cometchat_no_groups))
        onEmpty?.invoke()
    }

    private fun handleErrorState(exception: CometChatException? = null) {
        binding.shimmerEffectFrame.stopShimmer()
        if (errorStateVisibility == View.VISIBLE) {
            if (customErrorView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customErrorView)
                binding.customLayout.visibility = View.VISIBLE
            } else {
                binding.errorStateView.visibility = View.VISIBLE
            }
        }
        announceForAccessibility(context.getString(R.string.cometchat_error_loading_groups))
        exception?.let { onError?.invoke(it) }
    }

    private fun updateSelectionToolbar(count: Int) {
        if (selectionMode != UIKitConstants.SelectionMode.NONE && count > 0) {
            binding.toolbar.setSelectionMode(true)
            binding.toolbar.setSelectionCount(count)
        } else {
            binding.toolbar.setSelectionMode(false)
        }
    }

    private fun preparePopupMenu(view: View, group: Group) {
        val menuItems = mutableListOf<CometChatPopupMenu.MenuItem>()

        if (options != null) {
            menuItems.addAll(options!!.invoke(context, group))
        }

        addOptions?.let { menuItems.addAll(it.invoke(context, group)) }

        if (menuItems.isEmpty()) return

        popupMenu.setMenuItems(menuItems)
        popupMenu.setOnMenuItemClickListener { id, name ->
            for (item in menuItems) {
                if (id.equals(item.id, ignoreCase = true)) {
                    item.onClick?.invoke()
                    break
                }
            }
            popupMenu.dismiss()
        }

        popupMenu.show(view)
    }

    private fun scrollToTop() {
        val layoutManager = binding.recyclerviewGroupsList.layoutManager as? LinearLayoutManager
        val firstVisiblePosition = layoutManager?.findFirstVisibleItemPosition() ?: 0
        if (firstVisiblePosition < 3) {
            binding.recyclerviewGroupsList.scrollToPosition(0)
        }
    }

    // ==================== Public API Methods ====================

    fun setOnItemClick(callback: (Group) -> Unit) {
        onItemClick = callback
    }

    fun setOnItemLongClick(callback: (Group) -> Unit) {
        onItemLongClick = callback
    }

    fun setOnError(callback: (CometChatException) -> Unit) {
        onError = callback
    }

    fun setOnLoad(callback: (List<Group>) -> Unit) {
        onLoad = callback
    }

    fun setOnEmpty(callback: () -> Unit) {
        onEmpty = callback
    }

    fun setOnBackPress(callback: () -> Unit) {
        onBackPress = callback
    }

    fun setOnSelection(callback: (List<Group>) -> Unit) {
        onSelection = callback
    }

    fun setSelectionMode(mode: UIKitConstants.SelectionMode) {
        selectionMode = mode
        groupsAdapter.setSelectionMode(mode)
    }

    fun setGroupsRequestBuilder(builder: GroupsRequest.GroupsRequestBuilder) {
        groupsRequestBuilder = builder
        viewModel?.setGroupsRequestBuilder(builder)
    }

    fun setSearchRequestBuilder(builder: GroupsRequest.GroupsRequestBuilder) {
        searchRequestBuilder = builder
        viewModel?.setSearchRequestBuilder(builder)
    }

    fun setViewModel(vm: CometChatGroupsViewModel) {
        viewModel = vm
        isExternalViewModel = true
        observeViewModel()
    }

    fun setStyle(style: CometChatGroupsStyle) {
        this.style = style
        applyStyle()
    }

    fun setItemView(listener: GroupsViewHolderListener?) {
        itemViewListener = listener
        groupsAdapter.setItemView(listener)
    }

    fun setLeadingView(listener: GroupsViewHolderListener?) {
        leadingViewListener = listener
        groupsAdapter.setLeadingView(listener)
    }

    fun setTitleView(listener: GroupsViewHolderListener?) {
        titleViewListener = listener
        groupsAdapter.setTitleView(listener)
    }

    fun setSubtitleView(listener: GroupsViewHolderListener?) {
        subtitleViewListener = listener
        groupsAdapter.setSubtitleView(listener)
    }

    fun setTrailingView(listener: GroupsViewHolderListener?) {
        trailingViewListener = listener
        groupsAdapter.setTrailingView(listener)
    }

    fun setEmptyView(view: View?) {
        customEmptyView = view
    }

    fun setErrorView(view: View?) {
        customErrorView = view
    }

    fun setLoadingView(view: View?) {
        customLoadingView = view
    }

    fun setOptions(optionsProvider: (Context, Group) -> List<CometChatPopupMenu.MenuItem>) {
        options = optionsProvider
    }

    fun addOptions(optionsProvider: (Context, Group) -> List<CometChatPopupMenu.MenuItem>) {
        addOptions = optionsProvider
    }

    fun setTitle(title: String) {
        binding.toolbar.setTitle(title)
    }

    fun refreshList() {
        viewModel?.refreshList()
    }

    fun getSelectedGroups(): List<Group> {
        return viewModel?.getSelectedGroups() ?: emptyList()
    }

    fun clearSelection() {
        viewModel?.clearSelection()
    }

    /**
     * Refreshes the component's style by re-extracting values from the current theme.
     * Call this method when the theme changes to update all theme-dependent colors and properties.
     * 
     * This method re-extracts style attributes from the theme without triggering a full
     * data refresh, preserving scroll position during theme changes.
     */
    fun refreshStyle() {
        // Re-apply style attributes using the stored style resource ID
        val typedArray = if (currentStyleResId != 0) {
            context.theme.obtainStyledAttributes(
                null, R.styleable.CometChatGroups, R.attr.cometchatGroupsStyle, currentStyleResId
            )
        } else {
            context.theme.obtainStyledAttributes(
                null, R.styleable.CometChatGroups, R.attr.cometchatGroupsStyle, 0
            )
        }
        // fromTypedArray handles recycling internally
        style = CometChatGroupsStyle.fromTypedArray(context, typedArray)
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
     * Sets a custom overflow menu view in the toolbar.
     * The overflow menu appears in the toolbar's trailing area.
     *
     * @param view The view to set as overflow menu, or null to remove
     */
    fun setOverflowMenu(view: View?) {
        this.overflowMenu = view
        binding.toolbar.clearActionViews()
        if (view != null) {
            binding.toolbar.addActionView(view)
        }
    }

    /**
     * Gets the current overflow menu view.
     *
     * @return The overflow menu view, or null if not set
     */
    fun getOverflowMenu(): View? = overflowMenu

    /**
     * Sets the placeholder text for the search input.
     *
     * @param text The placeholder text to display
     */
    fun setSearchPlaceholderText(text: String) {
        this.searchPlaceholderText = text
        binding.searchBox.setSearchPlaceholderText(text)
    }

    /**
     * Gets the current search placeholder text.
     *
     * @return The search placeholder text, or null if not set
     */
    fun getSearchPlaceholderText(): String? = searchPlaceholderText

    /**
     * Programmatically sets the search keyword and triggers a search.
     * This updates the search input text and performs the search.
     *
     * @param keyword The search keyword, or null/empty to clear search
     */
    fun setSearchKeyword(keyword: String?) {
        binding.searchBox.setSearchInputText(keyword ?: "")
        if (keyword.isNullOrEmpty()) {
            viewModel?.refreshList()
        } else {
            viewModel?.searchGroups(keyword)
        }
    }

    /**
     * Programmatically selects a group based on the selection mode.
     * In SINGLE mode, clears existing selection and selects the provided group.
     * In MULTIPLE mode, toggles the selection state of the provided group.
     *
     * @param group The group to select
     * @param mode The selection mode to use
     */
    fun selectGroup(group: Group, mode: UIKitConstants.SelectionMode) {
        if (mode == UIKitConstants.SelectionMode.NONE) return
        this.selectionMode = mode
        groupsAdapter.setSelectionMode(mode)
        viewModel?.selectGroup(group, mode)
    }

    /**
     * Sets the visibility of the toolbar.
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
     * Sets the visibility of the search box.
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
     * Sets the visibility of the back icon.
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
     * When hidden, the empty state view will not be displayed when there are no groups.
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
     * Sets the visibility of the group type indicator.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setGroupTypeVisibility(visibility: Int) {
        groupTypeVisibility = visibility
        groupsAdapter.setHideGroupType(visibility != View.VISIBLE)
    }

    /**
     * Gets the current group type visibility.
     *
     * @return The group type visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getGroupTypeVisibility(): Int = groupTypeVisibility

    /**
     * Sets the visibility of the toolbar title.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setTitleVisibility(visibility: Int) {
        this.titleVisibility = visibility
        // The toolbar doesn't expose direct title visibility, so we use an empty title when hidden
        // This is a workaround - ideally CometChatToolbar would have setTitleVisibility
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            binding.toolbar.setTitle("")
        }
    }

    /**
     * Gets the current title visibility.
     *
     * @return The title visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getTitleVisibility(): Int = titleVisibility

    /**
     * Sets the visibility of the separator line between list items.
     *
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setSeparatorVisibility(visibility: Int) {
        separatorVisibility = visibility
        groupsAdapter.setHideSeparator(visibility != View.VISIBLE)
    }

    /**
     * Gets the current separator visibility.
     *
     * @return The separator visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getSeparatorVisibility(): Int = separatorVisibility

    /**
     * Gets the ViewModel associated with this component.
     *
     * @return The CometChatGroupsViewModel instance, or null if not initialized
     */
    fun getViewModel(): CometChatGroupsViewModel? = viewModel

    /**
     * Gets the adapter used for the groups list.
     *
     * @return The GroupsAdapter instance
     */
    fun getAdapter(): GroupsAdapter = groupsAdapter

    /**
     * Sets a custom adapter for the groups list.
     * Note: This replaces the internal adapter. Use with caution.
     *
     * @param adapter The custom GroupsAdapter to use
     */
    fun setAdapter(adapter: GroupsAdapter) {
        binding.recyclerviewGroupsList.adapter = adapter
    }

    /**
     * Gets the current selection mode.
     *
     * @return The current SelectionMode
     */
    fun getSelectionMode(): UIKitConstants.SelectionMode = selectionMode

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope?.cancel()
        searchJob?.cancel()
    }
}
