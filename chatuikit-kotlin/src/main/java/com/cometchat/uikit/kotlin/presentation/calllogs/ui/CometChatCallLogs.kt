package com.cometchat.uikit.kotlin.presentation.calllogs.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.core.factory.CometChatCallLogsViewModelFactory
import com.cometchat.uikit.core.state.CallLogsUIState
import com.cometchat.uikit.core.viewmodel.CometChatCallLogsViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatCallLogsBinding
import com.cometchat.uikit.kotlin.presentation.calllogs.style.CometChatCallLogsListItemStyle
import com.cometchat.uikit.kotlin.presentation.calllogs.style.CometChatCallLogsStyle
import com.cometchat.uikit.kotlin.presentation.calllogs.utils.CallLogsViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * CometChatCallLogs is a custom Android View that displays a list of call logs
 * with support for real-time updates, custom views, and full styling.
 *
 * This component uses the shared CometChatCallLogsViewModel from chatuikit-core,
 * ensuring consistent business logic with the Jetpack Compose version.
 *
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.calllogs.ui.CometChatCallLogs
 *     android:id="@+id/callLogs"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:cometchatCallLogsStyle="@style/CometChatCallLogsStyle" />
 * ```
 *
 * Usage in Kotlin:
 * ```kotlin
 * val callLogs = CometChatCallLogs(context)
 * callLogs.setOnItemClick { callLog ->
 *     // Handle click
 * }
 * ```
 */
class CometChatCallLogs @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatCallLogsStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatCallLogs::class.java.simpleName
    }

    // View Binding
    private val binding: CometchatCallLogsBinding

    // ViewModel (shared from chatuikit-core)
    private var viewModel: CometChatCallLogsViewModel? = null
    
    // Flag to track if ViewModel was externally provided
    private var isExternalViewModel: Boolean = false

    // Adapter
    private val callLogsAdapter: CallLogsAdapter

    // Lifecycle owner for observing flows
    private var lifecycleOwner: LifecycleOwner? = null

    // Coroutine scope for collecting flows
    private var viewScope: CoroutineScope? = null

    // Callbacks
    private var onItemClick: ((CallLog) -> Unit)? = null
    private var onItemLongClick: ((CallLog) -> Unit)? = null
    private var onCallTypeIconClick: ((CallLog) -> Unit)? = null
    private var onError: ((CometChatException) -> Unit)? = null
    private var onLoad: ((List<CallLog>) -> Unit)? = null
    private var onEmpty: (() -> Unit)? = null
    private var onBackPress: (() -> Unit)? = null

    // Custom views using ViewHolderListener pattern
    private var itemViewListener: CallLogsViewHolderListener? = null
    private var leadingViewListener: CallLogsViewHolderListener? = null
    private var titleViewListener: CallLogsViewHolderListener? = null
    private var subtitleViewListener: CallLogsViewHolderListener? = null
    private var trailingViewListener: CallLogsViewHolderListener? = null

    // Custom state views
    private var customEmptyView: View? = null
    private var customErrorView: View? = null
    private var customLoadingView: View? = null

    // Visibility controls
    private var toolbarVisibility = View.VISIBLE
    private var backIconVisibility = View.GONE
    private var errorStateVisibility = View.VISIBLE
    private var loadingStateVisibility = View.VISIBLE
    private var emptyStateVisibility = View.VISIBLE
    private var separatorVisibility = View.VISIBLE

    // Single style object - will be properly initialized in applyStyleAttributes
    private var style: CometChatCallLogsStyle = CometChatCallLogsStyle()
    
    // Stored style resource ID for refreshStyle
    @StyleRes private var currentStyleResId: Int = 0

    // Date/time formatter callback
    private var dateTimeFormatter: DateTimeFormatterCallback? = null

    // Menu options
    private var options: ((Context, CallLog) -> List<CometChatPopupMenu.MenuItem>)? = null
    private var addOptions: ((Context, CallLog) -> List<CometChatPopupMenu.MenuItem>)? = null
    private val popupMenu: CometChatPopupMenu by lazy { CometChatPopupMenu(context, 0) }

    init {
        // Inflate layout
        binding = CometchatCallLogsBinding.inflate(
            LayoutInflater.from(context), this, true
        )

        // Reset the card view to default values
        Utils.initMaterialCard(this)

        // Initialize adapter
        callLogsAdapter = CallLogsAdapter()

        // Setup RecyclerView
        setupRecyclerView()

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
        binding.recyclerviewCallLogs.layoutManager = layoutManager
        binding.recyclerviewCallLogs.adapter = callLogsAdapter
        setSeparatorVisibility(View.GONE)

        // Disable change animations for smoother updates
        val animator = binding.recyclerviewCallLogs.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        // Add scroll listener for pagination
        binding.recyclerviewCallLogs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel?.fetchCallLogs()
                }
            }
        })
    }

    /**
     * Applies style attributes from XML.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatCallLogs, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatCallLogs_cometchatCallLogsStyle, 0
        )
        typedArray.recycle()
        
        currentStyleResId = styleResId
        
        typedArray = if (styleResId != 0) {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatCallLogs, defStyleAttr, styleResId
            )
        } else {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatCallLogs, defStyleAttr, 0
            )
        }
        
        style = CometChatCallLogsStyle.fromTypedArray(context, typedArray)
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
        binding.toolbar.setShowSeparator(style.showToolbarSeparator)
        if (style.toolbarSeparatorColor != 0) {
            binding.toolbar.setSeparatorColor(style.toolbarSeparatorColor)
        }

        // Empty state
        if (style.emptyStateTitleTextColor != 0) {
            binding.tvEmptyTitle.setTextColor(style.emptyStateTitleTextColor)
        }
        if (style.emptyStateSubtitleTextColor != 0) {
            binding.tvEmptySubtitle.setTextColor(style.emptyStateSubtitleTextColor)
        }
        if (style.emptyStateTitleTextAppearance != 0) {
            binding.tvEmptyTitle.setTextAppearance(style.emptyStateTitleTextAppearance)
        }
        if (style.emptyStateSubtitleTextAppearance != 0) {
            binding.tvEmptySubtitle.setTextAppearance(style.emptyStateSubtitleTextAppearance)
        }
        style.emptyStateIcon?.let { binding.ivEmptyIcon.setImageDrawable(it) }

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
        style.errorStateIcon?.let { binding.ivErrorIcon.setImageDrawable(it) }
        
        // Retry button
        if (style.retryButtonBackgroundColor != 0) {
            binding.btnRetry.setBackgroundColor(style.retryButtonBackgroundColor)
        }
        if (style.retryButtonTextColor != 0) {
            binding.btnRetry.setTextColor(style.retryButtonTextColor)
        }

        // Update adapter item style
        updateAdapterItemStyle()
    }

    /**
     * Updates the adapter's item style.
     */
    private fun updateAdapterItemStyle() {
        android.util.Log.d(TAG, "updateAdapterItemStyle: itemStyle.incomingCallIcon=${style.itemStyle.incomingCallIcon}, outgoingCallIcon=${style.itemStyle.outgoingCallIcon}, missedCallIcon=${style.itemStyle.missedCallIcon}")
        android.util.Log.d(TAG, "updateAdapterItemStyle: itemStyle.audioCallIcon=${style.itemStyle.audioCallIcon}, videoCallIcon=${style.itemStyle.videoCallIcon}")
        callLogsAdapter.setItemStyle(style.itemStyle)
        callLogsAdapter.setHideSeparator(separatorVisibility != View.VISIBLE)
    }

    /**
     * Sets up click listeners for UI elements.
     */
    private fun setupClickListeners() {
        // Adapter item click
        callLogsAdapter.setOnItemClick { view, position, callLog ->
            onItemClick?.invoke(callLog)
        }

        // Adapter item long click - show popup menu if options are set
        callLogsAdapter.setOnItemLongClick { view, position, callLog ->
            onItemLongClick?.invoke(callLog)
            preparePopupMenu(view, callLog)
        }

        // Adapter call type icon click
        callLogsAdapter.setOnCallTypeIconClick { view, position, callLog ->
            onCallTypeIconClick?.invoke(callLog)
        }

        // Toolbar back press
        binding.toolbar.setOnBackPress {
            onBackPress?.invoke()
        }

        // Retry button
        binding.btnRetry.setOnClickListener {
            viewModel?.fetchCallLogs()
        }
    }

    /**
     * Prepares and displays a popup menu for the given call log.
     * If options is set, uses those exclusively. Otherwise uses addOptions if set.
     */
    private fun preparePopupMenu(view: View, callLog: CallLog) {
        val menuItems = buildMenuItems(callLog)
        if (menuItems.isNotEmpty()) {
            popupMenu.setMenuItems(menuItems)
            popupMenu.setOnMenuItemClickListener { id, _ ->
                menuItems.find { it.id == id }?.onClick?.invoke()
                popupMenu.dismiss()
            }
            popupMenu.showAsDropDown(view)
        }
    }

    /**
     * Builds the menu items list based on options or addOptions.
     */
    private fun buildMenuItems(callLog: CallLog): List<CometChatPopupMenu.MenuItem> {
        // If custom options are provided, use them exclusively
        options?.let { return it(context, callLog) }
        
        // Otherwise use addOptions if provided
        return addOptions?.invoke(context, callLog) ?: emptyList()
    }

    /**
     * Initializes the ViewModel and sets up observers.
     */
    private fun initViewModel() {
        lifecycleOwner = Utils.getLifecycleOwner(context)
        if (lifecycleOwner == null) return

        if (!isExternalViewModel && viewModel == null) {
            val factory = CometChatCallLogsViewModelFactory()
            viewModel = ViewModelProvider(
                lifecycleOwner as androidx.lifecycle.ViewModelStoreOwner,
                factory
            )[CometChatCallLogsViewModel::class.java]
        }

        observeViewModel()
    }
    
    /**
     * Sets up observers for the ViewModel's StateFlows and SharedFlows.
     */
    private fun observeViewModel() {
        val vm = viewModel ?: return
        
        viewScope?.cancel()
        viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Observe call logs
        viewScope?.launch {
            vm.callLogs.collectLatest { callLogs ->
                callLogsAdapter.setList(callLogs)
                onLoad?.invoke(callLogs)
            }
        }

        // Observe UI state
        viewScope?.launch {
            vm.uiState.collectLatest { state ->
                handleStateChange(state)
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
    private fun handleStateChange(state: CallLogsUIState) {
        hideAllStates()

        when (state) {
            is CallLogsUIState.Loading -> handleLoadingState()
            is CallLogsUIState.Content -> handleContentState()
            is CallLogsUIState.Empty -> handleEmptyState()
            is CallLogsUIState.Error -> handleErrorState(state.exception)
        }
    }

    /**
     * Hides all state views.
     */
    private fun hideAllStates() {
        binding.recyclerviewCallLogs.visibility = View.GONE
        binding.emptyStateView.visibility = View.GONE
        binding.errorStateView.visibility = View.GONE
        binding.shimmerParentLayout.visibility = View.GONE
        binding.customLayout.visibility = View.GONE
    }

    /**
     * Handles loading state.
     */
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
        announceForAccessibility(context.getString(R.string.cometchat_loading_call_logs))
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
        binding.recyclerviewCallLogs.visibility = View.VISIBLE
    }

    /**
     * Handles empty state.
     */
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
        announceForAccessibility(context.getString(R.string.cometchat_no_call_logs))
        onEmpty?.invoke()
    }

    /**
     * Handles error state.
     */
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
        announceForAccessibility(context.getString(R.string.cometchat_error_loading_call_logs))
        exception?.let { onError?.invoke(it) }
    }

    /**
     * Scrolls to the top of the list.
     */
    private fun scrollToTop() {
        val layoutManager = binding.recyclerviewCallLogs.layoutManager as? LinearLayoutManager
        val firstVisiblePosition = layoutManager?.findFirstVisibleItemPosition() ?: 0
        
        if (firstVisiblePosition < 3) {
            binding.recyclerviewCallLogs.scrollToPosition(0)
        }
    }

    // ==================== Public API Methods ====================

    /**
     * Sets the item click callback.
     */
    fun setOnItemClick(callback: (CallLog) -> Unit) {
        onItemClick = callback
    }

    /**
     * Sets the item long click callback.
     */
    fun setOnItemLongClick(callback: (CallLog) -> Unit) {
        onItemLongClick = callback
    }

    /**
     * Sets the call type icon click callback.
     */
    fun setOnCallTypeIconClick(callback: (CallLog) -> Unit) {
        onCallTypeIconClick = callback
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
    fun setOnLoad(callback: (List<CallLog>) -> Unit) {
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
     * Sets an external ViewModel instance.
     */
    fun setViewModel(viewModel: CometChatCallLogsViewModel?) {
        this.viewModel = viewModel
        this.isExternalViewModel = viewModel != null
        
        if (lifecycleOwner != null) {
            if (viewModel != null) {
                observeViewModel()
            } else {
                isExternalViewModel = false
                initViewModel()
            }
        }
    }

    /**
     * Returns the current ViewModel instance.
     */
    fun getViewModel(): CometChatCallLogsViewModel? = viewModel

    /**
     * Sets a custom CallLogRequestBuilder for fetching call logs.
     * This allows customization of the call logs query parameters.
     */
    fun setCallLogRequestBuilder(builder: com.cometchat.calls.core.CallLogRequest.CallLogRequestBuilder) {
        viewModel?.setCallLogRequestBuilder(builder)
    }

    /**
     * Sets custom item view listener.
     */
    fun setItemView(listener: CallLogsViewHolderListener?) {
        itemViewListener = listener
        callLogsAdapter.setItemView(listener)
    }

    /**
     * Sets custom leading view listener.
     */
    fun setLeadingView(listener: CallLogsViewHolderListener?) {
        leadingViewListener = listener
        callLogsAdapter.setLeadingView(listener)
    }

    /**
     * Sets custom title view listener.
     */
    fun setTitleView(listener: CallLogsViewHolderListener?) {
        titleViewListener = listener
        callLogsAdapter.setTitleView(listener)
    }

    /**
     * Sets custom subtitle view listener.
     */
    fun setSubtitleView(listener: CallLogsViewHolderListener?) {
        subtitleViewListener = listener
        callLogsAdapter.setSubtitleView(listener)
    }

    /**
     * Sets custom trailing view listener.
     */
    fun setTrailingView(listener: CallLogsViewHolderListener?) {
        trailingViewListener = listener
        callLogsAdapter.setTrailingView(listener)
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
     * Sets toolbar visibility.
     */
    fun setToolbarVisibility(visibility: Int) {
        toolbarVisibility = visibility
        binding.toolbar.visibility = visibility
    }

    /**
     * Sets title visibility.
     * When hidden, the title text will be empty but the toolbar remains visible.
     */
    fun setTitleVisibility(visibility: Int) {
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            binding.toolbar.setTitle("")
        }
    }

    /**
     * Sets back icon visibility.
     */
    fun setBackIconVisibility(visibility: Int) {
        backIconVisibility = visibility
        binding.toolbar.setBackIconVisibility(visibility)
    }

    /**
     * Sets separator visibility.
     */
    fun setSeparatorVisibility(visibility: Int) {
        separatorVisibility = visibility
        callLogsAdapter.setHideSeparator(visibility != View.VISIBLE)
    }

    /**
     * Sets toolbar separator visibility.
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
        loadingStateVisibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Hides or shows the empty state.
     * When hidden, the empty state view will not be displayed when there are no call logs.
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
     */
    fun setStyle(style: CometChatCallLogsStyle) {
        // Ensure itemStyle has proper icon resources
        this.style = style.withDefaultItemStyle(context)
        applyStyle()
    }
    
    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            currentStyleResId = styleRes
            val typedArray = context.theme.obtainStyledAttributes(styleRes, R.styleable.CometChatCallLogs)
            style = CometChatCallLogsStyle.fromTypedArray(context, typedArray)
            applyStyle()
        }
    }
    
    /**
     * Gets the current style.
     */
    fun getStyle(): CometChatCallLogsStyle = style

    /**
     * Refreshes the component with current theme colors.
     */
    fun refreshStyle() {
        val typedArray = if (currentStyleResId != 0) {
            context.theme.obtainStyledAttributes(
                null, R.styleable.CometChatCallLogs, R.attr.cometchatCallLogsStyle, currentStyleResId
            )
        } else {
            context.theme.obtainStyledAttributes(
                null, R.styleable.CometChatCallLogs, R.attr.cometchatCallLogsStyle, 0
            )
        }
        style = CometChatCallLogsStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Sets a custom date/time formatter callback for formatting dates in the call logs list.
     * This allows customization of how dates are displayed (today, yesterday, last week, etc.).
     *
     * @param formatter The DateTimeFormatterCallback for custom formatting, or null to use default.
     */
    fun setDateTimeFormatter(formatter: DateTimeFormatterCallback?) {
        dateTimeFormatter = formatter
        callLogsAdapter.setDateTimeFormatter(formatter)
    }

    /**
     * Gets the current date/time formatter callback.
     *
     * @return The current DateTimeFormatterCallback, or null if using default.
     */
    fun getDateTimeFormatter(): DateTimeFormatterCallback? = dateTimeFormatter

    /**
     * Triggers a refresh of the call logs by clearing existing data and fetching fresh.
     * This ensures the call logs are updated with the latest data from the server.
     */
    fun refreshCallLogs() {
        viewModel?.refreshCallLogs()
    }

    /**
     * Sets menu options that completely replace any default options.
     * When set, only these options will be shown in the long-press popup menu.
     *
     * @param options A function that takes Context and CallLog and returns a list of menu items,
     *                or null to clear custom options.
     */
    fun setOptions(options: ((Context, CallLog) -> List<CometChatPopupMenu.MenuItem>)?) {
        this.options = options
    }

    /**
     * Gets the current menu options function.
     *
     * @return The current options function, or null if not set.
     */
    fun getOptions(): ((Context, CallLog) -> List<CometChatPopupMenu.MenuItem>)? = options

    /**
     * Sets additional menu options that are added to the default options.
     * These options are only used if setOptions() has not been called.
     *
     * @param addOptions A function that takes Context and CallLog and returns a list of additional menu items,
     *                   or null to clear additional options.
     */
    fun setAddOptions(addOptions: ((Context, CallLog) -> List<CometChatPopupMenu.MenuItem>)?) {
        this.addOptions = addOptions
    }

    /**
     * Gets the current additional menu options function.
     *
     * @return The current addOptions function, or null if not set.
     */
    fun getAddOptions(): ((Context, CallLog) -> List<CometChatPopupMenu.MenuItem>)? = addOptions

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope?.cancel()
        viewScope = null
    }
}
