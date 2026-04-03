package com.cometchat.uikit.kotlin.presentation.stickerkeyboard.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.factory.CometChatStickerKeyboardViewModelFactory
import com.cometchat.uikit.core.state.StickerKeyboardUIState
import com.cometchat.uikit.core.viewmodel.CometChatStickerKeyboardViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerFrameLayout
import com.cometchat.uikit.kotlin.presentation.stickerkeyboard.adapter.StickerPagerAdapter
import com.cometchat.uikit.kotlin.presentation.stickerkeyboard.adapter.StickerTabAdapter
import com.cometchat.uikit.kotlin.presentation.stickerkeyboard.listener.StickerClickListener
import com.cometchat.uikit.kotlin.presentation.stickerkeyboard.style.CometChatStickerKeyboardStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * CometChatStickerKeyboard is a View-based component for displaying and selecting stickers.
 *
 * This component does NOT require a FragmentActivity, allowing it to be used in
 * Dialog, BottomSheet, or any embedded view context.
 *
 * Features:
 * - ViewPager2 for swipeable sticker set pages (NO Fragment dependency)
 * - Horizontal tab bar for quick navigation between sticker sets
 * - Loading, empty, and error states with customization
 * - GIF animation support
 * - Style customization via XML attributes or programmatically
 *
 * Usage:
 * ```kotlin
 * val stickerKeyboard = CometChatStickerKeyboard(context)
 * stickerKeyboard.setStickerClickListener { sticker ->
 *     // Handle sticker selection
 * }
 * ```
 */
class CometChatStickerKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatStickerKeyboardStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    // ==================== Views ====================
    private val emptyLayout: LinearLayout
    private val errorLayout: LinearLayout
    private val shimmerFrame: CometChatShimmerFrameLayout
    private val shimmerRecyclerView: RecyclerView
    private val customLayout: LinearLayout
    private val stickersView: LinearLayout
    private val viewPager: ViewPager2
    private val separator: View
    private val tabBar: RecyclerView
    private val tvEmptyTitle: TextView
    private val tvEmptySubtitle: TextView
    private val tvErrorTitle: TextView
    private val retryButton: MaterialButton

    // ==================== Adapters ====================
    private val pagerAdapter: StickerPagerAdapter
    private val tabAdapter: StickerTabAdapter

    // ==================== ViewModel ====================
    private var viewModel: CometChatStickerKeyboardViewModel? = null

    // ==================== Callbacks ====================
    private var stickerClickListener: StickerClickListener? = null
    private var onError: ((CometChatException) -> Unit)? = null

    // ==================== Custom Views ====================
    private var customLoadingView: View? = null
    private var customEmptyView: View? = null
    private var customErrorView: View? = null

    // ==================== Style ====================
    private var style: CometChatStickerKeyboardStyle = CometChatStickerKeyboardStyle.default(context)

    companion object {
        private const val TAG = "CometChatStickerKeyboard"
    }

    init {
        // Inflate layout
        LayoutInflater.from(context).inflate(R.layout.cometchat_sticker_keyboard, this, true)

        // Find views
        emptyLayout = findViewById(R.id.empty_sticker_layout)
        errorLayout = findViewById(R.id.error_sticker_layout)
        shimmerFrame = findViewById(R.id.shimmer_effect_frame)
        shimmerRecyclerView = findViewById(R.id.rv_shimmer_effect)
        customLayout = findViewById(R.id.sticker_custom_layout)
        stickersView = findViewById(R.id.stickers_view)
        viewPager = findViewById(R.id.view_pager)
        separator = findViewById(R.id.separator)
        tabBar = findViewById(R.id.rv_tab_bar)
        tvEmptyTitle = findViewById(R.id.tv_empty_sticker_title)
        tvEmptySubtitle = findViewById(R.id.tv_empty_sticker_subtitle)
        tvErrorTitle = findViewById(R.id.tv_error_sticker_title)
        retryButton = findViewById(R.id.retry_btn)

        // Initialize adapters
        pagerAdapter = StickerPagerAdapter { sticker ->
            stickerClickListener?.onStickerClick(sticker)
            viewModel?.onStickerClicked(sticker)
        }
        tabAdapter = StickerTabAdapter { index ->
            viewModel?.selectStickerSet(index)
        }

        // Setup ViewPager2
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel?.selectStickerSet(position)
            }
        })

        // Setup tab bar
        tabBar.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        tabBar.adapter = tabAdapter

        // Setup retry button
        retryButton.setOnClickListener {
            viewModel?.retry()
        }

        // Apply XML attributes
        attrs?.let { applyAttributes(it, defStyleAttr) }

        // Apply default style
        applyStyle(style)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow: Called")
        initializeViewModel()
    }

    private fun initializeViewModel() {
        Log.d(TAG, "initializeViewModel: Starting, viewModel=$viewModel")
        if (viewModel != null) {
            Log.d(TAG, "initializeViewModel: ViewModel already exists, skipping")
            return
        }

        val lifecycleOwner = findViewTreeLifecycleOwner()
        Log.d(TAG, "initializeViewModel: findViewTreeLifecycleOwner returned $lifecycleOwner")
        
        if (lifecycleOwner == null) {
            Log.e(TAG, "initializeViewModel: No LifecycleOwner found! ViewModel cannot be initialized.")
            return
        }

        // Create ViewModel
        viewModel = CometChatStickerKeyboardViewModelFactory().create(
            CometChatStickerKeyboardViewModel::class.java
        )
        Log.d(TAG, "initializeViewModel: ViewModel created successfully")

        // Observe state changes
        observeViewModel(lifecycleOwner)
    }

    private fun observeViewModel(lifecycleOwner: LifecycleOwner) {
        val vm = viewModel ?: return

        lifecycleOwner.lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                handleUIState(state)
            }
        }

        lifecycleOwner.lifecycleScope.launch {
            vm.stickerSets.collectLatest { sets ->
                pagerAdapter.submitList(sets)
                tabAdapter.submitList(sets)
            }
        }

        lifecycleOwner.lifecycleScope.launch {
            vm.selectedSetIndex.collectLatest { index ->
                if (viewPager.currentItem != index) {
                    viewPager.setCurrentItem(index, true)
                }
                tabAdapter.setSelectedIndex(index)
            }
        }
    }

    private fun handleUIState(state: StickerKeyboardUIState) {
        Log.d(TAG, "handleUIState: state=$state")
        when (state) {
            is StickerKeyboardUIState.Loading -> showLoadingState()
            is StickerKeyboardUIState.Content -> showContentState()
            is StickerKeyboardUIState.Empty -> showEmptyState()
            is StickerKeyboardUIState.Error -> {
                showErrorState()
                onError?.invoke(state.exception)
            }
        }
    }

    // ==================== State Display Methods ====================

    private fun showLoadingState() {
        Log.d(TAG, "showLoadingState: Showing loading state")
        hideAllStates()
        if (customLoadingView != null) {
            customLayout.removeAllViews()
            customLayout.addView(customLoadingView)
            customLayout.visibility = View.VISIBLE
        } else {
            setupShimmer()
            shimmerFrame.visibility = View.VISIBLE
            shimmerFrame.startShimmer()
        }
    }

    private fun showContentState() {
        Log.d(TAG, "showContentState: Showing content state")
        hideAllStates()
        stickersView.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        Log.d(TAG, "showEmptyState: Showing empty state")
        hideAllStates()
        if (customEmptyView != null) {
            customLayout.removeAllViews()
            customLayout.addView(customEmptyView)
            customLayout.visibility = View.VISIBLE
        } else {
            emptyLayout.visibility = View.VISIBLE
        }
    }

    private fun showErrorState() {
        Log.d(TAG, "showErrorState: Showing error state")
        hideAllStates()
        if (customErrorView != null) {
            customLayout.removeAllViews()
            customLayout.addView(customErrorView)
            customLayout.visibility = View.VISIBLE
        } else {
            errorLayout.visibility = View.VISIBLE
        }
    }

    private fun hideAllStates() {
        shimmerFrame.stopShimmer()
        shimmerFrame.visibility = View.GONE
        emptyLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
        customLayout.visibility = View.GONE
        stickersView.visibility = View.GONE
    }

    private fun setupShimmer() {
        if (shimmerRecyclerView.adapter == null) {
            shimmerRecyclerView.layoutManager = GridLayoutManager(context, 4)
            shimmerRecyclerView.adapter = CometChatShimmerAdapter(
                16, // Show 16 shimmer items (4x4 grid)
                R.layout.shimmer_cometchat_sticker_grid_item
            )
        }
    }

    // ==================== XML Attribute Handling ====================

    private fun applyAttributes(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CometChatStickerKeyboard,
            defStyleAttr,
            0
        )
        style = CometChatStickerKeyboardStyle.fromTypedArray(context, typedArray)
        // Note: typedArray is recycled inside fromTypedArray
    }

    // ==================== Style Application ====================

    private fun applyStyle(style: CometChatStickerKeyboardStyle) {
        this.style = style

        // Apply background color
        setCardBackgroundColor(style.backgroundColor)

        // Apply separator color
        separator.setBackgroundColor(style.separatorColor)

        // Apply tab active indicator color
        tabAdapter.setActiveIndicatorColor(style.tabActiveIndicatorColor)

        // Apply empty state styling
        tvEmptyTitle.setTextColor(style.emptyStateTitleTextColor)
        if (style.emptyStateTitleTextAppearance != 0) {
            tvEmptyTitle.setTextAppearance(style.emptyStateTitleTextAppearance)
        }
        tvEmptySubtitle.setTextColor(style.emptyStateSubtitleTextColor)
        if (style.emptyStateSubtitleTextAppearance != 0) {
            tvEmptySubtitle.setTextAppearance(style.emptyStateSubtitleTextAppearance)
        }

        // Apply error state styling
        tvErrorTitle.setTextColor(style.errorStateTextColor)
        if (style.errorStateTextAppearance != 0) {
            tvErrorTitle.setTextAppearance(style.errorStateTextAppearance)
        }
    }

    // ==================== Public API ====================

    /**
     * Sets the listener for sticker click events.
     *
     * @param listener The listener to receive sticker click callbacks
     */
    fun setStickerClickListener(listener: StickerClickListener?) {
        this.stickerClickListener = listener
    }

    /**
     * Sets the listener for sticker click events using a lambda.
     *
     * @param listener Lambda invoked when a sticker is clicked
     */
    fun setStickerClickListener(listener: (Sticker) -> Unit) {
        this.stickerClickListener = StickerClickListener { sticker -> listener(sticker) }
    }

    /**
     * Sets the error callback.
     *
     * @param callback Lambda invoked when an error occurs
     */
    fun setOnError(callback: ((CometChatException) -> Unit)?) {
        this.onError = callback
    }

    /**
     * Sets a custom loading state view.
     *
     * @param view The custom view to display during loading
     */
    fun setLoadingStateView(view: View?) {
        this.customLoadingView = view
    }

    /**
     * Sets a custom loading state view from a layout resource.
     *
     * @param layoutRes The layout resource ID for the custom loading view
     */
    fun setLoadingStateView(@LayoutRes layoutRes: Int) {
        this.customLoadingView = LayoutInflater.from(context).inflate(layoutRes, customLayout, false)
    }

    /**
     * Sets a custom empty state view.
     *
     * @param view The custom view to display when no stickers are available
     */
    fun setEmptyStateView(view: View?) {
        this.customEmptyView = view
    }

    /**
     * Sets a custom empty state view from a layout resource.
     *
     * @param layoutRes The layout resource ID for the custom empty view
     */
    fun setEmptyStateView(@LayoutRes layoutRes: Int) {
        this.customEmptyView = LayoutInflater.from(context).inflate(layoutRes, customLayout, false)
    }

    /**
     * Sets a custom error state view.
     *
     * @param view The custom view to display when an error occurs
     */
    fun setErrorStateView(view: View?) {
        this.customErrorView = view
    }

    /**
     * Sets a custom error state view from a layout resource.
     *
     * @param layoutRes The layout resource ID for the custom error view
     */
    fun setErrorStateView(@LayoutRes layoutRes: Int) {
        this.customErrorView = LayoutInflater.from(context).inflate(layoutRes, customLayout, false)
    }

    /**
     * Sets the style for the sticker keyboard.
     *
     * @param style The style configuration to apply
     */
    fun setStyle(style: CometChatStickerKeyboardStyle) {
        applyStyle(style)
    }

    /**
     * Gets the current style configuration.
     *
     * @return The current style
     */
    fun getStyle(): CometChatStickerKeyboardStyle = style

    // ==================== Individual Style Setters ====================

    /**
     * Sets the background color of the sticker keyboard.
     */
    fun setKeyboardBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        setCardBackgroundColor(color)
    }

    /**
     * Sets the separator color.
     */
    fun setSeparatorColor(@ColorInt color: Int) {
        style = style.copy(separatorColor = color)
        separator.setBackgroundColor(color)
    }

    /**
     * Sets the active tab indicator color.
     */
    fun setTabActiveIndicatorColor(@ColorInt color: Int) {
        style = style.copy(tabActiveIndicatorColor = color)
        tabAdapter.setActiveIndicatorColor(color)
    }

    /**
     * Sets the empty state title text color.
     */
    fun setEmptyStateTitleTextColor(@ColorInt color: Int) {
        style = style.copy(emptyStateTitleTextColor = color)
        tvEmptyTitle.setTextColor(color)
    }

    /**
     * Sets the empty state title text appearance.
     */
    fun setEmptyStateTitleTextAppearance(@androidx.annotation.StyleRes appearance: Int) {
        style = style.copy(emptyStateTitleTextAppearance = appearance)
        tvEmptyTitle.setTextAppearance(appearance)
    }

    /**
     * Sets the empty state subtitle text color.
     */
    fun setEmptyStateSubtitleTextColor(@ColorInt color: Int) {
        style = style.copy(emptyStateSubtitleTextColor = color)
        tvEmptySubtitle.setTextColor(color)
    }

    /**
     * Sets the empty state subtitle text appearance.
     */
    fun setEmptyStateSubtitleTextAppearance(@androidx.annotation.StyleRes appearance: Int) {
        style = style.copy(emptyStateSubtitleTextAppearance = appearance)
        tvEmptySubtitle.setTextAppearance(appearance)
    }

    /**
     * Sets the error state text color.
     */
    fun setErrorStateTextColor(@ColorInt color: Int) {
        style = style.copy(errorStateTextColor = color)
        tvErrorTitle.setTextColor(color)
    }

    /**
     * Sets the error state text appearance.
     */
    fun setErrorStateTextAppearance(@androidx.annotation.StyleRes appearance: Int) {
        style = style.copy(errorStateTextAppearance = appearance)
        tvErrorTitle.setTextAppearance(appearance)
    }

    /**
     * Sets the empty state title text.
     */
    fun setEmptyStateTitleText(text: String) {
        tvEmptyTitle.text = text
    }

    /**
     * Sets the empty state subtitle text.
     */
    fun setEmptyStateSubtitleText(text: String) {
        tvEmptySubtitle.text = text
    }

    /**
     * Sets the error state text.
     */
    fun setErrorStateText(text: String) {
        tvErrorTitle.text = text
    }

    // ==================== Getters ====================

    /**
     * Gets the ViewPager2 adapter.
     */
    fun getPagerAdapter(): StickerPagerAdapter = pagerAdapter

    /**
     * Gets the tab adapter.
     */
    fun getTabAdapter(): StickerTabAdapter = tabAdapter

    /**
     * Gets the sticker click listener.
     */
    fun getStickerClickListener(): StickerClickListener? = stickerClickListener

    /**
     * Gets the custom loading view.
     */
    fun getCustomLoadingView(): View? = customLoadingView

    /**
     * Gets the custom empty view.
     */
    fun getCustomEmptyView(): View? = customEmptyView

    /**
     * Gets the custom error view.
     */
    fun getCustomErrorView(): View? = customErrorView
}