package com.cometchat.uikit.kotlin.presentation.shared.suggestionlist

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatSuggestionListBinding
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.kotlin.shared.formatters.SuggestionItem
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.shared.resources.utils.itemclicklistener.OnItemClickListener
import com.cometchat.uikit.kotlin.shared.resources.utils.recycler_touch.ClickListener
import com.cometchat.uikit.kotlin.shared.resources.utils.recycler_touch.RecyclerTouchListener
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView

/**
 * CometChatSuggestionList is a custom view that displays a list of suggestions
 * for mentions or other autocomplete features.
 * 
 * Features:
 * - Shimmer loading state while fetching suggestions
 * - Item click handling for selection
 * - Max height limit to prevent taking too much screen space
 * - Custom item view support
 * - Scroll to bottom detection for pagination
 *
 * ## XML Usage
 *
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.shared.suggestionlist.CometChatSuggestionList
 *     android:id="@+id/suggestionList"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:cometchatSuggestionListStyle="@style/CometChatSuggestionListStyle" />
 * ```
 *
 * ## Programmatic Usage
 *
 * ```kotlin
 * val suggestionList = CometChatSuggestionList(context)
 *
 * // Configure appearance
 * suggestionList.setMaxHeightLimit(250.dp)
 * suggestionList.showAvatar(true)
 * suggestionList.setSuggestionListBackgroundColor(Color.WHITE)
 * suggestionList.setSuggestionListCornerRadius(8.dp)
 *
 * // Set listeners
 * suggestionList.setItemClickListener(object : OnItemClickListener<SuggestionItem> {
 *     override fun OnItemClick(item: SuggestionItem, position: Int) {
 *         // Handle selection
 *     }
 *
 *     override fun OnItemLongClick(item: SuggestionItem, position: Int) {
 *         // Handle long press
 *     }
 * })
 *
 * suggestionList.setOnScrollToBottomListener {
 *     // Load more suggestions (pagination)
 * }
 *
 * // Update suggestions
 * suggestionList.setList(suggestions)
 *
 * // Show/hide loading state
 * suggestionList.showShimmer(true)
 * ```
 *
 * ## Style Configuration
 *
 * ```kotlin
 * val style = CometChatSuggestionListStyle(
 *     backgroundColor = Color.WHITE,
 *     strokeColor = Color.LTGRAY,
 *     strokeWidth = 1.dp,
 *     cornerRadius = 8.dp,
 *     maxHeight = 250.dp,
 *     itemTextColor = Color.BLACK,
 *     itemInfoTextColor = Color.GRAY
 * )
 * suggestionList.setStyle(style)
 * ```
 *
 * @see CometChatSuggestionListStyle Style configuration class
 * @see SuggestionItem Data model for suggestion items
 * @see SuggestionListAdapter Adapter for rendering items
 */
