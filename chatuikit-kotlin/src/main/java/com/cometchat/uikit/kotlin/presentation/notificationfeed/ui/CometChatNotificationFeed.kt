package com.cometchat.uikit.kotlin.presentation.notificationfeed.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cometchat.chat.core.NotificationCategoriesRequest
import com.cometchat.chat.core.NotificationFeedRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.NotificationFeedItem
import com.cometchat.uikit.core.factory.CometChatNotificationFeedViewModelFactory
import com.cometchat.uikit.core.state.NotificationFeedUIState
import com.cometchat.uikit.core.viewmodel.CometChatNotificationFeedViewModel
import com.cometchat.uikit.kotlin.presentation.notificationfeed.style.CometChatNotificationFeedStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * CometChatNotificationFeed is a custom Android View that displays a notification feed
 * with category-based filtering, timestamp grouping, rich card rendering,
 * real-time updates, and engagement reporting.
 *
 * This component uses the shared CometChatNotificationFeedViewModel from chatuikit-core,
 * ensuring consistent business logic with the Jetpack Compose version.
 *
 * Usage:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.notificationfeed.ui.CometChatNotificationFeed
 *     android:id="@+id/notificationFeed"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 * ```
 */
class CometChatNotificationFeed @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // ViewModel
    private var viewModel: CometChatNotificationFeedViewModel? = null

    // Views
    private lateinit var toolbarTitle: TextView
    private lateinit var backButton: ImageButton
    private lateinit var filterChipsRecyclerView: RecyclerView
    private lateinit var feedRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingView: View
    private lateinit var emptyView: View
    private lateinit var errorView: View
    private lateinit var retryButton: View

    // Adapters
    private var feedAdapter: NotificationFeedAdapter? = null
    private var filterChipsAdapter: NotificationFeedFilterChipsAdapter? = null

    // Configuration
    private var title: String = "Notifications"
    private var showHeader: Boolean = true
    private var showBackButton: Boolean = false
    private var showFilterChips: Boolean = true
    private var style: CometChatNotificationFeedStyle = CometChatNotificationFeedStyle.default()

    // Callbacks
    var onItemClick: ((NotificationFeedItem) -> Unit)? = null
    var onActionClick: ((NotificationFeedItem, Map<String, Any>) -> Unit)? = null
    var onError: ((CometChatException) -> Unit)? = null
    var onBackPress: (() -> Unit)? = null

    // Request builders
    private var feedRequestBuilder: NotificationFeedRequest.NotificationFeedRequestBuilder? = null
    private var categoriesRequestBuilder: NotificationCategoriesRequest.NotificationCategoriesRequestBuilder? = null

    // Coroutine scope for collecting flows
    private var coroutineScope: CoroutineScope? = null

    init {
        initView()
    }

    private fun initView() {
        // Inflate a programmatic layout since we don't have XML layout files yet
        // In production, this would use ViewBinding with a layout XML
        setupProgrammaticLayout()
    }

    private fun setupProgrammaticLayout() {
        val density = resources.displayMetrics.density

        // Create views programmatically
        // Header
        val headerLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        backButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundResource(android.R.color.transparent)
            visibility = if (showBackButton) View.VISIBLE else View.GONE
            setOnClickListener { onBackPress?.invoke() }
            contentDescription = "Back"
        }
        headerLayout.addView(backButton, android.widget.LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
        ))

        toolbarTitle = TextView(context).apply {
            text = title
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(CometChatTheme.getTextColorPrimary(context))
            if (showBackButton) {
                setPadding((8 * density).toInt(), 0, 0, 0)
            }
        }
        headerLayout.addView(toolbarTitle, android.widget.LinearLayout.LayoutParams(
            0, LayoutParams.WRAP_CONTENT, 1f
        ))

        // Filter chips RecyclerView — matches Compose: padding(horizontal = 16.dp, vertical = 8.dp)
        filterChipsRecyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding((16 * density).toInt(), (8 * density).toInt(), (16 * density).toInt(), (8 * density).toInt())
            clipToPadding = false
        }

        // Feed RecyclerView
        feedRecyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // SwipeRefreshLayout wrapping feed
        swipeRefreshLayout = SwipeRefreshLayout(context).apply {
            addView(feedRecyclerView)
            setOnRefreshListener { viewModel?.refresh() }
        }

        // Loading view — matches Figma: spinner with track + "Loading..." text
        loadingView = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            gravity = android.view.Gravity.CENTER
            visibility = View.GONE

            val density = resources.displayMetrics.density
            val sizePx = (40 * density).toInt()

            // Material CircularProgressIndicator with trackColor — matches Compose exactly
            val progressIndicator = com.google.android.material.progressindicator.CircularProgressIndicator(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(sizePx, sizePx)
                isIndeterminate = true
                trackThickness = (4 * density).toInt()
                setIndicatorColor(CometChatTheme.getPrimaryColor(context))
                trackColor = CometChatTheme.getStrokeColorDefault(context)
                indicatorSize = sizePx
            }
            addView(progressIndicator)

            val loadingText = android.widget.TextView(context).apply {
                text = "Loading..."
                textSize = 14f
                setTextColor(CometChatTheme.getTextColorSecondary(context))
                gravity = android.view.Gravity.CENTER
                setPadding(0, (12 * density).toInt(), 0, 0)
            }
            addView(loadingText)
        }

        // Empty view — matches Compose: icon + "Nothing here yet" + subtitle
        emptyView = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            visibility = View.GONE
            setBackgroundColor(CometChatTheme.getBackgroundColor2(context))

            // Empty icon
            val iconSize = (120 * density).toInt()
            val emptyIcon = android.widget.ImageView(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(iconSize, iconSize)
                setImageResource(com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_empty_box)
                setColorFilter(CometChatTheme.getNeutralColor400(context), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            addView(emptyIcon)

            // Title
            val titleText = TextView(context).apply {
                text = "Nothing here yet"
                textSize = 20f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(CometChatTheme.getTextColorPrimary(context))
                gravity = android.view.Gravity.CENTER
                setPadding(0, (16 * density).toInt(), 0, 0)
            }
            addView(titleText)

            // Subtitle
            val subtitleText = TextView(context).apply {
                text = "New activity will appear here when available."
                textSize = 14f
                setTextColor(CometChatTheme.getTextColorTertiary(context))
                gravity = android.view.Gravity.CENTER
                setPadding((32 * density).toInt(), (8 * density).toInt(), (32 * density).toInt(), 0)
            }
            addView(subtitleText)
        }

        // Error view with retry — matches Compose: icon + "Oops!" + subtitle + Retry button
        val errorLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            visibility = View.GONE
            setBackgroundColor(CometChatTheme.getBackgroundColor2(context))
        }

        // Error icon
        val errorIconSize = (120 * density).toInt()
        val errorIcon = android.widget.ImageView(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(errorIconSize, errorIconSize)
            setImageResource(com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_error)
            setColorFilter(CometChatTheme.getNeutralColor400(context), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        errorLayout.addView(errorIcon)

        // Error title
        val errorTitle = TextView(context).apply {
            text = "Oops!"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(CometChatTheme.getTextColorPrimary(context))
            gravity = android.view.Gravity.CENTER
            setPadding(0, (16 * density).toInt(), 0, 0)
        }
        errorLayout.addView(errorTitle)

        // Error subtitle
        val errorSubtitle = TextView(context).apply {
            text = "Looks like something went wrong.\nPlease try again."
            textSize = 14f
            setTextColor(CometChatTheme.getTextColorTertiary(context))
            gravity = android.view.Gravity.CENTER
            setPadding((32 * density).toInt(), (8 * density).toInt(), (32 * density).toInt(), 0)
        }
        errorLayout.addView(errorSubtitle)

        // Retry button — primary bg, white text, rounded
        retryButton = TextView(context).apply {
            text = "Retry"
            textSize = 16f
            setTextColor(CometChatTheme.getTextColorWhite(context))
            gravity = android.view.Gravity.CENTER
            setPadding((32 * density).toInt(), (12 * density).toInt(), (32 * density).toInt(), (12 * density).toInt())
            val btnBg = android.graphics.drawable.GradientDrawable().apply {
                setColor(CometChatTheme.getPrimaryColor(context))
                cornerRadius = 8 * density
            }
            background = btnBg
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (24 * density).toInt()
            }
            setOnClickListener { viewModel?.fetchInitialItems() }
        }
        errorLayout.addView(retryButton)
        errorView = errorLayout

        // Main layout
        val mainLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setBackgroundColor(CometChatTheme.getBackgroundColor1(context))
        }

        if (showHeader) {
            mainLayout.addView(headerLayout)
            // Divider after header — matches Compose: HorizontalDivider(color = headerBorderColor, 1.dp)
            val divider = View(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, (1 * density).toInt()
                )
                setBackgroundColor(CometChatTheme.getStrokeColorDefault(context))
            }
            mainLayout.addView(divider)
        }

        // Content wrapper below header with content background
        val contentLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, 0, 1f
            )
            setBackgroundColor(CometChatTheme.getBackgroundColor2(context))
        }

        if (showFilterChips) {
            contentLayout.addView(filterChipsRecyclerView)
        }

        // Content frame (holds swipeRefresh, loading, empty, error)
        val contentFrame = FrameLayout(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        contentFrame.addView(swipeRefreshLayout)
        contentFrame.addView(loadingView)
        contentFrame.addView(emptyView)
        contentFrame.addView(errorView)

        contentLayout.addView(contentFrame)
        mainLayout.addView(contentLayout)

        addView(mainLayout)

        // Setup adapters
        setupAdapters()
    }

    private fun setupAdapters() {
        feedAdapter = NotificationFeedAdapter(
            style = style,
            onItemClick = { item ->
                onItemClick?.invoke(item)
            },
            onActionClick = { item, action ->
                onActionClick?.invoke(item, action)
            },
            onItemVisible = { item -> viewModel?.onItemBecameVisible(item) },
            onItemHidden = { item -> viewModel?.onItemBecameHidden(item) }
        )
        feedRecyclerView.adapter = feedAdapter

        filterChipsAdapter = NotificationFeedFilterChipsAdapter(
            style = style,
            onChipClick = { chipId ->
                val categoryId = if (chipId == "all") null else chipId
                viewModel?.switchCategory(categoryId)
            }
        )
        filterChipsRecyclerView.adapter = filterChipsAdapter

        // Infinite scroll
        feedRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (lastVisibleItem >= totalItemCount - 3) {
                    viewModel?.fetchNextPage()
                }
            }
        })
    }

    /**
     * Initialize the component with a ViewModel.
     * Must be called from an Activity or Fragment that implements LifecycleOwner and ViewModelStoreOwner.
     */
    fun init(owner: ViewModelStoreOwner) {
        val factory = CometChatNotificationFeedViewModelFactory(
            feedRequestBuilder = feedRequestBuilder,
            categoriesRequestBuilder = categoriesRequestBuilder
        )
        viewModel = ViewModelProvider(owner, factory)[CometChatNotificationFeedViewModel::class.java]

        // Start observing
        if (owner is LifecycleOwner) {
            observeViewModel(owner)
        }
    }

    private fun observeViewModel(lifecycleOwner: LifecycleOwner) {
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        coroutineScope?.launch {
            viewModel?.uiState?.collectLatest { state ->
                when (state) {
                    is NotificationFeedUIState.Loading -> showLoadingState()
                    is NotificationFeedUIState.Empty -> showEmptyState()
                    is NotificationFeedUIState.Error -> {
                        showErrorState()
                        onError?.invoke(state.exception)
                    }
                    is NotificationFeedUIState.Content -> showContentState(state)
                }
            }
        }

        coroutineScope?.launch {
            viewModel?.filterChips?.collectLatest { chips ->
                filterChipsAdapter?.submitList(chips)
            }
        }

        coroutineScope?.launch {
            viewModel?.activeCategory?.collectLatest { category ->
                feedAdapter?.setActiveCategory(category)
            }
        }

        coroutineScope?.launch {
            viewModel?.isRefreshing?.collectLatest { refreshing ->
                swipeRefreshLayout.isRefreshing = refreshing
            }
        }

        coroutineScope?.launch {
            viewModel?.isLoadingMore?.collectLatest { loading ->
                if (loading) {
                    feedAdapter?.showPaginationLoading()
                } else if (viewModel?.isPaginationError?.value != true) {
                    feedAdapter?.hidePaginationFooter()
                }
            }
        }

        coroutineScope?.launch {
            viewModel?.isPaginationError?.collectLatest { error ->
                if (error) {
                    feedAdapter?.showPaginationError { viewModel?.fetchNextPage() }
                } else {
                    feedAdapter?.hidePaginationFooter()
                }
            }
        }
    }

    private fun showLoadingState() {
        loadingView.visibility = View.VISIBLE
        swipeRefreshLayout.visibility = View.GONE
        emptyView.visibility = View.GONE
        errorView.visibility = View.GONE
    }

    private fun showEmptyState() {
        loadingView.visibility = View.GONE
        swipeRefreshLayout.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
    }

    private fun showErrorState() {
        loadingView.visibility = View.GONE
        swipeRefreshLayout.visibility = View.GONE
        emptyView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
    }

    private fun showContentState(state: NotificationFeedUIState.Content) {
        loadingView.visibility = View.GONE
        swipeRefreshLayout.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        errorView.visibility = View.GONE
        feedAdapter?.submitGroupedItems(state.groupedItems)
    }

    // region Public Configuration Methods

    fun setTitle(title: String) {
        this.title = title
        toolbarTitle.text = title
    }

    fun setShowHeader(show: Boolean) {
        this.showHeader = show
    }

    fun setShowBackButton(show: Boolean) {
        this.showBackButton = show
        backButton.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setShowFilterChips(show: Boolean) {
        this.showFilterChips = show
        filterChipsRecyclerView.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setStyle(style: CometChatNotificationFeedStyle) {
        this.style = style
        applyStyle()
    }

    fun setFeedRequestBuilder(builder: NotificationFeedRequest.NotificationFeedRequestBuilder) {
        this.feedRequestBuilder = builder
    }

    fun setCategoriesRequestBuilder(builder: NotificationCategoriesRequest.NotificationCategoriesRequestBuilder) {
        this.categoriesRequestBuilder = builder
    }

    private fun applyStyle() {
        if (style.backgroundColor != 0) {
            setBackgroundColor(style.backgroundColor)
        }
        if (style.headerTitleColor != 0) {
            toolbarTitle.setTextColor(style.headerTitleColor)
        }
        feedAdapter?.updateStyle(style)
        filterChipsAdapter?.updateStyle(style)
    }

    // endregion

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope?.cancel()
        coroutineScope = null
    }
}
