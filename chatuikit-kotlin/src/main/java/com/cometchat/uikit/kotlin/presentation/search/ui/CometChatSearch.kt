package com.cometchat.uikit.kotlin.presentation.search.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.constants.SearchFilter
import com.cometchat.uikit.core.constants.SearchScope
import com.cometchat.uikit.core.factory.CometChatSearchViewModelFactory
import com.cometchat.uikit.core.state.SearchUIState
import com.cometchat.uikit.core.viewmodel.CometChatSearchViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatSearchBinding
import com.cometchat.uikit.kotlin.presentation.search.adapter.CometChatSearchConversationsAdapter
import com.cometchat.uikit.kotlin.presentation.search.adapter.CometChatSearchMessageListAdapter
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchConversationsViewHolderListener
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchMessagesViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.kotlin.shared.formatters.CometChatRichTextFormatter
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderAdapter
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderDecoration
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * CometChatSearch is a custom Android View that provides search functionality across
 * conversations and messages with support for filter chips, debounced search,
 * pagination, and multiple view states.
 *
 * Features:
 * - Dual search: Searches both Conversations AND Messages simultaneously
 * - Filter chips: Photos, Videos, Documents, Links, Audio, Groups, Unread
 * - Debounced search: 450ms delay to prevent excessive API calls
 * - Search scope configuration: Messages, Conversations, or Both
 * - UID/GUID filtering: Contextual search within specific conversations
 * - Pagination support for both conversations and messages
 * - Sticky date headers for messages
 * - Multiple view states: Initial, Loading, Content, Empty, Error
 *
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.search.ui.CometChatSearch
 *     android:id="@+id/search"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:cometchatSearchStyle="@style/CometChatSearchStyle" />
 * ```
 *
 * Usage in Kotlin:
 * ```kotlin
 * val search = CometChatSearch(context)
 * search.setOnConversationClick { conversation ->
 *     // Handle conversation click
 * }
 * search.setOnMessageClick { message ->
 *     // Handle message click
 * }
 * ```
 */
class CometChatSearch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatSearchStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatSearch::class.java.simpleName
    }

    // View Binding
    private val binding: CometchatSearchBinding

    // ViewModel (shared from chatuikit-core)
    private var viewModel: CometChatSearchViewModel? = null
    private var isExternalViewModel: Boolean = false

    // Adapters
    private val conversationsAdapter: CometChatSearchConversationsAdapter
    private val messagesAdapter: CometChatSearchMessageListAdapter

    // Sticky header decoration for messages
    private var stickyHeaderDecoration: StickyHeaderDecoration? = null

    // Lifecycle owner for observing flows
    private var lifecycleOwner: LifecycleOwner? = null

    // Coroutine scope for collecting flows
    private var viewScope: CoroutineScope? = null

    // Current search text
    private var currentSearchText: String = ""

    // Track hasMore state locally (matching Java pattern)
    private var hasMorePreviousMessages: Boolean = false
    private var hasMorePreviousConversations: Boolean = false

    // Callbacks
    private var onBackPress: (() -> Unit)? = null
    private var onConversationClick: ((Conversation) -> Unit)? = null
    private var onMessageClick: ((BaseMessage) -> Unit)? = null
    private var onError: ((CometChatException) -> Unit)? = null
    private var onLoadConversations: ((List<Conversation>) -> Unit)? = null
    private var onLoadMessages: ((List<BaseMessage>) -> Unit)? = null
    private var onEmpty: (() -> Unit)? = null

    // Configuration
    private var searchScopes: List<SearchScope> = listOf(SearchScope.MESSAGES, SearchScope.CONVERSATIONS)
    private var searchFilters: List<SearchFilter> = getDefaultSearchFilters()
    private var uid: String? = null
    private var guid: String? = null

    // Visibility flags
    private var hideSearchBar: Boolean = false
    private var hideFilterChips: Boolean = false
    private var hideLoadingState: Boolean = false
    private var hideEmptyState: Boolean = false
    private var hideErrorState: Boolean = false
    private var hideInitialState: Boolean = false
    private var hideUserStatus: Boolean = false
    private var hideGroupType: Boolean = false

    // Custom state views
    private var customLoadingView: View? = null
    private var customEmptyView: View? = null
    private var customErrorView: View? = null
    private var customInitialView: View? = null

    // Mention-all label support
    private var mentionAllId: String? = null
    private var mentionAllLabel: String? = null

    // Text formatters
    private var textFormatters: MutableList<CometChatTextFormatter> = mutableListOf()
    private var cometchatMentionsFormatter: CometChatMentionsFormatter? = null

    // Custom ViewHolder listeners for conversations
    private var conversationItemViewListener: SearchConversationsViewHolderListener? = null
    private var conversationLeadingViewListener: SearchConversationsViewHolderListener? = null
    private var conversationTitleViewListener: SearchConversationsViewHolderListener? = null
    private var conversationSubtitleViewListener: SearchConversationsViewHolderListener? = null
    private var conversationTrailingViewListener: SearchConversationsViewHolderListener? = null

    // Custom ViewHolder listeners for messages
    private var textMessageItemViewListener: SearchMessagesViewHolderListener<TextMessage>? = null
    private var imageMessageItemViewListener: SearchMessagesViewHolderListener<MediaMessage>? = null
    private var videoMessageItemViewListener: SearchMessagesViewHolderListener<MediaMessage>? = null
    private var audioMessageItemViewListener: SearchMessagesViewHolderListener<MediaMessage>? = null
    private var documentMessageItemViewListener: SearchMessagesViewHolderListener<MediaMessage>? = null
    private var linkMessageItemViewListener: SearchMessagesViewHolderListener<TextMessage>? = null

    // Date/time formatter
    private var dateTimeFormatter: DateTimeFormatterCallback? = null

    // Style
    private var style: CometChatSearchStyle? = null
    @StyleRes private var currentStyleResId: Int = 0

    // Track selected filters for chip styling
    private val selectedFilterChips = mutableSetOf<SearchFilter>()
    private var isChipClickLocked = false

    init {
        // Inflate layout
        binding = CometchatSearchBinding.inflate(
            LayoutInflater.from(context), this, true
        )

        // Reset the card view to default values
        Utils.initMaterialCard(this)

        // Initialize adapters
        conversationsAdapter = CometChatSearchConversationsAdapter()
        messagesAdapter = CometChatSearchMessageListAdapter()

        // Setup RecyclerViews
        setupRecyclerViews()

        // Setup filter chips
        setupFilterChips()

        // Apply XML attributes
        applyStyleAttributes(attrs, defStyleAttr)

        // Setup click listeners
        setupClickListeners()

        // Setup search input
        setupSearchInput()

        // Initialize ViewModel
        initViewModel()

        // Initialize default text formatters (mentions, rich text) matching reference
        initDefaultTextFormatters()
    }


    /**
     * Sets up the RecyclerViews for conversations and messages.
     */
    private fun setupRecyclerViews() {
        // Conversations RecyclerView
        binding.recyclerviewConversations.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewConversations.adapter = conversationsAdapter
        binding.recyclerviewConversations.isNestedScrollingEnabled = false

        // Messages RecyclerView
        binding.recyclerviewMessages.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewMessages.adapter = messagesAdapter
        binding.recyclerviewMessages.isNestedScrollingEnabled = false

        // Add sticky header decoration for messages
        @Suppress("UNCHECKED_CAST")
        stickyHeaderDecoration = StickyHeaderDecoration(messagesAdapter as StickyHeaderAdapter<RecyclerView.ViewHolder>)
        binding.recyclerviewMessages.addItemDecoration(stickyHeaderDecoration!!)

        // Add scroll listener for pagination
        binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val view = binding.nestedScrollView.getChildAt(binding.nestedScrollView.childCount - 1)
            val diff = view.bottom - (binding.nestedScrollView.height + scrollY)
            
            // Only auto-paginate when See More is NOT applicable (filter-based search)
            // When text-only search (limit=3), pagination is manual via See More click
            val seeMoreApplicable = currentSearchText.isNotEmpty() && !hasActiveFilters()
            if (!seeMoreApplicable && diff < 200 && scrollY > oldScrollY) {
                viewModel?.fetchMoreConversations()
                viewModel?.fetchMoreMessages()
            }
        }
    }

    /**
     * Sets up the filter chips based on searchFilters configuration.
     * Creates custom chips with icons matching the reference implementation.
     */
    private fun setupFilterChips() {
        binding.chipGroup.removeAllViews()
        
        for ((index, filter) in searchFilters.withIndex()) {
            val chipCard = MaterialCardView(context).apply {
                Utils.initMaterialCard(this)
                // Disable ripple effect to match v5 chatuikit behavior
                rippleColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
            }

            val chipContainer = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.cometchat_padding_3),
                    resources.getDimensionPixelSize(R.dimen.cometchat_padding_2),
                    resources.getDimensionPixelSize(R.dimen.cometchat_padding_3),
                    resources.getDimensionPixelSize(R.dimen.cometchat_padding_2)
                )
            }

            val chipIconView = ImageView(context).apply {
                val iconParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.cometchat_14dp),
                    resources.getDimensionPixelSize(R.dimen.cometchat_14dp)
                )
                iconParams.marginEnd = resources.getDimensionPixelSize(R.dimen.cometchat_margin_1)
                layoutParams = iconParams
                scaleType = ImageView.ScaleType.FIT_CENTER
            }

            val chipTextView = TextView(context).apply {
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }

            // Set icon and text based on filter type
            setFilterChipContent(chipIconView, chipTextView, filter)

            val isSelected = selectedFilterChips.contains(filter)
            applyChipStyling(chipCard, chipIconView, chipTextView, isSelected)

            chipCard.setOnClickListener {
                if (isChipClickLocked) return@setOnClickListener
                isChipClickLocked = true
                handleFilterClick(index)
                Handler(Looper.getMainLooper()).postDelayed({
                    isChipClickLocked = false
                }, 50)
            }

            chipContainer.addView(chipIconView)
            chipContainer.addView(chipTextView)
            chipCard.addView(chipContainer)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = resources.getDimensionPixelSize(R.dimen.cometchat_margin_2)
                bottomMargin = resources.getDimensionPixelSize(R.dimen.cometchat_margin_2)
            }
            chipCard.layoutParams = layoutParams
            chipCard.tag = isSelected

            binding.chipGroup.addView(chipCard)
        }
    }

    /**
     * Sets the icon and text for a filter chip based on filter type.
     */
    private fun setFilterChipContent(iconView: ImageView, textView: TextView, filter: SearchFilter) {
        when (filter) {
            SearchFilter.UNREAD -> {
                iconView.setImageResource(R.drawable.cometchat_ic_unread_outlined)
                textView.text = context.getString(R.string.cometchat_unread)
            }
            SearchFilter.GROUPS -> {
                iconView.setImageResource(R.drawable.cometchat_ic_group_outlined)
                textView.text = context.getString(R.string.cometchat_groups)
            }
            SearchFilter.PHOTOS -> {
                iconView.setImageResource(R.drawable.cometchat_ic_photo_outlined)
                textView.text = context.getString(R.string.cometchat_photos)
            }
            SearchFilter.VIDEOS -> {
                iconView.setImageResource(R.drawable.cometchat_ic_video_outlined)
                textView.text = context.getString(R.string.cometchat_videos)
            }
            SearchFilter.LINKS -> {
                iconView.setImageResource(R.drawable.cometchat_ic_link_outlined)
                textView.text = context.getString(R.string.cometchat_links)
            }
            SearchFilter.DOCUMENTS -> {
                iconView.setImageResource(R.drawable.cometchat_ic_document_outlined)
                textView.text = context.getString(R.string.cometchat_documents)
            }
            SearchFilter.AUDIO -> {
                iconView.setImageResource(R.drawable.cometchat_ic_audio_outlined)
                textView.text = context.getString(R.string.cometchat_audio)
            }
        }
    }

    /**
     * Applies styling to a filter chip based on selection state.
     */
    private fun applyChipStyling(
        chipCard: MaterialCardView,
        chipIconView: ImageView,
        chipTextView: TextView,
        isSelected: Boolean
    ) {
        val currentStyle = style

        // Get colors from style or use defaults
        // Selected state: dark background with white text (matching Java UIKit v5)
        // Unselected state: light background with secondary text color
        val textColor = if (isSelected) {
            currentStyle?.filterChipSelectedTextColor ?: ContextCompat.getColor(context, R.color.cometchat_color_text_white)
        } else {
            currentStyle?.filterChipTextColor ?: ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
        }

        val backgroundColor = if (isSelected) {
            currentStyle?.filterChipSelectedBackgroundColor ?: ContextCompat.getColor(context, R.color.cometchat_color_secondary_button_background)
        } else {
            currentStyle?.filterChipBackgroundColor ?: ContextCompat.getColor(context, R.color.cometchat_color_background3)
        }

        val strokeColor = if (isSelected) {
            currentStyle?.filterChipSelectedStrokeColor ?: ContextCompat.getColor(context, R.color.cometchat_color_transparent)
        } else {
            currentStyle?.filterChipStrokeColor ?: ContextCompat.getColor(context, R.color.cometchat_color_transparent)
        }

        val strokeWidth = currentStyle?.filterChipStrokeWidth?.toInt() ?: 0

        val cornerRadius = currentStyle?.filterChipCornerRadius 
            ?: resources.getDimension(R.dimen.cometchat_radius_max)

        chipTextView.setTextColor(textColor)
        chipIconView.setColorFilter(textColor)
        chipCard.setCardBackgroundColor(backgroundColor)
        chipCard.strokeColor = strokeColor
        chipCard.strokeWidth = strokeWidth
        chipCard.radius = cornerRadius

        // Apply text appearance if available
        currentStyle?.filterChipTextAppearance?.let {
            chipTextView.setTextAppearance(it)
            // Re-apply text color after text appearance
            chipTextView.setTextColor(textColor)
        }
    }

    // Filter group constants
    private val MESSAGE_FILTERS = listOf(
        SearchFilter.AUDIO,
        SearchFilter.DOCUMENTS,
        SearchFilter.PHOTOS,
        SearchFilter.VIDEOS,
        SearchFilter.LINKS
    )
    
    private val CONVERSATION_FILTERS = listOf(
        SearchFilter.GROUPS,
        SearchFilter.UNREAD
    )

    /**
     * Handles filter chip click with grouped filter logic.
     * Delegates to the ViewModel's toggleFilter() to ensure consistent behavior
     * with the Compose module (which correctly handles Unread/Groups filtering).
     */
    private fun handleFilterClick(position: Int) {
        val filter = searchFilters.getOrNull(position) ?: return
        viewModel?.toggleFilter(filter)
    }

    /**
     * Toggles the selection state of a given search filter.
     * Implements grouped filter logic matching the reference implementation.
     */
    private fun toggleFilter(filter: SearchFilter) {
        // Handle media filters (PHOTOS, VIDEOS)
        if (filter == SearchFilter.PHOTOS || filter == SearchFilter.VIDEOS) {
            val mediaFilters = listOf(SearchFilter.PHOTOS, SearchFilter.VIDEOS)
            handleGroupedFilterToggle(filter, isFilterSelected(filter), mediaFilters) { hasSelectedMediaFilters() }
            return
        }

        // Handle document filters (DOCUMENTS, AUDIO)
        if (filter == SearchFilter.DOCUMENTS || filter == SearchFilter.AUDIO) {
            val documentFilters = listOf(SearchFilter.DOCUMENTS, SearchFilter.AUDIO)
            handleGroupedFilterToggle(filter, isFilterSelected(filter), documentFilters) { hasSelectedDocumentFilters() }
            return
        }

        // Handle conversation filters (UNREAD, GROUPS)
        if (filter == SearchFilter.UNREAD || filter == SearchFilter.GROUPS) {
            val conversationFilters = listOf(SearchFilter.UNREAD, SearchFilter.GROUPS)
            handleGroupedFilterToggle(filter, isFilterSelected(filter), conversationFilters) { hasSelectedConversationFilters() }
            return
        }

        // Handle individual filters (LINKS)
        if (isFilterSelected(filter)) {
            removeSelectedFilter(filter)
        } else {
            // Clear other groups when selecting LINKS
            if (!hasSelectedLinkFilter()) {
                clearSelectedFilters()
            }
            addSelectedFilter(filter)
        }
    }

    /**
     * Handles toggle logic for grouped filters.
     */
    private fun handleGroupedFilterToggle(
        filter: SearchFilter,
        isSelected: Boolean,
        groupFilters: List<SearchFilter>,
        hasGroupSelection: () -> Boolean
    ) {
        // Clear other groups if no filter from this group is currently selected
        if (!hasGroupSelection()) {
            clearSelectedFilters()
        }

        // Toggle the specific filter
        if (isSelected) {
            removeSelectedFilter(filter)
        } else {
            addSelectedFilter(filter)
        }

        // If any filter from this group is selected, remove filters from other groups
        if (hasGroupSelection()) {
            removeFiltersNotInGroup(groupFilters)
        }
    }

    /**
     * Removes all selected filters that are not in the specified group.
     */
    private fun removeFiltersNotInGroup(groupFilters: List<SearchFilter>) {
        val filtersToRemove = selectedFilterChips.filter { it !in groupFilters }
        filtersToRemove.forEach { removeSelectedFilter(it) }
    }

    /**
     * Refreshes the selection state of all filter chips based on the current active filters.
     */
    private fun refreshChipSelectionState() {
        val hasSelectedFilters = hasActiveFilters()

        for (i in 0 until binding.chipGroup.childCount) {
            val currentFilter = searchFilters.getOrNull(i) ?: continue
            val selected = isFilterSelected(currentFilter)
            val chipView = binding.chipGroup.getChildAt(i)

            if (hasSelectedFilters) {
                chipView.visibility = if (shouldShowFilterInCurrentSelection(currentFilter)) View.VISIBLE else View.GONE
            } else {
                chipView.visibility = View.VISIBLE
            }

            updateChipAppearance(i, selected)
            chipView.tag = selected
        }
    }

    /**
     * Updates the appearance of a filter chip at the specified position.
     */
    private fun updateChipAppearance(position: Int, isSelected: Boolean) {
        val chipCard = binding.chipGroup.getChildAt(position) as? MaterialCardView ?: return
        val chipContainer = chipCard.getChildAt(0) as? LinearLayout ?: return
        val chipIconView = chipContainer.getChildAt(0) as? ImageView ?: return
        val chipTextView = chipContainer.getChildAt(1) as? TextView ?: return

        applyChipStyling(chipCard, chipIconView, chipTextView, isSelected)
    }

    /**
     * Determines if a filter should be visible based on the current selection group.
     */
    private fun shouldShowFilterInCurrentSelection(filter: SearchFilter): Boolean {
        // If the filter is already selected, it should be visible
        if (isFilterSelected(filter)) {
            return true
        }

        // Check if any filter from the same group is selected
        return when {
            isConversationFilter(filter) -> hasSelectedConversationFilters()
            isMediaFilter(filter) -> hasSelectedMediaFilters()
            isDocumentFilter(filter) -> hasSelectedDocumentFilters()
            filter == SearchFilter.LINKS -> hasSelectedLinkFilter()
            else -> false
        }
    }

    /**
     * Fetches search results based on the current search text and selected filters.
     */
    private fun fetchBasedOnSelection() {
        val searchText = currentSearchText
        val selectedFilters = getCurrentSelectedFilters()

        if (searchText.isEmpty() && !hasActiveFilters()) {
            viewModel?.clear()
            return
        }
        viewModel?.searchConversationsAndMessages(searchText, selectedFilters)
    }

    // Filter helper methods

    private fun isFilterSelected(filter: SearchFilter): Boolean = selectedFilterChips.contains(filter)

    private fun addSelectedFilter(filter: SearchFilter) {
        if (!selectedFilterChips.contains(filter)) {
            selectedFilterChips.add(filter)
        }
    }

    private fun removeSelectedFilter(filter: SearchFilter) {
        selectedFilterChips.remove(filter)
    }

    private fun clearSelectedFilters() {
        selectedFilterChips.clear()
    }

    private fun getCurrentSelectedFilters(): Set<SearchFilter> = selectedFilterChips.toSet()

    private fun hasActiveFilters(): Boolean = selectedFilterChips.isNotEmpty()

    private fun hasSelectedFiltersFromGroup(filters: List<SearchFilter>): Boolean {
        return filters.any { selectedFilterChips.contains(it) }
    }

    private fun hasSelectedMessageFilters(): Boolean = hasSelectedFiltersFromGroup(MESSAGE_FILTERS)

    private fun hasSelectedConversationFilters(): Boolean = hasSelectedFiltersFromGroup(CONVERSATION_FILTERS)

    private fun isConversationFilter(filter: SearchFilter): Boolean {
        return filter == SearchFilter.UNREAD || filter == SearchFilter.GROUPS
    }

    private fun isMediaFilter(filter: SearchFilter): Boolean {
        return filter == SearchFilter.PHOTOS || filter == SearchFilter.VIDEOS
    }

    private fun isDocumentFilter(filter: SearchFilter): Boolean {
        return filter == SearchFilter.DOCUMENTS || filter == SearchFilter.AUDIO
    }

    private fun hasSelectedMediaFilters(): Boolean {
        return hasSelectedFiltersFromGroup(listOf(SearchFilter.PHOTOS, SearchFilter.VIDEOS))
    }

    private fun hasSelectedDocumentFilters(): Boolean {
        return hasSelectedFiltersFromGroup(listOf(SearchFilter.AUDIO, SearchFilter.DOCUMENTS))
    }

    private fun hasSelectedLinkFilter(): Boolean {
        return hasSelectedFiltersFromGroup(listOf(SearchFilter.LINKS))
    }

    /**
     * Returns the default search filters in the correct order.
     */
    private fun getDefaultSearchFilters(): List<SearchFilter> {
        return listOf(
            SearchFilter.UNREAD,
            SearchFilter.GROUPS,
            SearchFilter.PHOTOS,
            SearchFilter.VIDEOS,
            SearchFilter.LINKS,
            SearchFilter.DOCUMENTS,
            SearchFilter.AUDIO
        )
    }

    /**
     * Gets the display name for a filter.
     */
    private fun getFilterDisplayName(filter: SearchFilter): String {
        return when (filter) {
            SearchFilter.PHOTOS -> context.getString(R.string.cometchat_photos)
            SearchFilter.VIDEOS -> context.getString(R.string.cometchat_videos)
            SearchFilter.DOCUMENTS -> context.getString(R.string.cometchat_documents)
            SearchFilter.LINKS -> context.getString(R.string.cometchat_links)
            SearchFilter.AUDIO -> context.getString(R.string.cometchat_audio)
            SearchFilter.GROUPS -> context.getString(R.string.cometchat_groups)
            SearchFilter.UNREAD -> context.getString(R.string.cometchat_unread)
        }
    }

    /**
     * Applies style attributes from XML.
     * Reads all style attributes from TypedArray matching the Java chatuikit implementation.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        // First, obtain TypedArray with defStyleAttr to check for cometchatSearchStyle
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatSearch, defStyleAttr, 0
        )
        
        try {
            // Check if a specific style resource is specified
            val styleResId = typedArray.getResourceId(
                R.styleable.CometChatSearch_cometchatSearchStyle, 0
            )
            currentStyleResId = styleResId
            
            // Recycle the first TypedArray
            typedArray.recycle()
            
            // If a style resource is specified, use it to get attributes
            // Otherwise, use the default style from defStyleAttr (R.attr.cometchatSearchStyle -> @style/CometChatSearchStyle)
            typedArray = if (styleResId != 0) {
                context.theme.obtainStyledAttributes(styleResId, R.styleable.CometChatSearch)
            } else {
                // Use defStyleAttr to resolve the default style from theme
                // This will resolve R.attr.cometchatSearchStyle -> @style/CometChatSearchStyle from themes.xml
                context.theme.obtainStyledAttributes(
                    attrs, R.styleable.CometChatSearch, defStyleAttr, R.style.CometChatSearchStyle
                )
            }
            
            // Extract all attributes from TypedArray and build style (matching Java extractAttributesAndApplyDefaults)
            style = extractAttributesAndBuildStyle(typedArray)
            applyStyle()
            
            // Apply default icons for state views
            applyDefaultStateIcons()
        } finally {
            typedArray.recycle()
        }
    }

    /**
     * Extracts attributes from TypedArray and builds CometChatSearchStyle.
     * This matches the Java chatuikit extractAttributesAndApplyDefaults() method.
     */
    private fun extractAttributesAndBuildStyle(typedArray: android.content.res.TypedArray): CometChatSearchStyle {
        return CometChatSearchStyle.Builder(context)
            // Background
            .setBackgroundColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchBackgroundColor,
                ContextCompat.getColor(context, R.color.cometchat_color_background1)
            ))
            // Search bar styling
            .setSearchBarBackgroundColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchBarBackgroundColor,
                ContextCompat.getColor(context, R.color.cometchat_color_background3)
            ))
            .setSearchBarStrokeWidth(typedArray.getDimensionPixelSize(
                R.styleable.CometChatSearch_cometchatSearchBarStrokeWidth, 0
            ).toFloat())
            .setSearchBarStrokeColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchBarStrokeColor,
                ContextCompat.getColor(context, R.color.cometchat_color_stroke_dark)
            ))
            .setSearchBarCornerRadius(typedArray.getDimensionPixelSize(
                R.styleable.CometChatSearch_cometchatSearchBarCornerRadius, 0
            ).toFloat())
            .setSearchBarTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchBarTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_primary)
            ))
            .apply {
                val searchBarTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchBarTextAppearance, 0
                )
                if (searchBarTextAppearance != 0) setSearchBarTextAppearance(searchBarTextAppearance)
            }
            .setSearchBarHintTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchBarHintTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_tertiary)
            ))
            // Back icon
            .apply {
                typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchBackIcon)?.let { setBackIcon(it) }
            }
            .setBackIconTint(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchBackIconTint,
                ContextCompat.getColor(context, R.color.cometchat_color_icon_primary)
            ))
            // Clear icon
            .apply {
                typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchClearIcon)?.let { setClearIcon(it) }
            }
            .setClearIconTint(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchClearIconTint,
                ContextCompat.getColor(context, R.color.cometchat_color_icon_secondary)
            ))
            // Search icon
            .apply {
                typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchIcon)?.let { setSearchIcon(it) }
            }
            .setSearchIconTint(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchIconTint,
                ContextCompat.getColor(context, R.color.cometchat_color_icon_secondary)
            ))
            // Filter chip styling
            .setFilterChipBackgroundColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchFilterChipBackgroundColor,
                ContextCompat.getColor(context, R.color.cometchat_color_background3)
            ))
            .setFilterChipSelectedBackgroundColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchFilterChipSelectedBackgroundColor,
                ContextCompat.getColor(context, R.color.cometchat_color_secondary_button_background)
            ))
            .setFilterChipTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchFilterChipTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
            ))
            .setFilterChipSelectedTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchFilterChipSelectedTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_white)
            ))
            .apply {
                val filterChipTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchFilterChipTextAppearance, 0
                )
                if (filterChipTextAppearance != 0) setFilterChipTextAppearance(filterChipTextAppearance)
            }
            .setFilterChipStrokeColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchFilterChipStrokeColor,
                ContextCompat.getColor(context, R.color.cometchat_color_transparent)
            ))
            .setFilterChipSelectedStrokeColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchFilterChipSelectedStrokeColor,
                ContextCompat.getColor(context, R.color.cometchat_color_transparent)
            ))
            .setFilterChipStrokeWidth(typedArray.getDimensionPixelSize(
                R.styleable.CometChatSearch_cometchatSearchFilterChipStrokeWidth, 0
            ).toFloat())
            .setFilterChipCornerRadius(typedArray.getDimensionPixelSize(
                R.styleable.CometChatSearch_cometchatSearchFilterChipCornerRadius, 0
            ).toFloat())
            // Section header styling
            .setSectionHeaderTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchSectionHeaderTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
            ))
            .apply {
                val sectionHeaderTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchSectionHeaderTextAppearance, 0
                )
                if (sectionHeaderTextAppearance != 0) setSectionHeaderTextAppearance(sectionHeaderTextAppearance)
            }
            .setSectionHeaderBackgroundColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchSectionHeaderBackgroundColor,
                ContextCompat.getColor(context, R.color.cometchat_color_background1)
            ))
            // Conversation item styling
            .setConversationItemBackgroundColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchConversationItemBackgroundColor,
                ContextCompat.getColor(context, R.color.cometchat_color_background1)
            ))
            .setConversationTitleTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchConversationTitleTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_primary)
            ))
            .apply {
                val conversationTitleTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchConversationTitleTextAppearance, 0
                )
                if (conversationTitleTextAppearance != 0) setConversationTitleTextAppearance(conversationTitleTextAppearance)
            }
            .setConversationSubtitleTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchConversationSubtitleTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
            ))
            .apply {
                val conversationSubtitleTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchConversationSubtitleTextAppearance, 0
                )
                if (conversationSubtitleTextAppearance != 0) setConversationSubtitleTextAppearance(conversationSubtitleTextAppearance)
            }
            .setConversationTimestampTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchConversationTimestampTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
            ))
            .apply {
                val conversationTimestampTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchConversationTimestampTextAppearance, 0
                )
                if (conversationTimestampTextAppearance != 0) setConversationTimestampTextAppearance(conversationTimestampTextAppearance)
            }
            // Message item styling
            .setMessageItemBackgroundColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchMessageItemBackgroundColor,
                ContextCompat.getColor(context, R.color.cometchat_color_background1)
            ))
            .setMessageTitleTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchMessageTitleTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_primary)
            ))
            .apply {
                val messageTitleTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchMessageTitleTextAppearance, 0
                )
                if (messageTitleTextAppearance != 0) setMessageTitleTextAppearance(messageTitleTextAppearance)
            }
            .setMessageSubtitleTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchMessageSubtitleTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
            ))
            .apply {
                val messageSubtitleTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchMessageSubtitleTextAppearance, 0
                )
                if (messageSubtitleTextAppearance != 0) setMessageSubtitleTextAppearance(messageSubtitleTextAppearance)
            }
            .setMessageTimestampTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchMessageTimestampTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
            ))
            .apply {
                val messageTimestampTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchMessageTimestampTextAppearance, 0
                )
                if (messageTimestampTextAppearance != 0) setMessageTimestampTextAppearance(messageTimestampTextAppearance)
            }
            .setMessageLinkTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchMessageLinkTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_info)
            ))
            .apply {
                val messageLinkTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchMessageLinkTextAppearance, 0
                )
                if (messageLinkTextAppearance != 0) setMessageLinkTextAppearance(messageLinkTextAppearance)
            }
            .apply {
                typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchMessageThreadIcon)?.let { setMessageThreadIcon(it) }
            }
            // Empty state styling
            .setEmptyStateTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchEmptyStateTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_primary)
            ))
            .apply {
                val emptyStateTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchEmptyStateTextAppearance, 0
                )
                if (emptyStateTextAppearance != 0) setEmptyStateTextAppearance(emptyStateTextAppearance)
            }
            .setEmptyStateSubtitleTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchEmptyStateSubtitleTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
            ))
            .apply {
                val emptyStateSubtitleTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchEmptyStateSubtitleTextAppearance, 0
                )
                if (emptyStateSubtitleTextAppearance != 0) setEmptyStateSubtitleTextAppearance(emptyStateSubtitleTextAppearance)
            }
            .apply {
                typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchEmptyStateIcon)?.let { setEmptyStateIcon(it) }
            }
            .setEmptyStateIconTint(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchEmptyStateIconTint,
                ContextCompat.getColor(context, R.color.cometchat_color_icon_secondary)
            ))
            // Initial state styling
            .setInitialStateTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchInitialStateTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_primary)
            ))
            .apply {
                val initialStateTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchInitialStateTextAppearance, 0
                )
                if (initialStateTextAppearance != 0) setInitialStateTextAppearance(initialStateTextAppearance)
            }
            .setInitialStateSubtitleTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchInitialStateSubtitleTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
            ))
            .apply {
                val initialStateSubtitleTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchInitialStateSubtitleTextAppearance, 0
                )
                if (initialStateSubtitleTextAppearance != 0) setInitialStateSubtitleTextAppearance(initialStateSubtitleTextAppearance)
            }
            .apply {
                typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchInitialStateIcon)?.let { setInitialStateIcon(it) }
            }
            .setInitialStateIconTint(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchInitialStateIconTint,
                ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
            ))
            // Error state styling
            .setErrorStateTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchErrorStateTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_primary)
            ))
            .apply {
                val errorStateTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchErrorStateTextAppearance, 0
                )
                if (errorStateTextAppearance != 0) setErrorStateTextAppearance(errorStateTextAppearance)
            }
            .setErrorStateSubtitleTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchErrorStateSubtitleTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
            ))
            .apply {
                val errorStateSubtitleTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchErrorStateSubtitleTextAppearance, 0
                )
                if (errorStateSubtitleTextAppearance != 0) setErrorStateSubtitleTextAppearance(errorStateSubtitleTextAppearance)
            }
            .apply {
                typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchErrorStateIcon)?.let { setErrorStateIcon(it) }
            }
            .setErrorStateIconTint(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchErrorStateIconTint,
                ContextCompat.getColor(context, R.color.cometchat_color_icon_secondary)
            ))
            // See more button styling
            .setSeeMoreTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchSeeMoreTextColor,
                com.cometchat.uikit.kotlin.theme.CometChatTheme.getPrimaryButtonTextColor(context)
            ))
            .apply {
                val seeMoreTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchSeeMoreTextAppearance, 0
                )
                if (seeMoreTextAppearance != 0) setSeeMoreTextAppearance(seeMoreTextAppearance)
            }
            // Date separator styling
            .setDateSeparatorTextColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchDateSeparatorTextColor,
                ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
            ))
            .apply {
                val dateSeparatorTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatSearch_cometchatSearchDateSeparatorTextAppearance, 0
                )
                if (dateSeparatorTextAppearance != 0) setDateSeparatorTextAppearance(dateSeparatorTextAppearance)
            }
            .setDateSeparatorBackgroundColor(typedArray.getColor(
                R.styleable.CometChatSearch_cometchatSearchDateSeparatorBackgroundColor,
                ContextCompat.getColor(context, R.color.cometchat_color_transparent)
            ))
            .build()
    }

    /**
     * Applies default icons for state views.
     */
    private fun applyDefaultStateIcons() {
        // Set default initial state icon
        val initialIcon = ContextCompat.getDrawable(context, R.drawable.cometchat_ic_search_large)
        binding.ivInitialState.setImageDrawable(initialIcon)
        binding.ivInitialState.setColorFilter(ContextCompat.getColor(context, R.color.cometchat_color_icon_secondary))
        
        // Set default empty state icon
        binding.ivEmptyState.setImageDrawable(initialIcon)
        binding.ivEmptyState.setColorFilter(ContextCompat.getColor(context, R.color.cometchat_color_icon_secondary))
        
        // Set default error state icon
        binding.ivErrorState.setImageDrawable(initialIcon)
        binding.ivErrorState.setColorFilter(ContextCompat.getColor(context, R.color.cometchat_color_icon_secondary))
    }

    /**
     * Applies the style to all views.
     */
    private fun applyStyle() {
        val currentStyle = style

        // Container background
        val bgColor = currentStyle?.backgroundColor ?: ContextCompat.getColor(context, R.color.cometchat_color_background1)
        setCardBackgroundColor(bgColor)
        binding.parentLayout.setBackgroundColor(bgColor)

        // Search bar styling - apply defaults matching reference implementation
        val searchBarBgColor = currentStyle?.searchBarBackgroundColor ?: ContextCompat.getColor(context, R.color.cometchat_color_background3)
        val searchBarCornerRad = currentStyle?.searchBarCornerRadius ?: resources.getDimension(R.dimen.cometchat_radius_max)
        val searchBarStrokeWid = currentStyle?.searchBarStrokeWidth?.toInt() ?: 0
        val searchBarStrokeCol = currentStyle?.searchBarStrokeColor ?: ContextCompat.getColor(context, R.color.cometchat_color_stroke_dark)
        val searchBarTextCol = currentStyle?.searchBarTextColor ?: ContextCompat.getColor(context, R.color.cometchat_color_text_primary)
        val searchBarHintCol = currentStyle?.searchBarHintTextColor ?: ContextCompat.getColor(context, R.color.cometchat_color_text_tertiary)
        
        // Apply to search box (LinearLayout inside card) for background color
        binding.searchBox.setBackgroundColor(searchBarBgColor)
        binding.searchBoxCard.setCardBackgroundColor(searchBarBgColor)
        binding.searchBoxCard.radius = searchBarCornerRad
        binding.searchBoxCard.strokeWidth = searchBarStrokeWid
        binding.searchBoxCard.strokeColor = searchBarStrokeCol
        binding.searchInput.setTextColor(searchBarTextCol)
        binding.searchInput.setHintTextColor(searchBarHintCol)
        
        // Back icon - set default if not provided
        val backIconDrawable = currentStyle?.backIcon ?: ContextCompat.getDrawable(context, R.drawable.cometchat_ic_back_selected)
        val backIconTintColor = currentStyle?.backIconTint ?: ContextCompat.getColor(context, R.color.cometchat_color_icon_primary)
        binding.ivBack.setImageDrawable(backIconDrawable)
        binding.ivBack.setColorFilter(backIconTintColor)
        
        // Clear icon - set default if not provided
        val clearIconDrawable = currentStyle?.clearIcon ?: ContextCompat.getDrawable(context, R.drawable.cometchat_ic_close)
        val clearIconTintColor = currentStyle?.clearIconTint ?: ContextCompat.getColor(context, R.color.cometchat_color_icon_secondary)
        binding.ivClear.setImageDrawable(clearIconDrawable)
        binding.ivClear.setColorFilter(clearIconTintColor)

        // Section header styling - apply text appearance first, then color (matching Java flow)
        val sectionHeaderTextCol = currentStyle?.sectionHeaderTextColor ?: ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
        val sectionHeaderBgCol = currentStyle?.sectionHeaderBackgroundColor ?: ContextCompat.getColor(context, R.color.cometchat_color_background1)
        
        // Apply text appearance first (if available)
        currentStyle?.sectionHeaderTextAppearance?.let {
            binding.tvConversationsHeader.setTextAppearance(it)
            binding.tvMessagesHeader.setTextAppearance(it)
        }
        // Apply text color after text appearance (to override any color from text appearance)
        binding.tvConversationsHeader.setTextColor(sectionHeaderTextCol)
        binding.tvMessagesHeader.setTextColor(sectionHeaderTextCol)
        // Apply background color
        binding.tvConversationsHeader.setBackgroundColor(sectionHeaderBgCol)
        binding.tvMessagesHeader.setBackgroundColor(sectionHeaderBgCol)

        // See more styling
        val seeMoreTextCol = currentStyle?.seeMoreTextColor ?: com.cometchat.uikit.kotlin.theme.CometChatTheme.getPrimaryButtonTextColor(context)
        binding.tvConversationsSeeMore.setTextColor(seeMoreTextCol)
        binding.tvMessagesSeeMore.setTextColor(seeMoreTextCol)
        currentStyle?.seeMoreTextAppearance?.let {
            binding.tvConversationsSeeMore.setTextAppearance(it)
            binding.tvMessagesSeeMore.setTextAppearance(it)
        }

        // Initial state styling
        currentStyle?.initialStateIcon?.let { binding.ivInitialState.setImageDrawable(it) }
        currentStyle?.initialStateIconTint?.let { binding.ivInitialState.setColorFilter(it) }
        val initialStateTitleCol = currentStyle?.initialStateTextColor ?: ContextCompat.getColor(context, R.color.cometchat_color_text_primary)
        binding.tvInitialStateTitle.setTextColor(initialStateTitleCol)
        currentStyle?.initialStateTextAppearance?.let { binding.tvInitialStateTitle.setTextAppearance(it) }
        val initialStateSubtitleCol = currentStyle?.initialStateSubtitleTextColor ?: ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
        binding.tvInitialStateSubtitle.setTextColor(initialStateSubtitleCol)
        currentStyle?.initialStateSubtitleTextAppearance?.let { binding.tvInitialStateSubtitle.setTextAppearance(it) }

        // Empty state styling
        currentStyle?.emptyStateIcon?.let { binding.ivEmptyState.setImageDrawable(it) }
        currentStyle?.emptyStateIconTint?.let { binding.ivEmptyState.setColorFilter(it) }
        val emptyStateTitleCol = currentStyle?.emptyStateTextColor ?: ContextCompat.getColor(context, R.color.cometchat_color_text_primary)
        binding.tvEmptyStateTitle.setTextColor(emptyStateTitleCol)
        currentStyle?.emptyStateTextAppearance?.let { binding.tvEmptyStateTitle.setTextAppearance(it) }
        val emptyStateSubtitleCol = currentStyle?.emptyStateSubtitleTextColor ?: ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
        binding.tvEmptyStateSubtitle.setTextColor(emptyStateSubtitleCol)
        currentStyle?.emptyStateSubtitleTextAppearance?.let { binding.tvEmptyStateSubtitle.setTextAppearance(it) }

        // Error state styling
        currentStyle?.errorStateIcon?.let { binding.ivErrorState.setImageDrawable(it) }
        currentStyle?.errorStateIconTint?.let { binding.ivErrorState.setColorFilter(it) }
        val errorStateTitleCol = currentStyle?.errorStateTextColor ?: ContextCompat.getColor(context, R.color.cometchat_color_text_primary)
        binding.tvErrorStateTitle.setTextColor(errorStateTitleCol)
        currentStyle?.errorStateTextAppearance?.let { binding.tvErrorStateTitle.setTextAppearance(it) }
        val errorStateSubtitleCol = currentStyle?.errorStateSubtitleTextColor ?: ContextCompat.getColor(context, R.color.cometchat_color_text_secondary)
        binding.tvErrorStateSubtitle.setTextColor(errorStateSubtitleCol)
        currentStyle?.errorStateSubtitleTextAppearance?.let { binding.tvErrorStateSubtitle.setTextAppearance(it) }

        // Loading state styling
        currentStyle?.loadingStateBackgroundColor?.let { binding.loadingStateView.setBackgroundColor(it) }

        // Apply style to adapters
        currentStyle?.let {
            conversationsAdapter.setStyle(it)
            messagesAdapter.setStyle(it)
        }
    }

    /**
     * Sets up click listeners for UI elements.
     */
    private fun setupClickListeners() {
        // Back button
        binding.ivBack.setOnClickListener {
            onBackPress?.invoke()
        }

        // Clear button
        binding.ivClear.setOnClickListener {
            binding.searchInput.text?.clear()
            currentSearchText = ""
            viewModel?.searchConversationsAndMessages("", viewModel?.selectedFilters?.value ?: emptySet())
        }

        // Retry button
        binding.btnRetry.setOnClickListener {
            viewModel?.searchConversationsAndMessages(currentSearchText, viewModel?.selectedFilters?.value ?: emptySet())
        }

        // Conversation item click
        conversationsAdapter.setOnConversationClick { conversation ->
            onConversationClick?.invoke(conversation)
        }

        // Message item click
        messagesAdapter.setOnMessageClick { message ->
            onMessageClick?.invoke(message)
        }

        // See more conversations
        binding.tvConversationsSeeMore.setOnClickListener {
            viewModel?.fetchMoreConversations()
        }

        // See more messages
        binding.tvMessagesSeeMore.setOnClickListener {
            viewModel?.fetchMoreMessages()
        }
    }

    /**
     * Sets up the search input with text change listener and keyboard action.
     */
    private fun setupSearchInput() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                currentSearchText = text
                
                // Show/hide clear button
                binding.ivClear.isVisible = text.isNotEmpty()
                
                // Trigger search (ViewModel handles debouncing)
                viewModel?.searchConversationsAndMessages(text, viewModel?.selectedFilters?.value ?: emptySet())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })

        // Handle keyboard search action
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel?.searchConversationsAndMessages(currentSearchText, viewModel?.selectedFilters?.value ?: emptySet())
                true
            } else {
                false
            }
        }
    }

    /**
     * Initializes default text formatters (mentions, rich text) matching the reference.
     * Creates a CometChatMentionsFormatter and adds it to the formatters list,
     * then propagates to both adapters.
     */
    private fun initDefaultTextFormatters() {
        try {
            // Add rich text formatter (matching Java: CometChatRichTextFormatter for markdown)
            textFormatters.add(CometChatRichTextFormatter())
            // Add mentions formatter
            cometchatMentionsFormatter = CometChatMentionsFormatter(context)
            if (!mentionAllId.isNullOrEmpty() && !mentionAllLabel.isNullOrEmpty()) {
                cometchatMentionsFormatter?.setMentionAllLabel(mentionAllId!!, mentionAllLabel!!)
            }
            cometchatMentionsFormatter?.let { textFormatters.add(it) }
            conversationsAdapter.setTextFormatters(textFormatters)
            messagesAdapter.setTextFormatters(textFormatters)
        } catch (e: Exception) {
            // Silently handle if SDK not initialized yet
        }
    }

    /**
     * Initializes the ViewModel and sets up observers.
     */
    private fun initViewModel() {
        lifecycleOwner = Utils.getLifecycleOwner(context)
        if (lifecycleOwner == null) return

        // Only create internal ViewModel if not externally provided
        if (!isExternalViewModel && viewModel == null) {
            val factory = CometChatSearchViewModelFactory()
            viewModel = ViewModelProvider(
                lifecycleOwner as ViewModelStoreOwner,
                factory
            )[CometChatSearchViewModel::class.java]
        }

        // Apply configuration to ViewModel
        applyViewModelConfiguration()

        // Set up observers
        observeViewModel()
    }

    /**
     * Applies configuration to the ViewModel.
     */
    private fun applyViewModelConfiguration() {
        viewModel?.setSearchScopes(searchScopes)
        viewModel?.setUid(uid)
        viewModel?.setGuid(guid)
    }

    /**
     * Sets up observers for the ViewModel's StateFlows.
     */
    private fun observeViewModel() {
        val vm = viewModel ?: return

        // Cancel any existing scope and create a new one
        viewScope?.cancel()
        viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Observe UI state
        viewScope?.launch {
            vm.uiState.collectLatest { state ->
                handleStateChange(state)
            }
        }

        // Observe conversations
        viewScope?.launch {
            vm.conversations.collectLatest { conversations ->
                updateConversationsList(conversations)
                onLoadConversations?.invoke(conversations)
            }
        }

        // Observe messages
        viewScope?.launch {
            vm.messages.collectLatest { messages ->
                updateMessagesList(messages)
                onLoadMessages?.invoke(messages)
            }
        }

        // Observe selected filters
        viewScope?.launch {
            vm.selectedFilters.collectLatest { filters ->
                updateFilterChips(filters)
            }
        }

        // Observe visible filters to update chip visibility (e.g., hide conversation filters in uid/guid context)
        viewScope?.launch {
            vm.visibleFilters.collectLatest { visibleFilters ->
                val filteredList = searchFilters.filter { it in visibleFilters }
                if (filteredList != searchFilters) {
                    searchFilters = filteredList
                    setupFilterChips()
                }
            }
        }

        // Observe hasMore flags
        viewScope?.launch {
            vm.hasMoreConversations.collectLatest { hasMore ->
                hasMorePreviousConversations = hasMore
                updateSeeMoreVisibility()
            }
        }

        viewScope?.launch {
            vm.hasMoreMessages.collectLatest { hasMore ->
                hasMorePreviousMessages = hasMore
                updateSeeMoreVisibility()
            }
        }
    }


    /**
     * Handles UI state changes.
     */
    private fun handleStateChange(state: SearchUIState) {
        hideAllStates()

        when (state) {
            is SearchUIState.Initial -> handleInitialState()
            is SearchUIState.Loading -> handleLoadingState()
            is SearchUIState.Content -> handleContentState()
            is SearchUIState.Empty -> handleEmptyState()
            is SearchUIState.Error -> handleErrorState(state.exception)
        }
    }

    /**
     * Hides all state views.
     */
    private fun hideAllStates() {
        binding.nestedScrollView.visibility = GONE
        binding.initialStateView.visibility = GONE
        binding.emptyStateView.visibility = GONE
        binding.errorStateView.visibility = GONE
        binding.loadingStateView.visibility = GONE
        binding.customLayout.visibility = GONE
    }

    /**
     * Handles initial state (before any search).
     */
    private fun handleInitialState() {
        if (!hideInitialState) {
            if (customInitialView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customInitialView)
                binding.customLayout.visibility = VISIBLE
            } else {
                binding.initialStateView.visibility = VISIBLE
            }
        }
        announceForAccessibility(context.getString(R.string.cometchat_start_your_search))
    }

    /**
     * Handles loading state.
     */
    private fun handleLoadingState() {
        if (!hideLoadingState) {
            if (customLoadingView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customLoadingView)
                binding.customLayout.visibility = VISIBLE
            } else {
                binding.loadingStateView.visibility = VISIBLE
                setupShimmer()
            }
        }
        announceForAccessibility(context.getString(R.string.cometchat_loading_conversations))
    }

    /**
     * Sets up shimmer loading effect.
     */
    private fun setupShimmer() {
        val shimmerAdapter = CometChatShimmerAdapter(10, R.layout.shimmer_list_base)
        binding.shimmerRecyclerview.adapter = shimmerAdapter
        binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(context))
        binding.shimmerEffectFrame.startShimmer()
    }

    /**
     * Handles content state.
     */
    private fun handleContentState() {
        binding.shimmerEffectFrame.stopShimmer()
        binding.nestedScrollView.visibility = VISIBLE
    }

    /**
     * Handles empty state.
     */
    private fun handleEmptyState() {
        binding.shimmerEffectFrame.stopShimmer()
        if (!hideEmptyState) {
            if (customEmptyView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customEmptyView)
                binding.customLayout.visibility = VISIBLE
            } else {
                binding.emptyStateView.visibility = VISIBLE
            }
        }
        announceForAccessibility(context.getString(R.string.cometchat_no_results))
        onEmpty?.invoke()
    }

    /**
     * Handles error state.
     */
    private fun handleErrorState(exception: CometChatException?) {
        binding.shimmerEffectFrame.stopShimmer()
        if (!hideErrorState) {
            if (customErrorView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customErrorView)
                binding.customLayout.visibility = VISIBLE
            } else {
                binding.errorStateView.visibility = VISIBLE
            }
        }
        announceForAccessibility(context.getString(R.string.cometchat_something_went_wrong_please_try_again))
        exception?.let { onError?.invoke(it) }
    }

    /**
     * Updates the conversations list.
     */
    private fun updateConversationsList(conversations: List<Conversation>) {
        conversationsAdapter.submitList(conversations)
        val hasFilterChipsOnly = hasActiveFilters() && currentSearchText.isEmpty()
        binding.conversationsSection.isVisible = conversations.isNotEmpty()
        binding.tvConversationsHeader.isVisible = conversations.isNotEmpty() && !hasFilterChipsOnly
        updateSeeMoreVisibility(conversationCount = conversations.size)
    }

    /**
     * Updates the messages list.
     */
    private fun updateMessagesList(messages: List<BaseMessage>) {
        messagesAdapter.submitList(messages)
        val hasFilterChipsOnly = hasActiveFilters() && currentSearchText.isEmpty()
        binding.messagesSection.isVisible = messages.isNotEmpty()
        binding.tvMessagesHeader.isVisible = messages.isNotEmpty() && !hasFilterChipsOnly
        updateSeeMoreVisibility(messageCount = messages.size)
    }

    /**
     * Updates See More text visibility matching Java reference logic.
     * Visible when: searchText not empty AND no active filters AND hasMore AND list not empty
     */
    private fun updateSeeMoreVisibility(conversationCount: Int? = null, messageCount: Int? = null) {
        val showSeeMore = currentSearchText.isNotEmpty() && !hasActiveFilters()
        val convCount = conversationCount ?: conversationsAdapter.currentList.size
        val msgCount = messageCount ?: messagesAdapter.currentList.size
        binding.tvConversationsSeeMore.isVisible = showSeeMore
            && hasMorePreviousConversations && convCount > 0
        binding.tvMessagesSeeMore.isVisible = showSeeMore
            && hasMorePreviousMessages && msgCount > 0
    }

    /**
     * Updates the filter chips to reflect selected state.
     */
    private fun updateFilterChips(selectedFilters: Set<SearchFilter>) {
        selectedFilterChips.clear()
        selectedFilterChips.addAll(selectedFilters)
        refreshChipSelectionState()
    }

    // ==================== Public API ====================

    /**
     * Sets an externally created ViewModel.
     * Use this when you want to share a ViewModel across multiple components.
     */
    fun setViewModel(viewModel: CometChatSearchViewModel) {
        this.viewModel = viewModel
        this.isExternalViewModel = true
        applyViewModelConfiguration()
        observeViewModel()
    }

    /**
     * Sets the style for the search component.
     */
    fun setStyle(style: CometChatSearchStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the search scopes to determine which types of data to search.
     */
    fun setSearchScopes(scopes: List<SearchScope>) {
        this.searchScopes = scopes
        viewModel?.setSearchScopes(scopes)
    }

    /**
     * Sets the available filter chips.
     */
    fun setSearchFilters(filters: List<SearchFilter>) {
        this.searchFilters = filters
        setupFilterChips()
    }

    /**
     * Sets the UID for contextual search within a specific user conversation.
     */
    fun setUid(uid: String?) {
        this.uid = uid
        viewModel?.setUid(uid)
        messagesAdapter.setUid(uid)
    }

    /**
     * Sets the GUID for contextual search within a specific group conversation.
     */
    fun setGuid(guid: String?) {
        this.guid = guid
        viewModel?.setGuid(guid)
        messagesAdapter.setGuid(guid)
    }

    /**
     * Sets a custom ConversationsRequest builder.
     */
    fun setConversationsRequestBuilder(builder: ConversationsRequest.ConversationsRequestBuilder) {
        viewModel?.setConversationsRequestBuilder(builder)
    }

    /**
     * Sets a custom MessagesRequest builder.
     */
    fun setMessagesRequestBuilder(builder: MessagesRequest.MessagesRequestBuilder) {
        viewModel?.setMessagesRequestBuilder(builder)
    }

    /**
     * Sets the date/time formatter for timestamps.
     */
    fun setDateTimeFormatter(formatter: DateTimeFormatterCallback?) {
        this.dateTimeFormatter = formatter
        conversationsAdapter.setDateTimeFormatter(formatter)
        messagesAdapter.setDateTimeFormatter(formatter)
    }

    // ==================== Visibility Setters ====================

    /**
     * Sets whether to hide the search bar.
     */
    fun setHideSearchBar(hide: Boolean) {
        this.hideSearchBar = hide
        binding.searchBarLayout.isVisible = !hide
    }

    /**
     * Sets whether to hide the filter chips.
     */
    fun setHideFilterChips(hide: Boolean) {
        this.hideFilterChips = hide
        binding.chipGroup.isVisible = !hide
    }

    /**
     * Sets whether to hide the loading state.
     */
    fun setHideLoadingState(hide: Boolean) {
        this.hideLoadingState = hide
    }

    /**
     * Sets whether to hide the empty state.
     */
    fun setHideEmptyState(hide: Boolean) {
        this.hideEmptyState = hide
    }

    /**
     * Sets whether to hide the error state.
     */
    fun setHideErrorState(hide: Boolean) {
        this.hideErrorState = hide
    }

    /**
     * Sets whether to hide the initial state.
     */
    fun setHideInitialState(hide: Boolean) {
        this.hideInitialState = hide
    }

    /**
     * Sets whether to hide user online status indicator.
     */
    fun setHideUserStatus(hide: Boolean) {
        this.hideUserStatus = hide
        conversationsAdapter.setHideUserStatus(hide)
    }

    /**
     * Sets whether to hide group type indicator.
     */
    fun setHideGroupType(hide: Boolean) {
        this.hideGroupType = hide
        conversationsAdapter.setHideGroupType(hide)
    }

    // ==================== Custom View Setters ====================

    /**
     * Sets a custom loading view.
     */
    fun setLoadingView(view: View?) {
        this.customLoadingView = view
    }

    /**
     * Sets a custom empty view.
     */
    fun setEmptyView(view: View?) {
        this.customEmptyView = view
    }

    /**
     * Sets a custom error view.
     */
    fun setErrorView(view: View?) {
        this.customErrorView = view
    }

    /**
     * Sets a custom initial view.
     */
    fun setInitialView(view: View?) {
        this.customInitialView = view
    }

    // ==================== Conversation ViewHolder Listeners ====================

    /**
     * Sets the custom item view listener for conversations.
     */
    fun setConversationItemViewListener(listener: SearchConversationsViewHolderListener?) {
        this.conversationItemViewListener = listener
        conversationsAdapter.setItemViewListener(listener)
    }

    /**
     * Sets the custom leading view listener for conversations.
     */
    fun setConversationLeadingViewListener(listener: SearchConversationsViewHolderListener?) {
        this.conversationLeadingViewListener = listener
        conversationsAdapter.setLeadingViewListener(listener)
    }

    /**
     * Sets the custom title view listener for conversations.
     */
    fun setConversationTitleViewListener(listener: SearchConversationsViewHolderListener?) {
        this.conversationTitleViewListener = listener
        conversationsAdapter.setTitleViewListener(listener)
    }

    /**
     * Sets the custom subtitle view listener for conversations.
     */
    fun setConversationSubtitleViewListener(listener: SearchConversationsViewHolderListener?) {
        this.conversationSubtitleViewListener = listener
        conversationsAdapter.setSubtitleViewListener(listener)
    }

    /**
     * Sets the custom trailing view listener for conversations.
     */
    fun setConversationTrailingViewListener(listener: SearchConversationsViewHolderListener?) {
        this.conversationTrailingViewListener = listener
        conversationsAdapter.setTrailingViewListener(listener)
    }

    // ==================== Message ViewHolder Listeners ====================

    /**
     * Sets the custom view listener for text message items.
     */
    fun setTextMessageItemViewListener(listener: SearchMessagesViewHolderListener<TextMessage>?) {
        this.textMessageItemViewListener = listener
        messagesAdapter.setTextMessageItemViewListener(listener)
    }

    /**
     * Sets the custom view listener for image message items.
     */
    fun setImageMessageItemViewListener(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        this.imageMessageItemViewListener = listener
        messagesAdapter.setImageMessageItemViewListener(listener)
    }

    /**
     * Sets the custom view listener for video message items.
     */
    fun setVideoMessageItemViewListener(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        this.videoMessageItemViewListener = listener
        messagesAdapter.setVideoMessageItemViewListener(listener)
    }

    /**
     * Sets the custom view listener for audio message items.
     */
    fun setAudioMessageItemViewListener(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        this.audioMessageItemViewListener = listener
        messagesAdapter.setAudioMessageItemViewListener(listener)
    }

    /**
     * Sets the custom view listener for document message items.
     */
    fun setDocumentMessageItemViewListener(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        this.documentMessageItemViewListener = listener
        messagesAdapter.setDocumentMessageItemViewListener(listener)
    }

    /**
     * Sets the custom view listener for link message items.
     */
    fun setLinkMessageItemViewListener(listener: SearchMessagesViewHolderListener<TextMessage>?) {
        this.linkMessageItemViewListener = listener
        messagesAdapter.setLinkMessageItemViewListener(listener)
    }

    // ==================== Callback Setters ====================

    /**
     * Sets the callback for back button press.
     */
    fun setOnBackPress(callback: (() -> Unit)?) {
        this.onBackPress = callback
    }

    /**
     * Sets the callback for conversation item click.
     */
    fun setOnConversationClick(callback: ((Conversation) -> Unit)?) {
        this.onConversationClick = callback
    }

    /**
     * Sets the callback for message item click.
     */
    fun setOnMessageClick(callback: ((BaseMessage) -> Unit)?) {
        this.onMessageClick = callback
    }

    /**
     * Sets the callback for errors.
     */
    fun setOnError(callback: ((CometChatException) -> Unit)?) {
        this.onError = callback
    }

    /**
     * Sets the callback when conversations are loaded.
     */
    fun setOnLoadConversations(callback: ((List<Conversation>) -> Unit)?) {
        this.onLoadConversations = callback
    }

    /**
     * Sets the callback when messages are loaded.
     */
    fun setOnLoadMessages(callback: ((List<BaseMessage>) -> Unit)?) {
        this.onLoadMessages = callback
    }

    /**
     * Sets the callback when search results are empty.
     */
    fun setOnEmpty(callback: (() -> Unit)?) {
        this.onEmpty = callback
    }

    // ==================== Additional Public API Methods ====================

    /**
     * Pre-selects a filter chip on load.
     *
     * If the given filter is null or not present in the current [searchFilters],
     * this method does nothing.
     *
     * @param filter The [SearchFilter] to be initially selected.
     */
    fun setInitialSearchFilter(filter: SearchFilter?) {
        if (filter == null) return

        if (searchFilters.isEmpty()) {
            searchFilters = getDefaultSearchFilters()
        }

        if (!searchFilters.contains(filter)) return

        clearSelectedFilters()
        toggleFilter(filter)

        refreshChipSelectionState()
        fetchBasedOnSelection()
    }

    /**
     * Sets the search scopes to determine which types of data to search.
     *
     * This is an alias for [setSearchScopes] to match the documented API name.
     *
     * @param scopes List of [SearchScope] enums indicating where to search.
     */
    fun setSearchIn(scopes: List<SearchScope>) {
        setSearchScopes(scopes)
    }

    /**
     * Sets the visibility of the empty state view.
     *
     * When visibility is not [View.VISIBLE], the empty state will be hidden.
     *
     * @param visibility The visibility state (e.g., [View.VISIBLE], [View.GONE]).
     */
    fun setEmptyStateVisibility(visibility: Int) {
        hideEmptyState = visibility != View.VISIBLE
    }

    /**
     * Sets the visibility of the error state view.
     *
     * When visibility is not [View.VISIBLE], the error state will be hidden.
     *
     * @param visibility The visibility state (e.g., [View.VISIBLE], [View.GONE]).
     */
    fun setErrorStateVisibility(visibility: Int) {
        hideErrorState = visibility != View.VISIBLE
    }

    /**
     * Sets a custom label for the "mention all" feature for a specific ID.
     *
     * If either parameter is null or empty, this method does nothing.
     *
     * @param id The unique identifier (such as a group or user ID) for which the mention-all label should be set.
     * @param mentionAllLabel The custom label to display when mentioning all members.
     */
    fun setMentionAllLabelId(id: String, mentionAllLabel: String) {
        if (id.isNotEmpty() && mentionAllLabel.isNotEmpty()) {
            this.mentionAllId = id
            this.mentionAllLabel = mentionAllLabel
            cometchatMentionsFormatter?.setMentionAllLabel(id, mentionAllLabel)
        }
    }

    /**
     * Sets the hint text for the search input field.
     *
     * @param text The hint text to display in the search bar.
     */
    fun setHintText(text: String) {
        binding.searchInput.hint = text
    }

    /**
     * Sets the text formatters to use for formatting messages in search results.
     *
     * The formatters are passed to both the conversations adapter and the messages adapter.
     *
     * @param formatters The list of [CometChatTextFormatter] instances to apply.
     */
    fun setTextFormatters(formatters: List<CometChatTextFormatter>) {
        textFormatters.clear()
        textFormatters.addAll(formatters)
        conversationsAdapter.setTextFormatters(textFormatters)
        messagesAdapter.setTextFormatters(textFormatters)
    }

    // ==================== Utility Methods ====================

    /**
     * Clears the search and resets to initial state.
     */
    fun clear() {
        binding.searchInput.text?.clear()
        currentSearchText = ""
        viewModel?.clear()
    }

    /**
     * Performs a search with the given text.
     */
    fun search(text: String) {
        binding.searchInput.setText(text)
        currentSearchText = text
        viewModel?.searchConversationsAndMessages(text, viewModel?.selectedFilters?.value ?: emptySet())
    }

    /**
     * Gets the current search text.
     */
    fun getSearchText(): String = currentSearchText

    /**
     * Gets the current selected filters.
     */
    fun getSelectedFilters(): Set<SearchFilter> = viewModel?.selectedFilters?.value ?: emptySet()

    /**
     * Gets the current conversations list.
     */
    fun getConversations(): List<Conversation> = viewModel?.conversations?.value ?: emptyList()

    /**
     * Gets the current messages list.
     */
    fun getMessages(): List<BaseMessage> = viewModel?.messages?.value ?: emptyList()

    // ==================== Programmatic Style Setters ====================

    /**
     * Helper to rebuild the style with a single property change and re-apply.
     */
    private fun updateStyleAndApply(block: CometChatSearchStyle.Builder.() -> Unit) {
        val builder = CometChatSearchStyle.Builder(context)
        // Copy existing style properties into the builder
        style?.let { s ->
            s.backgroundColor?.let { builder.setBackgroundColor(it) }
            s.searchBarBackgroundColor?.let { builder.setSearchBarBackgroundColor(it) }
            s.searchBarStrokeColor?.let { builder.setSearchBarStrokeColor(it) }
            s.searchBarStrokeWidth?.let { builder.setSearchBarStrokeWidth(it) }
            s.searchBarCornerRadius?.let { builder.setSearchBarCornerRadius(it) }
            s.searchBarTextColor?.let { builder.setSearchBarTextColor(it) }
            s.searchBarTextAppearance?.let { builder.setSearchBarTextAppearance(it) }
            s.searchBarHintTextColor?.let { builder.setSearchBarHintTextColor(it) }
            s.searchBarHintTextAppearance?.let { builder.setSearchBarHintTextAppearance(it) }
            builder.setBackIcon(s.backIcon)
            s.backIconTint?.let { builder.setBackIconTint(it) }
            builder.setClearIcon(s.clearIcon)
            s.clearIconTint?.let { builder.setClearIconTint(it) }
            builder.setSearchIcon(s.searchIcon)
            s.searchIconTint?.let { builder.setSearchIconTint(it) }
            s.filterChipBackgroundColor?.let { builder.setFilterChipBackgroundColor(it) }
            s.filterChipSelectedBackgroundColor?.let { builder.setFilterChipSelectedBackgroundColor(it) }
            s.filterChipTextColor?.let { builder.setFilterChipTextColor(it) }
            s.filterChipSelectedTextColor?.let { builder.setFilterChipSelectedTextColor(it) }
            s.filterChipTextAppearance?.let { builder.setFilterChipTextAppearance(it) }
            s.filterChipStrokeColor?.let { builder.setFilterChipStrokeColor(it) }
            s.filterChipSelectedStrokeColor?.let { builder.setFilterChipSelectedStrokeColor(it) }
            s.filterChipStrokeWidth?.let { builder.setFilterChipStrokeWidth(it) }
            s.filterChipCornerRadius?.let { builder.setFilterChipCornerRadius(it) }
            s.sectionHeaderTextColor?.let { builder.setSectionHeaderTextColor(it) }
            s.sectionHeaderTextAppearance?.let { builder.setSectionHeaderTextAppearance(it) }
            s.sectionHeaderBackgroundColor?.let { builder.setSectionHeaderBackgroundColor(it) }
            s.conversationItemBackgroundColor?.let { builder.setConversationItemBackgroundColor(it) }
            s.conversationTitleTextColor?.let { builder.setConversationTitleTextColor(it) }
            s.conversationTitleTextAppearance?.let { builder.setConversationTitleTextAppearance(it) }
            s.conversationSubtitleTextColor?.let { builder.setConversationSubtitleTextColor(it) }
            s.conversationSubtitleTextAppearance?.let { builder.setConversationSubtitleTextAppearance(it) }
            s.conversationTimestampTextColor?.let { builder.setConversationTimestampTextColor(it) }
            s.conversationTimestampTextAppearance?.let { builder.setConversationTimestampTextAppearance(it) }
            s.conversationSeparatorColor?.let { builder.setConversationSeparatorColor(it) }
            s.messageItemBackgroundColor?.let { builder.setMessageItemBackgroundColor(it) }
            s.messageTitleTextColor?.let { builder.setMessageTitleTextColor(it) }
            s.messageTitleTextAppearance?.let { builder.setMessageTitleTextAppearance(it) }
            s.messageSubtitleTextColor?.let { builder.setMessageSubtitleTextColor(it) }
            s.messageSubtitleTextAppearance?.let { builder.setMessageSubtitleTextAppearance(it) }
            s.messageTimestampTextColor?.let { builder.setMessageTimestampTextColor(it) }
            s.messageTimestampTextAppearance?.let { builder.setMessageTimestampTextAppearance(it) }
            s.messageSeparatorColor?.let { builder.setMessageSeparatorColor(it) }
            s.messageLinkTextColor?.let { builder.setMessageLinkTextColor(it) }
            s.messageLinkTextAppearance?.let { builder.setMessageLinkTextAppearance(it) }
            builder.setMessageThreadIcon(s.messageThreadIcon)
            s.messageThreadIconTint?.let { builder.setMessageThreadIconTint(it) }
            s.dateSeparatorBackgroundColor?.let { builder.setDateSeparatorBackgroundColor(it) }
            s.dateSeparatorTextColor?.let { builder.setDateSeparatorTextColor(it) }
            s.dateSeparatorTextAppearance?.let { builder.setDateSeparatorTextAppearance(it) }
            builder.setDateSeparatorStyle(s.dateSeparatorStyle)
            builder.setAvatarStyle(s.avatarStyle)
            builder.setBadgeStyle(s.badgeStyle)
            builder.setStatusIndicatorStyle(s.statusIndicatorStyle)
            s.loadingStateBackgroundColor?.let { builder.setLoadingStateBackgroundColor(it) }
            s.emptyStateTextColor?.let { builder.setEmptyStateTextColor(it) }
            s.emptyStateTextAppearance?.let { builder.setEmptyStateTextAppearance(it) }
            s.emptyStateSubtitleTextColor?.let { builder.setEmptyStateSubtitleTextColor(it) }
            s.emptyStateSubtitleTextAppearance?.let { builder.setEmptyStateSubtitleTextAppearance(it) }
            builder.setEmptyStateIcon(s.emptyStateIcon)
            s.emptyStateIconTint?.let { builder.setEmptyStateIconTint(it) }
            s.initialStateTextColor?.let { builder.setInitialStateTextColor(it) }
            s.initialStateTextAppearance?.let { builder.setInitialStateTextAppearance(it) }
            s.initialStateSubtitleTextColor?.let { builder.setInitialStateSubtitleTextColor(it) }
            s.initialStateSubtitleTextAppearance?.let { builder.setInitialStateSubtitleTextAppearance(it) }
            builder.setInitialStateIcon(s.initialStateIcon)
            s.initialStateIconTint?.let { builder.setInitialStateIconTint(it) }
            s.errorStateTextColor?.let { builder.setErrorStateTextColor(it) }
            s.errorStateTextAppearance?.let { builder.setErrorStateTextAppearance(it) }
            s.errorStateSubtitleTextColor?.let { builder.setErrorStateSubtitleTextColor(it) }
            s.errorStateSubtitleTextAppearance?.let { builder.setErrorStateSubtitleTextAppearance(it) }
            builder.setErrorStateIcon(s.errorStateIcon)
            s.errorStateIconTint?.let { builder.setErrorStateIconTint(it) }
            s.seeMoreTextColor?.let { builder.setSeeMoreTextColor(it) }
            s.seeMoreTextAppearance?.let { builder.setSeeMoreTextAppearance(it) }
        }
        // Apply the caller's modification
        builder.block()
        style = builder.build()
        applyStyle()
    }

    // --- Conversation Item Setters ---

    /** Sets the background color for conversation items. */
    fun setConversationItemBackgroundColor(@ColorInt color: Int) {
        updateStyleAndApply { setConversationItemBackgroundColor(color) }
    }

    /** Sets the text color for conversation titles. */
    fun setConversationTitleTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setConversationTitleTextColor(color) }
    }

    /** Sets the text appearance for conversation titles. */
    fun setConversationTitleTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setConversationTitleTextAppearance(appearance) }
    }

    /** Sets the text color for conversation subtitles. */
    fun setConversationSubtitleTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setConversationSubtitleTextColor(color) }
    }

    /** Sets the text appearance for conversation subtitles. */
    fun setConversationSubtitleTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setConversationSubtitleTextAppearance(appearance) }
    }

    /** Sets the text color for conversation timestamps. */
    fun setConversationTimestampTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setConversationTimestampTextColor(color) }
    }

    /** Sets the text appearance for conversation timestamps. */
    fun setConversationTimestampTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setConversationTimestampTextAppearance(appearance) }
    }

    // --- Message Item Setters ---

    /** Sets the background color for message items. */
    fun setMessageItemBackgroundColor(@ColorInt color: Int) {
        updateStyleAndApply { setMessageItemBackgroundColor(color) }
    }

    /** Sets the text color for message titles. */
    fun setMessageTitleTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setMessageTitleTextColor(color) }
    }

    /** Sets the text appearance for message titles. */
    fun setMessageTitleTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setMessageTitleTextAppearance(appearance) }
    }

    /** Sets the text color for message subtitles. */
    fun setMessageSubtitleTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setMessageSubtitleTextColor(color) }
    }

    /** Sets the text appearance for message subtitles. */
    fun setMessageSubtitleTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setMessageSubtitleTextAppearance(appearance) }
    }

    /** Sets the text color for message timestamps. */
    fun setMessageTimestampTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setMessageTimestampTextColor(color) }
    }

    /** Sets the text appearance for message timestamps. */
    fun setMessageTimestampTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setMessageTimestampTextAppearance(appearance) }
    }

    /** Sets the text color for message links. */
    fun setMessageLinkTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setMessageLinkTextColor(color) }
    }

    /** Sets the text appearance for message links. */
    fun setMessageLinkTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setMessageLinkTextAppearance(appearance) }
    }

    // --- Search Bar Setters ---

    /** Sets the background color of the search bar. */
    fun setSearchBarBackgroundColor(@ColorInt color: Int) {
        updateStyleAndApply { setSearchBarBackgroundColor(color) }
    }

    /** Sets the stroke width of the search bar. */
    fun setSearchBarStrokeWidth(width: Int) {
        updateStyleAndApply { setSearchBarStrokeWidth(width.toFloat()) }
    }

    /** Sets the stroke color of the search bar. */
    fun setSearchBarStrokeColor(@ColorInt color: Int) {
        updateStyleAndApply { setSearchBarStrokeColor(color) }
    }

    /** Sets the corner radius of the search bar. */
    fun setSearchBarCornerRadius(radius: Int) {
        updateStyleAndApply { setSearchBarCornerRadius(radius.toFloat()) }
    }

    /** Sets the text color of the search bar input. */
    fun setSearchBarTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setSearchBarTextColor(color) }
    }

    /** Sets the text appearance of the search bar input. */
    fun setSearchBarTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setSearchBarTextAppearance(appearance) }
    }

    /** Sets the hint text color of the search bar input. */
    fun setSearchBarHintTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setSearchBarHintTextColor(color) }
    }

    // --- Icon Setters ---

    /** Sets the back icon drawable. */
    fun setBackIcon(drawable: Drawable) {
        updateStyleAndApply { setBackIcon(drawable) }
    }

    /** Sets the back icon tint color. */
    fun setBackIconTint(@ColorInt color: Int) {
        updateStyleAndApply { setBackIconTint(color) }
    }

    /** Sets the clear icon drawable. */
    fun setClearIcon(drawable: Drawable) {
        updateStyleAndApply { setClearIcon(drawable) }
    }

    /** Sets the clear icon tint color. */
    fun setClearIconTint(@ColorInt color: Int) {
        updateStyleAndApply { setClearIconTint(color) }
    }

    /** Sets the search icon drawable. */
    fun setSearchIcon(drawable: Drawable) {
        updateStyleAndApply { setSearchIcon(drawable) }
    }

    /** Sets the search icon tint color. */
    fun setSearchIconTint(@ColorInt color: Int) {
        updateStyleAndApply { setSearchIconTint(color) }
    }

    // --- Section Header Setters ---

    /** Sets the text color for section headers. */
    fun setSectionHeaderTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setSectionHeaderTextColor(color) }
    }

    /** Sets the text appearance for section headers. */
    fun setSectionHeaderTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setSectionHeaderTextAppearance(appearance) }
    }

    /** Sets the background color for section headers. */
    fun setSectionHeaderBackgroundColor(@ColorInt color: Int) {
        updateStyleAndApply { setSectionHeaderBackgroundColor(color) }
    }

    // --- See More Setters ---

    /** Sets the text color for the "See More" button. */
    fun setSeeMoreTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setSeeMoreTextColor(color) }
    }

    /** Sets the text appearance for the "See More" button. */
    fun setSeeMoreTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setSeeMoreTextAppearance(appearance) }
    }

    // --- Date Separator Setters ---

    /** Sets the text color for date separators. */
    fun setDateSeparatorTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setDateSeparatorTextColor(color) }
    }

    /** Sets the text appearance for date separators. */
    fun setDateSeparatorTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setDateSeparatorTextAppearance(appearance) }
    }

    /** Sets the background color for date separators. */
    fun setDateSeparatorBackgroundColor(@ColorInt color: Int) {
        updateStyleAndApply { setDateSeparatorBackgroundColor(color) }
    }

    // --- Component Style Setters ---

    /** Sets the avatar style resource. */
    fun setAvatarStyle(@StyleRes style: Int) {
        // Store the style res for future use; avatar style in CometChatSearchStyle uses CometChatAvatarStyle object
        // Apply via adapter's style propagation
        conversationsAdapter.notifyDataSetChanged()
    }

    /** Sets the badge style resource. */
    fun setBadgeStyle(@StyleRes style: Int) {
        // Store the style res for future use; badge style in CometChatSearchStyle uses CometChatBadgeCountStyle object
        // Apply via adapter's style propagation
        conversationsAdapter.notifyDataSetChanged()
    }

    // --- Filter Chip Setters ---

    /** Sets the background color for unselected filter chips. */
    fun setFilterChipBackgroundColor(@ColorInt color: Int) {
        updateStyleAndApply { setFilterChipBackgroundColor(color) }
        refreshChipSelectionState()
    }

    /** Sets the background color for selected filter chips. */
    fun setFilterChipSelectedBackgroundColor(@ColorInt color: Int) {
        updateStyleAndApply { setFilterChipSelectedBackgroundColor(color) }
        refreshChipSelectionState()
    }

    /** Sets the text color for unselected filter chips. */
    fun setFilterChipTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setFilterChipTextColor(color) }
        refreshChipSelectionState()
    }

    /** Sets the text color for selected filter chips. */
    fun setFilterChipSelectedTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setFilterChipSelectedTextColor(color) }
        refreshChipSelectionState()
    }

    /** Sets the text appearance for filter chips. */
    fun setFilterChipTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setFilterChipTextAppearance(appearance) }
        refreshChipSelectionState()
    }

    /** Sets the stroke color for unselected filter chips. */
    fun setFilterChipStrokeColor(@ColorInt color: Int) {
        updateStyleAndApply { setFilterChipStrokeColor(color) }
        refreshChipSelectionState()
    }

    /** Sets the stroke color for selected filter chips. */
    fun setFilterChipSelectedStrokeColor(@ColorInt color: Int) {
        updateStyleAndApply { setFilterChipSelectedStrokeColor(color) }
        refreshChipSelectionState()
    }

    /** Sets the stroke width for filter chips. */
    fun setFilterChipStrokeWidth(width: Int) {
        updateStyleAndApply { setFilterChipStrokeWidth(width.toFloat()) }
        refreshChipSelectionState()
    }

    /** Sets the corner radius for filter chips. */
    fun setFilterChipCornerRadius(radius: Float) {
        updateStyleAndApply { setFilterChipCornerRadius(radius) }
        refreshChipSelectionState()
    }

    // --- Empty State Setters ---

    /** Sets the text color for the empty state title. */
    fun setEmptyStateTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setEmptyStateTextColor(color) }
    }

    /** Sets the text appearance for the empty state title. */
    fun setEmptyStateTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setEmptyStateTextAppearance(appearance) }
    }

    /** Sets the text color for the empty state subtitle. */
    fun setEmptyStateSubtitleTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setEmptyStateSubtitleTextColor(color) }
    }

    /** Sets the text appearance for the empty state subtitle. */
    fun setEmptyStateSubtitleTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setEmptyStateSubtitleTextAppearance(appearance) }
    }

    /** Sets the icon for the empty state. */
    fun setEmptyStateIcon(drawable: Drawable) {
        updateStyleAndApply { setEmptyStateIcon(drawable) }
    }

    /** Sets the icon tint for the empty state. */
    fun setEmptyStateIconTint(@ColorInt color: Int) {
        updateStyleAndApply { setEmptyStateIconTint(color) }
    }

    // --- Error State Setters ---

    /** Sets the text color for the error state title. */
    fun setErrorStateTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setErrorStateTextColor(color) }
    }

    /** Sets the text appearance for the error state title. */
    fun setErrorStateTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setErrorStateTextAppearance(appearance) }
    }

    /** Sets the text color for the error state subtitle. */
    fun setErrorStateSubtitleTextColor(@ColorInt color: Int) {
        updateStyleAndApply { setErrorStateSubtitleTextColor(color) }
    }

    /** Sets the text appearance for the error state subtitle. */
    fun setErrorStateSubtitleTextAppearance(@StyleRes appearance: Int) {
        updateStyleAndApply { setErrorStateSubtitleTextAppearance(appearance) }
    }

    /** Sets the icon for the error state. */
    fun setErrorStateIcon(drawable: Drawable) {
        updateStyleAndApply { setErrorStateIcon(drawable) }
    }

    /** Sets the icon tint for the error state. */
    fun setErrorStateIconTint(@ColorInt color: Int) {
        updateStyleAndApply { setErrorStateIconTint(color) }
    }

    // ==================== Reference API Name Aliases ====================

    /** Alias for [setConversationItemViewListener] matching the reference API name. */
    fun setConversationItemView(listener: SearchConversationsViewHolderListener?) {
        setConversationItemViewListener(listener)
    }

    /** Alias for [setTextMessageItemViewListener] matching the reference API name. */
    fun setTextMessageItemView(listener: SearchMessagesViewHolderListener<TextMessage>?) {
        setTextMessageItemViewListener(listener)
    }

    /** Alias for [setImageMessageItemViewListener] matching the reference API name. */
    fun setImageMessageItemView(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        setImageMessageItemViewListener(listener)
    }

    /** Alias for [setVideoMessageItemViewListener] matching the reference API name. */
    fun setVideoMessageItemView(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        setVideoMessageItemViewListener(listener)
    }

    /** Alias for [setAudioMessageItemViewListener] matching the reference API name. */
    fun setAudioMessageItemView(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        setAudioMessageItemViewListener(listener)
    }

    /** Alias for [setDocumentMessageItemViewListener] matching the reference API name. */
    fun setDocumentMessageItemView(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        setDocumentMessageItemViewListener(listener)
    }

    /** Alias for [setLinkMessageItemViewListener] matching the reference API name. */
    fun setLinkMessageItemView(listener: SearchMessagesViewHolderListener<TextMessage>?) {
        setLinkMessageItemViewListener(listener)
    }

    /** Alias for [setOnConversationClick] matching the reference API name. */
    fun setOnConversationClicked(callback: ((Conversation) -> Unit)?) {
        setOnConversationClick(callback)
    }

    /** Alias for [setOnMessageClick] matching the reference API name. */
    fun setOnMessageClicked(callback: ((BaseMessage) -> Unit)?) {
        setOnMessageClick(callback)
    }

    /** Alias for [setOnBackPress] matching the reference API name. */
    fun setOnBackPressListener(callback: (() -> Unit)?) {
        setOnBackPress(callback)
    }

    // ==================== @LayoutRes Overloads ====================

    /**
     * Sets a custom initial view from a layout resource.
     * @param layoutRes The layout resource ID to inflate.
     */
    fun setInitialView(@LayoutRes layoutRes: Int) {
        if (layoutRes != 0) {
            try {
                val view = LayoutInflater.from(context).inflate(layoutRes, this, false)
                setInitialView(view)
            } catch (e: Exception) {
                // Silently ignore invalid layout resources
            }
        }
    }

    /**
     * Sets a custom empty view from a layout resource.
     * @param layoutRes The layout resource ID to inflate.
     */
    fun setEmptyView(@LayoutRes layoutRes: Int) {
        if (layoutRes != 0) {
            try {
                val view = LayoutInflater.from(context).inflate(layoutRes, this, false)
                setEmptyView(view)
            } catch (e: Exception) {
                // Silently ignore invalid layout resources
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope?.cancel()
        binding.shimmerEffectFrame.stopShimmer()
    }
}