class CometChatSuggestionList @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatSuggestionListStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatSuggestionList::class.java.simpleName
    }

    // View Binding
    private val binding: CometchatSuggestionListBinding

    // Adapter
    private val suggestionListAdapter: SuggestionListAdapter

    // Callbacks
    private var onItemClickListener: OnItemClickListener<SuggestionItem>? = null
    private var onScrollToBottomListener: (() -> Unit)? = null

    // Configuration
    private var showAvatar: Boolean = true
    @Dimension private var maxHeightLimit: Int = 0

    // Style properties
    @ColorInt private var suggestionListBackgroundColor: Int = 0
    @ColorInt private var suggestionListStrokeColor: Int = 0
    @Dimension private var suggestionListStrokeWidth: Int = 0
    @Dimension private var suggestionListCornerRadius: Int = 0
    @StyleRes private var suggestionListItemAvatarStyle: Int = 0
    @StyleRes private var suggestionListItemTextAppearance: Int = 0
    @ColorInt private var suggestionListItemTextColor: Int = 0
    @StyleRes private var suggestionListItemInfoTextAppearance: Int = 0
    @ColorInt private var suggestionListItemInfoTextColor: Int = 0

    init {
        binding = CometchatSuggestionListBinding.inflate(
            LayoutInflater.from(context), this, true
        )
        Utils.initMaterialCard(this)
        
        suggestionListAdapter = SuggestionListAdapter(context)
        binding.recyclerViewSuggestionList.adapter = suggestionListAdapter
        
        // Disable item animator to prevent flicker on list updates
        binding.recyclerViewSuggestionList.itemAnimator = null
        
        applyStyleAttributes(attrs, defStyleAttr)
        setupRecyclerViewListeners()
    }

    /**
     * Applies style attributes from XML.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        val directAttributes = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatSuggestionList, defStyleAttr, 0
        )
        
        @StyleRes val styleResId = directAttributes.getResourceId(
            R.styleable.CometChatSuggestionList_cometchatSuggestionListStyle, 0
        )
        
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatSuggestionList, defStyleAttr, styleResId
        )
        
        extractAttributesAndApplyDefaults(typedArray)
    }

    /**
     * Extracts style attributes and applies defaults.
     */
    private fun extractAttributesAndApplyDefaults(typedArray: TypedArray) {
        try {
            suggestionListBackgroundColor = typedArray.getColor(
                R.styleable.CometChatSuggestionList_cometchatSuggestionListBackgroundColor,
                CometChatTheme.getBackgroundColor1(context)
            )
            suggestionListStrokeColor = typedArray.getColor(
                R.styleable.CometChatSuggestionList_cometchatSuggestionListStrokeColor,
                CometChatTheme.getBorderColorLight(context)
            )
            suggestionListStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatSuggestionList_cometchatSuggestionListStrokeWidth,
                resources.getDimensionPixelSize(R.dimen.cometchat_1dp)
            )
            suggestionListCornerRadius = typedArray.getDimensionPixelSize(
                R.styleable.CometChatSuggestionList_cometchatSuggestionListCornerRadius,
                resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_2)
            )
            suggestionListItemAvatarStyle = typedArray.getResourceId(
                R.styleable.CometChatSuggestionList_cometchatSuggestionListItemAvatarStyle,
                R.style.CometChatAvatarStyle
            )
            suggestionListItemTextAppearance = typedArray.getResourceId(
                R.styleable.CometChatSuggestionList_cometchatSuggestionListItemTextAppearance,
                R.style.CometChatTextAppearanceHeading4_Medium
            )
            suggestionListItemTextColor = typedArray.getColor(
                R.styleable.CometChatSuggestionList_cometchatSuggestionListItemTextColor,
                CometChatTheme.getTextColorPrimary(context)
            )
            suggestionListItemInfoTextAppearance = typedArray.getResourceId(
                R.styleable.CometChatSuggestionList_cometchatSuggestionListItemInfoTextAppearance,
                R.style.CometChatTextAppearanceBody_Regular
            )
            suggestionListItemInfoTextColor = typedArray.getColor(
                R.styleable.CometChatSuggestionList_cometchatSuggestionListItemInfoTextColor,
                CometChatTheme.getTextColorSecondary(context)
            )
            
            updateUI()
        } finally {
            typedArray.recycle()
        }
    }

    /**
     * Updates the UI based on style properties.
     */
    private fun updateUI() {
        setCardBackgroundColor(suggestionListBackgroundColor)
        setStrokeColor(suggestionListStrokeColor)
        strokeWidth = suggestionListStrokeWidth
        radius = suggestionListCornerRadius.toFloat()
        
        suggestionListAdapter.setItemAvatarStyle(suggestionListItemAvatarStyle)
        suggestionListAdapter.setItemTextAppearance(suggestionListItemTextAppearance)
        suggestionListAdapter.setItemTextColor(suggestionListItemTextColor)
        suggestionListAdapter.setItemInfoTextAppearance(suggestionListItemInfoTextAppearance)
        suggestionListAdapter.setItemInfoTextColor(suggestionListItemInfoTextColor)
    }

    /**
     * Sets up RecyclerView listeners for item clicks and scroll detection.
     */
    private fun setupRecyclerViewListeners() {
        // Item click listener
        binding.recyclerViewSuggestionList.addOnItemTouchListener(
            RecyclerTouchListener(context, binding.recyclerViewSuggestionList, object : ClickListener {
                override fun onClick(view: View, position: Int) {
                    val item = view.getTag(R.string.cometchat_tag_item) as? SuggestionItem
                    item?.let { onItemClickListener?.OnItemClick(it, position) }
                }

                override fun onLongClick(view: View, position: Int) {
                    val item = view.getTag(R.string.cometchat_tag_item) as? SuggestionItem
                    item?.let { onItemClickListener?.OnItemLongClick(it, position) }
                }
            })
        )

        // Scroll listener for pagination
        binding.recyclerViewSuggestionList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    onScrollToBottomListener?.invoke()
                }
            }
        })
    }

    /**
     * Measures the view, enforcing max height limit if set.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var newHeightSpec = heightMeasureSpec
        if (maxHeightLimit > 0) {
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
            if (heightSize > maxHeightLimit) {
                val heightMode = MeasureSpec.getMode(heightMeasureSpec)
                newHeightSpec = MeasureSpec.makeMeasureSpec(maxHeightLimit, heightMode)
            }
        }
        super.onMeasure(widthMeasureSpec, newHeightSpec)
    }

    // ==================== Public API ====================

    /**
     * Sets the style using a style resource.
     */
    fun setStyle(@StyleRes style: Int) {
        if (style != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                style, R.styleable.CometChatSuggestionList
            )
            extractAttributesAndApplyDefaults(typedArray)
        }
    }

    /**
     * Sets the style using a CometChatSuggestionListStyle object.
     */
    fun setStyle(style: CometChatSuggestionListStyle) {
        suggestionListBackgroundColor = style.backgroundColor
        suggestionListStrokeColor = style.strokeColor
        suggestionListStrokeWidth = style.strokeWidth
        suggestionListCornerRadius = style.cornerRadius
        maxHeightLimit = style.maxHeight
        suggestionListItemAvatarStyle = style.itemAvatarStyle
        suggestionListItemTextAppearance = style.itemTextAppearance
        suggestionListItemTextColor = style.itemTextColor
        suggestionListItemInfoTextAppearance = style.itemInfoTextAppearance
        suggestionListItemInfoTextColor = style.itemInfoTextColor
        updateUI()
    }

    /**
     * Updates the suggestion list with new items.
     */
    fun setList(items: List<SuggestionItem>) {
        android.util.Log.d("MentionDebug", "[$TAG] setList() - received ${items.size} items: ${items.map { it.name }}")
        if (items.isNotEmpty()) {
            android.util.Log.d("MentionDebug", "[$TAG] setList() - hiding shimmer (items not empty)")
            showShimmer(false)
        }
        android.util.Log.d("MentionDebug", "[$TAG] setList() - calling adapter.updateList()")
        suggestionListAdapter.updateList(items)
    }

    /**
     * Shows or hides the shimmer loading state.
     */
    fun showShimmer(show: Boolean) {
        if (show) {
            // Use 1 shimmer item like Java implementation
            val shimmerAdapter = CometChatShimmerAdapter(
                1, R.layout.shimmer_cometchat_suggestion_list_items
            )
            binding.shimmerRecyclerView.adapter = shimmerAdapter
            binding.shimmerEffectFrame.setShimmer(
                CometChatShimmerUtils.getCometChatShimmerConfig(context)
            )
            binding.shimmerEffectFrame.startShimmer()
        } else {
            binding.shimmerEffectFrame.stopShimmer()
        }
        binding.recyclerViewSuggestionList.isVisible = !show
        binding.shimmerEffectFrame.isVisible = show
    }

    /**
     * Sets whether to show avatars in suggestion items.
     */
    fun showAvatar(show: Boolean) {
        showAvatar = show
        suggestionListAdapter.showAvatar(show)
    }

    /**
     * Sets the maximum height limit for the suggestion list.
     */
    fun setMaxHeightLimit(@Dimension maxHeight: Int) {
        if (maxHeight > 0) {
            maxHeightLimit = maxHeight
            requestLayout()
        }
    }

    /**
     * Sets the item click listener.
     */
    fun setItemClickListener(listener: OnItemClickListener<SuggestionItem>?) {
        onItemClickListener = listener
    }

    /**
     * Sets the scroll to bottom listener for pagination.
     */
    fun setOnScrollToBottomListener(listener: (() -> Unit)?) {
        onScrollToBottomListener = listener
    }

    /**
     * Sets a custom view holder listener for custom item views.
     */
    fun setListItemView(listener: SuggestionListViewHolderListener?) {
        suggestionListAdapter.setViewHolderListener(listener)
    }

    // ==================== Style Setters ====================

    fun setSuggestionListBackgroundColor(@ColorInt color: Int) {
        suggestionListBackgroundColor = color
        setCardBackgroundColor(color)
    }

    fun setSuggestionListStrokeColor(@ColorInt color: Int) {
        suggestionListStrokeColor = color
        setStrokeColor(color)
    }

    fun setSuggestionListStrokeWidth(@Dimension width: Int) {
        suggestionListStrokeWidth = width
        strokeWidth = width
    }

    fun setSuggestionListCornerRadius(@Dimension radius: Int) {
        suggestionListCornerRadius = radius
        this.radius = radius.toFloat()
    }

    fun setSuggestionListItemAvatarStyle(@StyleRes style: Int) {
        suggestionListItemAvatarStyle = style
        suggestionListAdapter.setItemAvatarStyle(style)
    }

    fun setSuggestionListItemTextAppearance(@StyleRes style: Int) {
        suggestionListItemTextAppearance = style
        suggestionListAdapter.setItemTextAppearance(style)
    }

    fun setSuggestionListItemTextColor(@ColorInt color: Int) {
        suggestionListItemTextColor = color
        suggestionListAdapter.setItemTextColor(color)
    }

    fun setSuggestionListItemInfoTextAppearance(@StyleRes style: Int) {
        suggestionListItemInfoTextAppearance = style
        suggestionListAdapter.setItemInfoTextAppearance(style)
    }

    fun setSuggestionListItemInfoTextColor(@ColorInt color: Int) {
        suggestionListItemInfoTextColor = color
        suggestionListAdapter.setItemInfoTextColor(color)
    }

    // ==================== Getters ====================

    fun getSuggestionListBackgroundColor(): Int = suggestionListBackgroundColor
    fun getSuggestionListStrokeColor(): Int = suggestionListStrokeColor
    fun getSuggestionListStrokeWidth(): Int = suggestionListStrokeWidth
    fun getSuggestionListCornerRadius(): Int = suggestionListCornerRadius
    fun getSuggestionListItemAvatarStyle(): Int = suggestionListItemAvatarStyle
    fun getSuggestionListItemTextAppearance(): Int = suggestionListItemTextAppearance
    fun getSuggestionListItemTextColor(): Int = suggestionListItemTextColor
    fun getSuggestionListItemInfoTextAppearance(): Int = suggestionListItemInfoTextAppearance
    fun getSuggestionListItemInfoTextColor(): Int = suggestionListItemInfoTextColor

    /**
     * Returns the binding for advanced customization.
     */
    fun getBinding(): CometchatSuggestionListBinding = binding
}
